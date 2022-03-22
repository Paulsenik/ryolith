package ooo.paulsen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import ooo.paulsen.io.*;
import ooo.paulsen.audiocontrol.AudioManager;

import javax.swing.*;

/**
 * @author Paul
 * @since 2021/08/04 => 2022/03/17
 */
public class Main {

    private static final boolean devMode = true;

    public static final String SAVEFOLDER = "AudioController";

    public static UI ui;
    public static AudioManager am;

    public static CopyOnWriteArrayList<Control> controls = new CopyOnWriteArrayList<>();

    // Only private to not accidentally be instanced
    private Main() {
    }

    public static void main(String[] args) {

        if (!devMode) {

            if (!new File(SAVEFOLDER).exists())
                PFolder.createFolder(SAVEFOLDER);

            // Set Console out
            if (new File(SAVEFOLDER + "/consoleOut.txt").exists())
                new PFile(SAVEFOLDER + "/consoleOut.txt").delete();
            try {
                PrintStream out = new PrintStream(new FileOutputStream(SAVEFOLDER + "/consoleOut.txt"));
                System.setOut(out);
                System.setErr(out);
            } catch (FileNotFoundException e) {
            }
        }

        try {
            am = new AudioManager();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Startup-Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        ui = new UI();

        try {
            initVariables();
            ui.f.updateElements();
        } catch (Exception e) {
            e.printStackTrace();
            PFile.copyFile(SAVEFOLDER + "/consoleOut.txt", SAVEFOLDER + "/error.log", false); // create error-log-file
            ui.f.sendUserError("An Error Occured!\nPlease check error.log !!!");
        }

        // AutoSave every 10 minutes
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                saveVariables();
            }
        }, 0, 10 * 60 * 1000);

    }

    public static void setControlVolume(String controlName, float volume) {
        for (Control c : controls)
            if (c != null && c.getName().equals(controlName))
                c.setVolume(volume);
    }

    public static void initVariables() {

        readVariables();

        System.out.println("[Main] :: initialized variables");
    }

    /**
     * Saves and exits program
     */
    public static void exitAll() {
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

    public static void readVariables() {

        PFolder.createFolder(SAVEFOLDER);
        PFolder.createFolder(SAVEFOLDER + "/" + "Processes");
        PFolder.createFolder(SAVEFOLDER + "/" + "Groups");
        PFolder.createFolder(SAVEFOLDER + "/" + "Controls");

        // TODO
    }


    public static void saveVariables() {

        PFolder.createFolder(SAVEFOLDER);
        PFolder.createFolder(SAVEFOLDER + "/" + "Processes");
        PFolder.createFolder(SAVEFOLDER + "/" + "Groups");
        PFolder.createFolder(SAVEFOLDER + "/" + "Controls");

        // TODO
    }

}
