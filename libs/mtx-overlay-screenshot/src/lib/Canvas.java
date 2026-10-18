package lib;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class Canvas extends javax.microedition.lcdui.Canvas {
   private Thread keyThread;
   public static int savelevel = 1;
   public static long waittime = 500L;
   public static long jietime = 200L;
   public static int menukey = 42;
   public static String savepath = "file:///c/MTX_JarSync/" + MIDlet.mid.getAppProperty("MIDlet-Name") + "/ChipShot_";
   public static int jiekey = 35;
   protected static Image SaveImg;
   public static Image image;
   public static Image buff;
   boolean screenshotCapturePending;

   protected Canvas() {
      if (SaveImg == null) {
         super.setFullScreenMode(true);
         SaveImg = Image.createImage(super.getWidth(), super.getHeight());
         super.setFullScreenMode(false);
      }
   }

   public final void keyPressed(int keyCode) {
      if (keyCode == menukey) {
         (this.keyThread = new PauseKeyThread(this, keyCode)).start();
      } else if (keyCode == jiekey) {
         this.screenshotCapturePending = true;
         (this.keyThread = new PngScreenshotThread(SaveImg, this, keyCode)).start();
      } else {
         try {
            this.leyPressed(keyCode);
         } catch (Throwable e) {
            Display.mc.showError(e, this);
         }
      }
   }

   public final void keyReleased(int keyCode) {
      if ((keyCode == menukey || keyCode == jiekey) && this.keyThread != null) {
         this.keyThread.interrupt();
      } else {
         try {
            this.leyReleased(keyCode);
         } catch (Throwable e) {
            Display.mc.showError(e, this);
         }
      }
   }

   public final void paint(Graphics g) {
      try {
         if (this.screenshotCapturePending) {
            this.renderCanvasWithOverlay(g);
            return;
         }

         this.renderCanvasWithOverlay(SaveImg.getGraphics());
      } catch (Throwable e) {
      }

      g.drawImage(SaveImg, 0, 0, 20);
   }

   private void renderCanvasWithOverlay(Graphics g) {
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
      } catch (Throwable e) {
         Display.mc.showError(e, this);
      }
   }

   protected void leyPressed(int keyCode) {
   }

   protected void PAINT(Graphics g) {
   }

   protected void leyReleased(int keyCode) {
   }

   void resetKeyThread() {
      this.keyThread = null;
   }
}
