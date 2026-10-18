package mtx.jarsync.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import mtx.jarsync.zip.ZipEntry;

/**
 * Represents a single MIDlet target JAR with its manifest attributes,
 * class entries, and resource files.
 */
public final class MidletEntry {
    private static final byte[] LINE_BUFFER = new byte[4096];

    public String jarPath;
    public Vector entries;
    public String[] titles;
    public String iconName;
    public String pngName;
    public String iniName;
    public Hashtable manifestProperties;
    public String midlet1;
    public String midlet2;
    public int selectedIndex;

    public MidletEntry(String jarPath, Vector entries) {
        this.jarPath = jarPath;
        this.entries = entries;
    }

    /**
     * Parses the raw MANIFEST.MF bytes and populates MIDlet attributes.
     */
    public boolean parseManifest(byte[] data) {
        try {
            this.manifestProperties = parseManifestAttributes(data);
            this.titles = new String[27];
            this.titles[0] = "10";

            this.midlet1 = (String) this.manifestProperties.get("MIDlet-1");
            if (this.midlet1 != null) {
                String str = this.midlet1;
                int firstComma = str.indexOf(',') + 1;
                int secondComma = str.indexOf(',', firstComma);
                if (firstComma > 0 && secondComma > firstComma) {
                    this.iconName = str.substring(firstComma, secondComma).trim().substring(1);
                }

                long now = System.currentTimeMillis();
                this.pngName = now + ".png";
                this.iniName = now + ".ini";

                String appName = str.substring(0, firstComma - 1).trim();
                for (int i = 1; i < 27; i++) {
                    this.titles[i] = appName + " " + i;
                }
            }

            this.midlet2 = (String) this.manifestProperties.get("MIDlet-2");
            if (this.midlet2 != null) {
                int comma = this.midlet2.indexOf(',');
                if (comma > 0) {
                    this.titles[26] = this.midlet2.substring(0, comma).trim();
                } else {
                    this.titles[26] = this.midlet2.trim();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Hashtable parseManifestAttributes(byte[] data) {
        Hashtable props = new Hashtable();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        String currentKey = "";
        try {
            String line;
            while ((line = readManifestLine(bais)) != null) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    currentKey = line.substring(0, colon).trim();
                    props.put(currentKey, line.substring(colon + 1).trim());
                } else {
                    String trimmed = line.trim();
                    if (trimmed.length() > 0 && currentKey.length() > 0) {
                        String oldVal = (String) props.get(currentKey);
                        props.put(currentKey, (oldVal != null ? oldVal : "") + trimmed);
                    }
                }
            }
            bais.close();
        } catch (IOException ignored) {
        }
        return props;
    }

    private static synchronized String readManifestLine(InputStream in) throws IOException {
        int len = 0;
        while (true) {
            int b = in.read();
            switch (b) {
                case -1:
                case 10: // '\n'
                    if (b == -1 && len == 0) {
                        return null;
                    }
                    return new String(LINE_BUFFER, 0, len, "utf-8");
                case 13: // '\r'
                    in.read(); // consume following '\n' if present
                    if (len == 0) {
                        return "";
                    }
                    return new String(LINE_BUFFER, 0, len, "utf-8");
                default:
                    if (len < LINE_BUFFER.length) {
                        LINE_BUFFER[len++] = (byte) b;
                    }
                    break;
            }
        }
    }

    /**
     * Filters all entries that end with .class.
     */
    public Vector getClassEntries() {
        if (this.entries == null) {
            return new Vector();
        }
        int total = this.entries.size() - 1;
        Vector classEntries = new Vector(total);
        for (int i = 1; i <= total; i++) {
            ZipEntry e = (ZipEntry) this.entries.elementAt(i);
            if (e.name != null && e.name.endsWith(".class")) {
                classEntries.addElement(e);
            }
        }
        return classEntries;
    }

    /**
     * Filters all non-class entries (resources, assets, configs).
     */
    public Vector getResourceEntries() {
        if (this.entries == null) {
            return new Vector();
        }
        int total = this.entries.size() - 1;
        Vector resEntries = new Vector(total);
        for (int i = 1; i <= total; i++) {
            ZipEntry e = (ZipEntry) this.entries.elementAt(i);
            if (e.name != null && !e.name.endsWith(".class")) {
                ZipEntry copy = new ZipEntry(e.name);
                copy.method = e.method;
                copy.flags = e.flags;
                copy.compressedSize = e.compressedSize;
                copy.size = e.size;
                copy.crc = e.crc;
                copy.time = e.time;
                copy.offset = e.offset;
                resEntries.addElement(copy);
            }
        }
        return resEntries;
    }
}
