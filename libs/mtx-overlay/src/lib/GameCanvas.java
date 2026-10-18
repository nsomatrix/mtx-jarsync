package lib;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class GameCanvas extends Canvas {
   private static Image offscreenBuffer;

   protected GameCanvas(boolean var1) {
      if (offscreenBuffer == null) {
         super.setFullScreenMode(true);
         offscreenBuffer = Image.createImage(super.getWidth(), super.getHeight());
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
      return offscreenBuffer.getGraphics();
   }

   public int getKeyStates() {
      return 0;
   }

   public void PAINT(Graphics var1) {
      try {
         var1.drawImage(offscreenBuffer, 0, 0, 20);
      } catch (Throwable var2) {
      }
   }
}
