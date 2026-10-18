package MrQuyet;

import com.nokia.mid.ui.DeviceControl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import lib.Canvas;
import lib.MidControl;

public class MrQuyet extends MIDlet implements Runnable, CommandListener, ItemStateListener, MidControl {
   // $VF: renamed from: a javax.microedition.lcdui.List
   private static List field_31;
   // $VF: renamed from: a javax.microedition.lcdui.Command
   private static final Command field_32 = new Command("Phục hồi RAM", 4, 11);
   // $VF: renamed from: b javax.microedition.lcdui.Command
   private static final Command field_33 = new Command("Phục hồi (ON)", 4, 27);
   // $VF: renamed from: c javax.microedition.lcdui.Command
   private static final Command field_34 = new Command("Hủy", 4, 27);
   // $VF: renamed from: d javax.microedition.lcdui.Command
   private static final Command field_35 = new Command("Quay lại", 2, 10);
   // $VF: renamed from: e javax.microedition.lcdui.Command
   private static final Command field_36 = new Command("Chế độ ngủ (ON)", 4, 25);
   // $VF: renamed from: f javax.microedition.lcdui.Command
   private static final Command field_37 = new Command("Chế độ ngủ (OFF)", 4, 25);
   // $VF: renamed from: g javax.microedition.lcdui.Command
   private static final Command field_38 = new Command("Quay lại", 2, 26);
   // $VF: renamed from: h javax.microedition.lcdui.Command
   private static final Command field_39 = new Command("Lưu", 1, 21);
   // $VF: renamed from: i javax.microedition.lcdui.Command
   private static final Command field_40 = new Command("Xóa", 1, 19);
   // $VF: renamed from: j javax.microedition.lcdui.Command
   private static final Command field_41 = new Command("Sửa", 1, 20);
   // $VF: renamed from: k javax.microedition.lcdui.Command
   private static final Command field_42 = new Command("Thêm mới", 1, 18);
   // $VF: renamed from: a int
   private int field_43;
   // $VF: renamed from: a java.lang.Throwable
   private static Throwable field_44;
   // $VF: renamed from: a javax.microedition.lcdui.Displayable
   private static Displayable field_45;
   // $VF: renamed from: b javax.microedition.lcdui.Displayable
   private static Displayable field_46;
   // $VF: renamed from: a javax.microedition.lcdui.Alert
   private static Alert field_47;
   // $VF: renamed from: a long
   private static long field_48 = 0L;
   // $VF: renamed from: b int
   private static int field_49 = 30;
   // $VF: renamed from: a javax.microedition.lcdui.Display
   private static Display field_50;
   // $VF: renamed from: c javax.microedition.lcdui.Displayable
   private static Displayable field_51;
   // $VF: renamed from: b javax.microedition.lcdui.List
   private static List field_52;
   // $VF: renamed from: now int
   public static int field_53;
   // $VF: renamed from: a lib.MIDlet[]
   private static lib.MIDlet[] field_54;
   // $VF: renamed from: a MrQuyet.MidletInfo[]
   private static MidletInfo[] field_55 = new MidletInfo[30];
   public static final PrintStream debugout = System.out;
   // $VF: renamed from: map java.util.Hashtable
   public static final Hashtable field_56 = new Hashtable();
   // $VF: renamed from: a java.lang.Thread
   private Thread field_57;
   // $VF: renamed from: b java.lang.Thread
   private Thread field_58;
   // $VF: renamed from: a java.lang.String
   private static String field_59;
   // $VF: renamed from: b java.lang.String
   private static String field_60;
   // $VF: renamed from: c java.lang.String
   private static String field_61 = "/MrQuyet/MrQuyet.png";
   // $VF: renamed from: b long
   private static long field_62 = 300000L;
   // $VF: renamed from: c long
   private static long field_63 = 5000L;
   // $VF: renamed from: a boolean
   private boolean field_64 = true;
   // $VF: renamed from: c int
   private static int field_65;
   // $VF: renamed from: d int
   private static int field_66 = 100;

   // $VF: renamed from: a (java.lang.String) javax.microedition.lcdui.List
   private List method_6(String var1) {
      if (field_31 == null) {
         (field_31 = new List(var1, 3)).setCommandListener(this);
      } else {
         String var2 = field_31.getTitle();
         field_31.deleteAll();
         if (var2 == "Thư mục lưu") {
            field_31.removeCommand(field_42);
            field_31.removeCommand(field_41);
            field_31.removeCommand(field_40);
            field_31.removeCommand(field_39);
            field_31.removeCommand(field_38);
         } else {
            field_31.removeCommand(field_32);
            field_31.removeCommand(field_33);
            field_31.removeCommand(field_34);
            field_31.removeCommand(field_35);
            field_31.removeCommand(field_36);
            field_31.removeCommand(field_37);
         }

         field_31.setTitle(var1);
      }

      return field_31;
   }

   // $VF: renamed from: a () void
   private void method_7() {
      field_45 = field_50.getCurrent();
      List var1;
      (var1 = this.method_6("Thư mục lưu")).addCommand(field_42);
      var1.addCommand(field_41);
      var1.setSelectCommand(field_41);
      var1.addCommand(field_40);
      var1.addCommand(field_39);
      var1.addCommand(field_38);
      Enumeration var2 = field_56.keys();

      while (var2.hasMoreElements()) {
         var1.append((String)var2.nextElement(), null);
      }

      var1.setCommandListener(this);
      field_50.setCurrent(var1);
   }

   // $VF: renamed from: b () void
   private void method_8() {
      List var1;
      (var1 = this.method_6("Bộ nhớ điện thoại")).addCommand(field_32);
      var1.addCommand(this.field_58 == null ? field_33 : field_34);
      var1.addCommand(field_35);
      var1.addCommand(this.field_57 == null ? field_36 : field_37);
      var1.setFitPolicy(1);
      Runtime var2 = Runtime.getRuntime();
      var1.append("RAM:" + var2.totalMemory() / 1024L + " k", null);
      var1.append("RAM trống:" + var2.freeMemory() / 1024L + " k", null);
      var1.append("Mạch:" + Thread.activeCount(), null);
      if (field_51 != null) {
         var1.append("Hiển thị : ", null);
         var1.append(field_51.toString(), null);
      }

      if (field_44 != null) {
         var1.append("Exception : ", null);
         var1.append(field_44.toString(), null);
      }

      field_50.setCurrent(var1);
   }

   // $VF: renamed from: a (java.lang.String, java.lang.String) void
   private void method_9(String var1, String var2) {
      field_46 = field_50.getCurrent();
      Form var3;
      (var3 = new Form("Sửa")).append(new TextField("Mặc định:", var1, 512, 0));
      var3.append(new TextField("Mới:", var2, 512, 0));
      var3.setCommandListener(this);
      var3.addCommand(new Command("Xác nhận", 4, 22));
      var3.addCommand(new Command("Hủy", 2, 23));
      field_50.setCurrent(var3);
   }

   // $VF: renamed from: c () void
   private static void method_10() {
      Hashtable var0 = field_56;

      try {
         RecordStore var1 = RecordStore.openRecordStore("MrQuyet_rms_urlmap", true);
         if (field_56.isEmpty() && var1.getNumRecords() > 0) {
            var1.setRecord(1, new byte[0], 0, 0);
            var1.closeRecordStore();
         } else {
            ByteArrayOutputStream var2 = new ByteArrayOutputStream();
            DataOutputStream var3 = new DataOutputStream(var2);
            Enumeration var4 = var0.keys();

            while (var4.hasMoreElements()) {
               String var5 = (String)var4.nextElement();
               var3.writeUTF(var5);
               var3.writeUTF((String)var0.get(var5));
            }

            var3.flush();
            var3.close();
            byte[] var7 = var2.toByteArray();
            if (var1.getNumRecords() > 0) {
               var1.setRecord(1, var7, 0, var7.length);
            } else {
               var1.addRecord(var7, 0, var7.length);
            }

            var1.closeRecordStore();
         }
      } catch (Exception var6) {
      }
   }

   // $VF: renamed from: a (javax.microedition.lcdui.Form, int) java.lang.String
   private static String method_11(Form var0, int var1) {
      return ((TextField)var0.get(var1)).getString();
   }

   // $VF: renamed from: a (java.lang.String, int) int
   private static int method_12(String var0, int var1) {
      try {
         return Integer.parseInt(var0);
      } catch (Exception var2) {
         return var1;
      }
   }

   // $VF: renamed from: a (java.lang.String, long) long
   private static long method_13(String var0, long var1) {
      try {
         return Long.parseLong(var0);
      } catch (Exception var3) {
         return var1;
      }
   }

   // $VF: renamed from: d () void
   private void method_14() {
      List var1 = field_52 = new List("AngelChip Menu", 3);
      if (field_60 == null) {
         field_60 = "file:///c/" + this.getAppProperty("MIDlet-Name") + ".db";
      }

      String var3;
      for (int var2 = 0; (var3 = this.getAppProperty(String.valueOf(var2))) != null; var2++) {
         MidletInfo var4 = MidletInfo.getInstance(var3);
         field_55[var2] = var4;
         var1.append(var4.name, var4.icon);
      }

      field_54 = new lib.MIDlet[field_52.size()];
      var1.addCommand(new Command("Chạy ứng dụng", 1, 0));
      var1.addCommand(new Command("Dừng ứng dụng", 1, 1));
      var1.addCommand(new Command("Thu nhỏ", 1, 5));
      var1.addCommand(new Command("Chỉnh độ sáng", 1, 8));
      var1.addCommand(new Command("Tùy chọn RAM", 1, 4));
      var1.addCommand(new Command("Cài đặt cơ bản", 1, 3));
      var1.addCommand(new Command("Thoát", 7, 2));
      var1.setCommandListener(this);
      field_50.setCurrent(field_52);
      if (field_61 != null) {
         method_18();
      }
   }

   // $VF: renamed from: run () void
   public void run() {
      if (field_65 >= 0) {
         try {
            lib.MIDlet var2;
            MidletInfo var7;
            if ((var2 = (var7 = field_55[field_65]).newInstance()) != null) {
               field_54[field_65] = var2;
               field_52.set(field_65, var7.name, MidletInfo.runimg(var7.icon));
               var2.start();
            }
         } catch (Throwable var3) {
            this.showError(var3, this);
         }

         this.field_64 = true;
      } else if (field_65 == -1) {
         try {
            Thread.sleep(field_62);

            for (int var1 = 0; var1 < field_54.length; var1++) {
               if (field_54[var1] != null) {
                  try {
                     field_54[var1].notifyDestroyed();
                  } catch (Throwable var4) {
                  }
               }
            }

            this.notifyDestroyed();
         } catch (Exception var5) {
         }
      } else {
         if (field_63 > 0L) {
            while (true) {
               try {
                  Thread.sleep(field_63);
                  System.gc();
               } catch (Exception var6) {
                  break;
               }
            }
         }
      }
   }

   public void showError(Throwable var1, Object var2) {
      field_44 = var1;
      showError(var2 + ": " + var1.toString());
   }

   public static void showError(String var0) {
      if (System.currentTimeMillis() - field_48 > 1500L) {
         field_48 = System.currentTimeMillis();
         field_47.setTimeout(-2);
         field_47.setString(var0);
         field_50.setCurrent(field_47, (Displayable)(field_50.getCurrent() instanceof Alert ? field_52 : field_50.getCurrent()));
      }
   }

   // $VF: renamed from: e () void
   private static void method_15() {
      FileConnection var0 = null;

      try {
         var0 = (FileConnection)Connector.open(field_60, 2);

         try {
            var0.truncate(0L);
         } catch (Exception var19) {
            var0.create();
         }
      } catch (Throwable var20) {
         lib.Display.mc.showError(var20, lib.Display.mc);
      }

      DataOutputStream var2;
      try {
         var2 = var0.openDataOutputStream();
      } catch (Exception var18) {
         lib.Display.mc.showError(var18, lib.Display.mc);
         return;
      }

      try {
         String[] var3;
         int var4;
         if ((var4 = (var3 = RecordStore.listRecordStores()).length) > 0) {
            for (int var5 = 0; var5 < var4; var5++) {
               String var1 = var3[var5];
               var2.writeUTF(var1);
               RecordStore var6;
               RecordEnumeration var23 = (var6 = RecordStore.openRecordStore(var1, false)).enumerateRecords(null, null, false);

               while (var23.hasNextElement()) {
                  int var7 = var23.nextRecordId();
                  byte[] var8 = var6.getRecord(var7);
                  var2.writeInt(var7);
                  if (var8 != null && var8.length > 0) {
                     var2.writeInt(var8.length);
                     var2.write(var8);
                     var2.flush();
                  } else {
                     var2.writeInt(0);
                  }
               }

               var2.writeInt(0);
               var6.closeRecordStore();
            }
         }

         showError("Đã tạo sao lưu");
         return;
      } catch (Throwable var21) {
         lib.Display.mc.showError(var21, lib.Display.mc);
      } finally {
         try {
            var2.flush();
            var2.close();
            var0.close();
         } catch (Exception var17) {
         }
      }
   }

   // $VF: renamed from: f () void
   private static void method_16() {
      byte[] var0;
      if ((var0 = method_17(field_60)) != null) {
         try {
            DataInputStream var7 = new DataInputStream(new ByteArrayInputStream(var0));
            byte[] var1 = new byte[0];

            while (true) {
               String var2;
               try {
                  var2 = var7.readUTF();
               } catch (Exception var5) {
                  var7.close();
                  showError("Đã nhập khẩu sao lư");
                  return;
               }

               RecordStore var8 = RecordStore.openRecordStore(var2, true);

               int var3;
               while ((var3 = var7.readInt()) > 0) {
                  int var4 = var7.readInt();

                  while (var8.getNextRecordID() <= var3) {
                     var8.addRecord(var1, 0, 0);
                  }

                  if (var4 > 0) {
                     byte[] var9 = new byte[var4];
                     var7.readFully(var9);
                     var8.setRecord(var3, var9, 0, var9.length);
                  }
               }

               var8.closeRecordStore();
            }
         } catch (Throwable var6) {
            lib.Display.mc.showError(var6, lib.Display.mc);
         }
      }
   }

   private static byte[] method_17(String var0) {
      FileConnection var7;
      try {
         var7 = (FileConnection)Connector.open(var0, 1);
      } catch (Throwable var5) {
         lib.Display.mc.showError(var5, lib.Display.mc);
         return null;
      }

      try {
         InputStream var1 = var7.openInputStream();
         int var2;
         byte[] var3 = new byte[var2 = (int)var7.fileSize()];
         int var4 = var2;

         while (var2 > 0) {
            var2 -= var1.read(var3, var2 - var4, var2);
         }

         var1.close();
         var7.close();
         return var3;
      } catch (Exception var6) {
         return null;
      }
   }

   public void itemStateChanged(Item var1) {
      try {
         Form var2 = (Form)field_50.getCurrent();
         Gauge var4;
         if ((var4 = (Gauge)var1).getMaxValue() > 10) {
            field_49 = var4.getValue();
            ((Gauge)var2.get(1)).setValue(field_49 / 10);
         } else {
            field_49 = var4.getValue() * 10;
            ((Gauge)var2.get(0)).setValue(field_49);
         }

         DeviceControl.setLights(0, field_49);
      } catch (Throwable var3) {
      }
   }

   // $VF: renamed from: g () void
   private static void method_18() {
      Canvas.buff = null;
      Canvas.image = null;
      System.gc();
      if (field_61 != null && field_61.length() > 0) {
         Image var0 = null;
         if (!field_61.startsWith("/")) {
            byte[] var1;
            if ((var1 = method_17(field_61)) == null) {
               return;
            }

            try {
               var0 = Image.createImage(var1, 0, var1.length);
            } catch (Throwable var6) {
            }
         } else {
            try {
               var0 = Image.createImage(field_61);
            } catch (Throwable var5) {
            }
         }

         if (var0 != null) {
            int var8 = var0.getWidth();
            int var2 = var0.getHeight();
            int[] var3 = new int[var8 * var2];
            var0.getRGB(var3, 0, var8, 0, 0, var8, var2);
            int var7 = field_66 << 24 | 16777215;

            for (int var4 = var3.length - 1; var4 >= 0; var4--) {
               var3[var4] &= var7;
            }

            Canvas.image = Image.createRGBImage(var3, var8, var2, true);
            System.gc();
         }
      }
   }

   public void destroy(lib.MIDlet var1) {
      field_50.setCurrent(field_52);
      lib.Display.midlets.remove(var1);
      int var2 = 0;

      while (var2 < field_54.length && var1 != field_54[var2]) {
         var2++;
      }

      field_54[var2] = null;
      MidletInfo var3 = field_55[var2];
      field_52.set(var2, var3.name, var3.icon);
      field_51 = field_52;
      var3.destroy(var1);
      System.gc();
   }

   public void pause() {
      if (field_50.getCurrent() != null) {
         field_51 = field_50.getCurrent();
      }

      field_50.setCurrent(field_52);
   }

   public void resume() {
      field_50.setCurrent(field_51);
   }

   public MrQuyet() {
      this.method_19();

      try {
         DeviceControl.setLights(0, field_49);
      } catch (Throwable var1) {
      }
   }

   public void startApp() {
   }

   public void pauseApp() {
   }

   public void destroyApp(boolean var1) {
   }

   public void commandAction(Command var1, Displayable var2) {
      label241: {
         int var3 = field_52 == null ? 0 : field_52.getSelectedIndex();
         switch (var1.getPriority()) {
            case 0:
               if (var2 == field_52) {
                  if (var3 < 0 || var3 >= field_54.length) {
                     return;
                  }

                  if (field_54[var3] == null) {
                     if (this.field_64) {
                        field_65 = var3;
                        this.field_64 = false;
                        new Thread(this).start();
                     }

                     return;
                  }

                  field_54[var3].showDisplay();
               }
               break;
            case 1:
               if (var2 == field_52) {
                  int var20 = var3;
                  if (var3 < 0 || var3 >= field_54.length || field_54[var3] == null) {
                     return;
                  }

                  try {
                     field_54[var20].notifyDestroyed();
                  } catch (Exception var7) {
                     this.showError(var7, this);
                     return;
                  }
               }
               break;
            case 2:
               for (int var13 = 0; var13 < field_54.length; var13++) {
                  if (field_54[var13] != null) {
                     try {
                        field_54[var13].notifyDestroyed();
                     } catch (Throwable var6) {
                     }
                  }
               }

               this.notifyDestroyed();
               return;
            case 3:
               Form var12;
               (var12 = new Form("Cài đặt cơ bản")).addCommand(new Command("Quay lại", 2, 10));
               var12.addCommand(new Command("Lưu", 4, 9));
               var12.append(new TextField("Phím thu nhỏ:", String.valueOf(Canvas.menukey), 10, 2));
               var12.append(new TextField("Thời gian đáp ứng:", String.valueOf(Canvas.waittime), 10, 2));
               var12.append(new TextField("Bấm giờ ngủ:", String.valueOf(field_62 / 1000L), 12, 2));
               var12.append(new TextField("Cỡ icon (rộng * cao):", MidletInfo.iconwidth + "*" + MidletInfo.iconheight, 12, 0));
               var12.append(new TextField("Khoảng RAM được phục hồi:", String.valueOf(field_63), 12, 2));
               var12.append(new TextField("Độ trong suốt (1-255):", String.valueOf(field_66), 3, 2));
               var12.append(new TextField("Đường dẫn hình nền:", String.valueOf(field_61), 256, 0));
               var12.append(new TextField("Ổ sao lưu:", field_60, 256, 0));
               var12.append(new TextField("Mã truy cập:", "", 20, 0));
               var12.addCommand(new Command("Tạo sao lưu", 4, 6));
               var12.addCommand(new Command("Nhập khẩu sao lưu", 4, 7));
               var12.addCommand(new Command("Thư mục lưu", 4, 17));
               var12.setCommandListener(this);
               field_50.setCurrent(var12);
               return;
            case 4:
               this.method_8();
               return;
            case 5:
               field_50.setCurrent(null);
               this.notifyPaused();
               return;
            case 6:
               method_15();
               return;
            case 7:
               method_16();
               return;
            case 8:
               Form var11;
               (var11 = new Form("Chỉnh đèn màn hình")).setCommandListener(this);
               var11.append(new Gauge("Độ sáng", true, 100, field_49));
               var11.append(new Gauge("Độ sáng", true, 10, field_49 / 10));
               var11.setItemStateListener(this);
               var11.addCommand(new Command("Quay lại", 2, 10));
               var11.addCommand(new Command("Lưu", 4, 24));
               field_50.setCurrent(var11);
               return;
            case 9:
               Form var10;
               Canvas.menukey = method_12(method_11(var10 = (Form)var2, 0), 42);
               Canvas.waittime = method_13(method_11(var10, 1), 500L);
               field_62 = method_13(method_11(var10, 2).concat("000"), 300000L);
               String var17;
               if ((var3 = (var17 = method_11(var10, 3)).indexOf(42)) > 0) {
                  MidletInfo.iconwidth = method_12(var17.substring(0, var3++), 20);
                  MidletInfo.iconheight = method_12(var17.substring(var3), 20);

                  for (int var18 = 0; var18 < field_54.length; var18++) {
                     MidletInfo var25;
                     (var25 = field_55[var18]).updateIcon();
                     field_52.set(var18, var25.name, field_54[var18] == null ? var25.icon : MidletInfo.runimg(var25.icon));
                  }
               }

               String var19 = field_61;
               var3 = field_66;
               field_63 = method_13(method_11(var10, 4), 5000L);
               field_66 = method_12(method_11(var10, 5), 100);
               field_61 = method_11(var10, 6);
               field_60 = method_11(var10, 7);
               field_59 = method_11(var10, 8);
               method_20();
               var10.deleteAll();
               if (field_61 != null && field_61.length() > 1 && !field_61.equals(var19) || var3 != field_66) {
                  method_18();
                  return;
               }
               break;
            case 10:
               field_46 = null;
               field_45 = null;
               field_50.setCurrent(field_52);
               return;
            case 11:
               System.gc();
               this.method_8();
               return;
            case 12:
               Form var9;
               if (method_11(var9 = (Form)var2, 0).equals(field_59)) {
                  var9.deleteAll();
                  this.method_14();
                  return;
               }

               showError("Mật khẩu lỗi");
               return;
            case 13:
               this.notifyDestroyed();
               return;
            case 14:
               return;
            case 15:
               return;
            case 16:
               return;
            case 17:
               this.method_7();
               return;
            case 18:
               this.field_43 = -1;
               this.method_9("", "");
               return;
            case 19:
               List var16 = (List)var2;

               try {
                  field_56.remove(var16.getString(var16.getSelectedIndex()));
                  var16.delete(var16.getSelectedIndex());
                  return;
               } catch (Exception var5) {
                  return;
               }
            case 20:
               List var15 = (List)var2;

               try {
                  this.field_43 = var15.getSelectedIndex();
                  this.method_9(var15.getString(this.field_43), (String)field_56.get(var15.getString(this.field_43)));
                  return;
               } catch (Exception var4) {
                  return;
               }
            case 21:
               method_10();
               return;
            case 22:
               Form var21 = (Form)var2;
               List var8 = (List)field_46;
               String var14 = ((TextField)var21.get(0)).getString();
               String var22 = ((TextField)var21.get(1)).getString();
               if (this.field_43 >= 0) {
                  field_56.remove(var8.getString(this.field_43));
                  var8.set(this.field_43, var14, null);
               } else {
                  var8.append(var14, null);
               }

               field_56.put(var14, var22);
            case 23:
               break label241;
            case 24:
               method_20();
               return;
            case 25:
               if (this.field_57 != null) {
                  this.field_57.interrupt();
                  this.field_57 = null;
               } else {
                  field_65 = -1;
                  (this.field_57 = new Thread(this)).start();
               }

               this.method_8();
               return;
            case 26:
               field_50.setCurrent(field_45);
               return;
            case 27:
               if (this.field_58 != null) {
                  this.field_58.interrupt();
                  this.field_58 = null;
               } else {
                  field_65 = -2;
                  (this.field_58 = new Thread(this)).start();
               }

               this.method_8();
         }

         return;
      }

      field_50.setCurrent(field_46);
   }

   // $VF: renamed from: h () void
   private void method_19() {
      lib.Display.mc = this;
      lib.MIDlet.mid = this;
      lib.Display.d = field_50 = Display.getDisplay(this);
      field_47 = new Alert("[!] Quyetdaik [!]", "", null, AlertType.INFO);

      try {
         RecordStore var1;
         byte[] var2;
         if ((var1 = RecordStore.openRecordStore("MrQuyet_rms_urlmap", false)).getNumRecords() > 0 && (var2 = var1.getRecord(1)) != null) {
            DataInputStream var7 = new DataInputStream(new ByteArrayInputStream(var2));

            while (true) {
               try {
                  field_56.put(var7.readUTF(), var7.readUTF());
               } catch (Exception var4) {
                  var7.close();
                  break;
               }
            }
         }

         var1.closeRecordStore();
      } catch (Exception var5) {
         field_56.put("sms://106*", "sms://10086");
      }

      try {
         RecordStore var6;
         if ((var6 = RecordStore.openRecordStore("MrQuyet_rms_setting", false)).getNumRecords() > 0) {
            byte[] var8;
            if ((var8 = var6.getRecord(1)) == null) {
               this.method_14();
               var6.closeRecordStore();
               return;
            }

            DataInputStream var9;
            Canvas.menukey = (var9 = new DataInputStream(new ByteArrayInputStream(var8))).readInt();
            Canvas.waittime = (long)var9.readInt();
            field_59 = var9.readUTF();
            field_60 = var9.readUTF();
            field_49 = var9.readShort();
            field_62 = var9.readLong();
            field_61 = var9.readUTF();
            field_66 = var9.readShort();
            MidletInfo.iconwidth = var9.readShort();
            MidletInfo.iconheight = var9.readShort();
            field_63 = var9.readLong();
            var9.close();
            if (field_59.length() > 0) {
               Form var10;
               (var10 = new Form("Mật khẩu")).append(new TextField("Mã truy cập:", "", 20, 0));
               var10.setCommandListener(this);
               var10.addCommand(new Command("Xác nhận", 4, 12));
               var10.addCommand(new Command("Thoát", 7, 13));
               field_50.setCurrent(var10);
            } else {
               this.method_14();
            }
         }

         var6.closeRecordStore();
      } catch (Exception var3) {
         this.method_14();
      }
   }

   // $VF: renamed from: i () void
   private static void method_20() {
      try {
         RecordStore var0 = RecordStore.openRecordStore("MrQuyet_rms_setting", true);
         ByteArrayOutputStream var1;
         DataOutputStream var2;
         (var2 = new DataOutputStream(var1 = new ByteArrayOutputStream())).writeInt(Canvas.menukey);
         var2.writeInt((int)Canvas.waittime);
         var2.writeUTF(field_59);
         var2.writeUTF(field_60);
         var2.writeShort(field_49);
         var2.writeLong(field_62);
         var2.writeUTF(field_61);
         var2.writeShort(field_66);
         var2.writeShort(MidletInfo.iconwidth);
         var2.writeShort(MidletInfo.iconheight);
         var2.writeLong(field_63);
         var2.close();
         byte[] var4 = var1.toByteArray();
         if (var0.getNumRecords() > 0) {
            var0.setRecord(1, var4, 0, var4.length);
         } else {
            var0.addRecord(var4, 0, var4.length);
         }

         var0.closeRecordStore();
      } catch (Exception var3) {
         lib.Display.mc.showError(var3, lib.Display.mc);
      }

      field_50.setCurrent(field_52);
   }
}
