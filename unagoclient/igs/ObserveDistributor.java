package unagoclient.igs;

import unagoclient.Dump;

/**
 * This distributor takes moves from the IgsStream and sends them to a
 * GoObserver, which is opened elswhere.
 */

public class ObserveDistributor extends Distributor {
    GoObserver P;
    boolean Blocked;

    public ObserveDistributor(IgsStream in, GoObserver p, int n) {
        super(in, 15, n, false);
        this.P = p;
        this.Blocked = true;
    }

    @Override
    public void allsended() {
        this.P.sended();
    }

    @Override
    public boolean blocked() {
        if (this.Playing) {
            return false;
        } else {
            return this.Blocked;
        }
    }

    @Override
    public boolean newmove() {
        return this.P.newmove();
    }

    @Override
    public void pass() {
        Dump.println("Observe Distributor got a pass");
        this.P.pass();
    }

    @Override
    public void refresh() {
        this.P.refresh();
    }

    @Override
    public void remove() {
        this.P.remove();
    }

    @Override
    public void send(String c) {
        this.P.receive(c);
    }

    public void set(int i, int j) {
        Dump.println("Observe Distributor got move at " + i + "," + j);
        this.P.set(i, j);
    }

    @Override
    public boolean started() {
        return this.P.started();
    }
}
