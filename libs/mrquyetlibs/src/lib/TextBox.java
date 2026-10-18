/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.CommandListener
 *  javax.microedition.lcdui.TextBox
 */
package lib;

import javax.microedition.lcdui.CommandListener;
import lib.Listener;

public class TextBox
extends javax.microedition.lcdui.TextBox {
    private Listener a;

    public void setCommandListener(CommandListener commandListener) {
        this.a.setCommandListener(commandListener);
    }

    public TextBox(String string, String string2, int n, int n2) {
        super(string, string2, n, n2);
        this.addCommand(Listener.small);
        this.a = new Listener();
        super.setCommandListener((CommandListener)this.a);
    }
}

