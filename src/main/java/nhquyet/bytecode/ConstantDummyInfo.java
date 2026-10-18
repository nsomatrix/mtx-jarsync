package nhquyet.bytecode;

import java.io.DataOutputStream;

/**
 * Dummy entry occupying the second 8-byte slot for CONSTANT_Long and CONSTANT_Double.
 */
public final class ConstantDummyInfo extends CpInfo {
    public static final ConstantDummyInfo INSTANCE = new ConstantDummyInfo();

    public void write(DataOutputStream dos) {
    }
}
