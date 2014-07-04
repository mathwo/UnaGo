package unagoclient.igs.games;

import rene.util.parser.StringParser;

/**
 * This is a Class for sorting games by W player rank.
 */

public class GamesObject implements Comparable<GamesObject> {
    String S;
    int V;
    String White, Black;

    public GamesObject(String s) {
        this.S = s;
        if (this.S.indexOf(']') == 3) {
            this.S = " " + this.S;
        }
        StringParser p = new StringParser(s);
        p.upto(']');
        p.skip("]");
        this.White = p.upto('[').trim();
        p.skip("[");
        final String h = p.parseword(']');
        p.upto('.');
        p.skip(".");
        this.Black = p.upto('[').trim();
        if (p.error()) {
            this.V = -50;
            return;
        }
        p = new StringParser(h);
        if (p.isint()) {
            this.V = p.parseint();
            if (p.skip("k")) {
                this.V = 100 - this.V;
            } else if (p.skip("d")) {
                this.V = 100 + this.V;
            } else if (p.skip("p")) {
                this.V += 200;
            }
        } else {
            this.V = 0;
        }
    }

    @Override
    public int compareTo(GamesObject o) {
        final GamesObject g = (GamesObject) o;
        if (this.V < g.V) {
            return 1;
        } else if (this.V > g.V) {
            return -1;
        } else {
            return 0;
        }
    }

    public boolean friend() {
        final String friends = rene.gui.Global.getParameter("friends", "");
        return friends.indexOf(" " + this.White) >= 0
                || friends.indexOf(" " + this.Black) >= 0;
    }

    String game() {
        return this.S;
    }
}
