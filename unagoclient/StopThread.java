package unagoclient;

/**
 * A thread with a Stopped flag.
 */

public class StopThread extends Thread {
    boolean Stopped = false;

    public void stopit() {
        this.Stopped = true;
    }

    public boolean stopped() {
        return this.Stopped;
    }
}