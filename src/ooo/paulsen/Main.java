package ooo.paulsen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import com.paulsen.io.*;
import ooo.paulsen.audiocontrol.AudioManager;

/**
 * @author Paul
 * @since 2021/08/04 => 2022/03/17
 */
public class Main {

    private static final boolean devMode = true;

    public static final String SAVEFOLDER = "AudioController";

    public static UI ui;
    public static AudioManager am;

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

        am = new AudioManager();
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

    public static void initVariables() {

        readSettings();

        if (!existNecessaryFiles()) {
            return;
        }

        readVariables();

        System.out.println("[Main] :: initialized variables");
    }

    private static boolean existNecessaryFiles() {

        if (!new File(SAVEFOLDER + "/" + "Processes/processes.pstorage").exists())
            return false;
        if (!new File(SAVEFOLDER + "/groups.pstorage").exists())
            return false;
        if (!new File(SAVEFOLDER + "/controls.pstorage").exists())
            return false;

        return true;
    }

    /**
     * Saves and exits program
     */
    public static void exitAll() {
        // TODO exit python-script / linux-terminal
        try {
            saveVariables();
        } catch (Exception e) {
            e.printStackTrace();
            ui.f.sendUserError("An Error Occured!\nPlease check consoleOut.txt !!!");
        }
        System.exit(0);
    }

    private static void readSettings() {
        // TODO
    }

    public static void readVariables() {
        // TODO
    }


    public static void saveVariables() {

        PFolder.createFolder(SAVEFOLDER);
        PFolder.createFolder(SAVEFOLDER + "/" + "Processes");
        PFolder.createFolder(SAVEFOLDER + "/" + "Groups");
        PFolder.createFolder(SAVEFOLDER + "/" + "Controls");

    }

}
