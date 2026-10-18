package nhquyet.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_NameAndType (tag 12)
 */
public final class ConstantNameAndTypeInfo extends CpInfo {
    public int nameIndex;
    public int descriptorIndex;

    public ConstantNameAndTypeInfo(int nameIndex, int descriptorIndex) {
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(12);
            dos.writeShort(this.nameIndex);
            dos.writeShort(this.descriptorIndex);
        } catch (IOException e) {
        }
    }
}
