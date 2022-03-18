package ooo.paulsen.audiocontrol;

import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.paulsen.io.serial.PSerialConnection;
import com.paulsen.io.serial.PSerialListener;

public class AudioManager {

    public PSerialConnection serial;
    private PSerialListener listener;

    public AudioManager() {
        initListener();
    }

    public boolean connectToSerial(String port) {
        try {
            if (serial != null)
                serial.disConnect();

            serial = new PSerialConnection(port);
            serial.addListener(listener);
            return serial.connect();
        } catch (SerialPortInvalidPortException e) {
            return false;
        }
    }

    public boolean disconnectSerial() {
        if (serial != null)
            return serial.disConnect();
        return true;
    }

    private void initListener() {
        listener = new PSerialListener() {
            @Override
            public void readLine(String s) {
                System.out.println("Incomming from " + serial.getPortName() + ": " + s);
            }
        };
    }


    public boolean isSerialConnected() {
        if (serial != null)
            return serial.isConnected();
        return false;
    }

    public boolean isAudioConnected() {
        return false; // TODO
    }

}
