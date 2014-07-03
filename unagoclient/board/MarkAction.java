package unagoclient.board;

import rene.util.list.ListElement;

import java.io.PrintWriter;

/**
 * This is a special action for marks. It will print it content depending on the
 * "puresgf" parameter. This is because the new SGF format no longer allows the
 * "M" tag.
 *
 * @see unagoclient.board.Action
 */

public class MarkAction extends Action {
    BoardInterface GF;

    public MarkAction(BoardInterface gf) {
        super("M");
        this.GF = gf;
    }

    public MarkAction(String arg, BoardInterface gf) {
        super("M", arg);
        this.GF = gf;
    }

    @Override
    public void print(PrintWriter o) {
        if (this.GF.getParameter("puresgf", false)) {
            o.println();
            o.print("MA");
            ListElement p = this.Arguments.first();
            while (p != null) {
                o.print("[" + (String) (p.content()) + "]");
                p = p.next();
            }
        } else {
            super.print(o);
        }
    }
}
