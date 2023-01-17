package ooo.paulsen.audiocontrol;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AudioController {

    public class AudioProcess {

        private String name;
        private float volume;

        AudioProcess(String name, float volume) {
            this.name = name;
            this.volume = volume;
        }

        public String getName() {
            return name;
        }

        public float getVolume() {
            return volume;
        }

        public void setVolume(float volume) {
            this.volume = volume;
        }

    }

    // Threadsafe
    private CopyOnWriteArrayList<AudioProcess> processQueue = new CopyOnWriteArrayList<>();

    private Thread queueWorker;

    /**
     * @throws Exception when controlling is not possible
     */
    public AudioController() {
        initProcessThread();
    }

    /**
     * Adds Process and its volume to a queue.<br>
     * If queue already has process, the volume-parameter is adjusted to the new one of this queue-object
     *
     * @param processName if null or process is not found: Nothing happens
     * @param volume      must be (0 <= volume <= 1)
     */
    public synchronized final void setVolume(String processName, float volume) {
        if (processName == null)
            return;
        volume = Math.min(1.0f, Math.max(0.0f, volume));

        AudioProcess temp = getProcess(processName);
        if (temp == null) { // AudioProcess doesnt exist yet
            processQueue.add(new AudioProcess(processName, volume));
        } else {
            temp.setVolume(volume);
        }
    }

    /**
     * inits Thread that handles queue and calls {@link #setProcessVolume(String, float) setProcessVolume(,)}
     */
    private void initProcessThread() {
        queueWorker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!queueWorker.isInterrupted()) {

                    try {
                        if (processQueue.size() > 0) { // something is in the queue
                            AudioProcess p;
                            p = processQueue.remove(0);

                            if (p != null) {
                                setProcessVolume(p.getName(), p.getVolume());
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    } catch (InterruptedException e) {
                        System.err.println("[AudioController] :: Thread interrupted and stopped");
                        return;
                    } catch (ConcurrentModificationException e) {
                        e.printStackTrace();
                    }

                }
                System.out.println("[AudioController] :: Thread stopped");
            }
        });
        queueWorker.start();
    }

    /**
     * @param name of process
     * @return AudioProcess of processQueue with same name
     */
    private AudioProcess getProcess(String name) {
        for (AudioProcess a : processQueue) {
            if (a != null && a.getName().equals(name))
                return a;
        }
        return null;
    }

    /**
     * refreshes the internal list of Processes
     */
    public abstract boolean refreshProcesses();

    /**
     * @return all audio-processes the OS can find (stored in Controller as variable)
     */
    public abstract ArrayList<String> getProcesses();

    /**
     * Sets the volume of the given Process if it's available<br>
     *
     * @param processName if null or process is not found: Nothing happens
     * @param volume      must be (0 <= volume <= 1)
     * @apiNote <b>Not recommended for normal use! Use {@link #setVolume(String, float) setVolume(,)} instead</b>
     */
    protected abstract void setProcessVolume(String processName, float volume);

    /**
     * @return true: if audio can be manipulated; false: if not
     */
    public abstract boolean isAudioConnected();

    /**
     * Stops Thread
     */
    public void stop() {
        queueWorker.interrupt();
    }

}
