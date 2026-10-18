/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Alert
 *  javax.microedition.lcdui.Image
 */
package lib;

import javax.microedition.lcdui.Image;
import lib.AlertType;

public class Alert
extends javax.microedition.lcdui.Alert {
    private AlertType a;

    public Alert(String string) {
        super(string);
    }

    public Alert(String string, String string2, Image image, AlertType alertType) {
        super(string, string2, image, alertType.a);
    }

    public void setType(AlertType alertType) {
        this.a = alertType;
        super.setType(this.a.a);
    }

    public AlertType geTType() {
        return this.a;
    }
}

