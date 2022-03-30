package ooo.paulsen;

import ooo.paulsen.audiocontrol.AudioManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class Group {

    public static CopyOnWriteArrayList<Group> groups = new CopyOnWriteArrayList<>();

    public static Group getGroup(String s) {
        for (Group g : groups)
            if (g.getName().equals(s))
                return g;
        return null;
    }

    public static void removeGroup(String name) {
        for (int i = 0; i < groups.size(); i++) {
            Group g = groups.get(i);
            if (g != null && g.getName().equals(name)) {

                // remove from index
                groups.remove(g);

                // remove this Group from all Controlls
                for (Control c : Control.getControls()) {
                    c.removeGroup(g);
                }

                if (Main.ui != null) {
                    Main.ui.updateControlList();
                    Main.ui.updateGroupList();
                }

                return;
            }
        }
    }

    private CopyOnWriteArrayList<String> processes = new CopyOnWriteArrayList<>();

    private String name;

    public Group(String name) {
        this.name = name;
        groups.add(this);
    }

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

    public ArrayList<String> getProcesses() {
        return new ArrayList<>(processes);
    }

    public boolean hasProcess(String processName) {
        return processes.contains(processName);
    }

    public String getName() {
        return name;
    }

}
