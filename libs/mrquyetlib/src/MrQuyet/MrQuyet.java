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
   private static List sharedSubMenu;
   private static final Command cmdRamRecovery = new Command("Phục hồi RAM", 4, 11);
   private static final Command cmdRamRecoveryOn = new Command("Phục hồi (ON)", 4, 27);
   private static final Command cmdCancel = new Command("Hủy", 4, 27);
   private static final Command cmdBack = new Command("Quay lại", 2, 10);
   private static final Command cmdSleepModeOn = new Command("Chế độ ngủ (ON)", 4, 25);
   private static final Command cmdSleepModeOff = new Command("Chế độ ngủ (OFF)", 4, 25);
   private static final Command cmdBackSubMenu = new Command("Quay lại", 2, 26);
   private static final Command cmdSave = new Command("Lưu", 1, 21);
   private static final Command cmdDelete = new Command("Xóa", 1, 19);
   private static final Command cmdEdit = new Command("Sửa", 1, 20);
   private static final Command cmdAddNew = new Command("Thêm mới", 1, 18);
   private int selectedSubMenuIndex;
   private static Throwable lastError;
   private static Displayable callerDisplayable;
   private static Displayable currentSubMenuDisplayable;
   private static Alert infoAlert;
   private static long errorTimestamp = 0L;
   private static int screenLightLevel = 30;
   private static Display mainDisplay;
   private static Displayable suspendedDisplayable;
   private static List mainMenuList;
   public static int currentMidletIndex;
   private static lib.MIDlet[] runningMidlets;
   private static MidletInfo[] midletInfoList = new MidletInfo[30];
   public static final PrintStream debugout = System.out;
   public static final Hashtable networkRedirectMap = new Hashtable();
   private Thread ramRecoveryThread;
   private Thread sleepModeThread;
   private static String securityPassword;
   private static String backupDirectory;
   private static String bgImagePath = "/MrQuyet/MrQuyet.png";
   private static long sleepIntervalMs = 300000L;
   private static long ramRecoveryIntervalMs = 5000L;
   private boolean firstTimeRun = true;
   private static int threadActionCode;
   private static int bgOpacity = 100;
   private List createOrResetSubMenu(String var1) {
      if (sharedSubMenu == null) {
         (sharedSubMenu = new List(var1, 3)).setCommandListener(this);
      } else {
         String var2 = sharedSubMenu.getTitle();
         sharedSubMenu.deleteAll();
         if (var2 == "Thư mục lưu") {
            sharedSubMenu.removeCommand(cmdAddNew);
            sharedSubMenu.removeCommand(cmdEdit);
            sharedSubMenu.removeCommand(cmdDelete);
            sharedSubMenu.removeCommand(cmdSave);
            sharedSubMenu.removeCommand(cmdBackSubMenu);
         } else {
            sharedSubMenu.removeCommand(cmdRamRecovery);
            sharedSubMenu.removeCommand(cmdRamRecoveryOn);
            sharedSubMenu.removeCommand(cmdCancel);
            sharedSubMenu.removeCommand(cmdBack);
            sharedSubMenu.removeCommand(cmdSleepModeOn);
            sharedSubMenu.removeCommand(cmdSleepModeOff);
         }

         sharedSubMenu.setTitle(var1);
      }

      return sharedSubMenu;
   }
   private void showFolderMenu() {
      callerDisplayable = mainDisplay.getCurrent();
      List var1;
      (var1 = this.createOrResetSubMenu("Thư mục lưu")).addCommand(cmdAddNew);
      var1.addCommand(cmdEdit);
      var1.setSelectCommand(cmdEdit);
      var1.addCommand(cmdDelete);
      var1.addCommand(cmdSave);
      var1.addCommand(cmdBackSubMenu);
      Enumeration var2 = networkRedirectMap.keys();

      while (var2.hasMoreElements()) {
         var1.append((String)var2.nextElement(), null);
      }

      var1.setCommandListener(this);
      mainDisplay.setCurrent(var1);
   }
   private void showRamOptionsMenu() {
      List var1;
      (var1 = this.createOrResetSubMenu("Bộ nhớ điện thoại")).addCommand(cmdRamRecovery);
      var1.addCommand(this.sleepModeThread == null ? cmdRamRecoveryOn : cmdCancel);
      var1.addCommand(cmdBack);
      var1.addCommand(this.ramRecoveryThread == null ? cmdSleepModeOn : cmdSleepModeOff);
      var1.setFitPolicy(1);
      Runtime var2 = Runtime.getRuntime();
      var1.append("RAM:" + var2.totalMemory() / 1024L + " k", null);
      var1.append("RAM trống:" + var2.freeMemory() / 1024L + " k", null);
      var1.append("Mạch:" + Thread.activeCount(), null);
      if (suspendedDisplayable != null) {
         var1.append("Hiển thị : ", null);
         var1.append(suspendedDisplayable.toString(), null);
      }

      if (lastError != null) {
         var1.append("Exception : ", null);
         var1.append(lastError.toString(), null);
      }

      mainDisplay.setCurrent(var1);
   }
   private void showEditEntryForm(String var1, String var2) {
      currentSubMenuDisplayable = mainDisplay.getCurrent();
      Form var3;
      (var3 = new Form("Sửa")).append(new TextField("Mặc định:", var1, 512, 0));
      var3.append(new TextField("Mới:", var2, 512, 0));
      var3.setCommandListener(this);
      var3.addCommand(new Command("Xác nhận", 4, 22));
      var3.addCommand(new Command("Hủy", 2, 23));
      mainDisplay.setCurrent(var3);
   }
   private static void saveRedirectMapRecord() {
      Hashtable var0 = networkRedirectMap;

      try {
         RecordStore var1 = RecordStore.openRecordStore("MrQuyet_rms_urlmap", true);
         if (networkRedirectMap.isEmpty() && var1.getNumRecords() > 0) {
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
   private static String getTextFieldValue(Form var0, int var1) {
      return ((TextField)var0.get(var1)).getString();
   }
   private static int parseStringToInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0);
      } catch (Exception var2) {
         return var1;
      }
   }
   private static long parseStringToLong(String var0, long var1) {
      try {
         return Long.parseLong(var0);
      } catch (Exception var3) {
         return var1;
      }
   }
   private void initMainUi() {
      List var1 = mainMenuList = new List("AngelChip Menu", 3);
      if (backupDirectory == null) {
         backupDirectory = "file:///c/" + this.getAppProperty("MIDlet-Name") + ".db";
      }

      String var3;
      for (int var2 = 0; (var3 = this.getAppProperty(String.valueOf(var2))) != null; var2++) {
         MidletInfo var4 = MidletInfo.getInstance(var3);
         midletInfoList[var2] = var4;
         var1.append(var4.name, var4.icon);
      }

      runningMidlets = new lib.MIDlet[mainMenuList.size()];
      var1.addCommand(new Command("Chạy ứng dụng", 1, 0));
      var1.addCommand(new Command("Dừng ứng dụng", 1, 1));
      var1.addCommand(new Command("Thu nhỏ", 1, 5));
      var1.addCommand(new Command("Chỉnh độ sáng", 1, 8));
      var1.addCommand(new Command("Tùy chọn RAM", 1, 4));
      var1.addCommand(new Command("Cài đặt cơ bản", 1, 3));
      var1.addCommand(new Command("Thoát", 7, 2));
      var1.setCommandListener(this);
      mainDisplay.setCurrent(mainMenuList);
      if (bgImagePath != null) {
         loadBackgroundImage();
      }
   }
   public void run() {
      if (threadActionCode >= 0) {
         try {
            lib.MIDlet var2;
            MidletInfo var7;
            if ((var2 = (var7 = midletInfoList[threadActionCode]).newInstance()) != null) {
               runningMidlets[threadActionCode] = var2;
               mainMenuList.set(threadActionCode, var7.name, MidletInfo.runimg(var7.icon));
               var2.start();
            }
         } catch (Throwable var3) {
            this.showError(var3, this);
         }

         this.firstTimeRun = true;
      } else if (threadActionCode == -1) {
         try {
            Thread.sleep(sleepIntervalMs);

            for (int var1 = 0; var1 < runningMidlets.length; var1++) {
               if (runningMidlets[var1] != null) {
                  try {
                     runningMidlets[var1].notifyDestroyed();
                  } catch (Throwable var4) {
                  }
               }
            }

            this.notifyDestroyed();
         } catch (Exception var5) {
         }
      } else {
         if (ramRecoveryIntervalMs > 0L) {
            while (true) {
               try {
                  Thread.sleep(ramRecoveryIntervalMs);
                  System.gc();
               } catch (Exception var6) {
                  break;
               }
            }
         }
      }
   }

   public void showError(Throwable var1, Object var2) {
      lastError = var1;
      showError(var2 + ": " + var1.toString());
   }

   public static void showError(String var0) {
      if (System.currentTimeMillis() - errorTimestamp > 1500L) {
         errorTimestamp = System.currentTimeMillis();
         infoAlert.setTimeout(-2);
         infoAlert.setString(var0);
         mainDisplay.setCurrent(infoAlert, (Displayable)(mainDisplay.getCurrent() instanceof Alert ? mainMenuList : mainDisplay.getCurrent()));
      }
   }
   private static void createBackupArchive() {
      FileConnection var0 = null;

      try {
         var0 = (FileConnection)Connector.open(backupDirectory, 2);

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
   private static void restoreBackupArchive() {
      byte[] var0;
      if ((var0 = showPromptAlert(backupDirectory)) != null) {
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

   private static byte[] showPromptAlert(String var0) {
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
         Form var2 = (Form)mainDisplay.getCurrent();
         Gauge var4;
         if ((var4 = (Gauge)var1).getMaxValue() > 10) {
            screenLightLevel = var4.getValue();
            ((Gauge)var2.get(1)).setValue(screenLightLevel / 10);
         } else {
            screenLightLevel = var4.getValue() * 10;
            ((Gauge)var2.get(0)).setValue(screenLightLevel);
         }

         DeviceControl.setLights(0, screenLightLevel);
      } catch (Throwable var3) {
      }
   }
   private static void loadBackgroundImage() {
      Canvas.buff = null;
      Canvas.image = null;
      System.gc();
      if (bgImagePath != null && bgImagePath.length() > 0) {
         Image var0 = null;
         if (!bgImagePath.startsWith("/")) {
            byte[] var1;
            if ((var1 = showPromptAlert(bgImagePath)) == null) {
               return;
            }

            try {
               var0 = Image.createImage(var1, 0, var1.length);
            } catch (Throwable var6) {
            }
         } else {
            try {
               var0 = Image.createImage(bgImagePath);
            } catch (Throwable var5) {
            }
         }

         if (var0 != null) {
            int var8 = var0.getWidth();
            int var2 = var0.getHeight();
            int[] var3 = new int[var8 * var2];
            var0.getRGB(var3, 0, var8, 0, 0, var8, var2);
            int var7 = bgOpacity << 24 | 16777215;

            for (int var4 = var3.length - 1; var4 >= 0; var4--) {
               var3[var4] &= var7;
            }

            Canvas.image = Image.createRGBImage(var3, var8, var2, true);
            System.gc();
         }
      }
   }

   public void destroy(lib.MIDlet var1) {
      mainDisplay.setCurrent(mainMenuList);
      lib.Display.midlets.remove(var1);
      int var2 = 0;

      while (var2 < runningMidlets.length && var1 != runningMidlets[var2]) {
         var2++;
      }

      runningMidlets[var2] = null;
      MidletInfo var3 = midletInfoList[var2];
      mainMenuList.set(var2, var3.name, var3.icon);
      suspendedDisplayable = mainMenuList;
      var3.destroy(var1);
      System.gc();
   }

   public void pause() {
      if (mainDisplay.getCurrent() != null) {
         suspendedDisplayable = mainDisplay.getCurrent();
      }

      mainDisplay.setCurrent(mainMenuList);
   }

   public void resume() {
      mainDisplay.setCurrent(suspendedDisplayable);
   }

   public MrQuyet() {
      this.initApp();

      try {
         DeviceControl.setLights(0, screenLightLevel);
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
         int var3 = mainMenuList == null ? 0 : mainMenuList.getSelectedIndex();
         switch (var1.getPriority()) {
            case 0:
               if (var2 == mainMenuList) {
                  if (var3 < 0 || var3 >= runningMidlets.length) {
                     return;
                  }

                  if (runningMidlets[var3] == null) {
                     if (this.firstTimeRun) {
                        threadActionCode = var3;
                        this.firstTimeRun = false;
                        new Thread(this).start();
                     }

                     return;
                  }

                  runningMidlets[var3].showDisplay();
               }
               break;
            case 1:
               if (var2 == mainMenuList) {
                  int var20 = var3;
                  if (var3 < 0 || var3 >= runningMidlets.length || runningMidlets[var3] == null) {
                     return;
                  }

                  try {
                     runningMidlets[var20].notifyDestroyed();
                  } catch (Exception var7) {
                     this.showError(var7, this);
                     return;
                  }
               }
               break;
            case 2:
               for (int var13 = 0; var13 < runningMidlets.length; var13++) {
                  if (runningMidlets[var13] != null) {
                     try {
                        runningMidlets[var13].notifyDestroyed();
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
               var12.append(new TextField("Bấm giờ ngủ:", String.valueOf(sleepIntervalMs / 1000L), 12, 2));
               var12.append(new TextField("Cỡ icon (rộng * cao):", MidletInfo.iconwidth + "*" + MidletInfo.iconheight, 12, 0));
               var12.append(new TextField("Khoảng RAM được phục hồi:", String.valueOf(ramRecoveryIntervalMs), 12, 2));
               var12.append(new TextField("Độ trong suốt (1-255):", String.valueOf(bgOpacity), 3, 2));
               var12.append(new TextField("Đường dẫn hình nền:", String.valueOf(bgImagePath), 256, 0));
               var12.append(new TextField("Ổ sao lưu:", backupDirectory, 256, 0));
               var12.append(new TextField("Mã truy cập:", "", 20, 0));
               var12.addCommand(new Command("Tạo sao lưu", 4, 6));
               var12.addCommand(new Command("Nhập khẩu sao lưu", 4, 7));
               var12.addCommand(new Command("Thư mục lưu", 4, 17));
               var12.setCommandListener(this);
               mainDisplay.setCurrent(var12);
               return;
            case 4:
               this.showRamOptionsMenu();
               return;
            case 5:
               mainDisplay.setCurrent(null);
               this.notifyPaused();
               return;
            case 6:
               createBackupArchive();
               return;
            case 7:
               restoreBackupArchive();
               return;
            case 8:
               Form var11;
               (var11 = new Form("Chỉnh đèn màn hình")).setCommandListener(this);
               var11.append(new Gauge("Độ sáng", true, 100, screenLightLevel));
               var11.append(new Gauge("Độ sáng", true, 10, screenLightLevel / 10));
               var11.setItemStateListener(this);
               var11.addCommand(new Command("Quay lại", 2, 10));
               var11.addCommand(new Command("Lưu", 4, 24));
               mainDisplay.setCurrent(var11);
               return;
            case 9:
               Form var10;
               Canvas.menukey = parseStringToInt(getTextFieldValue(var10 = (Form)var2, 0), 42);
               Canvas.waittime = parseStringToLong(getTextFieldValue(var10, 1), 500L);
               sleepIntervalMs = parseStringToLong(getTextFieldValue(var10, 2).concat("000"), 300000L);
               String var17;
               if ((var3 = (var17 = getTextFieldValue(var10, 3)).indexOf(42)) > 0) {
                  MidletInfo.iconwidth = parseStringToInt(var17.substring(0, var3++), 20);
                  MidletInfo.iconheight = parseStringToInt(var17.substring(var3), 20);

                  for (int var18 = 0; var18 < runningMidlets.length; var18++) {
                     MidletInfo var25;
                     (var25 = midletInfoList[var18]).updateIcon();
                     mainMenuList.set(var18, var25.name, runningMidlets[var18] == null ? var25.icon : MidletInfo.runimg(var25.icon));
                  }
               }

               String var19 = bgImagePath;
               var3 = bgOpacity;
               ramRecoveryIntervalMs = parseStringToLong(getTextFieldValue(var10, 4), 5000L);
               bgOpacity = parseStringToInt(getTextFieldValue(var10, 5), 100);
               bgImagePath = getTextFieldValue(var10, 6);
               backupDirectory = getTextFieldValue(var10, 7);
               securityPassword = getTextFieldValue(var10, 8);
               saveSettingsRecord();
               var10.deleteAll();
               if (bgImagePath != null && bgImagePath.length() > 1 && !bgImagePath.equals(var19) || var3 != bgOpacity) {
                  loadBackgroundImage();
                  return;
               }
               break;
            case 10:
               currentSubMenuDisplayable = null;
               callerDisplayable = null;
               mainDisplay.setCurrent(mainMenuList);
               return;
            case 11:
               System.gc();
               this.showRamOptionsMenu();
               return;
            case 12:
               Form var9;
               if (getTextFieldValue(var9 = (Form)var2, 0).equals(securityPassword)) {
                  var9.deleteAll();
                  this.initMainUi();
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
               this.showFolderMenu();
               return;
            case 18:
               this.selectedSubMenuIndex = -1;
               this.showEditEntryForm("", "");
               return;
            case 19:
               List var16 = (List)var2;

               try {
                  networkRedirectMap.remove(var16.getString(var16.getSelectedIndex()));
                  var16.delete(var16.getSelectedIndex());
                  return;
               } catch (Exception var5) {
                  return;
               }
            case 20:
               List var15 = (List)var2;

               try {
                  this.selectedSubMenuIndex = var15.getSelectedIndex();
                  this.showEditEntryForm(var15.getString(this.selectedSubMenuIndex), (String)networkRedirectMap.get(var15.getString(this.selectedSubMenuIndex)));
                  return;
               } catch (Exception var4) {
                  return;
               }
            case 21:
               saveRedirectMapRecord();
               return;
            case 22:
               Form var21 = (Form)var2;
               List var8 = (List)currentSubMenuDisplayable;
               String var14 = ((TextField)var21.get(0)).getString();
               String var22 = ((TextField)var21.get(1)).getString();
               if (this.selectedSubMenuIndex >= 0) {
                  networkRedirectMap.remove(var8.getString(this.selectedSubMenuIndex));
                  var8.set(this.selectedSubMenuIndex, var14, null);
               } else {
                  var8.append(var14, null);
               }

               networkRedirectMap.put(var14, var22);
            case 23:
               break label241;
            case 24:
               saveSettingsRecord();
               return;
            case 25:
               if (this.ramRecoveryThread != null) {
                  this.ramRecoveryThread.interrupt();
                  this.ramRecoveryThread = null;
               } else {
                  threadActionCode = -1;
                  (this.ramRecoveryThread = new Thread(this)).start();
               }

               this.showRamOptionsMenu();
               return;
            case 26:
               mainDisplay.setCurrent(callerDisplayable);
               return;
            case 27:
               if (this.sleepModeThread != null) {
                  this.sleepModeThread.interrupt();
                  this.sleepModeThread = null;
               } else {
                  threadActionCode = -2;
                  (this.sleepModeThread = new Thread(this)).start();
               }

               this.showRamOptionsMenu();
         }

         return;
      }

      mainDisplay.setCurrent(currentSubMenuDisplayable);
   }
   private void initApp() {
      lib.Display.mc = this;
      lib.MIDlet.mid = this;
      lib.Display.d = mainDisplay = Display.getDisplay(this);
      infoAlert = new Alert("[!] Quyetdaik [!]", "", null, AlertType.INFO);

      try {
         RecordStore var1;
         byte[] var2;
         if ((var1 = RecordStore.openRecordStore("MrQuyet_rms_urlmap", false)).getNumRecords() > 0 && (var2 = var1.getRecord(1)) != null) {
            DataInputStream var7 = new DataInputStream(new ByteArrayInputStream(var2));

            while (true) {
               try {
                  networkRedirectMap.put(var7.readUTF(), var7.readUTF());
               } catch (Exception var4) {
                  var7.close();
                  break;
               }
            }
         }

         var1.closeRecordStore();
      } catch (Exception var5) {
         networkRedirectMap.put("sms://106*", "sms://10086");
      }

      try {
         RecordStore var6;
         if ((var6 = RecordStore.openRecordStore("MrQuyet_rms_setting", false)).getNumRecords() > 0) {
            byte[] var8;
            if ((var8 = var6.getRecord(1)) == null) {
               this.initMainUi();
               var6.closeRecordStore();
               return;
            }

            DataInputStream var9;
            Canvas.menukey = (var9 = new DataInputStream(new ByteArrayInputStream(var8))).readInt();
            Canvas.waittime = (long)var9.readInt();
            securityPassword = var9.readUTF();
            backupDirectory = var9.readUTF();
            screenLightLevel = var9.readShort();
            sleepIntervalMs = var9.readLong();
            bgImagePath = var9.readUTF();
            bgOpacity = var9.readShort();
            MidletInfo.iconwidth = var9.readShort();
            MidletInfo.iconheight = var9.readShort();
            ramRecoveryIntervalMs = var9.readLong();
            var9.close();
            if (securityPassword.length() > 0) {
               Form var10;
               (var10 = new Form("Mật khẩu")).append(new TextField("Mã truy cập:", "", 20, 0));
               var10.setCommandListener(this);
               var10.addCommand(new Command("Xác nhận", 4, 12));
               var10.addCommand(new Command("Thoát", 7, 13));
               mainDisplay.setCurrent(var10);
            } else {
               this.initMainUi();
            }
         }

         var6.closeRecordStore();
      } catch (Exception var3) {
         this.initMainUi();
      }
   }
   private static void saveSettingsRecord() {
      try {
         RecordStore var0 = RecordStore.openRecordStore("MrQuyet_rms_setting", true);
         ByteArrayOutputStream var1;
         DataOutputStream var2;
         (var2 = new DataOutputStream(var1 = new ByteArrayOutputStream())).writeInt(Canvas.menukey);
         var2.writeInt((int)Canvas.waittime);
         var2.writeUTF(securityPassword);
         var2.writeUTF(backupDirectory);
         var2.writeShort(screenLightLevel);
         var2.writeLong(sleepIntervalMs);
         var2.writeUTF(bgImagePath);
         var2.writeShort(bgOpacity);
         var2.writeShort(MidletInfo.iconwidth);
         var2.writeShort(MidletInfo.iconheight);
         var2.writeLong(ramRecoveryIntervalMs);
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

      mainDisplay.setCurrent(mainMenuList);
   }
}
