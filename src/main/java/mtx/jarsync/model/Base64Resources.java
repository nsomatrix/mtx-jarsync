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
        RESOURCES.put("c", "Chọn tệp tin");
        RESOURCES.put("d", "Mở");
        RESOURCES.put("e", "Quay lại");
        RESOURCES.put("f", "Thoát");
        RESOURCES.put("h", "Ghép thành công! ");
        RESOURCES.put("i", "Có lỗi xảy ra! ");
        RESOURCES.put("j", " Tập tin ");
        RESOURCES.put("k", " Xử lý ");
        RESOURCES.put("n", "(MTX JarSync)");
        RESOURCES.put("o", "Xem tiến trình");
        RESOURCES.put("add", "Thêm vào d.sách");
        RESOURCES.put("delete", "Xóa");
        RESOURCES.put("icon", "file:///c/icon.png");
        RESOURCES.put("savedir", "file:///c/MultiMidlet/MtxJarSync.jar");
        RESOURCES.put("selectall", "Chọn tất cả");
        RESOURCES.put("set", "Cài đặt");
        RESOURCES.put("start", "Bắt đầu ghép");
        RESOURCES.put("view", "D.sách ứng dụng");
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
                return (String) RESOURCES.get(key);
            }
        }
        try {
            return new String(decodeBase64(key), "utf-8");
        } catch (UnsupportedEncodingException e) {
            return "a";
        }
    }

    public static String get(String key) {
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
