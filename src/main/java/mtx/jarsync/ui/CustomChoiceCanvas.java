package mtx.jarsync.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

/**
 * Custom full-screen interactive Choice/List Canvas.
 * Supports EXCLUSIVE, MULTIPLE, and IMPLICIT choice modes with marquee text,
 * theme color switching, and custom popup menus.
 */
public final class CustomChoiceCanvas extends Canvas implements Choice {
    private static final Command SELECT_COMMAND = List.SELECT_COMMAND;
    private static final Font DEFAULT_FONT = Font.getFont(0, 0, 8);
    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final Date DATE = new Date();

    public static int colorBg;
    public static int colorText;
    public static int colorHighlight;
    public static int colorBorder;
    public static int colorBar;
    public static int colorTitle;

    static {
        resetColors();
    }

    public static void resetColors() {
        colorBg = 0;             // Black
        colorText = 16777215;    // White (0xFFFFFF)
        colorHighlight = 16711680; // Red (0xFF0000)
        colorBorder = 65280;     // Green (0x00FF00)
        colorBar = 65280;        // Green (0x00FF00)
        colorTitle = 16776960;   // Yellow (0xFFFF00)
    }

    private String[] stringElements;
    private Image[] imageElements;
    private Font[] fontElements;
    private boolean[] selectedElements;

    private int topIndex;
    private int selectedIndex;
    private int itemCount;
    private int fitPolicy;
    private final int choiceType;
    private int menuSelectedIndex;
    private int exclusiveSelectedIndex;

    private CommandListener commandListener;
    private final Vector commands;
    private Command defaultSelectCommand;

    private int visibleCount;
    private boolean menuOpen;
    private String title;
    private final int itemHeight;
    private boolean isFullScreen;

    public boolean showIndexCount;
    public boolean isScrolling;
    private int marqueeOffset;
    private final Random random = new Random();

    public CustomChoiceCanvas(String title, int choiceType) {
        super.setFullScreenMode(true);
        if (choiceType != 1 && choiceType != 2 && choiceType != 3) {
            throw new IllegalArgumentException();
        }

        this.choiceType = choiceType;
        this.itemHeight = DEFAULT_FONT.getHeight() + 4;
        this.title = (title == null) ? "" : title;
        this.setTitle(title);
        this.setFullScreenMode(true);
        this.defaultSelectCommand = SELECT_COMMAND;
        this.commands = new Vector();
        this.visibleCount = 1;
        this.selectedIndex = 1;
        this.isFullScreen = false;

        this.stringElements = new String[20];
        this.fontElements = new Font[20];
        this.imageElements = new Image[20];
        if (choiceType == 2) { // MULTIPLE
            this.selectedElements = new boolean[20];
        }
        this.itemCount = 0;
    }

    public int append(String stringPart, Image imagePart) {
        if (stringPart == null) {
            return -1;
        }
        if (this.itemCount == this.stringElements.length - 1) {
            this.expandArrays();
        }

        this.stringElements[this.itemCount] = stringPart;
        this.imageElements[this.itemCount] = imagePart;
        this.fontElements[this.itemCount] = DEFAULT_FONT;
        if (this.choiceType == 2) {
            this.selectedElements[this.itemCount] = false;
        }

        this.repaint();
        return this.itemCount++;
    }

    public void delete(int itemNum) {
        if (itemNum >= 0 && itemNum < this.itemCount) {
            int moveCount = this.itemCount - itemNum - 1;
            System.arraycopy(this.stringElements, itemNum + 1, this.stringElements, itemNum, moveCount);
            System.arraycopy(this.imageElements, itemNum + 1, this.imageElements, itemNum, moveCount);
            System.arraycopy(this.fontElements, itemNum + 1, this.fontElements, itemNum, moveCount);
            if (this.choiceType == 2) {
                System.arraycopy(this.selectedElements, itemNum + 1, this.selectedElements, itemNum, moveCount);
            }
        }

        this.itemCount--;
        if (this.topIndex + this.selectedIndex > this.itemCount) {
            this.moveSelectionUp();
        }
        this.repaint();
    }

    public void deleteAll() {
        this.itemCount = 0;
        this.topIndex = 0;
        this.selectedIndex = 1;
        this.repaint();
    }

    public int getFitPolicy() {
        return this.fitPolicy;
    }

    public Font getFont(int itemNum) {
        return (itemNum >= 0 && itemNum < this.itemCount) ? this.fontElements[itemNum] : null;
    }

    public Image getImage(int itemNum) {
        return (itemNum >= 0 && itemNum < this.itemCount) ? this.imageElements[itemNum] : null;
    }

    public int getSelectedFlags(boolean[] selectedArray_return) {
        if (this.choiceType == 2) {
            int count = 0;
            for (int i = 0; i < this.itemCount; i++) {
                if (selectedArray_return[i] = this.selectedElements[i]) {
                    count++;
                }
            }
            return count;
        } else {
            selectedArray_return[this.topIndex + this.selectedIndex] = true;
            return 1;
        }
    }

    public int getSelectedIndex() {
        if (this.choiceType == 3) { // IMPLICIT
            return this.topIndex + this.selectedIndex - 1;
        } else if (this.choiceType == 1) { // EXCLUSIVE
            return this.exclusiveSelectedIndex;
        }
        return -1;
    }

    public String getString(int itemNum) {
        return (itemNum >= 0 && itemNum < this.itemCount) ? this.stringElements[itemNum] : null;
    }

    public void insert(int itemNum, String stringPart, Image imagePart) {
        if (itemNum >= this.itemCount) {
            this.append(stringPart, imagePart);
            return;
        }
        if (itemNum < 0) {
            itemNum = 0;
        }
        if (this.itemCount == this.stringElements.length - 1) {
            this.expandArrays();
        }

        for (int i = this.itemCount; i > itemNum; i--) {
            this.stringElements[i] = this.stringElements[i - 1];
            this.imageElements[i] = this.imageElements[i - 1];
            this.fontElements[i] = this.fontElements[i - 1];
            if (this.choiceType == 2) {
                this.selectedElements[i] = this.selectedElements[i - 1];
            }
        }

        this.itemCount++;
        this.stringElements[itemNum] = stringPart;
        this.fontElements[itemNum] = DEFAULT_FONT;
        this.imageElements[itemNum] = imagePart;
        if (this.choiceType == 2) {
            this.selectedElements[itemNum] = false;
        }
        this.repaint();
    }

    public boolean isSelected(int itemNum) {
        if (this.choiceType == 2) {
            return itemNum >= 0 && itemNum < this.itemCount && this.selectedElements[itemNum];
        }
        return this.getSelectedIndex() == itemNum;
    }

    public void removeCommand(Command cmd) {
        if (cmd == this.defaultSelectCommand) {
            this.defaultSelectCommand = null;
        }
        this.commands.removeElement(cmd);
        this.repaint();
    }

    public void set(int itemNum, String stringPart, Image imagePart) {
        if (itemNum >= 0 && itemNum < this.itemCount && stringPart != null) {
            this.stringElements[itemNum] = stringPart;
            this.imageElements[itemNum] = imagePart;
            this.repaint();
        }
    }

    public void setFitPolicy(int fitPolicy) {
        if (fitPolicy != 0 && fitPolicy != 2 && fitPolicy != 1) {
            throw new IllegalArgumentException();
        }
        this.fitPolicy = fitPolicy;
    }

    public void setFont(int itemNum, Font font) {
        if (itemNum >= 0 && itemNum < this.itemCount && font != null) {
            this.fontElements[itemNum] = font;
            this.repaint();
        }
    }

    public void setSelectedFlags(boolean[] selectedArray) {
        if (this.choiceType == 2) {
            System.arraycopy(selectedArray, 0, this.selectedElements, 0, this.itemCount);
            this.repaint();
        }
    }

    public void setSelectedIndex(int itemNum, boolean selected) {
        if (itemNum < this.itemCount && itemNum >= 0) {
            if (this.choiceType == 2 && selected) {
                this.selectedElements[itemNum] = selected;
            } else if (this.choiceType == 1) {
                this.exclusiveSelectedIndex = itemNum;
            } else {
                this.selectedIndex = 1;
                this.topIndex = itemNum;
            }
            this.repaint();
        }
    }

    public int size() {
        return this.itemCount;
    }

    public void addCommand(Command cmd) {
        if (cmd != null && !this.commands.contains(cmd)) {
            int type = cmd.getCommandType();
            if (this.commands.size() <= 0) {
                this.commands.addElement(cmd);
            } else {
                int idx = 0;
                while (idx < this.commands.size()) {
                    Command cur = (Command) this.commands.elementAt(idx);
                    if (type == cur.getCommandType()) {
                        if (cmd.getPriority() > cur.getPriority()) {
                            this.commands.insertElementAt(cmd, idx);
                            break;
                        }
                    } else {
                        boolean insertHere = shouldInsertCommandBefore(type, cur.getCommandType());
                        if (insertHere) {
                            this.commands.insertElementAt(cmd, idx);
                            break;
                        }
                    }
                    idx++;
                }
                if (idx >= this.commands.size()) {
                    this.commands.addElement(cmd);
                }
            }
            this.repaint();
        }
    }

    private static boolean shouldInsertCommandBefore(int newType, int curType) {
        switch (curType) {
            case 1:
                return newType != 4 && newType != 8;
            case 2:
                return newType == 6 || newType == 7;
            case 3:
                return newType == 6 || newType == 7 || newType == 2;
            case 5:
                return newType != 1 && newType != 8 && newType != 4;
            case 6:
                return false;
            case 7:
                return newType == 6;
            case 8:
                return newType != 4;
            default:
                return true;
        }
    }

    public void setCommandListener(CommandListener l) {
        this.commandListener = l;
    }

    private void expandArrays() {
        int newCap = (this.stringElements.length << 1) + 10;
        String[] newStrings = new String[newCap];
        System.arraycopy(this.stringElements, 0, newStrings, 0, this.itemCount);
        this.stringElements = newStrings;

        Font[] newFonts = new Font[newCap];
        System.arraycopy(this.fontElements, 0, newFonts, 0, this.itemCount);
        this.fontElements = newFonts;

        Image[] newImages = new Image[newCap];
        System.arraycopy(this.imageElements, 0, newImages, 0, this.itemCount);
        this.imageElements = newImages;

        if (this.choiceType == 2) {
            boolean[] newBools = new boolean[newCap];
            System.arraycopy(this.selectedElements, 0, newBools, 0, this.itemCount);
            this.selectedElements = newBools;
        }
    }

    public void keyRepeated(int keyCode) {
        this.keyPressed(keyCode);
    }

    private void triggerIndexTick() {
        if (!this.showIndexCount) {
            this.showIndexCount = true;
            new TickThread(this).start();
        }
    }

    public void keyPressed(int keyCode) {
        switch (keyCode) {
            case -7: // Right Softkey
                if (!this.isFullScreen) {
                    if (this.menuOpen) {
                        this.menuOpen = false;
                    } else if (this.commandListener != null && this.commands.size() > 0) {
                        this.commandListener.commandAction((Command) this.commands.elementAt(0), this);
                    }
                    this.repaint();
                    return;
                }
                this.isFullScreen = false;
                break;

            case -6: // Left Softkey
                if (!this.isFullScreen) {
                    if (this.menuOpen) {
                        this.menuOpen = false;
                        this.repaint();
                        if (this.commandListener != null) {
                            this.commandListener.commandAction((Command) this.commands.elementAt(this.menuSelectedIndex + 1), this);
                        }
                    } else if (this.commands.size() > 2) {
                        this.menuOpen = true;
                    } else if (this.commandListener != null && this.commands.size() > 1) {
                        this.commandListener.commandAction((Command) this.commands.elementAt(1), this);
                    }
                    this.repaint();
                    return;
                }
                this.isFullScreen = false;
                break;

            case -5: // Fire / OK Key
                if (this.menuOpen) {
                    this.keyPressed(-6);
                    return;
                }
                if (this.choiceType == 3 && this.commandListener != null && this.defaultSelectCommand != null) {
                    this.commandListener.commandAction(this.defaultSelectCommand, this);
                } else {
                    int sel = this.topIndex + this.selectedIndex - 1;
                    if (this.choiceType == 2) {
                        this.selectedElements[sel] = !this.selectedElements[sel];
                    } else if (this.choiceType == 1) {
                        if (this.exclusiveSelectedIndex == sel && this.commandListener != null && this.defaultSelectCommand != null) {
                            this.commandListener.commandAction(this.defaultSelectCommand, this);
                        } else {
                            this.exclusiveSelectedIndex = sel;
                        }
                    }
                }
                this.repaint();
                return;

            case -4: // Right Arrow
                if (!this.menuOpen) {
                    if (this.itemCount <= this.visibleCount) {
                        this.topIndex = 0;
                        this.selectedIndex = (this.selectedIndex == 1) ? this.itemCount : 1;
                    } else if (this.visibleCount + this.topIndex + this.selectedIndex <= this.itemCount) {
                        this.topIndex += this.visibleCount;
                    } else if (this.itemCount - this.topIndex <= this.visibleCount) {
                        this.topIndex = 0;
                        this.selectedIndex = 1;
                    } else {
                        this.topIndex += this.visibleCount;
                        this.selectedIndex = this.itemCount - this.topIndex;
                    }
                    this.triggerIndexTick();
                }
                this.repaint();
                return;

            case -3: // Left Arrow
                if (!this.menuOpen) {
                    if (this.itemCount <= this.visibleCount) {
                        this.topIndex = 0;
                        this.selectedIndex = (this.selectedIndex == 1) ? this.itemCount : 1;
                    } else if (this.topIndex >= this.visibleCount) {
                        this.topIndex -= this.visibleCount;
                    } else {
                        if (this.topIndex > 0) {
                            this.topIndex = 0;
                        } else if (this.itemCount > 0) {
                            this.topIndex = Math.max(0, this.itemCount - this.visibleCount);
                        }
                        this.selectedIndex = 1;
                    }
                    this.triggerIndexTick();
                }
                this.repaint();
                return;

            case -2: // Down Arrow
                if (!this.menuOpen) {
                    if (this.selectedIndex < this.visibleCount) {
                        if (this.selectedIndex + this.topIndex < this.itemCount) {
                            this.selectedIndex++;
                        } else {
                            this.topIndex = 0;
                            this.selectedIndex = 1;
                        }
                    } else if (this.selectedIndex + this.topIndex < this.itemCount) {
                        this.topIndex++;
                    } else {
                        this.topIndex = 0;
                        this.selectedIndex = 1;
                    }
                    this.triggerIndexTick();
                } else {
                    this.menuSelectedIndex--;
                    if (this.menuSelectedIndex < 0) {
                        this.menuSelectedIndex = this.commands.size() - 2;
                    }
                }
                this.repaint();
                return;

            case -1: // Up Arrow
                if (!this.menuOpen) {
                    this.moveSelectionUp();
                    this.triggerIndexTick();
                } else {
                    this.menuSelectedIndex++;
                    if (this.menuSelectedIndex > this.commands.size() - 2) {
                        this.menuSelectedIndex = 0;
                    }
                }
                this.repaint();
                return;

            case 35: // '#' Key - Toggle Fullscreen
                this.isFullScreen = !this.isFullScreen;
                this.repaint();
                return;

            case 42: // '*' Key - Randomize Colors
                colorBg = this.random.nextInt(16777215);
                colorBar = this.random.nextInt(16777215);
                colorBorder = this.random.nextInt(16777215);
                colorText = this.random.nextInt(16777215);
                colorHighlight = this.random.nextInt(16777215);
                colorTitle = this.random.nextInt(16777215);
                this.repaint();
                return;

            case 49: // '1'..'9'
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                int num = keyCode - 48;
                if (!this.menuOpen && num + this.topIndex <= this.itemCount) {
                    this.selectedIndex = num;
                    this.repaint();
                    if (this.choiceType == 3 && this.commandListener != null && this.defaultSelectCommand != null) {
                        this.commandListener.commandAction(this.defaultSelectCommand, this);
                    }
                }
                return;

            default:
                break;
        }
        this.repaint();
    }

    private void moveSelectionUp() {
        if (this.selectedIndex > 1) {
            this.selectedIndex--;
        } else if (this.topIndex > 0) {
            this.topIndex--;
        } else if (this.itemCount > 0) {
            this.topIndex = Math.max(0, this.itemCount - this.visibleCount);
            this.selectedIndex = this.itemCount - this.topIndex;
        }
    }

    protected void paint(Graphics g) {
        int width = super.getWidth();
        int height = super.getHeight();

        g.setColor(colorBg);
        g.fillRect(0, 0, width, height);
        g.setColor(colorText);
        g.setFont(DEFAULT_FONT);

        int y = (this.isFullScreen ? 0 : this.itemHeight) + 1;
        int maxBottom = this.isFullScreen ? height - 4 : height - (DEFAULT_FONT.getHeight() << 1) + 4;

        if (this.itemCount > 0) {
            int idx = this.topIndex;
            for (this.visibleCount = 0; idx < this.itemCount && y < maxBottom; idx++) {
                this.visibleCount++;
                Font font = this.fontElements[idx];
                Image img = this.imageElements[idx];
                int fontHeight = font.getHeight();
                int rowHeight = 4 + (img != null && img.getHeight() > fontHeight ? img.getHeight() : fontHeight);

                g.setFont(font);
                String label = this.stringElements[idx];

                if (idx == this.topIndex + this.selectedIndex - 1) {
                    // Draw highlight gradient
                    int colorVal = 0;
                    int step = this.calcGradientStep(colorHighlight);
                    for (int lineY = y + rowHeight; lineY > y; lineY--) {
                        g.setColor(colorVal -= step);
                        g.drawLine(0, lineY, width, lineY);
                    }

                    g.setColor(colorText);
                    int textWidth = DEFAULT_FONT.stringWidth(label);
                    int imgWidth = (img == null) ? 0 : img.getWidth();
                    if (15 + imgWidth + textWidth > width) {
                        if (!this.isScrolling) {
                            this.marqueeOffset = 0;
                            this.isScrolling = true;
                            new ScrollThread(this).start();
                        } else if (this.marqueeOffset < label.length() && 20 + imgWidth + DEFAULT_FONT.stringWidth(label.substring(this.marqueeOffset)) > width) {
                            label = label.substring(++this.marqueeOffset);
                        } else {
                            this.marqueeOffset = 0;
                        }
                    } else {
                        this.isScrolling = false;
                    }
                }

                int textY = y + (rowHeight - font.getHeight()) / 2;
                if (this.choiceType == 3) { // IMPLICIT
                    if (img != null) {
                        g.drawImage(img, 2, y + 2, Graphics.TOP | Graphics.LEFT);
                    }
                    g.drawString(label, 3 + (img == null ? 0 : img.getWidth()), textY, Graphics.TOP | Graphics.LEFT);
                } else if (this.choiceType == 1) { // EXCLUSIVE (Radio buttons)
                    g.drawArc(2, textY, fontHeight - 2, fontHeight - 2, 0, 360);
                    if (this.exclusiveSelectedIndex == idx) {
                        g.fillArc(7, textY + 5, fontHeight - 10, fontHeight - 10, 0, 360);
                    }
                    if (img != null) {
                        g.drawImage(img, fontHeight + 2, y + 2, Graphics.TOP | Graphics.LEFT);
                    }
                    g.drawString(label, fontHeight + 5 + (img == null ? 0 : img.getWidth()), y + (rowHeight - font.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
                } else { // MULTIPLE (Checkboxes)
                    g.drawRect(2, textY, fontHeight, fontHeight);
                    if (this.selectedElements[idx]) {
                        g.setColor(65280); // Green checkmark
                        g.drawLine(3, textY + fontHeight / 2, fontHeight / 2 + 1, textY + fontHeight - 1);
                        g.drawLine(fontHeight + 2, textY + 2, fontHeight / 2 + 1, textY + fontHeight - 1);
                    }
                    if (img != null) {
                        g.drawImage(img, rowHeight + 2, y + 2, Graphics.TOP | Graphics.LEFT);
                    }
                    g.setColor(colorText);
                    g.drawString(label, rowHeight + 5 + (img == null ? 0 : img.getWidth()), y + (rowHeight - font.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
                }

                y += rowHeight;
            }
        }

        int bottomBarY = height - this.itemHeight;
        if (!this.isFullScreen) {
            // Draw top bar
            int val = 0;
            int stepTop = this.calcGradientStep(colorBorder);
            for (int lineY = this.itemHeight; lineY >= 0; lineY--) {
                g.setColor(val += stepTop);
                g.drawLine(0, lineY, width, lineY);
            }

            // Draw bottom bar
            val = 0;
            int stepBottom = this.calcGradientStep(colorBar);
            for (int lineY = bottomBarY; lineY <= height; lineY++) {
                g.setColor(val += stepBottom);
                g.drawLine(0, lineY, width, lineY);
            }

            bottomBarY += 2;
            g.setColor(colorTitle);
            g.drawString(this.title, width >> 1, 2, Graphics.TOP | Graphics.HCENTER);

            String statusText;
            if (this.showIndexCount) {
                statusText = (this.getSelectedIndex() + 1) + "/" + this.itemCount;
            } else {
                DATE.setTime(System.currentTimeMillis());
                CALENDAR.setTime(DATE);
                StringBuffer sb = new StringBuffer();
                int hour = CALENDAR.get(Calendar.HOUR_OF_DAY);
                if (hour < 10) {
                    sb.append('0');
                }
                sb.append(hour).append(':');
                int min = CALENDAR.get(Calendar.MINUTE);
                if (min < 10) {
                    sb.append('0');
                }
                statusText = sb.append(min).toString();
            }
            g.drawString(statusText, width >> 1, bottomBarY, Graphics.TOP | Graphics.HCENTER);
        }

        int cmdCount = this.commands.size();
        if (cmdCount > 0) {
            if (!this.menuOpen) {
                if (!this.isFullScreen) {
                    if (cmdCount > 1) {
                        g.drawString(cmdCount > 2 ? "Tùy chọn" : ((Command) this.commands.elementAt(1)).getLabel(), 2, bottomBarY, Graphics.TOP | Graphics.LEFT);
                    }
                    g.drawString(((Command) this.commands.elementAt(0)).getLabel(), width - 2, bottomBarY, Graphics.TOP | Graphics.RIGHT);
                }
            } else {
                if (cmdCount > 1) {
                    g.drawString("Chọn", 2, bottomBarY, Graphics.TOP | Graphics.LEFT);
                    g.drawString("Hủy", width - 2, bottomBarY, Graphics.TOP | Graphics.RIGHT);
                }

                int maxCmdWidth = 0;
                for (int i = 1; i < this.commands.size(); i++) {
                    int w = DEFAULT_FONT.stringWidth(((Command) this.commands.elementAt(i)).getLabel());
                    if (w > maxCmdWidth) {
                        maxCmdWidth = w;
                    }
                }

                maxCmdWidth += 4;
                int menuItems = this.commands.size() - 1;
                int menuHeight = (this.itemHeight - 2) * menuItems + 3;
                int menuY = height - this.itemHeight - menuHeight - 1;

                g.setColor(16777215); // White border
                g.drawRect(0, menuY, maxCmdWidth, menuHeight);
                g.setColor(16514047); // Light gray background
                g.fillRect(1, menuY + 1, maxCmdWidth - 1, menuHeight - 1);

                g.setColor(0);
                menuY += menuHeight;
                for (int i = 1; i <= menuItems; i++) {
                    g.drawString(((Command) this.commands.elementAt(i)).getLabel(), 2, menuY - (this.itemHeight - 2) * i, Graphics.TOP | Graphics.LEFT);
                }

                // Highlight selected popup menu item
                g.setColor(0);
                g.fillRect(1, menuY - 1 - (this.itemHeight - 2) * (this.menuSelectedIndex + 1), maxCmdWidth - 1, this.itemHeight - 1);
                g.setColor(16514047);
                g.drawString(((Command) this.commands.elementAt(this.menuSelectedIndex + 1)).getLabel(), 2, menuY - (this.itemHeight - 2) * (this.menuSelectedIndex + 1), Graphics.TOP | Graphics.LEFT);
            }
        }
    }

    private int calcGradientStep(int color) {
        int r = (color & 0xFF) / this.itemHeight;
        int g = (color >> 8 & 0xFF) / this.itemHeight;
        int b = (color >> 16) / this.itemHeight;
        return r | (g << 8) | (b << 16);
    }

    public void setTitle(String title) {
        if (title != null) {
            this.title = title;
        }
    }
}
