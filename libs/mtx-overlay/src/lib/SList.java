package lib;

public class SList extends Thread {
   private List targetList;

   public SList(List var1) {
      this.targetList = var1;
   }
   public void run() {
      try {
         Thread.sleep(1000L);
      } catch (Exception var1) {
      }

      this.targetList.scrollTimerActive = false;
      this.targetList.repaint();
   }
}
