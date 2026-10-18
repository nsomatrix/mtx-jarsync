package lib;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class Canvas extends javax.microedition.lcdui.Canvas {
   // $VF: renamed from: a java.lang.Thread
   private Thread field_4;
   public static long waittime = 500L;
   public static int menukey = 42;
   public static Image image;
   public static Image buff;

   public final void keyPressed(int var1) {
      if (var1 == menukey) {
         (this.field_4 = new class_0(this, var1)).start();
      } else {
         try {
            this.leyPressed(var1);
         } catch (Throwable var2) {
            Display.mc.showError(var2, this);
         }
      }
   }

   public final void keyReleased(int var1) {
      if (var1 == menukey) {
         if (this.field_4 != null) {
            this.field_4.interrupt();
         }
      } else {
         try {
            this.leyReleased(var1);
         } catch (Throwable var2) {
            Display.mc.showError(var2, this);
         }
      }
   }

   public void paint(Graphics var1) {
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

   protected void PAINT(Graphics var1) {
   }

   protected void leyPressed(int var1) {
   }

   protected void leyReleased(int var1) {
   }

   // $VF: renamed from: a (lib.Canvas, java.lang.Thread) java.lang.Thread
   static Thread method_0(Canvas var0, Thread var1) {
      return var0.field_4 = null;
   }
}
