/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Image
 */
package mtx.jarsync.runtime;

import javax.microedition.lcdui.Image;
import lib.Display;
import lib.MIDlet;
import lib.Stack;

public class MidletInfo {
    public String name;
    public String classname;
    public Image icon;
    private String a;
    private static Image im;
    private String b;
    protected static int iconwidth;
    protected static int iconheight;

    public void destroy(MIDlet var1) {
    }

    public static MidletInfo getInstance(String var0) {
        Image var5;
        MidletInfo var1 = new MidletInfo();
        int var2 = var0.indexOf(44);
        var1.name = var0.substring(0, var2++).trim();
        int var3 = var0.indexOf(44, var2);
        try {
            var5 = Image.createImage((String)var0.substring(var2, var3++).trim());
        }
        catch (Exception var4) {
            var5 = im;
        }
        MidletInfo.a(var5, var1);
        var2 = var0.indexOf(44, var3);
        var1.classname = var0.substring(var3, var2++).trim();
        int idof = var1.classname.indexOf(".");
        String pa = idof != -1 ? var1.classname.substring(0, idof) : var1.classname;
        var1.b = pa.length() > 1 ? "Static" : pa + ".Static";
        var1.a = var0.substring(var2).trim();
        return var1;
    }

    public MIDlet newInstance() {
        MIDlet var1;
        MIDlet.nowmf = this.a;
        try {
            Stack var2;
            try {
                var2 = (Stack)Class.forName(this.b).newInstance();
            }
            catch (Throwable var3) {
                var2 = null;
            }
            var1 = (MIDlet)Class.forName(this.classname).newInstance();
            var1.stack = var2;
        }
        catch (Throwable var4) {
            var1 = null;
            Display.mc.showError(var4, this);
        }
        return var1;
    }

    private static void a(Image var0, MidletInfo var1) {
        int var2 = iconwidth;
        int var3 = iconheight;
        int var4 = var0.getWidth();
        int var5 = var0.getHeight();
        if ((var2 != var4 || var5 != var3) && var2 > 1 && var3 > 1) {
            int var10;
            int var9;
            int[] var6 = new int[var4 * var5];
            var0.getRGB(var6, 0, var4, 0, 0, var4, var5);
            float var7 = (float)var5 / (float)var3;
            float var11 = var2 == 0 ? var7 : (float)var4 / (float)var2;
            var2 = (int)(var2 == 0 ? (float)var4 / var11 : (float)var2);
            int[] var8 = new int[var5 * var2];
            for (var9 = 0; var9 < var5; ++var9) {
                for (var10 = 0; var10 < var2; ++var10) {
                    var8[var9 * var2 + var10] = var6[var4 * var9 + (int)(var11 * (float)var10)];
                }
            }
            var6 = new int[var2 * var3];
            for (var9 = 0; var9 < var2; ++var9) {
                for (var10 = 0; var10 < var3; ++var10) {
                    var6[var2 * var10 + var9] = var8[(int)((float)var10 * var7) * var2 + var9];
                }
            }
            var1.icon = Image.createRGBImage((int[])var6, (int)var2, (int)var3, (boolean)true);
        } else {
            var1.icon = var0;
        }
    }

    public static Image runimg(Image var0) {
        int var7;
        int var1 = var0.getWidth();
        int var2 = var0.getHeight();
        int[] var3 = new int[var1 * var2];
        var0.getRGB(var3, 0, var1, 0, 0, var1, var2);
        int var8 = var1 / 3;
        int var4 = var2 / 3;
        int var5 = 1;
        int var6 = var2 - var4;
        while (var5 < var4 - 2) {
            for (var7 = var1 - var8; var7 < var5 + var1 - var8; ++var7) {
                var3[var1 * var6 + var7] = -16711936;
            }
            var5 += 2;
            ++var6;
        }
        while (var5 > 0) {
            for (var7 = var1 - var8; var7 < var5 + var1 - var8; ++var7) {
                var3[var1 * var6 + var7] = -16711936;
            }
            var5 -= 2;
            ++var6;
        }
        return Image.createRGBImage((int[])var3, (int)var1, (int)var2, (boolean)true);
    }

    protected void updateIcon() {
        MidletInfo.a(this.icon, this);
    }

    static {
        iconwidth = 48;
        iconheight = 48;
        try {
            im = Image.createImage((String)"/mtx/mid.png");
        }
        catch (Throwable var0) {
            im = Image.createImage((int)20, (int)20);
        }
    }
}

