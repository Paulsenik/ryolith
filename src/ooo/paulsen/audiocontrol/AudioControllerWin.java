package ooo.paulsen.audiocontrol;

import ooo.paulsen.Main;
import ooo.paulsen.ui.UI;
import ooo.paulsen.utils.PSystem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class AudioControllerWin extends AudioController {

    /**
     * String = ProcessName<br>
     * Integer = ProcessID
     */
    private volatile HashMap<String, ArrayList<Integer>> processes = new HashMap<>();

    public AudioControllerWin(){
        super();

        if(Main.updateDependencys()) {

            // TODO

        } else {
            JOptionPane.showMessageDialog(null, "The AudioController might no be installed/set up correctly\nDetected OS: " +
                    PSystem.getOSType() + "\nRunning Python-Script returns ERROR!", "Startup-Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    @Override
    public synchronized boolean refreshProcesses() {
        return false;
    }

    @Override
    public synchronized ArrayList<String> getProcesses() {
        return null;
    }

    @Override
    public synchronized boolean isAudioConnected() {
        return false;
    }

    @Override
    protected synchronized void setProcessVolume(String processName, float volume) {

    }

}
