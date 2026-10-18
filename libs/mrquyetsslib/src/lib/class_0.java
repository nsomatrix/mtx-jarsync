package lib;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;

// $VF: renamed from: lib.f
final class class_0 extends Thread {
   // $VF: renamed from: a javax.microedition.lcdui.Image
   private Image field_0;
   // $VF: renamed from: a int
   private int field_1;
   // $VF: renamed from: a lib.Canvas
   private Canvas field_2;
   // $VF: renamed from: a java.util.Vector
   private static Vector field_3 = new Vector();
   // $VF: renamed from: a java.lang.String
   private static String field_4;
   // $VF: renamed from: a byte[]
   private static byte[] field_5 = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
   // $VF: renamed from: b byte[]
   private static byte[] field_6 = new byte[]{0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
   // $VF: renamed from: a lib.a
   private class_5 field_7;
   // $VF: renamed from: b int
   private int field_8;
   // $VF: renamed from: c int
   private int field_9;
   // $VF: renamed from: c byte[]
   private byte[] field_10;
   // $VF: renamed from: a java.io.OutputStream
   private OutputStream field_11;
   // $VF: renamed from: a int[]
   private static int[] field_12;

   // $VF: renamed from: a (java.lang.String) java.lang.Throwable
   private static Throwable method_0(String var0) {
      try {
         FileConnection var1;
         if (!(var1 = (FileConnection)Connector.open(var0, 1)).isDirectory()) {
            var1.close();
            var1 = (FileConnection)Connector.open(var0, 2);

            try {
               var1.mkdir();
               var1.close();
               return null;
            } catch (Throwable var3) {
               if (var0.endsWith("/")) {
                  var0 = var0.substring(0, var0.length() - 1);
               }

               method_0(var0.substring(0, var0.lastIndexOf(47) + 1));
               var1.mkdir();
               field_3.addElement(var0);
            }
         }

         try {
            var1.close();
         } catch (Throwable var2) {
         }

         return null;
      } catch (Throwable var4) {
         return var4;
      }
   }

   class_0(Image var1, Canvas var2, int var3) {
      this.field_0 = var1;
      this.field_9 = 0;
      this.field_1 = var3;
      this.field_2 = var2;
   }

   // $VF: renamed from: a (javax.microedition.lcdui.Image) byte[]
   private byte[] method_1(Image var1) {
      try {
         int var2 = var1.getWidth();
         int var3 = var1.getHeight();
         ByteArrayOutputStream var4 = new ByteArrayOutputStream(2048);
         int[] var5 = new int[var2];
         byte[] var6 = new byte[32768];
         this.field_11 = var4;
         byte var7 = 0;
         var7 = 15;
         int var8 = Canvas.savelevel;
         class_5 var20 = this.field_7;
         this.field_7.field_105 = new class_4();
         var20.field_105.method_25(var20, var8, 15);
         int var21 = 0;

         for (int var27 = 0; var27 < var3; var27++) {
            var6[var21++] = 0;
            var1.getRGB(var5, 0, var2, 0, var27, var2, 1);
            if (32768 - var21 < var2 << 2) {
               this.method_2(var6, 0, var21);
               var21 = 0;
            }

            for (int var9 = 0; var9 < var2; var9++) {
               var6[var21++] = (byte)(var5[var9] >> 16);
               var6[var21++] = (byte)(var5[var9] >> 8);
               var6[var21++] = (byte)var5[var9];
               var6[var21++] = (byte)(var5[var9] >> 24);
            }
         }

         if (var21 > 1) {
            this.method_2(var6, 0, var21);
         }

         this.method_3();
         var4.flush();
         class_5 var25 = this.field_7;
         if (this.field_7.field_105 == null) {
            byte var10000 = -2;
         } else {
            var8 = var25.field_105.method_26();
            var25.field_105 = null;
         }

         class_5 var26 = this.field_7;
         this.field_7.field_97 = null;
         var26.field_101 = null;
         this.field_7 = null;
         System.gc();
         byte[] var29 = var4.toByteArray();
         var4.reset();
         DataOutputStream var30;
         (var30 = new DataOutputStream(var4)).write(field_5);
         var30.writeInt(13);
         var30.writeInt(1229472850);
         var30.writeInt(var2);
         var30.writeInt(var3);
         var30.writeByte(8);
         var30.writeInt(100663296);
         var30.writeInt(0);
         int var11 = var29.length;
         var30.writeInt(var11);
         var30.writeInt(1229209940);
         var30.write(var29);
         var30.writeInt(0);
         var30.write(field_6);
         byte[] var16;
         var3 = method_4(var16 = var4.toByteArray(), 12, 17);
         var16[29] = (byte)(var3 >> 24);
         var16[30] = (byte)(var3 >> 16);
         var16[31] = (byte)(var3 >> 8);
         var16[32] = (byte)var3;
         var3 = method_4(var16, 37, var11 + 4);
         int var12 = var11 + 41;
         var16[var12++] = (byte)(var3 >> 24);
         var16[var12++] = (byte)(var3 >> 16);
         var16[var12++] = (byte)(var3 >> 8);
         var16[var12] = (byte)var3;
         System.gc();
         return var16;
      } catch (Throwable var10) {
         Display.mc.showError(var10, "Chụp ảnh màn hình, lỗi màn hình, không nén dữ liệu");
         return null;
      }
   }

   private void method_2(byte[] var1, int var2, int var3) throws IOException {
      if (var3 != 0) {
         this.field_7.field_97 = var1;
         this.field_7.field_98 = 0;
         this.field_7.field_99 = var3;

         do {
            this.field_7.field_101 = this.field_10;
            this.field_7.field_102 = 0;
            this.field_7.field_103 = this.field_8;
            this.field_7.method_29(0);
            this.field_11.write(this.field_10, 0, this.field_8 - this.field_7.field_103);
         } while (this.field_7.field_99 > 0 || this.field_7.field_103 == 0);
      }
   }

   private void method_3() throws IOException {
      do {
         this.field_7.field_101 = this.field_10;
         this.field_7.field_102 = 0;
         this.field_7.field_103 = this.field_8;
         int var1;
         if ((var1 = this.field_7.method_29(4)) != 1 && var1 != 0) {
            throw new IOException();
         }

         if (this.field_8 - this.field_7.field_103 > 0) {
            this.field_11.write(this.field_10, 0, this.field_8 - this.field_7.field_103);
         }
      } while (this.field_7.field_99 > 0 || this.field_7.field_103 == 0);
   }

   // $VF: renamed from: run () void
   public final void run() {
      try {
         Thread.sleep(Canvas.jietime);
         this.field_10 = new byte[this.field_8 = 2048];
         this.field_7 = new class_5();
         byte[] var1;
         label100:
         if ((var1 = this.method_1(this.field_0)) != null && var1.length > 0) {
            FileConnection var2 = null;
            OutputStream var3 = null;
            if (field_4 != Canvas.savepath) {
               String var4;
               int var5 = (var4 = Canvas.savepath).indexOf(47, var4.indexOf("//") + 2);
               int var6;
               if ((var6 = var4.lastIndexOf(47)) > var5) {
                  String var21 = var4.substring(0, var6);
                  Throwable var23;
                  if (!field_3.contains(var21) && (var23 = method_0(var21)) != null) {
                     Display.mc.showError(var23, "Chụp ảnh màn hình, không thể tạo thư mục" + var21);
                     break label100;
                  }
               }

               field_4 = Canvas.savepath;
            }

            String var22 = Canvas.savepath + (System.currentTimeMillis() - 1277000000000L) + ".png";

            try {
               var2 = (FileConnection)Connector.open(var22, 2);

               try {
                  var2.truncate(0L);
               } catch (Throwable var17) {
                  var2.create();
               }

               (var3 = var2.openOutputStream()).write(var1);
               var3.flush();
            } catch (Throwable var18) {
               Display.mc.showError(var18, "Chụp ảnh màn hình, không thể tạo file" + var22);
            } finally {
               try {
                  var3.close();
                  var2.close();
               } catch (Throwable var16) {
               }
            }
         }
      } catch (Throwable var20) {
         try {
            this.field_2.leyPressed(this.field_1);
            this.field_2.leyReleased(this.field_1);
         } catch (Throwable var15) {
         }
      }

      this.field_2.field_108 = false;
   }

   // $VF: renamed from: a (byte[], int, int) int
   private static int method_4(byte[] var0, int var1, int var2) {
      int var3 = -1;
      int[] var4 = field_12;

      while (--var2 >= 0) {
         var3 = var4[(var3 ^ var0[var1++]) & 0xFF] ^ var3 >>> 8;
      }

      return ~var3;
   }

   static {
      int[] var0 = new int[256];

      for (int var1 = 0; var1 < 256; var1++) {
         int var2 = var1;
         int var3 = 8;

         while (--var3 >= 0) {
            if ((var2 & 1) != 0) {
               var2 = -306674912 ^ var2 >>> 1;
            } else {
               var2 >>>= 1;
            }
         }

         var0[var1] = var2;
      }

      field_12 = var0;
   }
}
