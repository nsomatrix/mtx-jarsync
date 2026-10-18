package lib;
final class ZStream {
   public byte[] next_in;
   public int next_in_index;
   public int avail_in;
   public long total_in;
   public byte[] next_out;
   public int next_out_index;
   public int avail_out;
   public long total_out;
   Deflate dstate;
   public long adler;
   static long computeAdler32(long var0, byte[] var2, int var3, int var4) {
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
   public final int deflate(int var1) {
      return this.dstate == null ? -2 : this.dstate.deflate(this, var1);
   }
   final void flush_pending() {
      int var1 = this.dstate.pending;
      if (this.dstate.pending > this.avail_out) {
         var1 = this.avail_out;
      }

      if (var1 != 0) {
         System.arraycopy(this.dstate.pending_buf, this.dstate.pending_out, this.next_out, this.next_out_index, var1);
         this.next_out_index += var1;
         this.dstate.pending_out += var1;
         this.total_out += (long)var1;
         this.avail_out -= var1;
         this.dstate.pending -= var1;
         if (this.dstate.pending == 0) {
            this.dstate.pending_out = 0;
         }
      }
   }
}
