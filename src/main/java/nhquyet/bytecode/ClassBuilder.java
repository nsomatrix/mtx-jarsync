package nhquyet.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import nhquyet.model.StringPair;

/**
 * Generates synthetic class bytecode at runtime for embedding multi-midlet
 * state management, stack isolation, and static initializer cloning.
 */
public final class ClassBuilder {
    private static final MethodInfo[] methods = new MethodInfo[5];
    private static final Vector constantPool = new Vector(2048);
    private static final Vector utf8Entries = new Vector(2048);
    private static final Vector classNames = new Vector(200);
    private static final Vector fields = new Vector(1024);
    private static final Hashtable nameAndTypes = new Hashtable(1024);
    private static final Hashtable utf8Indices = new Hashtable(1024);
    private static int utf8BaseIndex;
    private static int initIntMethodRef;
    private static int staticVectorFieldRef;

    private static final ByteArrayOutputStream byteBufferA = new ByteArrayOutputStream();
    private static final DataOutputStream dataOutA = new DataOutputStream(byteBufferA);
    private static final ByteArrayOutputStream byteBufferB = new ByteArrayOutputStream();
    private static final DataOutputStream dataOutB = new DataOutputStream(byteBufferB);

    private static void writeClassFile(DataOutputStream dos) throws IOException {
        dos.writeInt(0xCAFEBABE); // Magic
        dos.writeInt(0x0003002D); // minor=3, major=45 (CLDC 1.1 / Java 1.1 bytecode format)

        int cpSize = constantPool.size();
        dos.writeShort(cpSize);
        for (int i = 1; i < cpSize; i++) {
            ((CpInfo) constantPool.elementAt(i)).write(dos);
        }

        dos.writeShort(17); // access_flags (ACC_PUBLIC | ACC_SUPER)
        dos.writeShort(1);  // this_class (points to class index 1)
        dos.writeShort(3);  // super_class (points to "lib/Stack")
        dos.writeShort(0);  // interfaces_count

        int fieldCount = fields.size();
        dos.writeShort(fieldCount);
        for (int i = 0; i < fieldCount; i++) {
            ((FieldInfo) fields.elementAt(i)).write(dos);
        }

        dos.writeShort(5); // methods_count
        for (int i = 0; i < 5; i++) {
            methods[i].write(dos);
        }

        dos.writeShort(0); // attributes_count
    }

    public static synchronized byte[] buildClass(Vector injectedClasses, String targetClassName) {
        if (injectedClasses.size() < 0) {
            return null;
        }

        utf8Entries.removeAllElements();
        fields.removeAllElements();
        classNames.removeAllElements();
        constantPool.removeAllElements();

        utf8Indices.clear();
        utf8Indices.put("()V", "");
        utf8Indices.put("Code", "");
        utf8Indices.put("(I)V", "");
        utf8Indices.put("java/util/Vector", "");
        utf8Indices.put("java/lang/Integer", "");
        utf8Indices.put("<init>", "");
        utf8Indices.put("clears", "");
        utf8Indices.put("<clinit>", "");
        utf8Indices.put("StackMap", "");
        utf8Indices.put("regClass", "");
        utf8Indices.put("cinitclone", "");
        utf8Indices.put("addElement", "");
        utf8Indices.put("cinitclones", "");
        utf8Indices.put("staticvector", "");
        utf8Indices.put("lib/Stack", "");
        utf8Indices.put("Ljava/util/Vector;", "");
        utf8Indices.put("(Ljava/util/Vector;)V", "");
        utf8Indices.put("(Ljava/lang/Object;)V", "");

        nameAndTypes.clear();
        nameAndTypes.put("<init>()V", new StringPair("<init>", "()V"));
        nameAndTypes.put("<init>(I)V", new StringPair("<init>", "(I)V"));
        nameAndTypes.put("clears()V", new StringPair("clears", "()V"));
        nameAndTypes.put("regClass(I)V", new StringPair("regClass", "(I)V"));
        nameAndTypes.put("cinitclone()V", new StringPair("cinitclone", "()V"));
        nameAndTypes.put("<init>(Ljava/util/Vector;)V", new StringPair("<init>", "(Ljava/util/Vector;)V"));
        nameAndTypes.put("staticvectorLjava/util/Vector;", new StringPair("staticvector", "Ljava/util/Vector;"));
        nameAndTypes.put("addElement(Ljava/lang/Object;)V", new StringPair("addElement", "(Ljava/lang/Object;)V"));

        try {
            utf8Indices.put(targetClassName, "");
            classNames.addElement(targetClassName);
            classNames.addElement("java/lang/Integer");
            classNames.addElement("lib/Stack");
            classNames.addElement("java/util/Vector");

            int injCount = injectedClasses.size();
            for (int i = 0; i < injCount; i++) {
                classNames.addElement(injectedClasses.elementAt(i));
            }

            int nextIdx = utf8BaseIndex = classNames.size() + 6 + nameAndTypes.size() + (injectedClasses.size() << 1);
            Enumeration utf8Keys = utf8Indices.keys();
            while (utf8Keys.hasMoreElements()) {
                Object key = utf8Keys.nextElement();
                utf8Indices.put(key, new Integer(nextIdx));
                utf8Entries.addElement(key);
                nextIdx++;
            }

            constantPool.addElement(ConstantDummyInfo.INSTANCE);
            int classTotal = classNames.size();
            for (int i = 0; i < classTotal; i++) {
                constantPool.addElement(new ConstantClassInfo(getUtf8Index(classNames.elementAt(i))));
            }

            int cpIndex = classTotal;
            Enumeration natKeys = nameAndTypes.keys();
            while (natKeys.hasMoreElements()) {
                Object key = natKeys.nextElement();
                StringPair pair = (StringPair) nameAndTypes.get(key);
                constantPool.addElement(new ConstantNameAndTypeInfo(getUtf8Index(pair.a), getUtf8Index(pair.b)));
                nameAndTypes.put(key, new Integer(++cpIndex));
            }

            fields.addElement(new FieldInfo(10, getUtf8Index("staticvector"), getUtf8Index("Ljava/util/Vector;")));
            constantPool.addElement(new ConstantFieldrefInfo(1, ((Integer) nameAndTypes.get("staticvectorLjava/util/Vector;")).intValue()));
            staticVectorFieldRef = ++cpIndex;

            constantPool.addElement(new ConstantMethodrefInfo(4, ((Integer) nameAndTypes.get("<init>()V")).intValue()));
            cpIndex++;
            constantPool.addElement(new ConstantMethodrefInfo(3, ((Integer) nameAndTypes.get("<init>(Ljava/util/Vector;)V")).intValue()));
            initIntMethodRef = ++cpIndex;

            constantPool.addElement(new ConstantMethodrefInfo(2, ((Integer) nameAndTypes.get("<init>(I)V")).intValue()));
            constantPool.addElement(new ConstantMethodrefInfo(4, ((Integer) nameAndTypes.get("addElement(Ljava/lang/Object;)V")).intValue()));

            int cinitCloneNat = ((Integer) nameAndTypes.get("cinitclone()V")).intValue();
            for (int i = 5; i <= classTotal; i++) {
                constantPool.addElement(new ConstantMethodrefInfo(i, cinitCloneNat));
            }

            int clearsNat = ((Integer) nameAndTypes.get("clears()V")).intValue();
            for (int i = 5; i <= classTotal; i++) {
                constantPool.addElement(new ConstantMethodrefInfo(i, clearsNat));
            }

            int utf8Total = utf8Entries.size();
            for (int i = 0; i < utf8Total; i++) {
                constantPool.addElement(new ConstantUtf8Info((String) utf8Entries.elementAt(i)));
            }

            int voidSig = getUtf8Index("()V");
            methods[0] = new MethodInfo(1, getUtf8Index("<init>"), voidSig);
            methods[0].attributes = new AttributeInfo[]{createInitCode()};

            methods[4] = new MethodInfo(9, getUtf8Index("<clinit>"), voidSig);
            methods[4].attributes = new AttributeInfo[]{createClinitCode()};

            int intSig = getUtf8Index("(I)V");
            methods[1] = new MethodInfo(9, getUtf8Index("regClass"), intSig);
            methods[1].attributes = new AttributeInfo[]{createRegClassCode()};

            methods[2] = new MethodInfo(17, getUtf8Index("cinitclones"), intSig);
            methods[2].attributes = new AttributeInfo[]{createCinitclonesCode()};

            methods[3] = new MethodInfo(17, getUtf8Index("clears"), intSig);
            methods[3].attributes = new AttributeInfo[]{createClearsCode()};
        } catch (Exception e) {
            e.printStackTrace();
        }

        byteBufferA.reset();
        try {
            writeClassFile(dataOutA);
            return byteBufferA.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getUtf8Index(Object key) {
        if (!utf8Indices.containsKey(key)) {
            int idx = utf8Entries.size() + utf8BaseIndex;
            utf8Indices.put(key, new Integer(idx));
            utf8Entries.addElement(key);
            return idx;
        }
        return ((Integer) utf8Indices.get(key)).intValue();
    }

    private static AttributeInfo createInitCode() {
        try {
            byteBufferA.reset();
            dataOutA.writeShort(2); // max_stack
            dataOutA.writeShort(1); // max_locals
            dataOutA.writeInt(8);   // code_length
            dataOutA.writeByte(42); // aload_0
            dataOutA.writeByte(178); // getstatic
            dataOutA.writeShort(staticVectorFieldRef);
            dataOutA.writeByte(183); // invokespecial
            dataOutA.writeShort(initIntMethodRef);
            dataOutA.writeByte(177); // return
            dataOutA.writeShort(0); // exception_table_length
            dataOutA.writeShort(0); // attributes_count
            dataOutA.flush();

            byte[] code = byteBufferA.toByteArray();
            return new AttributeInfo(getUtf8Index("Code"), code.length, code);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static AttributeInfo createClinitCode() {
        try {
            byteBufferA.reset();
            dataOutA.writeShort(2); // max_stack
            dataOutA.writeShort(0); // max_locals
            dataOutA.writeInt(11);  // code_length
            dataOutA.writeByte(187); // new
            dataOutA.writeShort(4);
            dataOutA.writeByte(89);  // dup
            dataOutA.writeByte(183); // invokespecial
            dataOutA.writeShort(initIntMethodRef - 1);
            dataOutA.writeByte(179); // putstatic
            dataOutA.writeShort(staticVectorFieldRef);
            dataOutA.writeByte(177); // return
            dataOutA.writeShort(0); // exception_table_length
            dataOutA.writeShort(0); // attributes_count
            dataOutA.flush();

            byte[] code = byteBufferA.toByteArray();
            return new AttributeInfo(getUtf8Index("Code"), code.length, code);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static AttributeInfo createRegClassCode() {
        try {
            byteBufferA.reset();
            dataOutA.writeShort(4); // max_stack
            dataOutA.writeShort(1); // max_locals
            dataOutA.writeInt(15);  // code_length
            dataOutA.writeByte(178); // getstatic
            dataOutA.writeShort(staticVectorFieldRef);
            dataOutA.writeByte(187); // new
            dataOutA.writeShort(2);
            dataOutA.writeByte(89);  // dup
            dataOutA.writeByte(26);  // iload_0
            dataOutA.writeByte(183); // invokespecial
            dataOutA.writeShort(initIntMethodRef + 1);
            dataOutA.writeByte(182); // invokevirtual
            dataOutA.writeShort(initIntMethodRef + 2);
            dataOutA.writeByte(177); // return
            dataOutA.writeInt(0);
            dataOutA.flush();

            byte[] code = byteBufferA.toByteArray();
            return new AttributeInfo(getUtf8Index("Code"), code.length, code);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static AttributeInfo createCinitclonesCode() {
        try {
            byteBufferA.reset();
            byteBufferB.reset();
            int count = classNames.size() - 4;

            dataOutB.writeShort(getUtf8Index("StackMap"));
            dataOutB.writeInt(count * 10 + 12);
            dataOutB.writeShort(count + 1);

            dataOutA.writeShort(1);
            dataOutA.writeShort(2);
            int codeLen = (count << 3) + 16;
            dataOutA.writeInt(codeLen);
            dataOutA.writeByte(27);  // iload_1
            dataOutA.writeByte(170); // tableswitch

            int offset = (count << 2) + 15;
            codeLen -= 2;
            dataOutA.writeShort(0);
            dataOutA.writeInt(codeLen);
            dataOutA.writeInt(0);
            dataOutA.writeInt(count - 1);

            for (int i = 0; i < count; i++) {
                dataOutA.writeInt(offset);
                dataOutB.writeShort(offset + 1);
                offset += 4;
                dataOutB.writeShort(2);
                dataOutB.writeByte(7);
                dataOutB.writeShort(1);
                dataOutB.writeByte(1);
                dataOutB.writeShort(0);
            }

            dataOutB.writeShort(codeLen + 1);
            dataOutB.writeShort(2);
            dataOutB.writeByte(7);
            dataOutB.writeShort(1);
            dataOutB.writeByte(1);
            dataOutB.writeShort(0);

            int endMethod = count + initIntMethodRef + 3;
            for (int i = initIntMethodRef + 3; i < endMethod; i++) {
                dataOutA.writeByte(184); // invokestatic
                dataOutA.writeShort(i);
                dataOutA.writeByte(177); // return
            }

            dataOutA.writeShort(0);
            dataOutA.writeShort(1);
            dataOutA.write(byteBufferB.toByteArray());
            dataOutA.flush();

            byte[] code = byteBufferA.toByteArray();
            return new AttributeInfo(getUtf8Index("Code"), code.length, code);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static AttributeInfo createClearsCode() {
        try {
            byteBufferA.reset();
            byteBufferB.reset();
            int count = classNames.size() - 4;

            dataOutB.writeShort(getUtf8Index("StackMap"));
            dataOutB.writeInt(count * 10 + 12);
            dataOutB.writeShort(count + 1);

            dataOutA.writeShort(1);
            dataOutA.writeShort(2);
            int codeLen = (count << 3) + 16;
            dataOutA.writeInt(codeLen);
            dataOutA.writeByte(27);  // iload_1
            dataOutA.writeByte(170); // tableswitch

            int offset = (count << 2) + 15;
            codeLen -= 2;
            dataOutA.writeShort(0);
            dataOutA.writeInt(codeLen);
            dataOutA.writeInt(0);
            dataOutA.writeInt(count - 1);

            for (int i = 0; i < count; i++) {
                dataOutA.writeInt(offset);
                dataOutB.writeShort(offset + 1);
                offset += 4;
                dataOutB.writeShort(2);
                dataOutB.writeByte(7);
                dataOutB.writeShort(1);
                dataOutB.writeByte(1);
                dataOutB.writeShort(0);
            }

            dataOutB.writeShort(codeLen + 1);
            dataOutB.writeShort(2);
            dataOutB.writeByte(7);
            dataOutB.writeShort(1);
            dataOutB.writeByte(1);
            dataOutB.writeShort(0);

            int endMethod = (count << 1) + initIntMethodRef + 3;
            for (int i = initIntMethodRef + 3 + count; i < endMethod; i++) {
                dataOutA.writeByte(184); // invokestatic
                dataOutA.writeShort(i);
                dataOutA.writeByte(177); // return
            }

            dataOutA.writeShort(0);
            dataOutA.writeShort(1);
            dataOutA.write(byteBufferB.toByteArray());
            dataOutA.flush();

            byte[] code = byteBufferA.toByteArray();
            return new AttributeInfo(getUtf8Index("Code"), code.length, code);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
