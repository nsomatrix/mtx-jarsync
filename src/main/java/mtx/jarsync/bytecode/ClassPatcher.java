package mtx.jarsync.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import mtx.jarsync.model.IntPair;

/**
 * Bytecode transformer and patcher for J2ME MIDlets.
 * Redirects runtime APIs (Display, Canvas, MIDlet, RecordStore) to isolated
 * wrapper hooks in MTX runtime overlay, intercepts paint/key events, and patches
 * static state initializers for clean multi-app switching.
 */
public final class ClassPatcher {
    private int thisClassIndex;
    private int superClassIndex;
    private Vector constantPool;
    private String currentPatchedClassName;
    private FieldInfo[] fields;
    private AttributeInfo[] attributes;
    private int[] interfaces;
    private int minorMajorVersion;
    private int accessFlags;

    private final Hashtable utf8IndexMap = new Hashtable();
    private final Hashtable classIndexMap = new Hashtable();

    private static final Hashtable classReplacements = new Hashtable(500);
    private static final Vector staticFieldsToClear = new Vector(500);
    public static final Vector patchedClasses = new Vector(500);
    private final Vector methods = new Vector(100);

    private static int midletClassCounter;
    private boolean hasStaticFields;
    private boolean hasClinit;

    private final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    private final DataOutputStream dataOut = new DataOutputStream(this.byteBuffer);

    public ClassPatcher() {
    }

    public static Hashtable initClassReplacements() {
        patchedClasses.removeAllElements();
        midletClassCounter = 0;
        Hashtable map = classReplacements;
        classReplacements.clear();
        map.put("javax/microedition/io/Connector", "lib/Connector");
        map.put("javax/microedition/lcdui/Form", "lib/Form");
        map.put("javax/microedition/lcdui/List", "lib/List");
        map.put("javax/microedition/lcdui/Alert", "lib/Alert");
        map.put("javax/microedition/lcdui/Canvas", "lib/Canvas");
        map.put("com/nokia/mid/ui/FullCanvas", "lib/FullCanvas");
        map.put("javax/microedition/midlet/MIDlet", "lib/MIDlet");
        map.put("javax/microedition/lcdui/Display", "lib/Display");
        map.put("javax/microedition/lcdui/TextBox", "lib/TextBox");
        map.put("javax/microedition/lcdui/AlertType", "lib/AlertType");
        map.put("javax/microedition/lcdui/game/GameCanvas", "lib/GameCanvas");
        return map;
    }

    public void readClassHeader(DataInputStream dis) {
        try {
            this.utf8IndexMap.clear();
            this.classIndexMap.clear();
            this.minorMajorVersion = dis.readInt();
            int cpCount = dis.readUnsignedShort();

            Vector cp = this.constantPool = new Vector(cpCount + 20);
            cp.addElement(ConstantDummyInfo.INSTANCE);

            for (int i = 1; i < cpCount; i++) {
                int tag = dis.read();
                switch (tag) {
                    case 1: // CONSTANT_Utf8
                        String val = dis.readUTF();
                        cp.addElement(new ConstantUtf8Info(val));
                        this.utf8IndexMap.put(val, new Integer(i));
                        break;
                    case 3: // CONSTANT_Integer
                        cp.addElement(new ConstantIntegerInfo(dis.readInt()));
                        break;
                    case 4: // CONSTANT_Float
                        cp.addElement(new ConstantFloatInfo(dis.readInt()));
                        break;
                    case 5: // CONSTANT_Long
                        cp.addElement(new ConstantLongInfo(dis.readLong()));
                        cp.addElement(ConstantDummyInfo.INSTANCE);
                        i++;
                        break;
                    case 6: // CONSTANT_Double
                        cp.addElement(new ConstantDoubleInfo(dis.readLong()));
                        cp.addElement(ConstantDummyInfo.INSTANCE);
                        i++;
                        break;
                    case 7: // CONSTANT_Class
                        cp.addElement(new ConstantClassInfo(dis.readUnsignedShort()));
                        break;
                    case 8: // CONSTANT_String
                        cp.addElement(new ConstantStringInfo(dis.readUnsignedShort()));
                        break;
                    case 9: // CONSTANT_Fieldref
                        cp.addElement(new ConstantFieldrefInfo(dis.readUnsignedShort(), dis.readUnsignedShort()));
                        break;
                    case 10: // CONSTANT_Methodref
                        cp.addElement(new ConstantMethodrefInfo(dis.readUnsignedShort(), dis.readUnsignedShort()));
                        break;
                    case 11: // CONSTANT_InterfaceMethodref
                        cp.addElement(new ConstantInterfaceMethodrefInfo(dis.readUnsignedShort(), dis.readUnsignedShort()));
                        break;
                    case 12: // CONSTANT_NameAndType
                        cp.addElement(new ConstantNameAndTypeInfo(dis.readUnsignedShort(), dis.readUnsignedShort()));
                        break;
                    default:
                        break;
                }
            }

            this.accessFlags = dis.readUnsignedShort();
            this.accessFlags = (this.accessFlags | 1) & -7; // Ensure ACC_PUBLIC, clear ACC_FINAL/ACC_SUPER constraints
            this.thisClassIndex = dis.readUnsignedShort();
            this.superClassIndex = dis.readUnsignedShort();

            ConstantClassInfo thisClass = (ConstantClassInfo) cp.elementAt(this.thisClassIndex);
            ConstantUtf8Info thisClassUtf8 = (ConstantUtf8Info) cp.elementAt(thisClass.nameIndex);
            String rep = (String) classReplacements.get(thisClassUtf8.value);
            this.currentPatchedClassName = (rep != null) ? rep : thisClassUtf8.value;
        } catch (Exception ignored) {
        }
    }

    public void patchClass(DataInputStream dis, char midletChar) {
        this.hasStaticFields = false;
        this.hasClinit = false;
        this.methods.removeAllElements();

        try {
            Vector cp = this.constantPool;
            int cpSize = cp.size();

            // Patch ConstantClass and ConstantMethodref entries
            for (int i = 1; i < cpSize; i++) {
                Object entry = cp.elementAt(i);
                if (entry instanceof ConstantClassInfo) {
                    ConstantClassInfo ci = (ConstantClassInfo) entry;
                    ConstantUtf8Info utf = (ConstantUtf8Info) cp.elementAt(ci.nameIndex);
                    if (!utf.isPatched) {
                        utf.isPatched = true;
                        String name = utf.value;
                        if (classReplacements.containsKey(name)) {
                            utf.value = (String) classReplacements.get(name);
                        } else if (name.startsWith("[")) {
                            int lIdx = name.indexOf('L') + 1;
                            if (lIdx > 0) {
                                String rep = (String) classReplacements.get(name.substring(lIdx, name.length() - 1));
                                if (rep != null) {
                                    utf.value = name.substring(0, lIdx) + rep + ';';
                                }
                            }
                        }
                    }
                } else if (entry instanceof ConstantMethodrefInfo) {
                    ConstantMethodrefInfo mr = (ConstantMethodrefInfo) entry;
                    ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) cp.elementAt(mr.nameAndTypeIndex);
                    String methodName = ((ConstantUtf8Info) cp.elementAt(nat.nameIndex)).value;
                    String targetClass = ((ConstantUtf8Info) cp.elementAt(((ConstantClassInfo) cp.elementAt(mr.classIndex)).nameIndex)).value;

                    if (targetClass.equals("java/lang/Class") && methodName.equals("forName") && midletChar != 'a') {
                        mr.classIndex = this.getOrCreateClassRef("lib/Class");
                        ConstantUtf8Info nameUtf = (ConstantUtf8Info) cp.elementAt(nat.nameIndex);
                        nameUtf.isPatched = true;
                        nameUtf.replacement = "forName".replace('f', midletChar);
                    } else if (targetClass.equals("javax/microedition/rms/RecordStore")) {
                        String suffix = methodName.substring(1);
                        if (suffix.equals("penRecordStore") || suffix.equals("istRecordStores") || suffix.equals("eleteRecordStore")) {
                            mr.classIndex = this.getOrCreateClassRef("lib/RecordStore");
                            ((ConstantUtf8Info) cp.elementAt(nat.nameIndex)).value = midletChar + suffix;
                        }
                    } else if (methodName.equals("paint") && !targetClass.equals("javax/microedition/lcdui/game/Sprite")) {
                        ConstantUtf8Info nameUtf = (ConstantUtf8Info) cp.elementAt(nat.nameIndex);
                        nameUtf.isPatched = true;
                        nameUtf.replacement = "PAINT";
                    } else {
                        patchMethodDescriptor((ConstantUtf8Info) cp.elementAt(nat.descriptorIndex));
                    }
                } else if (entry instanceof ConstantInterfaceMethodrefInfo) {
                    ConstantInterfaceMethodrefInfo imr = (ConstantInterfaceMethodrefInfo) entry;
                    ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) cp.elementAt(imr.nameAndTypeIndex);
                    patchMethodDescriptor((ConstantUtf8Info) cp.elementAt(nat.descriptorIndex));
                } else if (entry instanceof ConstantFieldrefInfo) {
                    ConstantFieldrefInfo fr = (ConstantFieldrefInfo) entry;
                    ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) cp.elementAt(fr.nameAndTypeIndex);
                    patchFieldDescriptor((ConstantUtf8Info) cp.elementAt(nat.descriptorIndex));
                }
            }

            // Patch NameAndType and String references
            cpSize = cp.size();
            for (int i = 1; i < cpSize; i++) {
                Object entry = cp.elementAt(i);
                if (entry instanceof ConstantNameAndTypeInfo) {
                    ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) entry;
                    ConstantUtf8Info nameUtf = (ConstantUtf8Info) cp.elementAt(nat.nameIndex);
                    String name = nameUtf.value;
                    if (name.equals("keyReleased") || name.equals("keyPressed")) {
                        nameUtf.replacement = name.replace('k', 'l');
                        nameUtf.isPatched = true;
                    }
                    if (nameUtf.isPatched && !nameUtf.replacement.equals(name)) {
                        nat.nameIndex = this.getUtf8ReplacementIndex(nameUtf);
                    }
                    ConstantUtf8Info descUtf = (ConstantUtf8Info) cp.elementAt(nat.descriptorIndex);
                    if (descUtf.isPatched && descUtf.value.indexOf('/') == 1) {
                        nat.descriptorIndex = this.getUtf8ReplacementIndex(descUtf);
                    }
                } else if (entry instanceof ConstantStringInfo) {
                    ConstantStringInfo str = (ConstantStringInfo) entry;
                    ConstantUtf8Info strUtf = (ConstantUtf8Info) cp.elementAt(str.stringIndex);
                    if (strUtf.isPatched && !strUtf.replacement.equals(strUtf.value)) {
                        str.stringIndex = this.getUtf8ReplacementIndex(strUtf);
                    }
                }
            }

            // Read interfaces
            int ifaceCount = dis.readUnsignedShort();
            this.interfaces = new int[ifaceCount];
            for (int i = 0; i < ifaceCount; i++) {
                this.interfaces[i] = dis.readUnsignedShort();
            }

            // Read and patch fields
            int fieldCount = dis.readUnsignedShort();
            this.fields = new FieldInfo[fieldCount];
            staticFieldsToClear.removeAllElements();

            for (int i = 0; i < fieldCount; i++) {
                int fAccess = dis.readUnsignedShort();
                int fNameIdx = dis.readUnsignedShort();
                int fDescIdx = dis.readUnsignedShort();

                ConstantUtf8Info nameUtf = (ConstantUtf8Info) cp.elementAt(fNameIdx);
                if (nameUtf.isPatched && !nameUtf.replacement.equals(nameUtf.value)) {
                    fNameIdx = this.getUtf8ReplacementIndex(nameUtf);
                }
                ConstantUtf8Info descUtf = (ConstantUtf8Info) cp.elementAt(fDescIdx);
                if (descUtf.isPatched && descUtf.value.indexOf('/') == 1) {
                    fDescIdx = this.getUtf8ReplacementIndex(descUtf);
                }
                patchFieldDescriptor((ConstantUtf8Info) cp.elementAt(fDescIdx));

                AttributeInfo[] fAttrs = this.readAttributes(dis);
                // Non-constant static field in a concrete class
                if (fAttrs.length == 0 && (fAccess & 8) != 0 && (this.accessFlags & 512) == 0) {
                    this.hasStaticFields = true;
                    IntPair pair = new IntPair();
                    pair.a = fNameIdx;
                    pair.b = fDescIdx;
                    staticFieldsToClear.addElement(pair);
                }

                this.fields[i] = new FieldInfo(fAccess, fNameIdx, fDescIdx);
                this.fields[i].attributes = fAttrs;
            }

            // Read and patch methods
            int methodCount = dis.readUnsignedShort();
            for (int i = 0; i < methodCount; i++) {
                int mAccess = dis.readUnsignedShort();
                int mNameIdx = dis.readUnsignedShort();
                int mDescIdx = dis.readUnsignedShort();

                ConstantUtf8Info nameUtf = (ConstantUtf8Info) cp.elementAt(mNameIdx);
                String mName = nameUtf.value;
                if (mName.equals("keyReleased") || mName.equals("keyPressed")) {
                    nameUtf.replacement = mName.replace('k', 'l');
                    nameUtf.isPatched = true;
                } else if (mName.equals("paint")) {
                    mNameIdx = this.addUtf8("PAINT");
                    nameUtf = (ConstantUtf8Info) cp.elementAt(mNameIdx);
                }

                if (nameUtf.isPatched && !nameUtf.replacement.equals(mName)) {
                    mNameIdx = this.getUtf8ReplacementIndex(nameUtf);
                }
                patchMethodDescriptor((ConstantUtf8Info) cp.elementAt(mDescIdx));

                MethodInfo mi = new MethodInfo(mAccess, mNameIdx, mDescIdx);
                mi.attributes = this.readAttributes(dis);
                this.methods.addElement(mi);

                // Clone <clinit> into cinitclone for multi-midlet isolation
                if (nameUtf.replacement.equals("<clinit>") && (this.accessFlags & 512) == 0) {
                    mi.nameIndex = this.getOrCreateUtf8("cinitclone");
                    mi.accessFlags = 9; // ACC_PUBLIC | ACC_STATIC
                    this.hasClinit = true;

                    MethodInfo newClinit = new MethodInfo(mAccess, mNameIdx, mDescIdx);
                    newClinit.attributes = this.createClinitPatchCode(midletChar);
                    this.methods.addElement(newClinit);
                }
            }

            this.attributes = this.readAttributes(dis);

            // Synthesize empty cinitclone and <clinit> if class has static fields but no original <clinit>
            if (this.hasStaticFields && !this.hasClinit) {
                this.hasClinit = true;

                MethodInfo emptyClone = new MethodInfo(9, this.getOrCreateUtf8("cinitclone"), this.getOrCreateUtf8("()V"));
                this.byteBuffer.reset();
                this.dataOut.writeShort(0); // max_stack
                this.dataOut.writeShort(1); // max_locals
                this.dataOut.writeInt(1);   // code_length
                this.dataOut.writeByte(177); // return
                this.dataOut.writeInt(0);
                byte[] code = this.byteBuffer.toByteArray();
                emptyClone.attributes = new AttributeInfo[]{new AttributeInfo(this.getOrCreateUtf8("Code"), code.length, code)};
                this.methods.addElement(emptyClone);

                MethodInfo newClinit = new MethodInfo(8, this.getOrCreateUtf8("<clinit>"), this.getOrCreateUtf8("()V"));
                newClinit.attributes = this.createClinitPatchCode(midletChar);
                this.methods.addElement(newClinit);
            }

            // Synthesize clears() method to reset static fields to default values
            if (this.hasStaticFields || this.hasClinit) {
                MethodInfo clearsMethod = new MethodInfo(9, this.getOrCreateUtf8("clears"), this.getOrCreateUtf8("()V"));
                clearsMethod.attributes = this.createClearsPatchCode(staticFieldsToClear);
                this.methods.addElement(clearsMethod);

                midletClassCounter++;
                if (this.currentPatchedClassName != null) {
                    patchedClasses.addElement(this.currentPatchedClassName);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private AttributeInfo[] createClinitPatchCode(char midletChar) throws IOException {
        this.byteBuffer.reset();
        this.dataOut.writeShort(1); // max_stack
        this.dataOut.writeShort(0); // max_locals
        this.dataOut.writeInt(midletClassCounter < 6 ? 8 : 10); // code_length

        if (midletClassCounter < 6) {
            this.dataOut.writeByte(3 + midletClassCounter); // iconst_0..iconst_5
        } else {
            this.dataOut.writeByte(17); // sipush
            this.dataOut.writeShort(midletClassCounter);
        }

        this.dataOut.writeByte(184); // invokestatic
        String staticClass = (midletChar == 'a') ? "Static" : (midletChar + "/Static");
        this.dataOut.writeShort(this.createMethodRef(this.getOrCreateClassRef(staticClass), "regClass", "(I)V"));

        this.dataOut.writeByte(184); // invokestatic
        this.dataOut.writeShort(this.createMethodRef(this.thisClassIndex, "cinitclone", "()V"));
        this.dataOut.writeByte(177); // return
        this.dataOut.writeInt(0);

        byte[] code = this.byteBuffer.toByteArray();
        return new AttributeInfo[]{new AttributeInfo(this.getOrCreateUtf8("Code"), code.length, code)};
    }

    private AttributeInfo[] createClearsPatchCode(Vector fieldPairs) {
        try {
            this.byteBuffer.reset();
            int total = fieldPairs.size();
            this.dataOut.writeShort(2); // max_stack
            this.dataOut.writeShort(0); // max_locals
            int codeLen = (total << 2) + 1;
            this.dataOut.writeInt(codeLen);

            Vector cp = this.constantPool;
            int cpSize = cp.size();

            for (int i = 0; i < total; i++) {
                IntPair pair = (IntPair) fieldPairs.elementAt(i);
                int fieldRefIdx = -1;

                // Look for existing Fieldref
                for (int j = 0; j < cpSize; j++) {
                    Object obj = cp.elementAt(j);
                    if (obj instanceof ConstantFieldrefInfo) {
                        ConstantFieldrefInfo fr = (ConstantFieldrefInfo) obj;
                        if (fr.classIndex == this.thisClassIndex) {
                            ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) cp.elementAt(fr.nameAndTypeIndex);
                            if (nat.nameIndex == pair.a && nat.descriptorIndex == pair.b) {
                                fieldRefIdx = j;
                                break;
                            }
                        }
                    }
                }

                // If not found, look for existing NameAndType or create one
                if (fieldRefIdx < 0) {
                    int natIdx = -1;
                    for (int j = 0; j < cpSize; j++) {
                        Object obj = cp.elementAt(j);
                        if (obj instanceof ConstantNameAndTypeInfo) {
                            ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) obj;
                            if (nat.nameIndex == pair.a && nat.descriptorIndex == pair.b) {
                                natIdx = j;
                                break;
                            }
                        }
                    }
                    if (natIdx < 0) {
                        cp.addElement(new ConstantNameAndTypeInfo(pair.a, pair.b));
                        natIdx = cp.size() - 1;
                    }
                    fieldRefIdx = this.createFieldRef(natIdx);
                }

                // Push default value (0 / null) onto stack based on descriptor type
                ConstantUtf8Info descUtf = (ConstantUtf8Info) cp.elementAt(pair.b);
                int pushOpcode;
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

                this.dataOut.writeByte(pushOpcode);
                this.dataOut.writeByte(179); // putstatic
                this.dataOut.writeShort(fieldRefIdx);
            }

            this.dataOut.writeByte(177); // return
            this.dataOut.writeInt(0);
            this.dataOut.flush();

            byte[] code = this.byteBuffer.toByteArray();
            return new AttributeInfo[]{new AttributeInfo(this.getOrCreateUtf8("Code"), code.length, code)};
        } catch (Exception e) {
            return null;
        }
    }

    private int getOrCreateClassRef(String className) {
        if (this.classIndexMap.containsKey(className)) {
            return ((Integer) this.classIndexMap.get(className)).intValue();
        }
        this.constantPool.addElement(new ConstantClassInfo(this.getOrCreateUtf8(className)));
        int idx = this.constantPool.size() - 1;
        this.classIndexMap.put(className, new Integer(idx));
        return idx;
    }

    private int getUtf8ReplacementIndex(ConstantUtf8Info utf8) {
        return this.getOrCreateUtf8(utf8.replacement);
    }

    private int createMethodRef(int classIdx, String name, String desc) {
        this.constantPool.addElement(new ConstantNameAndTypeInfo(this.getOrCreateUtf8(name), this.getOrCreateUtf8(desc)));
        this.constantPool.addElement(new ConstantMethodrefInfo(classIdx, this.constantPool.size() - 1));
        return this.constantPool.size() - 1;
    }

    private int getOrCreateUtf8(String str) {
        if (this.utf8IndexMap.containsKey(str)) {
            int idx = ((Integer) this.utf8IndexMap.get(str)).intValue();
            ConstantUtf8Info utf = (ConstantUtf8Info) this.constantPool.elementAt(idx);
            if (utf.isPatched && !utf.value.equals(str)) {
                this.utf8IndexMap.put(utf.value, new Integer(idx));
                idx = this.addUtf8(str);
                this.utf8IndexMap.put(str, new Integer(idx));
            }
            return idx;
        }
        return this.addUtf8(str);
    }

    private int addUtf8(String str) {
        int idx = this.constantPool.size();
        this.constantPool.addElement(new ConstantUtf8Info(str));
        this.utf8IndexMap.put(str, new Integer(idx));
        return idx;
    }

    public void writeClass(DataOutputStream dos) throws IOException {
        dos.writeInt(0xCAFEBABE);
        dos.writeInt(this.minorMajorVersion);

        int cpSize = this.constantPool.size();
        dos.writeShort(cpSize);
        for (int i = 1; i < cpSize; i++) {
            ((CpInfo) this.constantPool.elementAt(i)).write(dos);
        }

        dos.writeShort(this.accessFlags);
        dos.writeShort(this.thisClassIndex);
        dos.writeShort(this.superClassIndex);

        int ifaceLen = this.interfaces.length;
        dos.writeShort(ifaceLen);
        for (int i = 0; i < ifaceLen; i++) {
            dos.writeShort(this.interfaces[i]);
        }

        int fieldLen = this.fields.length;
        dos.writeShort(fieldLen);
        for (int i = 0; i < fieldLen; i++) {
            this.fields[i].write(dos);
        }

        int methodCount = this.methods.size();
        dos.writeShort(methodCount);
        for (int i = 0; i < methodCount; i++) {
            ((MethodInfo) this.methods.elementAt(i)).write(dos);
        }

        int attrLen = this.attributes.length;
        dos.writeShort(attrLen);
        for (int i = 0; i < attrLen; i++) {
            this.attributes[i].write(dos);
        }
    }

    private AttributeInfo[] readAttributes(DataInputStream dis) throws IOException {
        int count = dis.readUnsignedShort();
        AttributeInfo[] attrs = new AttributeInfo[count];
        for (int i = 0; i < count; i++) {
            int nameIdx = dis.readUnsignedShort();
            ConstantUtf8Info nameUtf = (ConstantUtf8Info) this.constantPool.elementAt(nameIdx);
            if (nameUtf.isPatched && !nameUtf.replacement.equals(nameUtf.value)) {
                nameIdx = this.getUtf8ReplacementIndex(nameUtf);
            }
            int len = dis.readInt();
            byte[] info = new byte[len];
            dis.readFully(info);
            attrs[i] = new AttributeInfo(nameIdx, len, info);
        }
        return attrs;
    }

    private static void patchFieldDescriptor(ConstantUtf8Info utf) {
        if (!utf.isPatched) {
            utf.isPatched = true;
            String desc = utf.value;
            if (desc.startsWith("[") || desc.startsWith("L")) {
                int lIdx = desc.indexOf('L') + 1;
                if (lIdx > 0) {
                    String rep = (String) classReplacements.get(desc.substring(lIdx, desc.length() - 1));
                    if (rep != null) {
                        utf.value = desc.substring(0, lIdx) + rep + ';';
                    }
                }
            }
        }
    }

    private static void patchMethodDescriptor(ConstantUtf8Info utf) {
        if (!utf.isPatched) {
            utf.isPatched = true;
            String desc = utf.value;
            if (!desc.startsWith("(") || desc.indexOf(';') < 0) {
                return;
            }

            StringBuffer sb = new StringBuffer();
            int lIdx;
            while ((lIdx = desc.indexOf('L') + 1) > 0) {
                sb.append(desc.substring(0, lIdx));
                desc = desc.substring(lIdx);
                int semi = desc.indexOf(';') + 1;
                if (semi <= 0) {
                    break;
                }
                String className = desc.substring(0, semi - 1);
                String rep = (String) classReplacements.get(className);
                if (rep != null) {
                    sb.append(rep).append(';');
                } else {
                    sb.append(desc.substring(0, semi));
                }
                desc = desc.substring(semi);
            }
            sb.append(desc);
            utf.value = sb.toString();
        }
    }

    private int createFieldRef(int natIdx) {
        this.constantPool.addElement(new ConstantFieldrefInfo(this.thisClassIndex, natIdx));
        return this.constantPool.size() - 1;
    }
}
