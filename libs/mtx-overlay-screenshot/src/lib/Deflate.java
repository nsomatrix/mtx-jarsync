package lib;
final class Deflate {
   private static final int[] CONFIG_GOOD_LENGTH = new int[]{0, 4, 4, 4, 4, 8, 8, 8, 32, 32};
   private static final int[] CONFIG_MAX_LAZY = new int[]{0, 4, 5, 6, 4, 16, 16, 32, 128, 258};
   private static final int[] CONFIG_NICE_LENGTH = new int[]{0, 8, 16, 32, 16, 32, 128, 128, 258, 258};
   private static final int[] CONFIG_MAX_CHAIN = new int[]{0, 4, 8, 32, 16, 32, 128, 256, 1024, 4096};
   private static final int[] CONFIG_FUNC = new int[]{0, 1, 1, 1, 2, 2, 2, 2, 2, 2};
   private ZStream strm;
   private int status;
   byte[] pending_buf;
   private int pending_buf_size;
   int pending_out;
   int pending;
   int wrap;
   private byte data_type;
   private int last_flush;
   private int w_size;
   private int w_bits;
   private int w_mask;
   private byte[] window;
   private int window_size;
   private short[] prev;
   private short[] head;
   private int ins_h;
   private int hash_size;
   private int hash_bits;
   private int hash_mask;
   private int hash_shift;
   private int block_start;
   private int match_length;
   private int prev_match;
   private int match_available;
   private int strstart;
   private int match_start;
   private int lookahead;
   private int prev_length;
   private int max_chain_length;
   private int max_lazy_match;
   private int level;
   private int strategy;
   private int good_match;
   private int nice_match;
   private short[] dyn_ltree;
   private short[] dyn_dtree;
   private short[] bl_tree;
   private Tree l_desc = new Tree();
   private Tree d_desc = new Tree();
   private Tree bl_desc = new Tree();
   short[] bl_count = new short[16];
   int[] heap = new int[573];
   int heap_len;
   int heap_max;
   byte[] depth = new byte[573];
   private int l_buf;
   private int lit_bufsize;
   private int last_lit;
   private int d_buf;
   int opt_len;
   int static_len;
   private int matches;
   private int bi_buf;
   private short bi_valid;
   private int bits_sent;

   Deflate() {
      this.dyn_ltree = new short[1146];
      this.dyn_dtree = new short[122];
      this.bl_tree = new short[78];
   }
   private void lm_init() {
      for (int var1 = 0; var1 < 286; var1++) {
         this.dyn_ltree[var1 << 1] = 0;
      }

      for (int var2 = 0; var2 < 30; var2++) {
         this.dyn_dtree[var2 << 1] = 0;
      }

      for (int var3 = 0; var3 < 19; var3++) {
         this.bl_tree[var3 << 1] = 0;
      }

      this.dyn_ltree[512] = 1;
      this.opt_len = this.static_len = 0;
      this.last_lit = this.matches = 0;
   }
   final void pqdownheap(short[] var1, int var2) {
      int var3 = this.heap[var2];

      for (int var4 = var2 << 1; var4 <= this.heap_len; var4 <<= 1) {
         if (var4 < this.heap_len && scan_tree(var1, this.heap[var4 + 1], this.heap[var4], this.depth)) {
            var4++;
         }

         if (scan_tree(var1, var3, this.heap[var4], this.depth)) {
            break;
         }

         this.heap[var2] = this.heap[var4];
         var2 = var4;
      }

      this.heap[var2] = var3;
   }
   private static boolean scan_tree(short[] var0, int var1, int var2, byte[] var3) {
      short var4 = var0[var1 << 1];
      short var5 = var0[var2 << 1];
      return var4 < var5 || var4 == var5 && var3[var1] <= var3[var2];
   }
   private void send_tree(short[] var1, int var2) {
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
               this.bl_tree[var5 << 1] = (short)(this.bl_tree[var5 << 1] + var7);
            } else if (var5 != 0) {
               if (var5 != var4) {
                  this.bl_tree[var5 << 1]++;
               }

               this.bl_tree[32]++;
            } else if (var7 <= 10) {
               this.bl_tree[34]++;
            } else {
               this.bl_tree[36]++;
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
   private void build_bl_tree(short[] var1, int var2) {
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
                  this.send_code(var5, this.bl_tree);
               } while (--var7 != 0);
            } else if (var5 != 0) {
               if (var5 != var4) {
                  this.send_code(var5, this.bl_tree);
                  var7--;
               }

               this.send_code(16, this.bl_tree);
               this.send_bits(var7 - 3, 2);
            } else if (var7 <= 10) {
               this.send_code(17, this.bl_tree);
               this.send_bits(var7 - 3, 3);
            } else {
               this.send_code(18, this.bl_tree);
               this.send_bits(var7 - 11, 7);
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
   private void send_all_trees(byte var1) {
      this.pending_buf[this.pending++] = var1;
   }
   private void init_block(int var1) {
      this.send_all_trees((byte)var1);
      this.send_all_trees((byte)(var1 >>> 8));
   }
   private void put_short(int var1) {
      this.send_all_trees((byte)(var1 >> 8));
      this.send_all_trees((byte)var1);
   }
   private void send_code(int var1, short[] var2) {
      var1 <<= 1;
      this.send_bits(var2[var1] & '\uffff', var2[var1 + 1] & '\uffff');
   }
   private void send_bits(int var1, int var2) {
      if (this.bits_sent > 16 - var2) {
         this.bi_valid = (short)(this.bi_valid | var1 << this.bits_sent & 65535);
         this.init_block(this.bi_valid);
         this.bi_valid = (short)(var1 >>> 16 - this.bits_sent);
         this.bits_sent += var2 - 16;
      } else {
         this.bi_valid = (short)(this.bi_valid | var1 << this.bits_sent & 65535);
         this.bits_sent += var2;
      }
   }
   private boolean _tr_tally(int var1, int var2) {
      this.pending_buf[this.d_buf + (this.last_lit << 1)] = (byte)(var1 >>> 8);
      this.pending_buf[this.d_buf + (this.last_lit << 1) + 1] = (byte)var1;
      this.pending_buf[this.l_buf + this.last_lit] = (byte)var2;
      this.last_lit++;
      if (var1 == 0) {
         this.dyn_ltree[var2 << 1]++;
      } else {
         this.matches++;
         var1--;
         this.dyn_ltree[Tree._length_code[var2] + 256 + 1 << 1]++;
         this.dyn_dtree[Tree.d_code(var1) << 1]++;
      }

      if ((this.last_lit & 8191) == 0 && this.level > 2) {
         int var5 = this.last_lit << 3;
         var2 = this.strstart - this.block_start;

         for (int var3 = 0; var3 < 30; var3++) {
            var5 = (int)((long)var5 + (long)this.dyn_dtree[var3 << 1] * (5L + (long)Tree.extra_dbits[var3]));
         }

         var1 = var5 >>> 3;
         if (this.matches < this.last_lit / 2 && var1 < var2 / 2) {
            return true;
         }
      }

      return this.last_lit == this.lit_bufsize - 1;
   }
   private void compress_block(short[] var1, short[] var2) {
      int var5 = 0;
      if (this.last_lit != 0) {
         do {
            int var3 = this.pending_buf[this.d_buf + (var5 << 1)] << 8 & 0xFF00 | this.pending_buf[this.d_buf + (var5 << 1) + 1] & 255;
            int var4 = this.pending_buf[this.l_buf + var5] & 255;
            var5++;
            if (var3 == 0) {
               this.send_code(var4, var1);
            } else {
               int var6 = Tree._length_code[var4];
               this.send_code(var6 + 256 + 1, var1);
               int var7;
               if ((var7 = Tree.extra_lbits[var6]) != 0) {
                  var4 -= Tree.base_length[var6];
                  this.send_bits(var4, var7);
               }

               var6 = Tree.d_code(--var3);
               this.send_code(var6, var2);
               if ((var7 = Tree.extra_dbits[var6]) != 0) {
                  var3 -= Tree.base_dist[var6];
                  this.send_bits(var3, var7);
               }
            }
         } while (var5 < this.last_lit);
      }

      this.send_code(256, var1);
      this.bi_buf = var1[513];
   }
   private void bi_windup() {
      if (this.bits_sent == 16) {
         this.init_block(this.bi_valid);
         this.bi_valid = 0;
         this.bits_sent = 0;
      } else {
         if (this.bits_sent >= 8) {
            this.send_all_trees((byte)this.bi_valid);
            this.bi_valid = (short)(this.bi_valid >>> 8);
            this.bits_sent -= 8;
         }
      }
   }
   private void bi_flush() {
      if (this.bits_sent > 8) {
         this.init_block(this.bi_valid);
      } else if (this.bits_sent > 0) {
         this.send_all_trees((byte)this.bi_valid);
      }

      this.bi_valid = 0;
      this.bits_sent = 0;
   }
   private void flush_block_only(boolean var1) {
      int var10001 = this.block_start >= 0 ? this.block_start : -1;
      int var3 = this.strstart - this.block_start;
      int var2 = var10001;
      int var7 = 0;
      int var11;
      int var14;
      if (this.level > 0) {
         if (this.data_type == 2) {
            Deflate var5 = this;
            int var6 = 0;
            short var16 = 0;

            short var8;
            for (var8 = 0; var6 < 7; var6++) {
               var8 += var5.dyn_ltree[var6 << 1];
            }

            while (var6 < 128) {
               var16 += var5.dyn_ltree[var6 << 1];
               var6++;
            }

            while (var6 < 256) {
               var8 += var5.dyn_ltree[var6 << 1];
               var6++;
            }

            var5.data_type = (byte)(var8 > var16 >>> 2 ? 0 : 1);
         }

         this.l_desc.build_tree(this);
         this.d_desc.build_tree(this);
         Deflate var10 = this;
         this.send_tree(this.dyn_ltree, this.l_desc.max_code);
         this.send_tree(this.dyn_dtree, this.d_desc.max_code);
         this.bl_desc.build_tree(this);
         int var13 = 18;

         while (var13 >= 3 && var10.bl_tree[(Tree.bl_order[var13] << 1) + 1] == 0) {
            var13--;
         }

         var10.opt_len += 3 * (var13 + 1) + 5 + 5 + 4;
         var7 = var13;
         var11 = this.opt_len + 3 + 7 >>> 3;
         if ((var14 = this.static_len + 3 + 7 >>> 3) <= var11) {
            var11 = var14;
         }
      } else {
         var11 = var14 = var3 + 5;
      }

      if (var3 + 4 <= var11 && var2 != -1) {
         this._tr_flush_block(var2, var3, var1);
      } else if (var14 == var11) {
         this.send_bits(2 + (var1 ? 1 : 0), 3);
         this.compress_block(StaticTree.static_ltree, StaticTree.static_dtree);
      } else {
         this.send_bits(4 + (var1 ? 1 : 0), 3);
         var10001 = this.l_desc.max_code + 1;
         int var10002 = this.d_desc.max_code + 1;
         int var18 = var7 + 1;
         var7 = var10002;
         var14 = var10001;
         Deflate var12 = this;
         this.send_bits(var14 - 257, 5);
         this.send_bits(var7 - 1, 5);
         this.send_bits(var18 - 4, 4);

         for (int var9 = 0; var9 < var18; var9++) {
            var12.send_bits(var12.bl_tree[(Tree.bl_order[var9] << 1) + 1], 3);
         }

         var12.build_bl_tree(var12.dyn_ltree, var14 - 1);
         var12.build_bl_tree(var12.dyn_dtree, var7 - 1);
         this.compress_block(this.dyn_ltree, this.dyn_dtree);
      }

      this.lm_init();
      if (var1) {
         this.bi_flush();
      }

      this.block_start = this.strstart;
      this.strm.flush_pending();
   }
   private void _tr_flush_block(int var1, int var2, boolean var3) {
      this.send_bits(0 + (var3 ? 1 : 0), 3);
      boolean var5 = true;
      this.bi_flush();
      this.bi_buf = 8;
      this.init_block((short)var2);
      this.init_block((short)(~var2));
      byte[] var7 = this.window;
      System.arraycopy(var7, var1, this.pending_buf, this.pending, var2);
      this.pending += var2;
   }
   private void fill_window() {
      do {
         int var4;
         if ((var4 = this.window_size - this.lookahead - this.strstart) == 0 && this.strstart == 0 && this.lookahead == 0) {
            var4 = this.w_size;
         } else if (var4 == -1) {
            var4--;
         } else if (this.strstart >= this.w_size + this.w_size - 262) {
            System.arraycopy(this.window, this.w_size, this.window, 0, this.w_size);
            this.match_start = this.match_start - this.w_size;
            this.strstart = this.strstart - this.w_size;
            this.block_start = this.block_start - this.w_size;
            int var1 = this.hash_size;
            int var3 = this.hash_size;

            do {
               int var2 = this.head[--var3] & '\uffff';
               this.head[var3] = var2 >= this.w_size ? (short)(var2 - this.w_size) : 0;
            } while (--var1 != 0);

            var1 = this.w_size;
            int var11 = this.w_size;

            do {
               int var9 = this.prev[--var11] & '\uffff';
               this.prev[var11] = var9 >= this.w_size ? (short)(var9 - this.w_size) : 0;
            } while (--var1 != 0);

            var4 += this.w_size;
         }

         if (this.strm.avail_in == 0) {
            return;
         }

         int var12 = this.strstart + this.lookahead;
         byte[] var10 = this.window;
         ZStream var7 = this.strm;
         int var5 = this.strm.avail_in;
         if (this.strm.avail_in > var4) {
            var5 = var4;
         }

         int var10000;
         if (var5 == 0) {
            var10000 = 0;
         } else {
            var7.avail_in -= var5;
            if (var7.dstate.wrap == 0) {
               var7.adler = ZStream.computeAdler32(var7.adler, var7.next_in, var7.next_in_index, var5);
            }

            System.arraycopy(var7.next_in, var7.next_in_index, var10, var12, var5);
            var7.next_in_index += var5;
            var7.total_in += (long)var5;
            var10000 = var5;
         }

         int var8 = var10000;
         this.lookahead += var8;
         if (this.lookahead >= 3) {
            this.ins_h = this.window[this.strstart] & 255;
            this.ins_h = (this.ins_h << this.hash_shift ^ this.window[this.strstart + 1] & 255) & this.hash_mask;
         }
      } while (this.lookahead < 262 && this.strm.avail_in != 0);
   }
   private int longest_match(int var1) {
      int var2 = this.max_chain_length;
      int var3 = this.strstart;
      int var5 = this.prev_length;
      int var6 = this.strstart > this.w_size - 262 ? this.strstart - (this.w_size - 262) : 0;
      int var7 = this.nice_match;
      int var8 = this.w_mask;
      int var9 = this.strstart + 258;
      byte var10 = this.window[var3 + var5 - 1];
      byte var11 = this.window[var3 + var5];
      if (this.prev_length >= this.good_match) {
         var2 >>= 2;
      }

      if (var7 > this.lookahead) {
         var7 = this.lookahead;
      }

      do {
         if (this.window[var1 + var5] == var11 && this.window[var1 + var5 - 1] == var10 && this.window[var1] == this.window[var3]) {
            int var4 = var1 + 1;
            if (this.window[var4] == this.window[var3 + 1]) {
               int var12 = var3 + 2;
               var4++;

               while (
                  this.window[++var12] == this.window[++var4]
                     && this.window[++var12] == this.window[++var4]
                     && this.window[++var12] == this.window[++var4]
                     && this.window[++var12] == this.window[++var4]
                     && this.window[++var12] == this.window[++var4]
                     && this.window[++var12] == this.window[++var4]
                     && this.window[++var12] == this.window[++var4]
                     && this.window[++var12] == this.window[++var4]
                     && var12 < var9
               ) {
               }

               var4 = 258 - (var9 - var12);
               var3 = var9 - 258;
               if (var4 > var5) {
                  this.match_start = var1;
                  var5 = var4;
                  if (var4 >= var7) {
                     break;
                  }

                  var10 = this.window[var3 + var4 - 1];
                  var11 = this.window[var3 + var4];
               }
            }
         }
      } while ((var1 = this.prev[var1 & var8] & '\uffff') > var6 && --var2 != 0);

      return var5 <= this.lookahead ? var5 : this.lookahead;
   }
   final int deflateInit(ZStream var1, int var2, int var3) {
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
         var1.dstate = this;
         this.wrap = var5;
         this.w_bits = var4;
         this.w_size = 1 << this.w_bits;
         this.w_mask = this.w_size - 1;
         this.hash_bits = 15;
         this.hash_size = 1 << this.hash_bits;
         this.hash_mask = this.hash_size - 1;
         this.hash_shift = (this.hash_bits + 3 - 1) / 3;
         this.window = new byte[this.w_size << 1];
         this.prev = new short[this.w_size];
         this.head = new short[this.hash_size];
         this.lit_bufsize = 16384;
         this.pending_buf = new byte[this.lit_bufsize << 2];
         this.pending_buf_size = this.lit_bufsize << 2;
         this.d_buf = this.lit_bufsize / 2;
         this.l_buf = 3 * this.lit_bufsize;
         this.level = var2;
         this.strategy = 0;
         var1.total_in = var1.total_out = 0L;
         this.pending = 0;
         this.pending_out = 0;
         if (this.wrap < 0) {
            this.wrap = 0;
         }

         this.status = this.wrap != 0 ? 113 : 42;
         var1.adler = ZStream.computeAdler32(0L, null, 0, 0);
         this.last_flush = 0;
         this.l_desc.dyn_tree = this.dyn_ltree;
         this.l_desc.stat_desc = StaticTree.static_l_desc;
         this.d_desc.dyn_tree = this.dyn_dtree;
         this.d_desc.stat_desc = StaticTree.static_d_desc;
         this.bl_desc.dyn_tree = this.bl_tree;
         this.bl_desc.stat_desc = StaticTree.static_bl_desc;
         this.bi_valid = 0;
         this.bits_sent = 0;
         this.bi_buf = 8;
         this.lm_init();
         Deflate var11 = this;
         this.window_size = 2 * this.w_size;
         this.head[this.hash_size - 1] = 0;

         for (int var9 = 0; var9 < var11.hash_size - 1; var9++) {
            var11.head[var9] = 0;
         }

         var11.max_lazy_match = CONFIG_MAX_LAZY[var11.level];
         var11.good_match = CONFIG_GOOD_LENGTH[var11.level];
         var11.nice_match = CONFIG_NICE_LENGTH[var11.level];
         var11.max_chain_length = CONFIG_MAX_CHAIN[var11.level];
         var11.strstart = 0;
         var11.block_start = 0;
         var11.lookahead = 0;
         var11.match_length = var11.prev_length = 2;
         var11.match_available = 0;
         var11.ins_h = 0;
         return 0;
      } else {
         return -2;
      }
   }
   final int deflateEnd() {
      if (this.status != 42 && this.status != 113 && this.status != 666) {
         return -2;
      } else {
         this.pending_buf = null;
         this.head = null;
         this.prev = null;
         this.window = null;
         return this.status == 113 ? -3 : 0;
      }
   }
   final int deflate(ZStream var1, int var2) {
      if (var2 <= 4 && var2 >= 0) {
         if (var1.next_out != null && (var1.next_in != null || var1.avail_in == 0) && (this.status != 666 || var2 == 4)) {
            if (var1.avail_out == 0) {
               return -5;
            } else {
               this.strm = var1;
               int var3 = this.last_flush;
               this.last_flush = var2;
               if (this.status == 42) {
                  int var4 = 8 + (this.w_bits - 8 << 4) << 8;
                  int var5;
                  if ((var5 = (this.level - 1 & 0xFF) >> 1) > 3) {
                     var5 = 3;
                  }

                  int var11 = var4 | var5 << 6;
                  if (this.strstart != 0) {
                     var11 |= 32;
                  }

                  var4 = var11 + (31 - var11 % 31);
                  this.status = 113;
                  this.put_short(var4);
                  if (this.strstart != 0) {
                     this.put_short((int)(var1.adler >>> 16));
                     this.put_short((int)(var1.adler & 65535L));
                  }

                  var1.adler = ZStream.computeAdler32(0L, null, 0, 0);
               }

               if (this.pending != 0) {
                  var1.flush_pending();
                  if (var1.avail_out == 0) {
                     this.last_flush = -1;
                     return 0;
                  }
               } else if (var1.avail_in == 0 && var2 <= var3 && var2 != 4) {
                  return -5;
               }

               if (this.status == 666 && var1.avail_in != 0) {
                  return -5;
               } else {
                  if (var1.avail_in != 0 || this.lookahead != 0 || var2 != 0 && this.status != 666) {
                     int var13 = -1;
                     switch (CONFIG_FUNC[this.level]) {
                        case 0:
                           var13 = var2;
                           Deflate var10 = this;
                           int var19 = 65535;
                           if (65535 > this.pending_buf_size - 5) {
                              var19 = this.pending_buf_size - 5;
                           }

                           int var24;
                           while (true) {
                              if (var10.lookahead <= 1) {
                                 var10.fill_window();
                                 if (var10.lookahead == 0 && var13 == 0) {
                                    var24 = 0;
                                    break;
                                 }

                                 if (var10.lookahead == 0) {
                                    var10.flush_block_only(var13 == 4);
                                    var24 = var10.strm.avail_out == 0 ? (var13 == 4 ? 2 : 0) : (var13 == 4 ? 3 : 1);
                                    break;
                                 }
                              }

                              var10.strstart = var10.strstart + var10.lookahead;
                              var10.lookahead = 0;
                              int var22 = var10.block_start + var19;
                              if (var10.strstart == 0 || var10.strstart >= var22) {
                                 var10.lookahead = var10.strstart - var22;
                                 var10.strstart = var22;
                                 var10.flush_block_only(false);
                                 if (var10.strm.avail_out == 0) {
                                    var24 = 0;
                                    break;
                                 }
                              }

                              if (var10.strstart - var10.block_start >= var10.w_size - 262) {
                                 var10.flush_block_only(false);
                                 if (var10.strm.avail_out == 0) {
                                    var24 = 0;
                                    break;
                                 }
                              }
                           }

                           var13 = var24;
                           break;
                        case 1:
                           var13 = var2;
                           Deflate var9 = this;
                           int var18 = 0;

                           int var23;
                           while (true) {
                              if (var9.lookahead < 262) {
                                 var9.fill_window();
                                 if (var9.lookahead < 262 && var13 == 0) {
                                    var23 = 0;
                                    break;
                                 }

                                 if (var9.lookahead == 0) {
                                    var9.flush_block_only(var13 == 4);
                                    var23 = var9.strm.avail_out == 0 ? (var13 == 4 ? 2 : 0) : (var13 == 4 ? 3 : 1);
                                    break;
                                 }
                              }

                              if (var9.lookahead >= 3) {
                                 var9.ins_h = (var9.ins_h << var9.hash_shift ^ var9.window[var9.strstart + 2] & 255) & var9.hash_mask;
                                 var18 = var9.head[var9.ins_h] & '\uffff';
                                 var9.prev[var9.strstart & var9.w_mask] = var9.head[var9.ins_h];
                                 var9.head[var9.ins_h] = (short)var9.strstart;
                              }

                              if ((long)var18 != 0L && (var9.strstart - var18 & 65535) <= var9.w_size - 262 && var9.strategy != 2) {
                                 var9.match_length = var9.longest_match(var18);
                              }

                              boolean var21;
                              if (var9.match_length < 3) {
                                 var21 = var9._tr_tally(0, var9.window[var9.strstart] & 255);
                                 var9.lookahead--;
                                 var9.strstart++;
                              } else {
                                 var21 = var9._tr_tally(var9.strstart - var9.match_start, var9.match_length - 3);
                                 var9.lookahead = var9.lookahead - var9.match_length;
                                 if (var9.match_length <= var9.max_lazy_match && var9.lookahead >= 3) {
                                    var9.match_length--;

                                    do {
                                       var9.strstart++;
                                       var9.ins_h = (var9.ins_h << var9.hash_shift ^ var9.window[var9.strstart + 2] & 255) & var9.hash_mask;
                                       var18 = var9.head[var9.ins_h] & '\uffff';
                                       var9.prev[var9.strstart & var9.w_mask] = var9.head[var9.ins_h];
                                       var9.head[var9.ins_h] = (short)var9.strstart;
                                    } while (--var9.match_length != 0);

                                    var9.strstart++;
                                 } else {
                                    var9.strstart = var9.strstart + var9.match_length;
                                    var9.match_length = 0;
                                    var9.ins_h = var9.window[var9.strstart] & 255;
                                    var9.ins_h = (var9.ins_h << var9.hash_shift ^ var9.window[var9.strstart + 1] & 255) & var9.hash_mask;
                                 }
                              }

                              if (var21) {
                                 var9.flush_block_only(false);
                                 if (var9.strm.avail_out == 0) {
                                    var23 = 0;
                                    break;
                                 }
                              }
                           }

                           var13 = var23;
                           break;
                        case 2:
                           var13 = var2;
                           Deflate var8 = this;
                           int var17 = 0;

                           int var10000;
                           while (true) {
                              if (var8.lookahead < 262) {
                                 var8.fill_window();
                                 if (var8.lookahead < 262 && var13 == 0) {
                                    var10000 = 0;
                                    break;
                                 }

                                 if (var8.lookahead == 0) {
                                    if (var8.match_available != 0) {
                                       var8._tr_tally(0, var8.window[var8.strstart - 1] & 255);
                                       var8.match_available = 0;
                                    }

                                    var8.flush_block_only(var13 == 4);
                                    var10000 = var8.strm.avail_out == 0 ? (var13 == 4 ? 2 : 0) : (var13 == 4 ? 3 : 1);
                                    break;
                                 }
                              }

                              if (var8.lookahead >= 3) {
                                 var8.ins_h = (var8.ins_h << var8.hash_shift ^ var8.window[var8.strstart + 2] & 255) & var8.hash_mask;
                                 var17 = var8.head[var8.ins_h] & '\uffff';
                                 var8.prev[var8.strstart & var8.w_mask] = var8.head[var8.ins_h];
                                 var8.head[var8.ins_h] = (short)var8.strstart;
                              }

                              var8.prev_length = var8.match_length;
                              var8.prev_match = var8.match_start;
                              var8.match_length = 2;
                              if (var17 != 0 && var8.prev_length < var8.max_lazy_match && (var8.strstart - var17 & 65535) <= var8.w_size - 262) {
                                 if (var8.strategy != 2) {
                                    var8.match_length = var8.longest_match(var17);
                                 }

                                 if (var8.match_length <= 5 && (var8.strategy == 1 || var8.match_length == 3 && var8.strstart - var8.match_start > 4096)) {
                                    var8.match_length = 2;
                                 }
                              }

                              if (var8.prev_length >= 3 && var8.match_length <= var8.prev_length) {
                                 int var7 = var8.strstart + var8.lookahead - 3;
                                 boolean var6 = var8._tr_tally(var8.strstart - 1 - var8.prev_match, var8.prev_length - 3);
                                 var8.lookahead = var8.lookahead - (var8.prev_length - 1);
                                 var8.prev_length -= 2;

                                 do {
                                    if (++var8.strstart <= var7) {
                                       var8.ins_h = (var8.ins_h << var8.hash_shift ^ var8.window[var8.strstart + 2] & 255) & var8.hash_mask;
                                       var17 = var8.head[var8.ins_h] & '\uffff';
                                       var8.prev[var8.strstart & var8.w_mask] = var8.head[var8.ins_h];
                                       var8.head[var8.ins_h] = (short)var8.strstart;
                                    }
                                 } while (--var8.prev_length != 0);

                                 var8.match_available = 0;
                                 var8.match_length = 2;
                                 var8.strstart++;
                                 if (var6) {
                                    var8.flush_block_only(false);
                                    if (var8.strm.avail_out == 0) {
                                       var10000 = 0;
                                       break;
                                    }
                                 }
                              } else if (var8.match_available != 0) {
                                 if (var8._tr_tally(0, var8.window[var8.strstart - 1] & 255)) {
                                    var8.flush_block_only(false);
                                 }

                                 var8.strstart++;
                                 var8.lookahead--;
                                 if (var8.strm.avail_out == 0) {
                                    var10000 = 0;
                                    break;
                                 }
                              } else {
                                 var8.match_available = 1;
                                 var8.strstart++;
                                 var8.lookahead--;
                              }
                           }

                           var13 = var10000;
                     }

                     if (var13 == 2 || var13 == 3) {
                        this.status = 666;
                     }

                     if (var13 == 0 || var13 == 2) {
                        if (var1.avail_out == 0) {
                           this.last_flush = -1;
                        }

                        return 0;
                     }

                     if (var13 == 1) {
                        if (var2 == 1) {
                           this.send_bits(2, 3);
                           this.send_code(256, StaticTree.static_ltree);
                           this.bi_windup();
                           if (1 + this.bi_buf + 10 - this.bits_sent < 9) {
                              this.send_bits(2, 3);
                              this.send_code(256, StaticTree.static_ltree);
                              this.bi_windup();
                           }

                           this.bi_buf = 7;
                        } else {
                           this._tr_flush_block(0, 0, false);
                           if (var2 == 3) {
                              for (int var20 = 0; var20 < this.hash_size; var20++) {
                                 this.head[var20] = 0;
                              }
                           }
                        }

                        var1.flush_pending();
                        if (var1.avail_out == 0) {
                           this.last_flush = -1;
                           return 0;
                        }
                     }
                  }

                  if (var2 != 4) {
                     return 0;
                  } else if (this.wrap != 0) {
                     return 1;
                  } else {
                     this.put_short((int)(var1.adler >>> 16));
                     this.put_short((int)(var1.adler & 65535L));
                     var1.flush_pending();
                     this.wrap = -1;
                     return this.pending != 0 ? 0 : 1;
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
