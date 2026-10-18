package lib;

public class RList extends Thread {
   private List targetList;

   public RList(List var1) {
      this.targetList = var1;
   }
   public void run() {
      while (this.targetList.scrollThreadRunning) {
         try {
            Thread.sleep(500L);
            this.targetList.repaint();
         } catch (Exception var1) {
         }
      }
   }
}
