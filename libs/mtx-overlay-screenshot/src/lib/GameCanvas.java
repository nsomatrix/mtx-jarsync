package lib;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class GameCanvas extends Canvas {
   protected GameCanvas(boolean var1) {
      if (SaveImg == null) {
         super.setFullScreenMode(true);
         SaveImg = Image.createImage(super.getWidth(), super.getHeight());
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
      return SaveImg.getGraphics();
   }

   public int getKeyStates() {
      return 0;
   }

   public void PAINT(Graphics var1) {
      try {
         var1.drawImage(SaveImg, 0, 0, 20);
      } catch (Throwable var2) {
      }
   }
}
