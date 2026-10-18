package nhquyet.zip;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Buffered output stream with little-endian writing methods and position tracking.
 */
public final class DeflaterOutputStream extends OutputStream {
    private OutputStream out;
    private byte[] buf;
    private int count;
    public int written;

    public DeflaterOutputStream(OutputStream out) {
        this.out = out;
        this.buf = new byte[40960];
        this.count = 0;
        this.written = 0;
    }

    public boolean seekBack(int pos) {
        int diff = this.written - pos;
        if (diff <= this.count) {
            this.count -= diff;
            this.written = pos;
            return true;
        }
        return false;
    }

    public void seekForward(int pos) {
        this.count += pos - this.written;
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
        this.buf = null;
        this.out.close();
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

    public void writeShortLE(int val) throws IOException {
        if (this.buf.length < this.count + 2) {
            this.flush();
        }
        this.written += 2;
        this.buf[this.count++] = (byte)val;
        this.buf[this.count++] = (byte)(val >> 8);
    }

    public void writeIntLE(int val) throws IOException {
        if (this.buf.length < this.count + 4) {
            this.flush();
        }
        this.written += 4;
        this.buf[this.count++] = (byte)val;
        this.buf[this.count++] = (byte)(val >> 8);
        this.buf[this.count++] = (byte)(val >> 16);
        this.buf[this.count++] = (byte)(val >> 24);
    }

    public void write(int b) throws IOException {
        if (this.buf.length < this.count + 1) {
            this.flush();
        }
        this.written++;
        this.buf[this.count++] = (byte)b;
    }
}
