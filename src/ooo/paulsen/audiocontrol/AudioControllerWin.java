package ooo.paulsen.audiocontrol;

import ooo.paulsen.Main;
import ooo.paulsen.io.PCustomProtocol;
import ooo.paulsen.io.serial.PSerialConnection;
import ooo.paulsen.io.serial.PSerialListener;
import ooo.paulsen.utils.PSystem;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AudioControllerWin extends AudioController {

    PCustomProtocol wacProtocol = new PCustomProtocol("wac",'[',']',"^<<^","^>>^");
    private Process script;

    public AudioControllerWin(){
        super();

        if(Main.updateDependencys()) {

            initScript();

        } else {
            JOptionPane.showMessageDialog(null, "The AudioController might no be installed/set up correctly\nDetected OS: " +
                    PSystem.getOSType() + "\nRunning Python-Script returns ERROR!", "Startup-Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void initScript(){
        try {
            script = Runtime.getRuntime().exec(Main.localEXEPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
