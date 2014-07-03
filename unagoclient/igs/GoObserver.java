package unagoclient.igs;

import unagoclient.Dump;
import rene.util.parser.StringParser;

import java.io.PrintWriter;

/**
 * A GoObserver interprets server input for an observed game. It uses an
 * ObserverDistributor to get this iput from IgsStream.
 * <p>
 * Since there is no other way, GoObserver first starts an
 * ObserveSizeDistributor (private in GoObserver.java) and sends the status
 * command to the server. The mere purpose of this is to get the size of the
 * board in the particular game.
 * <p>
 * After it got that information, it will start an ObserveDistributor to get the
 * moves of the game.
 * <p>
 * One problem is the undoing of moves. The server undoes moves, by resending
 * old moves. This has to be taken care of.
 * <p>
 * Moreover, when the user closes the window, the unobserve command will be sent
 * to the server. To make sure, the GoObserver waits 10 seconds before it
 * removes itself (otherwise, sudden moves could arrive, which correspond to no
 * observer and would open a plaing window, because that is the way the server
 * tells you about the re-load of a game). Most of these inconveniences are
 * caused by a dirty server protocol.
 *
 * @see unagoclient.igs.Player
 */

public class GoObserver {
    IgsGoFrame GF;
    IgsStream In;
    PrintWriter Out;
    ObserveDistributor PD;
    int N, L, BS;
    int Expected, Got;
    String Black, White;
    boolean Observing;
    String Kibitz;
    boolean Closed;

    public GoObserver(IgsGoFrame gf, IgsStream in, PrintWriter out, int n) {
        this.GF = gf;
        this.In = in;
        this.Out = out;
        new ObserveSizeDistributor(in, this);
        this.Out.println("status " + n);
        this.N = n;
        this.Expected = 0;
        this.Got = 0;
        this.L = 1;
        this.BS = 19;
        this.Observing = false;
        this.Kibitz = "";
        this.Closed = false;
    }

    public void finished() {
        Dump.println("GoObserver(" + this.N + ") is finished");
    }

    public void finishremove() {
        if (this.PD != null) {
            this.PD.unchain();
        }
        this.PD = null;
    }

    public boolean newmove() {
        return this.Expected > this.Got;
    }

    public void pass() {
        this.Out.println("pass");
        Dump.println("***> Player passes");
    }

    public void receive(String s) {
        if (this.Closed || !this.Observing) {
            return;
        }
        StringParser p;
        int nu, i, j;
        Dump.println("Observed(" + this.N + "): " + s);
        p = new StringParser(s);
        p.skipblanks();
        if (!p.isint()) {
            if (s.startsWith("Game")) {
                this.GF.settime(s);
            } else if (s.indexOf("Game") > 0 && s.indexOf("Game") < 4) {
                this.GF.addComment(s);
            } else if (s.startsWith("Kibitz")) {
                if (s.startsWith("Kibitz->")) {
                    final StringParser ps = new StringParser(s);
                    ps.skip("Kibitz->");
                    ps.skipblanks();
                    this.GF.addComment(this.Kibitz + ": " + ps.upto((char) 0));
                    this.Kibitz = "";
                } else {
                    final StringParser ps = new StringParser(s);
                    ps.skip("Kibitz");
                    ps.skipblanks();
                    this.Kibitz = ps.upto(':');
                }
            }
            return;
        }
        nu = p.parseint('(');
        if (p.error()) {
            return;
        }
        if (this.Expected < nu && this.Got == 0) {
            Dump.println("Oberver denies number " + nu + " expecting "
                    + this.Expected);
            this.Out.println("moves " + this.N);
            this.Got = nu;
            return;
        } else if (this.Expected > nu) {
            this.GF.undo(this.Expected - nu);
            this.Expected = nu;
        }
        this.Expected = nu + 1;
        p.skipblanks();
        p.skip("(");
        final String c = p.parseword(')');
        if (p.error()) {
            return;
        }
        p.skip(")");
        p.skipblanks();
        p.skip(":");
        final String m = p.parseword();
        if (m.length() < 2) {
            return;
        }
        if (m.equals("Pass")) {
            this.GF.setpass();
            return;
        }
        if (m.equals("Handicap")) {
            int hn;
            p.skipblanks();
            hn = p.parseint();
            this.GF.handicap(hn);
            return;
        }
        i = m.charAt(0) - 'A';
        if (i >= 9) {
            i--;
        }
        try {
            j = Integer.parseInt(m.substring(1)) - 1;
        } catch (final NumberFormatException e) {
            j = -1;
        }
        if (i < 0 || j < 0) {
            return;
        }
        Dump.println("GoObserver interpreted: " + c + " " + i + "," + j);
        if (c.equals("W")) {
            this.GF.white(i, this.BS - 1 - j);
        } else {
            this.GF.black(i, this.BS - 1 - j);
        }
    }

    void receivesize(String s) {
        if (this.L == 1) {
            this.White = s;
        } else if (this.L == 2) {
            this.Black = s;
        } else {
            while (true) {
                final StringParser p = new StringParser(s);
                p.skipblanks();
                if (!p.isint()) {
                    return;
                }
                p.parseint(':');
                if (p.error()) {
                    return;
                }
                if (!p.skip(":")) {
                    return;
                }
                p.skipblanks();
                int i = 0;
                while (!p.error()) {
                    p.next();
                    i++;
                }
                if (i != this.BS) {
                    if (i < 5 || i > 29) {
                        break;
                    }
                    this.BS = i;
                } else {
                    break;
                }
            }
        }
        this.L++;
    }

    public void refresh() {
        this.Got = this.Expected;
        this.Expected = 0;
        this.Out.println("moves " + this.N);
    }

    public void remove() {
        Dump.println("GoObserver(" + this.N + ") has ended, unobserving");
        this.Closed = true;
        this.Out.println("unobserve " + this.N);
        new ObserverCloser(this);
    }

    void sended() {
        if (!this.Observing) {
            this.Out.println("observe " + this.N);
        }
        this.Observing = true;
    }

    public void set(int i, int j) {
        if (i >= 8) {
            i++;
        }
        final char c[] = new char[1];
        c[0] = (char) ('a' + i);
        this.Out.println(new String(c) + (this.BS - j));
        Dump.println("***> Player sends " + new String(c) + (this.BS - j));
    }

    public void setinformation() {
        StringParser p;
        p = new StringParser(this.Black);
        final String BlackPlayer = p.parseword();
        final String BlackRank = p.parseword();
        p.parseword();
        p.parseword();
        p.parseword();
        p.parseword();
        final String Komi = p.parseword();
        final String Handicap = p.parseword();
        p = new StringParser(this.White);
        final String WhitePlayer = p.parseword();
        final String WhiteRank = p.parseword();
        this.GF.setinformation(BlackPlayer, BlackRank, WhitePlayer, WhiteRank,
                Komi, Handicap);
    }

    public void sizefinished() {
        this.PD = new ObserveDistributor(this.In, this, this.N);
        this.GF.distributor(this.PD);
        this.GF.doboardsize(this.BS);
        this.setinformation();
        this.Out.println("observe " + this.N);
        this.Observing = true;
    }

    public boolean started() {
        return this.Observing && (this.Got <= this.Expected);
    }
}

/**
 * Local class, which will remove the observer after a certain delay.
 */

class ObserverCloser extends Thread {
    GoObserver GO;

    public ObserverCloser(GoObserver go) {
        this.GO = go;
        this.start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10000);
        } catch (final InterruptedException e) {
        }
        this.GO.finishremove();
    }
}

/**
 * This is a local class to determine the size of the board, when the observer
 * distributor has sent the status command.
 */

class ObserveSizeDistributor extends Distributor {
    GoObserver P;

    public ObserveSizeDistributor(IgsStream in, GoObserver p) {
        super(in, 22, 0, true);
        this.P = p;
    }

    @Override
    public void finished() {
        this.P.sizefinished();
    }

    @Override
    public void send(String c) {
        this.P.receivesize(c);
    }
}
