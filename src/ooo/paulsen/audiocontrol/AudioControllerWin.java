package ooo.paulsen.audiocontrol;

import ooo.paulsen.Main;
import ooo.paulsen.utils.PSystem;

import javax.swing.*;
import java.util.ArrayList;

public class AudioControllerWin extends AudioController {

    public AudioControllerWin(){
        super();

        JOptionPane.showMessageDialog(Main.ui.f, "The AudioController might no be installed/set up correctly\nDetected OS: " +
                PSystem.getOSType() + "\nRunning Python-Script returns ERROR!", "Startup-Error", JOptionPane.ERROR_MESSAGE);
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
