package nhquyet.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_Methodref (tag 10)
 */
public final class ConstantMethodrefInfo extends CpInfo {
    public int classIndex;
    public int nameAndTypeIndex;

    public ConstantMethodrefInfo(int classIndex, int nameAndTypeIndex) {
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(10);
            dos.writeShort(this.classIndex);
            dos.writeShort(this.nameAndTypeIndex);
        } catch (IOException e) {
        }
    }
}
