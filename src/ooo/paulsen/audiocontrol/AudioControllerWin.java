package ooo.paulsen.audiocontrol;

import ooo.paulsen.Main;
import ooo.paulsen.io.PCustomProtocol;
import ooo.paulsen.utils.PSystem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AudioControllerWin extends AudioController {

    public PCustomProtocol wac = new PCustomProtocol("wac", '[', ']', "^<<^", "^>>^");

    private volatile HashSet<String> processes = new HashSet<>();

    public AudioControllerWin() {
        super();

        if (!Main.updateDependencys()) {
            JOptionPane.showMessageDialog(null, "The AudioController might no be installed/set up correctly\nDetected OS: " + PSystem.getOSType() + "\nRunning Python-Script returns ERROR!", "Startup-Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }


    }

    @Override
    public synchronized boolean refreshProcesses() {
        List<String> lines = WinAudio.getAudioList();
        if(lines == null)
            return false;

        for(String line : lines) {
            String message = wac.getMessage(line);
            int separatorIndex = message.indexOf('|');
            processes.add(message.substring(0,separatorIndex));
        }

        return true;
    }

    @Override
    public synchronized ArrayList<String> getProcesses() {
        return new ArrayList<>(processes);
    }

    @Override
    public synchronized boolean isAudioConnected() {
        WinAudio wa = WinAudio.getInstance();
        if (wa != null) {
            return wa.isAlive();
        }
        return false;
    }

    @Override
    protected synchronized void setProcessVolume(String processName, float volume) {
        WinAudio.setAudio(processName, volume);
    }

}
