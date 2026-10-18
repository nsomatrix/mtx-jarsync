package lib;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class List extends javax.microedition.lcdui.Canvas implements Choice {
   // $VF: renamed from: a java.lang.String[]
   private String[] field_6;
   // $VF: renamed from: a javax.microedition.lcdui.Image[]
   private Image[] field_7;
   // $VF: renamed from: a javax.microedition.lcdui.Font[]
   private Font[] field_8;
   // $VF: renamed from: a boolean[]
   private boolean[] field_9;
   // $VF: renamed from: a int
   private int field_10;
   // $VF: renamed from: b int
   private int field_11;
   // $VF: renamed from: c int
   private int field_12;
   // $VF: renamed from: d int
   private int field_13;
   // $VF: renamed from: e int
   private int field_14;
   // $VF: renamed from: f int
   private int field_15;
   // $VF: renamed from: g int
   private int field_16;
   // $VF: renamed from: a javax.microedition.lcdui.CommandListener
   private CommandListener field_17;
   // $VF: renamed from: a java.util.Vector
   private Vector field_18;
   public static final Command SELECT_COMMAND = javax.microedition.lcdui.List.SELECT_COMMAND;
   // $VF: renamed from: a javax.microedition.lcdui.Command
   private Command field_19;
   // $VF: renamed from: a javax.microedition.lcdui.Font
   private static Font field_20 = Font.getFont(0, 0, 8);
   // $VF: renamed from: h int
   private int field_21;
   // $VF: renamed from: c boolean
   private boolean field_22;
   // $VF: renamed from: a java.lang.String
   private String field_23;
   // $VF: renamed from: i int
   private int field_24;
   // $VF: renamed from: d boolean
   private boolean field_25;
   // $VF: renamed from: a boolean
   boolean field_26;
   // $VF: renamed from: a java.util.Calendar
   private static final Calendar field_27 = Calendar.getInstance();
   // $VF: renamed from: a java.util.Date
   private static final Date field_28 = new Date();
   // $VF: renamed from: j int
   private int field_29;
   // $VF: renamed from: b boolean
   boolean field_30;

   public List(String var1, int var2) {
      super.setFullScreenMode(true);
      this.method_1(var1, var2);
      this.field_6 = new String[20];
      this.field_8 = new Font[20];
      this.field_7 = new Image[20];
      if (var2 == 2) {
         this.field_9 = new boolean[20];
      }

      this.field_12 = 0;
   }

   // $VF: renamed from: a (java.lang.String, int) void
   private void method_1(String var1, int var2) {
      if (var2 != 1 && var2 != 2 && var2 != 3) {
         throw new IllegalArgumentException();
      } else {
         this.field_14 = var2;
         this.field_24 = field_20.getHeight() + 4;
         this.field_23 = var1 == null ? "" : var1;
         this.setTitle(var1);
         this.setFullScreenMode(true);
         this.field_19 = SELECT_COMMAND;
         this.field_18 = new Vector();
         this.field_21 = 1;
         this.field_11 = 1;
         this.field_25 = false;
      }
   }

   public List(String var1, int var2, String[] var3, Image[] var4) {
      this.method_1(var1, var2);
      if (var3 == null) {
         throw new NullPointerException();
      } else {
         this.field_6 = var3;
         this.field_12 = var3.length;
         if (var4 == null) {
            this.field_7 = new Image[this.field_12];
         } else {
            if (var4.length != this.field_12) {
               throw new IllegalArgumentException();
            }

            this.field_7 = var4;
         }

         this.field_8 = new Font[this.field_12];

         for (int var5 = var3.length - 1; var5 >= 0; var5--) {
            this.field_8[var5] = field_20;
         }

         if (var2 == 2) {
            this.field_9 = new boolean[this.field_12];
         }
      }
   }

   public int append(String var1, Image var2) {
      if (var1 != null) {
         if (this.field_12 == this.field_6.length - 1) {
            this.method_3();
         }

         this.field_6[this.field_12] = var1;
         this.field_7[this.field_12] = var2;
         this.field_8[this.field_12] = field_20;
         if (this.field_14 == 2) {
            this.field_9[this.field_12] = false;
         }

         this.repaint();
         return this.field_12++;
      } else {
         return -1;
      }
   }

   public void delete(int var1) {
      if (var1 < this.field_12 && var1 >= 0) {
         System.arraycopy(this.field_6, var1 + 1, this.field_6, var1, this.field_12 - var1 - 1);
         System.arraycopy(this.field_7, var1 + 1, this.field_7, var1, this.field_12 - var1 - 1);
         System.arraycopy(this.field_8, var1 + 1, this.field_8, var1, this.field_12 - var1 - 1);
         if (this.field_14 == 2) {
            System.arraycopy(this.field_9, var1 + 1, this.field_9, var1, this.field_12 - var1 - 1);
         }
      }

      this.field_12--;
      if (this.field_10 + this.field_11 > this.field_12) {
         this.method_5();
      }

      this.repaint();
   }

   public void deleteAll() {
      this.field_12 = 0;
      this.field_10 = 0;
      this.field_11 = 1;
   }

   public int getFitPolicy() {
      return this.field_13;
   }

   public Font getFont(int var1) {
      return this.field_8[var1];
   }

   public Image getImage(int var1) {
      return this.field_7[var1];
   }

   public int getSelectedFlags(boolean[] var1) {
      if (this.field_14 == 2) {
         int var2 = 0;

         for (int var3 = 0; var3 < this.field_12; var3++) {
            if (var1[var3] = this.field_9[var3]) {
               var2++;
            }
         }

         return var2;
      } else {
         var1[this.field_10 + this.field_11] = true;
         return 1;
      }
   }

   public int getSelectedIndex() {
      if (this.field_14 == 3) {
         return this.field_10 + this.field_11 - 1;
      } else {
         return this.field_14 == 1 ? this.field_16 : -1;
      }
   }

   public String getString(int var1) {
      return var1 >= 0 && var1 < this.field_12 ? this.field_6[var1] : null;
   }

   public void insert(int var1, String var2, Image var3) {
      if (var1 >= this.field_12) {
         this.append(var2, var3);
      } else {
         if (var1 < 0) {
            var1 = 0;
         }

         if (this.field_12 == this.field_6.length - 1) {
            this.method_3();
         }

         for (int var4 = this.field_12; var4 > var1; var4--) {
            this.field_6[var4] = this.field_6[var4 - 1];
            this.field_8[var4] = this.field_8[var4 - 1];
            this.field_7[var4] = this.field_7[var4 - 1];
            if (this.field_14 == 2) {
               this.field_9[var4] = this.field_9[var4 - 1];
            }
         }

         this.field_12++;
         this.field_6[var1] = var2;
         this.field_8[var1] = field_20;
         this.field_7[var1] = var3;
         if (this.field_14 == 2) {
            this.field_9[var1] = false;
         }
      }

      this.repaint();
   }

   public boolean isSelected(int var1) {
      return this.field_14 == 2 ? this.field_9[var1] : this.getSelectedIndex() == var1;
   }

   public void removeCommand(Command var1) {
      if (var1 == this.field_19) {
         this.field_19 = null;
      }

      this.field_18.removeElement(var1);
   }

   public void set(int var1, String var2, Image var3) {
      if (var1 >= 0 && var1 < this.field_12 && var2 != null) {
         this.field_6[var1] = var2;
         this.field_7[var1] = var3;
         this.repaint();
      }
   }

   public void setFitPolicy(int var1) {
      if (var1 != 0 && var1 != 2 && var1 != 1) {
         throw new IllegalArgumentException();
      } else {
         this.field_13 = var1;
      }
   }

   public void setFont(int var1, Font var2) {
      if (var1 >= 0 && var1 < this.field_12 && var2 != null) {
         this.field_8[var1] = var2;
         this.repaint();
      }
   }

   public void setSelectCommand(Command var1) {
      if (this.field_14 != 2) {
         this.field_19 = var1;
      }
   }

   public void setSelectedFlags(boolean[] var1) {
      if (this.field_14 == 2) {
         System.arraycopy(var1, 0, this.field_9, 0, this.field_12);
      }
   }

   public void setSelectedIndex(int var1, boolean var2) {
      if (var1 < this.field_12) {
         if (this.field_14 == 2 && var2) {
            this.field_9[var1] = var2;
         } else if (this.field_14 == 1) {
            this.field_16 = var1;
         } else {
            this.field_11 = 1;
            this.field_10 = var1;
         }
      }

      this.repaint();
   }

   public int size() {
      return this.field_12;
   }

   public void addCommand(Command var1) {
      if (var1 != null && !this.field_18.contains(var1)) {
         int var2 = var1.getCommandType();
         if (this.field_18.size() <= 0) {
            this.field_18.addElement(var1);
         } else {
            int var3 = 0;

            while (true) {
               if (var3 >= this.field_18.size()) {
                  this.field_18.addElement(var1);
                  break;
               }

               Command var4 = (Command)this.field_18.elementAt(var3);
               if (var2 == var4.getCommandType()) {
                  if (var1.getPriority() > var4.getPriority()) {
                     this.field_18.insertElementAt(var1, var3);
                     break;
                  }
               } else {
                  int var5 = var4.getCommandType();
                  boolean var10000;
                  switch (var5) {
                     case 1:
                        if (var2 != 4 && var2 != 8) {
                           var10000 = true;
                           break;
                        }

                        var10000 = false;
                        break;
                     case 2:
                        if (var2 != 6 && var2 != 7) {
                           var10000 = false;
                           break;
                        }

                        var10000 = true;
                        break;
                     case 3:
                        if (var2 != 6 && var2 != 7 && var2 != 2) {
                           var10000 = false;
                           break;
                        }

                        var10000 = true;
                        break;
                     case 4:
                     default:
                        var10000 = true;
                        break;
                     case 5:
                        if (var2 != 1 && var2 != 8 && var2 != 4) {
                           var10000 = true;
                           break;
                        }

                        var10000 = false;
                        break;
                     case 6:
                        var10000 = false;
                        break;
                     case 7:
                        var10000 = var2 == 6;
                        break;
                     case 8:
                        var10000 = var2 != 4;
                  }

                  if (var10000) {
                     this.field_18.insertElementAt(var1, var3);
                     break;
                  }
               }

               var3++;
            }
         }

         this.repaint();
      }
   }

   public void setCommandListener(CommandListener var1) {
      this.field_17 = var1;
   }

   // $VF: renamed from: a () void
   private void method_3() {
      int var1;
      String[] var2 = new String[var1 = (this.field_6.length << 1) + 10];
      System.arraycopy(this.field_6, 0, var2, 0, this.field_12);
      this.field_6 = var2;
      Font[] var4 = new Font[var1];
      System.arraycopy(this.field_8, 0, var4, 0, this.field_12);
      this.field_8 = var4;
      Image[] var5 = new Image[var1];
      System.arraycopy(this.field_7, 0, var5, 0, this.field_12);
      this.field_7 = var5;
      if (this.field_14 == 2) {
         boolean[] var3 = new boolean[var1];
         System.arraycopy(this.field_9, 0, var3, 0, this.field_12);
         this.field_9 = var3;
      }
   }

   public void keyRepeated(int var1) {
      this.keyPressed(var1);
   }

   // $VF: renamed from: b () void
   private void method_4() {
      if (!this.field_26) {
         this.field_26 = true;
         new SList(this).start();
      }
   }

   public void keyPressed(int var1) {
      label159: {
         label158: {
            List var10000;
            boolean var10001;
            label157: {
               switch (var1) {
                  case -7:
                     if (!this.field_25) {
                        if (this.field_22) {
                           this.field_22 = false;
                        } else if (this.field_17 != null && this.field_18.size() > 0) {
                           this.field_17.commandAction((Command)this.field_18.elementAt(0), this);
                        }
                        break label159;
                     }

                     var10000 = this;
                     break;
                  case -6:
                     if (!this.field_25) {
                        if (this.field_22) {
                           this.field_22 = false;
                           this.repaint();
                           if (this.field_17 != null) {
                              this.field_17.commandAction((Command)this.field_18.elementAt(this.field_15 + 1), this);
                           }
                        } else if (this.field_18.size() > 2) {
                           this.field_22 = true;
                        } else if (this.field_17 != null && this.field_18.size() > 1) {
                           this.field_17.commandAction((Command)this.field_18.elementAt(1), this);
                        }
                        break label159;
                     }

                     var10000 = this;
                     break;
                  case -5:
                     if (this.field_22) {
                        this.keyPressed(-6);
                        return;
                     }

                     if (this.field_14 == 3 && this.field_17 != null && this.field_19 != null) {
                        this.field_17.commandAction(this.field_19, this);
                     } else {
                        var1 = this.field_10 + this.field_11 - 1;
                        if (this.field_14 == 2) {
                           this.field_9[var1] = !this.field_9[var1];
                        } else if (this.field_14 == 1) {
                           if (this.field_16 == var1 && this.field_17 != null && this.field_19 != null) {
                              this.field_17.commandAction(this.field_19, this);
                           } else {
                              this.field_16 = var1;
                           }
                        }
                     }
                     break label159;
                  case -4:
                     if (!this.field_22) {
                        if (this.field_12 <= this.field_21) {
                           this.field_10 = 0;
                           this.field_11 = this.field_11 == 1 ? this.field_12 : 1;
                        } else if (this.field_21 + this.field_10 + this.field_11 <= this.field_12) {
                           this.field_10 = this.field_10 + this.field_21;
                        } else if (this.field_12 - this.field_10 <= this.field_21) {
                           this.field_10 = 0;
                           this.field_11 = 1;
                        } else {
                           this.field_10 = this.field_10 + this.field_21;
                           this.field_11 = this.field_12 - this.field_10;
                        }

                        this.method_4();
                     }
                     break label159;
                  case -3:
                     if (!this.field_22) {
                        if (this.field_12 <= this.field_21) {
                           this.field_10 = 0;
                           this.field_11 = this.field_11 == 1 ? this.field_12 : 1;
                        } else if (this.field_10 >= this.field_21) {
                           this.field_10 = this.field_10 - this.field_21;
                        } else {
                           if (this.field_10 > 0) {
                              this.field_10 = 0;
                           } else if (this.field_12 > 0) {
                              this.field_10 = Math.max(0, this.field_12 - this.field_21);
                           }

                           this.field_11 = 1;
                        }

                        this.method_4();
                     }
                     break label159;
                  case -2:
                     if (!this.field_22) {
                        if (this.field_11 < this.field_21) {
                           if (this.field_11 + this.field_10 < this.field_12) {
                              this.field_11++;
                           } else {
                              this.field_10 = 0;
                              this.field_11 = 1;
                           }
                        } else if (this.field_11 + this.field_10 < this.field_12) {
                           this.field_10++;
                        } else {
                           this.field_10 = 0;
                           this.field_11 = 1;
                        }

                        this.method_4();
                     } else {
                        this.field_15--;
                        if (this.field_15 < 0) {
                           this.field_15 = this.field_18.size() - 2;
                        }
                     }
                     break label159;
                  case -1:
                     if (!this.field_22) {
                        this.method_5();
                        this.method_4();
                     } else {
                        this.field_15++;
                        if (this.field_15 > this.field_18.size() - 2) {
                           this.field_15 = 0;
                        }
                     }
                  case 0:
                  case 1:
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                  case 7:
                  case 8:
                  case 9:
                  case 10:
                  case 11:
                  case 12:
                  case 13:
                  case 14:
                  case 15:
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                  case 20:
                  case 21:
                  case 22:
                  case 23:
                  case 24:
                  case 25:
                  case 26:
                  case 27:
                  case 28:
                  case 29:
                  case 30:
                  case 31:
                  case 32:
                  case 33:
                  case 34:
                  case 36:
                  case 37:
                  case 38:
                  case 39:
                  case 40:
                  case 41:
                  case 43:
                  case 44:
                  case 45:
                  case 46:
                  case 47:
                  case 48:
                  default:
                     break label159;
                  case 35:
                     var10000 = this;
                     if (!this.field_25) {
                        var10001 = true;
                        break label157;
                     }
                     break;
                  case 42:
                     break label158;
                  case 49:
                  case 50:
                  case 51:
                  case 52:
                  case 53:
                  case 54:
                  case 55:
                  case 56:
                  case 57:
                     int var2 = var1 - 48;
                     if (!this.field_22 && var2 + this.field_10 <= this.field_12) {
                        this.field_11 = var2;
                        this.repaint();
                        if (this.field_14 == 3 && this.field_17 != null && this.field_19 != null) {
                           this.field_17.commandAction(this.field_19, this);
                        }
                     }
                     break label158;
               }

               var10001 = false;
            }

            var10000.field_25 = var10001;
            break label159;
         }

         Display.mc.pause();
      }

      this.repaint();
   }

   // $VF: renamed from: c () void
   private void method_5() {
      if (this.field_11 > 1) {
         this.field_11--;
      } else if (this.field_10 > 0) {
         this.field_10--;
      } else {
         if (this.field_12 > 0) {
            this.field_10 = Math.max(0, this.field_12 - this.field_21);
            this.field_11 = this.field_12 - this.field_10;
         }
      }
   }

   protected void paint(Graphics var1) {
      int var2 = super.getWidth();
      int var3 = super.getHeight();
      var1.setColor(0);
      var1.fillRect(0, 0, var2, var3);
      var1.setColor(16777215);
      var1.setFont(field_20);
      int var4 = (this.field_25 ? 0 : this.field_24) + 1;
      int var5 = this.field_25 ? var3 - 4 : var3 - (field_20.getHeight() << 1) + 4;
      if (this.field_12 > 0) {
         int var6 = this.field_10;

         for (this.field_21 = 0; var6 < this.field_12 && var4 < var5; var6++) {
            this.field_21++;
            Font var8 = this.field_8[var6];
            Image var9 = this.field_7[var6];
            int var10 = var8.getHeight();
            int var7 = 4 + (var9 != null && var9.getHeight() > var10 ? var9.getHeight() : var10);
            var1.setFont(var8);
            String var11 = this.field_6[var6];
            if (var6 == this.field_10 + this.field_11 - 1) {
               int var12 = 11141120;
               int var13 = 102 / var7 << 16;

               for (int var14 = var4 + var7; var14 > var4; var14--) {
                  var1.setColor(var12 -= var13);
                  var1.drawLine(0, var14, var2, var14);
               }

               var1.setColor(16777215);
               if (15 + (var9 == null ? 0 : var9.getWidth()) + field_20.stringWidth(var11) > var2) {
                  if (!this.field_30) {
                     this.field_29 = 0;
                     this.field_30 = true;
                     new RList(this).start();
                  } else if (this.field_29 < var11.length()
                     && 20 + (var9 == null ? 0 : var9.getWidth()) + field_20.stringWidth(var11.substring(this.field_29)) > var2) {
                     var11 = var11.substring(++this.field_29);
                  } else {
                     this.field_29 = 0;
                  }
               } else {
                  this.field_30 = false;
               }
            }

            int var33 = var4 + (var7 - var8.getHeight()) / 2;
            if (this.field_14 == 3) {
               if (var9 != null) {
                  var1.drawImage(var9, 2, var4 + 2, 20);
               }

               var1.drawString(var11, 3 + (var9 == null ? 0 : var9.getWidth()), var33, 20);
            } else if (this.field_14 == 1) {
               var1.drawArc(2, var33, var10 - 2, var10 - 2, 0, 360);
               if (this.field_16 == var6) {
                  var1.fillArc(7, var33 + 5, var10 - 10, var10 - 10, 0, 360);
               }

               if (var9 != null) {
                  var1.drawImage(var9, var10 + 2, var4 + 2, 20);
               }

               var1.drawString(var11, var10 + 5 + (var9 == null ? 0 : var9.getWidth()), var4 + (var7 - var8.getHeight()) / 2, 20);
            } else {
               var1.drawRect(2, var33, var10, var10);
               if (this.field_9[var6]) {
                  var1.setColor(65280);
                  var1.drawLine(3, var33 + var10 / 2, var10 / 2 + 1, var33 + var10 - 1);
                  var1.drawLine(var10 + 2, var33 + 2, var10 / 2 + 1, var33 + var10 - 1);
               }

               if (var9 != null) {
                  var1.drawImage(var9, var7 + 2, var4 + 2, 20);
               }

               var1.setColor(16777215);
               var1.drawString(var11, var7 + 5 + (var9 == null ? 0 : var9.getWidth()), var4 + (var7 - var8.getHeight()) / 2, 20);
            }

            var4 += var7;
         }
      }

      int var18 = var3 - this.field_24;
      if (!this.field_25) {
         int var19 = 0;
         int var22;
         int var23 = (var22 = 170 / this.field_24 + 1) | var22 << 8 | var22 << 16;

         for (int var26 = this.field_24; var26 >= 0; var26--) {
            var1.setColor(var19 += var23);
            var1.drawLine(0, var26, var2, var26);
         }

         int var20 = 0;

         for (int var27 = var18; var27 <= var3; var27++) {
            var1.setColor(var20 += var23);
            var1.drawLine(0, var27, var2, var27);
         }

         var18 += 2;
         var1.setColor(16777113);
         var1.drawString(this.field_23, var2 >> 1, 2, 17);
         String var10001;
         if (this.field_26) {
            var10001 = this.getSelectedIndex() + 1 + "/" + this.field_12;
         } else {
            field_28.setTime(System.currentTimeMillis());
            field_27.setTime(field_28);
            StringBuffer var15 = new StringBuffer();
            if ((var5 = field_27.get(11)) < 10) {
               var15.append('0');
            }

            var15.append(var5).append(':');
            if ((var5 = field_27.get(12)) < 10) {
               var15.append('0');
            }

            var10001 = var15.append(var5).toString();
         }

         var1.drawString(var10001, var2 >> 1, var18, 17);
      }

      int var21;
      if ((var21 = this.field_18.size()) > 0) {
         if (!this.field_22) {
            if (!this.field_25) {
               if (var21 > 1) {
                  var1.drawString(var21 > 2 ? "Tùy chọn" : ((Command)this.field_18.elementAt(1)).getLabel(), 2, var18, 20);
               }

               var1.drawString(((Command)this.field_18.elementAt(0)).getLabel(), var2 - 2, var18, 24);
               return;
            }
         } else {
            if (var21 > 1) {
               var1.drawString("Chọn", 2, var18, 20);
               var1.drawString("Hủy", var2 - 2, var18, 24);
            }

            int var24 = 0;

            for (int var28 = 1; var28 < this.field_18.size(); var28++) {
               if (field_20.stringWidth(((Command)this.field_18.elementAt(var28)).getLabel()) > var24) {
                  var24 = field_20.stringWidth(((Command)this.field_18.elementAt(var28)).getLabel());
               }
            }

            int var25 = var24 + 4;
            int var29 = this.field_18.size() - 1;
            int var30 = (this.field_24 - 2) * var29 + 3;
            int var31 = var3 - this.field_24 - var30 - 1;
            var1.setColor(16777215);
            var1.drawRect(0, var31, var25, var30);
            var1.setColor(16514047);
            var1.fillRect(1, var31 + 1, var25 - 1, var30 - 1);
            var1.setColor(0);
            int var32 = var31 + var30;

            for (int var34 = 1; var34 <= var29; var34++) {
               var1.drawString(((Command)this.field_18.elementAt(var34)).getLabel(), 2, var32 - (this.field_24 - 2) * var34, 20);
            }

            var1.setColor(0);
            var1.fillRect(1, var32 - 1 - (this.field_24 - 2) * (this.field_15 + 1), var25 - 1, this.field_24 - 1);
            var1.setColor(16514047);
            var1.drawString(((Command)this.field_18.elementAt(this.field_15 + 1)).getLabel(), 2, var32 - (this.field_24 - 2) * (this.field_15 + 1), 20);
         }
      }
   }

   public void setTitle(String var1) {
      if (var1 != null) {
         this.field_23 = var1;
      }
   }
}
