/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.io.Connection
 *  javax.microedition.io.Connector
 */
package lib;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.Connection;

public class Connector {
    public static final int READ = 1;
    public static final int WRITE = 2;
    public static final int READ_WRITE = 3;
    public static final Hashtable UrlMap = new Hashtable();

    public static DataInputStream openDataInputStream(String string) throws IOException {
        return javax.microedition.io.Connector.openDataInputStream((String)Connector.a(string));
    }

    public static DataOutputStream openDataOutputStream(String string) throws IOException {
        return javax.microedition.io.Connector.openDataOutputStream((String)Connector.a(string));
    }

    public static InputStream openInputStream(String string) throws IOException {
        return javax.microedition.io.Connector.openInputStream((String)Connector.a(string));
    }

    public static OutputStream openOutputStream(String string) throws IOException {
        return javax.microedition.io.Connector.openOutputStream((String)Connector.a(string));
    }

    public static Connection open(String string) throws IOException {
        return javax.microedition.io.Connector.open((String)Connector.a(string));
    }

    public static Connection open(String string, int n) throws IOException {
        return javax.microedition.io.Connector.open((String)Connector.a(string), (int)n);
    }

    public static Connection open(String string, int n, boolean bl) throws IOException {
        return javax.microedition.io.Connector.open((String)Connector.a(string), (int)n, (boolean)bl);
    }

    private static String a(String string) {
        Object object = UrlMap;
        if (!((Hashtable)object).isEmpty()) {
            if (((Hashtable)object).containsKey(string)) {
                return (String)((Hashtable)object).get(string);
            }
            Enumeration enumeration = ((Hashtable)object).keys();
            while (enumeration.hasMoreElements()) {
                String string2;
                String string3 = string2 = (String)enumeration.nextElement();
                if (string2.length() <= 1 || !string2.endsWith("*")) continue;
                if ((string2 = string2.substring(0, string2.length() - 1)).startsWith("*")) {
                    int n = string.indexOf(string2 = string2.substring(1, string2.length()));
                    if (n <= 0) continue;
                    if (((String)(object = (String)((Hashtable)object).get(string3))).startsWith("*")) {
                        object = string.substring(0, n) + ((String)object).substring(1);
                    }
                    if (((String)object).endsWith("*")) {
                        object = ((String)object).substring(0, ((String)object).length() - 1) + string.substring(n + string2.length());
                    }
                    return (String) object;
                }
                if (!string.startsWith(string2)) continue;
                String string4 = (String)((Hashtable)object).get(string3);
                if (string4.endsWith("*")) {
                    return string4.substring(0, string4.length() - 1) + string.substring(string2.length());
                }
                return string4;
            }
        }
        return string;
    }
}

