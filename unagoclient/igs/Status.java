package unagoclient.igs;

import unagoclient.Dump;
import rene.util.parser.StringParser;

import java.io.PrintWriter;

/**
 * This interprets, what the server send in response to a status command (and to
 * the problem command). It gets a GoFrame to display the game status.
 */

public class Status {
    IgsGoFrame GF;
    IgsStream In;
    StatusDistributor PD;
    String Black, White;
    int L;

    /**
     * A status object for an unknown game. This is used, when the status
     * command has already been sent to the server.
     */
    public Status(IgsGoFrame gf, IgsStream in, PrintWriter out) {
        this.GF = gf;
        this.In = in;
        this.GF.active(false);
        this.PD = new StatusDistributor(in, this);
        this.L = 1;
    }

    /**
     * Sends a status command to the server for game n.
     *
     * @param n
     *            the game number.
     */
    public Status(IgsGoFrame gf, IgsStream in, PrintWriter out, int n) {
        this.GF = gf;
        this.In = in;
        this.GF.active(false);
        this.PD = new StatusDistributor(in, this);
        out.println("status " + n);
        this.L = 1;
    }

    /**
     * When the board status is complete the GoFrame window is asked to display
     * itself.
     */
    void finished() {
        Dump.println("Status is finished");
        this.GF.setVisible(true);
        this.GF.active(true);
        // GF.B.showinformation();
        // GF.B.repaint();
    }

    private String getname(String s) {
        final StringParser p = new StringParser(s);
        return p.parseword() + " (" + p.parseword() + ")";
    }

    /**
     * This is called from the StatusDistributor. The output is interpreted and
     * the go frame is updated.
     */
    public void receive(String s) {
        if (this.L == 1) {
            this.Black = s;
        } else if (this.L == 2) {
            this.White = s;
            this.GF.settitle(this.getname(this.Black) + " - "
                    + this.getname(this.White));
        } else {
            while (true) {
                final StringParser p = new StringParser(s);
                p.skipblanks();
                if (!p.isint()) {
                    return;
                }
                final int n = p.parseint(':');
                if (p.error()) {
                    return;
                }
                if (!p.skip(":")) {
                    return;
                }
                p.skipblanks();
                char c;
                int i = 0;
                while (!p.error()) {
                    c = p.next();
                    if (c == '0') {
                        this.GF.setblack(n, i);
                    } else if (c == '1') {
                        this.GF.setwhite(n, i);
                    } else if (c == '4' || c == '5') {
                        this.GF.territory(n, i);
                    }
                    i++;
                }
                if (i != this.GF.getboardsize()) {
                    if (i < 5 || i > 29) {
                        break;
                    }
                    this.GF.doboardsize(i);
                } else {
                    break;
                }
            }
        }
        this.L++;
    }

}
