# MTX JarSync 3.6.3 (Clean J2ME Source)

A cleanly reverse-engineered, modular, and maintainable Java source tree for **MTX JarSync 3.6.3** (by AngelChip / nhquyet).

This tool is a specialized J2ME (CLDC 1.1 / MIDP 2.0 / JSR-75) utility designed to dynamically merge multiple MIDlet JAR applications into a single unified JAR container with an interactive runtime switcher menu, bytecode API hooking, isolated RecordStore partitions, and static variable isolation.

---

## 1. Architectural Overview & Reverse Engineering Analysis

The original application (`mtx-jarsync.jar`) contained 34 root classes obfuscated with ProGuard (using single-letter uppercase `A`–`G` and lowercase `a`–`z` names), creating severe case-folding collisions on Windows/macOS filesystems. Through binary and bytecode analysis via REA, CFR, and Vineflower decompilers, the architecture was fully decoded and refactored into domain packages:

```
src/main/
├── java/
│   └── mtx/jarsync/
│       ├── Midlet.java                 # Main MIDlet entry & application workflow coordinator
│       ├── bytecode/                   # JVM classfile binary structures, parser & patcher
│       │   ├── AttributeInfo.java      # JVM attribute representation (Code, StackMap, etc.)
│       │   ├── ClassBuilder.java       # Runtime generator of isolated multi-app Static classes
│       │   ├── ClassPatcher.java       # Bytecode rewriter for API redirection (lib/* hooks)
│       │   ├── CpInfo.java             # Abstract base class for constant pool entries
│       │   ├── Constant*Info.java      # 12 concrete JVM constant pool types (tags 1..12)
│       │   ├── FieldInfo.java          # JVM class field container
│       │   └── MethodInfo.java         # JVM class method container
│       ├── model/                      # Data models and localization
│       │   ├── Base64Resources.java    # Embedded UI strings and Base64 icon decoder
│       │   ├── IntPair.java            # Dimension and coordinate tuple
│       │   ├── MidletEntry.java        # Target MIDlet JAR manifest parser & metadata
│       │   └── StringPair.java         # Key/value and name/type tuple
│       ├── ui/                         # Rich LCDUI Canvas graphics engine
│       │   ├── CustomChoiceCanvas.java # Full-screen custom Choice implementation with menus
│       │   ├── ScrollThread.java       # Marquee text smooth-scrolling worker thread
│       │   └── TickThread.java         # One-shot UI timer for selection counter
│       └── zip/                        # Self-contained RFC 1950/1951 Deflate/Inflate/ZIP engine
│           ├── ByteInputStream.java    # Fast memory stream with endian primitives
│           ├── DataFormatException.java# CLDC zip exception
│           ├── Deflater.java           # Pure-Java Deflate compression engine (GNU Classpath)
│           ├── Inflater.java           # Pure-Java Deflate decompression engine (GNU Classpath)
│           ├── LittleEndianOutputStream.java # Buffered stream with ZIP header rewind support
│           ├── ZipEntry.java           # ZIP file header metadata
│           └── ZipOutputStream.java    # Streaming ZIP archive generator
└── resources/
    ├── icon.png                        # Application launcher icon
    ├── META-INF/MANIFEST.MF            # MIDlet metadata and custom menu configuration
    ├── MrQuyetlib.jar                  # Core runtime wrapper hooks (Display, Canvas, Alert)
    ├── MrQuyetlibs.jar                 # Support libraries
    └── MrQuyetSSlib.jar                # Screenshot-enabled runtime wrapper hooks
```

---

## 2. Reverse Engineering Mapping

| Original Class | Target Clean Class | Description |
| :--- | :--- | :--- |
| `mtx/jarsync/Midlet` | `mtx.jarsync.Midlet` | Main J2ME MIDlet & UI Controller |
| `m` | `mtx.jarsync.bytecode.CpInfo` | Abstract Constant Pool item |
| `t` | `mtx.jarsync.bytecode.ConstantUtf8Info` | Tag 1: CONSTANT_Utf8 |
| `E` | `mtx.jarsync.bytecode.ConstantIntegerInfo` | Tag 3: CONSTANT_Integer |
| `G` | `mtx.jarsync.bytecode.ConstantFloatInfo` | Tag 4: CONSTANT_Float |
| `a` | `mtx.jarsync.bytecode.ConstantLongInfo` | Tag 5: CONSTANT_Long |
| `y` | `mtx.jarsync.bytecode.ConstantDoubleInfo` | Tag 6: CONSTANT_Double |
| `b` | `mtx.jarsync.bytecode.ConstantClassInfo` | Tag 7: CONSTANT_Class |
| `x` | `mtx.jarsync.bytecode.ConstantStringInfo` | Tag 8: CONSTANT_String |
| `C` | `mtx.jarsync.bytecode.ConstantFieldrefInfo` | Tag 9: CONSTANT_Fieldref |
| `w` | `mtx.jarsync.bytecode.ConstantMethodrefInfo` | Tag 10: CONSTANT_Methodref |
| `o` | `mtx.jarsync.bytecode.ConstantInterfaceMethodrefInfo` | Tag 11: CONSTANT_InterfaceMethodref |
| `u` | `mtx.jarsync.bytecode.ConstantNameAndTypeInfo` | Tag 12: CONSTANT_NameAndType |
| `z` | `mtx.jarsync.bytecode.ConstantDummyInfo` | 8-byte constant slot placeholder |
| `D` | `mtx.jarsync.bytecode.AttributeInfo` | JVM Attribute (`Code`, `StackMap`, etc.) |
| `h` | `mtx.jarsync.bytecode.FieldInfo` | JVM Class Field |
| `j` | `mtx.jarsync.bytecode.MethodInfo` | JVM Class Method |
| `d` | `mtx.jarsync.bytecode.ClassBuilder` | Dynamic MIDlet loader class generator |
| `q` | `mtx.jarsync.bytecode.ClassPatcher` | Bytecode rewriter / API hook injector |
| `B` | `mtx.jarsync.model.MidletEntry` | MIDlet metadata & manifest parser |
| `f` | `mtx.jarsync.model.StringPair` | String tuple |
| `k` | `mtx.jarsync.model.IntPair` | Integer coordinate tuple |
| `g` | `mtx.jarsync.model.Base64Resources` | Base64 decode & resource loader |
| `e` | `mtx.jarsync.ui.CustomChoiceCanvas` | Interactive visual menu / Choice Canvas |
| `i` | `mtx.jarsync.ui.TickThread` | Selection index timer thread |
| `F` | `mtx.jarsync.ui.ScrollThread` | Marquee scrolling thread |
| `c` | `mtx.jarsync.zip.ZipEntry` | ZIP entry metadata |
| `n` | `mtx.jarsync.zip.ZipOutputStream` | ZIP format writer |
| `p` | `mtx.jarsync.zip.LittleEndianOutputStream` | Buffered LE binary output stream with rewinding |
| `r` | `mtx.jarsync.zip.ByteInputStream` | Fast byte-array input stream with LE reads |
| `A`, `l`, `s`, `v` | `mtx.jarsync.zip.*` | RFC 1951 Deflate/Inflate compression engine |

---

## 3. How Multi-MIDlet Combining Works

1. **Metadata Parsing:** The tool scans the target JAR files via JSR-75 FileConnection, parses their `META-INF/MANIFEST.MF` files, and extracts MIDlet titles, main classes, and icon paths.
2. **Bytecode API Hooking:** Target class files are rewritten via `ClassPatcher`:
   - Standard APIs (`javax.microedition.lcdui.Display`, `Canvas`, `Form`, `List`, `Alert`, `javax.microedition.midlet.MIDlet`) are patched to wrapper hooks in `MrQuyetlib.jar` (`lib/Display`, `lib/Canvas`, etc.).
   - `RecordStore` calls are redirected to `lib/RecordStore` with partitioned namespaces to prevent storage conflicts across embedded apps.
   - `paint` methods are patched to `PAINT` for container lifecycle interception.
   - Namespaces are isolated using directory prefixes (`a/`, `b/`, `c/`, etc.).
3. **Static State Isolation:** When switching between embedded MIDlets, shared static fields could corrupt game state. `ClassBuilder` generates a synthetic `Static` class with `clears()` and `cinitclones()` methods to re-initialize and isolate static variables.
4. **Master Packaging:** An embedded JAR (`MrQuyetlib.jar` / `MrQuyetSSlib.jar`) containing the multi-app menu launcher is bundled alongside all patched classes into a single distributable JAR.

---

## 4. Build Instructions

### Prerequisites
- JDK 8 or higher (tested with Java 8, 11, 17, and 21)
- J2ME CLDC 1.1 / MIDP 2.0 stubs (already bundled in `lib/`)

### Standalone Build (Fastest, zero external tools required)
Run the included build script:
```bash
./build.sh
```
Output:
```
target/mtx-jarsync.jar
```

### Maven Build
If Maven is installed on your system:
```bash
mvn clean package
```
Output:
```
target/mtx-jarsync.jar
```

---

## 5. Key Improvements Over Original Binary

- **Zero Case-Folding Conflicts:** Cleanly structured into distinct packages (`mtx.jarsync.bytecode`, `mtx.jarsync.model`, `mtx.jarsync.ui`, `mtx.jarsync.zip`), fully eliminating FAT32/NTFS/APFS case collision bugs.
- **Pure-Java Self-Contained Engine:** Canonical GNU Classpath RFC 1951 Deflater/Inflater replaces decompiled obfuscator artifacts (`** GOTO` labels and duplicate variables).
- **Readable & Type-Safe Identifiers:** Every constant pool tag, bytecode instruction, UI coordinate, and ZIP header field has semantic names and complete Javadoc documentation.
- **Maintainable Standards:** Standard Maven layout (`src/main/java`, `src/main/resources`, `pom.xml`) with reproducible standalone CLI build support.
