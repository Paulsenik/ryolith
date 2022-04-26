package ooo.paulsen;

import ooo.paulsen.audiocontrol.AudioManager;
import ooo.paulsen.io.PDataStorage;
import ooo.paulsen.io.PFolder;
import ooo.paulsen.ui.UI;
import ooo.paulsen.utils.PSystem;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Paul
 * @since 2021/08/04 => 2022/03/17
 */
public class Main {

    public static final String version = "b2.2.1";
    private static final boolean devMode = false;

    public static final String saveDir = System.getProperty("user.home") + PSystem.getFileSeparator() + ".jaudiocontroller";

    public static UI ui;
    public static AudioManager am;

    // Only private to not accidentally be instanced
    private Main() {
    }

    public static void main(String[] args) {

        initVariables();

        if (!devMode) {

            PFolder.createFolder(saveDir);

            PFolder.createFolder(saveDir + PSystem.getFileSeparator() + "Logs");

            // Set Console out
            try {
                PrintStream out = new PrintStream(new FileOutputStream(saveDir + PSystem.getFileSeparator() + "Logs" + PSystem.getFileSeparator() + "log_" + System.currentTimeMillis()));
                System.setOut(out);
                System.setErr(out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            am = new AudioManager();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Startup-Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        ui = new UI();

        // autoconnect
        if (AudioManager.doAutoConnect && AudioManager.lastPort != null && !AudioManager.lastPort.isEmpty()) {
            if (am.connectToSerial(AudioManager.lastPort))
                System.out.println("[Main] :: Auto-Connected to " + AudioManager.lastPort);
            ui.updateCurrentSerialConnection();
        }

        // AutoSave every 10 minutes
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("[Main] :: Auto-Saving...");
                saveVariables();
            }
        }, 5 * 60 * 1000, 10 * 60 * 1000);
    }

    public static void setControlVolume(String controlName, float volume) {
        for (Control c : Control.getControls())
            if (c != null && c.getName().equals(controlName)) {
                c.setVolume(volume);
                ui.f.repaint();
            }
    }

    /**
     * Opens UserPopup for closing the Program
     */
    public static void close(){
        if (ui.f.getUserConfirm("Really Close Audio Controller", "Audio Controller")) {
            ui.f.dispose();
            Main.exitAll();
        }
    }

    /**
     * Saves and exits program
     */
    public static void exitAll() {
        System.out.println("[Main] :: exitAll()");
        try {
            saveVariables();
        } catch (Exception e) {
            e.printStackTrace();
            ui.f.sendUserError("An Error Occured!\nPlease check consoleOut.txt !!!");
        }

        // Disconnects Serial and stops AudioControl-Thread
        am.stop();

        System.exit(0);
    }

    public static void initVariables() {

        System.out.println("[Main] :: initVariables() ...");

        // Groups
        String groupFiles[] = PFolder.getFiles(saveDir + PSystem.getFileSeparator() + "Groups", "cfg");
        if (groupFiles != null)
            for (String group : groupFiles) {
                PDataStorage storage = new PDataStorage();
                try {
                    storage.read(group);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    continue;
                }

                try {
                    String name = storage.getString("name");
                    int processSize = storage.getInteger("ps");

                    Group g = new Group(name);
                    for (int i = 0; i < processSize; i++) {
                        String pName = storage.getString("p" + i);
                        if (pName != null)
                            g.addProcess(pName);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    continue;
                }
            }

        // Controls
        String controlFiles[] = PFolder.getFiles(saveDir + PSystem.getFileSeparator() + "Controls", "cfg");
        if (controlFiles != null)
            for (String control : controlFiles) {
                PDataStorage storage = new PDataStorage();
                try {
                    storage.read(control);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    continue;
                }

                try {
                    String name = storage.getString("name");
                    float volume = storage.getFloat("vol");

                    Control c = new Control(name, volume);

                    int groupSize = storage.getInteger("gs");

                    for (int i = 0; i < groupSize; i++) {
                        String gName = storage.getString("g" + i);
                        if (gName != null && Group.getGroup(gName) != null)
                            c.addGroup(Group.getGroup(gName));
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    continue;
                }

            }

        if (new File(saveDir + PSystem.getFileSeparator() + "settings.cfg").exists()) {
            PDataStorage settings = new PDataStorage();
            try {
                settings.read(saveDir + PSystem.getFileSeparator() + "settings.cfg");

                try {
                    AudioManager.doAutoConnect = settings.getBoolean("autoConnect");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                try {
                    AudioManager.lastPort = settings.getString("port");
                } catch (IllegalArgumentException e) {
                }

                try {
                    UI.startMinimized = settings.getBoolean("minimized");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Main] :: ...variables initialized");
    }


    public static void saveVariables() {
        System.out.println("[Main] :: saveVariables() ...");

        PFolder.createFolder(saveDir + PSystem.getFileSeparator() + "Controls");
        PFolder.createFolder(saveDir + PSystem.getFileSeparator() + "Groups");

        // Control
        int controlCount = 0;
        for (Control c : Control.getControls()) {
            PDataStorage storage = new PDataStorage();
            storage.add("name", c.getName());
            storage.add("vol", c.getVolume());
            storage.add("gs", c.getGroups().size());//Group-Size

            ArrayList<Group> groups = c.getGroups();
            for (int i = 0; i < groups.size(); i++) {
                storage.add("g" + i, groups.get(i).getName());
            }

            storage.save(saveDir + PSystem.getFileSeparator() + "Controls" + PSystem.getFileSeparator() + "Control" + controlCount++ + ".cfg");
        }

        // Group
        int groupCount = 0;
        for (Group g : Group.groups) {
            PDataStorage storage = new PDataStorage();
            storage.add("name", g.getName());
            storage.add("ps", g.getProcesses().size()); // Process-Size

            ArrayList<String> processes = g.getProcesses();
            for (int i = 0; i < processes.size(); i++) {
                storage.add("p" + i, processes.get(i));
            }

            storage.save(saveDir + PSystem.getFileSeparator() + "Groups" + PSystem.getFileSeparator() + "Groups" + groupCount++ + ".cfg");
        }

        // settings
        PDataStorage settings = new PDataStorage();
        if (am.getPortName() != null)
            settings.add("port", am.getPortName());

        settings.add("autoConnect", AudioManager.doAutoConnect);
        settings.add("minimized", !ui.f.isVisible());
        settings.save(saveDir + PSystem.getFileSeparator() + "settings.cfg");

        System.out.println("[Main] :: ...variables saved");
    }

}
