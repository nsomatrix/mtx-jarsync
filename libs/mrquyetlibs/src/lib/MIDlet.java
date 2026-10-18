/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.midlet.MIDlet
 */
package lib;

import java.io.InputStream;
import java.util.Hashtable;
import lib.Display;
import lib.Stack;

public abstract class MIDlet {
    private Hashtable var_java_util_Hashtable_a;
    private static final Hashtable b = new Hashtable();
    private boolean var_boolean_a = false;
    public static javax.microedition.midlet.MIDlet mid;
    public static String nowmf;
    public Stack stack;
    private static byte[] var_byte_arr_a;

    protected MIDlet() {
        this.setAppProperty(nowmf);
    }

    public void showDisplay() {
        Display display = Display.getDisplay(this);
        display.setCurrent(display.getCurrent());
    }

    public void start() {
        try {
            this.startApp();
            return;
        }
        catch (Throwable throwable) {
            Display.mc.showError(throwable, this);
            return;
        }
    }

    protected abstract void destroyApp(boolean var1);

    public String getAppProperty(String string) {
        return (String)this.var_java_util_Hashtable_a.get(string);
    }

    public void notifyDestroyed() {
        if (!this.var_boolean_a) {
            this.var_boolean_a = true;
            try {
                this.destroyApp(true);
            }
            catch (Throwable throwable) {
                Display.mc.showError(throwable, this);
            }
            Display.mc.destroy(this);
        }
    }

    public void notifyPaused() {
        Display.mc.pause();
    }

    protected abstract void pauseApp();

    public void resumeRequest() {
        Display.mc.resume();
    }

    protected abstract void startApp();

    public final boolean platformRequest(String string) throws javax.microedition.io.ConnectionNotFoundException {
        return mid.platformRequest(string);
    }

    public final int checkPermission(String string) {
        return mid.checkPermission(string);
    }

    public final void setAppProperty(String path) {
        this.var_java_util_Hashtable_a = (Hashtable)b.get(path);
        if (this.var_java_util_Hashtable_a == null) {
            this.var_java_util_Hashtable_a = new Hashtable();
            b.put(path, this.var_java_util_Hashtable_a);
            InputStream is = this.getClass().getResourceAsStream(path);
            if (is != null) {
                String string = "";
                try {
                    String string2;
                    while ((string2 = MIDlet.a(is)) != null) {
                        int n = string2.indexOf(58);
                        if (n > 0) {
                            string = string2.substring(0, n);
                            this.var_java_util_Hashtable_a.put(string, string2.substring(n + 1).trim());
                            continue;
                        }
                        if ((string2 = string2.trim()).length() <= 0) continue;
                        this.var_java_util_Hashtable_a.put(string, this.var_java_util_Hashtable_a.get(string) + string2);
                    }
                    is.close();
                    return;
                }
                catch (Throwable throwable) {
                    Display.mc.showError(throwable, this);
                }
            }
        }
    }

    private static String a(InputStream inputStream) throws Exception {
        int n;
        int n2 = 0;
        block4: while (true) {
            n = inputStream.read();
            switch (n) {
                case -1: 
                case 10: {
                    break block4;
                }
                case 13: {
                    inputStream.read();
                    break block4;
                }
                default: {
                    MIDlet.var_byte_arr_a[n2++] = (byte)n;
                    continue block4;
                }
            }
        }
        if (n == -1 && n2 == 0) {
            return null;
        }
        return new String(var_byte_arr_a, 0, n2, "utf-8");
    }

    static {
        var_byte_arr_a = new byte[4096];
    }
}

