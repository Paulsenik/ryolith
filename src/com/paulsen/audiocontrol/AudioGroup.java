package com.paulsen.audiocontrol;

import java.util.ArrayList;

import com.paulsen.UI;
import com.paulsen.ui.PUIText;

public class AudioGroup extends PUIText {

	private volatile ArrayList<AudioProcess> processes = new ArrayList<>();

	private String name;

	public AudioGroup(UI c, String name) {
		super(c, name);
		this.name = name;
	}

	public synchronized void addAudioProcess(AudioProcess p) {
		processes.add(p);
	}

	public synchronized boolean removeAudioProcess(AudioProcess p) {
		return processes.remove(p);
	}

	public void setVolume(float volume) {
		for (AudioProcess ap : processes)
			ap.setVolume(volume);
	}

	public String getName() {
		return name;
	}

	public ArrayList<AudioProcess> getProcesses() {
		return processes;
	}

}
