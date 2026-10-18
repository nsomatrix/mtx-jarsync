package mtx.jarsync.zip;

import java.io.InputStream;

/**
 * Fast in-memory byte buffer input stream with endian-aware primitives.
 */
public final class ByteInputStream extends InputStream {
    public int pos;
    public byte[] buf;

    public ByteInputStream(byte[] buf) {
        this.buf = buf;
        this.pos = 0;
    }

    public void close() {
        this.buf = null;
    }

    public void readFully(byte[] dest, int len) {
        System.arraycopy(this.buf, this.pos, dest, 0, len);
        this.pos += len;
    }

    public long skip(long n) {
        this.pos = (int)((long)this.pos + n);
        return n;
    }

    public int read(byte[] b, int off, int len) {
        System.arraycopy(this.buf, this.pos, b, off, len);
        this.pos += len;
        return len;
    }

    public int read(byte[] b) {
        return this.read(b, 0, b.length);
    }

    /**
     * Reads a 32-bit big-endian integer.
     */
    public int readIntBE() {
        return ((this.buf[this.pos++] & 0xFF) << 24)
             | ((this.buf[this.pos++] & 0xFF) << 16)
             | ((this.buf[this.pos++] & 0xFF) << 8)
             |  (this.buf[this.pos++] & 0xFF);
    }

    /**
     * Reads a 16-bit little-endian integer.
     */
    public int readShortLE() {
        return (this.buf[this.pos++] & 0xFF) | ((this.buf[this.pos++] & 0xFF) << 8);
    }

    /**
     * Reads a 32-bit little-endian integer.
     */
    public int readIntLE() {
        return  (this.buf[this.pos++] & 0xFF)
             | ((this.buf[this.pos++] & 0xFF) << 8)
             | ((this.buf[this.pos++] & 0xFF) << 16)
             | ((this.buf[this.pos++] & 0xFF) << 24);
    }

    public int read() {
        return this.buf[this.pos++];
    }
}
