package unagoclient.igs.who;

import unagoclient.igs.Distributor;
import unagoclient.igs.IgsStream;

/**
 * A distributor for the player listing.
 */

public class WhoDistributor extends Distributor {
    WhoFrame P;

    public WhoDistributor(IgsStream in, WhoFrame p) {
        super(in, 27, 0, true);
        this.P = p;
    }

    @Override
    public void allsended() {
        this.P.allsended();
    }

    @Override
    public void send(String c) {
        this.P.receive(c);
    }
}
