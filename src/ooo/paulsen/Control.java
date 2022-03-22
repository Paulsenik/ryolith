package ooo.paulsen;

import java.util.concurrent.CopyOnWriteArrayList;

public class Control {

    private CopyOnWriteArrayList<Group> groups = new CopyOnWriteArrayList<>();
    private String name;

    public Control(String name) {
        this.name = name;
    }

    public void setVolume(float volume) {
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

    public String[] getGroups() {
        return (String[]) groups.toArray();
    }

    public String getName() {
        return name;
    }

}
