package com.paulsen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import com.paulsen.audiocontrol.AudioControl;
import com.paulsen.audiocontrol.AudioGroup;
import com.paulsen.audiocontrol.AudioProcess;
import com.paulsen.io.*;
import com.paulsen.ui.PUIText;

/**
 *
 * @author Paul
 * @since 2021/08/04 => 2021/08/07
 */
public class Main {

	public static final String SAVEFOLDER = "AudioController";

	public static AudioManager am;

	private static final boolean devMode = false;

	public static void main(String[] args) {

		if (!devMode) {

			if (!new File(SAVEFOLDER).exists())
				PFolder.createFolder(SAVEFOLDER);

			// Set Console out
			if (new File(SAVEFOLDER + "/consoleOut.txt").exists())
				new PFile(SAVEFOLDER + "/consoleOut.txt").delete();
			PrintStream out;
			try {
				out = new PrintStream(new FileOutputStream(SAVEFOLDER + "/consoleOut.txt"));
				System.setOut(out);
				System.setErr(out);
			} catch (FileNotFoundException e) {
			}
		}

		new Main();

		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				Main.saveVariables();
			}
		}, 0, 10 * 60 * 1000);
	}

	public Main() {
		am = new AudioManager();

		UI ui = new UI(am);
		am.setUI(ui);

		try {
			initVariables();
			ui.updateElements();
		} catch (Exception e) {
			e.printStackTrace();
			ui.sendUserError("An Error Occured!\nPlease check consoleOut.txt !!!");
		}
	}

	public static void initVariables() {

		if (!existNecessaryFiles()) {
			return;
		}
		// processes
		readProcesses();
		am.ui.updateProcesses();

		// groups
		readGroups();

		// controls
		readControls();

		readSerial();

		readSettings();

		System.out.println("[Main] :: initialized variables");
	}

	private static boolean existNecessaryFiles() {

		if (!new File(SAVEFOLDER + "/" + "Processes/processes.pstorage").exists())
			return false;
		if (!new File(SAVEFOLDER + "/groups.pstorage").exists())
			return false;
		if (!new File(SAVEFOLDER + "/controls.pstorage").exists())
			return false;

		return true;
	}

	public static void exitAll() { // save & exit
		am.closePython();
		System.out.println("[WindowAdapter] :: closed python");
		try {
			Main.saveVariables();
		} catch (Exception e) {
			e.printStackTrace();
			am.ui.sendUserError("An Error Occured!\nPlease check consoleOut.txt !!!");
		}
		System.exit(0);
	}

	private static void readSettings() {
		PDataStorage settings = new PDataStorage();
		settings.read(SAVEFOLDER + "/" + "settings.pstorage");

		try {
			boolean minimized = settings.getBoolean("minimized");
			if (!minimized) {
				am.ui.jf.setVisible(true);
			}
		} catch (Exception e) {
			am.ui.jf.setVisible(true);
		}
	}

	private static void readProcesses() {
		PDataStorage processes = new PDataStorage();
		processes.read(SAVEFOLDER + "/" + "Processes/processes.pstorage");

		int size = processes.getInteger("size");
		for (int i = 0; i < size; i++) {
			try {
				am.avaliableProcesses.add(processes.getString("" + i));
			} catch (Exception e) {
				e.printStackTrace();
				am.ui.sendUserWarning(
						"An Error Occured with loading Controls!\nPlease check consoleOut.txt after closing!!!");
			}
		}
	}

	private static void readGroups() {
		PDataStorage groupInfo = new PDataStorage();
		groupInfo.read(SAVEFOLDER + "/groups.pstorage");

		int size = groupInfo.getInteger("size");
		for (int i = 0; i < size; i++) {
			try {
				AudioGroup g = am.createNewGroup(groupInfo.getString(i + ""));

				PDataStorage groupStorage = new PDataStorage();
				groupStorage.read(SAVEFOLDER + "/Groups/" + g.getName() + ".pstorage");

				int size2 = groupStorage.getInteger("size");
				for (int j = 0; j < size2; j++) {
					String processName = groupStorage.getString("" + j);
					AudioProcess ap = AudioProcess.findProcess(processName);
					if (ap != null) {
						g.addAudioProcess(ap);
						ap.setGroup(g);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				am.ui.sendUserWarning(
						"An Error Occured with loading Controls!\nPlease check consoleOut.txt after closing!!!");
			}
		}
	}

	private static void readControls() {
		PDataStorage controlInfo = new PDataStorage();
		controlInfo.read(SAVEFOLDER + "/controls.pstorage");

		int size = controlInfo.getInteger("size");
		for (int i = 0; i < size; i++) {
			try {
				AudioControl ac = am.ui.addAudioControl(controlInfo.getString("" + i));

				// control already exists
				if (ac == null)
					continue;

				try {
					ac.setVolume(controlInfo.getFloat(i + "v"));
					ac.rotaryVolumeControlUI.setValue(controlInfo.getFloat(i + "v"));
				} catch (Exception e) {
				}

				PDataStorage controlStorage = new PDataStorage();
				controlStorage.read(SAVEFOLDER + "/" + "Controls/" + ac.getName() + ".pstorage");

				int size2 = controlStorage.getInteger("size");
				for (int j = 0; j < size2; j++) {
					String groupName = controlStorage.getString("" + j);
					ac.addAudioGroup(am.getGroup(groupName));
				}
			} catch (Exception e) {
				e.printStackTrace();
				am.ui.sendUserWarning(
						"An Error Occured with loading Controls!\nPlease check consoleOut.txt after closing!!!");
			}
		}
	}

	private static void readSerial() {
		PDataStorage serialStorage = new PDataStorage();
		serialStorage.read(SAVEFOLDER + "/serial.pstorage");

		try {
			String port = serialStorage.getString("port");
			if (port != null) {
				am.serial.updatePorts();
				am.serial.connect(port);
				if (am.serial.isConnected())
					am.ui.serialConnectUI.setText("USB: " + am.serial.getCurrentPortName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			am.ui.sendUserWarning(
					"An Error Occured with loading Controls!\nPlease check consoleOut.txt after closing!!!");
		}
	}

	public static void saveVariables() {

		PFolder.createFolder(SAVEFOLDER);
		PFolder.createFolder(SAVEFOLDER + "/" + "Processes");
		PFolder.createFolder(SAVEFOLDER + "/" + "Groups");
		PFolder.createFolder(SAVEFOLDER + "/" + "Controls");

		// Processes
		PDataStorage processes = new PDataStorage();
		processes.add("size", am.ui.processUI.getElements().size());
		for (int i = 0; i < am.ui.processUI.getElements().size(); i++) {
			processes.add(i + "", ((PUIText) am.ui.processUI.getElements().get(i)).getText());
		}
		processes.save(SAVEFOLDER + "/" + "Processes/processes.pstorage");

		// Groups
		PDataStorage groupNameStorage = new PDataStorage();
		groupNameStorage.add("size", am.allGroups.size());
		for (int j = 0; j < am.allGroups.size(); j++) {
			AudioGroup g = am.allGroups.get(j);
			groupNameStorage.add("" + j, g.getName());

			PDataStorage groupStorage = new PDataStorage();
			groupStorage.add("size", g.getProcesses().size());
			for (int i = 0; i < g.getProcesses().size(); i++) {
				if (g.getProcesses().get(i) != null)
					groupStorage.add("" + i, g.getProcesses().get(i).getName());
			}
			groupStorage.save(SAVEFOLDER + "/Groups/" + g.getName() + ".pstorage");
		}
		groupNameStorage.save(SAVEFOLDER + "/groups.pstorage");

		// Controls
		PDataStorage controlNameStorage = new PDataStorage();
		controlNameStorage.add("size", am.ui.audioControlUI.getElements().size());
		for (int j = 0; j < am.ui.audioControlUI.getElements().size(); j++) {
			AudioControl ac = (AudioControl) am.ui.audioControlUI.getElements().get(j);
			controlNameStorage.add("" + j, ac.getName());
			controlNameStorage.add(j + "v", ac.getVolume());

			PDataStorage controlStorage = new PDataStorage();
			controlStorage.add("size", ac.getGroups().size());
			for (int i = 0; i < ac.getGroups().size(); i++) {
				controlStorage.add("" + i, ac.getGroups().get(i).getName());
			}
			controlStorage.save(SAVEFOLDER + "/" + "Controls/" + ac.getName() + ".pstorage");
		}
		controlNameStorage.save(SAVEFOLDER + "/controls.pstorage");

		// Serial
		PDataStorage serialStorage = new PDataStorage();
		serialStorage.add("port", am.serial.getCurrentPortName());
		serialStorage.save(SAVEFOLDER + "/serial.pstorage");

		// Settings
		PDataStorage settings = new PDataStorage();
		settings.add("minimized", !am.ui.jf.isVisible());
		settings.save(SAVEFOLDER + "/" + "settings.pstorage");

		System.out.println("[Main] :: saved variables");
	}

	public static void deleteGroupFile(AudioGroup g) {
		PFile.deleteFile(SAVEFOLDER + "/Groups/" + g.getName() + ".pstorage");
	}

	public static void deleteControlFiles(AudioControl ac) {
		PFile.deleteFile(SAVEFOLDER + "/Controls/" + ac.getName() + ".pstorage");
	}

}
