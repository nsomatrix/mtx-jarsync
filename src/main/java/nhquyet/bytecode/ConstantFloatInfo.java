package nhquyet.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_Float (tag 4)
 */
public final class ConstantFloatInfo extends CpInfo {
    public int value;

    public ConstantFloatInfo(int value) {
        this.value = value;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(4);
            dos.writeInt(this.value);
        } catch (IOException e) {
        }
    }
}
