package lib;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class Canvas extends javax.microedition.lcdui.Canvas {
    private PauseKeyThread pauseKeyThread;
    public static long waittime = 500L;
    public static int menukey = 42;
    public static Image image;
    public static Image buff;

    public final void keyPressed(int keyCode) {
        if (keyCode == menukey) {
            (this.pauseKeyThread = new PauseKeyThread(this, keyCode)).start();
        } else {
            try {
                this.leyPressed(keyCode);
            } catch (Throwable t) {
                Display.mc.showError(t, this);
            }
        }
    }

    public final void keyReleased(int keyCode) {
        if (keyCode == menukey) {
            if (this.pauseKeyThread != null) {
                this.pauseKeyThread.interrupt();
            }
        } else {
            try {
                this.leyReleased(keyCode);
            } catch (Throwable t) {
                Display.mc.showError(t, this);
            }
        }
    }

    public void paint(Graphics g) {
        try {
            if (image != null) {
                int width = super.getWidth();
                int height = super.getHeight();
                if (buff == null || buff.getHeight() != height) {
                    buff = Image.createImage(width, height);
                }

                this.PAINT(buff.getGraphics());
                g.translate(-g.getTranslateX(), -g.getTranslateY());
                g.setClip(0, 0, width, height);
                g.drawImage(buff, 0, 0, 20);
                g.drawImage(image, width >> 1, height >> 1, 3);
                return;
            }

            buff = null;
            this.PAINT(g);
        } catch (Throwable t) {
            Display.mc.showError(t, this);
        }
    }

    protected void PAINT(Graphics g) {
    }

    protected void leyPressed(int keyCode) {
    }

    protected void leyReleased(int keyCode) {
    }

    static void resetPauseKeyThread(Canvas canvas) {
        canvas.pauseKeyThread = null;
    }
}
