package unagoclient.igs.games;

import unagoclient.igs.Distributor;
import unagoclient.igs.IgsStream;

/**
 * The distributor to receive the games from the server. It assumes that there
 * is already an open games frame. This must be so, because sometimes the games
 * are displayed in an old GamesFrame via refresh.
 */

public class GamesDistributor extends Distributor {
    GamesFrame P;

    public GamesDistributor(IgsStream in, GamesFrame p) {
        super(in, 7, 0, true);
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
