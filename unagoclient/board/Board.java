package unagoclient.board;

import rene.util.list.ListElement;
import rene.util.xml.XmlReader;
import rene.util.xml.XmlReaderException;
import rene.util.xml.XmlWriter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Vector;

//******************* Board ***********************

/**
 * This is the main file for presenting a Go board.
 * <p>
 * Handles the complete display and storage of a Go board. The display is kept
 * on an offscreen image. Stores a Go game in a node list (with variants).
 * Handles the display of the current node.
 * <p>
 * This class handles mouse input to set the next move. It also has methods to
 * move in the node tree from external sources.
 * <p>
 * A BoardInterface is used to encorporate the board into an environment.
 */

public class Board extends Canvas implements MouseListener,
        MouseMotionListener, KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    static public unagoclient.board.TreeNode getNext(TreeNode p) {
        final ListElement l = p.listelement();
        if (l == null) {
            return null;
        }
        if (l.next() == null) {
            return null;
        }
        return (TreeNode) l.next().content();
    }

    // offset=offset, totalWidth=total width,
    // fieldWidth=field width, boardSize=board
    // size (9,11,13,19)
    // offsetRightBelow=offset for coordinates to the right and below
    // offsetAboveBelow=offset above the board and below for coordinates
    int offset;
    int totalWidth;
    int fieldWidth;
    int boardSize;
    int offsetRightBelow;
    int offsetAboveBelow;
    int pixelCoordinate; // pixel coordinates

    // last move (used to highlight the move)
    int lastMoveX = -1, lastMoveY = 0;

    // internal flag, if last move is to be highlighted
    boolean showLastMove;

    // off-screen images of empty and current board
    Image Empty, EmptyShadow, ActiveImage;

    // the game tree
    SGFTree sgfTree; // the game tree

    // the game trees (one of them is sgfTree)
    Vector gameTrees;

    // current displayed tree
    int currentTree;

    // current board position
    Position position;

    // number of the next move
    int number;

    // the current board position in this presentation
    TreeNode positionNode;

    // states:
    // 1: black
    // 2: white
    // 3: set black
    // 4: set white
    // 5: mark
    // 6: letter
    // 7: hide
    // 10: text mark
    // see GoFrame.setState(int)
    int state;//

    // Font for board letters and coordinates
    Font font;

    // Metrics of this font
    FontMetrics fontMetrics;

    // Frame containing the board.
    BoardInterface gameFrame;

    boolean isActive;
    int mainColor = 1;
    public int myColor = 0;

    //Board position which has been sent to server.
    int sendX = -1, sendY;

    // Note size to check for resizing at paint
    Dimension dim;

    int SpecialMarker = Field.SQUARE;
    String TextMarker = "A";

    // Prisoners (white and black)
    public int Pw, Pb;

    // File to be loaded at repaint
    BufferedReader laterLoad = null;

    Image blackStone, whiteStone;

    // Numbers display from this one
    int range = -1;

    boolean doesKeepRange = false;
    String nodeName = "", lText = "";
    boolean doesDisplayNodeName = false;
    public boolean isRemoving = false;
    boolean Activated = false;
    public boolean Teaching = false; // enable teaching
    // mode
    boolean VCurrent = false; // show variations
    // to

    // ******************** initialize board *******************

    // current move
    boolean VHide = false; // hide variation
    // markers

    public static WoodPaint woodpaint = null;

    EmptyPaint EPThread = null;

    final double pixel = 0.8, shadow = 0.7;

    // ************** paint ************************

    boolean MouseDown = false; // mouse
    // release
    // only, if
    // mouse
    // pressed

    protected int iTarget = -1, jTarget = -1;

    // captured: when a move is made by mouse click, captured represents how
    // many stores are captured by previous move.
    int captured = 0, capturei, capturej;

    // The following is for the thread, which tries to draw the
    // board on program start, to save time.

    public Board(int size, BoardInterface gf) {
        this.boardSize = size;
        this.fieldWidth = 16;
        this.totalWidth = this.boardSize * this.fieldWidth;
        this.Empty = null;
        this.EmptyShadow = null;
        this.showLastMove = true;
        this.gameFrame = gf;
        this.state = 1;
        this.position = new Position(this.boardSize);
        this.number = 1;
        this.sgfTree = new SGFTree(new Node(this.number));
        this.gameTrees = new Vector();
        this.gameTrees.addElement(this.sgfTree);
        this.currentTree = 0;
        this.positionNode = this.sgfTree.top();
        this.isActive = true;
        this.dim = new Dimension();
        this.dim.width = 0;
        this.dim.height = 0;
        this.Pw = this.Pb = 0;
        this.setfonts();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        this.VHide = this.gameFrame.getParameter("vhide", false);
        this.VCurrent = this.gameFrame.getParameter("vcurrent", true);
    }

    // Now come the normal routine to draw a board.

    public void active(boolean f) {
        this.isActive = f;
    }

    public void addcomment(String s)
    // add a string to the comments, notifies comment area
    {
        TreeNode p = this.sgfTree.top();
        while (p.haschildren()) {
            p = p.firstChild();
        }
        if (this.positionNode == p) {
            this.getinformation();
        }
        ListElement la = p.node().actions();
        Action a;
        String Added = "";
        ListElement larg;
        outer:
        while (true) {
            while (la != null) {
                a = (Action) la.content();
                if (a.getType().equals("C")) {
                    larg = a.arguments();
                    if ((larg.content()).equals("")) {
                        larg.content(s);
                        Added = s;
                    } else {
                        larg.content(larg.content() + "\n" + s);
                        Added = "\n" + s;
                    }
                    break outer;
                }
                la = la.next();
            }
            p.addaction(new Action("C", s));
            break;
        }
        if (this.positionNode == p) {
            this.gameFrame.appendComment(Added);
            this.showinformation();
        }
    }

    public synchronized void addnewgame() {
        this.state = 1;
        this.getinformation();
        this.sgfTree.top().setaction("AP", "UnaGo:" + this.gameFrame.version(), true);
        this.sgfTree.top().setaction("SZ", "" + this.boardSize, true);
        this.sgfTree.top().setaction("GM", "1", true);
        this.sgfTree.top().setaction("FF",
                this.gameFrame.getParameter("puresgf", false) ? "4" : "1", true);
        final Node n = new Node(this.number);
        this.sgfTree = new SGFTree(n);
        this.currentTree++;
        if (this.currentTree >= this.gameTrees.size()) {
            this.gameTrees.addElement(this.sgfTree);
        } else {
            this.gameTrees.insertElementAt(this.sgfTree, this.currentTree);
        }
        this.resettree();
        this.setnode();
        this.showinformation();
        this.copy();
    }

    public synchronized void allback()
    // to top of tree
    {
        this.getinformation();
        while (this.positionNode.parentPos() != null) {
            this.goback();
        }
        this.showinformation();
        this.copy();
    }

    public synchronized void allforward()
    // to end of variation
    {
        this.getinformation();
        while (this.positionNode.haschildren()) {
            this.goforward();
        }
        this.showinformation();
        this.copy();
    }

    public void asciisave(PrintWriter o)
    // an ASCII image of the board.
    {
        int i, j;
        o.println(this.sgfTree.top().getaction("GN"));
        o.print("      ");
        for (i = 0; i < this.boardSize; i++) {
            char a;
            if (i <= 7) {
                a = (char) ('A' + i);
            } else {
                a = (char) ('A' + i + 1);
            }
            o.print(" " + a);
        }
        o.println();
        o.print("      ");
        for (i = 0; i < this.boardSize; i++) {
            o.print("--");
        }
        o.println("-");
        for (i = 0; i < this.boardSize; i++) {
            o.print("  ");
            if (this.boardSize - i < 10) {
                o.print(" " + (this.boardSize - i));
            } else {
                o.print(this.boardSize - i);
            }
            o.print(" |");
            for (j = 0; j < this.boardSize; j++) {
                switch (this.position.getColor(j, i)) {
                    case 1:
                        o.print(" #");
                        break;
                    case -1:
                        o.print(" O");
                        break;
                    case 0:
                        if (this.position.haslabel(j, i)) {
                            o.print(" " + this.position.label(j, i));
                        } else if (this.position.letter(j, i) > 0) {
                            o.print(" "
                                    + (char) (this.position.letter(j, i) + 'a' - 1));
                        } else if (this.position.marker(j, i) > 0) {
                            o.print(" X");
                        } else if (this.ishand(i) && this.ishand(j)) {
                            o.print(" ,");
                        } else {
                            o.print(" .");
                        }
                        break;
                }
            }
            o.print(" | ");
            if (this.boardSize - i < 10) {
                o.print(" " + (this.boardSize - i));
            } else {
                o.print(this.boardSize - i);
            }
            o.println();
        }
        o.print("      ");
        for (i = 0; i < this.boardSize; i++) {
            o.print("--");
        }
        o.println("-");
        o.print("      ");
        for (i = 0; i < this.boardSize; i++) {
            char a;
            if (i <= 7) {
                a = (char) ('A' + i);
            } else {
                a = (char) ('A' + i + 1);
            }
            o.print(" " + a);
        }
        o.println();
    }

    // *************** mouse events **********************

    public synchronized void back()
    // one move up
    {
        this.state = 1;
        this.getinformation();
        this.goback();
        this.showinformation();
        this.copy();
    }

    public void black()
    // black to play
    {
        this.getinformation();
        this.state = 1;
        this.position.setNextTurnColor(1);
        this.showinformation();
    }

    public synchronized void black(int i, int j)
    // white move at i,j at the end of the main tree
    {
        if (i < 0 || j < 0 || i >= this.boardSize || j >= this.boardSize) {
            return;
        }
        TreeNode p = this.sgfTree.top();
        while (p.haschildren()) {
            p = p.firstChild();
        }
        final Action a = new Action("B", Field.string(i, j));
        final Node n = new Node(p.node().number() + 1);
        n.addaction(a);
        p.addchild(new TreeNode(n));
        n.main(p);
        this.gameFrame.yourMove(this.positionNode != p);
        if (this.positionNode == p) {
            this.forward();
        }
        this.mainColor = -1;
    }

    public boolean canfinish() {
        return this.positionNode.isLastMain();
    }

    public boolean canCaptureOpposite(int i, int j) {
        // Set c to be opposite color than point (i, j).
        final int c = -this.position.getColor(i, j);

        // Capture possible opposite groups in four directions.
        if (i > 0) {
            if (this.canCaptureGroup(i - 1, j, c)) {
                return true;
            }
        }
        if (j > 0) {
            if (this.canCaptureGroup(i, j - 1, c)) {
                return true;
            }
        }
        if (i < this.boardSize - 1) {
            if (this.canCaptureGroup(i + 1, j, c)) {
                return true;
            }
        }
        if (j < this.boardSize - 1) {
            if (this.canCaptureGroup(i, j + 1, c)) {
                return true;
            }
        }
        return false;
    }

    public boolean canCaptureOwn(int i, int j) {
        // Set c to be the same color as point (i, j).
        final int c = this.position.getColor(i, j);

        if (this.canCaptureGroup(i, j, c)) {
            return true;
        }
        return false;
    }

    /**
     * @param i
     * @param j
     * @param n
     */
    public void capture(int i, int j, Node n)
    // capture neighboring groups without liberties
    // capture own group on suicide
    {
        // Set c to be opposite color than point (i, j).
        final int c = -this.position.getColor(i, j);
        this.captured = 0;

        // Capture possible opposite groups in four directions.
        if (i > 0) {
            this.captureGroup(i - 1, j, c, n);
        }
        if (j > 0) {
            this.captureGroup(i, j - 1, c, n);
        }
        if (i < this.boardSize - 1) {
            this.captureGroup(i + 1, j, c, n);
        }
        if (j < this.boardSize - 1) {
            this.captureGroup(i, j + 1, c, n);
        }
        //if (this.position.getColor(i, j) == -c) {

        // The next line does not make sense: it is forbidden to make
        // suicide.
        // this.captureGroup(i, j, -c, n);
        //}

        if (this.captured == 1 && this.position.count(i, j) != 1) {
            this.captured = 0;
        }
        if (!this.gameFrame.getParameter("korule", true)) {
            this.captured = 0;
        }
    }

    // target rectangle things

    public boolean canCaptureGroup(int i, int j, int c) {
        if (this.position.getColor(i, j) != c) {
            return false;
        }
        if (!this.position.markgrouptest(i, j, 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * The method to capture opposite group, starting from point(i, j).
     * That is, point(i, j) should be one of the opposite stones to be captured.
     * c should be the color of the stones to be captured.
     *
     * @param i
     * @param j
     * @param c
     * @param n
     */
    public void captureGroup(int i, int j, int c, Node n)
    // Used by method capture to determine the state of the group at (i,j).
    // Remove it, if it has no liberties and note the removals as actions in
    // the current node.
    {
        if (this.position.getColor(i, j) != c) {
            return;
        }
        // If the current group has no liberties:
        if (!this.position.markgrouptest(i, j, 0)) {
            for (int ii = 0; ii < this.boardSize; ii++) {
                for (int jj = 0; jj < this.boardSize; jj++) {
                    if (this.position.marked(ii, jj)) {
                        n.addchange(new Change(ii, jj, this.position.getColor(ii, jj),
                                this.position.number(ii, jj)));
                        if (this.position.getColor(ii, jj) > 0) {
                            this.Pb++;
                            n.Pb++;
                        } else {
                            this.Pw++;
                            n.Pw++;
                        }
                        this.position.setFieldColor(ii, jj, 0);
                        this.update(ii, jj); // redraw the field (offscreen)
                        this.captured++;
                        this.capturei = ii;
                        this.capturej = jj;
                    }
                }
            }
        }
    }

    public void changemove(int i, int j)
    // change a move to a new field (dangerous!)
    {
        if (this.position.getColor(i, j) != 0) {
            return;
        }
        synchronized (this.positionNode) {
            final ListElement la = this.positionNode.node().actions();
            while (la != null) {
                final Action a = (Action) la.content();
                if (a.getType().equals("B") || a.getType().equals("W")) {
                    this.undonode();
                    a.arguments().content(Field.string(i, j));
                    this.setnode();
                    break;
                }
            }
        }
    }

    public int children() {
        if (!this.positionNode.haschildren()) {
            return 0;
        }
        final TreeNode p = this.positionNode.firstChild();
        if (p == null) {
            return 0;
        }
        ListElement l = p.listelement();
        if (l == null) {
            return 0;
        }
        while (l.previous() != null) {
            l = l.previous();
        }
        int count = 1;
        while (l.next() != null) {
            l = l.next();
            count++;
        }
        return count;
    }

    public void clearmarks()
    // clear all marks in the current node
    {
        this.getinformation();
        this.undonode();
        ListElement la = this.positionNode.node().actions(), lan;
        Action a;
        while (la != null) {
            a = (Action) la.content();
            lan = la.next();
            if (a.getType().equals("M") || a.getType().equals("L")
                    || a.getType().equals("MA") || a.getType().equals("SQ")
                    || a.getType().equals("SL") || a.getType().equals("CR")
                    || a.getType().equals("TR") || a.getType().equals("LB")) {
                this.positionNode.node().removeaction(la);
            }
            la = lan;
        }
        this.setnode();
        this.showinformation();
        this.copy();
    }

    public void clearrange() {
        if (this.range == -1) {
            return;
        }
        this.range = -1;
        this.updateall();
        this.copy();
    }

    // *************** keyboard events ********************

    public void clearremovals()
    // undo all removals in the current node
    {
        if (this.positionNode.haschildren()) {
            return;
        }
        this.getinformation();
        this.undonode();
        ListElement la = this.positionNode.node().actions(), lan;
        Action a;
        while (la != null) {
            a = (Action) la.content();
            lan = la.next();
            if (a.getType().equals("AB") || a.getType().equals("AW")
                    || a.getType().equals("AE")) {
                this.positionNode.node().removeaction(la);
            }
            la = lan;
        }
        this.setnode();
        this.showinformation();
        this.copy();
    }

    public void clearsend() {
        if (this.sendX >= 0) {
            final int i = this.sendX;
            this.sendX = -1;
            this.update(i, this.sendY);
        }
    }

    public void copy()
    // copy the offscreen board to the screen
    {
        if (this.ActiveImage == null) {
            return;
        }
        try {
            this.getGraphics().drawImage(this.ActiveImage, 0, 0, this);
        } catch (final Exception e) {
        }
    }

    // set things on the board

    public void delete(int i, int j)
    // delete the stone and note it
    {
        if (this.position.getColor(i, j) == 0) {
            return;
        }
        synchronized (this.positionNode) {
            Node n = this.positionNode.node();
            if (this.gameFrame.getParameter("puresgf", true)
                    && (n.contains("B") || n.contains("W"))) {
                n = this.newnode();
            }
            final String field = Field.string(i, j);
            if (n.contains("AB", field)) {
                this.undonode();
                n.toggleaction(new Action("AB", field));
                this.setnode();
            } else if (n.contains("AW", field)) {
                this.undonode();
                n.toggleaction(new Action("AW", field));
                this.setnode();
            } else if (n.contains("B", field)) {
                this.undonode();
                n.toggleaction(new Action("B", field));
                this.setnode();
            } else if (n.contains("W", field)) {
                this.undonode();
                n.toggleaction(new Action("W", field));
                this.setnode();
            } else {
                final Action a = new Action("AE", field);
                n.expandaction(a);
                n.addchange(new Change(i, j, this.position.getColor(i, j)));
                this.position.setFieldColor(i, j, 0);
                this.update(i, j);
            }
            this.showinformation();
            this.copy();
        }
    }

    void deletemouse(int i, int j)
    // delete a stone at i,j
    {
        if (this.positionNode.haschildren()) {
            return;
        }
        this.deletemousec(i, j);
    }

    void deletemousec(int i, int j)
    // delete a stone at i,j
    {
        this.delete(i, j);
        this.undonode();
        this.setnode();
        this.showinformation();
    }

    public void deletestones()
    // hide stones
    {
        this.getinformation();
        this.state = 7;
        this.showinformation();
    }

    public boolean deltree() {
        this.newtree();
        return true;
    }

    public String docount()
    // maka a local count and return result string
    {
        this.clearmarks();
        this.getinformation();
        int i, j;
        int tb = 0, tw = 0, sb = 0, sw = 0;
        this.position.getterritory();
        for (i = 0; i < this.boardSize; i++) {
            for (j = 0; j < this.boardSize; j++) {
                if (this.position.territory(i, j) == 1 || this.position.territory(i, j) == -1) {
                    this.markterritory(i, j, this.position.territory(i, j));
                    if (this.position.territory(i, j) > 0) {
                        tb++;
                    } else {
                        tw++;
                    }
                } else {
                    if (this.position.getColor(i, j) > 0) {
                        sb++;
                    } else if (this.position.getColor(i, j) < 0) {
                        sw++;
                    }
                }
            }
        }
        this.showinformation();
        this.copy();
        return this.gameFrame.resourceString("Chinese_count_") + "\n"
                + this.gameFrame.resourceString("Black__") + (sb + tb)
                + this.gameFrame.resourceString("__White__") + (sw + tw) + "\n"
                + this.gameFrame.resourceString("Japanese_count_") + "\n"
                + this.gameFrame.resourceString("Black__") + (this.Pw + tb)
                + this.gameFrame.resourceString("__White__") + (this.Pb + tw);
    }

    public String done()
    // count territory and return result string
    // notifies BoardInterface
    {
        if (this.positionNode.haschildren()) {
            return null;
        }
        this.clearmarks();
        this.getinformation();
        int i, j;
        int tb = 0, tw = 0, sb = 0, sw = 0;
        this.position.getterritory();
        for (i = 0; i < this.boardSize; i++) {
            for (j = 0; j < this.boardSize; j++) {
                if (this.position.territory(i, j) == 1 || this.position.territory(i, j) == -1) {
                    this.markterritory(i, j, this.position.territory(i, j));
                    if (this.position.territory(i, j) > 0) {
                        tb++;
                    } else {
                        tw++;
                    }
                } else {
                    if (this.position.getColor(i, j) > 0) {
                        sb++;
                    } else if (this.position.getColor(i, j) < 0) {
                        sw++;
                    }
                }
            }
        }
        final String s = this.gameFrame.resourceString("Chinese_count_") + "\n"
                + this.gameFrame.resourceString("Black__") + (sb + tb)
                + this.gameFrame.resourceString("__White__") + (sw + tw) + "\n"
                + this.gameFrame.resourceString("Japanese_count_") + "\n"
                + this.gameFrame.resourceString("Black__") + (this.Pw + tb)
                + this.gameFrame.resourceString("__White__") + (this.Pb + tw);
        this.showinformation();
        this.copy();
        if (this.positionNode.node().main()) {
            this.gameFrame.result(tb, tw);
        }
        return s;
    }

    public void doundo(TreeNode pos1) {
        if (pos1 != this.positionNode) {
            return;
        }
        if (this.positionNode.parentPos() == null) {
            this.undonode();
            this.positionNode.removeall();
            this.positionNode.node().removeactions();
            this.showinformation();
            this.copy();
            return;
        }
        final TreeNode pos = this.positionNode;
        this.goback();
        if (pos == this.positionNode.firstchild()) {
            this.positionNode.removeall();
        } else {
            this.positionNode.remove(pos);
        }
        this.goforward();
        this.showinformation();
        this.copy();
    }

    public void drawTarget(int i, int j) {
        this.copy();
        final Graphics g = this.getGraphics();
        if (g == null) {
            return;
        }
        i = this.offset + this.offsetAboveBelow + i * this.fieldWidth + this.fieldWidth / 2;
        j = this.offset + this.offsetAboveBelow + j * this.fieldWidth + this.fieldWidth / 2;
        if (this.gameFrame.bwColor()) {
            g.setColor(Color.white);
        } else {
            g.setColor(Color.gray.brighter());
        }
        g.drawRect(i - this.fieldWidth / 4, j - this.fieldWidth / 4, this.fieldWidth / 2, this.fieldWidth / 2);
        g.dispose();
    }

    public void emptyaction(Node n, Action a)
    // interpret a remove stone action
    {
        int i, j;
        ListElement larg = a.arguments();
        while (larg != null) {
            final String s = (String) larg.content();
            i = Field.i(s);
            j = Field.j(s);
            if (this.valid(i, j)) {
                n.addchange(new Change(i, j, this.position.getColor(i, j), this.position.number(
                        i, j)));
                if (this.position.getColor(i, j) < 0) {
                    n.Pw++;
                    this.Pw++;
                } else if (this.position.getColor(i, j) > 0) {
                    n.Pb++;
                    this.Pb++;
                }
                this.position.setFieldColor(i, j, 0);
                this.update(i, j);
            }
            larg = larg.next();
        }
    }

    public synchronized void emptypaint()
    // Draw an empty board onto the graphics context g.
    // Including lines, coordinates and markers.
    {
        if (Board.woodpaint != null && Board.woodpaint.isAlive()) {
            Board.woodpaint.stopit();
        }
        synchronized (this) {
            if (this.Empty == null || this.EmptyShadow == null) {
                return;
            }
            final Graphics2D g = (Graphics2D) this.Empty.getGraphics(), gs = (Graphics2D) this.EmptyShadow
                    .getGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            g.setColor(rene.gui.Global.ControlBackground);
            g.fillRect(0, 0, this.boardSize * this.fieldWidth + 2 * this.pixelCoordinate + 100, this.boardSize
                    * this.fieldWidth + 2 * this.pixelCoordinate + 100);
            if (!this.gameFrame.getParameter("beauty", true)
                    || !this.trywood(g, gs, this.boardSize * this.fieldWidth + 2 * this.pixelCoordinate)) // beauty
            // board
            // not
            // available
            {
                g.setColor(this.gameFrame.boardColor());
                g.fillRect(this.offset + this.offsetAboveBelow - this.pixelCoordinate, this.offset + this.offsetAboveBelow
                        - this.pixelCoordinate, this.boardSize * this.fieldWidth + 2 * this.pixelCoordinate, this.boardSize
                        * this.fieldWidth + 2 * this.pixelCoordinate);
                gs.setColor(this.gameFrame.boardColor());
                gs.fillRect(this.offset + this.offsetAboveBelow - this.pixelCoordinate, this.offset + this.offsetAboveBelow
                        - this.pixelCoordinate, this.boardSize * this.fieldWidth + 2 * this.pixelCoordinate, this.boardSize
                        * this.fieldWidth + 2 * this.pixelCoordinate);
            }
            if (this.gameFrame.getParameter("beautystones", true)) {
                this.stonespaint();
            }
            g.setColor(Color.black);
            gs.setColor(Color.black);
            int i, j, x, y1, y2;
            // Draw lines
            x = this.offset + this.offsetAboveBelow + this.fieldWidth / 2;
            y1 = this.offset + this.offsetAboveBelow + this.fieldWidth / 2;
            y2 = this.offset + this.fieldWidth / 2 + this.offsetAboveBelow + (this.boardSize - 1) * this.fieldWidth;
            for (i = 0; i < this.boardSize; i++) {
                g.drawLine(x, y1, x, y2);
                g.drawLine(y1, x, y2, x);
                gs.drawLine(x, y1, x, y2);
                gs.drawLine(y1, x, y2, x);
                x += this.fieldWidth;
            }
            if (this.boardSize == 19) // handicap markers
            {
                for (i = 0; i < 3; i++) {
                    for (j = 0; j < 3; j++) {
                        this.hand(g, 3 + i * 6, 3 + j * 6);
                        this.hand(gs, 3 + i * 6, 3 + j * 6);
                    }
                }
            } else if (this.boardSize >= 11) // handicap markers
            {
                if (this.boardSize >= 15 && this.boardSize % 2 == 1) {
                    final int k = this.boardSize / 2 - 3;
                    for (i = 0; i < 3; i++) {
                        for (j = 0; j < 3; j++) {
                            this.hand(g, 3 + i * k, 3 + j * k);
                            this.hand(gs, 3 + i * k, 3 + j * k);
                        }
                    }
                } else {
                    this.hand(g, 3, 3);
                    this.hand(g, this.boardSize - 4, 3);
                    this.hand(g, 3, this.boardSize - 4);
                    this.hand(g, this.boardSize - 4, this.boardSize - 4);
                    this.hand(gs, 3, 3);
                    this.hand(gs, this.boardSize - 4, 3);
                    this.hand(gs, 3, this.boardSize - 4);
                    this.hand(gs, this.boardSize - 4, this.boardSize - 4);
                }
            }
            // coordinates below and to the right
            if (this.offsetRightBelow > 0) {
                g.setFont(this.font);
                int y = this.offset + this.offsetAboveBelow;
                final int h = this.fontMetrics.getAscent() / 2 - 1;
                for (i = 0; i < this.boardSize; i++) {
                    final String s = "" + (this.boardSize - i);
                    final int w = this.fontMetrics.stringWidth(s) / 2;
                    g.drawString(s, this.offset + this.offsetAboveBelow + this.boardSize * this.fieldWidth
                            + this.fieldWidth / 2 + this.pixelCoordinate - w, y + this.fieldWidth / 2 + h);
                    y += this.fieldWidth;
                }
                x = this.offset + this.offsetAboveBelow;
                final char a[] = new char[1];
                for (i = 0; i < this.boardSize; i++) {
                    j = i;
                    if (j > 7) {
                        j++;
                    }
                    if (j > 'Z' - 'A') {
                        a[0] = (char) ('a' + j - ('Z' - 'A') - 1);
                    } else {
                        a[0] = (char) ('A' + j);
                    }
                    final String s = new String(a);
                    final int w = this.fontMetrics.stringWidth(s) / 2;
                    g.drawString(s, x + this.fieldWidth / 2 - w, this.offset + this.offsetAboveBelow
                            + this.boardSize * this.fieldWidth + this.fieldWidth / 2 + this.pixelCoordinate + h);
                    x += this.fieldWidth;
                }
            }
            // coordinates to the left and above
            if (this.offsetAboveBelow > 0) {
                g.setFont(this.font);
                int y = this.offset + this.offsetAboveBelow;
                final int h = this.fontMetrics.getAscent() / 2 - 1;
                for (i = 0; i < this.boardSize; i++) {
                    final String s = "" + (this.boardSize - i);
                    final int w = this.fontMetrics.stringWidth(s) / 2;
                    g.drawString(s, this.offset + this.fieldWidth / 2 - this.pixelCoordinate - w, y
                            + this.fieldWidth / 2 + h);
                    y += this.fieldWidth;
                }
                x = this.offset + this.offsetAboveBelow;
                final char a[] = new char[1];
                for (i = 0; i < this.boardSize; i++) {
                    j = i;
                    if (j > 7) {
                        j++;
                    }
                    if (j > 'Z' - 'A') {
                        a[0] = (char) ('a' + j - ('Z' - 'A') - 1);
                    } else {
                        a[0] = (char) ('A' + j);
                    }
                    final String s = new String(a);
                    final int w = this.fontMetrics.stringWidth(s) / 2;
                    g.drawString(s, x + this.fieldWidth / 2 - w, this.offset + this.fieldWidth / 2
                            - this.pixelCoordinate + h);
                    x += this.fieldWidth;
                }
            }
        }
    }

    public String extraInformation()
    // get a mixture from handicap, komi and prisoners
    {
        final StringBuffer b = new StringBuffer(this.gameFrame.resourceString("_("));
        final Node n = this.sgfTree.top().node();
        if (n.contains("HA")) {
            b.append(this.gameFrame.resourceString("Ha_"));
            b.append(n.getaction("HA"));
        }
        if (n.contains("KM")) {
            b.append(this.gameFrame.resourceString("__Ko"));
            b.append(n.getaction("KM"));
        }
        b.append(this.gameFrame.resourceString("__B"));
        b.append("" + this.Pw);
        b.append(this.gameFrame.resourceString("__W"));
        b.append("" + this.Pb);
        b.append(this.gameFrame.resourceString("_)"));
        return b.toString();
    }

    public synchronized void fastback()
    // 10 moves up
    {
        this.getinformation();
        for (int i = 0; i < 10; i++) {
            this.goback();
        }
        this.showinformation();
        this.copy();
    }

    public synchronized void fastforward()
    // 10 moves down
    {
        this.getinformation();
        for (int i = 0; i < 10; i++) {
            this.goforward();
        }
        this.showinformation();
        this.copy();
    }

    Node firstnode() {
        return this.sgfTree.top().node();
    }

    public String formtime(int t) {
        int s, m;
        final int h = t / 3600;
        if (h >= 1) {
            t = t - 3600 * h;
            m = t / 60;
            s = t - 60 * m;
            return "" + h + ":" + this.twodigits(m) + ":" + this.twodigits(s);
        } else {
            m = t / 60;
            s = t - 60 * m;
            return "" + m + ":" + this.twodigits(s);
        }
    }

    public synchronized void forward()
    // one move down
    {
        this.state = 1;
        this.getinformation();
        this.goforward();
        this.showinformation();
        this.copy();
    }

    Image getBoardImage() {
        return this.ActiveImage;
    }

    Dimension getBoardImageSize() {
        return new Dimension(this.ActiveImage.getWidth(this),
                this.ActiveImage.getHeight(this));
    }

    public int getboardsize() {
        return this.boardSize;
    }

    public void getinformation()
    // update the comment, when leaving the position
    {
        ListElement la = this.positionNode.node().actions();
        Action a;
        this.clearsend();
        while (la != null) {
            a = (Action) la.content();
            if (a.getType().equals("C")) {
                if (this.gameFrame.getComment().equals("")) {
                    this.positionNode.node().removeaction(la);
                } else {
                    a.arguments().content(this.gameFrame.getComment());
                }
                return;
            }
            la = la.next();
        }
        final String s = this.gameFrame.getComment();
        if (!s.equals("")) {
            this.positionNode.addaction(new Action("C", s));
        }
    }

    public String getKomi()
    // get Komi string
    {
        return this.sgfTree.top().getaction("KM");
    }

    @Override
    public Dimension getMinimumSize()
    // for the layout menager of the containing component
    {
        Dimension d = this.getSize();
        if (d.width == 0) {
            return d = this.dim;
        }
        d.width = d.height + 5;
        return d;
    }

    String getname()
    // get node name
    {
        return this.sgfTree.top().getaction("N");
    }

    @Override
    public Dimension getPreferredSize()
    // for the layout menager of the containing component
    {
        return this.getMinimumSize();
    }

    public void goback()
    // go one move back
    {
        this.state = 1;
        if (this.positionNode.parentPos() == null) {
            return;
        }
        this.undonode();
        this.positionNode = this.positionNode.parentPos();
        this.setlast();
    }

    public void goforward()
    // go one move forward
    {
        if (!this.positionNode.haschildren()) {
            return;
        }
        this.positionNode = this.positionNode.firstChild();
        this.setnode();
        this.setlast();
    }

    public void gotoMove(int move) {
        while (this.number <= move && this.positionNode.firstChild() != null) {
            this.goforward();
        }
    }

    public synchronized void gotonext()
    // goto next named node
    {
        this.state = 1;
        this.getinformation();
        this.goforward();
        while (this.positionNode.node().getaction("N").equals("")) {
            if (!this.positionNode.haschildren()) {
                break;
            }
            this.goforward();
        }
        this.showinformation();
        this.copy();
    }

    public synchronized void gotonextmain()
    // goto next game tree
    {
        if (this.currentTree + 1 >= this.gameTrees.size()) {
            return;
        }
        this.state = 1;
        this.getinformation();
        this.sgfTree.top().setaction("AP", "UnaGo:" + this.gameFrame.version(), true);
        this.sgfTree.top().setaction("SZ", "" + this.boardSize, true);
        this.sgfTree.top().setaction("GM", "1", true);
        this.sgfTree.top().setaction("FF",
                this.gameFrame.getParameter("puresgf", false) ? "4" : "1", true);
        this.currentTree++;
        this.sgfTree = (SGFTree) this.gameTrees.elementAt(this.currentTree);
        this.resettree();
        this.setnode();
        this.showinformation();
        this.copy();
    }

    public synchronized void gotoprevious()
    // gotoprevious named node
    {
        this.state = 1;
        this.getinformation();
        this.goback();
        while (this.positionNode.node().getaction("N").equals("")) {
            if (this.positionNode.parentPos() == null) {
                break;
            }
            this.goback();
        }
        this.showinformation();
        this.copy();
    }

    public synchronized void gotopreviousmain()
    // goto previous game tree
    {
        if (this.currentTree == 0) {
            return;
        }
        this.state = 1;
        this.getinformation();
        this.sgfTree.top().setaction("AP", "UnaGo:" + this.gameFrame.version(), true);
        this.sgfTree.top().setaction("SZ", "" + this.boardSize, true);
        this.sgfTree.top().setaction("GM", "1", true);
        this.sgfTree.top().setaction("FF",
                this.gameFrame.getParameter("puresgf", false) ? "4" : "1", true);
        this.currentTree--;
        this.sgfTree = (SGFTree) this.gameTrees.elementAt(this.currentTree);
        this.resettree();
        this.setnode();
        this.showinformation();
        this.copy();
    }

    void gotovariation(int i, int j)
    // goto the variation at (i,j)
    {
        final TreeNode newpos = this.position.tree(i, j);
        this.getinformation();
        if (this.VCurrent && newpos.parentPos() == this.positionNode.parentPos()) {
            this.goback();
            this.positionNode = newpos;
            this.setnode();
            this.setlast();
        } else if (!this.VCurrent && newpos.parentPos() == this.positionNode) {
            this.positionNode = newpos;
            this.setnode();
            this.setlast();
        }
        this.copy();
        this.showinformation();
    }

    public void hand(Graphics g, int i, int j)
    // help function for emptypaint (Handicap point)
    {
        g.setColor(Color.black);
        int s = this.fieldWidth / 10;
        if (s < 2) {
            s = 2;
        }
        g.fillRect(this.offset + this.offsetAboveBelow + this.fieldWidth / 2 + i * this.fieldWidth - s, this.offset
                + this.offsetAboveBelow + this.fieldWidth / 2 + j * this.fieldWidth - s, 2 * s + 1, 2 * s + 1);
    }

    public void handicap(int n)
    // set number of handicap points
    {
        final int h = this.boardSize < 13 ? 3 : 4;
        if (n > 5) {
            this.setblack(h - 1, this.boardSize / 2);
            this.setblack(this.boardSize - h, this.boardSize / 2);
        }
        if (n > 7) {
            this.setblack(this.boardSize / 2, h - 1);
            this.setblack(this.boardSize / 2, this.boardSize - h);
        }
        switch (n) {
            case 9:
            case 7:
            case 5:
                this.setblack(this.boardSize / 2, this.boardSize / 2);
            case 8:
            case 6:
            case 4:
                this.setblack(this.boardSize - h, this.boardSize - h);
            case 3:
                this.setblack(h - 1, h - 1);
            case 2:
                this.setblack(h - 1, this.boardSize - h);
            case 1:
                this.setblack(this.boardSize - h, h - 1);
        }
        this.mainColor = -1;
    }

    public boolean hasvariation() {
        final ListElement l = this.positionNode.listelement();
        if (l == null) {
            return false;
        }
        if (l.next() == null) {
            return false;
        }
        return true;
    }

    public void insertnode()
    // insert an empty node
    {
        if (this.positionNode.haschildren() && !this.gameFrame.askInsert()) {
            return;
        }
        final Node n = new Node(this.positionNode.node().number());
        this.positionNode.insertchild(new TreeNode(n));
        n.main(this.positionNode);
        this.getinformation();
        this.positionNode = this.positionNode.lastChild();
        this.setlast();
        this.showinformation();
        this.copy();
    }

    public void insertvariation()
    // insert an empty variation to the current
    {
        if (this.positionNode.parentPos() == null) {
            return;
        }
        this.getinformation();
        final int c = this.position.getNextTurnColor();
        this.back();
        final Node n = new Node(2);
        this.positionNode.addchild(new TreeNode(n));
        n.main(this.positionNode);
        this.positionNode = this.positionNode.lastChild();
        this.setlast();
        this.position.setNextTurnColor(-c);
        this.showinformation();
        this.copy();
    }

    boolean ishand(int i) {
        if (this.boardSize > 13) {
            return i == 3 || i == this.boardSize - 4 || i == this.boardSize / 2;
        } else if (this.boardSize > 9) {
            return i == 3 || i == this.boardSize - 4;
        } else {
            return false;
        }
    }

    public boolean ismain() {
        return this.positionNode.isLastMain();
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        if (e.isActionKey()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DOWN:
                    this.forward();
                    break;
                case KeyEvent.VK_UP:
                    this.back();
                    break;
                case KeyEvent.VK_LEFT:
                    this.varleft();
                    break;
                case KeyEvent.VK_RIGHT:
                    this.varright();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    this.fastforward();
                    break;
                case KeyEvent.VK_PAGE_UP:
                    this.fastback();
                    break;
                case KeyEvent.VK_BACK_SPACE:
                case KeyEvent.VK_DELETE:
                    this.undo();
                    break;
                case KeyEvent.VK_HOME:
                    this.varmain();
                    break;
                case KeyEvent.VK_END:
                    this.varmaindown();
                    break;
            }
        } else {
            switch (e.getKeyChar()) {
                case '*':
                    this.varmain();
                    break;
                case '/':
                    this.varmaindown();
                    break;
                case 'v':
                case 'V':
                    this.varup();
                    break;
                case 'm':
                case 'M':
                    this.mark();
                    break;
                case 'p':
                case 'P':
                    this.resume();
                    break;
                case 'c':
                case 'C':
                    this.specialmark(Field.CIRCLE);
                    break;
                case 's':
                case 'S':
                    this.specialmark(Field.SQUARE);
                    break;
                case 't':
                case 'T':
                    this.specialmark(Field.TRIANGLE);
                    break;
                case 'l':
                case 'L':
                    this.letter();
                    break;
                case 'r':
                case 'R':
                    this.specialmark(Field.CROSS);
                    break;
                case 'w':
                    this.setwhite();
                    break;
                case 'b':
                    this.setblack();
                    break;
                case 'W':
                    this.white();
                    break;
                case 'B':
                    this.black();
                    break;
                case '+':
                    this.gotonext();
                    break;
                case '-':
                    this.gotoprevious();
                    break;
                // Bug (VK_DELETE not reported as ActionEvent)
                case KeyEvent.VK_BACK_SPACE:
                case KeyEvent.VK_DELETE:
                    this.undo();
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void lastrange(int n)
    // set the range for stone numbers
    {
        final int l = this.positionNode.node().number() - 2;
        this.range = l / n * n;
        if (this.range < 0) {
            this.range = 0;
        }
        this.doesKeepRange = true;
        this.updateall();
        this.copy();
        this.doesKeepRange = false;
    }

    public void letter()
    // letter
    {
        this.getinformation();
        this.state = 6;
        this.showinformation();
    }

    public void letter(int i, int j)
    // Write a character to the field at i,j
    {
        final Action a = new LabelAction(Field.string(i, j), this.gameFrame);
        this.positionNode.node().toggleaction(a);
        this.update(i, j);
    }

    public void load(BufferedReader in) throws IOException
    // load a game from the stream
    {
        final Vector v = SGFTree.load(in, this.gameFrame);
        synchronized (this) {
            if (v.size() == 0) {
                return;
            }
            this.showLastMove = false;
            this.update(this.lastMoveX, this.lastMoveY);
            this.showLastMove = true;
            this.lastMoveX = this.lastMoveY = -1;
            this.newtree();
            this.gameTrees = v;
            this.sgfTree = (SGFTree) v.elementAt(0);
            this.currentTree = 0;
            this.resettree();
            this.setnode();
            this.showinformation();
            this.copy();
        }
    }

    public void loadXml(XmlReader xml) throws XmlReaderException
    // load a game from the stream
    {
        final Vector v = SGFTree.load(xml, this.gameFrame);
        synchronized (this) {
            if (v.size() == 0) {
                return;
            }
            this.showLastMove = false;
            this.update(this.lastMoveX, this.lastMoveY);
            this.showLastMove = true;
            this.lastMoveX = this.lastMoveY = -1;
            this.newtree();
            this.gameTrees = v;
            this.sgfTree = (SGFTree) v.elementAt(0);
            this.currentTree = 0;
            this.resettree();
            this.setnode();
            this.showinformation();
            this.copy();
        }
    }

    public String lookuptime(String type) {
        int t;
        if (this.positionNode.parentPos() != null) {
            final String s = this.positionNode.parentPos().node().getaction(type);
            if (!s.equals("")) {
                try {
                    t = Integer.parseInt(s);
                    return this.formtime(t);
                } catch (final Exception e) {
                    return "";
                }
            } else {
                return "";
            }
        }
        return "";
    }

    public int maincolor() {
        return this.mainColor;
    }

    public synchronized void makeimages()
    // create images and repaint, if ActiveImage is invalid.
    // uses parameters from the BoardInterface for coordinate layout.
    {
        this.dim = this.getSize();
        final boolean c = this.gameFrame.getParameter("coordinates", true);
        final boolean ulc = this.gameFrame.getParameter("upperleftcoordinates", true);
        final boolean lrc = this.gameFrame
                .getParameter("lowerrightcoordinates", false);
        this.fieldWidth = this.dim.height
                / (this.boardSize + 1 + (c ? (ulc ? 1 : 0) + (lrc ? 1 : 0) : 0));
        this.pixelCoordinate = this.fieldWidth / 4;
        this.offset = this.fieldWidth / 2 + this.pixelCoordinate;
        this.totalWidth = this.boardSize * this.fieldWidth + 2 * this.offset;
        if (c) {
            if (lrc) {
                this.offsetRightBelow = this.fieldWidth;
            } else {
                this.offsetRightBelow = 0;
            }
            if (ulc) {
                this.offsetAboveBelow = this.fieldWidth;
            } else {
                this.offsetAboveBelow = 0;
            }
        } else {
            this.offsetRightBelow = this.offsetAboveBelow = 0;
        }
        this.totalWidth += this.offsetAboveBelow + this.offsetRightBelow;
        if (!this.gameFrame.boardShowing()) {
            return;
        }
        // create image and paint empty board
        synchronized (this) {
            this.Empty = this.createImage(this.totalWidth, this.totalWidth);
            this.EmptyShadow = this.createImage(this.totalWidth, this.totalWidth);
        }
        this.emptypaint();
        this.ActiveImage = this.createImage(this.totalWidth, this.totalWidth);
        // update the emtpy board
        this.updateall();
        this.repaint();
    }

    public void mark()
    // marking
    {
        this.getinformation();
        this.state = 5;
        this.showinformation();
    }

    public void mark(int i, int j)
    // Emphasize the field at i,j
    {
        final Node n = this.positionNode.node();
        final Action a = new MarkAction(Field.string(i, j), this.gameFrame);
        n.toggleaction(a);
        this.update(i, j);
    }

    public void markterritory(int i, int j, int color) {
        Action a;
        if (color > 0) {
            a = new Action("TB", Field.string(i, j));
        } else {
            a = new Action("TW", Field.string(i, j));
        }
        this.positionNode.node().expandaction(a);
        this.update(i, j);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    // start showing coordinates
    {
        if (!this.isActive) {
            return;
        }
        if (this.doesDisplayNodeName) {
            this.gameFrame.setLabel(this.lText);
            this.doesDisplayNodeName = false;
        }
        int x = e.getX(), y = e.getY();
        x -= this.offset + this.offsetAboveBelow;
        y -= this.offset + this.offsetAboveBelow;
        final int i = x / this.fieldWidth, j = y / this.fieldWidth; // determine position
        if (i < 0 || j < 0 || i >= this.boardSize || j >= this.boardSize) {
            return;
        }
        if (this.gameFrame.showTarget()) {
            this.drawTarget(i, j);
            this.iTarget = i;
            this.jTarget = j;
        }
        this.gameFrame.setLabelM(Field.coordinate(i, j, this.boardSize));
    }

    // *****************************************
    // Methods to be called from outside sources
    // *****************************************

    // ****** navigational things **************

    // methods to move in the game tree, including
    // update of the visible board:

    @Override
    public void mouseExited(MouseEvent e)
    // stop showing coordinates
    {
        if (!this.isActive) {
            return;
        }
        this.gameFrame.setLabelM("--");
        if (!this.nodeName.equals("")) {
            this.gameFrame.setLabel(this.nodeName);
            this.doesDisplayNodeName = true;
        }
        if (this.gameFrame.showTarget()) {
            this.iTarget = this.jTarget = -1;
            this.copy();
        }
    }

    @Override
    public synchronized void mouseMoved(MouseEvent e)
    // show coordinates in the Lm Label
    {
        if (!this.isActive) {
            return;
        }
        if (this.doesDisplayNodeName) {
            this.gameFrame.setLabelM(this.lText);
            this.doesDisplayNodeName = false;
        }
        int x = e.getX(), y = e.getY();
        x -= this.offset + this.offsetAboveBelow;
        y -= this.offset + this.offsetAboveBelow;
        final int i = x / this.fieldWidth, j = y / this.fieldWidth; // determine position
        if (i < 0 || j < 0 || i >= this.boardSize || j >= this.boardSize) {
            if (this.gameFrame.showTarget()) {
                this.iTarget = this.jTarget = -1;
                this.copy();
            }
            return;
        }
        if (this.gameFrame.showTarget() && (this.iTarget != i || this.jTarget != j)) {
            this.drawTarget(i, j);
            this.iTarget = i;
            this.jTarget = j;
        }
        this.gameFrame.setLabelM(Field.coordinate(i, j, this.boardSize));
    }

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        this.MouseDown = true;
        this.requestFocus();
    }

    @Override
    public synchronized void mouseReleased(MouseEvent e)
    // handle mouse input
    {
        if (!this.MouseDown) {
            return;
        }
        int x = e.getX(), y = e.getY();
        this.MouseDown = false;
        if (this.ActiveImage == null) {
            return;
        }
        if (!this.isActive) {
            return;
        }
        this.getinformation();
        x -= this.offset + this.offsetAboveBelow;
        y -= this.offset + this.offsetAboveBelow;
        final int i = x / this.fieldWidth, j = y / this.fieldWidth; // determine position
        if (x < 0 || y < 0 || i < 0 || j < 0 || i >= this.boardSize || j >= this.boardSize) {
            return;
        }
        switch (this.state) {
            case 3: // set a black stone - set means this is not a game move.
                if (this.gameFrame.blocked() && this.positionNode.isLastMain()) {
                    return;
                }
                if (e.isShiftDown() && e.isControlDown()) {
                    this.setmousec(i, j, 1);
                } else {
                    this.setmouse(i, j, 1);
                }
                break;
            case 4: // set a white stone - set means this is not a game move.
                if (this.gameFrame.blocked() && this.positionNode.isLastMain()) {
                    return;
                }
                if (e.isShiftDown() && e.isControlDown()) {
                    this.setmousec(i, j, -1);
                } else {
                    this.setmouse(i, j, -1);
                }
                break;
            case 5:
                this.mark(i, j);
                break;
            case 6:
                this.letter(i, j);
                break;
            case 7: // delete a stone
                if (e.isShiftDown() && e.isControlDown()) {
                    this.deletemousec(i, j);
                } else {
                    this.deletemouse(i, j);
                }
                break;
            case 8: // remove a group
                this.removemouse(i, j);
                break;
            case 9:
                this.specialmark(i, j);
                break;
            case 10:
                this.textmark(i, j);
                break;
            case 1:
            case 2: // normal move mode
                if (e.isShiftDown()) // create variation
                {
                    if (e.isControlDown()) {
                        if (this.gameFrame.blocked() && this.positionNode.isLastMain()) {
                            return;
                        }
                        this.changemove(i, j);
                    } else {
                        this.variation(i, j);
                    }
                } else if (e.isControlDown())
                // goto variation
                {
                    if (this.position.tree(i, j) != null) {
                        this.gotovariation(i, j);
                    }
                } else if (e.isMetaDown()) // right click
                {
                    if (this.position.tree(i, j) != null) {
                        this.gotovariation(i, j);
                    } else {
                        this.variation(i, j);
                    }
                } else
                // place a W or B stone
                {
                    if (this.gameFrame.blocked() && this.positionNode.isLastMain()) {
                        return;
                    }
                    this.movemouse(i, j);
                }
                break;
        }
        this.showinformation();
        this.copy(); // show position
    }

    void movemouse(int i, int j)
    // set a move at i,j
    {
        if (this.positionNode.haschildren()) {
            return;
        }
        // Prevent ko.
        if (this.captured == 1 && this.capturei == i && this.capturej == j
                && this.gameFrame.getParameter("preventko", true)) {
            return;
        }
        if (this.position.getColor(i, j) != 0) {
            return;
        }

        boolean isForbidden = false;
        // Prevent forbidden move:
        // A forbidden move is a move that:
        // (1) It will not capture any stones;
        // (2) It will make the group at current move point have no liberties.

        // We try to make the current move at the point (i, j).
        this.position.setFieldColor(i, j, this.position.getNextTurnColor());

        // If it does not capture opposite stones but makes self stones
        // to be captured, the it is a forbidden move.
        if ((!this.canCaptureOpposite(i, j)) && (this.canCaptureOwn(i, j))) {
            isForbidden = true;
        }
        // We need to reset the current point, since this is just a try.
        this.position.setFieldColor(i, j, 0);
        if (isForbidden) {
            return;
        }

        // Set a new move.
        this.set(i, j);
    }

    public Node newnode() {
        final Node n = new Node(++this.number);
        final TreeNode newpos = new TreeNode(n);
        this.positionNode.addchild(newpos); // note the move
        n.main(this.positionNode);
        this.positionNode = newpos; // update current position pointerAction a;
        this.setlast();
        return n;
    }

    Node newtree() {
        this.number = 1;
        this.Pw = this.Pb = 0;
        final Node n = new Node(this.number);
        this.sgfTree = new SGFTree(n);
        this.gameTrees.setElementAt(this.sgfTree, this.currentTree);
        this.resettree();
        return n;
    }

    @Override
    synchronized public void paint(Graphics g)
    // repaint the board (generate images at first call)
    {
        final Dimension d = this.getSize();
        // test if ActiveImage is valid.
        if (this.dim.width != d.width || this.dim.height != d.height) {
            this.dim = d;
            this.makeimages();
            this.repaint();
            return;
        } else {
            if (this.ActiveImage != null) {
                g.drawImage(this.ActiveImage, 0, 0, this);
            }
        }
        if (!this.Activated && this.gameFrame.boardShowing()) {
            this.Activated = true;
            this.gameFrame.activate();
        }
        g.setColor(this.gameFrame.backgroundColor());
        if (d.width > this.totalWidth) {
            g.fillRect(this.totalWidth, 0, d.width - this.totalWidth, this.totalWidth);
        }
        if (d.height > this.totalWidth) {
            g.fillRect(0, this.totalWidth, d.width, d.height - this.totalWidth);
        }
    }

    public synchronized void pass()
    // pass at current node
    // notify BoardInterface
    {
        if (this.positionNode.haschildren()) {
            return;
        }
        if (this.gameFrame.blocked() && this.positionNode.node().main()) {
            return;
        }
        this.getinformation();
        this.position.setNextTurnColor(-this.position.getNextTurnColor());
        final Node n = new Node(this.number);
        this.positionNode.addchild(new TreeNode(n));
        n.main(this.positionNode);
        this.goforward();
        this.setlast();
        this.showinformation();
        this.copy();
        this.gameFrame.addComment(this.gameFrame.resourceString("Pass"));
        this.captured = 0;
    }

    public void placeaction(Node n, Action a, int c)
    // interpret a set move action, update the last move marker,
    // c being the color of the move.
    {
        int i, j;
        ListElement larg = a.arguments();
        while (larg != null) {
            final String s = (String) larg.content();
            i = Field.i(s);
            j = Field.j(s);
            if (this.valid(i, j)) {
                n.addchange(new Change(i, j, this.position.getColor(i, j), this.position.number(
                        i, j)));
                this.position.setFieldColor(i, j, c);
                this.update(i, j);
            }
            larg = larg.next();
        }
    }

    public void positionToNode(Node n)
    // copy the current position to a node.
    {
        n.setaction("AP", "UnaGo:" + this.gameFrame.version(), true);
        n.setaction("SZ", "" + this.boardSize, true);
        n.setaction("GM", "1", true);
        n.setaction("FF", this.gameFrame.getParameter("puresgf", false) ? "4" : "1",
                true);
        n.copyAction(this.sgfTree.top().node(), "GN");
        n.copyAction(this.sgfTree.top().node(), "DT");
        n.copyAction(this.sgfTree.top().node(), "PB");
        n.copyAction(this.sgfTree.top().node(), "BR");
        n.copyAction(this.sgfTree.top().node(), "PW");
        n.copyAction(this.sgfTree.top().node(), "WR");
        n.copyAction(this.sgfTree.top().node(), "PW");
        n.copyAction(this.sgfTree.top().node(), "US");
        n.copyAction(this.sgfTree.top().node(), "CP");
        int i, j;
        for (i = 0; i < this.boardSize; i++) {
            for (j = 0; j < this.boardSize; j++) {
                final String field = Field.string(i, j);
                switch (this.position.getColor(i, j)) {
                    case 1:
                        n.expandaction(new Action("AB", field));
                        break;
                    case -1:
                        n.expandaction(new Action("AW", field));
                        break;
                }
                if (this.position.marker(i, j) > 0) {
                    switch (this.position.marker(i, j)) {
                        case Field.SQUARE:
                            n.expandaction(new Action("SQ", field));
                            break;
                        case Field.TRIANGLE:
                            n.expandaction(new Action("TR", field));
                            break;
                        case Field.CIRCLE:
                            n.expandaction(new Action("CR", field));
                            break;
                        default:
                            n.expandaction(new MarkAction(field, this.gameFrame));
                    }
                } else if (this.position.haslabel(i, j)) {
                    n.expandaction(new Action("LB", field + ":"
                            + this.position.label(i, j)));
                } else if (this.position.letter(i, j) > 0) {
                    n.expandaction(new Action("LB", field + ":"
                            + this.position.letter(i, j)));
                }
            }
        }
    }

    public void print(Frame f)
    // print the board
    {
        final Position p = new Position(this.position);
        new PrintBoard(p, this.range, f);
    }

    public synchronized void remove(int i0, int j0)
    // completely remove a group there
    {
        final int s = this.state;
        this.varmaindown();
        this.state = s;
        if (this.position.getColor(i0, j0) == 0) {
            return;
        }
        Action a;
        this.position.markgroup(i0, j0);
        int i, j;
        this.position.getColor(i0, j0);
        Node n = this.positionNode.node();
        if (this.gameFrame.getParameter("puresgf", true)
                && (n.contains("B") || n.contains("W"))) {
            n = this.newnode();
        }
        for (i = 0; i < this.boardSize; i++) {
            for (j = 0; j < this.boardSize; j++) {
                if (this.position.marked(i, j)) {
                    a = new Action("AE", Field.string(i, j));
                    n.addchange(new Change(i, j, this.position.getColor(i, j), this.position
                            .number(i, j)));
                    n.expandaction(a);
                    if (this.position.getColor(i, j) > 0) {
                        n.Pb++;
                        this.Pb++;
                    } else {
                        n.Pw++;
                        this.Pw++;
                    }
                    this.position.setFieldColor(i, j, 0);
                    this.update(i, j);
                }
            }
        }
        this.copy();
    }

    public synchronized void removegame() {
        if (this.gameTrees.size() == 1) {
            return;
        }
        this.gameTrees.removeElementAt(this.currentTree);
        if (this.currentTree >= this.gameTrees.size()) {
            this.currentTree--;
        }
        this.sgfTree = (SGFTree) this.gameTrees.elementAt(this.currentTree);
        this.resettree();
        this.setnode();
        this.showinformation();
        this.copy();
    }

    public void removegroup(int i0, int j0)
    // completely remove a group (at end of game, before count)
    // note all removals
    {
        if (this.positionNode.haschildren()) {
            return;
        }
        if (this.position.getColor(i0, j0) == 0) {
            return;
        }
        Action a;
        this.position.markgroup(i0, j0);
        int i, j;
        this.position.getColor(i0, j0);
        Node n = this.positionNode.node();
        if (n.contains("B") || n.contains("W")) {
            n = this.newnode();
        }
        for (i = 0; i < this.boardSize; i++) {
            for (j = 0; j < this.boardSize; j++) {
                if (this.position.marked(i, j)) {
                    a = new Action("AE", Field.string(i, j));
                    n.addchange(new Change(i, j, this.position.getColor(i, j), this.position
                            .number(i, j)));
                    n.expandaction(a);
                    if (this.position.getColor(i, j) > 0) {
                        n.Pb++;
                        this.Pb++;
                    } else {
                        n.Pw++;
                        this.Pw++;
                    }
                    this.position.setFieldColor(i, j, 0);
                    this.update(i, j);
                }
            }
        }
        this.copy();
    }

    void removemouse(int i, int j)
    // remove a group at i,j
    {
        if (this.positionNode.haschildren()) {
            return;
        }
        this.removegroup(i, j);
        this.undonode();
        this.setnode();
        this.showinformation();
    }

    void resettree() {
        this.positionNode = this.sgfTree.top();
        this.position = new Position(this.boardSize);
        this.lastMoveX = this.lastMoveY = -1;
        this.Pb = this.Pw = 0;
        this.updateall();
        this.copy();
    }

    // ***** change the node at end of main tree ********
    // usually called by another board or server

    public void resume()
    // Resume playing after marking
    {
        this.getinformation();
        this.state = 1;
        this.showinformation();
    }

    public void save(PrintWriter o)
    // saves the file to the specified print stream
    // in SGF
    {
        this.getinformation();
        this.sgfTree.top().setaction("AP", "UnaGo:" + this.gameFrame.version(), true);
        this.sgfTree.top().setaction("SZ", "" + this.boardSize, true);
        this.sgfTree.top().setaction("GM", "1", true);
        this.sgfTree.top().setaction("FF",
                this.gameFrame.getParameter("puresgf", false) ? "4" : "1", true);
        for (int i = 0; i < this.gameTrees.size(); i++) {
            ((SGFTree) this.gameTrees.elementAt(i)).print(o);
        }
    }

    public void savePos(PrintWriter o)
    // saves the file to the specified print stream
    // in SGF
    {
        this.getinformation();
        final Node n = new Node(0);
        this.positionToNode(n);
        o.println("(");
        n.print(o);
        o.println(")");
    }

    public void saveXML(PrintWriter o, String encoding)
    // save the file in UnaGo's XML format
    {
        this.getinformation();
        this.sgfTree.top().setaction("AP", "UnaGo:" + this.gameFrame.version(), true);
        this.sgfTree.top().setaction("SZ", "" + this.boardSize, true);
        this.sgfTree.top().setaction("GM", "1", true);
        this.sgfTree.top().setaction("FF",
                this.gameFrame.getParameter("puresgf", false) ? "4" : "1", true);
        final XmlWriter xml = new XmlWriter(o);
        xml.printEncoding(encoding);
        xml.printXls("go.xsl");
        xml.printDoctype("Go", "go.dtd");
        xml.startTagNewLine("Go");
        for (int i = 0; i < this.gameTrees.size(); i++) {
            ((SGFTree) this.gameTrees.elementAt(i)).printXML(xml);
        }
        xml.endTagNewLine("Go");
    }

    public void saveXMLPos(PrintWriter o, String encoding)
    // save the file in UnaGo's XML format
    {
        this.getinformation();
        this.sgfTree.top().setaction("AP", "UnaGo:" + this.gameFrame.version(), true);
        this.sgfTree.top().setaction("SZ", "" + this.boardSize, true);
        this.sgfTree.top().setaction("GM", "1", true);
        this.sgfTree.top().setaction("FF",
                this.gameFrame.getParameter("puresgf", false) ? "4" : "1", true);
        final XmlWriter xml = new XmlWriter(o);
        xml.printEncoding(encoding);
        xml.printXls("go.xsl");
        xml.printDoctype("Go", "go.dtd");
        xml.startTagNewLine("Go");
        final Node n = new Node(0);
        this.positionToNode(n);
        final SGFTree t = new SGFTree(n);
        t.printXML(xml);
        xml.endTagNewLine("Go");
    }

    public boolean score()
    // board state is removing groups
    {
        if (this.positionNode.haschildren()) {
            return false;
        }
        this.getinformation();
        this.state = 8;
        this.isRemoving = true;
        this.showinformation();
        if (this.positionNode.node().main()) {
            return true;
        } else {
            return false;
        }
    }

    // ************ change the current node *****************

    /**
     * Search the string as substring of a comment, go to that node and report
     * success. On failure this routine will go up to the root node.
     */
    public boolean search(String s) {
        this.state = 1;
        this.getinformation();
        final TreeNode pos = this.positionNode;
        boolean found = true;
        outer:
        while (this.positionNode.node().getaction("C").indexOf(s) < 0
                || this.positionNode == pos) {
            if (!this.positionNode.haschildren()) {
                while (!this.hasvariation()) {
                    if (this.positionNode.parent() == null) {
                        found = false;
                        break outer;
                    } else {
                        this.goback();
                    }
                }
                this.tovarright();
            } else {
                this.goforward();
            }
        }
        this.showinformation();
        this.copy();
        return found;
    }

    public void set(int i, int j)
    // set a new move, if the board position is empty
    {
        Action a;
        synchronized (this.positionNode) {
            if (this.position.getColor(i, j) == 0) // empty?
            {
                if (this.positionNode.node().actions() != null
                        || this.positionNode.parentPos() == null)
                // create a new node, if there the current node is not
                // empty, else use the current node. exception is the first
                // move, where we always create a new move.
                {
                    final Node n = new Node(++this.number);
                    if (this.position.getNextTurnColor() > 0) {
                        a = new Action("B", Field.string(i, j));
                    } else {
                        a = new Action("W", Field.string(i, j));
                    }
                    n.addaction(a); // note the move action
                    this.setAction(n, a, this.position.getNextTurnColor()); // display the action
                    // !!! We alow for suicide moves
                    final TreeNode newpos = new TreeNode(n);
                    this.positionNode.addchild(newpos); // note the move
                    n.main(this.positionNode);
                    this.positionNode = newpos; // update current position pointer
                } else {
                    final Node n = this.positionNode.node();
                    if (this.position.getNextTurnColor() > 0) {
                        a = new Action("B", Field.string(i, j));
                    } else {
                        a = new Action("W", Field.string(i, j));
                    }
                    n.addaction(a); // note the move action
                    this.setAction(n, a, this.position.getNextTurnColor()); // display the action
                    // Crazy: how can you allow for suicide moves? You know Go?
                    // !!! We allow for suicide moves
                }
            }
        }
    }

    // *************** change the game tree ***********

    public void set(int i, int j, int c)
    // set a new stone, if the board position is empty
    // and we are on the last node.
    {
        if (this.positionNode.haschildren()) {
            return;
        }
        this.setc(i, j, c);
    }

    public void setAction(Node n, Action a, int c)
    // interpret a set move action, update the last move marker,
    // c being the color of the move.
    {
        final String s = (String) a.arguments().content();
        final int i = Field.i(s);
        final int j = Field.j(s);
        if (!this.valid(i, j)) {
            return;
        }
        n.addchange(new Change(i, j, this.position.getColor(i, j), this.position.number(i, j)));
        this.position.setFieldColor(i, j, c);
        this.position.number(i, j, n.number() - 1);
        this.showLastMove = false;
        this.update(this.lastMoveX, this.lastMoveY);
        this.showLastMove = true;
        this.lastMoveX = i;
        this.lastMoveY = j;
        this.update(i, j);
        this.position.setNextTurnColor(-c);
        // This is the only place to call capture method.
        this.capture(i, j, n);
    }

    public void setblack()
    // set black mode
    {
        this.getinformation();
        this.state = 3;
        this.showinformation();
    }

    // ********** set board state ******************

    public synchronized void setblack(int i, int j)
    // set a white stone at i,j at the end of the main tree
    {
        if (i < 0 || j < 0 || i >= this.boardSize || j >= this.boardSize) {
            return;
        }
        TreeNode p = this.sgfTree.top();
        while (p.haschildren()) {
            p = p.firstChild();
        }
        final Action a = new Action("AB", Field.string(i, j));
        Node n;
        if (p == this.sgfTree.top()) {
            TreeNode newpos;
            p.addchild(newpos = new TreeNode(1));
            if (this.positionNode == p) {
                this.positionNode = newpos;
            }
            p = newpos;
            p.main(true);
        }
        n = p.node();
        n.expandaction(a);
        if (this.positionNode == p) {
            n.addchange(new Change(i, j, this.position.getColor(i, j), this.position
                    .number(i, j)));
            this.position.setFieldColor(i, j, 1);
            this.update(i, j);
            this.copy();
        }
        this.mainColor = -1;
    }

    public void setc(int i, int j, int c) {
        synchronized (this.positionNode) {
            Action a;
            if (this.position.getColor(i, j) == 0) // empty?
            {
                Node n = this.positionNode.node();
                if (this.gameFrame.getParameter("puresgf", true)
                        && (n.contains("B") || n.contains("W"))) {
                    n = this.newnode();
                }
                n.addchange(new Change(i, j, 0));
                if (c > 0) {
                    a = new Action("AB", Field.string(i, j));
                } else {
                    a = new Action("AW", Field.string(i, j));
                }
                n.expandaction(a); // note the move action
                this.position.setFieldColor(i, j, c);
                this.update(i, j);
            }
        }
    }

    void setfonts()
    // get the font from the go frame
    {
        this.font = this.gameFrame.boardFont();
        this.fontMetrics = this.getFontMetrics(this.font);
    }

    public void setinformation(String black, String blackrank, String white,
                               String whiterank, String komi, String handicap)
    // set various things like names, rank etc.
    {
        this.sgfTree.top().setaction("PB", black, true);
        this.sgfTree.top().setaction("BR", blackrank, true);
        this.sgfTree.top().setaction("PW", white, true);
        this.sgfTree.top().setaction("WR", whiterank, true);
        this.sgfTree.top().setaction("KM", komi, true);
        this.sgfTree.top().setaction("HA", handicap, true);
        this.sgfTree.top().setaction("GN", white + "-" + black, true);
        this.sgfTree.top().setaction("DT", new Date().toString());
    }

    public void setlast()
    // update the last move marker applying all
    // set move actions in the node
    {
        final Node n = this.positionNode.node();
        ListElement l = n.actions();
        Action a;
        String s;
        int i = this.lastMoveX, j = this.lastMoveY;
        this.lastMoveX = -1;
        this.lastMoveY = -1;
        this.update(i, j);
        while (l != null) {
            a = (Action) l.content();
            if (a.getType().equals("B") || a.getType().equals("W")) {
                s = (String) a.arguments().content();
                i = Field.i(s);
                j = Field.j(s);
                if (this.valid(i, j)) {
                    this.lastMoveX = i;
                    this.lastMoveY = j;
                    this.update(this.lastMoveX, this.lastMoveY);
                    this.position.setNextTurnColor(-this.position.getColor(i, j));
                }
            }
            l = l.next();
        }
        this.number = n.number();
    }

    void setmouse(int i, int j, int c)
    // set a stone at i,j with specified color
    {
        this.set(i, j, c);
        this.undonode();
        this.setnode();
        this.showinformation();
    }

    void setmousec(int i, int j, int c)
    // set a stone at i,j with specified color
    {
        this.setc(i, j, c);
        this.undonode();
        this.setnode();
        this.showinformation();
    }

    void setname(String s)
    // set the name of the node
    {
        this.positionNode.setaction("N", s, true);
        this.showinformation();
    }

    public void setnode()
    // interpret all actions of a node
    {
        final Node n = this.positionNode.node();
        ListElement p = n.actions();
        if (p == null) {
            return;
        }
        Action a;
        while (p != null) {
            a = (Action) p.content();
            if (a.getType().equals("SZ")) {
                if (this.positionNode.parentPos() == null)
                // only at first node
                {
                    try {
                        final int ss = Integer.parseInt(a.argument().trim());
                        if (ss != this.boardSize) {
                            this.boardSize = ss;
                            this.position = new Position(this.boardSize);
                            this.makeimages();
                            this.updateall();
                            this.copy();
                        }
                    } catch (final NumberFormatException e) {
                    }
                }
            }
            p = p.next();
        }
        n.clearchanges();
        n.Pw = n.Pb = 0;
        p = n.actions();
        while (p != null) {
            a = (Action) p.content();
            if (a.getType().equals("B")) {
                this.setAction(n, a, 1);
            } else if (a.getType().equals("W")) {
                this.setAction(n, a, -1);
            }
            if (a.getType().equals("AB")) {
                this.placeaction(n, a, 1);
            }
            if (a.getType().equals("AW")) {
                this.placeaction(n, a, -1);
            } else if (a.getType().equals("AE")) {
                this.emptyaction(n, a);
            }
            p = p.next();
        }
    }

    public void setpass() {
        TreeNode p = this.sgfTree.top();
        while (p.haschildren()) {
            p = p.firstChild();
        }
        final Node n = new Node(this.number);
        p.addchild(new TreeNode(n));
        n.main(p);
        this.gameFrame.yourMove(this.positionNode != p);
        if (this.positionNode == p) {
            this.getinformation();
            final int c = this.position.getNextTurnColor();
            this.goforward();
            this.position.setNextTurnColor(-c);
            this.showinformation();
            this.gameFrame.addComment(this.gameFrame.resourceString("Pass"));
        }
        this.mainColor = -this.mainColor;
        this.captured = 0;
    }

    synchronized public void setsize(int s)
    // set the board size
    // clears the board !!!
    {
        if (s < 5 || s > 59) {
            return;
        }
        this.boardSize = s;
        this.position = new Position(this.boardSize);
        this.number = 1;
        final Node n = new Node(this.number);
        n.main(true);
        this.lastMoveX = this.lastMoveY = -1;
        this.sgfTree = new SGFTree(n);
        this.gameTrees.setElementAt(this.sgfTree, this.currentTree);
        this.positionNode = this.sgfTree.top();
        this.makeimages();
        this.showinformation();
        this.copy();
    }

    // ******** set board information **********

    void setVariationStyle(boolean hide, boolean current) {
        this.undonode();
        this.VHide = hide;
        this.VCurrent = current;
        this.setnode();
        this.updateall();
        this.copy();
    }

    public void setwhite()
    // set white mode
    {
        this.getinformation();
        this.state = 4;
        this.showinformation();
    }

    // ************ get board information ******

    public synchronized void setwhite(int i, int j)
    // set a white stone at i,j at the end of the main tree
    {
        if (i < 0 || j < 0 || i >= this.boardSize || j >= this.boardSize) {
            return;
        }
        TreeNode p = this.sgfTree.top();
        while (p.haschildren()) {
            p = p.firstChild();
        }
        final Action a = new Action("AW", Field.string(i, j));
        Node n;
        if (p == this.sgfTree.top()) {
            TreeNode newpos;
            p.addchild(newpos = new TreeNode(1));
            if (this.positionNode == p) {
                this.positionNode = newpos;
            }
            p = newpos;
            p.main(true);
        }
        n = p.node();
        n.expandaction(a);
        if (this.positionNode == p) {
            n.addchange(new Change(i, j, this.position.getColor(i, j), this.position
                    .number(i, j)));
            this.position.setFieldColor(i, j, -1);
            this.update(i, j);
            this.copy();
        }
        this.mainColor = 1;
    }

    public void showinformation()
    // update the label to display the next move and who's turn it is
    // and disable variation buttons
    // update the navigation buttons
    // update the comment
    {
        final Node n = this.positionNode.node();
        this.number = n.number();
        this.nodeName = n.getaction("N");
        String ms = "";
        if (n.main()) {
            if (!this.positionNode.haschildren()) {
                ms = "** ";
            } else {
                ms = "* ";
            }
        }
        switch (this.state) {
            case 3:
                this.lText = ms + this.gameFrame.resourceString("Set_black_stones");
                break;
            case 4:
                this.lText = ms + this.gameFrame.resourceString("Set_white_stones");
                break;
            case 5:
                this.lText = ms + this.gameFrame.resourceString("Mark_fields");
                break;
            case 6:
                this.lText = ms + this.gameFrame.resourceString("Place_letters");
                break;
            case 7:
                this.lText = ms + this.gameFrame.resourceString("Delete_stones");
                break;
            case 8:
                this.lText = ms + this.gameFrame.resourceString("Remove_prisoners");
                break;
            case 9:
                this.lText = ms + this.gameFrame.resourceString("Set_special_marker");
                break;
            case 10:
                this.lText = ms + this.gameFrame.resourceString("Text__")
                        + this.TextMarker;
                break;
            default:
                if (this.position.getNextTurnColor() > 0) {
                    final String s = this.lookuptime("BL");
                    if (!s.equals("")) {
                        this.lText = ms
                                + this.gameFrame.resourceString("Next_move__Black_")
                                + this.number + " (" + s + ")";
                    } else {
                        this.lText = ms
                                + this.gameFrame.resourceString("Next_move__Black_")
                                + this.number;
                    }
                } else {
                    final String s = this.lookuptime("WL");
                    if (!s.equals("")) {
                        this.lText = ms
                                + this.gameFrame.resourceString("Next_move__White_")
                                + this.number + " (" + s + ")";
                    } else {
                        this.lText = ms
                                + this.gameFrame.resourceString("Next_move__White_")
                                + this.number;
                    }
                }
        }
        this.lText = this.lText + " (" + this.siblings() + " "
                + this.gameFrame.resourceString("Siblings") + ", " + this.children()
                + " " + this.gameFrame.resourceString("Children") + ")";
        if (this.nodeName.equals("")) {
            this.gameFrame.setLabel(this.lText);
            this.doesDisplayNodeName = false;
        } else {
            this.gameFrame.setLabel(this.nodeName);
            this.doesDisplayNodeName = true;
        }
        this.gameFrame.setState(3, !n.main());
        this.gameFrame.setState(4, !n.main());
        this.gameFrame.setState(7, !n.main() || this.positionNode.haschildren());
        if (this.state == 1 || this.state == 2) {
            if (this.position.getNextTurnColor() == 1) {
                this.state = 1;
            } else {
                this.state = 2;
            }
        }
        this.gameFrame.setState(1, this.positionNode.parentPos() != null
                && this.positionNode.parentPos().firstChild() != this.positionNode);
        this.gameFrame.setState(2, this.positionNode.parentPos() != null
                && this.positionNode.parentPos().lastChild() != this.positionNode);
        this.gameFrame.setState(5, this.positionNode.haschildren());
        this.gameFrame.setState(6, this.positionNode.parentPos() != null);
        if (this.state != 9) {
            this.gameFrame.setState(this.state);
        } else {
            this.gameFrame.setMarkState(this.SpecialMarker);
        }
        int i, j;
        // delete all marks and variations
        for (i = 0; i < this.boardSize; i++) {
            for (j = 0; j < this.boardSize; j++) {
                if (this.position.tree(i, j) != null) {
                    this.position.tree(i, j, null);
                    this.update(i, j);
                }
                if (this.position.marker(i, j) != Field.NONE) {
                    this.position.marker(i, j, Field.NONE);
                    this.update(i, j);
                }
                if (this.position.letter(i, j) != 0) {
                    this.position.letter(i, j, 0);
                    this.update(i, j);
                }
                if (this.position.haslabel(i, j)) {
                    this.position.clearlabel(i, j);
                    this.update(i, j);
                }
            }
        }
        ListElement la = this.positionNode.node().actions();
        Action a;
        String s;
        String sc = "";
        int let = 1;
        while (la != null) // setup the marks and letters
        {
            a = (Action) la.content();
            if (a.getType().equals("C")) {
                sc = (String) a.arguments().content();
            } else if (a.getType().equals("SQ") || a.getType().equals("SL")) {
                ListElement larg = a.arguments();
                while (larg != null) {
                    s = (String) larg.content();
                    i = Field.i(s);
                    j = Field.j(s);
                    if (this.valid(i, j)) {
                        this.position.marker(i, j, Field.SQUARE);
                        this.update(i, j);
                    }
                    larg = larg.next();
                }
            } else if (a.getType().equals("MA") || a.getType().equals("M")
                    || a.getType().equals("TW") || a.getType().equals("TB")) {
                ListElement larg = a.arguments();
                while (larg != null) {
                    s = (String) larg.content();
                    i = Field.i(s);
                    j = Field.j(s);
                    if (this.valid(i, j)) {
                        this.position.marker(i, j, Field.CROSS);
                        this.update(i, j);
                    }
                    larg = larg.next();
                }
            } else if (a.getType().equals("TR")) {
                ListElement larg = a.arguments();
                while (larg != null) {
                    s = (String) larg.content();
                    i = Field.i(s);
                    j = Field.j(s);
                    if (this.valid(i, j)) {
                        this.position.marker(i, j, Field.TRIANGLE);
                        this.update(i, j);
                    }
                    larg = larg.next();
                }
            } else if (a.getType().equals("CR")) {
                ListElement larg = a.arguments();
                while (larg != null) {
                    s = (String) larg.content();
                    i = Field.i(s);
                    j = Field.j(s);
                    if (this.valid(i, j)) {
                        this.position.marker(i, j, Field.CIRCLE);
                        this.update(i, j);
                    }
                    larg = larg.next();
                }
            } else if (a.getType().equals("L")) {
                ListElement larg = a.arguments();
                while (larg != null) {
                    s = (String) larg.content();
                    i = Field.i(s);
                    j = Field.j(s);
                    if (this.valid(i, j)) {
                        this.position.letter(i, j, let);
                        let++;
                        this.update(i, j);
                    }
                    larg = larg.next();
                }
            } else if (a.getType().equals("LB")) {
                ListElement larg = a.arguments();
                while (larg != null) {
                    s = (String) larg.content();
                    i = Field.i(s);
                    j = Field.j(s);
                    if (this.valid(i, j) && s.length() >= 4
                            && s.charAt(2) == ':') {
                        this.position.setlabel(i, j, s.substring(3));
                        this.update(i, j);
                    }
                    larg = larg.next();
                }
            }
            la = la.next();
        }
        TreeNode p;
        ListElement l = null;
        if (this.VCurrent) {
            p = this.positionNode.parentPos();
            if (p != null) {
                l = p.firstChild().listelement();
            }
        } else if (this.positionNode.haschildren()
                && this.positionNode.firstChild() != this.positionNode.lastChild()) {
            l = this.positionNode.firstChild().listelement();
        }
        while (l != null) {
            p = (TreeNode) l.content();
            if (p != this.positionNode) {
                la = p.node().actions();
                while (la != null) {
                    a = (Action) la.content();
                    if (a.getType().equals("W") || a.getType().equals("B")) {
                        s = (String) a.arguments().content();
                        i = Field.i(s);
                        j = Field.j(s);
                        if (this.valid(i, j)) {
                            this.position.tree(i, j, p);
                            this.update(i, j);
                        }
                        break;
                    }
                    la = la.next();
                }
            }
            l = l.next();
        }
        if (!this.gameFrame.getComment().equals(sc)) {
            this.gameFrame.setComment(sc);
        }
        if (this.range >= 0 && !this.doesKeepRange) {
            this.clearrange();
        }
    }

    public int siblings() {
        ListElement l = this.positionNode.listelement();
        if (l == null) {
            return 0;
        }
        while (l.previous() != null) {
            l = l.previous();
        }
        int count = 0;
        while (l.next() != null) {
            l = l.next();
            count++;
        }
        return count;
    }

    // ***************** several other things ******

    public void specialmark(int i)
    // marking
    {
        this.getinformation();
        this.state = 9;
        this.SpecialMarker = i;
        this.showinformation();
    }

    public void specialmark(int i, int j)
    // Emphasize with the SpecialMarker
    {
        final Node n = this.positionNode.node();
        String s;
        switch (this.SpecialMarker) {
            case Field.SQUARE:
                s = "SQ";
                break;
            case Field.CIRCLE:
                s = "CR";
                break;
            case Field.TRIANGLE:
                s = "TR";
                break;
            default:
                s = "MA";
                break;
        }
        final Action a = new Action(s, Field.string(i, j));
        n.toggleaction(a);
        this.update(i, j);
    }

    public void stonespaint()
    // Create the (beauty) images of the stones (black and white)
    {
        final int col = this.gameFrame.boardColor().getRGB();
        final int blue = col & 0x0000FF, green = (col & 0x00FF00) >> 8, red = (col & 0xFF0000) >> 16;
        final boolean Alias = this.gameFrame.getParameter("alias", true);
        if (this.blackStone == null
                || this.blackStone.getWidth(this) != this.fieldWidth + 2) {
            final int d = this.fieldWidth + 2;
            final int pb[] = new int[d * d];
            final int pw[] = new int[d * d];
            int i, j, g, k;
            double di, dj;
            final double d2 = d / 2.0 - 5e-1;
            double r = d2 - 2e-1;
            final double f = Math.sqrt(3);
            double x, y, z, xr, xg, hh;
            k = 0;
            if (this.gameFrame.getParameter("smallerstones", false)) {
                r -= 1;
            }
            for (i = 0; i < d; i++) {
                for (j = 0; j < d; j++) {
                    di = i - d2;
                    dj = j - d2;
                    hh = r - Math.sqrt(di * di + dj * dj);
                    if (hh >= 0) {
                        z = r * r - di * di - dj * dj;
                        if (z > 0) {
                            z = Math.sqrt(z) * f;
                        } else {
                            z = 0;
                        }
                        x = di;
                        y = dj;
                        xr = Math.sqrt(6 * (x * x + y * y + z * z));
                        xr = (2 * z - x + y) / xr;
                        if (xr > 0.9) {
                            xg = (xr - 0.9) * 10;
                        } else {
                            xg = 0;
                        }
                        if (hh > this.pixel || !Alias) {
                            g = (int) (10 + 10 * xr + xg * 140);
                            pb[k] = 255 << 24 | g << 16 | g << 8 | g;
                            g = (int) (200 + 10 * xr + xg * 45);
                            pw[k] = 255 << 24 | g << 16 | g << 8 | g;
                        } else {
                            hh = (this.pixel - hh) / this.pixel;
                            g = (int) (10 + 10 * xr + xg * 140);
                            double shade;
                            if (di - dj < r / 3) {
                                shade = 1;
                            } else {
                                shade = this.shadow;
                            }
                            pb[k] = 255 << 24
                                    | (int) ((1 - hh) * g + hh * shade * red) << 16
                                    | (int) ((1 - hh) * g + hh * shade * green) << 8
                                    | (int) ((1 - hh) * g + hh * shade * blue);
                            g = (int) (200 + 10 * xr + xg * 45);
                            pw[k] = 255 << 24
                                    | (int) ((1 - hh) * g + hh * shade * red) << 16
                                    | (int) ((1 - hh) * g + hh * shade * green) << 8
                                    | (int) ((1 - hh) * g + hh * shade * blue);
                        }
                    } else {
                        pb[k] = pw[k] = 0;
                    }
                    k++;
                }
            }
            this.blackStone = this.createImage(new MemoryImageSource(d, d,
                    ColorModel.getRGBdefault(), pb, 0, d));
            this.whiteStone = this.createImage(new MemoryImageSource(d, d,
                    ColorModel.getRGBdefault(), pw, 0, d));
        }
    }

    public void territory(int i, int j) {
        this.mark(i, j);
        this.copy();
    }

    public void textmark(int i, int j) {
        final Action a = new Action("LB", Field.string(i, j) + ":"
                + this.TextMarker);
        this.positionNode.node().expandaction(a);
        this.update(i, j);
        this.gameFrame.advanceTextmark();
    }

    public void textmark(String s)
    // marking
    {
        this.getinformation();
        this.state = 10;
        this.TextMarker = s;
        this.showinformation();
    }

    public void tovarleft() {
        final ListElement l = this.positionNode.listelement();
        if (l == null) {
            return;
        }
        if (l.previous() == null) {
            return;
        }
        final TreeNode newpos = (TreeNode) l.previous().content();
        this.goback();
        this.positionNode = newpos;
        this.setnode();
    }

    public void tovarright() {
        final ListElement l = this.positionNode.listelement();
        if (l == null) {
            return;
        }
        if (l.next() == null) {
            return;
        }
        final TreeNode newpos = (TreeNode) l.next().content();
        this.goback();
        this.positionNode = newpos;
        this.setnode();
    }

    /**
     * Try to paint the wooden board. If the size is correct, use the predraw
     * board. Otherwise generate an EmptyPaint thread to paint a board.
     */
    public synchronized boolean trywood(Graphics gr, Graphics grs, int w) {
        if (EmptyPaint.haveImage(w, w,
                this.gameFrame.getColor("boardcolor", 170, 120, 70), this.pixelCoordinate + this.pixelCoordinate
                        / 2, this.pixelCoordinate - this.pixelCoordinate / 2, this.fieldWidth))
        // use predrawn image
        {
            gr.drawImage(EmptyPaint.StaticImage, this.offset + this.offsetAboveBelow - this.pixelCoordinate,
                    this.offset + this.offsetAboveBelow - this.pixelCoordinate, this);
            if (EmptyPaint.StaticShadowImage != null && grs != null) {
                grs.drawImage(EmptyPaint.StaticShadowImage, this.offset + this.offsetAboveBelow
                        - this.pixelCoordinate, this.offset + this.offsetAboveBelow - this.pixelCoordinate, this);
            }
            return true;
        } else {
            if (this.EPThread != null && this.EPThread.isAlive()) {
                this.EPThread.stopit();
            }
            this.EPThread = new EmptyPaint(this, w, w, this.gameFrame.getColor(
                    "boardcolor", 170, 120, 70), true, this.pixelCoordinate + this.pixelCoordinate / 2,
                    this.pixelCoordinate - this.pixelCoordinate / 2, this.fieldWidth);
        }
        return false;
    }

    public String twodigits(int n) {
        if (n < 10) {
            return "0" + n;
        } else {
            return "" + n;
        }
    }

    public void undo()
    // take back the last move, ask if necessary
    { // System.out.println("undo");
        if (this.positionNode.haschildren()
                || this.positionNode.parent() != null
                && this.positionNode.parent().lastchild() != this.positionNode.parent()
                .firstchild()
                && this.positionNode == this.positionNode.parent().firstchild()) {
            if (this.gameFrame.askUndo()) {
                this.doundo(this.positionNode);
            }
        } else {
            this.doundo(this.positionNode);
        }
    }

    public void undo(int n)
    // undo the n last moves, notify BoardInterface
    {
        this.varmaindown();
        for (int i = 0; i < n; i++) {
            this.goback();
            this.positionNode.removeall();
            this.showinformation();
            this.copy();
        }
        this.gameFrame.addComment(this.gameFrame.resourceString("Undo"));
    }

    public void undonode()
    // Undo everything that has been changed in the node.
    // (This will not correct the last move marker!)
    {
        final Node n = this.positionNode.node();
        this.clearrange();
        ListElement p = n.lastchange();
        while (p != null) {
            final Change c = (Change) p.content();
            this.position.setFieldColor(c.I, c.J, c.C);
            this.position.number(c.I, c.J, c.N);
            this.update(c.I, c.J);
            p = p.previous();
        }
        n.clearchanges();
        this.Pw -= n.Pw;
        this.Pb -= n.Pb;
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }

    public void update(int i, int j)
    // update the field (i,j) in the offscreen image Active
    // in dependance of the board position P.
    // display the last move mark, if applicable.
    {
        if (this.ActiveImage == null) {
            return;
        }
        if (i < 0 || j < 0) {
            return;
        }
        final Graphics g = this.ActiveImage.getGraphics();
        final int xi = this.offset + this.offsetAboveBelow + i * this.fieldWidth;
        final int xj = this.offset + this.offsetAboveBelow + j * this.fieldWidth;
        synchronized (this) {
            g.drawImage(this.Empty, xi, xj, xi + this.fieldWidth, xj + this.fieldWidth, xi, xj,
                    xi + this.fieldWidth, xj + this.fieldWidth, this);
            if (this.gameFrame.getParameter("shadows", true)
                    && this.gameFrame.getParameter("beauty", true)
                    && this.gameFrame.getParameter("beautystones", true)) {
                if (this.position.getColor(i, j) != 0) {
                    g.drawImage(this.EmptyShadow, xi - this.pixelCoordinate / 2, xj
                            + this.pixelCoordinate / 2, xi + this.fieldWidth - this.pixelCoordinate / 2, xj
                            + this.fieldWidth + this.pixelCoordinate / 2, xi - this.pixelCoordinate / 2, xj
                            + this.pixelCoordinate / 2, xi + this.fieldWidth - this.pixelCoordinate / 2, xj
                            + this.fieldWidth + this.pixelCoordinate / 2, this);
                } else {
                    g.drawImage(this.Empty, xi - this.pixelCoordinate / 2, xj + this.pixelCoordinate / 2,
                            xi + this.fieldWidth - this.pixelCoordinate / 2, xj + this.fieldWidth + this.pixelCoordinate
                                    / 2, xi - this.pixelCoordinate / 2, xj + this.pixelCoordinate / 2, xi
                                    + this.fieldWidth - this.pixelCoordinate / 2, xj + this.fieldWidth
                                    + this.pixelCoordinate / 2, this);
                }
                g.setClip(xi - this.pixelCoordinate / 2, xj + this.pixelCoordinate / 2, this.fieldWidth, this.fieldWidth);
                this.update1(g, i - 1, j);
                this.update1(g, i, j + 1);
                this.update1(g, i - 1, j + 1);
                g.setClip(xi, xj, this.fieldWidth, this.fieldWidth);
                if (i < this.boardSize - 1 && this.position.getColor(i + 1, j) != 0) {
                    g.drawImage(this.EmptyShadow, xi + this.fieldWidth - this.pixelCoordinate / 2, xj
                            + this.pixelCoordinate / 2, xi + this.fieldWidth, xj + this.fieldWidth, xi
                            + this.fieldWidth - this.pixelCoordinate / 2, xj + this.pixelCoordinate / 2, xi
                            + this.fieldWidth, xj + this.fieldWidth, this);
                }
                if (j > 0 && this.position.getColor(i, j - 1) != 0) {
                    g.drawImage(this.EmptyShadow, xi, xj, xi + this.fieldWidth - this.pixelCoordinate
                            / 2, xj + this.pixelCoordinate / 2, xi, xj, xi + this.fieldWidth
                            - this.pixelCoordinate / 2, xj + this.pixelCoordinate / 2, this);
                }
            }
        }
        g.setClip(xi, xj, this.fieldWidth, this.fieldWidth);
        this.update1(g, i, j);
        g.dispose();
    }

    void update1(Graphics g, int i, int j) {
        if (i < 0 || i >= this.boardSize || j < 0 || j >= this.boardSize) {
            return;
        }
        final char c[] = new char[1];
        final int xi = this.offset + this.offsetAboveBelow + i * this.fieldWidth;
        final int xj = this.offset + this.offsetAboveBelow + j * this.fieldWidth;
        if (this.position.getColor(i, j) > 0 || this.position.getColor(i, j) < 0
                && this.gameFrame.blackOnly()) {
            if (this.blackStone != null) {
                g.drawImage(this.blackStone, xi - 1, xj - 1, this);
            } else {
                g.setColor(this.gameFrame.blackColor());
                g.fillOval(xi + 1, xj + 1, this.fieldWidth - 2, this.fieldWidth - 2);
                g.setColor(this.gameFrame.blackSparkleColor());
                g.drawArc(xi + this.fieldWidth / 2, xj + this.fieldWidth / 4, this.fieldWidth / 4,
                        this.fieldWidth / 4, 40, 50);
            }
        } else if (this.position.getColor(i, j) < 0) {
            if (this.whiteStone != null) {
                g.drawImage(this.whiteStone, xi - 1, xj - 1, this);
            } else {
                g.setColor(this.gameFrame.whiteColor());
                g.fillOval(xi + 1, xj + 1, this.fieldWidth - 2, this.fieldWidth - 2);
                g.setColor(this.gameFrame.whiteSparkleColor());
                g.drawArc(xi + this.fieldWidth / 2, xj + this.fieldWidth / 4, this.fieldWidth / 4,
                        this.fieldWidth / 4, 40, 50);
            }
        }
        if (this.position.marker(i, j) != Field.NONE) {
            if (this.gameFrame.bwColor()) {
                if (this.position.getColor(i, j) >= 0) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(Color.black);
                }
            } else {
                g.setColor(this.gameFrame.markerColor(this.position.getColor(i, j)));
            }
            final int h = this.fieldWidth / 4;
            switch (this.position.marker(i, j)) {
                case Field.CIRCLE:
                    g.drawOval(xi + this.fieldWidth / 2 - h, xj + this.fieldWidth / 2 - h, 2 * h,
                            2 * h);
                    break;
                case Field.CROSS:
                    g.drawLine(xi + this.fieldWidth / 2 - h, xj + this.fieldWidth / 2 - h, xi
                            + this.fieldWidth / 2 + h, xj + this.fieldWidth / 2 + h);
                    g.drawLine(xi + this.fieldWidth / 2 + h, xj + this.fieldWidth / 2 - h, xi
                            + this.fieldWidth / 2 - h, xj + this.fieldWidth / 2 + h);
                    break;
                case Field.TRIANGLE:
                    g.drawLine(xi + this.fieldWidth / 2, xj + this.fieldWidth / 2 - h, xi
                            + this.fieldWidth / 2 - h, xj + this.fieldWidth / 2 + h);
                    g.drawLine(xi + this.fieldWidth / 2, xj + this.fieldWidth / 2 - h, xi
                            + this.fieldWidth / 2 + h, xj + this.fieldWidth / 2 + h);
                    g.drawLine(xi + this.fieldWidth / 2 - h, xj + this.fieldWidth / 2 + h, xi
                            + this.fieldWidth / 2 + h, xj + this.fieldWidth / 2 + h);
                    break;
                default:
                    g.drawRect(xi + this.fieldWidth / 2 - h, xj + this.fieldWidth / 2 - h, 2 * h,
                            2 * h);
            }
        }
        if (this.position.letter(i, j) != 0) {
            if (this.gameFrame.bwColor()) {
                if (this.position.getColor(i, j) >= 0) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(Color.black);
                }
            } else {
                g.setColor(this.gameFrame.labelColor(this.position.getColor(i, j)));
            }
            c[0] = (char) ('a' + this.position.letter(i, j) - 1);
            final String hs = new String(c);
            final int w = this.fontMetrics.stringWidth(hs) / 2;
            final int h = this.fontMetrics.getAscent() / 2 - 1;
            g.setFont(this.font);
            g.drawString(hs, xi + this.fieldWidth / 2 - w, xj + this.fieldWidth / 2 + h);
        } else if (this.position.haslabel(i, j)) {
            if (this.gameFrame.bwColor()) {
                if (this.position.getColor(i, j) >= 0) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(Color.black);
                }
            } else {
                g.setColor(this.gameFrame.labelColor(this.position.getColor(i, j)));
            }
            final String hs = this.position.label(i, j);
            final int w = this.fontMetrics.stringWidth(hs) / 2;
            final int h = this.fontMetrics.getAscent() / 2 - 1;
            g.setFont(this.font);
            g.drawString(hs, xi + this.fieldWidth / 2 - w, xj + this.fieldWidth / 2 + h);
        } else if (this.position.tree(i, j) != null && !this.VHide) {
            if (this.gameFrame.bwColor()) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.green);
            }
            g.drawLine(xi + this.fieldWidth / 2 - this.fieldWidth / 6, xj + this.fieldWidth / 2, xi
                    + this.fieldWidth / 2 + this.fieldWidth / 6, xj + this.fieldWidth / 2);
            g.drawLine(xi + this.fieldWidth / 2, xj + this.fieldWidth / 2 - this.fieldWidth / 6, xi
                    + this.fieldWidth / 2, xj + this.fieldWidth / 2 + this.fieldWidth / 6);
        }
        if (this.sendX == i && this.sendY == j) {
            if (this.gameFrame.bwColor()) {
                if (this.position.getColor(i, j) > 0) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(Color.black);
                }
            } else {
                g.setColor(Color.gray);
            }
            g.drawLine(xi + this.fieldWidth / 2 - 1, xj + this.fieldWidth / 2, xi + this.fieldWidth / 2
                    + 1, xj + this.fieldWidth / 2);
            g.drawLine(xi + this.fieldWidth / 2, xj + this.fieldWidth / 2 - 1, xi + this.fieldWidth / 2,
                    xj + this.fieldWidth / 2 + 1);
        }
        if (this.lastMoveX == i && this.lastMoveY == j && this.showLastMove) {
            if (this.gameFrame.lastNumber() || this.range >= 0
                    && this.position.number(i, j) > this.range) {
                if (this.position.getColor(i, j) > 0) {
                    g.setColor(Color.white);
                } else {
                    g.setColor(Color.black);
                }
                final String hs = "" + this.position.number(i, j) % 100;
                final int w = this.fontMetrics.stringWidth(hs) / 2;
                final int h = this.fontMetrics.getAscent() / 2 - 1;
                g.setFont(this.font);
                g.drawString(hs, xi + this.fieldWidth / 2 - w, xj + this.fieldWidth / 2 + h);
            } else {
                if (this.gameFrame.bwColor()) {
                    if (this.position.getColor(i, j) > 0) {
                        g.setColor(Color.white);
                    } else {
                        g.setColor(Color.black);
                    }
                } else {
                    g.setColor(Color.red);
                }
                g.drawLine(xi + this.fieldWidth / 2 - this.fieldWidth / 6, xj + this.fieldWidth / 2, xi
                        + this.fieldWidth / 2 + this.fieldWidth / 6, xj + this.fieldWidth / 2);
                g.drawLine(xi + this.fieldWidth / 2, xj + this.fieldWidth / 2 - this.fieldWidth / 6, xi
                        + this.fieldWidth / 2, xj + this.fieldWidth / 2 + this.fieldWidth / 6);
            }
        } else if (this.position.getColor(i, j) != 0 && this.range >= 0
                && this.position.number(i, j) > this.range) {
            if (this.position.getColor(i, j) > 0) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.black);
            }
            final String hs = "" + this.position.number(i, j) % 100;
            final int w = this.fontMetrics.stringWidth(hs) / 2;
            final int h = this.fontMetrics.getAscent() / 2 - 1;
            g.setFont(this.font);
            g.drawString(hs, xi + this.fieldWidth / 2 - w, xj + this.fieldWidth / 2 + h);
        }
    }

    public void updateall()
    // update all of the board
    {
        if (this.ActiveImage == null) {
            return;
        }
        synchronized (this) {
            this.ActiveImage.getGraphics().drawImage(this.Empty, 0, 0, this);
        }
        int i, j;
        for (i = 0; i < this.boardSize; i++) {
            for (j = 0; j < this.boardSize; j++) {
                this.update(i, j);
            }
        }
        this.showinformation();
    }

    public void updateboard()
    // redraw the board and its background
    {
        this.blackStone = this.whiteStone = null;
        this.EmptyShadow = null;
        this.setfonts();
        this.makeimages();
        this.updateall();
        this.copy();
    }

    boolean valid(int i, int j) {
        return i >= 0 && i < this.boardSize && j >= 0 && j < this.boardSize;
    }

    public void variation(int i, int j) {
        if (this.positionNode.parentPos() == null) {
            return;
        }
        if (this.position.getColor(i, j) == 0) // empty?
        {
            final int c = this.position.getNextTurnColor();
            this.goback();
            this.position.setNextTurnColor(-c);
            this.set(i, j);
            if (!this.gameFrame.getParameter("variationnumbers", false)) {
                this.position.number(i, j, 1);
                this.number = 2;
                this.positionNode.node().number(2);
            }
            this.update(i, j);
        }
    }

    // *****************************************************
    // procedures that might be overloaded for more control
    // (Callback to server etc.)
    // *****************************************************

    public synchronized void varleft()
    // one variation to the left
    {
        this.state = 1;
        this.getinformation();
        final ListElement l = this.positionNode.listelement();
        if (l == null) {
            return;
        }
        if (l.previous() == null) {
            return;
        }
        final TreeNode newpos = (TreeNode) l.previous().content();
        this.goback();
        this.positionNode = newpos;
        this.setnode();
        this.showinformation();
        this.copy();
    }

    public synchronized void varmain()
    // to the main variation
    {
        this.state = 1;
        this.getinformation();
        while (this.positionNode.parentPos() != null && !this.positionNode.node().main()) {
            this.goback();
        }
        if (this.positionNode.haschildren()) {
            this.goforward();
        }
        this.showinformation();
        this.copy();
    }

    public synchronized void varmaindown()
    // to end of main variation
    {
        this.state = 1;
        this.getinformation();
        while (this.positionNode.parentPos() != null && !this.positionNode.node().main()) {
            this.goback();
        }
        while (this.positionNode.haschildren()) {
            this.goforward();
        }
        this.showinformation();
        this.copy();
    }

    public synchronized void varright()
    // one variation to the right
    {
        this.state = 1;
        this.getinformation();
        final ListElement l = this.positionNode.listelement();
        if (l == null) {
            return;
        }
        if (l.next() == null) {
            return;
        }
        final TreeNode newpos = (TreeNode) l.next().content();
        this.goback();
        this.positionNode = newpos;
        this.setnode();
        this.showinformation();
        this.copy();
    }

    public synchronized void varup()
    // to the start of the variation
    {
        this.state = 1;
        this.getinformation();
        if (this.positionNode.parentPos() != null) {
            this.goback();
        }
        while (this.positionNode.parentPos() != null
                && this.positionNode.parentPos().firstChild() == this.positionNode.parentPos()
                .lastChild() && !this.positionNode.node().main()) {
            this.goback();
        }
        this.showinformation();
        this.copy();
    }

    public void white()
    // white to play
    {
        this.getinformation();
        this.state = 2;
        this.position.setNextTurnColor(-1);
        this.showinformation();
    }

    public synchronized void white(int i, int j)
    // black move at i,j at the end of the main tree
    {
        if (i < 0 || j < 0 || i >= this.boardSize || j >= this.boardSize) {
            return;
        }
        TreeNode p = this.sgfTree.top();
        while (p.haschildren()) {
            p = p.firstChild();
        }
        final Action a = new Action("W", Field.string(i, j));
        final Node n = new Node(p.node().number() + 1);
        n.addaction(a);
        p.addchild(new TreeNode(n));
        n.main(p);
        this.gameFrame.yourMove(this.positionNode != p);
        if (this.positionNode == p) {
            this.forward();
        }
        this.mainColor = 1;
    }
}
