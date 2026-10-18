/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.CommandListener
 *  javax.microedition.lcdui.Image
 *  javax.microedition.lcdui.List
 */
package lib;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Image;
import lib.Listener;

public class List
extends javax.microedition.lcdui.List {
    private Listener a;

    public void setCommandListener(CommandListener commandListener) {
        this.a.setCommandListener(commandListener);
    }

    public List(String string, int n) {
        super(string, n);
        this.a();
    }

    private void a() {
        this.addCommand(Listener.small);
        this.a = new Listener();
        super.setCommandListener((CommandListener)this.a);
    }

    public List(String string, int n, String[] stringArray, Image[] imageArray) {
        super(string, n, stringArray, imageArray);
        this.a();
    }
}

