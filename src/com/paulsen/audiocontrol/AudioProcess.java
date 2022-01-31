package com.paulsen.audiocontrol;

import java.util.ArrayList;

import com.paulsen.AudioManager;
import com.paulsen.ui.PUIText;

public class AudioProcess extends PUIText {

	public static ArrayList<AudioProcess> registeredProcesses = new ArrayList<AudioProcess>();

	private String processName;
	private AudioGroup ag;
	private AudioManager am;

	public AudioProcess(AudioManager am, String name) {
		super(am.ui, name);
		registeredProcesses.add(this);
		doPaintOverOnHover(false);
		doPaintOverOnPress(false);
		processName = name;
		this.am = am;
	}

	public void setVolume(float f) {
		am.setVolume(processName, f);
	}

	public void setGroup(AudioGroup ag) {
		this.ag = ag;
	}

	public AudioGroup getGroup() {
		return ag;
	}

	public String getName() {
		return processName;
	}

	public static AudioProcess findProcess(String name) {
		for (AudioProcess ap : registeredProcesses) {
			if (ap.getName().equals(name))
				return ap;
		}
		return null;
	}

}
