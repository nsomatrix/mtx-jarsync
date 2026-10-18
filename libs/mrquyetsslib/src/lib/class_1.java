package lib;

// $VF: renamed from: lib.e
final class class_1 extends Thread {
   // $VF: renamed from: a int
   private final int field_13;
   // $VF: renamed from: a lib.Canvas
   private final Canvas field_14;

   class_1(Canvas var1, int var2) {
      this.field_14 = var1;
      this.field_13 = var2;
   }

   // $VF: renamed from: run () void
   public final void run() {
      try {
         Thread.sleep(Canvas.waittime);
         Display.mc.pause();
      } catch (Exception var3) {
         try {
            this.field_14.leyPressed(this.field_13);
            this.field_14.leyReleased(this.field_13);
         } catch (Throwable var2) {
            Display.mc.showError(var2, this);
         }
      }

      Canvas.method_32(this.field_14, null);
   }
}
