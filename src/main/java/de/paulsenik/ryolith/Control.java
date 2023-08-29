package de.paulsenik.ryolith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Control {

    private static CopyOnWriteArrayList<Control> controls = new CopyOnWriteArrayList<>();

    public static void removeControl(String name) {
        for (Control c : controls)
            if (c != null && c.getName().equals(name)) {
                controls.remove(c);
                if (Main.ui != null)
                    Main.ui.updateControlList();
                return;
            }
    }

    public static Control getControl(String name) {
        for (Control c : controls)
            if (c != null && c.getName().equals(name))
                return c;
        return null;
    }

    public static List<Control> getControls() {
        return controls;
    }

    private CopyOnWriteArrayList<Group> groups = new CopyOnWriteArrayList<>();
    private String name;

    private float volume;

    public Control(String name) {
        this.name = name;
        controls.add(this);
        if (Main.ui != null)
            Main.ui.updateControlList();
    }

    public Control(String name, float volume) {
        this.name = name;
        this.volume = volume; // startvolume
        controls.add(this);
        if (Main.ui != null)
            Main.ui.updateControlList();
    }

    public void setVolume(float volume) {
        this.volume = volume;
        for (Group g : groups)
            g.setVolume(volume);
    }

    public boolean addGroup(Group g) {
        if (g != null) {
            return groups.add(g);
        }
        return false;
    }

    public boolean removeGroup(Group g) {
        return groups.remove(g);
    }

    public ArrayList<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    public String getName() {
        return name;
    }

    public float getVolume() {
        return volume;
    }

}
