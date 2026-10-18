/*
 * Decompiled with CFR 0.152.
 */
package lib;

import java.util.Vector;

public abstract class Stack {
    private final Vector a;

    protected Stack(Vector vector) {
        this.a = vector;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void clear() {
        Vector vector = this.a;
        synchronized (vector) {
            int n = this.a.size();
            for (int i = 0; i < n; ++i) {
                try {
                    this.clears((Integer)this.a.elementAt(i));
                    System.gc();
                    continue;
                }
                catch (Throwable throwable) {}
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cinitclone() {
        Vector vector = this.a;
        synchronized (vector) {
            int n = this.a.size();
            for (int i = 0; i < n; ++i) {
                try {
                    this.cinitclones((Integer)this.a.elementAt(i));
                    continue;
                }
                catch (Throwable throwable) {}
            }
            return;
        }
    }

    protected abstract void cinitclones(int var1);

    protected abstract void clears(int var1);
}

