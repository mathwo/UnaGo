package unagoclient.igs.who;

import rene.util.parser.StringParser;

public class WhoObject implements Comparable<WhoObject> {
    String S, Name, Stat;
    public int V;
    boolean SortName;

    public WhoObject(String s, boolean sortname) {
        this.S = s;
        this.SortName = sortname;
        if (s.length() <= 30) {
            this.V = -50;
            this.Name = "";
            this.Stat = "";
            return;
        }
        this.Stat = s.substring(0, 5);
        StringParser p = new StringParser(s.substring(30));
        final String h = p.parseword();
        p = new StringParser(h);
        if (p.isint()) {
            this.V = p.parseint();
            if (p.skip("k")) {
                this.V = 100 - this.V;
            } else if (p.skip("d")) {
                this.V = 100 + this.V;
            } else if (p.skip("p")) {
                this.V += 200;
            } else if (p.skip("NR")) {
                this.V = 0;
            }
        } else {
            this.V = -50;
        }
        if (s.length() < 14) {
            this.Name = "";
        } else {
            p = new StringParser(s.substring(12));
            this.Name = p.parseword();
        }
    }

    @Override
    public int compareTo(WhoObject o) {
        final WhoObject g = o;
        if (this.SortName) {
            return this.Name.compareTo(g.Name);
        } else {
            if (this.V < g.V) {
                return 1;
            } else if (this.V > g.V) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public boolean friend() {
        return rene.gui.Global.getParameter("friends", "").indexOf(
                " " + this.Name) >= 0;
    }

    public boolean looking() {
        return this.Stat.indexOf('!') >= 0;
    }

    public boolean marked() {
        return rene.gui.Global.getParameter("marked", "").indexOf(
                " " + this.Name) >= 0;
    }

    public boolean quiet() {
        return this.Stat.indexOf('Q') >= 0;
    }

    public boolean silent() {
        return this.Stat.indexOf('X') >= 0;
    }

    String who() {
        return this.S;
    }
}
