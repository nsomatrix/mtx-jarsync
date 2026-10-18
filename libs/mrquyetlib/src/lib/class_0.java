package lib;

// $VF: renamed from: lib.a
final class class_0 extends Thread {
   // $VF: renamed from: a int
   private final int field_2;
   // $VF: renamed from: a lib.Canvas
   private final Canvas field_3;

   class_0(Canvas var1, int var2) {
      this.field_3 = var1;
      this.field_2 = var2;
   }

   // $VF: renamed from: run () void
   public final void run() {
      try {
         Thread.sleep(Canvas.waittime);
         Display.mc.pause();
      } catch (Exception var3) {
         try {
            this.field_3.leyPressed(this.field_2);
            this.field_3.leyReleased(this.field_2);
         } catch (Throwable var2) {
            Display.mc.showError(var2, this);
         }
      }

      Canvas.method_0(this.field_3, null);
   }
}
