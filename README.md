# MTX JarSync 1.0.0

A modern, high-performance J2ME (Java Micro Edition) multi-package management and bytecode manipulation suite. 

**MTX JarSync** provides both a **Web Studio interface** (running pure TypeScript in the browser) and a **native J2ME engine** (CLDC 1.1 / MIDP 2.0 / JSR-75) to replicate, split, and merge retro mobile Java applications with full state isolation, independent RecordStore partitions, and interactive runtime switchers.

---

## Key Features

### 1. Web Studio (`web/`)
* **100% Client-Side JVM Bytecode Engine:** Executes directly in modern browsers without uploading JARs to any backend server.
* **Replicate J2ME Package:** Replicates a single J2ME JAR into multiple independent instances with isolated package namespaces (`root`, `b/`, `c/`...), dedicated RecordStore save storage, cloned static initializers (`<clinit>`), custom master icons, and Nokia no-exit backgrounding.
* **Split X Merge Multi-Package:**
  * **Split:** Deconstructs combined or multi-session packages back into standalone, single-game J2ME JARs that launch directly without a switcher menu.
  * **Merge:** Recombines selected sub-applications into customized merged suites featuring **Package Inspection** and **Custom Instance Names** for the in-game switcher menu.
* **Design & Usability:** Clean mobile-responsive UI with dark/light mode themes, touch-friendly segmented controls, and bilingual support (English and Vietnamese).

### 2. Native J2ME Engine (`src/main/`)
* **On-Device Combining:** Runs on legacy Symbian, Nokia Series 40, BlackBerry, and modern J2ME emulators (J2ME Loader, KEmulator, MicroEmulator).
* **Self-Contained Compression:** Embedded RFC 1950/1951 Deflate/Inflate engine (GNU Classpath standard) with zero external JAR dependencies.
* **Case-Folding Safe:** Structured cleanly into distinct namespaces (`mtx.jarsync.bytecode`, `mtx.jarsync.model`, `mtx.jarsync.ui`, `mtx.jarsync.zip`), eliminating filesystem collisions.

---

## Web Frontend (JarSync Studio)

### Tech Stack
* **Framework:** Next.js 16 (App Router / Turbopack)
* **UI & Styling:** React 19, Tailwind CSS, Lucide Icons
* **Engine Core:** TypeScript bytecode manipulation (`JarProcessor`, `ClassBuilder`, `ClassPatcher`, `JSZip`)
* **Localization:** English (`EN`) and Vietnamese (`VN`)

### Running Locally

1. Navigate to the `web` directory:
   ```bash
   cd web
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Launch the development server:
   ```bash
   npm run dev
   ```
   Open [http://localhost:3000](http://localhost:3000) in your browser.

4. Build for production:
   ```bash
   npm run build
   npm start
   ```

---

## Core Architecture & Engine Design

The codebase is organized into clean domain modules across both Java and TypeScript implementations:

```
mtx-jarsync/
├── web/                                # Next.js Web Studio
│   ├── app/                            # UI layouts, pages, and components
│   │   ├── components/                 # Navbar, ReplicateStudio, SplitStudio
│   │   └── page.tsx                    # Studio coordinator
│   └── lib/
│       ├── engine/                     # Browser-based JVM bytecode rewriting
│       │   ├── BytecodeUtils.ts        # Binary data reader / writer primitives
│       │   ├── ClassBuilder.ts         # Synthetic Static class generation
│       │   ├── ClassPatcher.ts         # Constant pool and descriptor rewriting
│       │   └── JarProcessor.ts         # Core Replicate, Split, and Merge pipeline
│       └── i18n/                       # Dual-language translations (EN / VN)
│
└── src/main/java/mtx/jarsync/          # Native J2ME Tool Source
    ├── Midlet.java                     # Main MIDlet entry & workflow coordinator
    ├── bytecode/                       # JVM classfile parser, patcher & structures
    │   ├── AttributeInfo.java          # JVM attribute representation (Code, StackMap)
    │   ├── ClassBuilder.java           # Runtime generator of isolated multi-app Static classes
    │   ├── ClassPatcher.java           # Bytecode rewriter for API redirection (lib/* hooks)
    │   ├── CpInfo.java                 # Abstract constant pool entry
    │   ├── Constant*Info.java          # 12 JVM constant pool types (tags 1..12)
    │   ├── FieldInfo.java              # JVM class field container
    │   └── MethodInfo.java             # JVM class method container
    ├── model/                          # Data models and localization
    │   ├── Base64Resources.java        # Embedded UI strings and Base64 icon decoder
    │   ├── IntPair.java                # Dimension and coordinate tuple
    │   ├── MidletEntry.java            # Target MIDlet manifest parser & metadata
    │   └── StringPair.java             # Key/value and name/type tuple
    ├── ui/                             # Rich LCDUI Canvas graphics engine
    │   ├── CustomChoiceCanvas.java     # Full-screen custom menu with checkboxes
    │   ├── ScrollThread.java           # Marquee text smooth-scrolling worker thread
    │   └── TickThread.java             # Selection counter timer thread
    └── zip/                            # Self-contained RFC 1950/1951 ZIP engine
        ├── ByteInputStream.java        # Memory stream with endian primitives
        ├── Deflater.java               # Pure-Java Deflate compression engine
        ├── Inflater.java               # Pure-Java Deflate decompression engine
        ├── LittleEndianOutputStream.java # Stream with ZIP header rewind support
        ├── ZipEntry.java               # ZIP file header metadata
        └── ZipOutputStream.java        # Streaming ZIP archive generator
```

---

## How Multi-Instance Isolation Works

1. **Namespace Isolation:** Each cloned instance is assigned a unique package directory prefix (e.g. `root`, `b/`, `c/`...). All internal class descriptor strings (e.g. `Lcom/game/Main;` $\rightarrow$ `Lb/com/game/Main;`) are rewritten in the constant pool.
2. **Bytecode API Hooking:** Class files are dynamically patched to redirect display, canvas lifecycle, and alert handlers through the runtime overlay switcher (`mtx.jarsync.runtime.MtxSwitcher`).
3. **RecordStore (RMS) Partitioning:** Storage access is partitioned per instance, allowing each clone or merged game to maintain independent save files and login credentials without overwriting other accounts.
4. **Static Variable Reset:** A synthetic `Static` class is generated with dedicated `clears()` and `cinitclones()` methods to re-run static initializers (`<clinit>`), preventing static variable contamination across instances.

---

## Native Build Instructions

### Prerequisites
* JDK 8 or higher (tested with Java 8, 11, 17, and 21)
* J2ME CLDC 1.1 / MIDP 2.0 stubs (bundled in `lib/`)

### Standalone Build (Recommended)
Run the build script directly:
```bash
./build.sh
```
Output artifact:
```
target/mtx-jarsync.jar
```

### Maven Build
```bash
mvn clean package
```
Output artifact:
```
target/mtx-jarsync.jar
```

---

## License

Maintained by **NSOMatrix™**. Distributed for research and retro-computing preservation purposes.
