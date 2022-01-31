package com.paulsen;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.paulsen.audiocontrol.AudioGroup;
import com.paulsen.audiocontrol.AudioProcess;
import com.paulsen.audiocontrol.CustomProtocol;
import com.paulsen.ui.PUIAction;
import com.paulsen.ui.PUIElement;

public class AudioManager {

	public ArrayList<AudioGroup> allGroups = new ArrayList<AudioGroup>();

	/**
	 * List of Processes that have been reported by the last time Python exited
	 */
	public ArrayList<String> avaliableProcesses = new ArrayList<>();

	public UI ui;
	public SerialManager serial; // Arduino

	private CustomProtocol pac;

	private final String pyLocation = Main.SAVEFOLDER + "/Python/WinAudioControl.py";
	private Process python = null;
	private OutputStream out; // python Input

	public AudioManager() {
		pac = new CustomProtocol("[PAC]::AudioProcess", '[', ']', "^<<^", "^>>^");
		refreshProcess();

		serial = new SerialManager();
		serial.disconnectAction = new Runnable() {
			@Override
			public void run() {
				ui.updateElements();
			}
		};
	}

	public void setUI(UI ui) {
		this.ui = ui;
	}

	public void closePython() {
		if (python != null && python.isAlive()) {
			try {
				out.write((("[PAC]::request[exit]\n").getBytes()));
				out.flush();
			} catch (IOException e) {
			}
		}
	}

	public boolean isPythonRunning() {
		return (python != null && python.isAlive());
	}

	public synchronized void refreshProcess() {

		avaliableProcesses.clear();

		closePython();

		try {
			python = Runtime.getRuntime().exec("py " + pyLocation);
			initReader();
			out = python.getOutputStream();
		} catch (IOException e) {
			System.err.println("NO PYTHONSCRIPT found");
		}
	}

	@SuppressWarnings("resource")
	private void initReader() {
		Scanner scn = new Scanner(python.getInputStream());
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (scn.hasNext()) {
					String line = scn.nextLine();
					String message = pac.getMessage(line);
					if (message != null) {
						// message-example: "steam.exe|=>0.4869283139705658"
						String processName = extractName(message);
						if (processName != null)
							avaliableProcesses.add(processName);
					}
					System.out.println("PyReader >> " + line);
				}
			}
		}, 0, 1);

		Scanner scnError = new Scanner(python.getErrorStream());
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (scnError.hasNext()) {
					System.err.println("PyErrorReader >> " + scnError.nextLine());
					ui.sendUserError("SOME ERROR OCCURED WITH THE PYTHON SCRIPT!");
					refreshProcess();
					return;
				}
			}
		}, 0, 1);
	}

	// is only called by AudioProcess
	public synchronized void setVolume(String processname, float volume) {
		if (python.isAlive() && out != null)
			try {
				String sOut = pac.getProtocolOutput(processname + "|=>" + volume) + "\n";
//				System.out.println("OUTPUT: " + sOut);
				out.write((sOut.getBytes()));
				out.flush();
			} catch (IOException e) {
			}
	}

	public AudioGroup createNewGroup(String s) {
		if (s == null)
			return null;
		for (AudioGroup ag : allGroups)
			if (ag.getName().equals(s))
				return null;
		AudioGroup ag = new AudioGroup(ui, s);
		ag.doPaintOverOnHover(false);
		ag.doPaintOverOnPress(false);
		ag.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				if (ui.selectedGroup == ag) {
					ui.selectedGroup = null;
					ag.setBackgroundColor(Color.LIGHT_GRAY);
				} else {
					ui.selectedGroup = ag;
					for (AudioGroup g : allGroups)
						g.setBackgroundColor(Color.LIGHT_GRAY);
					ag.setBackgroundColor(Color.green);
				}
				ui.updateElements();
			}
		});
		allGroups.add(ag);
		return ag;
	}

	public void deleteGroup(AudioGroup ag) {
		for (AudioProcess ap : ag.getProcesses()) {
			ap.setGroup(null);
		}
		Main.deleteGroupFile(ag);
		allGroups.remove(ag);
	}

	public ArrayList<AudioGroup> getGroups() {
		return allGroups;
	}

	public AudioGroup getGroup(String name) {
		for (AudioGroup ag : allGroups)
			if (ag.getName().equals(name))
				return ag;
		return null;
	}

	private String extractName(String message) {
		String s = "";
		for (int i = 0; i < message.length(); i++) {
			if (message.charAt(i) == '|')
				break;
			s += message.charAt(i);
		}
		return s;
	}

}
