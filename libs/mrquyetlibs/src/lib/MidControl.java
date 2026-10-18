/*
 * Decompiled with CFR 0.152.
 */
package lib;

import lib.MIDlet;

public interface MidControl {
    public void showError(Throwable var1, Object var2);

    public void pause();

    public void destroy(MIDlet var1);

    public void resume();
}

