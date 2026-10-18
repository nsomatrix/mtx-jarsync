package nhquyet.zip;

/**
 * ZIP file entry metadata container representing a file inside a ZIP archive.
 */
public final class ZipEntry {
    public byte[] nameBytes = null;
    public int nameLength;
    public int time;
    public int size;
    public int compressedSize;
    public int offset;
    public int crc;
    public short method;
    public String name;
    public int flags;

    public ZipEntry(String name) {
        if (name != null) {
            this.name = name.replace('\\', '/');
            this.nameBytes = this.name.getBytes();
            this.nameLength = this.nameBytes.length;
        }
    }

    public void setNameBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 92) { // '\\'
                bytes[i] = 47;    // '/'
            }
        }
        this.nameLength = bytes.length;
        this.nameBytes = bytes;
        this.name = new String(bytes);
    }

    public String toString() {
        return this.name;
    }
}
