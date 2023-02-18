package ooo.paulsen;

import ooo.paulsen.audiocontrol.AudioManager;
import ooo.paulsen.io.PDataStorage;
import ooo.paulsen.io.PFile;
import ooo.paulsen.io.PFolder;
import ooo.paulsen.ui.UI;
import ooo.paulsen.utils.PInstance;
import ooo.paulsen.utils.PSystem;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Timer;

/**
 * @author Paul
 * @since 2021/08/04 => 2022/03/17
 */
public class Main {

    // Change before Commit or Build
    public static String version = "v2.0.1";
    private static final boolean devMode = false;

    // Folder in Home-dir
    public static final String saveDir = System.getProperty("user.home") + PSystem.getFileSeparator() + ".jaudiocontroller";
    public static final String localEXEPath = saveDir + PSystem.getFileSeparator() + "WinAudioControl.exe";
    public static final String webEXEPath_1 = "https://raw.githubusercontent.com/realPaulsen/AudioController/release/src/ooo/paulsen/python/dist/WinAudioControl.exe";
    public static final String webEXEPath_2 = "https://raw.githubusercontent.com/realPaulsen/AudioController/development/src/ooo/paulsen/python/dist/WinAudioControl.exe"; // Backup-link
    public static final int PORT = 6434;
    public static UI ui;
    public static AudioManager am;

    private static PInstance instance;

    // Only private to not accidentally be instanced
    private Main() {
    }

    public static void main(String[] args) {

        try {
            instance = new PInstance(PORT, new Runnable() {
                @Override
                public void run() {
                    focusOnFrame();
                    ui.f.setVisible(true);
                    ui.f.setState(JFrame.NORMAL);
                    focusOnFrame();
                }
            });
        } catch (IOException e) { // other Instace is running
            // JOptionPane.showMessageDialog(null, "AudioController already running!", "AudioController", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("[AudioController] : [main] : Already running! Opened other instance.");
            System.exit(0);
        }

        initVariables();

        PFolder.createFolder(saveDir);

        PFolder.createFolder(saveDir + PSystem.getFileSeparator() + "Logs");

        if (!devMode) {

            // Set Console out
            try {
                PrintStream out = new PrintStream(new FileOutputStream(saveDir + PSystem.getFileSeparator() + "Logs" + PSystem.getFileSeparator() + "log_" + System.currentTimeMillis()));
                System.setOut(out);
                System.setErr(out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            version += " - Devmode";
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

        // Update processes
        Main.am.refreshProcesses();
        Main.ui.updateProcessList();

        // AutoSave every 10 minutes
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("[Main] :: Auto-Saving...");
                saveVariables();
            }
        }, 5 * 60 * 1000, 10 * 60 * 1000);
    }

    /**
     * Show and bring frame on top
     */
    public static void focusOnFrame() {
        ui.f.toFront();
        ui.f.requestFocus();
    }

    public static void setControlVolume(String controlName, float volume) {
        for (Control c : Control.getControls())
            if (c != null && c.getName().equals(controlName)) {
                c.setVolume(volume);
                ui.f.repaint();
            }
    }

    public static Set<String> getSavedProcesses(){
        HashSet<String> list = new HashSet<>();
        for (Control c : Control.getControls()){
            for(Group g : c.getGroups()){
                list.addAll(g.getProcesses());
            }
        }
        return list;
    }

    /**
     * Opens UserPopup for closing the Program
     */
    public static void close() {
        focusOnFrame();
        if (ui.f.getUserConfirm("Really Close Audio Controller", "Audio Controller")) {
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

    /**
     * Updates the python-dependencys from the GiHub-Repository by downloading the newest WAC-Binary (Windows Audio Control)
     * @return true: if depedencys are up-to-date and working - false: if the update failed.
     */
    public static boolean updateDependencys(){
        if(downloadFile(webEXEPath_1,localEXEPath)){

            System.out.println("Successfully updated WindowsAudioControl.exe from direct-link");
            return true;
        }else if(downloadFile(webEXEPath_2,localEXEPath)){

            System.out.println("Successfully updated WindowsAudioControl.exe from backup-link");
            return true;
        }else if(new File(localEXEPath).exists()) { // file exists but is not the newest
            // JOptionPane.showMessageDialog(null, "Could not update dependencys for your OS", "Startup-Warning", JOptionPane.INFORMATION_MESSAGE);
            System.err.println("[Main] :: Could not update dependencys for your OS!");
            return true;
        }
        // exit if an error occurred and no local dependencys exist
        return false;
    }

    /**
     * Downloads File from given web-url and saves/overwrites it to the given local destination
     * @param url
     * @param filepath
     * @return true: if download successful - false: if some error occured
     */
    public static boolean downloadFile(String url, String filepath) {
        System.out.println(filepath);
        try {
            URL website = new URL(url);
            InputStream in = website.openStream();
            File f = new File(filepath);
            Files.copy(in, new File(filepath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Could not download file");
            return false;
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

}
