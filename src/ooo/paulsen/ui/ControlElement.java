package ooo.paulsen.ui;

import ooo.paulsen.Control;
import ooo.paulsen.Group;
import ooo.paulsen.Main;
import ooo.paulsen.ui.core.PUIAction;
import ooo.paulsen.ui.core.PUIFrame;

import java.awt.*;
import java.util.ArrayList;

public class ControlElement extends PUIElement {

    private PUIList groups;
    private PUIRotaryControl rotaryControl;
    private PUIElement removeControl, addGroup;

    private String name;

    private float lastVolume = 0.5f;

    public ControlElement(String name, PUIFrame f) {
        super(f);
        this.name = name;

        init();
        updateGroupList();
    }

    private void init() {
        removeControl = new PUIElement(getFrame(), getInteractionLayer() + 1) {
            @Override
            public synchronized void draw(Graphics2D g) {
                if (!isEnabled())
                    return;

                super.draw(g);

                int space = Math.min(w, h) / 6;

                g.setColor(Color.white);
                g.fillRect(x + space, y + h / 2 - space / 2, w - space * 2, space);
            }
        };
        removeControl.setMetadata(this); // set this ControlElement as reference for the ActionListeners
        removeControl.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement that) {
                System.out.println("[ControlElement] :: pressed removeControl");
                ControlElement c = (ControlElement) that.getMetadata();

                if (!c.getName().isEmpty()) {

                    if (getUserConfirm("Do you want to delete " + c.getName() + " ?", "Delete")) {
                        Control.removeControl(c.getName());
                    }
                }
            }
        });
        removeControl.doPaintOverOnHover(false);
        removeControl.doPaintOverOnPress(false);

        rotaryControl = new PUIRotaryControl(getFrame(), getInteractionLayer() + 1);
        rotaryControl.addValueUpdateAction(new PUIAction() {
            @Override
            public synchronized void run(PUIElement that) {
                Main.setControlVolume(name, rotaryControl.getValue());
            }
        });
        rotaryControl.doPaintOverOnHover(false);
        rotaryControl.doPaintOverOnPress(false);
        rotaryControl.setValue_NoUpdate(0f);

        groups = new PUIList(getFrame(), getInteractionLayer() + 1);
        groups.setShowedElements(3);
        groups.doPaintOverOnHover(false);
        groups.doPaintOverOnPress(false);

        addGroup = new PUIElement(getFrame(), getInteractionLayer() + 1) {
            @Override
            public synchronized void draw(Graphics2D g) {
                if (!isEnabled())
                    return;

                super.draw(g);

                int space = Math.min(w, h) / 6;

                g.setColor(Color.white);
                g.fillRect(x + space, y + h / 2 - space / 2, w - space * 2, space);
                g.fillRect(x + w / 2 - space / 2, y + space, space, h - space * 2);
            }
        };
        addGroup.setMetadata(this); // set this ControlElement as reference for the ActionListeners
        addGroup.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement that) {
                System.out.println("[ControlElement] :: pressed addGroup");

                Control c = Control.getControl(name);
                if (c == null) {
                    sendUserError("This Control doesn't exist!");
                    return;
                }

                ArrayList<String> groupNames = new ArrayList<>();
                groupNames.add(" ");
                for (Group g : Group.groups) {
                    if (!c.getGroups().contains(g)) // can't add Group if the Control already has it
                        groupNames.add(g.getName());
                }

                if (groupNames.size() == 1) {
                    sendUserInfo("No more Groups available");
                    return;
                }

                int selectedIndex = getUserSelection("Select a Group", groupNames);

                if (selectedIndex == 0)
                    return;

                boolean b = getUserConfirm("Add Group: " + groupNames.get(selectedIndex) + "?", "Control: " + name);

                if (b) {
                    c.addGroup(Group.getGroup(groupNames.get(selectedIndex)));
                }
                updateGroupList();

            }
        });
        addGroup.doPaintOverOnHover(false);
        addGroup.doPaintOverOnPress(false);
    }

    public void updateGroupList() {

        Control c = Control.getControl(name);
        if (c == null) {
            sendUserError("This Control doesn't exist!");
            return;
        }

        ArrayList<String> groupNames = new ArrayList<>();
        for (Group g : Group.groups) {
            if (c.getGroups().contains(g)) // can't add Group if the Control already has it
                groupNames.add(g.getName());
        }

        ArrayList<PUIElement> elems = new ArrayList<>();
        for (String s : groupNames) {
            PUIText t = new PUIText(frame, s);

            t.setMetadata(this); // here, metadata represents if it's selected in the UI

            t.addActionListener(new PUIAction() {
                @Override
                public void run(PUIElement that) {

                    System.out.println("[ControlElement] :: pressed Group: " + ((PUIText) that).getText());

                    // Remove
                    boolean b = getUserConfirm("Really remove \"" + ((PUIText) that).getText() + "\" from " + name + " ?", "Control");
                    if (b) {
                        Control c = Control.getControl(name);
                        if (c != null) {
                            c.removeGroup(Group.getGroup(((PUIText) that).getText()));
                            updateGroupList();
                        }
                    }

                }
            });

            elems.add(t);
        }

        // refresh List
        groups.clearElements();
        groups.addAllElements(elems);
    }

    public void updateRotaryValue(float f) {
        if (f != lastVolume) {
            lastVolume = f;
            rotaryControl.setValue_NoUpdate(f);
        }
    }

    public String getName() {
        return name;
    }

    private int topSize;

    @Override
    public synchronized void draw(Graphics2D g) {
        if (!isEnabled())
            return;

        super.draw(g);

        Shape c = g.getClip();
        g.setClip(x, y, w, h);

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, topSize));
        g.drawString(name, x, (int) (y + topSize * 0.9));

        g.setClip(c);

        int valueHeight = (int) ((h / 3 * 2 - topSize) * lastVolume);

        g.setColor(new Color(28, 135, 222));
        g.fillRect(x, y + h / 3 * 2 - valueHeight, w, valueHeight);

        rotaryControl.draw(g);
        addGroup.draw(g);
        groups.draw(g);
    }

    @Override
    public synchronized void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);

        topSize = h / 6;
        int bSize = h / 10;

        int min = Math.min(w, h / 3 * 2 - topSize);

        removeControl.setBounds(x + w - topSize, y, topSize, topSize);

        rotaryControl.setBounds(x + (w - (min - bSize)) / 2, y + topSize + bSize / 2, min - bSize);

        addGroup.setBounds(x + w - bSize, y + h / 3 * 2 - bSize, bSize, bSize);

        groups.setBounds(x, y + h / 3 * 2, w, h / 3);
        groups.setSliderWidth(bSize);

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        removeControl.setEnabled(enabled);
        rotaryControl.setEnabled(enabled);
        addGroup.setEnabled(enabled);
        groups.setEnabled(enabled);
    }

    @Override
    public void setLayer(int l) {
        super.setLayer(l);
        removeControl.setLayer(l + 1);
        rotaryControl.setLayer(l + 1);
        addGroup.setLayer(l + 1);
        groups.setLayer(l + 1);
    }

    @Override
    public void release() {
        super.release();
        // remove all child-elements from frame as well
        frame.remove(removeControl);
        frame.remove(rotaryControl);
        frame.remove(addGroup);
        frame.remove(groups);
    }

}
