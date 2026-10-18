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
}

export interface MergeOptions {
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
  metadata: JarMetadata;
}

export class JarProcessor {
  /**
   * Parses a JAR File and returns its extracted metadata (Manifest, App Name, Icon, MIDlet classes).
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

    // Parse icon from MIDlet-1: "Name, /icon.png, class"
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

    // Fallback: check /icon.png if not found
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
        originalFileName: file.name
      },
      zip
    };
  }

  /**
   * Merges multiple J2ME JARs into an isolated multi-app JAR with an in-game switcher overlay.
   */
  public static async mergeJars(
    files: File[],
    options: MergeOptions,
    onProgress?: (progress: number, status: string) => void
  ): Promise<Blob> {
    const totalSteps = files.length * 3 + 4;
    let completedSteps = 0;

    const report = (status: string) => {
      completedSteps++;
      const pct = Math.min(99, Math.round((completedSteps / totalSteps) * 100));
      onProgress?.(pct, status);
    };

    const outZip = new JSZip();
    const patcher = new ClassPatcher();
    const combinedMidletEntries: string[] = [];
    let currentAppIndex = 0;

    // 1. Load runtime overlay archives
    report('Loading MTX runtime overlay binaries...');
    const [coreBytes, switcherBytes] = await Promise.all([
      fetch('/runtime/mtx-core.jar').then(r => r.arrayBuffer()),
      fetch(options.featureScreenshot ? '/runtime/mtx-switcher-ss.jar' : '/runtime/mtx-switcher.jar').then(r => r.arrayBuffer())
    ]);

    // Copy runtime classes into root of output JAR
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

    // 2. Process each input JAR
    for (let fIdx = 0; fIdx < files.length; fIdx++) {
      const file = files[fIdx];
      report(`Analyzing ${file.name}...`);
      const { metadata, zip } = await JarProcessor.inspectJar(file);

      const timestamp = Date.now() + fIdx;
      const iniName = `${timestamp}.ini`;
      const pngName = `${timestamp}.png`;

      // Copy icon for switcher display
      if (metadata.iconFileName && zip.file(metadata.iconFileName)) {
        const iconData = await zip.file(metadata.iconFileName)!.async('uint8array');
        outZip.file(pngName, iconData);
      } else {
        outZip.file(pngName, new Uint8Array(iconRes));
      }

      // Record manifest properties to .ini
      let iniContent = '';
      const manifestFile = zip.file('META-INF/MANIFEST.MF');
      if (manifestFile) {
        const mfText = await manifestFile.async('text');
        iniContent = mfText.replace(/\r?\n/g, '\r\n');
      }
      outZip.file(iniName, iniContent);

      // Collect classes and resources
      const classFiles: { path: string; file: JSZip.JSZipObject }[] = [];
      const resourceFiles: { path: string; file: JSZip.JSZipObject }[] = [];

      zip.forEach((path, zipEntry) => {
        if (zipEntry.dir || path.startsWith('META-INF/')) return;
        if (path.endsWith('.class')) {
          classFiles.push({ path, file: zipEntry });
        } else {
          resourceFiles.push({ path, file: zipEntry });
        }
      });

      // Compute prefix for this sub-game
      const prefix = currentAppIndex === 0 ? '' : `${String.fromCharCode(97 + currentAppIndex)}/`;
      const prefixChar = currentAppIndex === 0 ? 'a' : String.fromCharCode(97 + currentAppIndex);
      const subAppName = `${metadata.name} ${currentAppIndex + 1}`;

      // Update Combined Master Manifest Table:
      // index: Title,/icon,prefix.Class,/timestamp.ini
      const targetMainClass = metadata.midlet1Class || 'Main';
      const qualifiedMainClass = currentAppIndex === 0 ? targetMainClass : `${String.fromCharCode(97 + currentAppIndex)}.${targetMainClass}`;
      combinedMidletEntries.push(`${subAppName},/${pngName},${qualifiedMainClass},/${iniName}`);

      // Setup Class Replacements mapping
      report(`Rewriting bytecode for ${file.name}...`);
      const replacements = ClassPatcher.initClassReplacements();
      for (const cf of classFiles) {
        const cNameNoExt = cf.path.substring(0, cf.path.length - 6);
        replacements.set(cNameNoExt, prefix + cNameNoExt);
      }

      // Patch classes
      for (const cf of classFiles) {
        const rawBytes = await cf.file.async('uint8array');
        const patchedBytes = JarProcessor.patchClassBytes(patcher, rawBytes, prefixChar);
        outZip.file(prefix + cf.path, patchedBytes);
      }

      // Generate synthetic Static.class for this sub-app
      const staticClassName = prefix.length > 0 ? `${prefixChar}/Static` : 'Static';
      const staticClassBytes = ClassBuilder.buildClass(ClassPatcher.patchedClasses, staticClassName);
      if (staticClassBytes) {
        outZip.file(`${staticClassName}.class`, staticClassBytes);
      }

      // Copy resources unchanged
      report(`Packing resources for ${file.name}...`);
      for (const rf of resourceFiles) {
        const resBytes = await rf.file.async('uint8array');
        outZip.file(rf.path, resBytes);
      }

      currentAppIndex++;
    }

    // 3. Generate Master Manifest META-INF/MANIFEST.MF
    report('Generating unified J2ME manifest...');
    let mf = 'Manifest-Version: 1.0\r\n';
    mf += 'MicroEdition-Configuration: CLDC-1.1\r\n';
    mf += 'MicroEdition-Profile: MIDP-2.0\r\n';
    mf += 'MIDlet-Version: 2.5\r\n';
    mf += `MIDlet-Vendor: ${options.author || 'MTX JarSync'}\r\n`;
    mf += `MIDlet-Name: ${options.appName || 'MTX Combined'}\r\n`;
    mf += `MIDlet-1: ${options.displayName || options.appName || 'MTX Multi'},/mtx/mid.png,mtx.jarsync.runtime.MtxSwitcher\r\n`;
    if (options.featureNoExit) {
      mf += 'Nokia-MIDlet-no-exit: true\r\n';
    }
    for (let i = 0; i < combinedMidletEntries.length; i++) {
      mf += `${i}: ${combinedMidletEntries[i]}\r\n`;
    }
    mf += '\r\n';

    outZip.file('META-INF/MANIFEST.MF', mf);

    report('Compressing final JAR package...');
    const resultBlob = await outZip.generateAsync({
      type: 'blob',
      compression: 'DEFLATE',
      compressionOptions: { level: 9 }
    });

    onProgress?.(100, 'Done');
    return resultBlob;
  }

  /**
   * Splits a multi-app JAR back into individual standalone playable JARs.
   */
  public static async splitJar(
    file: File,
    onProgress?: (progress: number, status: string) => void
  ): Promise<SplitResult[]> {
    onProgress?.(10, 'Analyzing combined JAR structure...');
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
      throw new Error('This JAR does not appear to be an MTX/Multi-MIDlet combined JAR (no numbered index entries found).');
    }

    const results: SplitResult[] = [];
    const stepWeight = 85 / subAppEntries.length;

    for (let idx = 0; idx < subAppEntries.length; idx++) {
      const entry = subAppEntries[idx];
      const curPct = Math.round(10 + idx * stepWeight);
      onProgress?.(curPct, `Extracting ${entry.title}...`);

      const singleZip = new JSZip();

      // Determine package prefix for this sub-app
      const prefix = idx === 0 ? '' : `${String.fromCharCode(97 + idx)}/`;
      const dotPrefix = idx === 0 ? '' : `${String.fromCharCode(97 + idx)}.`;

      // Read original manifest properties from its .ini file
      let standaloneManifest = '';
      const iniFileObj = zip.file(entry.iniFile);
      if (iniFileObj) {
        standaloneManifest = await iniFileObj.async('text');
      }

      // If .ini didn't have full manifest, generate standard J2ME manifest
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

      // Copy icon
      const iconObj = zip.file(entry.icon);
      if (iconObj) {
        const iconData = await iconObj.async('uint8array');
        singleZip.file('icon.png', iconData);
      }

      // Extract and un-prefix classes
      zip.forEach((path, fileObj) => {
        if (fileObj.dir || path.startsWith('META-INF/')) return;

        // Skip runtime overlays
        if (path.startsWith('lib/') || path.startsWith('mtx/') || path.startsWith('MrQuyet/')) return;
        if (path.endsWith('.ini')) return;

        if (idx === 0) {
          // App 0: classes at root level that aren't prefixed with single-letter dirs b/, c/, etc.
          const isOtherPrefixed = /^[b-z]\//.test(path);
          if (!isOtherPrefixed) {
            if (path !== 'Static.class') {
              singleZip.file(path, fileObj.async('uint8array'));
            }
          }
        } else {
          // App idx: classes start with `${prefix}`
          if (path.startsWith(prefix)) {
            const unPrefixed = path.substring(prefix.length);
            if (unPrefixed !== 'Static.class') {
              singleZip.file(unPrefixed, fileObj.async('uint8array'));
            }
          } else if (!path.endsWith('.class') && !/^[b-z]\//.test(path)) {
            // Also include shared resources
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
          fileSize: singleBlob.size,
          originalFileName: cleanFileName
        }
      });
    }

    onProgress?.(100, 'Split complete');
    return results;
  }

  private static patchClassBytes(patcher: ClassPatcher, bytes: Uint8Array, midletChar: string): Uint8Array {
    const reader = new DataReader(bytes);
    if (reader.readI4() !== 0xCAFEBABE) {
      return bytes;
    }
    patcher.readClassHeader(reader);
    patcher.patchClass(reader, midletChar);

    const writer = new DataWriter();
    patcher.writeClass(writer);
    return writer.toByteArray();
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
        buf[i] = 108; // 'l'
        buf[i + 1] = 105; // 'i'
        buf[i + 2] = 98; // 'b'
        let p = i - 1;
        while (buf[p] !== 1 && p > 0) { // find CONSTANT_Utf8 tag
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
