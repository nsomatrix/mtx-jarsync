import JSZip from 'jszip';
import { DataReader, DataWriter } from './DataStream';
import { ClassPatcher } from './ClassPatcher';
import { ClassBuilder } from './ClassBuilder';

export interface JarMetadata {
  name: string;
  vendor: string;
  version: string;
  iconDataUrl?: string;
  iconFileName?: string;
  midlet1Class?: string;
  midlet2Class?: string;
  fileSize: number;
  originalFileName: string;
  manifestProperties: Map<string, string>;
}

export interface ReplicateOptions {
  count: number; // e.g. 1 to 26
  displayNames: string[]; // custom display name per replica
  appName: string;
  displayName: string;
  author: string;
  featureScreenshot: boolean;
  featureList: boolean;
  featureMainPageList: boolean;
  featureNoExit: boolean;
}

export interface SplitResult {
  fileName: string;
  blob: Blob;
  metadata: {
    name: string;
    vendor: string;
    version: string;
    fileSize: number;
  };
}

export class JarProcessor {
  /**
   * Inspects a JAR File and extracts metadata, icon, manifest properties, and MIDlet classes.
   */
  public static async inspectJar(file: File): Promise<{ metadata: JarMetadata; zip: JSZip }> {
    const arrayBuffer = await file.arrayBuffer();
    const zip = await JSZip.loadAsync(arrayBuffer);

    let manifestText = '';
    const manifestFile = zip.file('META-INF/MANIFEST.MF');
    if (manifestFile) {
      manifestText = await manifestFile.async('text');
    }

    const props = JarProcessor.parseManifest(manifestText);
    const midlet1 = props.get('MIDlet-1') || '';
    const midlet2 = props.get('MIDlet-2') || '';
    const name = props.get('MIDlet-Name') || file.name.replace(/\.[^/.]+$/, '');
    const vendor = props.get('MIDlet-Vendor') || 'Unknown Vendor';
    const version = props.get('MIDlet-Version') || '1.0.0';

    let iconFileName = '';
    let iconDataUrl: string | undefined;

    if (midlet1) {
      const parts = midlet1.split(',');
      if (parts.length >= 2) {
        let rawIcon = parts[1].trim();
        if (rawIcon.startsWith('/')) rawIcon = rawIcon.substring(1);
        iconFileName = rawIcon;

        const iconFile = zip.file(rawIcon);
        if (iconFile) {
          const iconBlob = await iconFile.async('blob');
          iconDataUrl = await JarProcessor.blobToDataUrl(iconBlob);
        }
      }
    }

    if (!iconDataUrl) {
      const fallbackIcon = zip.file('icon.png') || zip.file('i.png');
      if (fallbackIcon) {
        const iconBlob = await fallbackIcon.async('blob');
        iconDataUrl = await JarProcessor.blobToDataUrl(iconBlob);
      }
    }

    return {
      metadata: {
        name,
        vendor,
        version,
        iconDataUrl,
        iconFileName,
        midlet1Class: midlet1 ? midlet1.split(',')[2]?.trim() : undefined,
        midlet2Class: midlet2 ? midlet2.split(',')[2]?.trim() : undefined,
        fileSize: file.size,
        originalFileName: file.name,
        manifestProperties: props
      },
      zip
    };
  }

  /**
   * Replicates a single J2ME JAR N times (e.g. 2 to 26 accounts/clones)
   * into a single multi-session JAR with full state and bytecode isolation,
   * static initializer cloning, and in-game switching overlay.
   */
  public static async replicateJar(
    file: File,
    options: ReplicateOptions,
    onProgress?: (progress: number, status: string) => void
  ): Promise<Blob> {
    const count = Math.min(26, Math.max(1, options.count));
    const totalSteps = count * 2 + 5;
    let completedSteps = 0;

    const report = (status: string) => {
      completedSteps++;
      const pct = Math.min(99, Math.round((completedSteps / totalSteps) * 100));
      onProgress?.(pct, status);
    };

    report('Inspecting source JAR...');
    const { metadata, zip } = await JarProcessor.inspectJar(file);

    const outZip = new JSZip();
    const combinedMidletEntries: string[] = [];

    // 1. Load runtime overlay archives
    report('Loading runtime overlay binaries...');
    const [coreBytes, switcherBytes] = await Promise.all([
      fetch('/runtime/mtx-core.jar').then(r => r.arrayBuffer()),
      fetch(options.featureScreenshot ? '/runtime/mtx-switcher-ss.jar' : '/runtime/mtx-switcher.jar').then(r => r.arrayBuffer())
    ]);

    // Copy core runtime classes
    const coreZip = await JSZip.loadAsync(coreBytes);
    for (const [relativePath, fileObj] of Object.entries(coreZip.files)) {
      if (!fileObj.dir && relativePath.endsWith('.class')) {
        if ((options.featureList || options.featureMainPageList) && relativePath.endsWith('List.class')) {
          continue;
        }
        const content = await fileObj.async('uint8array');
        outZip.file(relativePath, content);
      }
    }

    // Copy switcher runtime classes
    const switcherZip = await JSZip.loadAsync(switcherBytes);
    for (const [relativePath, fileObj] of Object.entries(switcherZip.files)) {
      if (!fileObj.dir && relativePath.endsWith('.class')) {
        if (!options.featureList && !options.featureMainPageList && relativePath.endsWith('List.class')) {
          continue;
        }
        const content = await fileObj.async('uint8array');
        if (options.featureMainPageList && relativePath.endsWith('MtxSwitcher.class')) {
          outZip.file(relativePath, JarProcessor.patchSwitcherClassListRef(content));
        } else {
          outZip.file(relativePath, content);
        }
      }
    }

    // Default icon
    const iconRes = await fetch('/runtime/icon.png').then(r => r.arrayBuffer());
    outZip.file('mtx/mid.png', new Uint8Array(iconRes));

    // 2. Prepare sub-app identifiers
    const timestamp = Date.now();
    const iniName = `${timestamp}.ini`;
    const pngName = `${timestamp}.png`;

    // Save icon under timestamp.png
    if (metadata.iconFileName && zip.file(metadata.iconFileName)) {
      const iconData = await zip.file(metadata.iconFileName)!.async('uint8array');
      outZip.file(pngName, iconData);
    } else {
      outZip.file(pngName, new Uint8Array(iconRes));
    }

    // Write original manifest properties to timestamp.ini
    let iniContent = '';
    const manifestFile = zip.file('META-INF/MANIFEST.MF');
    if (manifestFile) {
      const mfText = await manifestFile.async('text');
      iniContent = mfText.replace(/\r?\n/g, '\r\n');
    }
    outZip.file(iniName, iniContent);

    // Extract raw class files and resources
    const classFiles: { path: string; data: Uint8Array }[] = [];
    const resourceFiles: { path: string; data: Uint8Array }[] = [];

    for (const [path, zipEntry] of Object.entries(zip.files)) {
      if (zipEntry.dir || path.startsWith('META-INF/')) continue;
      const data = await zipEntry.async('uint8array');
      if (path.endsWith('.class')) {
        classFiles.push({ path, data });
      } else {
        resourceFiles.push({ path, data });
      }
    }

    const midlet1Target = metadata.midlet1Class || 'Main';

    // 3. Replicate the class files across instances
    for (let i = 0; i < count; i++) {
      const prefix = i === 0 ? '' : `${String.fromCharCode(97 + i)}/`;
      const prefixChar = i === 0 ? 'a' : String.fromCharCode(97 + i);
      const titleName = options.displayNames[i] || `${metadata.name} ${i + 1}`;
      const qualifiedClass = i === 0 ? midlet1Target : `${String.fromCharCode(97 + i)}.${midlet1Target}`;

      combinedMidletEntries.push(`${titleName},/${pngName},${qualifiedClass},/${iniName}`);

      report(`Synthesizing clone #${i + 1} (prefix: '${prefix || "root"}')...`);

      // Initialize class replacements mapping for this clone
      const replacements = ClassPatcher.initClassReplacements();
      for (const cf of classFiles) {
        const cNameNoExt = cf.path.substring(0, cf.path.length - 6);
        replacements.set(cNameNoExt, prefix + cNameNoExt);
      }

      // Patch and write each class
      for (const cf of classFiles) {
        const patchedBytes = JarProcessor.patchClassBytes(cf.data, prefixChar);
        outZip.file(prefix + cf.path, patchedBytes);
      }

      // Generate Static.class for this clone
      const staticClassName = prefix.length > 0 ? `${prefixChar}/Static` : 'Static';
      const staticClassBytes = ClassBuilder.buildClass(ClassPatcher.patchedClasses, staticClassName);
      if (staticClassBytes) {
        outZip.file(`${staticClassName}.class`, staticClassBytes);
      }
    }

    // 4. Copy shared resources once to root
    report('Packaging static game resources...');
    for (const rf of resourceFiles) {
      outZip.file(rf.path, rf.data);
    }

    // 5. Generate Master Manifest
    report('Writing master manifest...');
    let mf = 'Manifest-Version: 1.0\r\n';
    mf += 'MicroEdition-Configuration: CLDC-1.1\r\n';
    mf += 'MicroEdition-Profile: MIDP-2.0\r\n';
    mf += 'MIDlet-Version: 2.5\r\n';
    mf += `MIDlet-Vendor: ${options.author || metadata.vendor || 'MTX'}\r\n`;
    mf += `MIDlet-Name: ${options.appName || `${metadata.name} Multi`}\r\n`;
    mf += `MIDlet-1: ${options.displayName || options.appName || `${metadata.name} Multi`},/mtx/mid.png,mtx.jarsync.runtime.MtxSwitcher\r\n`;
    if (options.featureNoExit) {
      mf += 'Nokia-MIDlet-no-exit: true\r\n';
    }
    for (let i = 0; i < combinedMidletEntries.length; i++) {
      mf += `${i}: ${combinedMidletEntries[i]}\r\n`;
    }
    mf += '\r\n';

    outZip.file('META-INF/MANIFEST.MF', mf);

    report('Compressing final JAR...');
    const resultBlob = await outZip.generateAsync({
      type: 'blob',
      compression: 'DEFLATE',
      compressionOptions: { level: 9 }
    });

    onProgress?.(100, 'Replication Complete');
    return resultBlob;
  }

  /**
   * Splits a multi-session replicated JAR back into standalone single-app JARs.
   */
  public static async splitJar(
    file: File,
    onProgress?: (progress: number, status: string) => void
  ): Promise<SplitResult[]> {
    onProgress?.(10, 'Reading archive manifest...');
    const arrayBuffer = await file.arrayBuffer();
    const zip = await JSZip.loadAsync(arrayBuffer);

    const manifestFile = zip.file('META-INF/MANIFEST.MF');
    if (!manifestFile) {
      throw new Error('Invalid JAR: Missing META-INF/MANIFEST.MF');
    }

    const manifestText = await manifestFile.async('text');
    const lines = manifestText.split(/\r?\n/);
    const subAppEntries: { index: number; title: string; icon: string; className: string; iniFile: string }[] = [];

    for (const line of lines) {
      const match = line.match(/^(\d+):\s*(.+)$/);
      if (match) {
        const parts = match[2].split(',');
        if (parts.length >= 4) {
          subAppEntries.push({
            index: parseInt(match[1]),
            title: parts[0].trim(),
            icon: parts[1].trim().replace(/^\//, ''),
            className: parts[2].trim(),
            iniFile: parts[3].trim().replace(/^\//, '')
          });
        }
      }
    }

    if (subAppEntries.length === 0) {
      throw new Error('No replicated sub-app entries found in this JAR. Make sure this is a multi-MIDlet archive.');
    }

    const results: SplitResult[] = [];
    const stepWeight = 85 / subAppEntries.length;

    for (let idx = 0; idx < subAppEntries.length; idx++) {
      const entry = subAppEntries[idx];
      const curPct = Math.round(10 + idx * stepWeight);
      onProgress?.(curPct, `Extracting ${entry.title}...`);

      const singleZip = new JSZip();
      const prefix = idx === 0 ? '' : `${String.fromCharCode(97 + idx)}/`;
      const dotPrefix = idx === 0 ? '' : `${String.fromCharCode(97 + idx)}.`;

      let standaloneManifest = '';
      const iniFileObj = zip.file(entry.iniFile);
      if (iniFileObj) {
        standaloneManifest = await iniFileObj.async('text');
      }

      if (!standaloneManifest || !standaloneManifest.includes('MIDlet-1:')) {
        const cleanClass = entry.className.startsWith(dotPrefix)
          ? entry.className.substring(dotPrefix.length)
          : entry.className;

        standaloneManifest = [
          'Manifest-Version: 1.0',
          'MicroEdition-Configuration: CLDC-1.1',
          'MicroEdition-Profile: MIDP-2.0',
          'MIDlet-Version: 1.0.0',
          `MIDlet-Name: ${entry.title}`,
          `MIDlet-Vendor: MTX JarSync`,
          `MIDlet-1: ${entry.title},/icon.png,${cleanClass}`,
          ''
        ].join('\r\n');
      }

      singleZip.file('META-INF/MANIFEST.MF', standaloneManifest);

      // Extract icon
      const iconObj = zip.file(entry.icon) || zip.file('icon.png');
      if (iconObj) {
        const iconData = await iconObj.async('uint8array');
        singleZip.file('icon.png', iconData);
      }

      // Copy classes (stripping clone prefix) and resources
      zip.forEach((path, fileObj) => {
        if (fileObj.dir || path.startsWith('META-INF/')) return;
        if (path.startsWith('lib/') || path.startsWith('mtx/') || path.startsWith('MrQuyet/')) return;
        if (path.endsWith('.ini')) return;

        if (idx === 0) {
          const isOtherPrefixed = /^[b-z]\//.test(path);
          if (!isOtherPrefixed && path !== 'Static.class') {
            singleZip.file(path, fileObj.async('uint8array'));
          }
        } else {
          if (path.startsWith(prefix)) {
            const unPrefixed = path.substring(prefix.length);
            if (unPrefixed !== 'Static.class') {
              singleZip.file(unPrefixed, fileObj.async('uint8array'));
            }
          } else if (!path.endsWith('.class') && !/^[b-z]\//.test(path)) {
            singleZip.file(path, fileObj.async('uint8array'));
          }
        }
      });

      const singleBlob = await singleZip.generateAsync({
        type: 'blob',
        compression: 'DEFLATE',
        compressionOptions: { level: 9 }
      });

      const cleanFileName = `${entry.title.replace(/[^a-zA-Z0-9_\-]/g, '_')}.jar`;
      results.push({
        fileName: cleanFileName,
        blob: singleBlob,
        metadata: {
          name: entry.title,
          vendor: 'Extracted',
          version: '1.0.0',
          fileSize: singleBlob.size
        }
      });
    }

    onProgress?.(100, 'Split complete');
    return results;
  }

  private static patchClassBytes(bytes: Uint8Array, midletChar: string): Uint8Array {
    const reader = new DataReader(bytes);
    if (reader.readU4() !== 0xCAFEBABE) {
      return bytes;
    }
    const patcher = new ClassPatcher();
    patcher.readClassHeader(reader);
    patcher.patchClass(reader, midletChar);

    const writer = new DataWriter();
    return patcher.writeClass(writer);
  }

  private static patchSwitcherClassListRef(bytes: Uint8Array): Uint8Array {
    const target = new TextEncoder().encode('javax/microedition/lcdui/List');
    const buf = new Uint8Array(bytes);
    const limit = buf.length - 35;

    for (let i = 10; i < limit; i++) {
      let match = true;
      for (let k = 0; k < target.length; k++) {
        if (buf[i + k] !== target[k]) {
          match = false;
          break;
        }
      }
      if (match) {
        buf[i] = 108;
        buf[i + 1] = 105;
        buf[i + 2] = 98;
        let p = i - 1;
        while (buf[p] !== 1 && p > 0) {
          p--;
        }
        if (buf[p] === 1) {
          const oldLen = buf[p + 2];
          buf[p + 2] = oldLen - 21;
          buf.copyWithin(i + 3, i + 24, buf.length);
          return buf.subarray(0, buf.length - 21);
        }
      }
    }
    return buf;
  }

  private static parseManifest(text: string): Map<string, string> {
    const props = new Map<string, string>();
    const lines = text.split(/\r?\n/);
    let currentKey = '';

    for (const line of lines) {
      const colonIdx = line.indexOf(':');
      if (colonIdx > 0 && !line.startsWith(' ')) {
        currentKey = line.substring(0, colonIdx).trim();
        props.set(currentKey, line.substring(colonIdx + 1).trim());
      } else if (line.startsWith(' ') && currentKey) {
        const prev = props.get(currentKey) || '';
        props.set(currentKey, prev + line.trim());
      }
    }
    return props;
  }

  private static blobToDataUrl(blob: Blob): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  }
}
