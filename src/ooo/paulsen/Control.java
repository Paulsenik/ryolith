package ooo.paulsen;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Control {

    private CopyOnWriteArrayList<Group> groups = new CopyOnWriteArrayList<>();
    private String name;

    private float volume;

    public Control(String name) {
        this.name = name;
    }

    /**
     * // TODO - use for save-system
     */
    public Control(String name, float volume) {
        this.name = name;
        this.volume = volume; // startvolume
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
