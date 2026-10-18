package lib;

// $VF: renamed from: lib.b
final class class_4 {
   // $VF: renamed from: b int[]
   private static final int[] field_36 = new int[]{0, 4, 4, 4, 4, 8, 8, 8, 32, 32};
   // $VF: renamed from: c int[]
   private static final int[] field_37 = new int[]{0, 4, 5, 6, 4, 16, 16, 32, 128, 258};
   // $VF: renamed from: d int[]
   private static final int[] field_38 = new int[]{0, 8, 16, 32, 16, 32, 128, 128, 258, 258};
   // $VF: renamed from: e int[]
   private static final int[] field_39 = new int[]{0, 4, 8, 32, 16, 32, 128, 256, 1024, 4096};
   // $VF: renamed from: f int[]
   private static final int[] field_40 = new int[]{0, 1, 1, 1, 2, 2, 2, 2, 2, 2};
   // $VF: renamed from: a lib.a
   private class_5 field_41;
   // $VF: renamed from: h int
   private int field_42;
   // $VF: renamed from: a byte[]
   byte[] field_43;
   // $VF: renamed from: i int
   private int field_44;
   // $VF: renamed from: a int
   int field_45;
   // $VF: renamed from: b int
   int field_46;
   // $VF: renamed from: c int
   int field_47;
   // $VF: renamed from: a byte
   private byte field_48;
   // $VF: renamed from: j int
   private int field_49;
   // $VF: renamed from: k int
   private int field_50;
   // $VF: renamed from: l int
   private int field_51;
   // $VF: renamed from: m int
   private int field_52;
   // $VF: renamed from: c byte[]
   private byte[] field_53;
   // $VF: renamed from: n int
   private int field_54;
   // $VF: renamed from: b short[]
   private short[] field_55;
   // $VF: renamed from: c short[]
   private short[] field_56;
   // $VF: renamed from: o int
   private int field_57;
   // $VF: renamed from: p int
   private int field_58;
   // $VF: renamed from: q int
   private int field_59;
   // $VF: renamed from: r int
   private int field_60;
   // $VF: renamed from: s int
   private int field_61;
   // $VF: renamed from: t int
   private int field_62;
   // $VF: renamed from: u int
   private int field_63;
   // $VF: renamed from: v int
   private int field_64;
   // $VF: renamed from: w int
   private int field_65;
   // $VF: renamed from: x int
   private int field_66;
   // $VF: renamed from: y int
   private int field_67;
   // $VF: renamed from: z int
   private int field_68;
   // $VF: renamed from: A int
   private int field_69;
   // $VF: renamed from: B int
   private int field_70;
   // $VF: renamed from: C int
   private int field_71;
   // $VF: renamed from: D int
   private int field_72;
   // $VF: renamed from: E int
   private int field_73;
   // $VF: renamed from: F int
   private int field_74;
   // $VF: renamed from: G int
   private int field_75;
   // $VF: renamed from: d short[]
   private short[] field_76;
   // $VF: renamed from: e short[]
   private short[] field_77;
   // $VF: renamed from: f short[]
   private short[] field_78;
   // $VF: renamed from: a lib.d
   private class_2 field_79 = new class_2();
   // $VF: renamed from: b lib.d
   private class_2 field_80 = new class_2();
   // $VF: renamed from: c lib.d
   private class_2 field_81 = new class_2();
   // $VF: renamed from: a short[]
   short[] field_82 = new short[16];
   // $VF: renamed from: a int[]
   int[] field_83 = new int[573];
   // $VF: renamed from: d int
   int field_84;
   // $VF: renamed from: e int
   int field_85;
   // $VF: renamed from: b byte[]
   byte[] field_86 = new byte[573];
   // $VF: renamed from: H int
   private int field_87;
   // $VF: renamed from: I int
   private int field_88;
   // $VF: renamed from: J int
   private int field_89;
   // $VF: renamed from: K int
   private int field_90;
   // $VF: renamed from: f int
   int field_91;
   // $VF: renamed from: g int
   int field_92;
   // $VF: renamed from: L int
   private int field_93;
   // $VF: renamed from: M int
   private int field_94;
   // $VF: renamed from: a short
   private short field_95;
   // $VF: renamed from: N int
   private int field_96;

   class_4() {
      this.field_76 = new short[1146];
      this.field_77 = new short[122];
      this.field_78 = new short[78];
   }

   // $VF: renamed from: a () void
   private void method_7() {
      for (int var1 = 0; var1 < 286; var1++) {
         this.field_76[var1 << 1] = 0;
      }

      for (int var2 = 0; var2 < 30; var2++) {
         this.field_77[var2 << 1] = 0;
      }

      for (int var3 = 0; var3 < 19; var3++) {
         this.field_78[var3 << 1] = 0;
      }

      this.field_76[512] = 1;
      this.field_91 = this.field_92 = 0;
      this.field_89 = this.field_93 = 0;
   }

   // $VF: renamed from: a (short[], int) void
   final void method_8(short[] var1, int var2) {
      int var3 = this.field_83[var2];

      for (int var4 = var2 << 1; var4 <= this.field_84; var4 <<= 1) {
         if (var4 < this.field_84 && method_9(var1, this.field_83[var4 + 1], this.field_83[var4], this.field_86)) {
            var4++;
         }

         if (method_9(var1, var3, this.field_83[var4], this.field_86)) {
            break;
         }

         this.field_83[var2] = this.field_83[var4];
         var2 = var4;
      }

      this.field_83[var2] = var3;
   }

   // $VF: renamed from: a (short[], int, int, byte[]) boolean
   private static boolean method_9(short[] var0, int var1, int var2, byte[] var3) {
      short var4 = var0[var1 << 1];
      short var5 = var0[var2 << 1];
      return var4 < var5 || var4 == var5 && var3[var1] <= var3[var2];
   }

   // $VF: renamed from: b (short[], int) void
   private void method_10(short[] var1, int var2) {
      short var4 = -1;
      short var6 = var1[1];
      int var7 = 0;
      short var8 = 7;
      byte var9 = 4;
      if (var6 == 0) {
         var8 = 138;
         var9 = 3;
      }

      var1[(var2 + 1 << 1) + 1] = -1;

      for (int var3 = 0; var3 <= var2; var3++) {
         short var5 = var6;
         var6 = var1[(var3 + 1 << 1) + 1];
         if (++var7 >= var8 || var5 != var6) {
            if (var7 < var9) {
               this.field_78[var5 << 1] = (short)(this.field_78[var5 << 1] + var7);
            } else if (var5 != 0) {
               if (var5 != var4) {
                  this.field_78[var5 << 1]++;
               }

               this.field_78[32]++;
            } else if (var7 <= 10) {
               this.field_78[34]++;
            } else {
               this.field_78[36]++;
            }

            var7 = 0;
            var4 = var5;
            if (var6 == 0) {
               var8 = 138;
               var9 = 3;
            } else if (var5 == var6) {
               var8 = 6;
               var9 = 3;
            } else {
               var8 = 7;
               var9 = 4;
            }
         }
      }
   }

   // $VF: renamed from: c (short[], int) void
   private void method_11(short[] var1, int var2) {
      short var4 = -1;
      short var6 = var1[1];
      int var7 = 0;
      short var8 = 7;
      byte var9 = 4;
      if (var6 == 0) {
         var8 = 138;
         var9 = 3;
      }

      for (int var3 = 0; var3 <= var2; var3++) {
         short var5 = var6;
         var6 = var1[(var3 + 1 << 1) + 1];
         if (++var7 >= var8 || var5 != var6) {
            if (var7 < var9) {
               do {
                  this.method_15(var5, this.field_78);
               } while (--var7 != 0);
            } else if (var5 != 0) {
               if (var5 != var4) {
                  this.method_15(var5, this.field_78);
                  var7--;
               }

               this.method_15(16, this.field_78);
               this.method_16(var7 - 3, 2);
            } else if (var7 <= 10) {
               this.method_15(17, this.field_78);
               this.method_16(var7 - 3, 3);
            } else {
               this.method_15(18, this.field_78);
               this.method_16(var7 - 11, 7);
            }

            var7 = 0;
            var4 = var5;
            if (var6 == 0) {
               var8 = 138;
               var9 = 3;
            } else if (var5 == var6) {
               var8 = 6;
               var9 = 3;
            } else {
               var8 = 7;
               var9 = 4;
            }
         }
      }
   }

   // $VF: renamed from: a (byte) void
   private void method_12(byte var1) {
      this.field_43[this.field_46++] = var1;
   }

   // $VF: renamed from: a (int) void
   private void method_13(int var1) {
      this.method_12((byte)var1);
      this.method_12((byte)(var1 >>> 8));
   }

   // $VF: renamed from: b (int) void
   private void method_14(int var1) {
      this.method_12((byte)(var1 >> 8));
      this.method_12((byte)var1);
   }

   // $VF: renamed from: a (int, short[]) void
   private void method_15(int var1, short[] var2) {
      var1 <<= 1;
      this.method_16(var2[var1] & '\uffff', var2[var1 + 1] & '\uffff');
   }

   // $VF: renamed from: a (int, int) void
   private void method_16(int var1, int var2) {
      if (this.field_96 > 16 - var2) {
         this.field_95 = (short)(this.field_95 | var1 << this.field_96 & 65535);
         this.method_13(this.field_95);
         this.field_95 = (short)(var1 >>> 16 - this.field_96);
         this.field_96 += var2 - 16;
      } else {
         this.field_95 = (short)(this.field_95 | var1 << this.field_96 & 65535);
         this.field_96 += var2;
      }
   }

   // $VF: renamed from: a (int, int) boolean
   private boolean method_17(int var1, int var2) {
      this.field_43[this.field_90 + (this.field_89 << 1)] = (byte)(var1 >>> 8);
      this.field_43[this.field_90 + (this.field_89 << 1) + 1] = (byte)var1;
      this.field_43[this.field_87 + this.field_89] = (byte)var2;
      this.field_89++;
      if (var1 == 0) {
         this.field_76[var2 << 1]++;
      } else {
         this.field_93++;
         var1--;
         this.field_76[class_2.field_20[var2] + 256 + 1 << 1]++;
         this.field_77[class_2.method_5(var1) << 1]++;
      }

      if ((this.field_89 & 8191) == 0 && this.field_72 > 2) {
         int var5 = this.field_89 << 3;
         var2 = this.field_66 - this.field_62;

         for (int var3 = 0; var3 < 30; var3++) {
            var5 = (int)((long)var5 + (long)this.field_77[var3 << 1] * (5L + (long)class_2.field_16[var3]));
         }

         var1 = var5 >>> 3;
         if (this.field_93 < this.field_89 / 2 && var1 < var2 / 2) {
            return true;
         }
      }

      return this.field_89 == this.field_88 - 1;
   }

   // $VF: renamed from: a (short[], short[]) void
   private void method_18(short[] var1, short[] var2) {
      int var5 = 0;
      if (this.field_89 != 0) {
         do {
            int var3 = this.field_43[this.field_90 + (var5 << 1)] << 8 & 0xFF00 | this.field_43[this.field_90 + (var5 << 1) + 1] & 255;
            int var4 = this.field_43[this.field_87 + var5] & 255;
            var5++;
            if (var3 == 0) {
               this.method_15(var4, var1);
            } else {
               int var6 = class_2.field_20[var4];
               this.method_15(var6 + 256 + 1, var1);
               int var7;
               if ((var7 = class_2.field_15[var6]) != 0) {
                  var4 -= class_2.field_21[var6];
                  this.method_16(var4, var7);
               }

               var6 = class_2.method_5(--var3);
               this.method_15(var6, var2);
               if ((var7 = class_2.field_16[var6]) != 0) {
                  var3 -= class_2.field_22[var6];
                  this.method_16(var3, var7);
               }
            }
         } while (var5 < this.field_89);
      }

      this.method_15(256, var1);
      this.field_94 = var1[513];
   }

   // $VF: renamed from: b () void
   private void method_19() {
      if (this.field_96 == 16) {
         this.method_13(this.field_95);
         this.field_95 = 0;
         this.field_96 = 0;
      } else {
         if (this.field_96 >= 8) {
            this.method_12((byte)this.field_95);
            this.field_95 = (short)(this.field_95 >>> 8);
            this.field_96 -= 8;
         }
      }
   }

   // $VF: renamed from: c () void
   private void method_20() {
      if (this.field_96 > 8) {
         this.method_13(this.field_95);
      } else if (this.field_96 > 0) {
         this.method_12((byte)this.field_95);
      }

      this.field_95 = 0;
      this.field_96 = 0;
   }

   // $VF: renamed from: a (boolean) void
   private void method_21(boolean var1) {
      int var10001 = this.field_62 >= 0 ? this.field_62 : -1;
      int var3 = this.field_66 - this.field_62;
      int var2 = var10001;
      int var7 = 0;
      int var11;
      int var14;
      if (this.field_72 > 0) {
         if (this.field_48 == 2) {
            class_4 var5 = this;
            int var6 = 0;
            short var16 = 0;

            short var8;
            for (var8 = 0; var6 < 7; var6++) {
               var8 += var5.field_76[var6 << 1];
            }

            while (var6 < 128) {
               var16 += var5.field_76[var6 << 1];
               var6++;
            }

            while (var6 < 256) {
               var8 += var5.field_76[var6 << 1];
               var6++;
            }

            var5.field_48 = (byte)(var8 > var16 >>> 2 ? 0 : 1);
         }

         this.field_79.method_6(this);
         this.field_80.method_6(this);
         class_4 var10 = this;
         this.method_10(this.field_76, this.field_79.field_24);
         this.method_10(this.field_77, this.field_80.field_24);
         this.field_81.method_6(this);
         int var13 = 18;

         while (var13 >= 3 && var10.field_78[(class_2.field_18[var13] << 1) + 1] == 0) {
            var13--;
         }

         var10.field_91 += 3 * (var13 + 1) + 5 + 5 + 4;
         var7 = var13;
         var11 = this.field_91 + 3 + 7 >>> 3;
         if ((var14 = this.field_92 + 3 + 7 >>> 3) <= var11) {
            var11 = var14;
         }
      } else {
         var11 = var14 = var3 + 5;
      }

      if (var3 + 4 <= var11 && var2 != -1) {
         this.method_22(var2, var3, var1);
      } else if (var14 == var11) {
         this.method_16(2 + (var1 ? 1 : 0), 3);
         this.method_18(class_3.field_26, class_3.field_27);
      } else {
         this.method_16(4 + (var1 ? 1 : 0), 3);
         var10001 = this.field_79.field_24 + 1;
         int var10002 = this.field_80.field_24 + 1;
         int var18 = var7 + 1;
         var7 = var10002;
         var14 = var10001;
         class_4 var12 = this;
         this.method_16(var14 - 257, 5);
         this.method_16(var7 - 1, 5);
         this.method_16(var18 - 4, 4);

         for (int var9 = 0; var9 < var18; var9++) {
            var12.method_16(var12.field_78[(class_2.field_18[var9] << 1) + 1], 3);
         }

         var12.method_11(var12.field_76, var14 - 1);
         var12.method_11(var12.field_77, var7 - 1);
         this.method_18(this.field_76, this.field_77);
      }

      this.method_7();
      if (var1) {
         this.method_20();
      }

      this.field_62 = this.field_66;
      this.field_41.method_30();
   }

   // $VF: renamed from: a (int, int, boolean) void
   private void method_22(int var1, int var2, boolean var3) {
      this.method_16(0 + (var3 ? 1 : 0), 3);
      boolean var5 = true;
      this.method_20();
      this.field_94 = 8;
      this.method_13((short)var2);
      this.method_13((short)(~var2));
      byte[] var7 = this.field_53;
      System.arraycopy(var7, var1, this.field_43, this.field_46, var2);
      this.field_46 += var2;
   }

   // $VF: renamed from: d () void
   private void method_23() {
      do {
         int var4;
         if ((var4 = this.field_54 - this.field_68 - this.field_66) == 0 && this.field_66 == 0 && this.field_68 == 0) {
            var4 = this.field_50;
         } else if (var4 == -1) {
            var4--;
         } else if (this.field_66 >= this.field_50 + this.field_50 - 262) {
            System.arraycopy(this.field_53, this.field_50, this.field_53, 0, this.field_50);
            this.field_67 = this.field_67 - this.field_50;
            this.field_66 = this.field_66 - this.field_50;
            this.field_62 = this.field_62 - this.field_50;
            int var1 = this.field_58;
            int var3 = this.field_58;

            do {
               int var2 = this.field_56[--var3] & '\uffff';
               this.field_56[var3] = var2 >= this.field_50 ? (short)(var2 - this.field_50) : 0;
            } while (--var1 != 0);

            var1 = this.field_50;
            int var11 = this.field_50;

            do {
               int var9 = this.field_55[--var11] & '\uffff';
               this.field_55[var11] = var9 >= this.field_50 ? (short)(var9 - this.field_50) : 0;
            } while (--var1 != 0);

            var4 += this.field_50;
         }

         if (this.field_41.field_99 == 0) {
            return;
         }

         int var12 = this.field_66 + this.field_68;
         byte[] var10 = this.field_53;
         class_5 var7 = this.field_41;
         int var5 = this.field_41.field_99;
         if (this.field_41.field_99 > var4) {
            var5 = var4;
         }

         int var10000;
         if (var5 == 0) {
            var10000 = 0;
         } else {
            var7.field_99 -= var5;
            if (var7.field_105.field_47 == 0) {
               var7.field_106 = class_5.method_28(var7.field_106, var7.field_97, var7.field_98, var5);
            }

            System.arraycopy(var7.field_97, var7.field_98, var10, var12, var5);
            var7.field_98 += var5;
            var7.field_100 += (long)var5;
            var10000 = var5;
         }

         int var8 = var10000;
         this.field_68 += var8;
         if (this.field_68 >= 3) {
            this.field_57 = this.field_53[this.field_66] & 255;
            this.field_57 = (this.field_57 << this.field_61 ^ this.field_53[this.field_66 + 1] & 255) & this.field_60;
         }
      } while (this.field_68 < 262 && this.field_41.field_99 != 0);
   }

   // $VF: renamed from: a (int) int
   private int method_24(int var1) {
      int var2 = this.field_70;
      int var3 = this.field_66;
      int var5 = this.field_69;
      int var6 = this.field_66 > this.field_50 - 262 ? this.field_66 - (this.field_50 - 262) : 0;
      int var7 = this.field_75;
      int var8 = this.field_52;
      int var9 = this.field_66 + 258;
      byte var10 = this.field_53[var3 + var5 - 1];
      byte var11 = this.field_53[var3 + var5];
      if (this.field_69 >= this.field_74) {
         var2 >>= 2;
      }

      if (var7 > this.field_68) {
         var7 = this.field_68;
      }

      do {
         if (this.field_53[var1 + var5] == var11 && this.field_53[var1 + var5 - 1] == var10 && this.field_53[var1] == this.field_53[var3]) {
            int var4 = var1 + 1;
            if (this.field_53[var4] == this.field_53[var3 + 1]) {
               int var12 = var3 + 2;
               var4++;

               while (
                  this.field_53[++var12] == this.field_53[++var4]
                     && this.field_53[++var12] == this.field_53[++var4]
                     && this.field_53[++var12] == this.field_53[++var4]
                     && this.field_53[++var12] == this.field_53[++var4]
                     && this.field_53[++var12] == this.field_53[++var4]
                     && this.field_53[++var12] == this.field_53[++var4]
                     && this.field_53[++var12] == this.field_53[++var4]
                     && this.field_53[++var12] == this.field_53[++var4]
                     && var12 < var9
               ) {
               }

               var4 = 258 - (var9 - var12);
               var3 = var9 - 258;
               if (var4 > var5) {
                  this.field_67 = var1;
                  var5 = var4;
                  if (var4 >= var7) {
                     break;
                  }

                  var10 = this.field_53[var3 + var4 - 1];
                  var11 = this.field_53[var3 + var4];
               }
            }
         }
      } while ((var1 = this.field_55[var1 & var8] & '\uffff') > var6 && --var2 != 0);

      return var5 <= this.field_68 ? var5 : this.field_68;
   }

   // $VF: renamed from: a (lib.a, int, int) int
   final int method_25(class_5 var1, int var2, int var3) {
      boolean var6 = false;
      byte var7 = 8;
      int var4 = var3;
      byte var8 = 8;
      byte var5 = 0;
      if (var3 < 0) {
         var5 = 1;
         var4 = -var3;
      }

      if (var4 >= 9 && var4 <= 15 && var2 >= 0 && var2 <= 9) {
         var1.field_105 = this;
         this.field_47 = var5;
         this.field_51 = var4;
         this.field_50 = 1 << this.field_51;
         this.field_52 = this.field_50 - 1;
         this.field_59 = 15;
         this.field_58 = 1 << this.field_59;
         this.field_60 = this.field_58 - 1;
         this.field_61 = (this.field_59 + 3 - 1) / 3;
         this.field_53 = new byte[this.field_50 << 1];
         this.field_55 = new short[this.field_50];
         this.field_56 = new short[this.field_58];
         this.field_88 = 16384;
         this.field_43 = new byte[this.field_88 << 2];
         this.field_44 = this.field_88 << 2;
         this.field_90 = this.field_88 / 2;
         this.field_87 = 3 * this.field_88;
         this.field_72 = var2;
         this.field_73 = 0;
         var1.field_100 = var1.field_104 = 0L;
         this.field_46 = 0;
         this.field_45 = 0;
         if (this.field_47 < 0) {
            this.field_47 = 0;
         }

         this.field_42 = this.field_47 != 0 ? 113 : 42;
         var1.field_106 = class_5.method_28(0L, null, 0, 0);
         this.field_49 = 0;
         this.field_79.field_23 = this.field_76;
         this.field_79.field_25 = class_3.field_28;
         this.field_80.field_23 = this.field_77;
         this.field_80.field_25 = class_3.field_29;
         this.field_81.field_23 = this.field_78;
         this.field_81.field_25 = class_3.field_30;
         this.field_95 = 0;
         this.field_96 = 0;
         this.field_94 = 8;
         this.method_7();
         class_4 var11 = this;
         this.field_54 = 2 * this.field_50;
         this.field_56[this.field_58 - 1] = 0;

         for (int var9 = 0; var9 < var11.field_58 - 1; var9++) {
            var11.field_56[var9] = 0;
         }

         var11.field_71 = field_37[var11.field_72];
         var11.field_74 = field_36[var11.field_72];
         var11.field_75 = field_38[var11.field_72];
         var11.field_70 = field_39[var11.field_72];
         var11.field_66 = 0;
         var11.field_62 = 0;
         var11.field_68 = 0;
         var11.field_63 = var11.field_69 = 2;
         var11.field_65 = 0;
         var11.field_57 = 0;
         return 0;
      } else {
         return -2;
      }
   }

   // $VF: renamed from: a () int
   final int method_26() {
      if (this.field_42 != 42 && this.field_42 != 113 && this.field_42 != 666) {
         return -2;
      } else {
         this.field_43 = null;
         this.field_56 = null;
         this.field_55 = null;
         this.field_53 = null;
         return this.field_42 == 113 ? -3 : 0;
      }
   }

   // $VF: renamed from: a (lib.a, int) int
   final int method_27(class_5 var1, int var2) {
      if (var2 <= 4 && var2 >= 0) {
         if (var1.field_101 != null && (var1.field_97 != null || var1.field_99 == 0) && (this.field_42 != 666 || var2 == 4)) {
            if (var1.field_103 == 0) {
               return -5;
            } else {
               this.field_41 = var1;
               int var3 = this.field_49;
               this.field_49 = var2;
               if (this.field_42 == 42) {
                  int var4 = 8 + (this.field_51 - 8 << 4) << 8;
                  int var5;
                  if ((var5 = (this.field_72 - 1 & 0xFF) >> 1) > 3) {
                     var5 = 3;
                  }

                  int var11 = var4 | var5 << 6;
                  if (this.field_66 != 0) {
                     var11 |= 32;
                  }

                  var4 = var11 + (31 - var11 % 31);
                  this.field_42 = 113;
                  this.method_14(var4);
                  if (this.field_66 != 0) {
                     this.method_14((int)(var1.field_106 >>> 16));
                     this.method_14((int)(var1.field_106 & 65535L));
                  }

                  var1.field_106 = class_5.method_28(0L, null, 0, 0);
               }

               if (this.field_46 != 0) {
                  var1.method_30();
                  if (var1.field_103 == 0) {
                     this.field_49 = -1;
                     return 0;
                  }
               } else if (var1.field_99 == 0 && var2 <= var3 && var2 != 4) {
                  return -5;
               }

               if (this.field_42 == 666 && var1.field_99 != 0) {
                  return -5;
               } else {
                  if (var1.field_99 != 0 || this.field_68 != 0 || var2 != 0 && this.field_42 != 666) {
                     int var13 = -1;
                     switch (field_40[this.field_72]) {
                        case 0:
                           var13 = var2;
                           class_4 var10 = this;
                           int var19 = 65535;
                           if (65535 > this.field_44 - 5) {
                              var19 = this.field_44 - 5;
                           }

                           int var24;
                           while (true) {
                              if (var10.field_68 <= 1) {
                                 var10.method_23();
                                 if (var10.field_68 == 0 && var13 == 0) {
                                    var24 = 0;
                                    break;
                                 }

                                 if (var10.field_68 == 0) {
                                    var10.method_21(var13 == 4);
                                    var24 = var10.field_41.field_103 == 0 ? (var13 == 4 ? 2 : 0) : (var13 == 4 ? 3 : 1);
                                    break;
                                 }
                              }

                              var10.field_66 = var10.field_66 + var10.field_68;
                              var10.field_68 = 0;
                              int var22 = var10.field_62 + var19;
                              if (var10.field_66 == 0 || var10.field_66 >= var22) {
                                 var10.field_68 = var10.field_66 - var22;
                                 var10.field_66 = var22;
                                 var10.method_21(false);
                                 if (var10.field_41.field_103 == 0) {
                                    var24 = 0;
                                    break;
                                 }
                              }

                              if (var10.field_66 - var10.field_62 >= var10.field_50 - 262) {
                                 var10.method_21(false);
                                 if (var10.field_41.field_103 == 0) {
                                    var24 = 0;
                                    break;
                                 }
                              }
                           }

                           var13 = var24;
                           break;
                        case 1:
                           var13 = var2;
                           class_4 var9 = this;
                           int var18 = 0;

                           int var23;
                           while (true) {
                              if (var9.field_68 < 262) {
                                 var9.method_23();
                                 if (var9.field_68 < 262 && var13 == 0) {
                                    var23 = 0;
                                    break;
                                 }

                                 if (var9.field_68 == 0) {
                                    var9.method_21(var13 == 4);
                                    var23 = var9.field_41.field_103 == 0 ? (var13 == 4 ? 2 : 0) : (var13 == 4 ? 3 : 1);
                                    break;
                                 }
                              }

                              if (var9.field_68 >= 3) {
                                 var9.field_57 = (var9.field_57 << var9.field_61 ^ var9.field_53[var9.field_66 + 2] & 255) & var9.field_60;
                                 var18 = var9.field_56[var9.field_57] & '\uffff';
                                 var9.field_55[var9.field_66 & var9.field_52] = var9.field_56[var9.field_57];
                                 var9.field_56[var9.field_57] = (short)var9.field_66;
                              }

                              if ((long)var18 != 0L && (var9.field_66 - var18 & 65535) <= var9.field_50 - 262 && var9.field_73 != 2) {
                                 var9.field_63 = var9.method_24(var18);
                              }

                              boolean var21;
                              if (var9.field_63 < 3) {
                                 var21 = var9.method_17(0, var9.field_53[var9.field_66] & 255);
                                 var9.field_68--;
                                 var9.field_66++;
                              } else {
                                 var21 = var9.method_17(var9.field_66 - var9.field_67, var9.field_63 - 3);
                                 var9.field_68 = var9.field_68 - var9.field_63;
                                 if (var9.field_63 <= var9.field_71 && var9.field_68 >= 3) {
                                    var9.field_63--;

                                    do {
                                       var9.field_66++;
                                       var9.field_57 = (var9.field_57 << var9.field_61 ^ var9.field_53[var9.field_66 + 2] & 255) & var9.field_60;
                                       var18 = var9.field_56[var9.field_57] & '\uffff';
                                       var9.field_55[var9.field_66 & var9.field_52] = var9.field_56[var9.field_57];
                                       var9.field_56[var9.field_57] = (short)var9.field_66;
                                    } while (--var9.field_63 != 0);

                                    var9.field_66++;
                                 } else {
                                    var9.field_66 = var9.field_66 + var9.field_63;
                                    var9.field_63 = 0;
                                    var9.field_57 = var9.field_53[var9.field_66] & 255;
                                    var9.field_57 = (var9.field_57 << var9.field_61 ^ var9.field_53[var9.field_66 + 1] & 255) & var9.field_60;
                                 }
                              }

                              if (var21) {
                                 var9.method_21(false);
                                 if (var9.field_41.field_103 == 0) {
                                    var23 = 0;
                                    break;
                                 }
                              }
                           }

                           var13 = var23;
                           break;
                        case 2:
                           var13 = var2;
                           class_4 var8 = this;
                           int var17 = 0;

                           int var10000;
                           while (true) {
                              if (var8.field_68 < 262) {
                                 var8.method_23();
                                 if (var8.field_68 < 262 && var13 == 0) {
                                    var10000 = 0;
                                    break;
                                 }

                                 if (var8.field_68 == 0) {
                                    if (var8.field_65 != 0) {
                                       var8.method_17(0, var8.field_53[var8.field_66 - 1] & 255);
                                       var8.field_65 = 0;
                                    }

                                    var8.method_21(var13 == 4);
                                    var10000 = var8.field_41.field_103 == 0 ? (var13 == 4 ? 2 : 0) : (var13 == 4 ? 3 : 1);
                                    break;
                                 }
                              }

                              if (var8.field_68 >= 3) {
                                 var8.field_57 = (var8.field_57 << var8.field_61 ^ var8.field_53[var8.field_66 + 2] & 255) & var8.field_60;
                                 var17 = var8.field_56[var8.field_57] & '\uffff';
                                 var8.field_55[var8.field_66 & var8.field_52] = var8.field_56[var8.field_57];
                                 var8.field_56[var8.field_57] = (short)var8.field_66;
                              }

                              var8.field_69 = var8.field_63;
                              var8.field_64 = var8.field_67;
                              var8.field_63 = 2;
                              if (var17 != 0 && var8.field_69 < var8.field_71 && (var8.field_66 - var17 & 65535) <= var8.field_50 - 262) {
                                 if (var8.field_73 != 2) {
                                    var8.field_63 = var8.method_24(var17);
                                 }

                                 if (var8.field_63 <= 5 && (var8.field_73 == 1 || var8.field_63 == 3 && var8.field_66 - var8.field_67 > 4096)) {
                                    var8.field_63 = 2;
                                 }
                              }

                              if (var8.field_69 >= 3 && var8.field_63 <= var8.field_69) {
                                 int var7 = var8.field_66 + var8.field_68 - 3;
                                 boolean var6 = var8.method_17(var8.field_66 - 1 - var8.field_64, var8.field_69 - 3);
                                 var8.field_68 = var8.field_68 - (var8.field_69 - 1);
                                 var8.field_69 -= 2;

                                 do {
                                    if (++var8.field_66 <= var7) {
                                       var8.field_57 = (var8.field_57 << var8.field_61 ^ var8.field_53[var8.field_66 + 2] & 255) & var8.field_60;
                                       var17 = var8.field_56[var8.field_57] & '\uffff';
                                       var8.field_55[var8.field_66 & var8.field_52] = var8.field_56[var8.field_57];
                                       var8.field_56[var8.field_57] = (short)var8.field_66;
                                    }
                                 } while (--var8.field_69 != 0);

                                 var8.field_65 = 0;
                                 var8.field_63 = 2;
                                 var8.field_66++;
                                 if (var6) {
                                    var8.method_21(false);
                                    if (var8.field_41.field_103 == 0) {
                                       var10000 = 0;
                                       break;
                                    }
                                 }
                              } else if (var8.field_65 != 0) {
                                 if (var8.method_17(0, var8.field_53[var8.field_66 - 1] & 255)) {
                                    var8.method_21(false);
                                 }

                                 var8.field_66++;
                                 var8.field_68--;
                                 if (var8.field_41.field_103 == 0) {
                                    var10000 = 0;
                                    break;
                                 }
                              } else {
                                 var8.field_65 = 1;
                                 var8.field_66++;
                                 var8.field_68--;
                              }
                           }

                           var13 = var10000;
                     }

                     if (var13 == 2 || var13 == 3) {
                        this.field_42 = 666;
                     }

                     if (var13 == 0 || var13 == 2) {
                        if (var1.field_103 == 0) {
                           this.field_49 = -1;
                        }

                        return 0;
                     }

                     if (var13 == 1) {
                        if (var2 == 1) {
                           this.method_16(2, 3);
                           this.method_15(256, class_3.field_26);
                           this.method_19();
                           if (1 + this.field_94 + 10 - this.field_96 < 9) {
                              this.method_16(2, 3);
                              this.method_15(256, class_3.field_26);
                              this.method_19();
                           }

                           this.field_94 = 7;
                        } else {
                           this.method_22(0, 0, false);
                           if (var2 == 3) {
                              for (int var20 = 0; var20 < this.field_58; var20++) {
                                 this.field_56[var20] = 0;
                              }
                           }
                        }

                        var1.method_30();
                        if (var1.field_103 == 0) {
                           this.field_49 = -1;
                           return 0;
                        }
                     }
                  }

                  if (var2 != 4) {
                     return 0;
                  } else if (this.field_47 != 0) {
                     return 1;
                  } else {
                     this.method_14((int)(var1.field_106 >>> 16));
                     this.method_14((int)(var1.field_106 & 65535L));
                     var1.method_30();
                     this.field_47 = -1;
                     return this.field_46 != 0 ? 0 : 1;
                  }
               }
            }
         } else {
            return -2;
         }
      } else {
         return -2;
      }
   }
}
