/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Alert
 *  javax.microedition.lcdui.Display
 *  javax.microedition.lcdui.Displayable
 *  javax.microedition.lcdui.Item
 */
package lib;

import java.util.Hashtable;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import lib.Alert;
import lib.MIDlet;
import lib.MidControl;

public class Display {
    private Displayable a;
    public static javax.microedition.lcdui.Display d;
    public static Hashtable midlets;
    public static MidControl mc;

    private Display(MIDlet mIDlet) {
        midlets.put(mIDlet, this);
    }

    public void callSerially(Runnable runnable) {
        d.callSerially(runnable);
    }

    public void setCurrentItem(Item item) {
        if (item != null) {
            try {
                d.setCurrentItem(item);
                this.a = d.getCurrent();
                return;
            }
            catch (Throwable throwable) {
                mc.showError(throwable, this);
            }
        }
    }

    public boolean flashBacklight(int n) {
        return d.flashBacklight(n);
    }

    public boolean vibrate(int n) {
        return d.vibrate(n);
    }

    public static Display getDisplay(MIDlet mIDlet) {
        Display display = (Display)midlets.get(mIDlet);
        if (display == null) {
            display = new Display(mIDlet);
        }
        return display;
    }

    public synchronized void setCurrent(Displayable displayable) {
        if (d == null) {
            mc.pause();
            return;
        }
        this.a = displayable;
        d.setCurrent(this.a);
    }

    public void setCurrent(Alert alert, Displayable displayable) {
        d.setCurrent((javax.microedition.lcdui.Alert)alert, displayable == null ? this.a : (this.a = displayable));
    }

    public boolean isColor() {
        return d.isColor();
    }

    public int numAlphaLevels() {
        return d.numAlphaLevels();
    }

    public int numColors() {
        return d.numColors();
    }

    public Displayable getCurrent() {
        return this.a;
    }

    public int getColor(int n) {
        return d.getColor(n);
    }

    public int getBestImageWidth(int n) {
        return d.getBestImageWidth(n);
    }

    public int getBestImageHeight(int n) {
        return d.getBestImageHeight(n);
    }

    static {
        midlets = new Hashtable();
    }
}

