package ooo.paulsen.audiocontrol;

import ooo.paulsen.Main;
import ooo.paulsen.utils.PConsole;
import ooo.paulsen.utils.PSystem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AudioControllerLinux extends AudioController {

    /**
     * String = ProcessName<br>
     * Integer = ProcessID
     */
    private volatile HashMap<String, Integer> processes = new HashMap<>();

    private boolean doesPactlExist = false;

    public AudioControllerLinux() {
        super();
        doesPactlExist = !PConsole.run("pactl stat").contains("ERROR");

        if (!doesPactlExist)
            JOptionPane.showMessageDialog(Main.ui.f, "The AudioController might no be installed/set up correctly\nDetected OS: " +
                    PSystem.getOSType() + "\nRunning \"pactl stat\" returns ERROR!", "Startup-Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public synchronized boolean refreshProcesses() {
        if (!doesPactlExist)
            return false;

        String out = PConsole.run("pactl list sink-inputs");
        if (out.contains("ERROR")) {
            return false;
        }
        String lines[] = out.split(String.valueOf((char) 10));

        int lastBeginning = 0; // marks the beginning-line of a new Process of the output
        int equalCount = 0; // counts lines that contain "=" without one line, not having one
        for (int i = 0; i < lines.length; i++) {
            if (equalCount > 3) {
                if (i + 1 >= lines.length || lines[i + 1].trim().isEmpty()) {

                    addProcessToList(Arrays.copyOfRange(lines, lastBeginning, i));
                    lastBeginning = i + 2;

                    if (i + 1 < lines.length)
                        i++;
                }
            }


            if (lines[i].contains("=")) {
                equalCount++;
            } else {
                equalCount = 0;
            }
        }

        return true;
    }

    /**
     * finds out of the lines the ID and NAME of the application and adds it to the list
     *
     * @param lines of Command-Output
     * @return if it succeeded
     */
    private boolean addProcessToList(String lines[]) {

        try {

            // find ID
            int indexID = lines[0].lastIndexOf("#"); // index of first line where the ID lies

            if (indexID == -1) // No "#" found
                return false;

            int ID = Integer.parseInt(lines[0].substring(indexID + 1, lines[0].length()));

            // find application.name
            String name = "";
            for (String l : lines) {
                if (l.contains("application.name")) {
                    int indexName = l.indexOf("\"");

                    if (indexName == -1) // No '"' found
                        return false;

                    name = l.stripTrailing().substring(indexName + 1, l.length() - 1);
                }
            }

            processes.put(name, ID);

            return true;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    protected synchronized void setProcessVolume(String processName, float volume) {

        String out = "ERROR";
        if (processes.containsKey(processName)) {
            out = run(processes.get(processName), volume);
        }

        // - didn't find Process with this ID
        // - No process found in map
        if (out.contains("ERROR")) {
            refreshProcesses();
            if (processes.containsKey(processName)) {
                run(processes.get(processName), volume); // run again
            }
        }
    }

    /**
     * Only runs Command
     *
     * @param processID (0<=ID)
     * @param volume    (0<=volume<=1)
     * @return result of Terminal
     */
    private String run(int processID, float volume) {
        if (!doesPactlExist)
            return "ERROR";

        return PConsole.run("pactl set-sink-input-volume " + processID + " " + volume);
    }

    @Override
    public synchronized ArrayList<String> getProcesses() {
        return new ArrayList(processes.keySet());
    }

    @Override
    public synchronized boolean isAudioConnected() {
        return doesPactlExist;
    }

}
