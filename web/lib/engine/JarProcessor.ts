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
  customIconBytes?: Uint8Array;
  language?: 'en' | 'vi';
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

    // Determine master launcher icon: custom uploaded -> source game's icon -> fallback default icon
    let masterIconBytes: Uint8Array;
    if (options.customIconBytes && options.customIconBytes.length > 0) {
      masterIconBytes = options.customIconBytes;
    } else if (metadata.iconFileName && zip.file(metadata.iconFileName)) {
      masterIconBytes = await zip.file(metadata.iconFileName)!.async('uint8array');
    } else {
      const iconRes = await fetch('/runtime/icon.png').then(r => r.arrayBuffer());
      masterIconBytes = new Uint8Array(iconRes);
    }

    // Set master launcher icon in replicated JAR
    outZip.file('mtx/mid.png', masterIconBytes);

    // 2. Prepare sub-app identifiers
    const timestamp = Date.now();
    const iniName = `${timestamp}.ini`;
    const pngName = `${timestamp}.png`;

    // Save game icon under timestamp.png for switcher menu
    outZip.file(pngName, masterIconBytes);

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
    if (metadata.midlet2Class) {
      combinedMidletEntries.push(`${metadata.name} Tool,/${pngName},${metadata.midlet2Class},/${iniName}`);
    }

    let mf = 'Manifest-Version: 1.0\r\n';
    mf += 'MicroEdition-Configuration: CLDC-1.1\r\n';
    mf += 'MicroEdition-Profile: MIDP-2.0\r\n';
    mf += 'MIDlet-Version: 2.5\r\n';
    mf += `MIDlet-Vendor: ${options.author || metadata.vendor || 'MTX'}\r\n`;
    mf += `MIDlet-Name: ${options.appName || (options.language === 'vi' ? `${metadata.name} Đa ứng dụng` : `${metadata.name} Multi`)}\r\n`;
    mf += `MIDlet-1: ${options.displayName || options.appName || (options.language === 'vi' ? `${metadata.name} Đa ứng dụng` : `${metadata.name} Multi`)},/mtx/mid.png,mtx.jarsync.runtime.MtxSwitcher\r\n`;
    if (options.featureNoExit) {
      mf += 'Nokia-MIDlet-no-exit: true\r\n';
    }
    if (options.language) {
      mf += `Mtx-Language: ${options.language}\r\n`;
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
    onProgress?: (progress: number, status: string) => void,
    options?: { language?: 'en' | 'vi' }
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

    // Load runtime overlay archives for standalone execution
    let coreBytes: ArrayBuffer | null = null;
    let switcherBytes: ArrayBuffer | null = null;
    try {
      [coreBytes, switcherBytes] = await Promise.all([
        fetch('/runtime/mtx-core.jar').then(r => r.arrayBuffer()),
        fetch('/runtime/mtx-switcher.jar').then(r => r.arrayBuffer())
      ]);
    } catch {
      // fallback
    }

    const coreZip = coreBytes ? await JSZip.loadAsync(coreBytes) : null;
    const switcherZip = switcherBytes ? await JSZip.loadAsync(switcherBytes) : null;

    const results: SplitResult[] = [];
    const stepWeight = 85 / subAppEntries.length;

    for (let idx = 0; idx < subAppEntries.length; idx++) {
      const entry = subAppEntries[idx];
      const curPct = Math.round(10 + idx * stepWeight);
      onProgress?.(curPct, `Extracting ${entry.title}...`);

      const singleZip = new JSZip();
      const prefix = idx === 0 ? '' : `${String.fromCharCode(97 + idx)}/`;

      // 1. Copy mtx-core and mtx-switcher runtime (provides lib.MIDlet adapter and direct auto-launch)
      if (coreZip) {
        for (const [p, f] of Object.entries(coreZip.files)) {
          if (!f.dir) singleZip.file(p, await f.async('uint8array'));
        }
      }
      if (switcherZip) {
        for (const [p, f] of Object.entries(switcherZip.files)) {
          if (!f.dir) singleZip.file(p, await f.async('uint8array'));
        }
      }

      // 2. Extract icon
      const iconObj = zip.file(entry.icon) || zip.file('icon.png');
      if (iconObj) {
        const iconData = await iconObj.async('uint8array');
        singleZip.file('icon.png', iconData);
        singleZip.file(entry.icon, iconData);
        singleZip.file('mtx/mid.png', iconData);
      }

      // 3. Extract ini file
      const iniFileObj = zip.file(entry.iniFile);
      if (iniFileObj) {
        singleZip.file(entry.iniFile, await iniFileObj.async('uint8array'));
      }

      let vendor = 'MTX JarSync';
      let version = '1.0.0';
      let config = 'CLDC-1.1';
      let profile = 'MIDP-2.0';

      if (iniFileObj) {
        const iniText = await iniFileObj.async('text');
        for (const l of iniText.split(/\r?\n/)) {
          const m = l.match(/^([^:]+):\s*(.*)$/);
          if (m) {
            const key = m[1].trim();
            const val = m[2].trim();
            if (key === 'MIDlet-Vendor') vendor = val;
            else if (key === 'MIDlet-Version') version = val;
            else if (key === 'MicroEdition-Configuration') config = val;
            else if (key === 'MicroEdition-Profile') profile = val;
          }
        }
      }

      // 4. Standalone direct-launch manifest with single sub-app entry
      const manifestLines = [
        'Manifest-Version: 1.0',
        `MicroEdition-Configuration: ${config}`,
        `MicroEdition-Profile: ${profile}`,
        `MIDlet-Version: ${version}`,
        `MIDlet-Name: ${entry.title}`,
        `MIDlet-Vendor: ${vendor}`,
        `MIDlet-1: ${entry.title},/icon.png,mtx.jarsync.runtime.MtxSwitcher`,
        'Nokia-MIDlet-no-exit: true',
      ];
      if (options?.language) {
        manifestLines.push(`Mtx-Language: ${options.language}`);
      }
      manifestLines.push(`0: ${entry.title},/icon.png,${entry.className},/${entry.iniFile}`);
      manifestLines.push('');
      const standaloneManifest = manifestLines.join('\r\n');

      singleZip.file('META-INF/MANIFEST.MF', standaloneManifest);

      // 5. Copy classes (retaining lib/ runtime classes and Static.class) and resources
      zip.forEach((path, fileObj) => {
        if (fileObj.dir || path.startsWith('META-INF/')) return;
        if (path.startsWith('mtx/') || path.startsWith('MrQuyet/')) return;
        if (path.endsWith('.ini')) return;

        // Retain lib/ runtime classes (lib/MIDlet.class, etc.) so split standalone apps execute independently
        if (path.startsWith('lib/')) {
          singleZip.file(path, fileObj.async('uint8array'));
          return;
        }

        if (idx === 0) {
          // Root clone: include all non-prefixed classes (including Static.class) and resources
          const isOtherPrefixed = /^[b-z]\//.test(path);
          if (!isOtherPrefixed) {
            singleZip.file(path, fileObj.async('uint8array'));
          }
        } else {
          // Prefixed clone: include clone classes (including prefix/Static.class) and shared resources
          if (path.startsWith(prefix)) {
            singleZip.file(path, fileObj.async('uint8array'));
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

  /**
   * Merges a selected subset of sub-apps from an existing multi-game JAR into a new multi-JAR archive.
   */
  public static async mergeSubApps(
    sourceJar: File | Blob,
    selectedIndices: number[],
    options?: {
      appName?: string;
      displayName?: string;
      author?: string;
      language?: 'en' | 'vi';
    },
    onProgress?: (percent: number, status: string) => void
  ): Promise<Blob> {
    onProgress?.(10, 'Reading source archive...');
    const zip = await JSZip.loadAsync(sourceJar);
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

    const selectedEntries = subAppEntries.filter((_, idx) => selectedIndices.includes(idx));
    if (selectedEntries.length === 0) {
      throw new Error('No valid sub-apps selected to merge.');
    }

    onProgress?.(30, 'Merging selected applications...');
    const outZip = new JSZip();

    // Load updated runtime overlay binaries if available
    try {
      const [coreBytes, switcherBytes] = await Promise.all([
        fetch('/runtime/mtx-core.jar').then(r => r.arrayBuffer()),
        fetch('/runtime/mtx-switcher.jar').then(r => r.arrayBuffer())
      ]);
      const coreZip = await JSZip.loadAsync(coreBytes);
      const switcherZip = await JSZip.loadAsync(switcherBytes);
      for (const [p, f] of Object.entries(coreZip.files)) {
        if (!f.dir) outZip.file(p, await f.async('uint8array'));
      }
      for (const [p, f] of Object.entries(switcherZip.files)) {
        if (!f.dir) outZip.file(p, await f.async('uint8array'));
      }
    } catch {
      // fallback to source classes
    }

    for (const [path, fileObj] of Object.entries(zip.files)) {
      if (fileObj.dir || path.startsWith('META-INF/')) continue;
      // Retain runtime classes and resources
      if (path.startsWith('lib/') || path.startsWith('mtx/') || path.startsWith('MrQuyet/')) {
        if (!outZip.file(path)) {
          outZip.file(path, await fileObj.async('uint8array'));
        }
        continue;
      }
      // Root classes
      if (!/^[b-z]\//.test(path)) {
        if (path.endsWith('.class')) {
          if (selectedIndices.includes(0)) {
            outZip.file(path, await fileObj.async('uint8array'));
          }
        } else {
          // Shared non-class resources, icons, and inis
          outZip.file(path, await fileObj.async('uint8array'));
        }
        continue;
      }
      // Prefixed clone classes
      const prefixMatch = path.match(/^([b-z])\//);
      if (prefixMatch) {
        const prefixIdx = prefixMatch[1].charCodeAt(0) - 97;
        if (selectedIndices.includes(prefixIdx)) {
          outZip.file(path, await fileObj.async('uint8array'));
        }
      }
    }

    // Generate manifest
    onProgress?.(70, 'Generating merged manifest...');
    const combinedMidletEntries: string[] = [];
    for (let i = 0; i < selectedEntries.length; i++) {
      const e = selectedEntries[i];
      combinedMidletEntries.push(`${e.title},/${e.icon},${e.className},/${e.iniFile}`);
    }

    const defaultMergeName = options?.language === 'vi'
      ? `MTX Tổng hợp (${selectedEntries.length}-trong-1)`
      : `MTX Multi (${selectedEntries.length}-in-1)`;
    const appName = options?.appName || defaultMergeName;
    const displayName = options?.displayName || appName;
    const author = options?.author || 'MTX JarSync';

    let mf = 'Manifest-Version: 1.0\r\n';
    mf += 'MicroEdition-Configuration: CLDC-1.1\r\n';
    mf += 'MicroEdition-Profile: MIDP-2.0\r\n';
    mf += 'MIDlet-Version: 2.5\r\n';
    mf += `MIDlet-Vendor: ${author}\r\n`;
    mf += `MIDlet-Name: ${appName}\r\n`;
    mf += `MIDlet-1: ${displayName},/mtx/mid.png,mtx.jarsync.runtime.MtxSwitcher\r\n`;
    mf += 'Nokia-MIDlet-no-exit: true\r\n';
    if (options?.language) {
      mf += `Mtx-Language: ${options.language}\r\n`;
    }
    for (let i = 0; i < combinedMidletEntries.length; i++) {
      mf += `${i}: ${combinedMidletEntries[i]}\r\n`;
    }
    mf += '\r\n';

    outZip.file('META-INF/MANIFEST.MF', mf);

    onProgress?.(90, 'Compressing merged archive...');
    const resultBlob = await outZip.generateAsync({
      type: 'blob',
      compression: 'DEFLATE',
      compressionOptions: { level: 9 }
    });

    onProgress?.(100, 'Merge complete');
    return resultBlob;
  }

  public static bundleSubApps = JarProcessor.mergeSubApps;

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
