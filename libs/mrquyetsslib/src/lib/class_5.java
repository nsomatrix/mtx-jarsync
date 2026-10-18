package lib;

// $VF: renamed from: lib.a
final class class_5 {
   // $VF: renamed from: a byte[]
   public byte[] field_97;
   // $VF: renamed from: a int
   public int field_98;
   // $VF: renamed from: b int
   public int field_99;
   // $VF: renamed from: a long
   public long field_100;
   // $VF: renamed from: b byte[]
   public byte[] field_101;
   // $VF: renamed from: c int
   public int field_102;
   // $VF: renamed from: d int
   public int field_103;
   // $VF: renamed from: b long
   public long field_104;
   // $VF: renamed from: a lib.b
   class_4 field_105;
   // $VF: renamed from: c long
   public long field_106;

   // $VF: renamed from: a (long, byte[], int, int) long
   static long method_28(long var0, byte[] var2, int var3, int var4) {
      if (var2 == null) {
         return 1L;
      } else {
         long var5 = var0 & 65535L;

         long var7;
         for (var7 = var0 >> 16 & 65535L; var4 > 0; var7 %= 65521L) {
            int var9 = var4 < 5552 ? var4 : 5552;

            for (var4 -= var9; var9 >= 16; var9 -= 16) {
               long var25;
               long var26;
               long var27;
               long var28;
               long var29;
               long var30;
               long var31;
               long var32;
               long var33;
               long var34;
               long var35;
               long var36;
               long var37;
               long var38;
               long var39;
               long var40;
               var7 = (var40 = var7 + (var25 = var5 + (long)(var2[var3++] & 255)))
                  + (var26 = var25 + (long)(var2[var3++] & 255))
                  + (var27 = var26 + (long)(var2[var3++] & 255))
                  + (var28 = var27 + (long)(var2[var3++] & 255))
                  + (var29 = var28 + (long)(var2[var3++] & 255))
                  + (var30 = var29 + (long)(var2[var3++] & 255))
                  + (var31 = var30 + (long)(var2[var3++] & 255))
                  + (var32 = var31 + (long)(var2[var3++] & 255))
                  + (var33 = var32 + (long)(var2[var3++] & 255))
                  + (var34 = var33 + (long)(var2[var3++] & 255))
                  + (var35 = var34 + (long)(var2[var3++] & 255))
                  + (var36 = var35 + (long)(var2[var3++] & 255))
                  + (var37 = var36 + (long)(var2[var3++] & 255))
                  + (var38 = var37 + (long)(var2[var3++] & 255))
                  + (var39 = var38 + (long)(var2[var3++] & 255))
                  + (var5 = var39 + (long)(var2[var3++] & 255));
            }

            if (var9 != 0) {
               do {
                  var5 += (long)(var2[var3++] & 255);
                  var7 += var5;
               } while (--var9 != 0);
            }

            var5 %= 65521L;
         }

         return var7 << 16 | var5;
      }
   }

   // $VF: renamed from: a (int) int
   public final int method_29(int var1) {
      return this.field_105 == null ? -2 : this.field_105.method_27(this, var1);
   }

   // $VF: renamed from: a () void
   final void method_30() {
      int var1 = this.field_105.field_46;
      if (this.field_105.field_46 > this.field_103) {
         var1 = this.field_103;
      }

      if (var1 != 0) {
         System.arraycopy(this.field_105.field_43, this.field_105.field_45, this.field_101, this.field_102, var1);
         this.field_102 += var1;
         this.field_105.field_45 += var1;
         this.field_104 += (long)var1;
         this.field_103 -= var1;
         this.field_105.field_46 -= var1;
         if (this.field_105.field_46 == 0) {
            this.field_105.field_45 = 0;
         }
      }
   }
}
