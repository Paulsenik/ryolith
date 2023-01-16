package ooo.paulsen.audiocontrol;

import ooo.paulsen.Main;
import ooo.paulsen.io.PCustomProtocol;

import java.io.*;
import java.util.*;

public class WinAudio {

    // TEST
    static long t;

    public static void testSpeed() throws InterruptedException {

        t = System.currentTimeMillis();

        WinAudio a = WinAudio.getInstance();

        int sample = 100;
        for (float i = 0; i < sample; i++) {
            a.setAudio("firefox.exe", i / sample);
        }

        System.out.println("end: " + (float) (System.currentTimeMillis() - t) / sample + "ms/command");

        Thread.sleep(100);
        List<String> l = a.getAudioList();
        for(String s : l)
            System.out.println(s);

        a.close();

    }

    public static void main(String[] args) throws InterruptedException {
        testSpeed();
    }
    //////////////////

    public static PCustomProtocol wac = new PCustomProtocol("wac", '[', ']', "^<<^", "^>>^");
    private static WinAudio instance;

    public static WinAudio getInstance() {
        if (instance == null) {
            try {
                instance = new WinAudio(Main.localEXEPath);
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return instance;
    }

    public void close() {
        run("exit");
        p.destroy();
    }

    public static synchronized List<String> getAudioList() {
        try {
            WinAudio wa = new WinAudio(Main.localEXEPath);
            List<String> l = wa.runOut("list");
            wa.close();
            return l;
        } catch (IOException e) {
            return null;
        }
    }

    public boolean setAudio(String channelname, float value) {
        String message = channelname + "|" + value;
        if (p.isAlive())
            return run(message);
        return false;
    }

    private Process p;
    private OutputStream out;
    private BufferedReader br;

    private WinAudio(String executable) throws IOException {
        p = Runtime.getRuntime().exec(executable);
        out = p.getOutputStream();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                WinAudio.getInstance().close();
            }
        }));
    }

    private boolean run(String message) {
        try {
            // when trying to list: simply exiting does list as well
            out.write((wac.getProtocolOutput(message) + "\n").getBytes());
            out.flush();
            return true;

        } catch (IOException e) {
            // programm terminated
            return false;
        }
    }

    private List<String> runOut(String message) {
        br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        try {
            // when trying to list: simply exiting does list as well
            out.write((wac.getProtocolOutput(message) + "\n").getBytes());
            out.write((wac.getProtocolOutput("exit") + "\n").getBytes());
            out.flush();

            ArrayList<String> l = new ArrayList<>();
            br.lines().forEach(line -> l.add(line));

            return l;

        } catch (IOException e) {
            // programm terminated
            return null;
        }
    }

}
