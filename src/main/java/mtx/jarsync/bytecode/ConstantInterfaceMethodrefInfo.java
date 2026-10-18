package mtx.jarsync.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_InterfaceMethodref (tag 11)
 */
public final class ConstantInterfaceMethodrefInfo extends CpInfo {
    public int classIndex;
    public int nameAndTypeIndex;

    public ConstantInterfaceMethodrefInfo(int classIndex, int nameAndTypeIndex) {
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(11);
            dos.writeShort(this.classIndex);
            dos.writeShort(this.nameAndTypeIndex);
        } catch (IOException e) {
        }
    }
}
