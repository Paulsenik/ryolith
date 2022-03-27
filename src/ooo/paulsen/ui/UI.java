package ooo.paulsen.ui;

import ooo.paulsen.Control;
import ooo.paulsen.Group;
import ooo.paulsen.Main;
import ooo.paulsen.io.serial.PSerialConnection;
import ooo.paulsen.ui.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class UI {

    class WindowEventHandler implements WindowListener {

        public void windowClosing(WindowEvent evt) {
            if (f.getUserConfirm("Really Close Audio Controller", "Audio Controller")) {
                Main.exitAll();
            }
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
            if (isSystemTrayWorking) {
                f.setVisible(false);
            } else
                f.setVisible(true);
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

    }

    Image img; // TrayIcon

    public PUIFrame f;

    // Top-Bar
    private PUIElement minimizeUI, settingsUI; // bottomButtons
    private PUIText serialButton;
    private PUICanvas c; // gets used in

    // Main-Menu
    private PUIScrollPanel audioControlUI; // list of Audio-Controllers
    private PUIElement addAudioControlB;

    // Settings
    private PUIScrollPanel groupPanel, processPanel;
    private PUIElement addGroupB, removeGroupB, refreshProcessesB;

    // variables get set once
    private boolean hasInit = false, isSystemTrayWorking = false;

    // important changing variables
    private boolean isSettings = false;
    private int bHeight, space, textHeight;

    public UI() {

        // PUI - DarkMODE
        PUIElement.darkUIMode = true;

        f = new PUIFrame("Serial Audio Controller", 1300, 600);

        try {

            if (!new File(Main.SAVEFOLDER + "/Audio.png").exists()) {
                System.out.println("Couldn't find Application-Image");
                f.sendUserInfo("Couldn't find Application-Image");
            } else {
                img = Toolkit.getDefaultToolkit().getImage(Main.SAVEFOLDER + "/Audio.png");
            }

        } catch (SecurityException e) {
            System.out.println("Couldn't set Application-Image");
            f.sendUserInfo("Couldn't set Application-Image");
        }

        if (img != null)
            f.setIconImage(img);

        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setMinimumSize(new Dimension(600, 400));
        f.addWindowListener(new WindowEventHandler());

        initSystemTray();
        initElements();
        initUpdate();

        for (int i = 0; i < 20; i++) {
            PUIElement e = new PUIText(f, "" + i);
            e.addActionListener(new PUIAction() {
                @Override
                public void run(PUIElement puiElement) {
                    System.out.println(((PUIText) puiElement).getText());
                }
            });
            processPanel.addElement(e);
        }

        updateControlList();
        updateGroupList();
        updateProcessList();

        f.updateElements();
        hasInit = true;
    }


    public void initSystemTray() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();


            PopupMenu popup = new PopupMenu();

            MenuItem show = new MenuItem("SHOW/MINIMIZE");
            show.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    f.setAlwaysOnTop(true);

                    if (isSystemTrayWorking) {
                        f.setState(JFrame.NORMAL);
                        f.setVisible(!f.isVisible());
                    } else {
                        if (f.getState() == JFrame.NORMAL)
                            f.setState(JFrame.ICONIFIED);
                        else
                            f.setState(JFrame.NORMAL);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                    }
                    f.setAlwaysOnTop(false);
                }
            });
            popup.add(show);

            MenuItem close = new MenuItem("-EXIT-");
            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
                }
            });
            popup.add(close);

            try {
                TrayIcon trayIcon = new TrayIcon(img, "Serial Audio Controller", popup);
                trayIcon.addActionListener(show.getActionListeners()[0]);

                tray.add(trayIcon);
                isSystemTrayWorking = true;
            } catch (Exception e) {
                isSystemTrayWorking = false;
                f.sendUserInfo("Couldn't load System Tray!");
            }
        }
    }

    public void initElements() {

        // Buttons
        minimizeUI = new PUIElement(f);
        minimizeUI.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement arg0) {
                System.out.println("Minimize");
                if (SystemTray.isSupported() && isSystemTrayWorking) {
                    f.setVisible(false);
                } else {
                    if (f.getState() == JFrame.NORMAL) {
                        f.setState(JFrame.ICONIFIED);
                        System.out.println("iconified");
                    } else
                        f.setState(JFrame.NORMAL);
                }
            }
        });
        minimizeUI.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                g.setColor(PUIElement.darkBG_1);
                g.fillRect(x, y, w, h);
                g.setColor(PUIElement.darkOutline);
                g.drawRect(x, y, w, h);

                int space = Math.min(w, h) / 6;

                g.setColor(Color.white);
                g.fillRect(x + space, y + h - space * 2, w - space * 2, space);
            }
        });

        settingsUI = new PUIElement(f);
        settingsUI.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement arg0) {
                isSettings = !isSettings;
                f.updateElements();
            }
        });
        settingsUI.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                g.setColor(PUIElement.darkBG_1);
                g.fillRect(x, y, w, h);
                g.setColor(PUIElement.darkOutline);
                g.drawRect(x, y, w, h);

                float space = ((float) Math.min(w, h)) / 7f;

                g.setColor(Color.white);
                g.fillRect((int) (x + space), (int) (y + space), (int) (w - space * 2), (int) space);
                g.fillRect((int) (x + space), (int) (y + space * 3), (int) (w - space * 2), (int) space);
                g.fillRect((int) (x + space), (int) (y + space * 5), (int) (w - space * 2), (int) space);
            }
        });

        serialButton = new PUIText(f, "-");
        serialButton.setTextColor(new Color(20, 116, 171));
        serialButton.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement that) {

                if (Main.am.isSerialConnected()) {
                    Main.am.disconnectSerial();
                    serialButton.setText("-");
                    f.updateElements();

                    return;
                }

                String[] ports = PSerialConnection.getSerialPorts();
                int index = f.getUserSelection("Choose your USB-Port", ports);

                if (index == -1) {
                    f.sendUserWarning("Select a valid Port");
                    return;
                }

                if (Main.am.connectToSerial(ports[index])) {
                    serialButton.setText(ports[index]);
                    f.updateElements();
                } else {
                    f.sendUserInfo("Could not connect to " + ports[index]);
                }
            }
        });

        // Main Menu
        // // AudioControl
        audioControlUI = new PUIScrollPanel(f);
        audioControlUI.setAlignment(PUIElement.ElementAlignment.HORIZONTAL);
        audioControlUI.setShowedElements(4);
        audioControlUI.setSliderWidth(f.getHeight() / 20);

        // // addAudioControlB
        addAudioControlB = new PUIElement(f);
        addAudioControlB.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                g.setColor(PUIElement.darkBG_1);
                g.fillRect(x, y, w, h);
                g.setColor(PUIElement.darkOutline);
                g.drawRect(x, y, w, h);

                int space = Math.min(w, h) / 6;

                g.setColor(Color.white);
                g.fillRect(x + space, y + h / 2 - space / 2, w - space * 2, space);
                g.fillRect(x + w / 2 - space / 2, y + space, space, h - space * 2);
            }
        });
        addAudioControlB.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement that) {
                String input = f.getUserInput("Input Control-Name", "");

                if (Main.getControl(input) != null) {
                    f.sendUserWarning("Control \"" + input + "\" already exists!");
                    return;
                }

                if (input != null && !input.isEmpty()) {
                    Main.createControl(input);
                }
            }
        });

        // Settings
        // // Panels
        groupPanel = new PUIScrollPanel(f);
        groupPanel.setShowedElements(6);

        processPanel = new PUIScrollPanel(f);
        processPanel.setShowedElements(10);

        // // Buttons
        addGroupB = new PUIElement(f);
        addGroupB.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement that) {

                String input = f.getUserInput("Create a new Group:", "Group1");
                if(input == null)
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
                System.out.println("TODO - remove Group");
            }
        });
        removeGroupB.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                g.setColor(PUIElement.darkBG_1);
                g.fillRect(x, y, w, h);
                g.setColor(PUIElement.darkOutline);
                g.drawRect(x, y, w, h);

                int space = Math.min(w, h) / 6;

                g.setColor(Color.white);
                g.fillRect(x + space, y + h / 2 - space / 2, w - space * 2, space);
            }
        });

        refreshProcessesB = new PUIElement(f);
        refreshProcessesB.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement puiElement) {
                Main.am.refreshProcesses();
                updateProcessList();
            }
        });
        refreshProcessesB.setDraw(new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {
                g.setColor(PUIElement.darkBG_1);
                g.fillRect(x, y, w, h);
                g.setColor(PUIElement.darkOutline);
                g.drawRect(x, y, w, h);

                int space = (int) (Math.min(w, h) * 0.3);
                int space2 = Math.min(w, h) / 6;

                g.setColor(Color.white);
                g.fillOval(x + space2, y + space2, w - space2 * 2, h - space2 * 2); // outer circle
                g.setColor(PUIElement.darkBG_1);
                g.fillOval(x + space, y + space, w - space * 2, h - space * 2); // inner circle
                g.fillRect(x + space, y + h / 2, w / 2 - space + 1, h / 2); // cut off down-left-section

                int[][] poly = new int[2][3];
                poly[0][0] = (int) (x + w * 0.6); //x
                poly[1][0] = (int) (y + h * 0.6);
                poly[0][1] = (int) (x + w * 0.6); //x
                poly[1][1] = (int) (y + h * 0.9);
                poly[0][2] = (int) (x + w * 0.4); //x
                poly[1][2] = y + h - h / 4;
                g.setColor(Color.white);
                g.fillPolygon(poly[0], poly[1], poly[0].length);
            }
        });

        c = new PUICanvas(f, new PUIPaintable() {
            @Override
            public void paint(Graphics2D g, int x, int y, int w, int h) {

                updateRotaryControls();

                g.setFont(new Font("Arial", Font.PLAIN, bHeight / 2));

                // Top-Bar
                int textY = bHeight / 2 - space / 4;
                // // USB
                g.setColor(Main.am.isSerialConnected() ? new Color(4, 189, 24) : new Color(231, 10, 53));
                g.fillOval(bHeight + space / 2, space / 2, bHeight / 2 - space, bHeight / 2 - space);
                g.drawString("USB", bHeight + bHeight / 2, textY);
                // // Audio
                g.setColor(Main.am.isAudioConnected() ? new Color(4, 189, 24) : new Color(231, 10, 53));
                g.fillOval(bHeight + space / 2, space / 2 + bHeight / 2, bHeight / 2 - space, bHeight / 2 - space);
                g.drawString("Audio", bHeight + bHeight / 2, bHeight / 2 + textY);


                g.setFont(new Font("Arial", Font.PLAIN, textHeight));

                // // Port
                g.setColor(serialButton.getTextColor());
                g.drawString("Port:", w / 2 + space, textHeight - space / 4 + space);

                // Menu
                if (isSettings) {

                    g.setColor(Color.white);
                    g.drawString("Groups", space, space / 2 + bHeight + textHeight);
                    g.drawString("Programs", w / 2 + space, space / 2 + bHeight + textHeight);

                } else { // Main-Screen
                    g.setColor(Color.white);
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
                    audioControlUI.setShowedElements(w / 400 + 1);
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

                minimizeUI.setBounds(0, 0, bHeight, bHeight);
                settingsUI.setBounds(w - bHeight, 0, bHeight, bHeight);
                serialButton.setBounds((int) (w / 2 + textHeight * 2.8f), space, (int) (w / 2 - textHeight * 2.8f - bHeight - space), bHeight - space);
            }
        });
    }

    public synchronized void updateControlList() {
        ArrayList<PUIElement> elems = new ArrayList<>();

        for (Control c : Main.getControls()) {
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
                    if ((boolean) getMetadata() == false) {
                        setBackgroundColor(new Color(210, 150, 53));
                    } else {
                        setBackgroundColor(new Color(45, 155, 45));
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
                    System.out.println("Click on Group : " + ((PUIText) that).getText() + " selected = " + that.getMetadata());

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

        for (Group g : Group.groups)
            processSet.addAll(g.getProcesses());

        if (Main.am.getProcesses() != null)
            processSet.addAll(Main.am.getProcesses());

        ArrayList<PUIElement> elems = new ArrayList<>();
        for (String process : processSet) {
            PUIElement e = new PUIText(f, process) {
                @Override
                public void draw(Graphics2D g) {
                    // TODO - change color according to selected Group
                    super.draw(g);
                }
            };

            e.addActionListener(new PUIAction() {
                @Override
                public void run(PUIElement puiElement) {
                    System.out.println("Clicked " + ((PUIText) puiElement).getText());
                    // TODO
                }
            });

            elems.add(e);
        }

        // refresh List
        processPanel.clearElements();
        processPanel.addAllElements(elems);

        f.updateElements();
    }

    public synchronized void updateRotaryControls() {

        for (PUIElement e : audioControlUI.getElements()) {

            ControlElement ce = ((ControlElement) e);
            Control c = Main.getControl(ce.getName());

            if (c != null) {
                ce.updateRotaryValue(c.getVolume());
            }
        }
    }

}
