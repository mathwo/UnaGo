package unagoclient.igs;

import unagoclient.Dump;
import rene.util.parser.StringParser;

import java.io.PrintWriter;

class PeekDistributor extends Distributor {
    Peeker P;

    public PeekDistributor(IgsStream in, Peeker p, int n) {
        super(in, 15, n, true);
        this.P = p;
    }

    @Override
    public void finished() {
        this.P.finished();
    }

    @Override
    public void remove() {
        this.P.remove();
    }

    @Override
    public void send(String c) {
        this.P.receive(c);
    }
}

/**
 * The Peeker class is much like a player, but it does follow a game. Thus there
 * is no complication about the order of moves.
 *
 * @see unagoclient.igs.Player
 */

public class Peeker {
    IgsGoFrame GF;
    IgsStream In;
    PrintWriter Out;
    PeekDistributor PD;
    int N, L, BS;
    String Black, White;

    public Peeker(IgsGoFrame gf, IgsStream in, PrintWriter out, int n) {
        this.GF = gf;
        this.In = in;
        this.GF.active(false);
        new PeekerSizeDistributor(in, this);
        out.println("status " + n);
        this.Out = out;
        this.N = n;
        this.L = 1;
        this.BS = 19;
    }

    void finished() {
        Dump.println("Peeker is finished");
        this.GF.active(true);
    }

    void receive(String s) {
        StringParser p;
        int i, j;
        Dump.println("Peeked: " + s);
        p = new StringParser(s);
        p.skipblanks();
        if (!p.isint()) {
            if (s.startsWith("Game")) {
                this.GF.settime(s);
            }
            return;
        }
        p.parseint('(');
        if (p.error()) {
            return;
        }
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
            this.GF.pass();
            return;
        }
        if (m.equals("Handicap")) {
            int hn;
            p.skipblanks();
            hn = p.parseint();
            Dump.println("Peeker read: Handicap " + hn);
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
        Dump.println("Peeker read: " + c + " " + i + "," + j);
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

    void remove() {
        this.PD = null;
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

    void sizefinished() {
        if (this.BS != 19) {
            this.GF.doboardsize(this.BS);
        }
        this.setinformation();
        this.PD = new PeekDistributor(this.In, this, this.N);
        this.Out.println("moves " + this.N);
    }
}

class PeekerSizeDistributor extends Distributor {
    Peeker P;

    public PeekerSizeDistributor(IgsStream in, Peeker p) {
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
