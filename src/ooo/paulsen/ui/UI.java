package ooo.paulsen.ui;

import ooo.paulsen.Control;
import ooo.paulsen.Group;
import ooo.paulsen.Main;
import ooo.paulsen.io.serial.PSerialConnection;
import ooo.paulsen.ui.core.*;
import ooo.paulsen.utils.PSystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class UI {

    class WindowEventHandler implements WindowListener {

        public void windowClosing(WindowEvent evt) {
            if (isSystemTrayWorking)
                toggleMinimized(true);
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

    }

    public static final String TITLE = "Audio-Controller";
    public static boolean startMinimized = false;

    public PUIFrame f;

    BufferedImage img; // TrayIcon
    TrayIcon trayIcon;

    // Top-Bar
    private PUIElement settingsUI; // bottomButtons
    private PUIText serialButton;
    private PUICanvas c; // gets used in

    // Main-Menu
    private PUIList audioControlUI; // list of Audio-Controllers
    private PUIElement addAudioControlB;

    // Settings
    private PUIList groupPanel, processPanel;
    private PUIElement addGroupB, removeGroupB, refreshProcessesB;

    // variables get set once
    private boolean isSystemTrayWorking = false;

    // important changing variables
    private boolean isSettings = false;
    private int bHeight, space, textHeight;
    private String selectedGroup = null;

    // Render Desktop-Icon
    public static BufferedImage getApplicationImage(int size, Color mainColor, Color bg) {

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        Graphics g = img.getGraphics();

        {

            g.setColor(bg);
            g.fillRect(0, 0, size, size);

            int offset = size / 20;

            g.setColor(mainColor);
            g.fillArc(-size - offset, 0, size * 2, size, -50, 100);
            g.setColor(bg);
            g.fillArc((int) (-size * 0.85) - offset, 0, (int) (size * 2 * 0.85), size, -60, 120);
            g.setColor(mainColor);
            g.fillArc((int) (-size * 0.75) - offset, 0, (int) (size * 2 * 0.75), size, -30, 60);
            g.setColor(bg);
            g.fillArc((int) (-size * 0.6) - offset, 0, (int) (size * 2 * 0.6), size, -60, 120);

            g.setColor(mainColor);
            g.fillOval(size / 20, size / 4, size / 3, size / 2);

            int[][] poly = new int[2][4];
            poly[0][0] = size / 20 + size / 3 / 2;
            poly[1][0] = size / 4;
            poly[0][1] = size / 5 * 2;
            poly[1][1] = size / 8;
            poly[0][2] = size / 5 * 2;
            poly[1][2] = size - size / 8;
            poly[0][3] = size / 20 + size / 3 / 2;
            poly[1][3] = size - size / 4;

            g.fillPolygon(poly[0], poly[1], poly[0].length);
        }

        ImageFilter filter = new RGBImageFilter() {
            int transparentColor = bg.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == transparentColor) {
                    return 0x00FFFFFF & rgb;
                } else {
                    return rgb;
                }
            }
        };

        ImageProducer filteredImgProd = new FilteredImageSource(img.getSource(), filter);
        Image transparentImg = Toolkit.getDefaultToolkit().createImage(filteredImgProd);

        BufferedImage b_img = new BufferedImage(transparentImg.getWidth(null), transparentImg.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);

        b_img.getGraphics().drawImage(transparentImg, 0, 0, null);

        return b_img;
    }

    public UI() {

        f = new PUIFrame(TITLE, 1300, 600, false);

        // Main/Text
        PUIElement.setDefaultColor(1, new Color(240, 240, 240));

        PUIElement.setDefaultColor(4, new Color(75, 75, 75));

        PUIElement.setDefaultColor(10, new Color(44, 183, 14, 255));
        PUIElement.setDefaultColor(11, new Color(222, 40, 40, 255));
        PUIElement.setDefaultColor(12, new Color(222, 149, 40, 255));

        PUIElement.setDefaultColor(20, new Color(0, 0, 0, 20));
        PUIElement.setDefaultColor(21, new Color(255, 255, 255, 5));

        try {
            img = ImageIO.read(new File("Audio.png"));
        } catch (SecurityException | IOException e) {
            System.out.println("[UI] :: No Application-Image available");
        }

        // set self-drawn image
        if (img == null) {
            img = getApplicationImage(265, Color.white, Color.black);
        }

        if (img != null) {
            f.setIconImage(img);
        }

        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setMinimumSize(new Dimension(600, 400));
        f.addWindowListener(new WindowEventHandler());

        initElements();
        initUpdate();
        initSystemTray();

        updateControlList();
        updateGroupList();
        updateProcessList();

        f.updateElements();

        // load minimized-setting
        if (startMinimized && isSystemTrayWorking) {
            toggleMinimized(true);
        } else {
            f.setVisible(true);
        }
    }

    public void toggleMinimized() {

        boolean b;
        if (SystemTray.isSupported() && isSystemTrayWorking)
            b = f.isVisible();
        else
            b = f.getState() == JFrame.NORMAL;

        toggleMinimized(b);
    }

    public void toggleMinimized(boolean b) {

        if (SystemTray.isSupported() && isSystemTrayWorking) {
            System.out.println("[UI] :: Minimized " + b);
            f.setVisible(!b);
        } else { // No Systemtray
            if (b) {
                f.setState(JFrame.ICONIFIED);
                System.out.println("[UI] :: Iconified");
            } else {
                f.setState(JFrame.NORMAL);
            }
        }
    }

    public void initSystemTray() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            PopupMenu popup = new PopupMenu();

            MenuItem show = new MenuItem("SHOW/MINIMIZE");
            show.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    toggleMinimized();
                }
            });
            popup.add(show);

            MenuItem close = new MenuItem("-EXIT-");
            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Main.close();
                }
            });
            popup.add(close);

            try {

                int size = (int) SystemTray.getSystemTray().getTrayIconSize().getHeight() * 4;
                if (PSystem.getOSType() == PSystem.OSType.WINDOWS)
                    img = getApplicationImage(size, Color.white, Color.black);
                else
                    img = getApplicationImage(size, Color.darkGray, Color.white);

                trayIcon = new TrayIcon(img, "Serial Audio Controller", popup);
                trayIcon.addActionListener(show.getActionListeners()[0]);
                trayIcon.setToolTip("Audio-Controller");
                trayIcon.setImageAutoSize(true);

                tray.add(trayIcon);
                isSystemTrayWorking = true;
            } catch (Exception e) {
                e.printStackTrace();
                isSystemTrayWorking = false;
                f.sendUserInfo("Couldn't load System Tray!");
            }
        }
    }

    public void initElements() {

        // Buttons

        settingsUI = new PUIElement(f);
        settingsUI.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement arg0) {
                isSettings = !isSettings;
                f.updateElements();
                System.out.println("[UI] :: pressed settings; isSettings: " + isSettings);
            }
        });
        settingsUI.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                float space = ((float) Math.min(w, h)) / 7f;

                g.setColor(settingsUI.getTextColor());
                g.fillRoundRect((int) (x + space), (int) (y + space), (int) (w - space * 2), (int) space, 5, 5);
                g.fillRoundRect((int) (x + space), (int) (y + space * 3), (int) (w - space * 2), (int) space, 5, 5);
                g.fillRoundRect((int) (x + space), (int) (y + space * 5), (int) (w - space * 2), (int) space, 5, 5);
            }
        });

        serialButton = new PUIText(f, "-");
        serialButton.setBackgroundColor(serialButton.getDefaultColor(4));
        serialButton.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement that) {

                System.out.println("[UI] :: pressed serialButton");

                if (Main.am.isSerialConnected()) {
                    Main.am.disconnectSerial();
                    updateCurrentSerialConnection();

                    return;
                }

                ArrayList<String> ports = new ArrayList<>();
                ports.add("");
                ports.addAll(Arrays.asList(PSerialConnection.getSerialPorts()));
                int index = f.getUserSelection("Choose your USB-Port", ports);


                if (index == 0) // escaped
                    return;

                if (index == -1) {
                    f.sendUserWarning("Select a valid Port");
                    return;
                }

                System.out.println("[UI] :: serialButton selected port: " + ports.get(index));

                if (Main.am.connectToSerial(ports.get(index))) {
                    updateCurrentSerialConnection();
                    System.out.println("[UI] :: Connected to " + ports.get(index));
                } else {
                    f.sendUserInfo("Could not connect to " + ports.get(index));
                    System.out.println("[UI] :: Could not connect to " + ports.get(index));
                }
            }
        });

        // Main Menu
        // // AudioControl
        audioControlUI = new PUIList(f);
        audioControlUI.setAlignment(PUIElement.ElementAlignment.HORIZONTAL);
        audioControlUI.setShowedElements(4);
        audioControlUI.setSliderWidth(f.getHeight() / 20);
        audioControlUI.setBackgroundColor(PUIElement.getDefaultColor(20));

        // // addAudioControlB
        addAudioControlB = new PUIElement(f);
        addAudioControlB.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                int space = Math.min(w, h) / 6;

                g.setColor(addAudioControlB.getTextColor());
                g.fillRoundRect(x + space, y + h / 2 - space / 2, w - space * 2, space, 5, 5);
                g.fillRoundRect(x + w / 2 - space / 2, y + space, space, h - space * 2, 5, 5);
            }
        });

        addAudioControlB.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement that) {
                System.out.println("[UI] :: pressed addAudioControlB");
                String input = f.getUserInput("Input Control-Name", "");

                if (Control.getControl(input) != null) {
                    f.sendUserWarning("Control \"" + input + "\" already exists!");
                    return;
                }

                if (input != null && !input.isEmpty()) {
                    new Control(input);
                }
            }
        });

        // Settings
        // // Panels
        groupPanel = new PUIList(f);
        groupPanel.setShowedElements(6);

        processPanel = new PUIList(f);
        processPanel.setShowedElements(10);

        // // Buttons
        addGroupB = new PUIElement(f);
        addGroupB.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement that) {
                System.out.println("[UI] :: pressed addGroupB");

                String input = f.getUserInput("Create a new Group:", "Group1");
                if (input == null)
                    return;

                if (Group.getGroup(input) != null) {
                    f.sendUserWarning("Group \"" + input + "\" already exists!");
                    return;
                }

                new Group(input);

                updateGroupList();

            }
        });
        addGroupB.setDraw(addAudioControlB.getDraw());

        removeGroupB = new PUIElement(f);
        removeGroupB.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement that) {
                if (selectedGroup != null) {
                    System.out.println("[UI] :: pressed removeGroupB");
                    if (f.getUserConfirm("Remove \"" + selectedGroup + "\" ?", "Group")) {
                        Group.removeGroup(selectedGroup);
                        selectedGroup = null;
                        updateGroupList();
                    }
                }
            }
        });
        removeGroupB.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                int space = Math.min(w, h) / 6;

                g.setColor(removeGroupB.getTextColor());
                g.fillRoundRect(x + space, y + h / 2 - space / 2, w - space * 2, space, 5, 5);
            }
        });

        refreshProcessesB = new PUIElement(f);
        refreshProcessesB.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement puiElement) {
                System.out.println("[UI] :: pressed refreshProcessesB");
                Main.am.refreshProcesses();
                updateProcessList();
            }
        });
        refreshProcessesB.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {

                int space = (int) (Math.min(w, h) * 0.3);
                int space2 = Math.min(w, h) / 6;

                g.setColor(refreshProcessesB.getTextColor());
                g.fillOval(x + space2, y + space2, w - space2 * 2, h - space2 * 2); // outer circle
                g.setColor(new Color(64, 64, 64));
                g.fillOval(x + space, y + space, w - space * 2, h - space * 2); // inner circle
                g.fillRect(x + space, y + h / 2, w / 2 - space + 1, h / 2); // cut off down-left-section

                int[][] poly = new int[2][3];
                poly[0][0] = (int) (x + w * 0.6); //x
                poly[1][0] = (int) (y + h * 0.6);
                poly[0][1] = (int) (x + w * 0.6); //x
                poly[1][1] = (int) (y + h * 0.9);
                poly[0][2] = (int) (x + w * 0.4); //x
                poly[1][2] = y + h - h / 4;
                g.setColor(refreshProcessesB.getTextColor());
                g.fillPolygon(poly[0], poly[1], poly[0].length);
            }
        });

        c = new PUICanvas(f, new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {

                updateRotaryControls();

                g.setColor(PUIElement.getDefaultColor(20));
                g.fillRect(0, 0, w + 10, bHeight + space / 2);
                g.setColor(PUIElement.getDefaultColor(20));
                g.fillRect(0, bHeight + space / 2, w + 10, 2);

                g.setColor(PUIElement.getDefaultColor(1));
                g.setFont(new Font("Arial", Font.PLAIN, space));
                g.drawString(Main.version, 0, h);

                g.setFont(new Font("Arial", Font.PLAIN, bHeight / 2));

                // Top-Bar
                int textY = bHeight / 2 - space / 4;
                // // USB
                g.setColor(Main.am.isSerialConnected() ? PUIElement.getDefaultColor(10) : PUIElement.getDefaultColor(11));
                g.fillOval(space / 2, space / 2, bHeight / 2 - space, bHeight / 2 - space);
                g.drawString("USB", bHeight / 2, textY);
                // // Audio
                g.setColor(Main.am.isAudioConnected() ? PUIElement.getDefaultColor(10) : PUIElement.getDefaultColor(11));
                g.fillOval(space / 2, space / 2 + bHeight / 2, bHeight / 2 - space, bHeight / 2 - space);
                g.drawString("Audio", bHeight / 2, bHeight / 2 + textY);


                g.setFont(new Font("Arial", Font.PLAIN, textHeight));

                // // Port
                g.setColor(serialButton.getTextColor());
                g.drawString("Port:", w / 2 + space, textHeight - space / 4 + space);

                // Menu
                if (isSettings) {

                    g.setColor(PUIElement.getDefaultColor(1));
                    g.drawString("Groups", space, space / 2 + bHeight + textHeight);
                    g.drawString("Programs", w / 2 + space, space / 2 + bHeight + textHeight);

                } else { // Main-Screen
                    g.setColor(PUIElement.getDefaultColor(1));
                    g.drawString("Controls", space, space / 2 + bHeight + textHeight);
                }
            }
        }, -1);


        //// NO paintOvers!!!!
        for (PUIElement e : PUIElement.registeredElements) {
            e.doPaintOverOnHover(false);
            e.doPaintOverOnPress(false);
        }
    }

    public void initUpdate() {
        f.setUpdateElements(new PUIUpdatable() {
            @Override
            public void update(int w, int h) {

                bHeight = Math.min(w, h) / 8;
                space = bHeight / 5;
                textHeight = (int) (bHeight * 0.6f);


                if (!isSettings) {

                    // AudioControl
                    audioControlUI.setShowedElements(5);
                    audioControlUI.setBounds(space, bHeight + space + textHeight, w - space * 2, h - space * 2 - bHeight - textHeight);
                    audioControlUI.setSliderWidth(h / 20);
                    audioControlUI.updateElements();
                    // //

                    addAudioControlB.setBounds(w - space - textHeight, bHeight + space, textHeight, textHeight);

                    //
                    audioControlUI.setEnabled(true);
                    addAudioControlB.setEnabled(true);
                    //
                    groupPanel.setEnabled(false);
                    processPanel.setEnabled(false);
                    addGroupB.setEnabled(false);
                    removeGroupB.setEnabled(false);
                    refreshProcessesB.setEnabled(false);

                } else { // settings

                    groupPanel.setSliderWidth(w / 30);
                    groupPanel.setBounds(space, bHeight + space + textHeight, w / 2 - space * 2, h - space * 2 - bHeight - textHeight);
                    processPanel.setSliderWidth(w / 30);
                    processPanel.setBounds(w / 2 + space, bHeight + space + textHeight, w / 2 - space * 2, h - space * 2 - bHeight - textHeight);

                    addGroupB.setBounds(w / 2 - space - textHeight * 2, bHeight + space, textHeight, textHeight);
                    removeGroupB.setBounds(w / 2 - space - textHeight, bHeight + space, textHeight, textHeight);

                    refreshProcessesB.setBounds(w - space - textHeight, bHeight + space, textHeight, textHeight);

                    //
                    groupPanel.setEnabled(true);
                    processPanel.setEnabled(true);
                    addGroupB.setEnabled(true);
                    removeGroupB.setEnabled(true);
                    refreshProcessesB.setEnabled(true);
                    //
                    audioControlUI.setEnabled(false);
                    addAudioControlB.setEnabled(false);
                }

                settingsUI.setBounds(w - bHeight, 0, bHeight, bHeight);
                serialButton.setBounds((int) (w / 2 + textHeight * 2.8f), space, (int) (w / 2 - textHeight * 2.8f - bHeight - space), bHeight - space);
            }
        });
    }

    public synchronized void sendUserPopUp(String title, String message) {
        if (trayIcon != null && isSystemTrayWorking) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    public synchronized void updateControlList() {
        ArrayList<PUIElement> elems = new ArrayList<>();

        for (Control c : Control.getControls()) {
            elems.add(new ControlElement(c.getName(), f));
        }

        // refresh List
        audioControlUI.clearElements();
        audioControlUI.addAllElements(elems);

        f.updateElements();
    }

    public synchronized void updateGroupList() {
        ArrayList<PUIElement> elems = new ArrayList<>();

        for (Group g : Group.groups) {
            PUIText t = new PUIText(f, g.getName()) {
                @Override
                public void draw(Graphics2D g) {
                    if (!((boolean) getMetadata())) {
                        setBackgroundColor(getDefaultColor(12));
                    } else {
                        setBackgroundColor(getDefaultColor(10));
                    }
                    super.draw(g);
                }
            };

            t.setMetadata(false); // here, metadata represents if it's selected in the UI

            t.addActionListener(new PUIAction() {
                @Override
                public void run(PUIElement that) {

                    for (PUIElement e : groupPanel.getElements())
                        if (e != that)
                            e.setMetadata(false);

                    that.setMetadata(!(boolean) (that.getMetadata()));

                    if ((boolean) that.getMetadata())
                        selectedGroup = ((PUIText) that).getText();
                    else
                        selectedGroup = null;

                    System.out.println("[UI] :: Clicked on Group: " + ((PUIText) that).getText() + "; Selected: " + that.getMetadata());

                    f.repaint();
                }
            });

            elems.add(t);
        }

        // refresh List
        groupPanel.clearElements();
        groupPanel.addAllElements(elems);

        f.updateElements();
    }

    public synchronized void updateProcessList() {
        HashSet<String> processSet = new HashSet<>();
        HashSet<String> activeProcessSet = new HashSet<>();

        // Currently active processes that have played audio since start
        if (Main.am.getProcesses() != null) activeProcessSet.addAll(Main.am.getProcesses());

        for (Group g : Group.groups) {
            for (String p : g.getProcesses()) {
                processSet.addAll(g.getProcesses());
            }
        }

        ArrayList<PUIElement> elems = new ArrayList<>();

        // active processes
        for (String process : activeProcessSet) {
            elems.add(getProcessElement(process, true));
            //System.out.println("cached: " + process);
        }

        for (String process : processSet) {
            if (!activeProcessSet.contains(process)) {
                elems.add(getProcessElement(process, false));
                //System.out.println("saved: " + process);
            }
        }

        // refresh List
        processPanel.clearElements();

        processPanel.addAllElements(elems);

        f.updateElements();
    }

    public PUIElement getProcessElement(String processName, boolean isActive) {
        PUIElement e = new PUIText(f, processName);
        e.setMetadata(isActive);
        e.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                if (!e.isEnabled())
                    return;

                Object o = e.getMetadata();
                boolean isActive = false;
                if (o != null && o instanceof Boolean) {
                    isActive = (boolean) o;
                }


                Group group = Group.getGroup(selectedGroup);

                if (group != null && group.getProcesses().contains(((PUIText) e).getText())) {
                    g.setColor(PUIElement.getDefaultColor(10));
                } else {
                    if(Main.getSavedProcesses().contains(processName))
                        g.setColor(PUIElement.getDefaultColor(12));
                    else
                        g.setColor(PUIElement.getDefaultColor(11));
                }

                g.fillRoundRect(x, y, w, h, 15, 15);

                if (!isActive) {
                    g.setColor(new Color(0, 0, 0, 100));
                    g.fillRoundRect(x, y, w, h, 15, 15);
                }

            }
        });

        e.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement puiElement) {
                System.out.println("Clicked " + ((PUIText) puiElement).getText());

                Group g = Group.getGroup(selectedGroup);

                if (g != null) {
                    String name = ((PUIText) puiElement).getText();

                    if (g.getProcesses().contains(name)) { // Group already has this Process

                        if (f.getUserConfirm("Remove \"" + name + "\" ?", "Group: " + g.getName())) {
                            g.removeProcess(name);
                            updateProcessList();
                        }

                    } else {
                        g.addProcess(name);
                        updateProcessList();
                    }
                }

            }
        });

        return e;
    }

    public synchronized void updateRotaryControls() {

        for (PUIElement e : audioControlUI.getElements()) {

            ControlElement ce = ((ControlElement) e);
            Control c = Control.getControl(ce.getName());

            if (c != null) {
                ce.updateRotaryValue(c.getVolume());
            }
        }
    }

    public synchronized void updateCurrentSerialConnection() {
        if (Main.am.isSerialConnected()) {
            serialButton.setText(Main.am.getPortName());
            f.setTitle(TITLE + " - USB: " + Main.am.getPortName());
        } else {
            serialButton.setText("-");
            f.setTitle(TITLE);
        }
        f.repaint();

        // TODO replace for windows
        if (PSystem.getOSType() == PSystem.OSType.WINDOWS)
            sendUserPopUp("Not Supported", "Is not supported yet");
        // Maybe add library for Linux/Gnome-support
    }

}
