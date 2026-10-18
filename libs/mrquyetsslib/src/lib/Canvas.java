package lib;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class Canvas extends javax.microedition.lcdui.Canvas {
   // $VF: renamed from: a java.lang.Thread
   private Thread field_107;
   public static int savelevel = 1;
   public static long waittime = 500L;
   public static long jietime = 200L;
   public static int menukey = 42;
   public static String savepath = "file:///c/AngelChip/" + MIDlet.mid.getAppProperty("MIDlet-Name") + "/ChipShot_";
   public static int jiekey = 35;
   protected static Image SaveImg;
   public static Image image;
   public static Image buff;
   // $VF: renamed from: a boolean
   boolean field_108;

   protected Canvas() {
      if (SaveImg == null) {
         super.setFullScreenMode(true);
         SaveImg = Image.createImage(super.getWidth(), super.getHeight());
         super.setFullScreenMode(false);
      }
   }

   public final void keyPressed(int var1) {
      if (var1 == menukey) {
         (this.field_107 = new class_1(this, var1)).start();
      } else if (var1 == jiekey) {
         this.field_108 = true;
         (this.field_107 = new class_0(SaveImg, this, var1)).start();
      } else {
         try {
            this.leyPressed(var1);
         } catch (Throwable var2) {
            Display.mc.showError(var2, this);
         }
      }
   }

   public final void keyReleased(int var1) {
      if ((var1 == menukey || var1 == jiekey) && this.field_107 != null) {
         this.field_107.interrupt();
      } else {
         try {
            this.leyReleased(var1);
         } catch (Throwable var2) {
            Display.mc.showError(var2, this);
         }
      }
   }

   public final void paint(Graphics var1) {
      try {
         if (this.field_108) {
            this.method_31(var1);
            return;
         }

         this.method_31(SaveImg.getGraphics());
      } catch (Throwable var2) {
      }

      var1.drawImage(SaveImg, 0, 0, 20);
   }

   // $VF: renamed from: a (javax.microedition.lcdui.Graphics) void
   private void method_31(Graphics var1) {
      try {
         if (image != null) {
            int var2 = super.getWidth();
            int var3 = super.getHeight();
            if (buff == null || buff.getHeight() != var3) {
               buff = Image.createImage(var2, var3);
            }

            this.PAINT(buff.getGraphics());
            var1.translate(-var1.getTranslateX(), -var1.getTranslateY());
            var1.setClip(0, 0, var2, var3);
            var1.drawImage(buff, 0, 0, 20);
            var1.drawImage(image, var2 >> 1, var3 >> 1, 3);
            return;
         }

         buff = null;
         this.PAINT(var1);
      } catch (Throwable var4) {
         Display.mc.showError(var4, this);
      }
   }

   protected void leyPressed(int var1) {
   }

   protected void PAINT(Graphics var1) {
   }

   protected void leyReleased(int var1) {
   }

   // $VF: renamed from: a (lib.Canvas, java.lang.Thread) java.lang.Thread
   static Thread method_32(Canvas var0, Thread var1) {
      return var0.field_107 = null;
   }
}
