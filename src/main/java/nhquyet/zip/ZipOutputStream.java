package nhquyet.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Streaming ZIP archive output stream.
 * Supports STORED (0) and DEFLATED (8) compression methods.
 */
public final class ZipOutputStream extends OutputStream {
    private static final int[] CRC_TABLE = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int c = i;
            for (int k = 8; --k >= 0; ) {
                if ((c & 1) != 0) {
                    c = 0xEDB88320 ^ (c >>> 1);
                } else {
                    c >>>= 1;
                }
            }
            CRC_TABLE[i] = c;
        }
    }

    private final Vector entries = new Vector();
    private int crc;
    public ZipEntry currentEntry = null;
    private int method;
    private int bytesWrittenForEntry;
    private final byte[] buffer;
    private final Deflater deflater;
    private LittleEndianOutputStream out;

    public ZipOutputStream(LittleEndianOutputStream out) {
        this.out = out;
        this.buffer = new byte[10240];
        this.deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
        this.crc = 0;
        this.method = 8;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public void putNextEntry(String name) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.flags = 8; // Bit 3 set: crc and sizes placed in data descriptor if not rewound
        entry.method = (short) this.method;

        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        if (now > 0L) {
            cal.setTime(new Date(now));
        }

        entry.time = (cal.get(Calendar.YEAR) - 1980 << 25)
                   | (cal.get(Calendar.MONTH) + 1 << 21)
                   | (cal.get(Calendar.DAY_OF_MONTH) << 16)
                   | (cal.get(Calendar.HOUR_OF_DAY) << 11)
                   | (cal.get(Calendar.MINUTE) << 5)
                   | (cal.get(Calendar.SECOND) >> 1);

        this.putNextEntry(entry);
    }

    public void putNextEntry(ZipEntry entry) throws IOException {
        if (this.currentEntry != null) {
            this.closeEntry();
        }

        this.currentEntry = entry;
        this.entries.addElement(entry);
        entry.offset = this.out.written;

        // Local file header signature 0x04034b50
        this.out.writeIntLE(0x04034b50);
        this.out.writeShortLE(entry.method == 0 ? 10 : 20); // Version needed to extract
        this.writeHeaderFields(entry);
        this.out.write(entry.nameBytes);

        this.crc = 0;
        this.deflater.reset();
        this.bytesWrittenForEntry = 0;
    }

    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    public void write(int b) throws IOException {
        this.write(new byte[]{(byte) b}, 0, 1);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len <= 0) {
            return;
        }

        if (this.method == 0) {
            this.out.write(b, off, len);
        } else {
            this.deflater.setInput(b, off, len);
            while (!this.deflater.needsInput()) {
                int deflated = this.deflater.deflate(this.buffer, 0, this.buffer.length);
                if (deflated > 0) {
                    this.out.write(this.buffer, 0, deflated);
                } else {
                    break;
                }
            }
        }

        // Update CRC32
        int c = ~this.crc;
        int end = off + len;
        for (int i = off; i < end; i++) {
            c = CRC_TABLE[(c ^ b[i]) & 0xFF] ^ (c >>> 8);
        }
        this.crc = ~c;
        this.bytesWrittenForEntry += len;
    }

    public void closeEntry() throws IOException {
        if (this.currentEntry == null) {
            return;
        }

        if (this.method == 8) {
            this.deflater.finish();
            while (!this.deflater.finished()) {
                int deflated = this.deflater.deflate(this.buffer, 0, this.buffer.length);
                if (deflated > 0) {
                    this.out.write(this.buffer, 0, deflated);
                } else {
                    break;
                }
            }
        }

        ZipEntry entry = this.currentEntry;
        entry.size = this.bytesWrittenForEntry;
        entry.compressedSize = (this.method == 8) ? (int) this.deflater.getTotalOut() : this.bytesWrittenForEntry;
        entry.crc = this.crc;

        int oldPos = this.out.written;
        // Check if we can rewind back to header fields (offset + 6)
        if (!this.out.rewindTo(entry.offset + 6)) {
            // Stream has already flushed buffer: append data descriptor (0x08074b50)
            this.out.writeIntLE(0x08074b50);
            this.out.writeIntLE(entry.crc);
            this.out.writeIntLE(entry.compressedSize);
            this.out.writeIntLE(entry.size);
        } else {
            // Buffer rewind succeeded: patch local header directly
            entry.flags = 0;
            this.writeHeaderFields(entry);
            this.out.advanceTo(oldPos);
        }

        this.out.flush();
        this.currentEntry = null;
    }

    private void writeHeaderFields(ZipEntry entry) throws IOException {
        this.out.writeShortLE(entry.flags);
        this.out.writeShortLE(entry.method);
        this.out.writeIntLE(entry.time);
        this.out.writeIntLE(entry.crc);
        this.out.writeIntLE(entry.compressedSize);
        this.out.writeIntLE(entry.size);
        this.out.writeShortLE(entry.nameLength);
        this.out.writeShortLE(0); // Extra field length
    }

    public void finish() throws IOException {
        if (this.out == null) {
            return;
        }

        if (this.currentEntry != null) {
            this.closeEntry();
        }

        int centralDirStart = this.out.written;
        int entryCount = 0;
        int centralDirSize = 0;

        Enumeration enumEntries = this.entries.elements();
        while (enumEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) enumEntries.nextElement();
            short method = entry.method;

            // Central directory file header signature 0x02014b50
            this.out.writeIntLE(0x02014b50);
            this.out.writeShortLE(method == 0 ? 10 : 20); // Version made by
            this.out.writeShortLE(method == 0 ? 10 : 20); // Version needed to extract
            this.writeHeaderFields(entry);
            this.out.writeShortLE(0); // File comment length
            this.out.writeShortLE(0); // Disk number start
            this.out.writeShortLE(0); // Internal file attributes
            this.out.writeIntLE(0);   // External file attributes
            this.out.writeIntLE(entry.offset); // Relative offset of local header
            this.out.write(entry.nameBytes);

            entryCount++;
            centralDirSize += 46 + entry.nameLength;
        }

        // End of central directory record signature 0x06054b50
        this.out.writeIntLE(0x06054b50);
        this.out.writeShortLE(0); // Number of this disk
        this.out.writeShortLE(0); // Disk with start of central directory
        this.out.writeShortLE(entryCount); // Total entries on this disk
        this.out.writeShortLE(entryCount); // Total entries in central directory
        this.out.writeIntLE(centralDirSize);  // Size of central directory
        this.out.writeIntLE(centralDirStart); // Offset of start of central directory
        this.out.writeShortLE(0); // ZIP file comment length
        this.out.flush();
        this.out = null;
    }

    public void close() throws IOException {
        this.finish();
    }
}
