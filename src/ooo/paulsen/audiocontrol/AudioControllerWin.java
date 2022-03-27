package ooo.paulsen.audiocontrol;

import java.util.ArrayList;

public class AudioControllerWin extends AudioController {

    public AudioControllerWin() throws Exception {
        super();

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
