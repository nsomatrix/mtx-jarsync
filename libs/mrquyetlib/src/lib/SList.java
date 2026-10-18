package lib;

public class SList extends Thread {
   // $VF: renamed from: a lib.List
   private List field_0;

   public SList(List var1) {
      this.field_0 = var1;
   }

   // $VF: renamed from: run () void
   public void run() {
      try {
         Thread.sleep(1000L);
      } catch (Exception var1) {
      }

      this.field_0.field_26 = false;
      this.field_0.repaint();
   }
}
