package mtx.jarsync.bytecode;

import java.io.DataOutputStream;

/**
 * Base abstract class for JVM constant pool entries.
 */
public abstract class CpInfo {
    public abstract void write(DataOutputStream dos);
}
