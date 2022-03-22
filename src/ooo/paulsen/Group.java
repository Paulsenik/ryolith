package ooo.paulsen;

import ooo.paulsen.audiocontrol.AudioManager;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class Group {

    private CopyOnWriteArrayList<String> processes = new CopyOnWriteArrayList<>();

    public void setVolume(float volume) {
        for (String process : processes)
            Main.am.setVolume(process, volume);
    }

    public boolean addProcess(String s) {
        if (s != null) {
            return processes.add(s);
        }
        return false;
    }

    public boolean removeProcess(String s) {
        return processes.remove(s);
    }

    public String[] getProcesses() {
        return (String[]) processes.toArray();
    }

    public boolean hasProcess(String processName) {
        return processes.contains(processName);
    }

}
