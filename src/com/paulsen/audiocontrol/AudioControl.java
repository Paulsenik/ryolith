package com.paulsen.audiocontrol;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import com.paulsen.UI;
import com.paulsen.ui.*;

/*
 * Interface for direct inputs from Arduino or Userinterface
 */
public class AudioControl extends PUIElement {

	private volatile ArrayList<AudioGroup> groups = new ArrayList<>();

	private volatile float volume = 0f;
	private String name;
	private PUIAction onDeleteButton;

	// UI
	private UI ui;
	private PUIText nameUI, deleteAudioControl;
	public PUIRotaryControl rotaryVolumeControlUI;
	private PUIScrollPanel groupsUI;
	private PUIText addGroupUI;

	public AudioControl(UI c, String name, PUIAction onDeleteButton) {
		super(c);
		this.ui = c;
		this.name = name;
		this.onDeleteButton = onDeleteButton;
		init();
	}

	private void init() {
		doPaintOverOnHover(false);
		doPaintOverOnPress(false);

		nameUI = new PUIText(ui, name);
		nameUI.doPaintOverOnHover(false);
		nameUI.doPaintOverOnPress(false);
		PUIElement.registeredElements.remove(nameUI);

		deleteAudioControl = new PUIText(ui, "-");
		deleteAudioControl.doPaintOverOnHover(false);
		deleteAudioControl.doPaintOverOnPress(false);
		PUIElement.registeredElements.remove(deleteAudioControl);
		deleteAudioControl.addActionListener(onDeleteButton);

		rotaryVolumeControlUI = new PUIRotaryControl(ui);
		rotaryVolumeControlUI.doPaintOverOnHover(false);
		rotaryVolumeControlUI.doPaintOverOnPress(false);
		rotaryVolumeControlUI.addValueUpdateAction(new Runnable() {
			@Override
			public void run() {
				setVolume(rotaryVolumeControlUI.getValue());
				runAllActions();
			}
		});
		rotaryVolumeControlUI.setValueLength(.75f);
		PUIElement.registeredElements.remove(rotaryVolumeControlUI);

		groupsUI = new PUIScrollPanel(ui);
		groupsUI.setShowedElements(4);
		groupsUI.doPaintOverOnHover(false);
		groupsUI.doPaintOverOnPress(false);
		groupsUI.addValueUpdateAction(new Runnable() {
			@Override
			public void run() {
				runAllActions();
			}
		});
		PUIElement.registeredElements.remove(groupsUI);

		paintInvoke = new PUIPaintable() {
			@Override
			public void paint(Graphics g, int x, int y, int w, int h) {
				g.setColor(PUIElement.darkBG_1);
				g.fillRect(x, y, w, h);
				g.setColor(Color.black);
				g.drawRect(x, y, w, h);

				nameUI.draw(g);
				deleteAudioControl.draw(g);
				rotaryVolumeControlUI.draw(g);
				groupsUI.draw(g);
				addGroupUI.draw(g);
			}
		};

		addGroupUI = new PUIText(ui, "+");
		addGroupUI.doPaintOverOnHover(false);
		addGroupUI.doPaintOverOnPress(false);
		PUIElement.registeredElements.remove(addGroupUI);
		addGroupUI.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {

				ArrayList<String> groupnames = new ArrayList<String>();
				for (AudioGroup ag : ui.am.getGroups()) {
					if (!groups.contains(ag)) {
						groupnames.add(ag.getName());
					}
				}

				if (groupnames.isEmpty()) {
					ui.sendUserInfo("No Groups available");
					return;
				}

				String selection[] = new String[groupnames.size()];
				for (int i = 0; i < selection.length; i++) {
					selection[i] = groupnames.get(i);
				}
				int index = ui.getUserSelection("Choose a Group you want to add", selection);
				addAudioGroup(selection[index]);
			}
		});
	}

	@Override
	public synchronized void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		nameUI.setBounds(x, y, w - h / 8, h / 8);
		deleteAudioControl.setBounds(x + w - h / 8, y, h / 8, h / 8);

		int volH = (h / 8 * 7 / 3);
		int small = w > volH ? volH : w;
		rotaryVolumeControlUI.setBounds(x + (w - small) / 2, h / 8, small);

		groupsUI.setSliderWidth(w / 8);
		groupsUI.setBounds(x, rotaryVolumeControlUI.getY() + rotaryVolumeControlUI.getH(), w,
				h - rotaryVolumeControlUI.getH() - h / 8 * 2);

		addGroupUI.setBounds(x + w / 2 - h / 8 / 2, y + h - h / 8, h / 8, h / 8);
	}

	public void setVolume(float volume) {
		System.out.println("access");
		volume = (volume > 1f ? 1 : (volume < 0 ? 0 : volume));
		if (this.volume != volume) {
			this.volume = volume;
			rotaryVolumeControlUI.setValue(volume);
			setAudioGroupAudio(volume);
			runAllActions();
		}
	}

	public float getVolume() {
		return volume;
	}

	private void setAudioGroupAudio(float volume) {
		for (AudioGroup ag : groups) {
			if (ag != null)
				ag.setVolume(volume);
			else
				System.err.println("Some Error occured with Audiogroups");
		}
	}

	public void addAudioGroup(String name) {
		for (AudioGroup ag : ui.am.getGroups()) {
			if (ag.getName().equals(name)) {
				addAudioGroup(ag);
				runAllActions();
				return;
			}
		}
	}

	public synchronized void addAudioGroup(AudioGroup g) {
		if (g != null) {
			groups.add(g);
			PUIText groupButton = new PUIText(ui, g.getName());
			groupButton.addActionListener(new PUIAction() {
				@Override
				public void run(PUIElement arg0) {
					if (groupsUI.getElements().contains(groupButton))
						if (ui.getUserConfirm("Delete Group \"" + g.getName() + "\"", "DELETE Group")) {
							removeAudioGroup(g);
							runAllActions();
						}
				}
			});
			groupsUI.addElement(groupButton);
			runAllActions();
		}
	}

	public synchronized boolean removeAudioGroup(AudioGroup g) {
		if (g == null || g.getName() == null)
			return false;

		for (PUIElement t : groupsUI.getElements()) {
			if (((PUIText) t).getText().equals(g.getName())) {
				groupsUI.removeElement(t);
				break;
			}
		}
		runAllActions();
		return groups.remove(g);
	}

	public ArrayList<AudioGroup> getGroups() {
		return groups;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		nameUI.setEnabled(enabled);
		deleteAudioControl.setEnabled(enabled);
		rotaryVolumeControlUI.setEnabled(enabled);
		groupsUI.setEnabled(enabled);
		addGroupUI.setEnabled(enabled);
	}

	public String getName() {
		return name;
	}

}
