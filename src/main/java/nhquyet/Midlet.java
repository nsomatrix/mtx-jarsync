package nhquyet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;
import nhquyet.bytecode.ClassBuilder;
import nhquyet.bytecode.ClassPatcher;
import nhquyet.model.Base64Resources;
import nhquyet.model.MidletEntry;
import nhquyet.ui.CustomChoiceCanvas;
import nhquyet.zip.ByteInputStream;
import nhquyet.zip.Inflater;
import nhquyet.zip.LittleEndianOutputStream;
import nhquyet.zip.ZipEntry;
import nhquyet.zip.ZipOutputStream;

/**
 * Main Application Entry for Embed Advance Menu (AngelChip).
 * Combines multiple J2ME MIDlets into a unified single package with runtime
 * switcher menus, bytecode patching, and resource isolation.
 */
public class Midlet extends MIDlet implements Runnable, CommandListener {
    private static final Alert ALERT = new Alert("[!] Quyetdaik [!]", "", null, AlertType.INFO);
    private static Display display;

    private static final ClassPatcher classPatcher = new ClassPatcher();
    private static final ByteArrayOutputStream sharedByteArrayOut = new ByteArrayOutputStream();
    private static final DataOutputStream sharedDataOut = new DataOutputStream(sharedByteArrayOut);

    private static final Hashtable parsedEntriesMap = new Hashtable();
    private static final Hashtable failedJarsMap = new Hashtable();
    private static final Vector combinedMidletList = new Vector();
    private static final Hashtable readConnectionCache = new Hashtable(200);
    private static final Hashtable writeConnectionCache = new Hashtable(200);

    public static final char[] BASE64_ALPHABET = new char[]{
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };
    public static final byte[] BASE64_DECODE_TABLE = new byte[256];

    static {
        for (int i = 0; i < 255; i++) {
            BASE64_DECODE_TABLE[i] = -1;
        }
        for (int i = 0; i < 64; i++) {
            BASE64_DECODE_TABLE[BASE64_ALPHABET[i]] = (byte) i;
        }
    }

    private static String currentDirectory = "file:///";
    private static String pendingOperationPath;

    private Choice fileBrowserChoice;
    private Displayable fileBrowserDisplayable;
    private Choice appListChoice;
    private Displayable appListDisplayable;
    private Choice settingsChoice;
    private Displayable settingsDisplayable;

    private final Form progressForm = new Form("");
    private final Form buildSettingsForm = new Form("Thêm & C.đặt");
    private final Form midletSettingsForm = new Form("Thêm vào cài đặt");

    private Image iconImage;
    private Image folderImage;
    private ByteInputStream byteInputStream;

    private TextField tfIcon;
    private TextField tfAppName;
    private TextField tfDisplayName;
    private TextField tfAuthor;
    private TextField tfSavePath;

    private int currentMidletActionCounter;
    private int currentWorkerTask;
    private String[] rootDirectories;
    private MidletEntry activeMidletEntry;

    // Feature toggles
    private boolean featureScreenshot;
    private boolean featureList;
    private boolean featureMainPageList;
    private boolean featureNoExit;
    private boolean featureCustomCanvasUI;

    public Midlet() {
        this.iconImage = Base64Resources.getImage('a');
        this.folderImage = Base64Resources.getImage('b');

        this.tfIcon = new TextField("Icon:", null, 200, 0);
        this.tfAppName = new TextField("Tên ứng dụng:", null, 200, 0);
        this.tfDisplayName = new TextField("Tên hiển thị:", null, 200, 0);
        this.tfAuthor = new TextField("Tên tác giả:", null, 200, 0);
        this.tfSavePath = new TextField("Nơi lưu:", null, 200, 0);

        this.buildSettingsForm.append(this.tfIcon);
        this.buildSettingsForm.append(this.tfAppName);
        this.buildSettingsForm.append(this.tfDisplayName);
        this.buildSettingsForm.append(this.tfAuthor);
        this.buildSettingsForm.append(this.tfSavePath);
        this.buildSettingsForm.addCommand(new Command(Base64Resources.get("start"), 1, 5));
        this.buildSettingsForm.setCommandListener(this);

        Command backCmd = new Command(Base64Resources.getString('e'), 2, 2);
        this.buildSettingsForm.addCommand(backCmd);
        this.midletSettingsForm.addCommand(backCmd);
        this.progressForm.addCommand(backCmd);

        this.midletSettingsForm.append(new TextField("Số lượng:", null, 2, 2));
        for (int i = 1; i < 27; i++) {
            this.midletSettingsForm.append(new TextField("Tên hiển thị " + i + ":", null, 20, 0));
        }
        this.midletSettingsForm.setCommandListener(this);
        this.midletSettingsForm.addCommand(new Command("Thêm", 4, 1));
        this.progressForm.setCommandListener(this);

        try {
            RecordStore.openRecordStore("danhsach", false).closeRecordStore();
            this.featureCustomCanvasUI = true;
            this.initCanvasUI();
        } catch (Throwable t) {
            this.featureCustomCanvasUI = false;
            this.initStandardUI();
        }
    }

    private void initStandardUI() {
        this.fileBrowserDisplayable = new List(Base64Resources.getString('c'), Choice.IMPLICIT);
        this.fileBrowserChoice = (Choice) this.fileBrowserDisplayable;
        this.appListDisplayable = new List("Danh sách", Choice.MULTIPLE);
        this.appListChoice = (Choice) this.appListDisplayable;
        this.settingsDisplayable = new List("Cài đặt", Choice.MULTIPLE);
        this.settingsChoice = (Choice) this.settingsDisplayable;
        this.setupDisplayables();
    }

    private void initCanvasUI() {
        this.fileBrowserDisplayable = new CustomChoiceCanvas(Base64Resources.getString('c'), Choice.IMPLICIT);
        this.fileBrowserChoice = (Choice) this.fileBrowserDisplayable;
        this.appListDisplayable = new CustomChoiceCanvas("Danh sách", Choice.MULTIPLE);
        this.appListChoice = (Choice) this.appListDisplayable;
        this.settingsDisplayable = new CustomChoiceCanvas("Cài đặt", Choice.MULTIPLE);
        this.settingsChoice = (Choice) this.settingsDisplayable;
        this.setupDisplayables();
    }

    private void setupDisplayables() {
        display = Display.getDisplay(this);
        display.setCurrent(this.fileBrowserDisplayable);
        this.fileBrowserDisplayable.setCommandListener(this);

        this.fileBrowserDisplayable.addCommand(new Command(Base64Resources.getString('d'), 4, 1));
        Command backCmd = new Command(Base64Resources.getString('e'), 2, 2);
        this.fileBrowserDisplayable.addCommand(backCmd);
        this.appListDisplayable.addCommand(backCmd);
        this.settingsDisplayable.addCommand(backCmd);
        this.settingsDisplayable.addCommand(new Command("Lưu", 4, 1));
        this.settingsDisplayable.setCommandListener(this);

        this.settingsChoice.append("Kich hoạt chụp ảnh m.hình", null);
        this.settingsChoice.append("Kích hoạt d.sách", null);
        this.settingsChoice.append("Kích hoạt d.sách trang chính", null);
        this.settingsChoice.append("Thêm hình nền", null);
        this.settingsChoice.append("S.dụng d.sách", null);
        this.settingsChoice.append("Kích hoạt d.sách màu", null);
        this.settingsChoice.setSelectedIndex(4, this.featureCustomCanvasUI);

        this.appListChoice.setFitPolicy(Choice.TEXT_WRAP_OFF);
        this.fileBrowserDisplayable.addCommand(new Command(Base64Resources.getString('f'), 7, 3));
        this.fileBrowserDisplayable.addCommand(new Command(Base64Resources.getString('o'), 5, 4));
        this.fileBrowserDisplayable.addCommand(new Command(Base64Resources.get("add"), 8, 5));
        this.fileBrowserDisplayable.addCommand(new Command("Thêm tất cả", 1, 6));
        this.fileBrowserDisplayable.addCommand(new Command("Cài đặt", 3, 7));

        this.appListDisplayable.addCommand(new Command(Base64Resources.get("start"), 1, 5));
        this.appListDisplayable.addCommand(new Command(Base64Resources.get("delete"), 3, 5));
        this.appListDisplayable.addCommand(new Command(Base64Resources.get("selectall"), 8, 5));
        this.appListDisplayable.setCommandListener(this);

        String savedDir = loadSavedDirectory();
        this.navigateDirectory(savedDir);
        this.loadSettings();
        this.fileBrowserDisplayable.addCommand(new Command(Base64Resources.get("view"), 6, 5));
    }

    public void commandAction(Command cmd, Displayable disp) {
        int cmdType = cmd.getCommandType();
        if (disp == this.fileBrowserDisplayable) {
            if (cmd == List.SELECT_COMMAND) {
                this.handleFileOrDirSelection();
                return;
            }
            switch (cmdType) {
                case 7: // Exit
                    this.fileBrowserChoice.deleteAll();
                    this.notifyDestroyed();
                    return;
                case 2: // Back
                    this.navigateDirectory(currentDirectory + "../");
                    return;
                case 5: // View progress
                    display.setCurrent(this.progressForm);
                    return;
                case 4: // Open
                    this.handleFileOrDirSelection();
                    return;
                case 8: // Add current
                    String sel = this.fileBrowserChoice.getString(this.fileBrowserChoice.getSelectedIndex());
                    if (isArchiveFilename(sel)) {
                        this.appListChoice.setSelectedIndex(this.appListChoice.append(currentDirectory + sel, null), true);
                    }
                    return;
                case 1: // Add all
                    if (!currentDirectory.equals("file:///")) {
                        for (int i = this.fileBrowserChoice.size() - 1; i > 0 && this.fileBrowserChoice.getImage(i) == this.folderImage; i--) {
                            String path = currentDirectory + this.fileBrowserChoice.getString(i);
                            if (!parsedEntriesMap.containsKey(path)) {
                                this.appListChoice.append(path, null);
                            }
                        }
                    }
                    return;
                case 6: // App list
                    display.setCurrent(this.appListDisplayable);
                    return;
                case 3: // Settings
                    display.setCurrent(this.settingsDisplayable);
                    return;
            }
            return;
        }

        if (disp == this.progressForm) {
            display.setCurrent(this.fileBrowserDisplayable);
            return;
        }

        if (disp == this.appListDisplayable) {
            switch (cmdType) {
                case 2: // Back
                    display.setCurrent(this.fileBrowserDisplayable);
                    return;
                case 1: // Build
                    boolean hasSel = false;
                    for (int i = this.appListChoice.size() - 1; i >= 0; i--) {
                        if (this.appListChoice.isSelected(i)) {
                            hasSel = true;
                            break;
                        }
                    }
                    if (hasSel) {
                        display.setCurrent(this.buildSettingsForm);
                    }
                    return;
                case 3: // Delete selected
                    for (int i = this.appListChoice.size() - 1; i >= 0; i--) {
                        if (this.appListChoice.isSelected(i)) {
                            this.appListChoice.delete(i);
                        }
                    }
                    return;
                case 8: // Select all
                    for (int i = this.appListChoice.size() - 1; i >= 0; i--) {
                        this.appListChoice.setSelectedIndex(i, true);
                    }
                    return;
            }
            return;
        }

        if (disp == this.buildSettingsForm) {
            if (cmdType == 2) {
                display.setCurrent(this.appListDisplayable);
                return;
            }
            this.saveSettings();
            this.currentWorkerTask = 2;
            new Thread(this).start();
            return;
        }

        if (disp == this.midletSettingsForm) {
            if (cmdType == 4) {
                boolean isNew = true;
                String jarPath = this.activeMidletEntry.jarPath;
                for (int i = 0; i < this.appListChoice.size(); i++) {
                    if (jarPath.equals(this.appListChoice.getString(i))) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    this.appListChoice.setSelectedIndex(this.appListChoice.append(jarPath, null), true);
                }
            }
            this.saveMidletSettingsFromForm(this.activeMidletEntry);
            display.setCurrent(this.fileBrowserDisplayable);
            return;
        }

        if (disp == this.settingsDisplayable) {
            if (cmdType == 4) {
                this.featureScreenshot = this.settingsChoice.isSelected(0);
                this.featureList = this.settingsChoice.isSelected(1);
                this.featureMainPageList = this.settingsChoice.isSelected(2);
                this.featureNoExit = this.settingsChoice.isSelected(3);
                this.featureCustomCanvasUI = this.settingsChoice.isSelected(4);

                if (this.settingsChoice.isSelected(5)) {
                    saveColorSettings();
                }
                this.saveSettings();
            }
            display.setCurrent(this.fileBrowserDisplayable);
        }
    }

    private void handleFileOrDirSelection() {
        String sel = this.fileBrowserChoice.getString(this.fileBrowserChoice.getSelectedIndex());
        if (sel.endsWith("/")) {
            pendingOperationPath = currentDirectory + sel;
            this.currentWorkerTask = 0;
            new Thread(this).start();
            return;
        }
        if (isArchiveFilename(sel)) {
            String fullPath = currentDirectory + sel;
            if (failedJarsMap.containsKey(fullPath)) {
                return;
            }
            if (parsedEntriesMap.containsKey(fullPath)) {
                this.openMidletSettingsForm((MidletEntry) parsedEntriesMap.get(fullPath));
                return;
            }
            pendingOperationPath = fullPath;
            this.currentWorkerTask = 1;
            new Thread(this).start();
        }
    }

    public void run() {
        switch (this.currentWorkerTask) {
            case 0:
                this.navigateDirectory(pendingOperationPath);
                return;
            case 1:
                String path = pendingOperationPath;
                byte[] rawJar = readFileBytes(path);
                if (this.initZipByteStream(rawJar)) {
                    try {
                        Vector entries = this.parseZipEntries(this.byteInputStream);
                        if (entries.elementAt(0) == this) {
                            entries.removeAllElements();
                            this.byteInputStream.close();
                            showAlert(path + " Đọc thất bại: file zip không hợp lệ");
                            return;
                        }
                        MidletEntry entry = new MidletEntry(path, entries);
                        parsedEntriesMap.put(path, entry);
                        this.openMidletSettingsForm(entry);
                    } catch (Exception e) {
                        showAlert(path + " Đọc thất bại: " + e.toString());
                        failedJarsMap.put(path, path);
                    }
                }
                return;
            case 2:
                long startTime = System.currentTimeMillis();
                boolean success = false;
                try {
                    Vector selectedList = new Vector();
                    for (int i = 0; i < this.appListChoice.size(); i++) {
                        if (this.appListChoice.isSelected(i)) {
                            selectedList.addElement(this.appListChoice.getString(i));
                        }
                    }
                    if (selectedList.size() > 0) {
                        this.progressForm.deleteAll();
                        display.setCurrent(this.progressForm);
                        String[] targetJars = new String[selectedList.size()];
                        selectedList.copyInto(targetJars);
                        success = this.buildCombinedJar(targetJars);
                    }
                } catch (Throwable t) {
                    this.progressForm.append(t.toString());
                }

                if (this.byteInputStream != null) {
                    this.byteInputStream.close();
                    this.byteInputStream = null;
                }

                char statusChar = success ? 'h' : 'i';
                StringBuffer msg = new StringBuffer();
                msg.append(Base64Resources.getString(statusChar));
                msg.append(Base64Resources.getString('j')).append("Tổng thời gian\n").append(System.currentTimeMillis() - startTime);
                this.progressForm.append(msg.append(Base64Resources.getString('k')).toString());
                try {
                    Thread.sleep(1000L);
                } catch (Throwable ignored) {
                }
                display.setCurrent(this.fileBrowserDisplayable);
                break;
        }
    }

    private boolean buildCombinedJar(String[] jarPaths) {
        String outPath = this.tfSavePath.getString();
        LittleEndianOutputStream leos = null;
        try {
            int lastSlash = outPath.lastIndexOf('/');
            String dirPath = outPath.substring(0, lastSlash + 1);
            FileConnection dirConn = getReadConnection(dirPath);
            if (dirConn == null) {
                this.progressForm.append("Đường dẫn không hợp lệ " + outPath);
                return false;
            }
            if (!dirConn.isDirectory()) {
                Throwable t = ensureDirectoryExists(dirPath);
                if (t != null) {
                    this.progressForm.append("Không thể tạo thư mục\n" + dirPath + "\n" + t.toString());
                    return false;
                }
            }

            FileConnection targetConn = outPath.toLowerCase().endsWith(".nhq")
                    ? getWriteConnection(outPath + "_jar")
                    : getWriteConnection(outPath);

            try {
                targetConn.setWritable(true);
                targetConn.delete();
            } catch (Exception ignored) {
            }
            try {
                targetConn.create();
            } catch (Exception e) {
                this.progressForm.append("Không thể tạo file\n" + outPath);
                return false;
            }

            leos = new LittleEndianOutputStream(targetConn.openOutputStream());
            ZipOutputStream zos = new ZipOutputStream(leos);

            this.injectRuntimeLibraries(zos, leos);
            combinedMidletList.removeAllElements();
            this.mergeTargetJars(zos, leos, jarPaths);

            zos.putNextEntry("META-INF/MANIFEST.MF");
            StringBuffer mf = new StringBuffer("Manifest-Version: 1.0\r\nMicroEdition-Configuration: CLDC-1.1\r\nMicroEdition-Profile: MIDP-2.0\r\nMIDlet-Version: 2.5\r\nMIDlet-Vendor: ");
            mf.append(this.tfAuthor.getString()).append("\r\nMIDlet-Name: ").append(this.tfAppName.getString()).append("\r\nMIDlet-1: ").append(this.tfDisplayName.getString()).append(",/MrQuyet/mid.png,MrQuyet.MrQuyet\n");
            if (this.featureNoExit) {
                mf.append("Nokia-MIDlet-no-exit: true\r\n");
            }
            for (int i = 0; i < combinedMidletList.size(); i++) {
                mf.append(i).append(": ").append(combinedMidletList.elementAt(i)).append("\r\n");
            }
            zos.write(mf.append("\r\n").toString().getBytes("utf-8"));
            zos.closeEntry();
            zos.finish();
            zos.close();

            if (outPath.toLowerCase().endsWith(".nhq")) {
                try {
                    targetConn.rename(outPath.substring(lastSlash + 1));
                    writeConnectionCache.remove(outPath + "_jar");
                } catch (Exception ignored) {
                }
            }
            System.gc();
            return true;
        } catch (Throwable t) {
            if (leos != null) {
                try {
                    leos.close();
                } catch (IOException ignored) {
                }
            }
            this.progressForm.append(t.toString());
            return false;
        }
    }

    private void mergeTargetJars(ZipOutputStream zos, OutputStream rawOut, String[] jarPaths) {
        this.currentMidletActionCounter = 0;
        for (int i = 0; i < jarPaths.length; i++) {
            String path = jarPaths[i];
            MidletEntry entry = (MidletEntry) parsedEntriesMap.get(path);
            if (entry == null) {
                this.byteInputStream = null;
                try {
                    this.initZipByteStream(readFileBytes(path));
                    Vector entries = this.parseZipEntries(this.byteInputStream);
                    entry = new MidletEntry(path, entries);
                    entry.parseManifest(this.extractZipEntry((ZipEntry) entries.elementAt(0)));
                } catch (Exception e) {
                    this.progressForm.append(path + "\nRead Failure");
                    continue;
                }
            } else {
                this.byteInputStream = new ByteInputStream(readFileBytes(path));
            }

            long start = System.currentTimeMillis();
            this.mergeSingleMidlet(entry, zos, rawOut);
            StringBuffer sb = new StringBuffer().append(path).append("\n").append(System.currentTimeMillis() - start);
            this.progressForm.append(sb.append(Base64Resources.getString('k')).append("\n").toString());
        }
    }

    private void mergeSingleMidlet(MidletEntry entry, ZipOutputStream zos, OutputStream rawOut) {
        entry.selectedIndex = Math.min(Integer.parseInt(entry.titles[0]), entry.midlet2 == null ? 26 : 25);
        int baseIdx = this.currentMidletActionCounter;
        String midlet1 = entry.midlet1;
        String midletClass = midlet1.substring(midlet1.lastIndexOf(',') + 1).trim();

        int endIdx = entry.selectedIndex + baseIdx;
        for (int i = baseIdx; i < endIdx; i++) {
            combinedMidletList.addElement(entry.titles[i - baseIdx + 1] + ",/" + entry.pngName + "," + (i == 0 ? "" : (char) (i + 97) + ".") + midletClass + ",/" + entry.iniName);
        }

        if (entry.midlet2 != null) {
            String midlet2Class = entry.midlet2.substring(entry.midlet2.lastIndexOf(',') + 1).trim();
            combinedMidletList.addElement(entry.titles[26] + ",/" + entry.pngName + "," + (char) (baseIdx + 97) + "." + midlet2Class + ",/" + entry.iniName);
        }

        // Write INI file
        StringBuffer ini = new StringBuffer();
        Enumeration keys = entry.manifestProperties.keys();
        while (keys.hasMoreElements()) {
            String k = (String) keys.nextElement();
            ini.append(k).append(": ").append(entry.manifestProperties.get(k)).append("\r\n");
        }
        ini.append("\r\n");

        try {
            zos.putNextEntry(entry.iniName);
            zos.write(ini.toString().getBytes("utf-8"));
            zos.closeEntry();
        } catch (Exception ignored) {
        }

        // Patch and write classes
        Vector classEntries = entry.getClassEntries();
        int classTotal = classEntries.size();
        StringItem itemStatus = new StringItem("", "");

        for (int i = 0; i < entry.selectedIndex; i++) {
            String prefix = (this.currentMidletActionCounter == 0) ? "" : ((char) (97 + this.currentMidletActionCounter) + "/");
            this.currentMidletActionCounter++;

            Gauge progressGauge = new Gauge(this.currentMidletActionCounter + ": " + entry.jarPath, false, classTotal, 0);
            this.progressForm.append(progressGauge);
            int statusIdx = this.progressForm.append(itemStatus);
            display.setCurrent(this.progressForm);

            Hashtable replacements = ClassPatcher.initClassReplacements();
            for (int k = 0; k < classTotal; k++) {
                String cName = ((ZipEntry) classEntries.elementAt(k)).name;
                String className = cName.substring(0, cName.length() - 6);
                replacements.put(className, prefix + className);
            }

            for (int k = 0; k < classTotal; k++) {
                ZipEntry ze = (ZipEntry) classEntries.elementAt(k);
                String name = ze.name;
                itemStatus.setText(name);
                progressGauge.setValue(k + 1);

                try {
                    byte[] classBytes = this.extractZipEntry(ze);
                    if (classBytes != null) {
                        zos.putNextEntry(prefix + name);
                        char midletChar = prefix.length() > 0 ? prefix.charAt(0) : 'a';
                        zos.write(patchClassBytes(classBytes, midletChar));
                        zos.closeEntry();
                    } else {
                        this.progressForm.append(name + " Xuất ra thất bại\n");
                    }
                } catch (Exception ignored) {
                }
            }

            String staticClassName = (prefix.length() > 0) ? (prefix.charAt(0) + "/Static") : "Static";
            try {
                byte[] staticClass = ClassBuilder.buildClass(ClassPatcher.patchedClasses, staticClassName);
                if (staticClass != null) {
                    zos.putNextEntry(staticClassName + ".class");
                    zos.write(staticClass);
                    zos.closeEntry();
                } else {
                    System.err.println("[ClassBuilder] Warning: Failed to build synthetic class: " + staticClassName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.progressForm.delete(statusIdx);
        }

        // Copy non-class resources
        Vector resEntries = entry.getResourceEntries();
        int resTotal = resEntries.size();
        if (resTotal > 0) {
            Gauge resGauge = new Gauge(entry.jarPath + " Tài nguyên", false, resTotal, 0);
            this.progressForm.append(resGauge);
            int statusIdx = this.progressForm.append(itemStatus);
            display.setCurrent(this.progressForm);

            for (int i = 0; i < resTotal; i++) {
                ZipEntry re = (ZipEntry) resEntries.elementAt(i);
                int off = re.offset;
                String rName = re.name;
                resGauge.setValue(i + 1);
                itemStatus.setText(rName);

                try {
                    zos.putNextEntry(re);
                    zos.currentEntry = null; // direct copy
                    this.byteInputStream.pos = off + 26;
                    this.byteInputStream.skip(this.byteInputStream.readShortLE() + this.byteInputStream.readShortLE());
                    rawOut.write(this.byteInputStream.buf, this.byteInputStream.pos, re.compressedSize);
                    rawOut.flush();

                    // If icon, also copy under generated pngName
                    if (rName.equals(entry.iconName)) {
                        ZipEntry pngEntry = new ZipEntry(entry.pngName);
                        pngEntry.method = re.method;
                        pngEntry.flags = re.flags;
                        pngEntry.compressedSize = re.compressedSize;
                        pngEntry.size = re.size;
                        pngEntry.crc = re.crc;
                        pngEntry.time = re.time;

                        zos.putNextEntry(pngEntry);
                        zos.currentEntry = null;
                        this.byteInputStream.pos = off + 26;
                        this.byteInputStream.skip(this.byteInputStream.readShortLE() + this.byteInputStream.readShortLE());
                        rawOut.write(this.byteInputStream.buf, this.byteInputStream.pos, re.compressedSize);
                        rawOut.flush();
                    }
                } catch (Exception ignored) {
                }
            }
            this.progressForm.delete(statusIdx);
        }

        this.byteInputStream.close();
        this.byteInputStream = null;
    }

    private void injectRuntimeLibraries(ZipOutputStream zos, OutputStream rawOut) {
        try {
            zos.putNextEntry("MrQuyet/mid.png");
            byte[] iconBytes = readFileBytes(this.tfIcon.getString());
            if (iconBytes == null) {
                this.progressForm.append(this.tfIcon.getString() + " Đọc thất bại");
                zos.write(readResourceBytes("/icon.png"));
            } else {
                zos.write(iconBytes);
            }
            zos.closeEntry();

            // Extract MrQuyetlibs.jar
            this.initZipByteStream(readResourceBytes("/MrQuyetlibs.jar"));
            Vector baseLibEntries = this.parseZipEntries(this.byteInputStream);
            for (int i = 1; i < baseLibEntries.size(); i++) {
                ZipEntry ze = (ZipEntry) baseLibEntries.elementAt(i);
                String name = ze.name;
                if (!name.endsWith(".class") || ((this.featureList || this.featureMainPageList) && name.endsWith("List.class"))) {
                    continue;
                }
                int off = ze.offset;
                this.byteInputStream.pos = off + 26;
                zos.putNextEntry(ze);
                zos.currentEntry = null;
                this.byteInputStream.pos = off + 26;
                this.byteInputStream.skip(this.byteInputStream.readShortLE() + this.byteInputStream.readShortLE());
                rawOut.write(this.byteInputStream.buf, this.byteInputStream.pos, ze.compressedSize);
                rawOut.flush();
            }

            // Extract MrQuyetSSlib.jar or MrQuyetlib.jar
            String mainLibPath = this.featureScreenshot ? "/MrQuyetSSlib.jar" : "/MrQuyetlib.jar";
            this.initZipByteStream(readResourceBytes(mainLibPath));
            Vector mainLibEntries = this.parseZipEntries(this.byteInputStream);
            for (int i = 1; i < mainLibEntries.size(); i++) {
                ZipEntry ze = (ZipEntry) mainLibEntries.elementAt(i);
                String name = ze.name;
                if (!name.endsWith(".class") || (!this.featureList && !this.featureMainPageList && name.endsWith("List.class"))) {
                    continue;
                }
                if (this.featureMainPageList && name.endsWith("MrQuyet.class")) {
                    byte[] rawClass = this.extractZipEntry(ze);
                    int patchedLen = patchMrQuyetClassListRef(rawClass);
                    zos.putNextEntry(name);
                    zos.write(rawClass, 0, patchedLen);
                    zos.closeEntry();
                    continue;
                }

                int off = ze.offset;
                this.byteInputStream.pos = off + 26;
                zos.putNextEntry(ze);
                zos.currentEntry = null;
                this.byteInputStream.pos = off + 26;
                this.byteInputStream.skip(this.byteInputStream.readShortLE() + this.byteInputStream.readShortLE());
                rawOut.write(this.byteInputStream.buf, this.byteInputStream.pos, ze.compressedSize);
                rawOut.flush();
            }

            if (this.featureScreenshot && (this.featureList || this.featureMainPageList)) {
                this.initZipByteStream(readResourceBytes("/MrQuyetlib.jar"));
                Vector fallbackEntries = this.parseZipEntries(this.byteInputStream);
                for (int i = 1; i < fallbackEntries.size(); i++) {
                    ZipEntry ze = (ZipEntry) fallbackEntries.elementAt(i);
                    if (ze.name.endsWith("List.class")) {
                        int off = ze.offset;
                        this.byteInputStream.pos = off + 26;
                        zos.putNextEntry(ze);
                        zos.currentEntry = null;
                        this.byteInputStream.pos = off + 26;
                        this.byteInputStream.skip(this.byteInputStream.readShortLE() + this.byteInputStream.readShortLE());
                        rawOut.write(this.byteInputStream.buf, this.byteInputStream.pos, ze.compressedSize);
                        rawOut.flush();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static int patchMrQuyetClassListRef(byte[] bytes) {
        int len = bytes.length;
        int limit = len - 35;
        byte[] target = "javax/microedition/lcdui/List".getBytes();
        for (int i = 10; i < limit; i++) {
            boolean match = true;
            for (int k = 0; k < target.length; k++) {
                if (bytes[i + k] != target[k]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                bytes[i] = 'l';
                bytes[i + 1] = 'i';
                bytes[i + 2] = 'b';
                int p = i - 1;
                while (bytes[p] != 1) { // find tag CONSTANT_Utf8
                    p--;
                }
                int oldLen = bytes[p + 2] & 0xFF;
                bytes[p + 2] = (byte) (oldLen - 21);
                System.arraycopy(bytes, i + 24, bytes, i + 3, len - i - 24);
                len -= 21;
                limit -= 21;
            }
        }
        return len;
    }

    private byte[] extractZipEntry(ZipEntry entry) {
        this.byteInputStream.pos = entry.offset + 26;
        try {
            int size = entry.size;
            byte[] dest = new byte[size];
            this.byteInputStream.skip(this.byteInputStream.readShortLE() + this.byteInputStream.readShortLE());
            if (entry.method == 8) {
                Inflater inf = new Inflater(true);
                inf.setInput(this.byteInputStream.buf, this.byteInputStream.pos, entry.compressedSize);
                inf.inflate(dest, 0, size);
            } else {
                this.byteInputStream.readFully(dest, size);
            }
            return dest;
        } catch (Throwable t) {
            return null;
        }
    }

    private static byte[] patchClassBytes(byte[] classBytes, char midletChar) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(classBytes));
        try {
            if (dis.readInt() != 0xCAFEBABE) {
                dis.close();
                return classBytes;
            }
        } catch (Exception ignored) {
        }

        classPatcher.readClassHeader(dis);
        sharedByteArrayOut.reset();
        classPatcher.patchClass(dis, midletChar);
        try {
            classPatcher.writeClass(sharedDataOut);
            sharedDataOut.flush();
        } catch (Exception ignored) {
        }
        return sharedByteArrayOut.toByteArray();
    }

    private Vector parseZipEntries(ByteInputStream bis) {
        Vector list = new Vector();
        list.addElement(this);
        while (bis.readIntBE() == 0x504B0102) { // Central directory record signature
            ZipEntry ze = new ZipEntry(null);
            bis.skip(4L);
            ze.flags = bis.readShortLE();
            ze.method = (short) bis.readShortLE();
            ze.time = bis.readIntLE();
            ze.crc = bis.readIntLE();
            ze.compressedSize = bis.readIntLE();
            ze.size = bis.readIntLE();
            ze.nameLength = bis.readShortLE();
            int extraLen = bis.readShortLE();
            int commentLen = bis.readShortLE();
            bis.skip(8L);
            ze.offset = bis.readIntLE();
            byte[] nameBytes = new byte[ze.nameLength];
            bis.readFully(nameBytes, ze.nameLength);
            ze.setNameBytes(nameBytes);

            if (!ze.name.endsWith("/")) {
                if (ze.name.equals("META-INF/MANIFEST.MF")) {
                    list.setElementAt(ze, 0);
                } else {
                    list.addElement(ze);
                }
            }
            bis.skip(extraLen + commentLen);
        }
        return list;
    }

    private boolean initZipByteStream(byte[] data) {
        try {
            if (data != null) {
                int p;
                for (p = data.length - 22; p > 0 && (data[p] != 0x50 || data[p + 1] != 0x4B || data[p + 2] != 0x05 || data[p + 3] != 0x06); p--) {
                }
                if (p < 20) {
                    return false;
                }
                p += 16;
                int centralDirOff = (data[p++] & 0xFF) | ((data[p++] & 0xFF) << 8) | ((data[p++] & 0xFF) << 16) | ((data[p] & 0xFF) << 24);
                this.byteInputStream = new ByteInputStream(data);
                this.byteInputStream.pos = centralDirOff;
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private void navigateDirectory(String targetPath) {
        while (true) {
            if (targetPath == null || targetPath.equals("file:///")) {
                currentDirectory = "file:///";
                saveCurrentDirectory("file:///");
                this.fileBrowserChoice.deleteAll();
                if (this.rootDirectories == null) {
                    Enumeration roots = FileSystemRegistry.listRoots();
                    while (roots.hasMoreElements()) {
                        this.fileBrowserChoice.append((String) roots.nextElement(), this.iconImage);
                    }
                    this.rootDirectories = new String[this.fileBrowserChoice.size()];
                    for (int i = this.rootDirectories.length - 1; i >= 0; i--) {
                        this.rootDirectories[i] = this.fileBrowserChoice.getString(i);
                    }
                    return;
                }
                for (int i = 0; i < this.rootDirectories.length; i++) {
                    this.fileBrowserChoice.append(this.rootDirectories[i], this.iconImage);
                }
                return;
            }

            if (targetPath.endsWith("../")) {
                int slash = targetPath.lastIndexOf('/', targetPath.length() - 5);
                targetPath = (slash > 0) ? targetPath.substring(0, slash + 1) : null;
                if (targetPath == null || targetPath.length() <= 8) {
                    targetPath = null;
                    continue;
                }
                FileConnection fc = openDirectory(targetPath);
                if (fc != null) {
                    this.listDirectoryContents(fc);
                    return;
                }
                targetPath = null;
                continue;
            }

            FileConnection fc = openDirectory(targetPath);
            if (fc != null) {
                this.listDirectoryContents(fc);
                return;
            }
            targetPath = null;
        }
    }

    private void listDirectoryContents(FileConnection fc) {
        try {
            Enumeration list = fc.list("*", true);
            saveCurrentDirectory(currentDirectory);
            this.fileBrowserChoice.deleteAll();
            this.fileBrowserChoice.append("../", this.iconImage);

            Vector subDirs = new Vector();
            Vector files = new Vector();
            while (list.hasMoreElements()) {
                String item = (String) list.nextElement();
                if (item.endsWith("/")) {
                    subDirs.addElement(item);
                } else if (isArchiveFilename(item)) {
                    files.addElement(item);
                }
            }

            for (int i = 0; i < subDirs.size(); i++) {
                this.fileBrowserChoice.append((String) subDirs.elementAt(i), this.iconImage);
            }
            for (int i = 0; i < files.size(); i++) {
                this.fileBrowserChoice.append((String) files.elementAt(i), this.folderImage);
            }
        } catch (IOException ignored) {
        }
    }

    private static FileConnection openDirectory(String path) {
        currentDirectory = path;
        FileConnection fc = getReadConnection(currentDirectory);
        while (fc == null || !fc.isDirectory()) {
            int slash = path.lastIndexOf('/', path.length() - 2);
            path = (slash > 0) ? path.substring(0, slash + 1) : null;
            currentDirectory = path;
            if (path != null && path.length() > 8) {
                fc = getReadConnection(currentDirectory);
                continue;
            }
            return null;
        }
        return fc;
    }

    private static boolean isArchiveFilename(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        return lower.endsWith(".zip") || lower.endsWith(".jar") || lower.endsWith("_jar") || lower.endsWith("_zip");
    }

    private static FileConnection getReadConnection(String url) {
        try {
            if (readConnectionCache.containsKey(url)) {
                return (FileConnection) readConnectionCache.get(url);
            }
            FileConnection conn = (FileConnection) Connector.open(url, Connector.READ);
            readConnectionCache.put(url, conn);
            return conn;
        } catch (Throwable t) {
            showAlert(t.toString());
            return null;
        }
    }

    private static FileConnection getWriteConnection(String url) {
        try {
            if (writeConnectionCache.containsKey(url)) {
                return (FileConnection) writeConnectionCache.get(url);
            }
            FileConnection conn = (FileConnection) Connector.open(url, Connector.READ_WRITE);
            writeConnectionCache.put(url, conn);
            return conn;
        } catch (Throwable t) {
            return null;
        }
    }

    private static Throwable ensureDirectoryExists(String dirUrl) {
        try {
            FileConnection fc = (FileConnection) Connector.open(dirUrl, Connector.READ);
            if (!fc.isDirectory()) {
                fc.close();
                fc = (FileConnection) Connector.open(dirUrl, Connector.READ_WRITE);
                try {
                    fc.mkdir();
                    fc.close();
                    return null;
                } catch (Throwable t) {
                    if (dirUrl.endsWith("/")) {
                        dirUrl = dirUrl.substring(0, dirUrl.length() - 1);
                    }
                    ensureDirectoryExists(dirUrl.substring(0, dirUrl.lastIndexOf('/') + 1));
                    fc.mkdir();
                }
            }
            try {
                fc.close();
            } catch (Throwable ignored) {
            }
        } catch (Throwable t) {
            return t;
        }
        return null;
    }

    private static byte[] readFileBytes(String url) {
        FileConnection fc = getReadConnection(url);
        if (fc == null) {
            return null;
        }
        try {
            int size = (int) fc.fileSize();
            if (size < 10 || size > 0x2800000) {
                return null;
            }
            DataInputStream dis = fc.openDataInputStream();
            byte[] buf = new byte[size];
            dis.readFully(buf);
            dis.close();
            return buf;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] readResourceBytes(String resPath) {
        try {
            DataInputStream dis = new DataInputStream(this.getClass().getResourceAsStream(resPath));
            byte[] buf = new byte[dis.available()];
            dis.readFully(buf);
            dis.close();
            return buf;
        } catch (Exception e) {
            return null;
        }
    }

    private void openMidletSettingsForm(MidletEntry entry) {
        this.activeMidletEntry = entry;
        if (entry.titles == null) {
            entry.parseManifest(this.extractZipEntry((ZipEntry) entry.entries.elementAt(0)));
            this.byteInputStream.close();
        }
        for (int i = 0; i < 27; i++) {
            ((TextField) this.midletSettingsForm.get(i)).setString(entry.titles[i]);
        }
        display.setCurrent(this.midletSettingsForm);
    }

    private void saveMidletSettingsFromForm(MidletEntry entry) {
        for (int i = 0; i < 27; i++) {
            entry.titles[i] = ((TextField) this.midletSettingsForm.get(i)).getString();
        }
    }

    private void saveSettings() {
        if (this.featureCustomCanvasUI) {
            try {
                RecordStore.openRecordStore("danhsach", true).closeRecordStore();
            } catch (Throwable ignored) {
            }
        } else {
            try {
                RecordStore.deleteRecordStore("danhsach");
            } catch (Throwable ignored) {
            }
        }

        try {
            RecordStore rs = RecordStore.openRecordStore("setting", true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeUTF(this.tfIcon.getString());
            dos.writeUTF(this.tfAppName.getString());
            dos.writeUTF(this.tfDisplayName.getString());
            dos.writeUTF(this.tfAuthor.getString());
            dos.writeUTF(this.tfSavePath.getString());
            dos.writeBoolean(this.featureScreenshot);
            dos.writeBoolean(this.featureList);
            dos.writeBoolean(this.featureMainPageList);
            dos.writeBoolean(this.featureNoExit);
            dos.flush();
            byte[] rec1 = baos.toByteArray();

            baos.reset();
            int count = this.appListChoice.size();
            dos.writeInt(count);
            for (int i = 0; i < count; i++) {
                dos.writeUTF(this.appListChoice.getString(i));
            }
            dos.flush();
            dos.close();
            byte[] rec2 = baos.toByteArray();

            if (rs.getNumRecords() > 0) {
                rs.setRecord(1, rec1, 0, rec1.length);
                rs.setRecord(2, rec2, 0, rec2.length);
            } else {
                rs.addRecord(rec1, 0, rec1.length);
                rs.addRecord(rec2, 0, rec2.length);
            }
            rs.closeRecordStore();
        } catch (Throwable ignored) {
        }

        if (this.featureCustomCanvasUI && !(this.fileBrowserDisplayable instanceof CustomChoiceCanvas)) {
            this.initCanvasUI();
        } else if (!this.featureCustomCanvasUI && (this.fileBrowserDisplayable instanceof CustomChoiceCanvas)) {
            this.initStandardUI();
        }
    }

    private void loadSettings() {
        try {
            RecordStore rs = RecordStore.openRecordStore("setting", true);
            if (rs.getNumRecords() > 0) {
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rs.getRecord(1)));
                this.tfIcon.setString(dis.readUTF());
                this.tfAppName.setString(dis.readUTF());
                this.tfDisplayName.setString(dis.readUTF());
                this.tfAuthor.setString(dis.readUTF());
                this.tfSavePath.setString(dis.readUTF());
                this.featureScreenshot = dis.readBoolean();
                this.settingsChoice.setSelectedIndex(0, this.featureScreenshot);
                this.featureList = dis.readBoolean();
                this.settingsChoice.setSelectedIndex(1, this.featureList);
                this.featureMainPageList = dis.readBoolean();
                this.settingsChoice.setSelectedIndex(2, this.featureMainPageList);
                this.featureNoExit = dis.readBoolean();
                this.settingsChoice.setSelectedIndex(3, this.featureNoExit);
                dis.close();

                dis = new DataInputStream(new ByteArrayInputStream(rs.getRecord(2)));
                int count = dis.readInt();
                for (int i = 0; i < count; i++) {
                    this.appListChoice.append(dis.readUTF(), null);
                }
                dis.close();
            } else {
                this.tfIcon.setString(Base64Resources.get("icon"));
                this.tfAppName.setString(this.getAppProperty("MIDlet-Name"));
                this.tfDisplayName.setString(this.getAppProperty("MIDlet-Name"));
                this.tfAuthor.setString(this.getAppProperty("MIDlet-Vendor"));
                this.tfSavePath.setString(Base64Resources.get("savedir"));
            }
            rs.closeRecordStore();
        } catch (Throwable ignored) {
        }

        try {
            RecordStore rs = RecordStore.openRecordStore("color_setting", false);
            if (rs.getNumRecords() > 0) {
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rs.getRecord(1)));
                CustomChoiceCanvas.colorBg = dis.readInt();
                CustomChoiceCanvas.colorText = dis.readInt();
                CustomChoiceCanvas.colorBar = dis.readInt();
                CustomChoiceCanvas.colorBorder = dis.readInt();
                CustomChoiceCanvas.colorTitle = dis.readInt();
                CustomChoiceCanvas.colorHighlight = dis.readInt();
                dis.close();
            }
            rs.closeRecordStore();
        } catch (Exception e) {
            CustomChoiceCanvas.resetColors();
        }
    }

    private static void saveColorSettings() {
        try {
            RecordStore rs = RecordStore.openRecordStore("color_setting", true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(CustomChoiceCanvas.colorBg);
            dos.writeInt(CustomChoiceCanvas.colorText);
            dos.writeInt(CustomChoiceCanvas.colorBar);
            dos.writeInt(CustomChoiceCanvas.colorBorder);
            dos.writeInt(CustomChoiceCanvas.colorTitle);
            dos.writeInt(CustomChoiceCanvas.colorHighlight);
            dos.close();
            byte[] data = baos.toByteArray();
            if (rs.getNumRecords() > 0) {
                rs.setRecord(1, data, 0, data.length);
            } else {
                rs.addRecord(data, 0, data.length);
            }
            rs.closeRecordStore();
        } catch (Exception ignored) {
        }
    }

    private static String loadSavedDirectory() {
        try {
            RecordStore rs = RecordStore.openRecordStore("dir", true);
            if (rs.getNumRecords() > 0) {
                String dir = new String(rs.getRecord(1), "utf-8");
                rs.closeRecordStore();
                return dir;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void saveCurrentDirectory(String path) {
        try {
            RecordStore rs = RecordStore.openRecordStore("dir", true);
            byte[] bytes = path.getBytes("utf-8");
            if (rs.getNumRecords() > 0) {
                rs.setRecord(1, bytes, 0, bytes.length);
            } else {
                rs.addRecord(bytes, 0, bytes.length);
            }
            rs.closeRecordStore();
        } catch (Throwable ignored) {
        }
    }

    private static void showAlert(String msg) {
        ALERT.setString(msg);
        if (display != null) {
            display.setCurrent(ALERT);
        }
    }

    protected void startApp() {
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
    }
}
