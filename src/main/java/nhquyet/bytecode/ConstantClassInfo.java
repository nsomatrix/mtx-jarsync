package nhquyet.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_Class (tag 7)
 */
public final class ConstantClassInfo extends CpInfo {
    public int nameIndex;

    public ConstantClassInfo(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(7);
            dos.writeShort(this.nameIndex);
        } catch (IOException e) {
        }
    }
}
