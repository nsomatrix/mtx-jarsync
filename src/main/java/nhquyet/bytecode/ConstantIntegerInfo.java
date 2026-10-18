package nhquyet.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_Integer (tag 3)
 */
public final class ConstantIntegerInfo extends CpInfo {
    public int value;

    public ConstantIntegerInfo(int value) {
        this.value = value;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(3);
            dos.writeInt(this.value);
        } catch (IOException e) {
        }
    }
}
