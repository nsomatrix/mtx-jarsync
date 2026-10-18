import { DataReader, DataWriter } from './DataStream';
import {
  CpInfo, ConstantDummyInfo, ConstantUtf8Info, ConstantIntegerInfo,
  ConstantFloatInfo, ConstantLongInfo, ConstantDoubleInfo, ConstantClassInfo,
  ConstantStringInfo, ConstantFieldrefInfo, ConstantMethodrefInfo,
  ConstantInterfaceMethodrefInfo, ConstantNameAndTypeInfo,
  FieldInfo, MethodInfo, AttributeInfo
} from './BytecodeTypes';

export interface IntPair {
  a: number;
  b: number;
}

export class ClassPatcher {
  public thisClassIndex: number = 0;
  public superClassIndex: number = 0;
  public constantPool: CpInfo[] = [];
  public currentPatchedClassName: string = '';
  public fields: FieldInfo[] = [];
  public attributes: AttributeInfo[] = [];
  public interfaces: number[] = [];
  public minorMajorVersion: number = 0;
  public accessFlags: number = 0;

  private utf8IndexMap = new Map<string, number>();
  private classIndexMap = new Map<string, number>();

  public static classReplacements = new Map<string, string>();
  public static patchedClasses: string[] = [];
  public static staticFieldsToClear: IntPair[] = [];
  private static midletClassCounter = 0;

  private methods: MethodInfo[] = [];
  private hasStaticFields = false;
  private hasClinit = false;

  public static initClassReplacements(): Map<string, string> {
    ClassPatcher.patchedClasses = [];
    ClassPatcher.midletClassCounter = 0;
    const map = ClassPatcher.classReplacements;
    map.clear();
    map.set("javax/microedition/io/Connector", "lib/Connector");
    map.set("javax/microedition/lcdui/Form", "lib/Form");
    map.set("javax/microedition/lcdui/List", "lib/List");
    map.set("javax/microedition/lcdui/Alert", "lib/Alert");
    map.set("javax/microedition/lcdui/Canvas", "lib/Canvas");
    map.set("com/nokia/mid/ui/FullCanvas", "lib/FullCanvas");
    map.set("javax/microedition/midlet/MIDlet", "lib/MIDlet");
    map.set("javax/microedition/lcdui/Display", "lib/Display");
    map.set("javax/microedition/lcdui/TextBox", "lib/TextBox");
    map.set("javax/microedition/lcdui/AlertType", "lib/AlertType");
    map.set("javax/microedition/lcdui/game/GameCanvas", "lib/GameCanvas");
    return map;
  }

  public readClassHeader(reader: DataReader) {
    this.utf8IndexMap.clear();
    this.classIndexMap.clear();
    this.minorMajorVersion = reader.readI4();
    const cpCount = reader.readU2();

    this.constantPool = [ConstantDummyInfo.INSTANCE];

    for (let i = 1; i < cpCount; i++) {
      const tag = reader.readU1();
      switch (tag) {
        case 1: { // Utf8
          const val = reader.readUTF();
          this.constantPool.push(new ConstantUtf8Info(val));
          this.utf8IndexMap.set(val, i);
          break;
        }
        case 3: // Integer
          this.constantPool.push(new ConstantIntegerInfo(reader.readI4()));
          break;
        case 4: // Float
          this.constantPool.push(new ConstantFloatInfo(reader.readI4()));
          break;
        case 5: // Long
          this.constantPool.push(new ConstantLongInfo(reader.readI8()));
          this.constantPool.push(ConstantDummyInfo.INSTANCE);
          i++;
          break;
        case 6: // Double
          this.constantPool.push(new ConstantDoubleInfo(reader.readI8()));
          this.constantPool.push(ConstantDummyInfo.INSTANCE);
          i++;
          break;
        case 7: // Class
          this.constantPool.push(new ConstantClassInfo(reader.readU2()));
          break;
        case 8: // String
          this.constantPool.push(new ConstantStringInfo(reader.readU2()));
          break;
        case 9: // Fieldref
          this.constantPool.push(new ConstantFieldrefInfo(reader.readU2(), reader.readU2()));
          break;
        case 10: // Methodref
          this.constantPool.push(new ConstantMethodrefInfo(reader.readU2(), reader.readU2()));
          break;
        case 11: // InterfaceMethodref
          this.constantPool.push(new ConstantInterfaceMethodrefInfo(reader.readU2(), reader.readU2()));
          break;
        case 12: // NameAndType
          this.constantPool.push(new ConstantNameAndTypeInfo(reader.readU2(), reader.readU2()));
          break;
      }
    }

    this.accessFlags = reader.readU2();
    this.accessFlags = (this.accessFlags | 1) & ~6; // ACC_PUBLIC, remove ACC_FINAL
    this.thisClassIndex = reader.readU2();
    this.superClassIndex = reader.readU2();

    const thisClass = this.constantPool[this.thisClassIndex] as ConstantClassInfo;
    const thisClassUtf8 = this.constantPool[thisClass.nameIndex] as ConstantUtf8Info;
    const rep = ClassPatcher.classReplacements.get(thisClassUtf8.value);
    this.currentPatchedClassName = rep ? rep : thisClassUtf8.value;
  }

  public patchClass(reader: DataReader, midletChar: string): Uint8Array {
    this.hasStaticFields = false;
    this.hasClinit = false;
    this.methods = [];

    // Interfaces
    const ifaceCount = reader.readU2();
    this.interfaces = [];
    for (let i = 0; i < ifaceCount; i++) {
      this.interfaces.push(reader.readU2());
    }

    // Fields
    const fieldCount = reader.readU2();
    this.fields = [];
    for (let i = 0; i < fieldCount; i++) {
      const f = new FieldInfo(reader.readU2(), reader.readU2(), reader.readU2());
      const fAttrCount = reader.readU2();
      for (let a = 0; a < fAttrCount; a++) {
        const nameIdx = reader.readU2();
        const len = reader.readI4();
        f.attributes.push(new AttributeInfo(nameIdx, reader.readBytes(len)));
      }

      if ((f.accessFlags & 8) !== 0) { // ACC_STATIC
        f.accessFlags &= ~16; // remove ACC_FINAL
        f.accessFlags |= 1;   // ensure ACC_PUBLIC
        this.hasStaticFields = true;
      }
      this.fields.push(f);
    }

    // Methods
    const methodCount = reader.readU2();
    for (let i = 0; i < methodCount; i++) {
      const m = new MethodInfo(reader.readU2(), reader.readU2(), reader.readU2());
      const mAttrCount = reader.readU2();
      for (let a = 0; a < mAttrCount; a++) {
        const nameIdx = reader.readU2();
        const len = reader.readI4();
        m.attributes.push(new AttributeInfo(nameIdx, reader.readBytes(len)));
      }

      const mNameUtf8 = this.constantPool[m.nameIndex] as ConstantUtf8Info;
      const mName = mNameUtf8 ? mNameUtf8.value : '';

      // Intercept paint and key events in Canvas
      if (mName === 'paint' || mName === 'keyPressed' || mName === 'keyReleased') {
        const mDescUtf8 = this.constantPool[m.descriptorIndex] as ConstantUtf8Info;
        const mDesc = mDescUtf8 ? mDescUtf8.value : '';
        if (mName === 'paint' && mDesc === '(Ljavax/microedition/lcdui/Graphics;)V') {
          m.nameIndex = this.getOrCreateUtf8('PAINT');
        } else if (mName === 'keyPressed' && mDesc === '(I)V') {
          m.nameIndex = this.getOrCreateUtf8('leyPressed');
        } else if (mName === 'keyReleased' && mDesc === '(I)V') {
          m.nameIndex = this.getOrCreateUtf8('leyReleased');
        }
      }

      if (mName === '<clinit>') {
        this.hasClinit = true;
        // Clone original <clinit> to cinitclone
        m.nameIndex = this.getOrCreateUtf8('cinitclone');
        m.accessFlags = 9; // ACC_PUBLIC | ACC_STATIC
      }

      this.methods.push(m);
    }

    if (this.hasStaticFields) {
      ClassPatcher.patchedClasses.push(this.currentPatchedClassName);
      this.synthesizeClearsMethod();

      if (!this.hasClinit) {
        // Synthesize empty cinitclone
        const emptyClone = new MethodInfo(9, this.getOrCreateUtf8('cinitclone'), this.getOrCreateUtf8('()V'));
        const codeWriter = new DataWriter(32);
        codeWriter.writeU2(0); // max_stack
        codeWriter.writeU2(0); // max_locals
        codeWriter.writeI4(1); // code_length
        codeWriter.writeU1(177); // return
        codeWriter.writeU2(0); // exception_table_length
        codeWriter.writeU2(0); // attributes_count
        emptyClone.attributes.push(new AttributeInfo(this.getOrCreateUtf8('Code'), codeWriter.toByteArray()));
        this.methods.push(emptyClone);
      }

      this.synthesizeClinitMethod(midletChar);
    }

    // Attributes
    const attrCount = reader.readU2();
    this.attributes = [];
    for (let i = 0; i < attrCount; i++) {
      const nameIdx = reader.readU2();
      const len = reader.readI4();
      this.attributes.push(new AttributeInfo(nameIdx, reader.readBytes(len)));
    }

    this.applyClassReplacements();
    return this.writeClass();
  }

  private synthesizeClearsMethod() {
    const mi = new MethodInfo(9, this.getOrCreateUtf8('clears'), this.getOrCreateUtf8('()V'));
    const w = new DataWriter(128);
    w.writeU2(1); // max_stack
    w.writeU2(0); // max_locals

    let codeLen = 1;
    for (const f of this.fields) {
      if ((f.accessFlags & 8) !== 0) { // ACC_STATIC
        const descUtf8 = this.constantPool[f.descriptorIndex] as ConstantUtf8Info;
        const d = descUtf8.value;
        if (d === 'D' || d === 'J') codeLen += 4;
        else if (d === 'F' || d === 'I' || d === 'C' || d === 'S' || d === 'Z' || d === 'B' || d.startsWith('L') || d.startsWith('[')) codeLen += 4;
      }
    }

    w.writeI4(codeLen);
    for (const f of this.fields) {
      if ((f.accessFlags & 8) !== 0) {
        const descUtf8 = this.constantPool[f.descriptorIndex] as ConstantUtf8Info;
        const nameUtf8 = this.constantPool[f.nameIndex] as ConstantUtf8Info;
        const d = descUtf8.value;
        const fieldRef = this.createFieldRef(this.thisClassIndex, nameUtf8.value, d);

        if (d === 'D') {
          w.writeU1(14); // dconst_0
          w.writeU1(179); // putstatic
          w.writeU2(fieldRef);
        } else if (d === 'J') {
          w.writeU1(9); // lconst_0
          w.writeU1(179); // putstatic
          w.writeU2(fieldRef);
        } else if (d === 'F') {
          w.writeU1(11); // fconst_0
          w.writeU1(179); // putstatic
          w.writeU2(fieldRef);
        } else if (d === 'I' || d === 'C' || d === 'S' || d === 'Z' || d === 'B') {
          w.writeU1(3); // iconst_0
          w.writeU1(179); // putstatic
          w.writeU2(fieldRef);
        } else if (d.startsWith('L') || d.startsWith('[')) {
          w.writeU1(1); // aconst_null
          w.writeU1(179); // putstatic
          w.writeU2(fieldRef);
        }
      }
    }
    w.writeU1(177); // return
    w.writeU2(0);   // exception_table_length
    w.writeU2(0);   // attributes_count
    mi.attributes.push(new AttributeInfo(this.getOrCreateUtf8('Code'), w.toByteArray()));
    this.methods.push(mi);
  }

  private synthesizeClinitMethod(midletChar: string) {
    const mi = new MethodInfo(9, this.getOrCreateUtf8('<clinit>'), this.getOrCreateUtf8('()V'));
    const w = new DataWriter(128);
    w.writeU2(2); // max_stack
    w.writeU2(0); // max_locals

    const staticClassName = (midletChar === 'a') ? 'Static' : (midletChar + '/Static');
    const staticClassIdx = this.getOrCreateClass(staticClassName);
    const vectorFieldRef = this.createFieldRef(staticClassIdx, 'staticvector', 'Ljava/util/Vector;');
    const vectorClassIdx = this.getOrCreateClass('java/util/Vector');
    const addElementMethodRef = this.createMethodRef(vectorClassIdx, 'addElement', '(Ljava/lang/Object;)V');
    const cinitCloneMethodRef = this.createMethodRef(this.thisClassIndex, 'cinitclone', '()V');

    // Code:
    // getstatic Static.staticvector
    // bipush classIndex
    // invokestatic Integer.valueOf / constructor
    // invokevirtual Vector.addElement
    // invokestatic thisClass.cinitclone
    // return
    w.writeI4(17); // code length
    w.writeU1(178); // getstatic
    w.writeU2(vectorFieldRef);
    w.writeU1(16); // bipush
    w.writeU1(ClassPatcher.patchedClasses.length - 1);
    const intClassIdx = this.getOrCreateClass('java/lang/Integer');
    const intInitRef = this.createMethodRef(intClassIdx, '<init>', '(I)V');
    w.writeU1(187); // new Integer
    w.writeU2(intClassIdx);
    w.writeU1(89);  // dup_x1
    w.writeU1(95);  // swap
    w.writeU1(183); // invokespecial Integer.<init>(I)V
    w.writeU2(intInitRef);
    w.writeU1(182); // invokevirtual Vector.addElement(Object)V
    w.writeU2(addElementMethodRef);
    w.writeU1(184); // invokestatic cinitclone()V
    w.writeU2(cinitCloneMethodRef);
    w.writeU1(177); // return
    w.writeU2(0);   // exception table
    w.writeU2(0);   // attributes
    mi.attributes.push(new AttributeInfo(this.getOrCreateUtf8('Code'), w.toByteArray()));
    this.methods.push(mi);
  }

  private applyClassReplacements() {
    for (const cp of this.constantPool) {
      if (cp.tag === 7) { // CONSTANT_Class
        const cls = cp as ConstantClassInfo;
        const utf = this.constantPool[cls.nameIndex] as ConstantUtf8Info;
        if (utf && ClassPatcher.classReplacements.has(utf.value)) {
          cls.nameIndex = this.getOrCreateUtf8(ClassPatcher.classReplacements.get(utf.value)!);
        }
      }
    }
  }

  public getOrCreateUtf8(str: string): number {
    if (this.utf8IndexMap.has(str)) {
      return this.utf8IndexMap.get(str)!;
    }
    const idx = this.constantPool.length;
    this.constantPool.push(new ConstantUtf8Info(str));
    this.utf8IndexMap.set(str, idx);
    return idx;
  }

  public getOrCreateClass(className: string): number {
    if (this.classIndexMap.has(className)) {
      return this.classIndexMap.get(className)!;
    }
    const nameIdx = this.getOrCreateUtf8(className);
    const idx = this.constantPool.length;
    this.constantPool.push(new ConstantClassInfo(nameIdx));
    this.classIndexMap.set(className, idx);
    return idx;
  }

  public createFieldRef(classIdx: number, name: string, desc: string): number {
    const nameIdx = this.getOrCreateUtf8(name);
    const descIdx = this.getOrCreateUtf8(desc);
    const natIdx = this.constantPool.length;
    this.constantPool.push(new ConstantNameAndTypeInfo(nameIdx, descIdx));
    const fRefIdx = this.constantPool.length;
    this.constantPool.push(new ConstantFieldrefInfo(classIdx, natIdx));
    return fRefIdx;
  }

  public createMethodRef(classIdx: number, name: string, desc: string): number {
    const nameIdx = this.getOrCreateUtf8(name);
    const descIdx = this.getOrCreateUtf8(desc);
    const natIdx = this.constantPool.length;
    this.constantPool.push(new ConstantNameAndTypeInfo(nameIdx, descIdx));
    const mRefIdx = this.constantPool.length;
    this.constantPool.push(new ConstantMethodrefInfo(classIdx, natIdx));
    return mRefIdx;
  }

  public writeClass(w?: DataWriter): Uint8Array {
    if (!w) w = new DataWriter();
    w.writeI4(0xCAFEBABE);
    w.writeI4(this.minorMajorVersion);

    w.writeU2(this.constantPool.length);
    for (let i = 1; i < this.constantPool.length; i++) {
      this.constantPool[i].write(w);
    }

    w.writeU2(this.accessFlags);
    w.writeU2(this.thisClassIndex);
    w.writeU2(this.superClassIndex);

    w.writeU2(this.interfaces.length);
    for (const iface of this.interfaces) {
      w.writeU2(iface);
    }

    w.writeU2(this.fields.length);
    for (const f of this.fields) {
      f.write(w);
    }

    w.writeU2(this.methods.length);
    for (const m of this.methods) {
      m.write(w);
    }

    w.writeU2(this.attributes.length);
    for (const attr of this.attributes) {
      attr.write(w);
    }

    return w.toByteArray();
  }
}
