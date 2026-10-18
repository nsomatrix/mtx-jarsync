package mtx.jarsync.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_String (tag 8)
 */
public final class ConstantStringInfo extends CpInfo {
    public int stringIndex;

    public ConstantStringInfo(int stringIndex) {
        this.stringIndex = stringIndex;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(8);
            dos.writeShort(this.stringIndex);
        } catch (IOException e) {
        }
    }
}
