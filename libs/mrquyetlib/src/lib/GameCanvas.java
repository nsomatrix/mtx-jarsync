package lib;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class GameCanvas extends Canvas {
   // $VF: renamed from: a javax.microedition.lcdui.Image
   private static Image field_5;

   protected GameCanvas(boolean var1) {
      if (field_5 == null) {
         super.setFullScreenMode(true);
         field_5 = Image.createImage(super.getWidth(), super.getHeight());
         super.setFullScreenMode(false);
      }
   }

   public void flushGraphics() {
      this.repaint();
   }

   public void flushGraphics(int var1, int var2, int var3, int var4) {
      this.repaint(var1, var2, var3, var4);
   }

   protected Graphics getGraphics() {
      return field_5.getGraphics();
   }

   public int getKeyStates() {
      return 0;
   }

   public void PAINT(Graphics var1) {
      try {
         var1.drawImage(field_5, 0, 0, 20);
      } catch (Throwable var2) {
      }
   }
}
