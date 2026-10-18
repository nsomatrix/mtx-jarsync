/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.lcdui.Command
 *  javax.microedition.lcdui.CommandListener
 *  javax.microedition.lcdui.Displayable
 */
package lib;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import lib.Display;

public class Listener
implements CommandListener {
    public static final Command small = new Command("Menu MrQuyet", 7, 8);
    private CommandListener a;

    public void commandAction(Command command, Displayable displayable) {
        if (command == small) {
            Display.mc.pause();
            return;
        }
        if (this.a != null) {
            try {
                this.a.commandAction(command, displayable);
                return;
            }
            catch (Throwable throwable) {
                Display.mc.showError(throwable, this.a);
            }
        }
    }

    public void setCommandListener(CommandListener commandListener) {
        this.a = commandListener;
    }
}

