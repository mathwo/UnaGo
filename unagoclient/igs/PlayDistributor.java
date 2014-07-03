package unagoclient.igs;

import unagoclient.Dump;
import unagoclient.Global;

import java.io.PrintWriter;

/**
 * The PlayDistributor is opened with a ConnectionFrame to display tha board.
 * When its game method is invoked, it will start a Player object and send
 * output to it.
 *
 * @see unagoclient.igs.Player
 */

public class PlayDistributor extends Distributor {
    Player P;
    IgsStream In;
    PrintWriter Out;
    ConnectionFrame F;

    public PlayDistributor(ConnectionFrame f, IgsStream in, PrintWriter out) {
        super(in, 15, -1, false);
        this.F = f;
        this.In = in;
        this.Out = out;
        this.P = null;
        this.Playing = true;
    }

    /**
     * This method opens an IgsGoFrame and a Player to handle the moves.
     */
    @Override
    public void game(int n) {
        Dump.println("Opening go frame for game " + n);
        final IgsGoFrame gf = new IgsGoFrame(this.F,
                Global.resourceString("Play_game"));
        gf.distributor(this);
        gf.Playing.setState(true);
        gf.setVisible(true);
        gf.repaint();
        this.P = new Player(gf, this.In, this.Out, this);
        this.G = n;
        this.P.game(n);
        new PlayDistributor(this.F, this.In, this.Out);
    }

    /**
     * called from the goframe to pass (passed to Player)
     */
    @Override
    public void pass() {
        Dump.println("Play Distributor got a pass");
        this.P.pass();
    }

    /**
     * called from the goframe to refresh the board (passed to Player)
     */
    @Override
    public void refresh() {
        this.P.refresh();
    }

    @Override
    public void remove() {
        if (this.P != null) {
            this.P.remove();
        }
        this.out("adjourn " + this.G);
    }

    @Override
    public void send(String c) {
        if (this.P != null) {
            this.P.receive(c);
        }
    }

    /**
     * called from the goframe to set a move (passed to Player)
     */
    @Override
    public void set(int i, int j, int sec) {
        Dump.println("Play Distributor got move at " + i + "," + j);
        this.P.set(i, j, sec);
    }

    @Override
    public boolean started() {
        return this.P.started();
    }
}
