package de.paulsenik.ryolith.audiocontrol;

import de.paulsenik.ryolith.Control;
import de.paulsenik.ryolith.Main;
import de.paulsenik.jpl.io.PCustomProtocol;
import de.paulsenik.jpl.io.serial.PSerialConnection;
import de.paulsenik.jpl.io.serial.PSerialListener;
import de.paulsenik.jpl.utils.PSystem;

import java.util.ArrayList;

public class AudioManager {

    public static final int MAX_RECONNECT_TRIES = 5;
    public static final int RECONNECT_DELAY = 1000;

    public static boolean autoConnect = true;
    // TODO add to config & UiElement
    public static boolean autoReconnect = true;
    public static String lastPort = "";

    // Serial
    private PSerialConnection serial;
    private PSerialListener listener;
    private PCustomProtocol audioControlProtocol_NEW, audioControlProtocol_OLD;
    private int reconnectCount = 0;

    // AudioControl
    private AudioController controller;
    private ArrayList<String> processes = new ArrayList<>();

    /**
     * @throws Exception when the Controller is not supporting the current Operating-System
     */
    public AudioManager() throws Exception {
        initListener();
        initController();

        audioControlProtocol_OLD = new PCustomProtocol("am", '[', ']', "^{^", "^}^");
        audioControlProtocol_NEW = new PCustomProtocol("ac", '[', ']', "^{^", "^}^");
    }

    private void initController() throws Exception {

        switch (PSystem.getOSType()) {
            case LINUX: {
                System.out.println("[AudioManager] :: Detected OS: Linux");
                controller = new AudioControllerLinux();
                break;
            }
            case WINDOWS: {
                System.out.println("[AudioManager] :: Detected OS: Windows");
                controller = new AudioControllerWin();
                break;
            }
            default: {
                throw new Exception("This Operating-System is not supported!\nDetected: " + PSystem.getOSType());
            }
        }
    }

    private void initListener() {
        listener = new PSerialListener() {
            @Override
            public synchronized void readLine(String s) {
//                System.out.println(System.currentTimeMillis() + " Incomming from " + serial.getPortName() + ": " + s);

                String message1 = audioControlProtocol_NEW.getMessage(s); // new Arduino-Software
                String message2 = audioControlProtocol_OLD.getMessage(s); // old Arduino-Software (deprecated)

                if (message1 != null || message2 != null) {
                    if (message1 != null)
                        s = message1.replace("=>", "");
                    else
                        s = message2.replace("=>", "");

                    String controlName = "", volumeInt = "";
                    boolean hasName = false;
                    for (int i = 0; i < s.length(); i++) {
                        if (hasName)
                            volumeInt += s.charAt(i);
                        else if (s.charAt(i) == '|')
                            hasName = true;
                        else {
                            controlName += s.charAt(i);
                        }
                    }

                    if (Control.getControl(controlName) == null) {
                        new Control(controlName);
                    }

                    try {
                        float controlVolume = (float) Math.min(Math.max(Integer.parseInt(volumeInt), 0), 1000) / 1000;

                        Main.setControlVolume(controlName, controlVolume);
                    } catch (NumberFormatException e) {
                    }
                }

                // if s is part of no protocol the line gets ignored!
            }
        };
    }

    /**
     * Event-Function is called when
     */
    private void disconnected() {
        Main.ui.updateCurrentSerialConnection();
        System.out.println("[AudioManager] :: disconnected");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (reconnectCount = 0; !serial.isConnected() && reconnectCount < MAX_RECONNECT_TRIES; reconnectCount++) {
                        System.out.println("[AudioManager] :: reconnecting " + reconnectCount + "/" + MAX_RECONNECT_TRIES);

                        connectToSerial(AudioManager.lastPort);

                        Thread.sleep(RECONNECT_DELAY);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (!serial.isConnected()) {
                    System.out.println("[AudioManager] :: reconnecting failed");
                } else {
                    Main.ui.updateCurrentSerialConnection();
                }
            }
        });
        t.start();
    }

    public boolean connectToSerial(String port) {
        try {
            if (serial != null)
                serial.disconnect();

            serial = new PSerialConnection(port);
            serial.setDisconnectEvent(this::disconnected);
            serial.addListener(listener);
            return serial.connect();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean disconnectSerial() {
        if (serial != null)
            return serial.disconnect();
        return true;
    }

    /**
     * Uses exponential use of Volume
     *
     * @param process
     * @param volume
     */
    public void setVolume(String process, float volume) {
        controller.setVolume(process, (float) Math.pow(volume, 1.7));
    }

    public ArrayList<String> getProcesses() {
        for (String s : processes)
            System.out.println("- " + s);
        return processes;
    }

    public void refreshProcesses() {
        if (controller != null) {
            controller.refreshProcesses();
            processes = controller.getProcesses();
        }
    }

    /**
     * exits Serial and AudioController
     */
    public void stop() {
        disconnectSerial();
        controller.stop();
    }

    public String getPortName() {
        if (serial != null)
            return serial.getPortName();
        return null;
    }

    public boolean isSerialConnected() {
        if (serial != null)
            return serial.isConnected();
        return false;
    }

    public boolean isAudioConnected() {
        return controller.isAudioConnected();
    }

}
