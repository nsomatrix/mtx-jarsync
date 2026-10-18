package nhquyet.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * JVM class file method_info representation.
 */
public final class MethodInfo {
    public int accessFlags;
    public int nameIndex;
    public int descriptorIndex;
    public AttributeInfo[] attributes;

    public MethodInfo(int accessFlags, int nameIndex, int descriptorIndex) {
        this.accessFlags = accessFlags;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeShort(this.accessFlags);
        dos.writeShort(this.nameIndex);
        dos.writeShort(this.descriptorIndex);
        if (this.attributes != null) {
            dos.writeShort(this.attributes.length);
            for (int i = 0; i < this.attributes.length; i++) {
                this.attributes[i].write(dos);
            }
        } else {
            dos.writeShort(0);
        }
    }
}
