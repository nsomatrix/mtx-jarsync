/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.AlertType
 */
package lib;

import lib.Display;

public class AlertType {
    javax.microedition.lcdui.AlertType a;
    public static final AlertType WARNING = new AlertType(javax.microedition.lcdui.AlertType.WARNING);
    public static final AlertType ERROR = new AlertType(javax.microedition.lcdui.AlertType.ERROR);
    public static final AlertType ALARM = new AlertType(javax.microedition.lcdui.AlertType.ALARM);
    public static final AlertType CONFIRMATION = new AlertType(javax.microedition.lcdui.AlertType.CONFIRMATION);
    public static final AlertType INFO = new AlertType(javax.microedition.lcdui.AlertType.INFO);

    protected AlertType() {
    }

    private AlertType(javax.microedition.lcdui.AlertType alertType) {
        this.a = alertType;
    }

    public boolean playSound(Display display) {
        return this.a.playSound(Display.d);
    }
}

