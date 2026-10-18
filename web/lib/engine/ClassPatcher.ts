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
    this.accessFlags = (this.accessFlags | 1) & ~6; // ensure ACC_PUBLIC, clear ACC_FINAL/ACC_SUPER constraints
    this.thisClassIndex = reader.readU2();
    this.superClassIndex = reader.readU2();

    const thisClass = this.constantPool[this.thisClassIndex] as ConstantClassInfo;
    const thisClassUtf8 = this.constantPool[thisClass.nameIndex] as ConstantUtf8Info;
    const rep = ClassPatcher.classReplacements.get(thisClassUtf8.value);
    this.currentPatchedClassName = rep ? rep : thisClassUtf8.value;
  }

  public patchClass(reader: DataReader, midletChar: string) {
    this.hasStaticFields = false;
    this.hasClinit = false;
    this.methods = [];

    const cp = this.constantPool;
    const cpSize = cp.length;

    // 1. Patch ConstantClass and ConstantMethodref entries
    for (let i = 1; i < cpSize; i++) {
      const entry = cp[i];
      if (entry instanceof ConstantClassInfo) {
        const utf = cp[entry.nameIndex] as ConstantUtf8Info;
        if (!utf.isPatched) {
          utf.isPatched = true;
          const name = utf.value;
          if (ClassPatcher.classReplacements.has(name)) {
            utf.value = ClassPatcher.classReplacements.get(name)!;
          } else if (name.startsWith('[')) {
            const lIdx = name.indexOf('L') + 1;
            if (lIdx > 0) {
              const rep = ClassPatcher.classReplacements.get(name.substring(lIdx, name.length - 1));
              if (rep) {
                utf.value = name.substring(0, lIdx) + rep + ';';
              }
            }
          }
        }
      } else if (entry instanceof ConstantMethodrefInfo) {
        const nat = cp[entry.nameAndTypeIndex] as ConstantNameAndTypeInfo;
        const methodName = (cp[nat.nameIndex] as ConstantUtf8Info).value;
        const targetClass = (cp[(cp[entry.classIndex] as ConstantClassInfo).nameIndex] as ConstantUtf8Info).value;

        if (targetClass === 'java/lang/Class' && methodName === 'forName' && midletChar !== 'a') {
          entry.classIndex = this.getOrCreateClassRef('lib/Class');
          const nameUtf = cp[nat.nameIndex] as ConstantUtf8Info;
          nameUtf.isPatched = true;
          nameUtf.replacement = 'forName'.replace('f', midletChar);
        } else if (targetClass === 'javax/microedition/rms/RecordStore') {
          const suffix = methodName.substring(1);
          if (suffix === 'penRecordStore' || suffix === 'istRecordStores' || suffix === 'eleteRecordStore') {
            entry.classIndex = this.getOrCreateClassRef('lib/RecordStore');
            (cp[nat.nameIndex] as ConstantUtf8Info).value = midletChar + suffix;
          }
        } else if (methodName === 'paint' && targetClass !== 'javax/microedition/lcdui/game/Sprite') {
          const nameUtf = cp[nat.nameIndex] as ConstantUtf8Info;
          nameUtf.isPatched = true;
          nameUtf.replacement = 'PAINT';
        } else {
          this.patchMethodDescriptor(cp[nat.descriptorIndex] as ConstantUtf8Info);
        }
      } else if (entry instanceof ConstantInterfaceMethodrefInfo) {
        const nat = cp[entry.nameAndTypeIndex] as ConstantNameAndTypeInfo;
        this.patchMethodDescriptor(cp[nat.descriptorIndex] as ConstantUtf8Info);
      } else if (entry instanceof ConstantFieldrefInfo) {
        const nat = cp[entry.nameAndTypeIndex] as ConstantNameAndTypeInfo;
        this.patchFieldDescriptor(cp[nat.descriptorIndex] as ConstantUtf8Info);
      }
    }

    // 2. Patch NameAndType and String references
    for (let i = 1; i < cpSize; i++) {
      const entry = cp[i];
      if (entry instanceof ConstantNameAndTypeInfo) {
        const nameUtf = cp[entry.nameIndex] as ConstantUtf8Info;
        const name = nameUtf.value;
        if (name === 'keyReleased' || name === 'keyPressed') {
          nameUtf.replacement = name.replace('k', 'l');
          nameUtf.isPatched = true;
        }
        if (nameUtf.isPatched && nameUtf.replacement !== name) {
          entry.nameIndex = this.getUtf8ReplacementIndex(nameUtf);
        }
        const descUtf = cp[entry.descriptorIndex] as ConstantUtf8Info;
        if (descUtf.isPatched && descUtf.value.indexOf('/') === 1) {
          entry.descriptorIndex = this.getUtf8ReplacementIndex(descUtf);
        }
      } else if (entry instanceof ConstantStringInfo) {
        const strUtf = cp[entry.stringIndex] as ConstantUtf8Info;
        if (strUtf.isPatched && strUtf.replacement !== strUtf.value) {
          entry.stringIndex = this.getUtf8ReplacementIndex(strUtf);
        }
      }
    }

    // 3. Read interfaces
    const ifaceCount = reader.readU2();
    this.interfaces = [];
    for (let i = 0; i < ifaceCount; i++) {
      this.interfaces.push(reader.readU2());
    }

    // 4. Read and patch fields
    const fieldCount = reader.readU2();
    this.fields = [];
    const fieldsToClear: IntPair[] = [];

    for (let i = 0; i < fieldCount; i++) {
      const fAccess = reader.readU2();
      let fNameIdx = reader.readU2();
      let fDescIdx = reader.readU2();

      const nameUtf = cp[fNameIdx] as ConstantUtf8Info;
      if (nameUtf.isPatched && nameUtf.replacement !== nameUtf.value) {
        fNameIdx = this.getUtf8ReplacementIndex(nameUtf);
      }
      const descUtf = cp[fDescIdx] as ConstantUtf8Info;
      if (descUtf.isPatched && descUtf.value.indexOf('/') === 1) {
        fDescIdx = this.getUtf8ReplacementIndex(descUtf);
      }
      this.patchFieldDescriptor(cp[fDescIdx] as ConstantUtf8Info);

      const fAttrs = this.readAttributes(reader);
      // Non-constant static field in a concrete class
      if (fAttrs.length === 0 && (fAccess & 8) !== 0 && (this.accessFlags & 512) === 0) {
        this.hasStaticFields = true;
        fieldsToClear.push({ a: fNameIdx, b: fDescIdx });
      }

      const f = new FieldInfo(fAccess, fNameIdx, fDescIdx);
      f.attributes = fAttrs;
      this.fields.push(f);
    }

    // 5. Read and patch methods
    const methodCount = reader.readU2();
    for (let i = 0; i < methodCount; i++) {
      const mAccess = reader.readU2();
      let mNameIdx = reader.readU2();
      const mDescIdx = reader.readU2();

      let nameUtf = cp[mNameIdx] as ConstantUtf8Info;
      const mName = nameUtf.value;
      if (mName === 'keyReleased' || mName === 'keyPressed') {
        nameUtf.replacement = mName.replace('k', 'l');
        nameUtf.isPatched = true;
      } else if (mName === 'paint') {
        mNameIdx = this.addUtf8('PAINT');
        nameUtf = cp[mNameIdx] as ConstantUtf8Info;
      }

      if (nameUtf.isPatched && nameUtf.replacement !== mName) {
        mNameIdx = this.getUtf8ReplacementIndex(nameUtf);
      }
      this.patchMethodDescriptor(cp[mDescIdx] as ConstantUtf8Info);

      const mi = new MethodInfo(mAccess, mNameIdx, mDescIdx);
      mi.attributes = this.readAttributes(reader);
      this.methods.push(mi);

      // Clone <clinit> into cinitclone for multi-midlet isolation
      if (nameUtf.replacement === '<clinit>' && (this.accessFlags & 512) === 0) {
        mi.nameIndex = this.getOrCreateUtf8('cinitclone');
        mi.accessFlags = 9; // ACC_PUBLIC | ACC_STATIC
        this.hasClinit = true;

        const newClinit = new MethodInfo(mAccess, mNameIdx, mDescIdx);
        newClinit.attributes = this.createClinitPatchCode(midletChar);
        this.methods.push(newClinit);
      }
    }

    this.attributes = this.readAttributes(reader);

    // 6. Synthesize empty cinitclone and <clinit> if class has static fields but no original <clinit>
    if (this.hasStaticFields && !this.hasClinit) {
      this.hasClinit = true;

      const emptyClone = new MethodInfo(9, this.getOrCreateUtf8('cinitclone'), this.getOrCreateUtf8('()V'));
      const codeW = new DataWriter(16);
      codeW.writeU2(0); // max_stack
      codeW.writeU2(1); // max_locals
      codeW.writeI4(1); // code_length
      codeW.writeU1(177); // return
      codeW.writeI4(0); // exception_table_length(2) + attributes_count(2)
      emptyClone.attributes = [new AttributeInfo(this.getOrCreateUtf8('Code'), 1, codeW.toByteArray())];
      this.methods.push(emptyClone);

      const newClinit = new MethodInfo(9, this.getOrCreateUtf8('<clinit>'), this.getOrCreateUtf8('()V'));
      newClinit.attributes = this.createClinitPatchCode(midletChar);
      this.methods.push(newClinit);
    }

    // 7. Synthesize clears() method to reset static fields to default values
    if (this.hasStaticFields || this.hasClinit) {
      const clearsMethod = new MethodInfo(9, this.getOrCreateUtf8('clears'), this.getOrCreateUtf8('()V'));
      clearsMethod.attributes = this.createClearsPatchCode(fieldsToClear);
      this.methods.push(clearsMethod);

      ClassPatcher.midletClassCounter++;
      if (this.currentPatchedClassName) {
        ClassPatcher.patchedClasses.push(this.currentPatchedClassName);
      }
    }
  }

  private createClinitPatchCode(midletChar: string): AttributeInfo[] {
    const w = new DataWriter(32);
    w.writeU2(1); // max_stack
    w.writeU2(0); // max_locals
    w.writeI4(ClassPatcher.midletClassCounter < 6 ? 8 : 10); // code_length

    if (ClassPatcher.midletClassCounter < 6) {
      w.writeU1(3 + ClassPatcher.midletClassCounter); // iconst_0..iconst_5
    } else {
      w.writeU1(17); // sipush
      w.writeU2(ClassPatcher.midletClassCounter);
    }

    w.writeU1(184); // invokestatic
    const staticClass = (midletChar === 'a') ? 'Static' : `${midletChar}/Static`;
    w.writeU2(this.createMethodRef(this.getOrCreateClassRef(staticClass), 'regClass', '(I)V'));

    w.writeU1(184); // invokestatic
    w.writeU2(this.createMethodRef(this.thisClassIndex, 'cinitclone', '()V'));
    w.writeU1(177); // return
    w.writeI4(0); // exception_table_length(2) + attributes_count(2)

    const code = w.toByteArray();
    return [new AttributeInfo(this.getOrCreateUtf8('Code'), code.length, code)];
  }

  private createClearsPatchCode(fieldPairs: IntPair[]): AttributeInfo[] {
    const total = fieldPairs.length;
    const w = new DataWriter(total * 4 + 32);
    w.writeU2(2); // max_stack
    w.writeU2(0); // max_locals
    const codeLen = (total << 2) + 1;
    w.writeI4(codeLen);

    const cp = this.constantPool;
    const cpSize = cp.length;

    for (let i = 0; i < total; i++) {
      const pair = fieldPairs[i];
      let fieldRefIdx = -1;

      for (let j = 0; j < cpSize; j++) {
        const obj = cp[j];
        if (obj instanceof ConstantFieldrefInfo && obj.classIndex === this.thisClassIndex) {
          const nat = cp[obj.nameAndTypeIndex] as ConstantNameAndTypeInfo;
          if (nat.nameIndex === pair.a && nat.descriptorIndex === pair.b) {
            fieldRefIdx = j;
            break;
          }
        }
      }

      if (fieldRefIdx < 0) {
        let natIdx = -1;
        for (let j = 0; j < cpSize; j++) {
          const obj = cp[j];
          if (obj instanceof ConstantNameAndTypeInfo && obj.nameIndex === pair.a && obj.descriptorIndex === pair.b) {
            natIdx = j;
            break;
          }
        }
        if (natIdx < 0) {
          cp.push(new ConstantNameAndTypeInfo(pair.a, pair.b));
          natIdx = cp.length - 1;
        }
        cp.push(new ConstantFieldrefInfo(this.thisClassIndex, natIdx));
        fieldRefIdx = cp.length - 1;
      }

      const descUtf = cp[pair.b] as ConstantUtf8Info;
      let pushOpcode: number;
      switch (descUtf.value.charAt(0)) {
        case 'L':
        case '[':
          pushOpcode = 1; // aconst_null
          break;
        case 'J':
          pushOpcode = 9; // lconst_0
          break;
        case 'F':
          pushOpcode = 11; // fconst_0
          break;
        case 'D':
          pushOpcode = 14; // dconst_0
          break;
        default:
          pushOpcode = 3; // iconst_0
          break;
      }

      w.writeU1(pushOpcode);
      w.writeU1(179); // putstatic
      w.writeU2(fieldRefIdx);
    }

    w.writeU1(177); // return
    w.writeI4(0); // exception_table_length(2) + attributes_count(2)

    const code = w.toByteArray();
    return [new AttributeInfo(this.getOrCreateUtf8('Code'), code.length, code)];
  }

  private getOrCreateClassRef(className: string): number {
    if (this.classIndexMap.has(className)) {
      return this.classIndexMap.get(className)!;
    }
    this.constantPool.push(new ConstantClassInfo(this.getOrCreateUtf8(className)));
    const idx = this.constantPool.length - 1;
    this.classIndexMap.set(className, idx);
    return idx;
  }

  private getUtf8ReplacementIndex(utf8: ConstantUtf8Info): number {
    return this.getOrCreateUtf8(utf8.replacement);
  }

  private createMethodRef(classIdx: number, name: string, desc: string): number {
    this.constantPool.push(new ConstantNameAndTypeInfo(this.getOrCreateUtf8(name), this.getOrCreateUtf8(desc)));
    this.constantPool.push(new ConstantMethodrefInfo(classIdx, this.constantPool.length - 1));
    return this.constantPool.length - 1;
  }

  public getOrCreateUtf8(str: string): number {
    if (this.utf8IndexMap.has(str)) {
      const idx = this.utf8IndexMap.get(str)!;
      const utf = this.constantPool[idx] as ConstantUtf8Info;
      if (utf.isPatched && utf.value !== str) {
        this.utf8IndexMap.set(utf.value, idx);
        const newIdx = this.addUtf8(str);
        this.utf8IndexMap.set(str, newIdx);
        return newIdx;
      }
      return idx;
    }
    return this.addUtf8(str);
  }

  private addUtf8(str: string): number {
    const idx = this.constantPool.length;
    this.constantPool.push(new ConstantUtf8Info(str));
    this.utf8IndexMap.set(str, idx);
    return idx;
  }

  private patchFieldDescriptor(utf: ConstantUtf8Info) {
    if (!utf.isPatched) {
      utf.isPatched = true;
      let desc = utf.value;
      if (desc.startsWith('[') || desc.startsWith('L')) {
        const lIdx = desc.indexOf('L') + 1;
        if (lIdx > 0) {
          const rep = ClassPatcher.classReplacements.get(desc.substring(lIdx, desc.length - 1));
          if (rep) {
            utf.value = desc.substring(0, lIdx) + rep + ';';
          }
        }
      }
    }
  }

  private patchMethodDescriptor(utf: ConstantUtf8Info) {
    if (!utf.isPatched) {
      utf.isPatched = true;
      let desc = utf.value;
      if (!desc.startsWith('(') || desc.indexOf(';') < 0) {
        return;
      }

      let res = '';
      let lIdx: number;
      while ((lIdx = desc.indexOf('L') + 1) > 0) {
        res += desc.substring(0, lIdx);
        desc = desc.substring(lIdx);
        const semi = desc.indexOf(';') + 1;
        if (semi <= 0) break;
        const className = desc.substring(0, semi - 1);
        const rep = ClassPatcher.classReplacements.get(className);
        if (rep) {
          res += rep + ';';
        } else {
          res += desc.substring(0, semi);
        }
        desc = desc.substring(semi);
      }
      res += desc;
      utf.value = res;
    }
  }

  private readAttributes(reader: DataReader): AttributeInfo[] {
    const count = reader.readU2();
    const attrs: AttributeInfo[] = [];
    for (let i = 0; i < count; i++) {
      let nameIdx = reader.readU2();
      const nameUtf = this.constantPool[nameIdx] as ConstantUtf8Info;
      if (nameUtf.isPatched && nameUtf.replacement !== nameUtf.value) {
        nameIdx = this.getUtf8ReplacementIndex(nameUtf);
      }
      const len = reader.readI4();
      const info = reader.readBytes(len);
      attrs.push(new AttributeInfo(nameIdx, len, info));
    }
    return attrs;
  }

  public writeClass(writer?: DataWriter): Uint8Array {
    const w = writer || new DataWriter();
    w.writeI4(0xCAFEBABE);
    w.writeI4(this.minorMajorVersion);

    const cpSize = this.constantPool.length;
    w.writeU2(cpSize);
    for (let i = 1; i < cpSize; i++) {
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
