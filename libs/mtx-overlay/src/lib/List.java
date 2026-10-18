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
   private String[] itemStrings;
   private Image[] itemImages;
   private Font[] itemFonts;
   private boolean[] itemSelectedFlags;
   private int topVisibleIndex;
   private int cursorRow;
   private int itemCount;
   private int fitPolicy;
   private int choiceType;
   private int activeCommandIndex;
   private int implicitSelectedIndex;
   private CommandListener commandListener;
   private Vector commandVector;
   public static final Command SELECT_COMMAND = javax.microedition.lcdui.List.SELECT_COMMAND;
   private Command selectCommand;
   private static Font defaultFont = Font.getFont(0, 0, 8);
   private int visibleRowCount;
   private boolean showingCommandsMenu;
   private String listTitle;
   private int itemHeight;
   private boolean hideTitleBar;
   boolean scrollTimerActive;
   private static final Calendar calendarInstance = Calendar.getInstance();
   private static final Date dateInstance = new Date();
   private int textScrollOffset;
   boolean scrollThreadRunning;

   public List(String var1, int var2) {
      super.setFullScreenMode(true);
      this.initList(var1, var2);
      this.itemStrings = new String[20];
      this.itemFonts = new Font[20];
      this.itemImages = new Image[20];
      if (var2 == 2) {
         this.itemSelectedFlags = new boolean[20];
      }

      this.itemCount = 0;
   }
   private void initList(String var1, int var2) {
      if (var2 != 1 && var2 != 2 && var2 != 3) {
         throw new IllegalArgumentException();
      } else {
         this.choiceType = var2;
         this.itemHeight = defaultFont.getHeight() + 4;
         this.listTitle = var1 == null ? "" : var1;
         this.setTitle(var1);
         this.setFullScreenMode(true);
         this.selectCommand = SELECT_COMMAND;
         this.commandVector = new Vector();
         this.visibleRowCount = 1;
         this.cursorRow = 1;
         this.hideTitleBar = false;
      }
   }

   public List(String var1, int var2, String[] var3, Image[] var4) {
      this.initList(var1, var2);
      if (var3 == null) {
         throw new NullPointerException();
      } else {
         this.itemStrings = var3;
         this.itemCount = var3.length;
         if (var4 == null) {
            this.itemImages = new Image[this.itemCount];
         } else {
            if (var4.length != this.itemCount) {
               throw new IllegalArgumentException();
            }

            this.itemImages = var4;
         }

         this.itemFonts = new Font[this.itemCount];

         for (int var5 = var3.length - 1; var5 >= 0; var5--) {
            this.itemFonts[var5] = defaultFont;
         }

         if (var2 == 2) {
            this.itemSelectedFlags = new boolean[this.itemCount];
         }
      }
   }

   public int append(String var1, Image var2) {
      if (var1 != null) {
         if (this.itemCount == this.itemStrings.length - 1) {
            this.growItemArrays();
         }

         this.itemStrings[this.itemCount] = var1;
         this.itemImages[this.itemCount] = var2;
         this.itemFonts[this.itemCount] = defaultFont;
         if (this.choiceType == 2) {
            this.itemSelectedFlags[this.itemCount] = false;
         }

         this.repaint();
         return this.itemCount++;
      } else {
         return -1;
      }
   }

   public void delete(int var1) {
      if (var1 < this.itemCount && var1 >= 0) {
         System.arraycopy(this.itemStrings, var1 + 1, this.itemStrings, var1, this.itemCount - var1 - 1);
         System.arraycopy(this.itemImages, var1 + 1, this.itemImages, var1, this.itemCount - var1 - 1);
         System.arraycopy(this.itemFonts, var1 + 1, this.itemFonts, var1, this.itemCount - var1 - 1);
         if (this.choiceType == 2) {
            System.arraycopy(this.itemSelectedFlags, var1 + 1, this.itemSelectedFlags, var1, this.itemCount - var1 - 1);
         }
      }

      this.itemCount--;
      if (this.topVisibleIndex + this.cursorRow > this.itemCount) {
         this.moveCursorUp();
      }

      this.repaint();
   }

   public void deleteAll() {
      this.itemCount = 0;
      this.topVisibleIndex = 0;
      this.cursorRow = 1;
   }

   public int getFitPolicy() {
      return this.fitPolicy;
   }

   public Font getFont(int var1) {
      return this.itemFonts[var1];
   }

   public Image getImage(int var1) {
      return this.itemImages[var1];
   }

   public int getSelectedFlags(boolean[] var1) {
      if (this.choiceType == 2) {
         int var2 = 0;

         for (int var3 = 0; var3 < this.itemCount; var3++) {
            if (var1[var3] = this.itemSelectedFlags[var3]) {
               var2++;
            }
         }

         return var2;
      } else {
         var1[this.topVisibleIndex + this.cursorRow] = true;
         return 1;
      }
   }

   public int getSelectedIndex() {
      if (this.choiceType == 3) {
         return this.topVisibleIndex + this.cursorRow - 1;
      } else {
         return this.choiceType == 1 ? this.implicitSelectedIndex : -1;
      }
   }

   public String getString(int var1) {
      return var1 >= 0 && var1 < this.itemCount ? this.itemStrings[var1] : null;
   }

   public void insert(int var1, String var2, Image var3) {
      if (var1 >= this.itemCount) {
         this.append(var2, var3);
      } else {
         if (var1 < 0) {
            var1 = 0;
         }

         if (this.itemCount == this.itemStrings.length - 1) {
            this.growItemArrays();
         }

         for (int var4 = this.itemCount; var4 > var1; var4--) {
            this.itemStrings[var4] = this.itemStrings[var4 - 1];
            this.itemFonts[var4] = this.itemFonts[var4 - 1];
            this.itemImages[var4] = this.itemImages[var4 - 1];
            if (this.choiceType == 2) {
               this.itemSelectedFlags[var4] = this.itemSelectedFlags[var4 - 1];
            }
         }

         this.itemCount++;
         this.itemStrings[var1] = var2;
         this.itemFonts[var1] = defaultFont;
         this.itemImages[var1] = var3;
         if (this.choiceType == 2) {
            this.itemSelectedFlags[var1] = false;
         }
      }

      this.repaint();
   }

   public boolean isSelected(int var1) {
      return this.choiceType == 2 ? this.itemSelectedFlags[var1] : this.getSelectedIndex() == var1;
   }

   public void removeCommand(Command var1) {
      if (var1 == this.selectCommand) {
         this.selectCommand = null;
      }

      this.commandVector.removeElement(var1);
   }

   public void set(int var1, String var2, Image var3) {
      if (var1 >= 0 && var1 < this.itemCount && var2 != null) {
         this.itemStrings[var1] = var2;
         this.itemImages[var1] = var3;
         this.repaint();
      }
   }

   public void setFitPolicy(int var1) {
      if (var1 != 0 && var1 != 2 && var1 != 1) {
         throw new IllegalArgumentException();
      } else {
         this.fitPolicy = var1;
      }
   }

   public void setFont(int var1, Font var2) {
      if (var1 >= 0 && var1 < this.itemCount && var2 != null) {
         this.itemFonts[var1] = var2;
         this.repaint();
      }
   }

   public void setSelectCommand(Command var1) {
      if (this.choiceType != 2) {
         this.selectCommand = var1;
      }
   }

   public void setSelectedFlags(boolean[] var1) {
      if (this.choiceType == 2) {
         System.arraycopy(var1, 0, this.itemSelectedFlags, 0, this.itemCount);
      }
   }

   public void setSelectedIndex(int var1, boolean var2) {
      if (var1 < this.itemCount) {
         if (this.choiceType == 2 && var2) {
            this.itemSelectedFlags[var1] = var2;
         } else if (this.choiceType == 1) {
            this.implicitSelectedIndex = var1;
         } else {
            this.cursorRow = 1;
            this.topVisibleIndex = var1;
         }
      }

      this.repaint();
   }

   public int size() {
      return this.itemCount;
   }

   public void addCommand(Command var1) {
      if (var1 != null && !this.commandVector.contains(var1)) {
         int var2 = var1.getCommandType();
         if (this.commandVector.size() <= 0) {
            this.commandVector.addElement(var1);
         } else {
            int var3 = 0;

            while (true) {
               if (var3 >= this.commandVector.size()) {
                  this.commandVector.addElement(var1);
                  break;
               }

               Command var4 = (Command)this.commandVector.elementAt(var3);
               if (var2 == var4.getCommandType()) {
                  if (var1.getPriority() > var4.getPriority()) {
                     this.commandVector.insertElementAt(var1, var3);
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
                     this.commandVector.insertElementAt(var1, var3);
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
      this.commandListener = var1;
   }
   private void growItemArrays() {
      int var1;
      String[] var2 = new String[var1 = (this.itemStrings.length << 1) + 10];
      System.arraycopy(this.itemStrings, 0, var2, 0, this.itemCount);
      this.itemStrings = var2;
      Font[] var4 = new Font[var1];
      System.arraycopy(this.itemFonts, 0, var4, 0, this.itemCount);
      this.itemFonts = var4;
      Image[] var5 = new Image[var1];
      System.arraycopy(this.itemImages, 0, var5, 0, this.itemCount);
      this.itemImages = var5;
      if (this.choiceType == 2) {
         boolean[] var3 = new boolean[var1];
         System.arraycopy(this.itemSelectedFlags, 0, var3, 0, this.itemCount);
         this.itemSelectedFlags = var3;
      }
   }

   public void keyRepeated(int var1) {
      this.keyPressed(var1);
   }
   private void triggerScrollTimer() {
      if (!this.scrollTimerActive) {
         this.scrollTimerActive = true;
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
                     if (!this.hideTitleBar) {
                        if (this.showingCommandsMenu) {
                           this.showingCommandsMenu = false;
                        } else if (this.commandListener != null && this.commandVector.size() > 0) {
                           this.commandListener.commandAction((Command)this.commandVector.elementAt(0), this);
                        }
                        break label159;
                     }

                     var10000 = this;
                     break;
                  case -6:
                     if (!this.hideTitleBar) {
                        if (this.showingCommandsMenu) {
                           this.showingCommandsMenu = false;
                           this.repaint();
                           if (this.commandListener != null) {
                              this.commandListener.commandAction((Command)this.commandVector.elementAt(this.activeCommandIndex + 1), this);
                           }
                        } else if (this.commandVector.size() > 2) {
                           this.showingCommandsMenu = true;
                        } else if (this.commandListener != null && this.commandVector.size() > 1) {
                           this.commandListener.commandAction((Command)this.commandVector.elementAt(1), this);
                        }
                        break label159;
                     }

                     var10000 = this;
                     break;
                  case -5:
                     if (this.showingCommandsMenu) {
                        this.keyPressed(-6);
                        return;
                     }

                     if (this.choiceType == 3 && this.commandListener != null && this.selectCommand != null) {
                        this.commandListener.commandAction(this.selectCommand, this);
                     } else {
                        var1 = this.topVisibleIndex + this.cursorRow - 1;
                        if (this.choiceType == 2) {
                           this.itemSelectedFlags[var1] = !this.itemSelectedFlags[var1];
                        } else if (this.choiceType == 1) {
                           if (this.implicitSelectedIndex == var1 && this.commandListener != null && this.selectCommand != null) {
                              this.commandListener.commandAction(this.selectCommand, this);
                           } else {
                              this.implicitSelectedIndex = var1;
                           }
                        }
                     }
                     break label159;
                  case -4:
                     if (!this.showingCommandsMenu) {
                        if (this.itemCount <= this.visibleRowCount) {
                           this.topVisibleIndex = 0;
                           this.cursorRow = this.cursorRow == 1 ? this.itemCount : 1;
                        } else if (this.visibleRowCount + this.topVisibleIndex + this.cursorRow <= this.itemCount) {
                           this.topVisibleIndex = this.topVisibleIndex + this.visibleRowCount;
                        } else if (this.itemCount - this.topVisibleIndex <= this.visibleRowCount) {
                           this.topVisibleIndex = 0;
                           this.cursorRow = 1;
                        } else {
                           this.topVisibleIndex = this.topVisibleIndex + this.visibleRowCount;
                           this.cursorRow = this.itemCount - this.topVisibleIndex;
                        }

                        this.triggerScrollTimer();
                     }
                     break label159;
                  case -3:
                     if (!this.showingCommandsMenu) {
                        if (this.itemCount <= this.visibleRowCount) {
                           this.topVisibleIndex = 0;
                           this.cursorRow = this.cursorRow == 1 ? this.itemCount : 1;
                        } else if (this.topVisibleIndex >= this.visibleRowCount) {
                           this.topVisibleIndex = this.topVisibleIndex - this.visibleRowCount;
                        } else {
                           if (this.topVisibleIndex > 0) {
                              this.topVisibleIndex = 0;
                           } else if (this.itemCount > 0) {
                              this.topVisibleIndex = Math.max(0, this.itemCount - this.visibleRowCount);
                           }

                           this.cursorRow = 1;
                        }

                        this.triggerScrollTimer();
                     }
                     break label159;
                  case -2:
                     if (!this.showingCommandsMenu) {
                        if (this.cursorRow < this.visibleRowCount) {
                           if (this.cursorRow + this.topVisibleIndex < this.itemCount) {
                              this.cursorRow++;
                           } else {
                              this.topVisibleIndex = 0;
                              this.cursorRow = 1;
                           }
                        } else if (this.cursorRow + this.topVisibleIndex < this.itemCount) {
                           this.topVisibleIndex++;
                        } else {
                           this.topVisibleIndex = 0;
                           this.cursorRow = 1;
                        }

                        this.triggerScrollTimer();
                     } else {
                        this.activeCommandIndex--;
                        if (this.activeCommandIndex < 0) {
                           this.activeCommandIndex = this.commandVector.size() - 2;
                        }
                     }
                     break label159;
                  case -1:
                     if (!this.showingCommandsMenu) {
                        this.moveCursorUp();
                        this.triggerScrollTimer();
                     } else {
                        this.activeCommandIndex++;
                        if (this.activeCommandIndex > this.commandVector.size() - 2) {
                           this.activeCommandIndex = 0;
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
                     if (!this.hideTitleBar) {
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
                     if (!this.showingCommandsMenu && var2 + this.topVisibleIndex <= this.itemCount) {
                        this.cursorRow = var2;
                        this.repaint();
                        if (this.choiceType == 3 && this.commandListener != null && this.selectCommand != null) {
                           this.commandListener.commandAction(this.selectCommand, this);
                        }
                     }
                     break label158;
               }

               var10001 = false;
            }

            var10000.hideTitleBar = var10001;
            break label159;
         }

         Display.mc.pause();
      }

      this.repaint();
   }
   private void moveCursorUp() {
      if (this.cursorRow > 1) {
         this.cursorRow--;
      } else if (this.topVisibleIndex > 0) {
         this.topVisibleIndex--;
      } else {
         if (this.itemCount > 0) {
            this.topVisibleIndex = Math.max(0, this.itemCount - this.visibleRowCount);
            this.cursorRow = this.itemCount - this.topVisibleIndex;
         }
      }
   }

   protected void paint(Graphics var1) {
      int var2 = super.getWidth();
      int var3 = super.getHeight();
      var1.setColor(0);
      var1.fillRect(0, 0, var2, var3);
      var1.setColor(16777215);
      var1.setFont(defaultFont);
      int var4 = (this.hideTitleBar ? 0 : this.itemHeight) + 1;
      int var5 = this.hideTitleBar ? var3 - 4 : var3 - (defaultFont.getHeight() << 1) + 4;
      if (this.itemCount > 0) {
         int var6 = this.topVisibleIndex;

         for (this.visibleRowCount = 0; var6 < this.itemCount && var4 < var5; var6++) {
            this.visibleRowCount++;
            Font var8 = this.itemFonts[var6];
            Image var9 = this.itemImages[var6];
            int var10 = var8.getHeight();
            int var7 = 4 + (var9 != null && var9.getHeight() > var10 ? var9.getHeight() : var10);
            var1.setFont(var8);
            String var11 = this.itemStrings[var6];
            if (var6 == this.topVisibleIndex + this.cursorRow - 1) {
               int var12 = 11141120;
               int var13 = 102 / var7 << 16;

               for (int var14 = var4 + var7; var14 > var4; var14--) {
                  var1.setColor(var12 -= var13);
                  var1.drawLine(0, var14, var2, var14);
               }

               var1.setColor(16777215);
               if (15 + (var9 == null ? 0 : var9.getWidth()) + defaultFont.stringWidth(var11) > var2) {
                  if (!this.scrollThreadRunning) {
                     this.textScrollOffset = 0;
                     this.scrollThreadRunning = true;
                     new RList(this).start();
                  } else if (this.textScrollOffset < var11.length()
                     && 20 + (var9 == null ? 0 : var9.getWidth()) + defaultFont.stringWidth(var11.substring(this.textScrollOffset)) > var2) {
                     var11 = var11.substring(++this.textScrollOffset);
                  } else {
                     this.textScrollOffset = 0;
                  }
               } else {
                  this.scrollThreadRunning = false;
               }
            }

            int var33 = var4 + (var7 - var8.getHeight()) / 2;
            if (this.choiceType == 3) {
               if (var9 != null) {
                  var1.drawImage(var9, 2, var4 + 2, 20);
               }

               var1.drawString(var11, 3 + (var9 == null ? 0 : var9.getWidth()), var33, 20);
            } else if (this.choiceType == 1) {
               var1.drawArc(2, var33, var10 - 2, var10 - 2, 0, 360);
               if (this.implicitSelectedIndex == var6) {
                  var1.fillArc(7, var33 + 5, var10 - 10, var10 - 10, 0, 360);
               }

               if (var9 != null) {
                  var1.drawImage(var9, var10 + 2, var4 + 2, 20);
               }

               var1.drawString(var11, var10 + 5 + (var9 == null ? 0 : var9.getWidth()), var4 + (var7 - var8.getHeight()) / 2, 20);
            } else {
               var1.drawRect(2, var33, var10, var10);
               if (this.itemSelectedFlags[var6]) {
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

      int var18 = var3 - this.itemHeight;
      if (!this.hideTitleBar) {
         int var19 = 0;
         int var22;
         int var23 = (var22 = 170 / this.itemHeight + 1) | var22 << 8 | var22 << 16;

         for (int var26 = this.itemHeight; var26 >= 0; var26--) {
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
         var1.drawString(this.listTitle, var2 >> 1, 2, 17);
         String var10001;
         if (this.scrollTimerActive) {
            var10001 = this.getSelectedIndex() + 1 + "/" + this.itemCount;
         } else {
            dateInstance.setTime(System.currentTimeMillis());
            calendarInstance.setTime(dateInstance);
            StringBuffer var15 = new StringBuffer();
            if ((var5 = calendarInstance.get(11)) < 10) {
               var15.append('0');
            }

            var15.append(var5).append(':');
            if ((var5 = calendarInstance.get(12)) < 10) {
               var15.append('0');
            }

            var10001 = var15.append(var5).toString();
         }

         var1.drawString(var10001, var2 >> 1, var18, 17);
      }

      int var21;
      if ((var21 = this.commandVector.size()) > 0) {
         if (!this.showingCommandsMenu) {
            if (!this.hideTitleBar) {
               if (var21 > 1) {
                  var1.drawString(var21 > 2 ? "Options" : ((Command)this.commandVector.elementAt(1)).getLabel(), 2, var18, 20);
               }

               var1.drawString(((Command)this.commandVector.elementAt(0)).getLabel(), var2 - 2, var18, 24);
               return;
            }
         } else {
            if (var21 > 1) {
               var1.drawString("Select", 2, var18, 20);
               var1.drawString("Cancel", var2 - 2, var18, 24);
            }

            int var24 = 0;

            for (int var28 = 1; var28 < this.commandVector.size(); var28++) {
               if (defaultFont.stringWidth(((Command)this.commandVector.elementAt(var28)).getLabel()) > var24) {
                  var24 = defaultFont.stringWidth(((Command)this.commandVector.elementAt(var28)).getLabel());
               }
            }

            int var25 = var24 + 4;
            int var29 = this.commandVector.size() - 1;
            int var30 = (this.itemHeight - 2) * var29 + 3;
            int var31 = var3 - this.itemHeight - var30 - 1;
            var1.setColor(16777215);
            var1.drawRect(0, var31, var25, var30);
            var1.setColor(16514047);
            var1.fillRect(1, var31 + 1, var25 - 1, var30 - 1);
            var1.setColor(0);
            int var32 = var31 + var30;

            for (int var34 = 1; var34 <= var29; var34++) {
               var1.drawString(((Command)this.commandVector.elementAt(var34)).getLabel(), 2, var32 - (this.itemHeight - 2) * var34, 20);
            }

            var1.setColor(0);
            var1.fillRect(1, var32 - 1 - (this.itemHeight - 2) * (this.activeCommandIndex + 1), var25 - 1, this.itemHeight - 1);
            var1.setColor(16514047);
            var1.drawString(((Command)this.commandVector.elementAt(this.activeCommandIndex + 1)).getLabel(), 2, var32 - (this.itemHeight - 2) * (this.activeCommandIndex + 1), 20);
         }
      }
   }

   public void setTitle(String var1) {
      if (var1 != null) {
         this.listTitle = var1;
      }
   }
}
