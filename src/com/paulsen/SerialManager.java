package com.paulsen;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.fazecast.jSerialComm.SerialPort;
import com.paulsen.audiocontrol.AudioControl;
import com.paulsen.audiocontrol.CustomProtocol;

public class SerialManager {

	public Runnable disconnectAction;

	private SerialPort chosenPort;
	private volatile ArrayList<String> portList = new ArrayList<>();

	private Thread t;
	private boolean isConnected = false;
	private int lastConnectedIndex;

	private CustomProtocol amProtocol;

	public SerialManager() {
		amProtocol = new CustomProtocol("am", '[', ']', "^<<^", "^>>^");
		/*
		 * Arduino:
		 * am[CTRL|=>1023]
		 * 
		 * Message:
		 * CTRL|=>1023
		 * 
		 * Values:
		 * CTRL 1023
		 * 
		 */
		updatePorts();
	}

	private void initThread() {
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Scanner scn = new Scanner(chosenPort.getInputStream());
				while (isConnected) {
					try {
						String s = scn.nextLine();
						if (amProtocol.isPartOfProtocol(s)) {
							String output[] = getData(amProtocol.getMessage(s));

							String controlName = output[0];
							float volume = ((float) Integer.parseInt(output[1])) / 1024;

							AudioControl ac = Main.am.ui.getAudioControl(controlName);
							if (ac != null) {
								ac.setVolume(volume);
							} else {
								Main.am.ui.addAudioControl(controlName);
							}
						}
					} catch (NumberFormatException e) {
						System.err.println("[SerialManager] :: Error with communikation!");
					} catch (NoSuchElementException e) {
						scn.close();
						isConnected = false;
						System.out.println("[SerialManager] :: Connection closed");
						try {
							disconnectAction.run();
							return;
						} catch (NullPointerException e2) {
							System.err.println("[SerialManager] :: Something went wrong!");
						}
						return;
					} catch (Exception e) {
						System.err.println("[SerialManager] :: Error with communikation 2!");
					}
				}
				disconnectAction.run();
				scn.close();
			}
		});
		t.start();
	}

	public boolean connect(String portname) {
		for (int i = 0; i < portList.size(); i++) {
			if (portList.get(i).equals(portname)) {
				return connect(i);
			}
		}
		return false;
	}

	public synchronized boolean connect(int portIndex) {
		lastConnectedIndex = portIndex;
		if (!isConnected) {
			chosenPort = SerialPort.getCommPort(portList.get(portIndex));
			chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
			chosenPort.openPort();
			initThread();
			isConnected = true;
			System.out.println("[SerialManager] :: connected");
			return true;
		} else if (disConnect())
			return connect(portIndex);
		return false;
	}

	public synchronized boolean disConnect() {
		if (isConnected) {
			t.interrupt();
			chosenPort.closePort();
			isConnected = false;
			System.out.println("[SerialManager] :: disconnected");
			return true;
		}
		return false;
	}

	public void updatePorts() {
		portList.clear();
		SerialPort[] portNames = SerialPort.getCommPorts();
		for (int i = 0; i < portNames.length; i++) {
			portList.add(portNames[i].getSystemPortName().toString());
		}
	}

	public String getCurrentPortName() {
		if (chosenPort != null)
			return portList.get(lastConnectedIndex);
		return "";
	}

	public boolean isConnected() {
		return isConnected;
	}

	public ArrayList<String> getPortList() {
		return portList;
	}

	// bsp: pot1|=>1024
	private String[] getData(String in) {
		String name = "";
		String num = "";

		boolean hasName = false;
		for (int i = 0; i < in.length(); i++) {
			if (!hasName) {
				if (in.charAt(i) == '|') {
					i += 2; // jump over "=>"
					hasName = true;
				} else {
					name += in.charAt(i);
				}
			} else {
				num += in.charAt(i);
			}
		}

		String s[] = { name, num };
		return s;
	}
}
