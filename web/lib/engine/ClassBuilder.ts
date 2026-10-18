import { DataWriter } from './DataStream';
import {
  CpInfo, ConstantDummyInfo, ConstantUtf8Info, ConstantClassInfo,
  ConstantFieldrefInfo, ConstantMethodrefInfo, ConstantNameAndTypeInfo,
  FieldInfo, MethodInfo, AttributeInfo
} from './BytecodeTypes';

/**
 * Generates synthetic `Static.class` bytecode at runtime for embedding multi-midlet
 * state management, stack isolation, and static initializer cloning.
 * Ported with 100% fidelity from Java ClassBuilder.java.
 */
export class ClassBuilder {
  private static methods: MethodInfo[] = [];
  private static constantPool: CpInfo[] = [];
  private static utf8Entries: string[] = [];
  private static classNames: string[] = [];
  private static fields: FieldInfo[] = [];
  private static nameAndTypes = new Map<string, { a: string; b: string }>();
  private static nameAndTypeIndices = new Map<string, number>();
  private static utf8Indices = new Map<string, number>();
  private static utf8BaseIndex: number = 0;
  private static initIntMethodRef: number = 0;
  private static staticVectorFieldRef: number = 0;

  private static getUtf8Index(key: string): number {
    if (!ClassBuilder.utf8Indices.has(key)) {
      const idx = ClassBuilder.utf8Entries.length + ClassBuilder.utf8BaseIndex;
      ClassBuilder.utf8Indices.set(key, idx);
      ClassBuilder.utf8Entries.push(key);
      return idx;
    }
    return ClassBuilder.utf8Indices.get(key)!;
  }

  public static buildClass(injectedClasses: string[], targetClassName: string): Uint8Array | null {
    if (injectedClasses.length < 0) {
      return null;
    }

    ClassBuilder.utf8Entries = [];
    ClassBuilder.fields = [];
    ClassBuilder.classNames = [];
    ClassBuilder.constantPool = [];
    ClassBuilder.methods = new Array(5);

    ClassBuilder.utf8Indices.clear();
    const defaultUtf8 = [
      "()V", "Code", "(I)V", "java/util/Vector", "java/lang/Integer",
      "<init>", "clears", "<clinit>", "StackMap", "regClass",
      "cinitclone", "addElement", "cinitclones", "staticvector",
      "lib/Stack", "Ljava/util/Vector;", "(Ljava/util/Vector;)V", "(Ljava/lang/Object;)V"
    ];
    for (const u of defaultUtf8) {
      ClassBuilder.utf8Indices.set(u, -1);
    }

    ClassBuilder.nameAndTypes.clear();
    ClassBuilder.nameAndTypes.set("<init>()V", { a: "<init>", b: "()V" });
    ClassBuilder.nameAndTypes.set("<init>(I)V", { a: "<init>", b: "(I)V" });
    ClassBuilder.nameAndTypes.set("clears()V", { a: "clears", b: "()V" });
    ClassBuilder.nameAndTypes.set("regClass(I)V", { a: "regClass", b: "(I)V" });
    ClassBuilder.nameAndTypes.set("cinitclone()V", { a: "cinitclone", b: "()V" });
    ClassBuilder.nameAndTypes.set("<init>(Ljava/util/Vector;)V", { a: "<init>", b: "(Ljava/util/Vector;)V" });
    ClassBuilder.nameAndTypes.set("staticvectorLjava/util/Vector;", { a: "staticvector", b: "Ljava/util/Vector;" });
    ClassBuilder.nameAndTypes.set("addElement(Ljava/lang/Object;)V", { a: "addElement", b: "(Ljava/lang/Object;)V" });

    try {
      ClassBuilder.utf8Indices.set(targetClassName, -1);
      ClassBuilder.classNames.push(targetClassName);
      ClassBuilder.classNames.push("java/lang/Integer");
      ClassBuilder.classNames.push("lib/Stack");
      ClassBuilder.classNames.push("java/util/Vector");

      for (const inj of injectedClasses) {
        ClassBuilder.classNames.push(inj);
      }

      let nextIdx = ClassBuilder.utf8BaseIndex =
        ClassBuilder.classNames.length + 6 + ClassBuilder.nameAndTypes.size + (injectedClasses.length << 1);

      for (const key of Array.from(ClassBuilder.utf8Indices.keys())) {
        ClassBuilder.utf8Indices.set(key, nextIdx);
        ClassBuilder.utf8Entries.push(key);
        nextIdx++;
      }

      ClassBuilder.constantPool.push(ConstantDummyInfo.INSTANCE);
      const classTotal = ClassBuilder.classNames.length;
      for (let i = 0; i < classTotal; i++) {
        ClassBuilder.constantPool.push(new ConstantClassInfo(ClassBuilder.getUtf8Index(ClassBuilder.classNames[i])));
      }

      let cpIndex = classTotal;
      ClassBuilder.nameAndTypeIndices.clear();
      for (const [key, pair] of Array.from(ClassBuilder.nameAndTypes.entries())) {
        ClassBuilder.constantPool.push(
          new ConstantNameAndTypeInfo(ClassBuilder.getUtf8Index(pair.a), ClassBuilder.getUtf8Index(pair.b))
        );
        ClassBuilder.nameAndTypeIndices.set(key, ++cpIndex);
      }

      ClassBuilder.fields.push(
        new FieldInfo(10, ClassBuilder.getUtf8Index("staticvector"), ClassBuilder.getUtf8Index("Ljava/util/Vector;"))
      );
      ClassBuilder.constantPool.push(
        new ConstantFieldrefInfo(1, ClassBuilder.nameAndTypeIndices.get("staticvectorLjava/util/Vector;")!)
      );
      ClassBuilder.staticVectorFieldRef = ++cpIndex;

      ClassBuilder.constantPool.push(
        new ConstantMethodrefInfo(4, ClassBuilder.nameAndTypeIndices.get("<init>()V")!)
      );
      cpIndex++;
      ClassBuilder.constantPool.push(
        new ConstantMethodrefInfo(3, ClassBuilder.nameAndTypeIndices.get("<init>(Ljava/util/Vector;)V")!)
      );
      ClassBuilder.initIntMethodRef = ++cpIndex;

      ClassBuilder.constantPool.push(
        new ConstantMethodrefInfo(2, ClassBuilder.nameAndTypeIndices.get("<init>(I)V")!)
      );
      ClassBuilder.constantPool.push(
        new ConstantMethodrefInfo(4, ClassBuilder.nameAndTypeIndices.get("addElement(Ljava/lang/Object;)V")!)
      );

      const cinitCloneNat = ClassBuilder.nameAndTypeIndices.get("cinitclone()V")!;
      for (let i = 5; i <= classTotal; i++) {
        ClassBuilder.constantPool.push(new ConstantMethodrefInfo(i, cinitCloneNat));
      }

      const clearsNat = ClassBuilder.nameAndTypeIndices.get("clears()V")!;
      for (let i = 5; i <= classTotal; i++) {
        ClassBuilder.constantPool.push(new ConstantMethodrefInfo(i, clearsNat));
      }

      for (const entry of ClassBuilder.utf8Entries) {
        ClassBuilder.constantPool.push(new ConstantUtf8Info(entry));
      }

      const voidSig = ClassBuilder.getUtf8Index("()V");
      ClassBuilder.methods[0] = new MethodInfo(1, ClassBuilder.getUtf8Index("<init>"), voidSig);
      ClassBuilder.methods[0].attributes = [ClassBuilder.createInitCode()];

      ClassBuilder.methods[4] = new MethodInfo(9, ClassBuilder.getUtf8Index("<clinit>"), voidSig);
      ClassBuilder.methods[4].attributes = [ClassBuilder.createClinitCode()];

      const intSig = ClassBuilder.getUtf8Index("(I)V");
      ClassBuilder.methods[1] = new MethodInfo(9, ClassBuilder.getUtf8Index("regClass"), intSig);
      ClassBuilder.methods[1].attributes = [ClassBuilder.createRegClassCode()];

      ClassBuilder.methods[2] = new MethodInfo(17, ClassBuilder.getUtf8Index("cinitclones"), intSig);
      ClassBuilder.methods[2].attributes = [ClassBuilder.createCinitclonesCode()];

      ClassBuilder.methods[3] = new MethodInfo(17, ClassBuilder.getUtf8Index("clears"), intSig);
      ClassBuilder.methods[3].attributes = [ClassBuilder.createClearsCode()];

      return ClassBuilder.writeClassFile();
    } catch (e) {
      console.error("[ClassBuilder] Error building class:", e);
      return null;
    }
  }

  private static writeClassFile(): Uint8Array {
    const writer = new DataWriter();
    writer.writeI4(0xCAFEBABE);
    writer.writeI4(0x0003002D); // minor=3, major=45 (CLDC 1.1)

    const cpSize = ClassBuilder.constantPool.length;
    writer.writeU2(cpSize);
    for (let i = 1; i < cpSize; i++) {
      ClassBuilder.constantPool[i].write(writer);
    }

    writer.writeU2(17); // access_flags (ACC_PUBLIC | ACC_SUPER)
    writer.writeU2(1);  // this_class
    writer.writeU2(3);  // super_class ("lib/Stack")
    writer.writeU2(0);  // interfaces_count

    writer.writeU2(ClassBuilder.fields.length);
    for (const f of ClassBuilder.fields) {
      f.write(writer);
    }

    writer.writeU2(5);  // methods_count
    for (let i = 0; i < 5; i++) {
      ClassBuilder.methods[i].write(writer);
    }

    writer.writeU2(0);  // attributes_count
    return writer.toByteArray();
  }

  private static createInitCode(): AttributeInfo {
    const writer = new DataWriter();
    writer.writeU2(2); // max_stack
    writer.writeU2(1); // max_locals
    writer.writeI4(8); // code_length
    writer.writeU1(42); // aload_0
    writer.writeU1(178); // getstatic
    writer.writeU2(ClassBuilder.staticVectorFieldRef);
    writer.writeU1(183); // invokespecial
    writer.writeU2(ClassBuilder.initIntMethodRef);
    writer.writeU1(177); // return
    writer.writeU2(0); // exception_table_length
    writer.writeU2(0); // attributes_count

    const code = writer.toByteArray();
    return new AttributeInfo(ClassBuilder.getUtf8Index("Code"), code);
  }

  private static createClinitCode(): AttributeInfo {
    const writer = new DataWriter();
    writer.writeU2(2);  // max_stack
    writer.writeU2(0);  // max_locals
    writer.writeI4(11); // code_length
    writer.writeU1(187); // new
    writer.writeU2(4);
    writer.writeU1(89);  // dup
    writer.writeU1(183); // invokespecial
    writer.writeU2(ClassBuilder.initIntMethodRef - 1);
    writer.writeU1(179); // putstatic
    writer.writeU2(ClassBuilder.staticVectorFieldRef);
    writer.writeU1(177); // return
    writer.writeU2(0);   // exception_table_length
    writer.writeU2(0);   // attributes_count

    const code = writer.toByteArray();
    return new AttributeInfo(ClassBuilder.getUtf8Index("Code"), code);
  }

  private static createRegClassCode(): AttributeInfo {
    const writer = new DataWriter();
    writer.writeU2(4);  // max_stack
    writer.writeU2(1);  // max_locals
    writer.writeI4(15); // code_length
    writer.writeU1(178); // getstatic
    writer.writeU2(ClassBuilder.staticVectorFieldRef);
    writer.writeU1(187); // new
    writer.writeU2(2);
    writer.writeU1(89);  // dup
    writer.writeU1(26);  // iload_0
    writer.writeU1(183); // invokespecial
    writer.writeU2(ClassBuilder.initIntMethodRef + 1);
    writer.writeU1(182); // invokevirtual
    writer.writeU2(ClassBuilder.initIntMethodRef + 2);
    writer.writeU1(177); // return
    writer.writeI4(0);   // exception_table_length(2) + attributes_count(2)

    const code = writer.toByteArray();
    return new AttributeInfo(ClassBuilder.getUtf8Index("Code"), code);
  }

  private static createCinitclonesCode(): AttributeInfo {
    const writerA = new DataWriter();
    const writerB = new DataWriter();
    const count = ClassBuilder.classNames.length - 4;

    writerB.writeU2(ClassBuilder.getUtf8Index("StackMap"));
    writerB.writeI4(count * 10 + 12);
    writerB.writeU2(count + 1);

    writerA.writeU2(1); // max_stack
    writerA.writeU2(2); // max_locals
    let codeLen = (count << 3) + 16;
    writerA.writeI4(codeLen);
    writerA.writeU1(27);  // iload_1
    writerA.writeU1(170); // tableswitch

    let offset = (count << 2) + 15;
    codeLen -= 2;
    writerA.writeU2(0);
    writerA.writeI4(codeLen);
    writerA.writeI4(0);
    writerA.writeI4(count - 1);

    for (let i = 0; i < count; i++) {
      writerA.writeI4(offset);
      writerB.writeU2(offset + 1);
      offset += 4;
      writerB.writeU2(2);
      writerB.writeU1(7);
      writerB.writeU2(1);
      writerB.writeU1(1);
      writerB.writeU2(0);
    }

    writerB.writeU2(codeLen + 1);
    writerB.writeU2(2);
    writerB.writeU1(7);
    writerB.writeU2(1);
    writerB.writeU1(1);
    writerB.writeU2(0);

    const endMethod = count + ClassBuilder.initIntMethodRef + 3;
    for (let i = ClassBuilder.initIntMethodRef + 3; i < endMethod; i++) {
      writerA.writeU1(184); // invokestatic
      writerA.writeU2(i);
      writerA.writeU1(177); // return
    }

    writerA.writeU2(0); // exception_table_length
    writerA.writeU2(1); // attributes_count
    writerA.writeBytes(writerB.toByteArray());

    const code = writerA.toByteArray();
    return new AttributeInfo(ClassBuilder.getUtf8Index("Code"), code);
  }

  private static createClearsCode(): AttributeInfo {
    const writerA = new DataWriter();
    const writerB = new DataWriter();
    const count = ClassBuilder.classNames.length - 4;

    writerB.writeU2(ClassBuilder.getUtf8Index("StackMap"));
    writerB.writeI4(count * 10 + 12);
    writerB.writeU2(count + 1);

    writerA.writeU2(1); // max_stack
    writerA.writeU2(2); // max_locals
    let codeLen = (count << 3) + 16;
    writerA.writeI4(codeLen);
    writerA.writeU1(27);  // iload_1
    writerA.writeU1(170); // tableswitch

    let offset = (count << 2) + 15;
    codeLen -= 2;
    writerA.writeU2(0);
    writerA.writeI4(codeLen);
    writerA.writeI4(0);
    writerA.writeI4(count - 1);

    for (let i = 0; i < count; i++) {
      writerA.writeI4(offset);
      writerB.writeU2(offset + 1);
      offset += 4;
      writerB.writeU2(2);
      writerB.writeU1(7);
      writerB.writeU2(1);
      writerB.writeU1(1);
      writerB.writeU2(0);
    }

    writerB.writeU2(codeLen + 1);
    writerB.writeU2(2);
    writerB.writeU1(7);
    writerB.writeU2(1);
    writerB.writeU1(1);
    writerB.writeU2(0);

    const endMethod = (count << 1) + ClassBuilder.initIntMethodRef + 3;
    for (let i = ClassBuilder.initIntMethodRef + 3 + count; i < endMethod; i++) {
      writerA.writeU1(184); // invokestatic
      writerA.writeU2(i);
      writerA.writeU1(177); // return
    }

    writerA.writeU2(0); // exception_table_length
    writerA.writeU2(1); // attributes_count
    writerA.writeBytes(writerB.toByteArray());

    const code = writerA.toByteArray();
    return new AttributeInfo(ClassBuilder.getUtf8Index("Code"), code);
  }
}
