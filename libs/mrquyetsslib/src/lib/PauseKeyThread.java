package lib;

final class PauseKeyThread extends Thread {
   private final int keyCode;
   private final Canvas canvas;

   PauseKeyThread(Canvas canvas, int keyCode) {
      this.canvas = canvas;
      this.keyCode = keyCode;
   }

   public final void run() {
      try {
         Thread.sleep(Canvas.waittime);
         Display.mc.pause();
      } catch (Exception var3) {
         try {
            this.canvas.leyPressed(this.keyCode);
            this.canvas.leyReleased(this.keyCode);
         } catch (Throwable var2) {
            Display.mc.showError(var2, this);
         }
      }

      this.canvas.resetKeyThread();
   }
}
