package nhquyet.zip;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Buffered output stream that supports little-endian primitive writes
 * and rewinding for ZIP local header patching.
 */
public final class LittleEndianOutputStream extends OutputStream {
    private OutputStream out;
    private final byte[] buf;
    private int count;
    public int written;

    public LittleEndianOutputStream(OutputStream out) {
        this.out = out;
        this.buf = new byte[40960];
        this.count = 0;
        this.written = 0;
    }

    /**
     * Checks if the position can be rewound within the current buffer.
     * If so, rewinds the buffer pointer and updates written count.
     */
    public boolean rewindTo(int pos) {
        int diff = this.written - pos;
        if (diff <= this.count) {
            this.count -= diff;
            this.written = pos;
            return true;
        }
        return false;
    }

    /**
     * Advances the write pointer back to the specified position.
     */
    public void advanceTo(int pos) {
        this.count += (pos - this.written);
        this.written = pos;
    }

    public void flush() throws IOException {
        if (this.count > 0) {
            this.out.write(this.buf, 0, this.count);
            this.count = 0;
        }
    }

    public void close() throws IOException {
        this.flush();
        OutputStream o = this.out;
        this.out = null;
        if (o != null) {
            o.close();
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len < this.buf.length - this.count) {
            System.arraycopy(b, off, this.buf, this.count, len);
            this.count += len;
        } else {
            this.flush();
            this.out.write(b, off, len);
        }
        this.written += len;
    }

    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    /**
     * Writes a 16-bit little-endian integer.
     */
    public void writeShortLE(int val) throws IOException {
        if (this.buf.length < this.count + 2) {
            this.flush();
        }
        this.written += 2;
        this.buf[this.count++] = (byte) val;
        this.buf[this.count++] = (byte) (val >> 8);
    }

    /**
     * Writes a 32-bit little-endian integer.
     */
    public void writeIntLE(int val) throws IOException {
        if (this.buf.length < this.count + 4) {
            this.flush();
        }
        this.written += 4;
        this.buf[this.count++] = (byte) val;
        this.buf[this.count++] = (byte) (val >> 8);
        this.buf[this.count++] = (byte) (val >> 16);
        this.buf[this.count++] = (byte) (val >> 24);
    }

    public void write(int b) throws IOException {
        if (this.buf.length < this.count + 1) {
            this.flush();
        }
        this.written++;
        this.buf[this.count++] = (byte) b;
    }
}
