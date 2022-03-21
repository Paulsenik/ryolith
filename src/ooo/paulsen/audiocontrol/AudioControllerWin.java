package ooo.paulsen.audiocontrol;

public class AudioControllerWin extends AudioController {

    public AudioControllerWin() throws Exception {
        super();

    }

    @Override
    public synchronized boolean refreshProcesses() {
        return false;
    }

    @Override
    public synchronized String[] getProcesses() {
        return new String[0];
    }

    @Override
    public synchronized boolean isAudioConnected() {
        return false;
    }

    @Override
    protected synchronized void setProcessVolume(String processName, float volume) {

    }

}
