package lib;

public class RList extends Thread {
   // $VF: renamed from: a lib.List
   private List field_1;

   public RList(List var1) {
      this.field_1 = var1;
   }

   // $VF: renamed from: run () void
   public void run() {
      while (this.field_1.field_30) {
         try {
            Thread.sleep(500L);
            this.field_1.repaint();
         } catch (Exception var1) {
         }
      }
   }
}
