package unagoclient.igs;

/**
 * This class takes messages from IgsStream and handles it. Most of the time, it
 * has a client to send the message to. Sometimes it will open a new client
 * window.
 * <p>
 * The distributor has a number N, which is the command number it is wating for.
 * It has a second number G, which is used to store additional information. E.g.
 * PlayDistributor will store the game number there. IgsStream can ask the
 * distributor for these numbers and determine, if it should send the message to
 * this distributor.
 *
 * @see unagoclient.igs.IgsStream
 */

public class Distributor {
    int N; // number to expect
    int G; // game number, if applicable
    boolean Once = false; // needed only for one input
    IgsStream In;
    public boolean Playing;

    public Distributor(IgsStream in, int n, int game, boolean once) {
        this.N = n;
        this.G = game;
        in.distributor(this);
        this.Once = once;
        this.In = in;
        this.Playing = false;
    }

    public void allsended() {
    }

    // (called from IgsStream at connection end)
    public boolean blocked() {
        return false;
    }

    public void finished() {
    } // message for a once-distributor at end

    public int game() {
        return this.G;
    }

    public void game(int n) {
    }

    public boolean newmove() {
        return false;
    }

    public int number() {
        return this.N;
    }

    public boolean once() {
        return this.Once;
    }

    public void out(String s) {
        this.In.out(s);
    }

    public void pass() {
    }

    public void refresh() {
    }

    public void remove() {
    } // remove client

    public void send(String C) {
    }

    public void set(int i, int j, int sec) {
    }

    public boolean started() {
        return false;
    }

    public void unchain() {
        this.In.unchain(this);
    }

    public boolean wantsmove() {
        return this.Playing;
    }
}
