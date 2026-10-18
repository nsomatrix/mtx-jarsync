package mtx.jarsync.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM Constant Pool entry: CONSTANT_Utf8 (tag 1)
 */
public final class ConstantUtf8Info extends CpInfo {
    public String value;
    public String replacement;
    public boolean isPatched;

    public ConstantUtf8Info(String value) {
        this.value = value;
        this.replacement = value;
        this.isPatched = false;
    }

    public void write(DataOutputStream dos) {
        try {
            dos.write(1);
            dos.writeUTF(this.value);
        } catch (IOException e) {
        }
    }
}
