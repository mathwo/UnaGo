package unagoclient.board;

import rene.util.list.ListElement;
import rene.util.parser.StringParser;
import rene.util.xml.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This is a class wich contains a TreeNode. It used to store complete game
 * trees.
 *
 * @see unagoclient.board.TreeNode
 */

public class SGFTree {
    public static void getBoardSize(XmlTree tree) throws XmlReaderException {
        final Enumeration e = tree.getContent();
        SGFTree.BoardSize = 19;
        while (e.hasMoreElements()) {
            tree = (XmlTree) e.nextElement();
            if (tree.getTag().name().equals("BoardSize")) {
                tree = tree.xmlFirstContent();
                final XmlTag tag = tree.getTag();
                if (tag instanceof XmlTagText) {
                    try {
                        SGFTree.BoardSize = Integer.parseInt(((XmlTagText) tag)
                                .getContent());
                    } catch (final Exception ex) {
                        throw new XmlReaderException("Illegal <BoardSize>");
                    }
                } else {
                    throw new XmlReaderException("Illegal <BoardSize>");
                }
                break;
            }
        }
    }

    public static String getText(XmlTree tree) throws XmlReaderException {
        final Enumeration e = tree.getContent();
        if (!e.hasMoreElements()) {
            return "";
        }
        final XmlTree t = (XmlTree) e.nextElement();
        final XmlTag tag = t.getTag();
        if (!(tag instanceof XmlTagText) || e.hasMoreElements()) {
            throw new XmlReaderException("<" + tree.getTag().name()
                    + "> has wrong content.");
        }
        return ((XmlTagText) tag).getContent();
    }

    /**
     * Read the tree from an BufferedReader in SGF format. The BoardInterfaces
     * is only used to determine the "sgfcomments" parameter.
     */
    public static Vector load(BufferedReader in, BoardInterface gf)
            throws IOException {
        final Vector v = new Vector();
        boolean linestart = true;
        int c;
        reading: while (true) {
            final SGFTree T = new SGFTree(new Node(1));
            while (true) // search for ( at line start
            {
                try {
                    c = T.readchar(in);
                } catch (final IOException ex) {
                    break reading;
                }
                if (linestart && c == '(') {
                    break;
                }
                if (c == '\n') {
                    linestart = true;
                } else {
                    linestart = false;
                }
            }
            T.GF = gf;
            T.readnodes(T.History, in); // read the nodes
            v.addElement(T);
        }
        return v;
    }

    /**
     * Read a number of trees from an XML file.
     */
    public static Vector load(XmlReader xml, BoardInterface gf)
            throws XmlReaderException {
        final XmlTree t = xml.scan();
        if (t == null) {
            throw new XmlReaderException("Illegal file format");
        }
        final Vector v = SGFTree.readnodes(t, gf);
        return v;
    }

    public static String parseComment(XmlTree t) throws XmlReaderException {
        final StringBuffer s = new StringBuffer();
        final Enumeration e = t.getContent();
        while (e.hasMoreElements()) {
            final XmlTree tree = (XmlTree) e.nextElement();
            final XmlTag tag = tree.getTag();
            if (tag.name().equals("P")) {
                if (!tree.haschildren()) {
                    s.append("\n");
                } else {
                    final XmlTree h = tree.xmlFirstContent();
                    String k = ((XmlTagText) h.getTag()).getContent();
                    k = k.replace('\n', ' ');
                    final StringParser p = new StringParser(k);
                    final Vector v = p.wraplines(1000);
                    for (int i = 0; i < v.size(); i++) {
                        s.append((String) v.elementAt(i));
                        s.append("\n");
                    }
                }
            } else if (tag instanceof XmlTagText) {
                String k = ((XmlTagText) tag).getContent();
                k = k.replace('\n', ' ');
                final StringParser p = new StringParser(k);
                final Vector v = p.wraplines(1000);
                for (int i = 0; i < v.size(); i++) {
                    s.append((String) v.elementAt(i));
                    s.append("\n");
                }
            } else {
                throw new XmlReaderException("<" + tag.name()
                        + "> not proper here.");
            }
        }
        return s.toString();
    }

    /**
     * Read all games from a tree.
     *
     * @return Vector of trees.
     */
    static Vector readnodes(XmlTree tree, BoardInterface gf)
            throws XmlReaderException {
        final Vector v = new Vector();
        final Enumeration root = tree.getContent();
        while (root.hasMoreElements()) {
            tree = (XmlTree) root.nextElement();
            XmlTag tag = tree.getTag();
            if (tag instanceof XmlTagPI) {
                continue;
            }
            SGFTree.testTag(tag, "Go");
            final Enumeration trees = tree.getContent();
            while (trees.hasMoreElements()) {
                tree = (XmlTree) trees.nextElement();
                tag = tree.getTag();
                SGFTree.testTag(tag, "GoGame");
                if (tag.hasParam("name")) {
                    SGFTree.GameName = tag.getValue("name");
                }
                final Enumeration e = tree.getContent();
                if (!e.hasMoreElements()) {
                    SGFTree.xmlMissing("Information");
                }
                final XmlTree information = (XmlTree) e.nextElement();
                SGFTree.testTag(information.getTag(), "Information");
                SGFTree.getBoardSize(information);
                final SGFTree t = new SGFTree(new Node(1));
                t.GF = gf;
                final TreeNode p = t.readnodes(e, null, tree, true, 1);
                if (p != null) {
                    SGFTree.setInformation(p, information);
                }
                t.History = p;
                if (p != null) {
                    v.addElement(t);
                }
            }
        }
        return v;
    }

    public static void setInformation(TreeNode p, XmlTree information)
            throws XmlReaderException {
        final Enumeration e = information.getContent();
        while (e.hasMoreElements()) {
            final XmlTree tree = (XmlTree) e.nextElement();
            final XmlTag tag = tree.getTag();
            if (tag.name().equals("BoardSize")) {
                p.addaction(new Action("SZ", "" + SGFTree.BoardSize));
            } else if (tag.name().equals("BlackPlayer")) {
                p.addaction(new Action("PB", SGFTree.getText(tree)));
            } else if (tag.name().equals("BlackRank")) {
                p.addaction(new Action("BR", SGFTree.getText(tree)));
            } else if (tag.name().equals("WhitePlayer")) {
                p.addaction(new Action("PW", SGFTree.getText(tree)));
            } else if (tag.name().equals("WhiteRank")) {
                p.addaction(new Action("WR", SGFTree.getText(tree)));
            } else if (tag.name().equals("Date")) {
                p.addaction(new Action("DT", SGFTree.getText(tree)));
            } else if (tag.name().equals("Time")) {
                p.addaction(new Action("TM", SGFTree.getText(tree)));
            } else if (tag.name().equals("Komi")) {
                p.addaction(new Action("KM", SGFTree.getText(tree)));
            } else if (tag.name().equals("Result")) {
                p.addaction(new Action("RE", SGFTree.getText(tree)));
            } else if (tag.name().equals("Handicap")) {
                p.addaction(new Action("HA", SGFTree.getText(tree)));
            } else if (tag.name().equals("User")) {
                p.addaction(new Action("US", SGFTree.getText(tree)));
            } else if (tag.name().equals("Copyright")) {
                p.addaction(new Action("CP", SGFTree.parseComment(tree)));
            }
        }
        if (!SGFTree.GameName.equals("")) {
            p.addaction(new Action("GN", SGFTree.GameName));
        }
    }

    public static void testTag(XmlTag tag, String name)
            throws XmlReaderException {
        if (!tag.name().equals(name)) {
            throw new XmlReaderException("<" + name + "> expected instead of <"
                    + tag.name() + ">");
        }
    }

    public static void xmlMissing(String s) throws XmlReaderException {
        throw new XmlReaderException("Missing <" + s + ">");
    }

    protected TreeNode History; // the game history

    final int maxbuffer = 4096;

    char[] Buffer = new char[this.maxbuffer]; // the buffer for
    // reading of

    // files
    int BufferN;

    BoardInterface GF;

    static int lastnl = 0;

    /*
     * XML Reader Stuff
     */

    // Assumption on Boardsize of xml file, if <BoardSize> is not found
    static int BoardSize = 19;
    static String GameName = "";

    /**
     * initlialize with a specific Node
     */
    public SGFTree(Node n) {
        this.History = new TreeNode(n);
        this.History.node().main(true);
    }

    // Check for the terrible compressed point list and expand into
    // single points
    boolean expand(Action a, String s) {
        final String t = a.getType();
        if (!(t.equals("MA") || t.equals("SQ") || t.equals("TR")
                || t.equals("CR") || t.equals("AW") || t.equals("AB")
                || t.equals("AE") || t.equals("SL"))) {
            return false;
        }
        if (s.length() != 5 || s.charAt(2) != ':') {
            return false;
        }
        final String s0 = s.substring(0, 2), s1 = s.substring(3);
        final int i0 = Field.i(s0), j0 = Field.j(s0);
        final int i1 = Field.i(s1), j1 = Field.j(s1);
        if (i1 < i0 || j1 < j0) {
            return false;
        }
        int i, j;
        for (i = i0; i <= i1; i++) {
            for (j = j0; j <= j1; j++) {
                a.addargument(Field.string(i, j));
            }
        }
        return true;
    }

    public int getSize() {
        try {
            return Integer.parseInt(this.History.getaction("SZ"));
        } catch (final Exception e) {
            return 19;
        }
    }

    /**
     * Print this tree to the PrintWriter starting at the root node.
     */
    public void print(PrintWriter o) {
        this.printtree(this.History, o);
    }

    public void printInformation(XmlWriter xml, TreeNode p, String tag,
            String xmltag) {
        final String s = p.getaction(tag);
        if (s != null && !s.equals("")) {
            xml.printTagNewLine(xmltag, s);
        }
    }

    public void printInformationText(XmlWriter xml, TreeNode p, String tag,
            String xmltag) {
        final String s = p.getaction(tag);
        if (s != null && !s.equals("")) {
            xml.startTagNewLine(xmltag);
            xml.printParagraphs(s, 60);
            xml.endTagNewLine(xmltag);
        }
    }

    /**
     * Print the tree to the specified PrintWriter.
     *
     * @param p
     *            the subtree to be printed
     */
    void printtree(TreeNode p, PrintWriter o) {
        o.println("(");
        while (true) {
            p.node().print(o);
            if (!p.haschildren()) {
                break;
            }
            if (p.lastChild() != p.firstChild()) {
                ListElement e = p.children().first();
                while (e != null) {
                    this.printtree((TreeNode) e.content(), o);
                    e = e.next();
                }
                break;
            }
            p = p.firstChild();
        }
        o.println(")");
    }

    /**
     * Print the tree to the specified PrintWriter.
     *
     * @param p
     *            the subtree to be printed
     */
    void printtree(TreeNode p, XmlWriter xml, int size, boolean top) {
        if (top) {
            final String s = p.getaction("GN");
            if (s != null && !s.equals("")) {
                xml.startTagNewLine("GoGame", "name", s);
            } else {
                xml.startTagNewLine("GoGame");
            }
            xml.startTagNewLine("Information");
            this.printInformation(xml, p, "AP", "Application");
            this.printInformation(xml, p, "SZ", "BoardSize");
            this.printInformation(xml, p, "PB", "BlackPlayer");
            this.printInformation(xml, p, "BR", "BlackRank");
            this.printInformation(xml, p, "PW", "WhitePlayer");
            this.printInformation(xml, p, "WR", "WhiteRank");
            this.printInformation(xml, p, "DT", "Date");
            this.printInformation(xml, p, "TM", "Time");
            this.printInformation(xml, p, "KM", "Komi");
            this.printInformation(xml, p, "RE", "Result");
            this.printInformation(xml, p, "HA", "Handicap");
            this.printInformation(xml, p, "US", "User");
            this.printInformationText(xml, p, "CP", "Copyright");
            xml.endTagNewLine("Information");
        } else {
            xml.startTagNewLine("Variation");
        }
        if (top) {
            xml.startTagNewLine("Nodes");
        }
        while (true) {
            p.node().print(xml, size);
            if (!p.haschildren()) {
                break;
            }
            if (p.lastChild() != p.firstChild()) {
                ListElement e = p.children().first();
                p = p.firstChild();
                p.node().print(xml, size);
                e = e.next();
                while (e != null) {
                    this.printtree((TreeNode) e.content(), xml, size, false);
                    e = e.next();
                }
                if (!p.haschildren()) {
                    break;
                }
            }
            p = p.firstChild();
        }
        if (top) {
            xml.endTagNewLine("Nodes");
        }
        if (top) {
            xml.endTagNewLine("GoGame");
        } else {
            xml.endTagNewLine("Variation");
        }
    }

    public void printXML(XmlWriter xml) {
        this.printtree(this.History, xml, this.getSize(), true);
    }

    char readchar(BufferedReader in) throws IOException {
        int c;
        while (true) {
            c = in.read();
            if (c == -1) {
                throw new IOException();
            }
            if (c == 13) {
                if (SGFTree.lastnl == 10) {
                    SGFTree.lastnl = 0;
                } else {
                    SGFTree.lastnl = 13;
                    return '\n';
                }
            } else if (c == 10) {
                if (SGFTree.lastnl == 13) {
                    SGFTree.lastnl = 0;
                } else {
                    SGFTree.lastnl = 10;
                    return '\n';
                }
            } else {
                SGFTree.lastnl = 0;
                return (char) c;
            }
        }
    }

    char readnext(BufferedReader in) throws IOException {
        int c = this.readchar(in);
        while (c == '\n' || c == '\t' || c == ' ') {
            if (c == -1) {
                throw new IOException();
            }
            c = this.readchar(in);
        }
        return (char) c;
    }

    public Node readnode(int number, XmlTree tree) throws XmlReaderException {
        final Node n = new Node(number);
        XmlTag tag = tree.getTag();
        if (tag.hasParam("name")) {
            n.addaction(new Action("N", tag.getValue("name")));
        }
        if (tag.hasParam("blacktime")) {
            n.addaction(new Action("BL", tag.getValue("blacktime")));
        }
        if (tag.hasParam("whitetime")) {
            n.addaction(new Action("WL", tag.getValue("whitetime")));
        }
        final Enumeration e = tree.getContent();
        while (e.hasMoreElements()) {
            final XmlTree t = (XmlTree) e.nextElement();
            tag = t.getTag();
            if (tag.name().equals("Black")) {
                try {
                    n.addaction(new Action("B", this.xmlToSgf(t)));
                    n.number(n.number() + 1);
                } catch (final XmlReaderException ey) {
                }
            } else if (tag.name().equals("White")) {
                try {
                    n.addaction(new Action("W", this.xmlToSgf(t)));
                    n.number(n.number() + 1);
                } catch (final XmlReaderException ey) {
                }
            } else if (tag.name().equals("AddBlack")) {
                n.addaction(new Action("AB", this.xmlToSgf(t)));
            } else if (tag.name().equals("AddWhite")) {
                n.addaction(new Action("AW", this.xmlToSgf(t)));
            } else if (tag.name().equals("Delete")) {
                n.expandaction(new Action("AE", this.xmlToSgf(t)));
            } else if (tag.name().equals("Mark")) {
                if (tag.hasParam("type")) {
                    final String s = tag.getValue("type");
                    if (s.equals("triangle")) {
                        n.expandaction(new Action("TR", this.xmlToSgf(t)));
                    } else if (s.equals("square")) {
                        n.expandaction(new Action("SQ", this.xmlToSgf(t)));
                    } else if (s.equals("circle")) {
                        n.expandaction(new Action("CR", this.xmlToSgf(t)));
                    }
                } else if (tag.hasParam("label")) {
                    final String s = tag.getValue("label");
                    n.expandaction(new Action("LB", this.xmlToSgf(t) + ":" + s));
                } else if (tag.hasParam("territory")) {
                    final String s = tag.getValue("territory");
                    if (s.equals("white")) {
                        n.expandaction(new Action("TW", this.xmlToSgf(t)));
                    } else if (s.equals("black")) {
                        n.expandaction(new Action("TB", this.xmlToSgf(t)));
                    }
                } else {
                    n.expandaction(new MarkAction(this.xmlToSgf(t), this.GF));
                }
            } else if (tag.name().equals("BlackTimeLeft")) {
                n.addaction(new Action("BL", SGFTree.getText(t)));
            } else if (tag.name().equals("WhiteTimeLeft")) {
                n.addaction(new Action("WL", SGFTree.getText(t)));
            } else if (tag.name().equals("Comment")) {
                n.addaction(new Action("C", SGFTree.parseComment(t)));
            } else if (tag.name().equals("SGF")) {
                if (!tag.hasParam("type")) {
                    throw new XmlReaderException("Illegal <SGF> tag.");
                }
                Action a;
                if (tag.getValue("type").equals("M")) {
                    a = new MarkAction(this.GF);
                } else {
                    a = new Action(tag.getValue("type"));
                }
                final Enumeration eh = t.getContent();
                while (eh.hasMoreElements()) {
                    final XmlTree th = (XmlTree) eh.nextElement();
                    final XmlTag tagh = th.getTag();
                    if (!tagh.name().equals("Arg")) {
                        throw new XmlReaderException("Illegal <SGF> tag.");
                    }
                    if (!th.isText()) {
                        throw new XmlReaderException("Illegal <SGF> tag.");
                    } else {
                        a.addargument(th.getText());
                    }
                }
                n.addaction(a);
            }
        }
        return n;
    }

    // read a node assuming that ; has been found
    // return the character, which did not fit into node properties,
    // usually ;, ( or )
    char readnode(TreeNode p, BufferedReader in) throws IOException {
        final boolean sgf = this.GF.getParameter("sgfcomments", false);
        char c = this.readnext(in);
        Action a;
        final Node n = new Node(((Node) p.content()).number());
        String s;
        loop: while (true) // read all actions
        {
            this.BufferN = 0;
            while (true) {
                if (c >= 'A' && c <= 'Z') {
                    this.store(c);
                } else if (c == '(' || c == ';' || c == ')') {
                    break loop;
                } else if (c == '[') {
                    break;
                } else if (c < 'a' || c > 'z') {
                    throw new IOException();
                }
                // this is an error
                c = this.readnext(in);
            }
            if (this.BufferN == 0) {
                throw new IOException();
            }
            s = new String(this.Buffer, 0, this.BufferN);
            if (s.equals("L")) {
                a = new LabelAction(this.GF);
            } else if (s.equals("M")) {
                a = new MarkAction(this.GF);
            } else {
                a = new Action(s);
            }
            while (c == '[') {
                this.BufferN = 0;
                while (true) {
                    c = this.readchar(in);
                    if (c == '\\') {
                        c = this.readchar(in);
                        if (sgf && c == '\n') {
                            if (this.BufferN > 1
                            && this.Buffer[this.BufferN - 1] == ' ') {
                                continue;
                            } else {
                                c = ' ';
                            }
                        }
                    } else if (c == ']') {
                        break;
                    }
                    this.store(c);
                }
                c = this.readnext(in); // prepare next argument
                String s1;
                if (this.BufferN > 0) {
                    s1 = new String(this.Buffer, 0, this.BufferN);
                } else {
                    s1 = "";
                }
                if (!this.expand(a, s1)) {
                    a.addargument(s1);
                }
            }
            // no more arguments
            n.addaction(a);
            if (a.getType().equals("B") || a.getType().equals("W")) {
                n.number(n.number() + 1);
            }
        } // end of actions has been found
        // append node
        n.main(p);
        TreeNode newp;
        if (((Node) p.content()).actions() == null) {
            p.content(n);
        } else {
            p.addchild(newp = new TreeNode(n));
            n.main(p);
            p = newp;
            if (p.parentPos() != null && p != p.parentPos().firstChild()) {
                ((Node) p.content()).number(2);
            }
        }
        return c;
    }

    TreeNode readnodes(Enumeration e, TreeNode p, XmlTree father, boolean main,
            int number) throws XmlReaderException {
        TreeNode ret = null;
        while (e.hasMoreElements()) {
            final XmlTree tree = (XmlTree) e.nextElement();
            final XmlTag tag = tree.getTag();
            if (tag.name().equals("Nodes")) {
                return this.readnodes(tree.getContent(), p, father, main,
                        number);
            } else if (tag.name().equals("Node")) {
                if (p != null) {
                    number = ((Node) p.content()).number();
                }
                final Node n = this.readnode(number, tree);
                n.main(main);
                final TreeNode newp = new TreeNode(n);
                if (p == null) {
                    ret = newp;
                }
                if (p != null) {
                    p.addchild(newp);
                }
                p = newp;
            } else if (tag.name().equals("White")) {
                if (p != null) {
                    number = ((Node) p.content()).number();
                }
                final Node n = new Node(number);
                try {
                    n.addaction(new Action("W", this.xmlToSgf(tree)));
                    n.number(n.number() + 1);
                    n.main(main);
                } catch (final XmlReaderException ey) {
                    n.addaction(new Action("C", "Pass"));
                }
                if (tag.hasParam("name")) {
                    n.addaction(new Action("N", tag.getValue("name")));
                }
                if (tag.hasParam("timeleft")) {
                    n.addaction(new Action("WL", tag.getValue("timeleft")));
                }
                final TreeNode newp = new TreeNode(n);
                if (p == null) {
                    ret = newp;
                }
                if (p != null) {
                    p.addchild(newp);
                }
                p = newp;
            } else if (tag.name().equals("Black")) {
                if (p != null) {
                    number = ((Node) p.content()).number();
                }
                final Node n = new Node(number);
                try {
                    n.addaction(new Action("B", this.xmlToSgf(tree)));
                    n.number(n.number() + 1);
                    n.main(main);
                } catch (final XmlReaderException ey) {
                    n.addaction(new Action("C", "Pass"));
                }
                if (tag.hasParam("name")) {
                    n.addaction(new Action("N", tag.getValue("name")));
                }
                if (tag.hasParam("timeleft")) {
                    n.addaction(new Action("BL", tag.getValue("timeleft")));
                }
                final TreeNode newp = new TreeNode(n);
                if (p == null) {
                    ret = newp;
                }
                if (p != null) {
                    p.addchild(newp);
                }
                p = newp;
            } else if (tag.name().equals("Comment")) {
                if (p == null) {
                    final Node n = new Node(number);
                    n.main(main);
                    p = new TreeNode(n);
                    ret = p;
                }
                final Node n = (Node) p.content();
                n.addaction(new Action("C", SGFTree.parseComment(tree)));
            } else if (tag.name().equals("Variation")) {
                final TreeNode parent = (TreeNode) p.parent();
                if (parent == null) {
                    throw new XmlReaderException(
                            "Root node cannot have variation");
                }
                final TreeNode newp = this.readnodes(tree.getContent(), null,
                        tree, false, 1);
                parent.addchild(newp);
            } else {
                throw new XmlReaderException("Illegal Node or Variation <"
                        + tag.name() + ">");
            }
        }
        return ret;
    }

    /**
     * Read the nodes belonging to a tree. this assumes that ( has been found.
     */
    void readnodes(TreeNode p, BufferedReader in) throws IOException {
        char c = this.readnext(in);
        while (true) {
            if (c == ';') {
                c = this.readnode(p, in);
                if (p.haschildren()) {
                    p = p.lastChild();
                }
                continue;
            } else if (c == '(') {
                this.readnodes(p, in);
            } else if (c == ')') {
                break;
            }
            c = this.readnext(in);
        }
    }

    /**
     * Store c into the Buffer extending its length, if necessary. This is a fix
     * by Bogdar Creanga from 2000-10-17 (Many Thanks)
     */
    private void store(char c) {
        try {
            this.Buffer[this.BufferN] = c;
            this.BufferN++;
        } catch (final ArrayIndexOutOfBoundsException e) {
            final int newLength = this.Buffer.length + this.maxbuffer;
            final char[] newBuffer = new char[newLength];
            System.arraycopy(this.Buffer, 0, newBuffer, 0, this.Buffer.length);
            this.Buffer = newBuffer;
            this.Buffer[this.BufferN++] = c;
        }
    }

    /**
     * return the top node of this game tree
     */
    public TreeNode top() {
        return this.History;
    }

    public void wrongBoardPosition(String s) throws XmlReaderException {
        throw new XmlReaderException("Wrong Board Position " + s);
    }

    public String xmlToSgf(String pos) throws XmlReaderException {
        if (pos.length() < 2) {
            this.wrongBoardPosition(pos);
        }
        final int n = pos.indexOf(",");
        if (n > 0 && n < pos.length()) {
            final String s1 = pos.substring(0, n), s2 = pos.substring(n + 1);
            try {
                final int i = Integer.parseInt(s1) - 1;
                int j = Integer.parseInt(s2);
                j = SGFTree.BoardSize - j;
                if (i < 0 || i >= SGFTree.BoardSize || j < 0
                        || j >= SGFTree.BoardSize) {
                    this.wrongBoardPosition(pos);
                }
                return Field.string(i, j);
            } catch (final Exception ex) {
                this.wrongBoardPosition(pos);
            }
        }
        char c = Character.toUpperCase(pos.charAt(0));
        if (c >= 'J') {
            c--;
        }
        final int i = c - 'A';
        int j = 0;
        try {
            j = Integer.parseInt(pos.substring(1));
        } catch (final Exception ex) {
            this.wrongBoardPosition(pos);
        }
        j = SGFTree.BoardSize - j;
        if (i < 0 || i >= SGFTree.BoardSize || j < 0 || j >= SGFTree.BoardSize) {
            this.wrongBoardPosition(pos);
        }
        return Field.string(i, j);
    }

    public String xmlToSgf(XmlTree tree) throws XmlReaderException {
        XmlTag tag = tree.getTag();
        if (tag.hasParam("at")) {
            return this.xmlToSgf(tag.getValue("at"));
        }
        final Enumeration e = tree.getContent();
        if (!e.hasMoreElements()) {
            throw new XmlReaderException("Missing board position.");
        }
        tag = ((XmlTree) e.nextElement()).getTag();
        if (tag instanceof XmlTagText) {
            final String pos = ((XmlTagText) tag).getContent();
            return this.xmlToSgf(pos);
        } else if (tag.name().equals("at")) {
            final String pos = ((XmlTagText) tag).getContent();
            return this.xmlToSgf(pos);
        } else {
            throw new XmlReaderException(tag.name()
                    + " contains wrong board position.");
        }
    }
}
