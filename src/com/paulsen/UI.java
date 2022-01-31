package com.paulsen;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.paulsen.audiocontrol.AudioControl;
import com.paulsen.audiocontrol.AudioGroup;
import com.paulsen.audiocontrol.AudioProcess;
import com.paulsen.ui.*;

public class UI extends JLabel {
	private static final long serialVersionUID = 1L;

	class WindowEventHandler implements WindowListener {

		public void windowClosing(WindowEvent evt) {
			if (getUserConfirm("Really Close Audio Coltroller", "Audio Controller")) {
				Main.exitAll();
			}
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
			jf.setVisible(false);
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}
	}

	public PUIScrollPanel audioControlUI;
	PUIText minimizeUI, settingsUI;

	PUIText pythonDisplay, serialDisplay;

	// Settings/Menu
	public PUIScrollPanel groupUI, processUI;
	PUIText serialConnectUI, addGroupUI, deleteGroupUI, refresh, processesText;

	Image img;

	JFrame jf;
	public AudioManager am;
	public int w = 1300, h = 600;
	public boolean isMenu = false;
	private boolean hasInit = false;

	public AudioGroup selectedGroup = null;

	public UI(AudioManager am) {

		// PUI - DarkMODE
		PUIElement.darkUIMode = true;

		img = Toolkit.getDefaultToolkit().getImage(Main.SAVEFOLDER + "/Audio.png");

		this.am = am;
		jf = new JFrame("Serial Audio Controller");
		jf.setIconImage(img);
		jf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		jf.setSize(w, h);
		jf.setMinimumSize(new Dimension(600, 400));
		jf.setLocationRelativeTo(null);
		jf.addWindowListener(new WindowEventHandler());

		jf.add(this);
//		jf.setVisible(true);

		initSystemTray();
		initElements();
		initTimer();

		hasInit = true;
	}

	public AudioControl addAudioControl(String name) {
		for (PUIElement e : audioControlUI.getElements())
			if (((AudioControl) e).getName().equals(name))
				return (AudioControl) e;

		AudioControl ac = new AudioControl(this, name, new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				for (PUIElement e : audioControlUI.getElements())
					if (((AudioControl) e).getName().equals(name)) {

						if (e.getUserConfirm("Delete Audio-Control: \"" + ((AudioControl) e).getName() + '"', "")) {
							audioControlUI.removeElement(e);
							Main.deleteControlFiles(((AudioControl) e));
							repaint();
						}
						return;
					}
			}
		});
		ac.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				repaint();
			}
		});
		audioControlUI.addElement(ac);
		return ac;
	}

	public AudioControl getAudioControl(String name) {
		for (PUIElement e : audioControlUI.getElements())
			if (((AudioControl) e).getName().equals(name))
				return (AudioControl) e;
		return null;
	}

	public boolean hasAudioControl(String name) {
		return getAudioControl(name) != null;
	}

	public void initSystemTray() {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			PopupMenu popup = new PopupMenu();

			MenuItem show = new MenuItem("SHOW/MINIMIZE");
			show.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jf.setState(JFrame.NORMAL);
					jf.setAlwaysOnTop(true);
					jf.setVisible(!jf.isVisible());
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
					}
					jf.setAlwaysOnTop(false);
				}
			});
			popup.add(show);

			MenuItem close = new MenuItem("-EXIT-");
			close.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jf.dispatchEvent(new WindowEvent(jf, WindowEvent.WINDOW_CLOSING));
				}
			});
			popup.add(close);

			TrayIcon trayIcon = new TrayIcon(img, "Serial Audio Controller", popup);
			trayIcon.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jf.setState(JFrame.NORMAL);
					jf.setAlwaysOnTop(true);
					jf.setVisible(!jf.isVisible());
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
					}
					jf.setAlwaysOnTop(false);
				}
			});

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				sendUserWarning("Couldn't load System Tray!");
			}
		}
	}

	public void initElements() {
		// AudioControl
		audioControlUI = new PUIScrollPanel(this);
		audioControlUI.setAlignment(PUIElement.ElementAlignment.HORIZONTAL);
		audioControlUI.setShowedElements(2);
		audioControlUI.setSliderWidth(h / 20);
		audioControlUI.addValueUpdateAction(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});
		audioControlUI.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				repaint();
			}
		});

		// Buttons
		minimizeUI = new PUIText(this, "M");
		minimizeUI.doPaintOverOnHover(false);
		minimizeUI.doPaintOverOnPress(false);
		minimizeUI.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				if (SystemTray.isSupported()) {
					jf.setVisible(false);
					System.out.println("Minimize");
				}
			}
		});
		settingsUI = new PUIText(this, "S");
		settingsUI.doPaintOverOnHover(false);
		settingsUI.doPaintOverOnPress(false);
		settingsUI.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				isMenu = !isMenu;
				updateElements();
			}
		});

		pythonDisplay = new PUIText(this, "Python Script");
		pythonDisplay.doPaintOverOnHover(false);
		pythonDisplay.doPaintOverOnPress(false);
		pythonDisplay.setTextColor(new Color(0, 0, 0, 100));

		serialDisplay = new PUIText(this, "Serial USB");
		serialDisplay.doPaintOverOnHover(false);
		serialDisplay.doPaintOverOnPress(false);
		serialDisplay.setTextColor(new Color(0, 0, 0, 100));

		groupUI = new PUIScrollPanel(this);
		groupUI.setShowedElements(6);
		groupUI.addValueUpdateAction(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});
		groupUI.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				repaint();
			}
		});

		processUI = new PUIScrollPanel(this);
		processUI.setShowedElements(10);
		processUI.addValueUpdateAction(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});
		processUI.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				repaint();
			}
		});

		refresh = new PUIText(this, "R");
		refresh.doPaintOverOnHover(false);
		refresh.doPaintOverOnPress(false);
		refresh.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				am.refreshProcess();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
				updateElements();
			}
		});

		processesText = new PUIText(this, "Processes:");
		processesText.doPaintOverOnHover(false);
		processesText.doPaintOverOnPress(false);
		processesText.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				am.refreshProcess();
				updateElements();
			}
		});

		addGroupUI = new PUIText(this, "+");
		addGroupUI.doPaintOverOnHover(false);
		addGroupUI.doPaintOverOnPress(false);
		addGroupUI.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				System.out.println("[UI] :: addGroup");
				String s = getUserInput("Add a New Group", "groupname");
				if (am.createNewGroup(s) == null) {
					sendUserWarning("No valid Groupname!\nGroup already exists or no name!");
				}
				updateElements();
			}
		});

		deleteGroupUI = new PUIText(this, "-");
		deleteGroupUI.doPaintOverOnHover(false);
		deleteGroupUI.doPaintOverOnPress(false);
		deleteGroupUI.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {
				System.out.println("[UI] :: deleteGroup");
				if (selectedGroup == null)
					return;
				boolean confirm = getUserConfirm("Do you really want to delete \"" + selectedGroup.getName() + "\"?",
						"DELETE group");
				if (confirm) {
					AudioGroup tempGroup = selectedGroup;
					selectedGroup = null;

					for (PUIElement e : audioControlUI.getElements()) {
						if (AudioControl.class == (e).getClass()) {
							((AudioControl) e).removeAudioGroup(tempGroup);
						}
					}
					groupUI.removeElement(tempGroup);
					am.deleteGroup(tempGroup);
				}
				updateElements();
			}
		});

		serialConnectUI = new PUIText(this, "USB: -");
		serialConnectUI.doPaintOverOnHover(false);
		serialConnectUI.doPaintOverOnPress(false);
		serialConnectUI.addActionListener(new PUIAction() {
			@Override
			public void run(PUIElement arg0) {

				am.serial.updatePorts();

				if (am.serial.isConnected()) {
					System.out.println("[UI] :: disconnect USB");
					am.serial.disConnect();
					serialConnectUI.setText("USB: -");
				} else {
					System.out.println("[UI] :: connect USB");

					int sel = getUserSelection("Choose your device:", am.serial.getPortList());
					if (!am.serial.connect(sel))
						System.out.println("something went wrong while connecting");

					serialConnectUI.setText("USB: " + am.serial.getCurrentPortName());
				}
				repaint();
			}
		});

	}

	public void initTimer() {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (w != getWidth() || h != getHeight()) {
					w = getWidth();
					h = getHeight();
					updateElements();
				}
			}
		}, 100, 10);
	}

	public synchronized void updateElements() {

		audioControlUI.setShowedElements(w / 400 + 1);
		audioControlUI.updateElements();
		audioControlUI.runAllValueUpdateActions();

		if (!isMenu) {

			audioControlUI.setBounds(0, 0, w, h - h / 8);
			audioControlUI.setSliderWidth(h / 20);
			minimizeUI.setBounds(0, h - h / 8, h / 8, h / 8);
			pythonDisplay.setBounds(h / 8, h - h / 8, w - h / 8 * 2, h / 16);
			serialDisplay.setBounds(h / 8, h - h / 16, w - h / 8 * 2, h / 16);
			pythonDisplay.setBackgroundColor(am.isPythonRunning() ? Color.green : Color.red);
			serialDisplay.setBackgroundColor(am.serial.isConnected() ? Color.green : Color.red);

			audioControlUI.setEnabled(true);
			minimizeUI.setEnabled(true);
			pythonDisplay.setEnabled(true);
			serialDisplay.setEnabled(true);

			groupUI.setEnabled(false);
			processUI.setEnabled(false);
			addGroupUI.setEnabled(false);
			deleteGroupUI.setEnabled(false);
			serialConnectUI.setEnabled(false);
			refresh.setEnabled(false);
			processesText.setEnabled(false);
		} else { // menu

			// update groups
			for (AudioGroup ag : am.getGroups()) {
				if (!groupUI.getElements().contains(ag)) {
					groupUI.addElement(ag);
				}
			}

			updateProcesses();

			if (!am.serial.isConnected())
				serialConnectUI.setText("USB: -");

			serialConnectUI.setBounds(0, 0, w / 2, h / 8);
			groupUI.setSliderWidth(w / 30);
			groupUI.setBounds(w / 32, h / 8 + h / 32, w / 2 - w / 32 * 2, h - h / 8 * 2 - h / 32);
			addGroupUI.setBounds(w / 4 - h / 8, h - h / 8, h / 8, h / 8);
			deleteGroupUI.setBounds(w / 4, h - h / 8, h / 8, h / 8);
			refresh.setBounds(w / 4 * 3 - h / 8 / 2, h - h / 8, h / 8, h / 8);
			processUI.setSliderWidth(w / 30);
			processUI.setBounds(w / 2 + w / 32, h / 8, w / 2 - w / 32 * 2, h - h / 8 * 2);
			processesText.setBounds(w / 2, 0, w / 2, h / 8);

			groupUI.setEnabled(true);
			processUI.setEnabled(true);
			addGroupUI.setEnabled(true);
			refresh.setEnabled(true);
			processesText.setEnabled(true);
			deleteGroupUI.setEnabled(true);
			serialConnectUI.setEnabled(true);

			audioControlUI.setEnabled(false);
			minimizeUI.setEnabled(false);
			pythonDisplay.setEnabled(false);
			serialDisplay.setEnabled(false);
		}
		settingsUI.setBounds(w - h / 8, h - h / 8, h / 8, h / 8);
		repaint();
	}

	protected void paintComponent(Graphics g) {
		if (!hasInit) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			repaint();
		}

		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (isMenu) {
			serialConnectUI.draw(g);
			groupUI.draw(g);
			addGroupUI.draw(g);
			deleteGroupUI.draw(g);
			refresh.draw(g);
			processesText.draw(g);
			processUI.draw(g);
		} else {
			audioControlUI.draw(g);
			minimizeUI.draw(g);
			pythonDisplay.draw(g);
			serialDisplay.draw(g);
		}
		settingsUI.draw(g);
	}

	/**
	 * removes Processes which arent running currently and are not used by any Group
	 */
	private void removeUnusedProcesses() {
		for (PUIElement e : processUI.getElements()) {
			AudioProcess ap = (AudioProcess) e;
			if (ap.getGroup() == null) {
				if (!am.avaliableProcesses.contains(ap.getName())) {
					processUI.removeElement(e);
					removeUnusedProcesses();
					return;
				}
			}
		}
	}

	public void updateProcesses() {// update Processes
		// add Processes that havent been added
		schleife: for (String nProcess : am.avaliableProcesses) {
			for (PUIElement e : processUI.getElements()) {
				AudioProcess ap = (AudioProcess) e;
				if (ap.getName().equals(nProcess)) {
					continue schleife;
				}
			}

			System.out.println("[UI] :: found new Process: " + nProcess);

			AudioProcess ap = new AudioProcess(am, nProcess);
			ap.addActionListener(new PUIAction() {
				@Override
				public void run(PUIElement arg0) {
					if (selectedGroup == null)
						return;

					if (ap.getGroup() != selectedGroup) {
						if (ap.getGroup() != null) { // process had group before

							boolean confirm = getUserConfirm(
									"Really want to add \"" + ap.getName()
											+ "\" to this GROUP and remove it from the GROUP \""
											+ ap.getGroup().getName() + "\"",
									"The Process you want to add is added by Group \"" + ap.getGroup().getName()
											+ "\"");
							if (!confirm)
								return;

							ap.getGroup().removeAudioProcess(ap);
						}
						ap.setGroup(selectedGroup);
						selectedGroup.addAudioProcess(ap);
					} else { // reset group
						ap.setGroup(null);
					}
					updateElements();
				}
			});
			processUI.addElement(ap);
		}

		// remove processes which are not available AND unused
		removeUnusedProcesses();

		// set Color according to selected Group
		for (PUIElement e : processUI.getElements()) {
			AudioProcess ap = (AudioProcess) e;
			if (selectedGroup != null) {
				if (am.avaliableProcesses.contains(ap.getName())) { // Process currentliy active
					if (ap.getGroup() == selectedGroup) { // process is in selected group
						e.setBackgroundColor(new Color(41, 237, 38));
					} else if (ap.getGroup() == null) { // process is in no group
						e.setBackgroundColor(new Color(255, 255, 255));
					} else { // process is in other group
						e.setBackgroundColor(new Color(255, 0, 0));
					}
				} else { // process not active but stored
					if (ap.getGroup() == selectedGroup) { // process is in selected group
						e.setBackgroundColor(new Color(21, 130, 20));
					} else if (ap.getGroup() == null) { // process is in no group
						e.setBackgroundColor(Color.DARK_GRAY);
					} else { // process is in other group
						e.setBackgroundColor(new Color(115, 0, 0));
					}
				}
			} else { // no group selected
				e.setBackgroundColor(Color.DARK_GRAY);
			}
		}
	}

	public String getUserInput(String message, String initialValue) {
		return JOptionPane.showInputDialog(this, message, initialValue);
	}

	public void sendUserError(String message) {
		JOptionPane.showMessageDialog(this, message, "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	public void sendUserWarning(String message) {
		JOptionPane.showMessageDialog(this, message, "WARNING", JOptionPane.WARNING_MESSAGE);
	}

	public void sendUserInfo(String message) {
		JOptionPane.showMessageDialog(this, message, "INFO", JOptionPane.INFORMATION_MESSAGE);
	}

	public boolean getUserConfirm(String message, String title) {
		return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
	}

	/**
	 * Creates a popup-window which lets u choose one of the Options
	 * 
	 * @param parent
	 * @param title
	 * @param comboBoxInput String-Array of Options
	 * @return index from 0 to comboBoxInput.length
	 */
	public int getUserSelection(String title, String comboBoxInput[]) {
		JComboBox<String> box = new JComboBox<>(comboBoxInput);
		JOptionPane.showMessageDialog(this, box, title, JOptionPane.QUESTION_MESSAGE);
		return box.getSelectedIndex();
	}

	public int getUserSelection(String title, ArrayList<String> comboBoxInput) {
		String s[] = new String[comboBoxInput.size()];
		for (int i = 0; i < s.length; i++)
			s[i] = comboBoxInput.get(i);
		return getUserSelection(title, s);
	}

}
