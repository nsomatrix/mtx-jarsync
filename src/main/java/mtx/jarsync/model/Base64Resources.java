package mtx.jarsync.model;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import javax.microedition.lcdui.Image;

/**
 * Embedded resource manager providing localized string tables and base64 UI icons.
 */
public final class Base64Resources {
    private static Base64Resources instance;
    private static final Hashtable RESOURCES = new Hashtable();
    private static final Hashtable VI_RESOURCES = new Hashtable();
    private static String currentLanguage = "en";
    private static final byte[] BASE64_DECODE_TABLE = new byte[256];

    static {
        for (int i = 0; i < 255; i++) {
            BASE64_DECODE_TABLE[i] = -1;
        }
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        for (int i = 0; i < 64; i++) {
            BASE64_DECODE_TABLE[alphabet[i]] = (byte) i;
        }

        RESOURCES.put("a", "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAaVBMVEX//////5zGmhj///+lcgz/64T/85yceRj/54T/43vw5Hn/14T/12v91W//x1riwU/av2vnvlLvukL//6W/mSjAlhj/+/f7+Zb/95TOoiHPoiXJoCD/84z38InOvFudehnXuD/is0LeskJgRnPAAAAAAXRSTlMAQObYZgAAAGVJREFUeF6NzMUSAjEURNEnybgrbv//kaRDhhRVLObs+i6a/jg6P1sdiVuUHRXPh+QCCnJHsK1ExoVSOtVFIVOEWq4cNCvC8zwegv6GcHqwDQaDUyk4D16fwNVmQqA5SslLv2iXNzT1Bkn/ZlYxAAAAAElFTkSuQmCC");
        RESOURCES.put("b", "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABWElEQVR42qSSu0rEQBSG/4lB1rUwbzBYe2MLGx9ARRTFW2thJVjYWNoJFtr5AJY+g4KPICJrIQgS62V3c1lJshv3OMlkQ2Z3IqIHBs5k5vvPf06G2TfTKAka2jPdJbMMnlrYgFXbF2kE5+kW7vMD6USMsvLW/DLg24Bnw5pZKrumFSC+dwn0PoHQE8sFuj745qGurZIWWE9Ub6N+dZFu504OgAnjVw6I755LAQoLX0XOIvD1tREXpr56C+gHBQGR+0KkYvzogPjOmRDoSpgC1UE/TMX56qLiQpVMYK8h4ayFsXFTwhRJZ8aXimQPifj2qThM7DckPBAZwCTEIc4nK0Ac4+PuJeULM0gqNBW4fn0v/8JRTcIUA50OUK0qMyC+dZxOeaRyHhmcUwb4ymzqXDoI2lrbSuViJC6KQ3TeHst7HoazcOxW/g6Y+/5KYuEPwRgR4T/xLcAAcgGzK7lP0fEAAAAASUVORK5CYII=");
        RESOURCES.put("c", "Select File");
        RESOURCES.put("d", "Open");
        RESOURCES.put("e", "Back");
        RESOURCES.put("f", "Exit");
        RESOURCES.put("h", "Build Successful! ");
        RESOURCES.put("i", "An error occurred! ");
        RESOURCES.put("j", " Files ");
        RESOURCES.put("k", " Processing ");
        RESOURCES.put("n", "(MTX JarSync)");
        RESOURCES.put("o", "View Progress");
        RESOURCES.put("add", "Add to List");
        RESOURCES.put("delete", "Delete");
        RESOURCES.put("icon", "file:///c/icon.png");
        RESOURCES.put("savedir", "file:///c/MultiMidlet/MtxJarSync.jar");
        RESOURCES.put("selectall", "Select All");
        RESOURCES.put("set", "Settings");
        RESOURCES.put("start", "Start Build");
        RESOURCES.put("view", "Application List");
        RESOURCES.put("add_all", "Add All");
        RESOURCES.put("save", "Save");
        RESOURCES.put("add_item", "Add");

        VI_RESOURCES.put("c", "Ch\u1ecdn t\u1ec7p tin");
        VI_RESOURCES.put("d", "M\u1edf");
        VI_RESOURCES.put("e", "Quay l\u1ea1i");
        VI_RESOURCES.put("f", "Tho\u00e1t");
        VI_RESOURCES.put("h", "Gh\u00e9p th\u00e0nh c\u00f4ng! ");
        VI_RESOURCES.put("i", "C\u00f3 l\u1ed7i x\u1ea3y ra! ");
        VI_RESOURCES.put("j", " T\u1eadp tin ");
        VI_RESOURCES.put("k", " X\u1eed l\u00fd ");
        VI_RESOURCES.put("n", "(MTX JarSync)");
        VI_RESOURCES.put("o", "Xem ti\u1ebfn tr\u00ecnh");
        VI_RESOURCES.put("add", "Th\u00eam v\u00e0o d.s\u00e1ch");
        VI_RESOURCES.put("delete", "X\u00f3a");
        VI_RESOURCES.put("icon", "file:///c/icon.png");
        VI_RESOURCES.put("savedir", "file:///c/MultiMidlet/MtxJarSync.jar");
        VI_RESOURCES.put("selectall", "Ch\u1ecdn t\u1ea5t c\u1ea3");
        VI_RESOURCES.put("set", "C\u00e0i \u0111\u1eb7t");
        VI_RESOURCES.put("start", "B\u1eaft \u0111\u1ea7u gh\u00e9p");
        VI_RESOURCES.put("view", "D.s\u00e1ch \u1ee9ng d\u1ee5ng");
        VI_RESOURCES.put("add_all", "Th\u00eam t\u1ea5t c\u1ea3");
        VI_RESOURCES.put("save", "L\u01b0u");
        VI_RESOURCES.put("add_item", "Th\u00eam");
    }

    public static void setLanguage(String lang) {
        if (lang != null && lang.trim().toLowerCase().startsWith("vi")) {
            currentLanguage = "vi";
        } else {
            currentLanguage = "en";
        }
    }

    public static Base64Resources getInstance() {
        if (instance == null) {
            instance = new Base64Resources();
        }
        return instance;
    }

    public static Image getImage(char ch) {
        try {
            byte[] bytes = decodeBase64(String.valueOf(ch));
            return Image.createImage(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getString(char ch) {
        String key = String.valueOf(ch);
        String[] plainKeys = new String[]{"c", "d", "e", "f", "h", "i", "j", "k", "n", "o"};
        for (int i = 0; i < plainKeys.length; i++) {
            if (key.equals(plainKeys[i])) {
                return get(key);
            }
        }
        try {
            return new String(decodeBase64(key), "utf-8");
        } catch (UnsupportedEncodingException e) {
            return "a";
        }
    }

    public static String get(String key) {
        if ("vi".equals(currentLanguage)) {
            Object viVal = VI_RESOURCES.get(key);
            if (viVal != null) {
                return viVal.toString();
            }
        }
        Object val = RESOURCES.get(key);
        return val != null ? val.toString() : key;
    }

    public static byte[] decodeBase64(String str) {
        Object obj = RESOURCES.get(str);
        if (obj == null) {
            return new byte[0];
        }
        byte[] src = obj.toString().getBytes();
        int len = src.length;
        if (len / 4 * 3 == 0) {
            return src;
        }
        if (src[src.length - 1] == '=') {
            len--;
            if (src[src.length - 2] == '=') {
                len--;
            }
        }
        byte[] out = new byte[len];
        int inIdx = 0;
        int outIdx = 0;

        for (int remaining = src.length; remaining > 0; remaining -= 4) {
            int validBytes = 3;
            int bits = (BASE64_DECODE_TABLE[src[inIdx++] & 0xFF] << 6 | BASE64_DECODE_TABLE[src[inIdx++] & 0xFF]) << 6;
            if (src[inIdx] != '=') {
                bits |= BASE64_DECODE_TABLE[src[inIdx++] & 0xFF];
            } else {
                validBytes--;
            }

            bits <<= 6;
            if (src[inIdx] != '=') {
                bits |= BASE64_DECODE_TABLE[src[inIdx++] & 0xFF];
            } else {
                validBytes--;
            }

            if (validBytes > 2) {
                out[outIdx + 2] = (byte) bits;
            }
            bits >>= 8;
            if (validBytes > 1) {
                out[outIdx + 1] = (byte) bits;
            }
            bits >>= 8;
            out[outIdx] = (byte) bits;
            outIdx += validBytes;
        }
        return out;
    }
}
