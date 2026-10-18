package lib;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Image;
final class PngScreenshotThread extends Thread {
   private Image sourceImage;
   private int triggerKeyCode;
   private Canvas parentCanvas;
   private static Vector createdDirectories = new Vector();
   private static String lastSavedPath;
   private static byte[] PNG_SIGNATURE = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
   private static byte[] IEND_CHUNK = new byte[]{0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
   private ZStream zstream;
   private int bufferSize;
   private int unusedInt;
   private byte[] compressBuffer;
   private OutputStream outputStream;
   private static int[] CRC32_TABLE;
   private static Throwable ensureParentDirectoryExists(String var0) {
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

               ensureParentDirectoryExists(var0.substring(0, var0.lastIndexOf(47) + 1));
               var1.mkdir();
               createdDirectories.addElement(var0);
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

   PngScreenshotThread(Image var1, Canvas var2, int var3) {
      this.sourceImage = var1;
      this.unusedInt = 0;
      this.triggerKeyCode = var3;
      this.parentCanvas = var2;
   }
   private byte[] encodePngBytes(Image var1) {
      try {
         int var2 = var1.getWidth();
         int var3 = var1.getHeight();
         ByteArrayOutputStream var4 = new ByteArrayOutputStream(2048);
         int[] var5 = new int[var2];
         byte[] var6 = new byte[32768];
         this.outputStream = var4;
         byte var7 = 0;
         var7 = 15;
         int var8 = Canvas.savelevel;
         ZStream var20 = this.zstream;
         this.zstream.dstate = new Deflate();
         var20.dstate.deflateInit(var20, var8, 15);
         int var21 = 0;

         for (int var27 = 0; var27 < var3; var27++) {
            var6[var21++] = 0;
            var1.getRGB(var5, 0, var2, 0, var27, var2, 1);
            if (32768 - var21 < var2 << 2) {
               this.deflateChunk(var6, 0, var21);
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
            this.deflateChunk(var6, 0, var21);
         }

         this.finishDeflate();
         var4.flush();
         ZStream var25 = this.zstream;
         if (this.zstream.dstate == null) {
            byte var10000 = -2;
         } else {
            var8 = var25.dstate.deflateEnd();
            var25.dstate = null;
         }

         ZStream var26 = this.zstream;
         this.zstream.next_in = null;
         var26.next_out = null;
         this.zstream = null;
         System.gc();
         byte[] var29 = var4.toByteArray();
         var4.reset();
         DataOutputStream var30;
         (var30 = new DataOutputStream(var4)).write(PNG_SIGNATURE);
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
         var30.write(IEND_CHUNK);
         byte[] var16;
         var3 = updateCrc32(var16 = var4.toByteArray(), 12, 17);
         var16[29] = (byte)(var3 >> 24);
         var16[30] = (byte)(var3 >> 16);
         var16[31] = (byte)(var3 >> 8);
         var16[32] = (byte)var3;
         var3 = updateCrc32(var16, 37, var11 + 4);
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

   private void deflateChunk(byte[] var1, int var2, int var3) throws IOException {
      if (var3 != 0) {
         this.zstream.next_in = var1;
         this.zstream.next_in_index = 0;
         this.zstream.avail_in = var3;

         do {
            this.zstream.next_out = this.compressBuffer;
            this.zstream.next_out_index = 0;
            this.zstream.avail_out = this.bufferSize;
            this.zstream.deflate(0);
            this.outputStream.write(this.compressBuffer, 0, this.bufferSize - this.zstream.avail_out);
         } while (this.zstream.avail_in > 0 || this.zstream.avail_out == 0);
      }
   }

   private void finishDeflate() throws IOException {
      do {
         this.zstream.next_out = this.compressBuffer;
         this.zstream.next_out_index = 0;
         this.zstream.avail_out = this.bufferSize;
         int var1;
         if ((var1 = this.zstream.deflate(4)) != 1 && var1 != 0) {
            throw new IOException();
         }

         if (this.bufferSize - this.zstream.avail_out > 0) {
            this.outputStream.write(this.compressBuffer, 0, this.bufferSize - this.zstream.avail_out);
         }
      } while (this.zstream.avail_in > 0 || this.zstream.avail_out == 0);
   }
   public final void run() {
      try {
         Thread.sleep(Canvas.jietime);
         this.compressBuffer = new byte[this.bufferSize = 2048];
         this.zstream = new ZStream();
         byte[] var1;
         label100:
         if ((var1 = this.encodePngBytes(this.sourceImage)) != null && var1.length > 0) {
            FileConnection var2 = null;
            OutputStream var3 = null;
            if (lastSavedPath != Canvas.savepath) {
               String var4;
               int var5 = (var4 = Canvas.savepath).indexOf(47, var4.indexOf("//") + 2);
               int var6;
               if ((var6 = var4.lastIndexOf(47)) > var5) {
                  String var21 = var4.substring(0, var6);
                  Throwable var23;
                  if (!createdDirectories.contains(var21) && (var23 = ensureParentDirectoryExists(var21)) != null) {
                     Display.mc.showError(var23, "Chụp ảnh màn hình, không thể tạo thư mục" + var21);
                     break label100;
                  }
               }

               lastSavedPath = Canvas.savepath;
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
            this.parentCanvas.leyPressed(this.triggerKeyCode);
            this.parentCanvas.leyReleased(this.triggerKeyCode);
         } catch (Throwable var15) {
         }
      }

      this.parentCanvas.screenshotCapturePending = false;
   }
   private static int updateCrc32(byte[] var0, int var1, int var2) {
      int var3 = -1;
      int[] var4 = CRC32_TABLE;

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

      CRC32_TABLE = var0;
   }
}
