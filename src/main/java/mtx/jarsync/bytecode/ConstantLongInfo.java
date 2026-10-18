package mtx.jarsync.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_Long (tag 5)
 */
public final class ConstantLongInfo extends CpInfo {
    public long value;

    public ConstantLongInfo(long value) {
        this.value = value;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(5);
            dos.writeLong(this.value);
        } catch (IOException e) {
        }
    }
}
