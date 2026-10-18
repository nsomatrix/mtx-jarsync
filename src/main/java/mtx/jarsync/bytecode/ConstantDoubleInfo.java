package mtx.jarsync.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_Double (tag 6)
 */
public final class ConstantDoubleInfo extends CpInfo {
    public long value;

    public ConstantDoubleInfo(long value) {
        this.value = value;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(6);
            dos.writeLong(this.value);
        } catch (IOException e) {
        }
    }
}
