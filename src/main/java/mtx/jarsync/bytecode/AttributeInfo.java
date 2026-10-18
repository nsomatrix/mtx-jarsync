package mtx.jarsync.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM class file attribute_info representation.
 */
public final class AttributeInfo {
    public int nameIndex;
    public int length;
    public byte[] info;

    public AttributeInfo(int nameIndex, int length, byte[] info) {
        this.nameIndex = nameIndex;
        this.length = length;
        this.info = info;
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeShort(this.nameIndex);
        dos.writeInt(this.length);
        dos.write(this.info);
    }
}
