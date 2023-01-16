package ooo.paulsen.audiocontrol;

import ooo.paulsen.Main;
import ooo.paulsen.io.PCustomProtocol;

import java.io.*;
import java.util.*;

public class WinAudio {

    public static PCustomProtocol wac = new PCustomProtocol("wac", '[', ']', "^<<^", "^>>^");

    static long t;
    public static void main(String[] args) {

        t = System.currentTimeMillis();

        getAudioList();
        //setAudio("firefox.exe", 1);
        System.out.println("end: "+(System.currentTimeMillis() - t));

    }

    public static List<String> getAudioList() {
        try {
            return run(Main.localEXEPath, "list");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setAudio(String channelname, float value) {
        String message = channelname + "|" + value;
        try {
            run(Main.localEXEPath, message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Process p;

    public WinAudio(String executable) throws IOException {
        p = Runtime.getRuntime().exec(executable);
    }

    private static ArrayList<String> run(String command) throws IOException {

        OutputStream out = p.getOutputStream();

        // when trying to list: simply exiting does list as well
        if (!command.contentEquals(wac.getProtocolOutput("list"))) {
            out.write((wac.getProtocolOutput(command) + "\n").getBytes());
        }

        out.flush();

        return getOutput(p);
    }

    private static ArrayList<String> getOutput(Process p) {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        br.lines().forEach(line -> System.out.println(line));
        return lines;
    }

}
