package ooo.paulsen.ui;

import ooo.paulsen.Main;
import ooo.paulsen.ui.core.PUIAction;
import ooo.paulsen.ui.core.PUIFrame;

import java.awt.*;

public class ControlElement extends PUIElement {

    private PUIScrollPanel groups;
    private PUIRotaryControl rotaryControl;
    private PUIElement removeControl, addGroup;

    private String name;

    private float lastVolume = 0.5f;

    public ControlElement(String name, PUIFrame f) {
        super(f);
        this.name = name;

        init();
    }

    private void init() {
        removeControl = new PUIElement(getFrame(), getInteractionLayer() + 1);

        rotaryControl = new PUIRotaryControl(getFrame(), getInteractionLayer() + 1);
        rotaryControl.addValueUpdateAction(new PUIAction() {
            @Override
            public synchronized void run(PUIElement that) {
                Main.setControlVolume(name, rotaryControl.getValue());
                System.out.println("Rotary: " + name + " " + rotaryControl.getValue());
            }
        });
        rotaryControl.doPaintOverOnHover(false);
        rotaryControl.doPaintOverOnPress(false);

        groups = new PUIScrollPanel(getFrame(), getInteractionLayer() + 1);
        groups.setShowedElements(3);
        groups.doPaintOverOnHover(false);
        groups.doPaintOverOnPress(false);

        addGroup = new PUIElement(getFrame(), getInteractionLayer() + 1) {
            @Override
            public synchronized void draw(Graphics2D g) {
                super.draw(g);

                int space = Math.min(w, h) / 6;

                g.setColor(Color.white);
                g.fillRect(x + space, y + h / 2 - space / 2, w - space * 2, space);
                g.fillRect(x + w / 2 - space / 2, y + space, space, h - space * 2);
            }
        };
        addGroup.addActionListener(new PUIAction() {
            @Override
            public void run(PUIElement puiElement) {
                System.out.println("add Group button");
            }
        });
        addGroup.doPaintOverOnHover(false);
        addGroup.doPaintOverOnPress(false);
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
        g.setFont(new Font("Arial", 0, topSize));
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

}
