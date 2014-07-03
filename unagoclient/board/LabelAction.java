package unagoclient.board;

import rene.util.list.ListElement;

import java.io.PrintWriter;

/**
 * This action class takes special care to print labels in SGF form. UnaGo notes
 * labes in consecutive letters, but SGF does not have this feature, thus it
 * outputs labels as LB[field:letter].
 */

public class LabelAction extends Action {
    BoardInterface GF;

    public LabelAction(BoardInterface gf) {
        super("L");
        this.GF = gf;
    }

    public LabelAction(String arg, BoardInterface gf) {
        super("L", arg);
        this.GF = gf;
    }

    @Override
    public void print(PrintWriter o) {
        if (this.GF.getParameter("puresgf", false)) {
            o.println();
            o.print("LB");
            final char[] c = new char[1];
            int i = 0;
            ListElement p = this.Arguments.first();
            while (p != null) {
                c[0] = (char) ('a' + i);
                o.print("[" + (String) (p.content()) + ":" + new String(c)
                + "]");
                i++;
                p = p.next();
            }
        } else {
            super.print(o);
        }
    }
}
