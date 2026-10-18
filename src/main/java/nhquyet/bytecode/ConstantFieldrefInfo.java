package nhquyet.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_Fieldref (tag 9)
 */
public final class ConstantFieldrefInfo extends CpInfo {
    public int classIndex;
    public int nameAndTypeIndex;

    public ConstantFieldrefInfo(int classIndex, int nameAndTypeIndex) {
        this.classIndex = classIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(9);
            dos.writeShort(this.classIndex);
            dos.writeShort(this.nameAndTypeIndex);
        } catch (IOException e) {
        }
    }
}
