package unagoclient.board;

/**
 * Store a complete game position. Contains methods for group determination and
 * liberties.
 */

public class Position {
    private int size; // size (9,11,13 or 19)
    private int nextTurnColor; // next turn (1 is black, -1 is white)
    private Field[][] fields; // the board

    /**
     * Initialize fields with an empty board, and set the next turn to black.
     */
    public Position(int size) {
        this.size = size;
        this.fields = new Field[this.size][this.size];
        int i, j;
        for (i = 0; i < this.size; i++) {
            for (j = 0; j < this.size; j++) {
                this.fields[i][j] = new Field();
            }
        }
        this.nextTurnColor = 1;
    }

    /**
     * Initialize fields with an empty board, and set the next turn to black.
     */
    public Position(Position P) {
        this.size = P.size;
        this.fields = new Field[this.size][this.size];
        int i, j;
        for (i = 0; i < this.size; i++) {
            for (j = 0; j < this.size; j++) {
                this.fields[i][j] = new Field();
            }
        }
        for (i = 0; i < this.size; i++) {
            for (j = 0; j < this.size; j++) {
                this.setFieldColor(i, j, P.getColor(i, j));
                this.number(i, j, P.number(i, j));
                this.marker(i, j, P.marker(i, j));
                this.letter(i, j, P.letter(i, j));
                if (P.haslabel(i, j)) {
                    this.setlabel(i, j, P.label(i, j));
                }
            }
        }
        this.setNextTurnColor(P.getNextTurnColor());
    }

    void clearlabel(int i, int j) {
        this.fields[i][j].clearlabel();
    }

    int getNextTurnColor() {
        return this.nextTurnColor;
    }

    void setNextTurnColor(int c) {
        this.nextTurnColor = c;
    }

    // Interface routines to set or ask a field:
    int getColor(int i, int j) {
        return this.fields[i][j].getColor();
    }

    void setFieldColor(int i, int j, int c) {
        this.fields[i][j].setColor(c);
    }

    public int count(int i, int j) {
        this.unmarkall();
        this.markgroup(i, j);
        int count = 0;
        for (i = 0; i < this.size; i++) {
            for (j = 0; j < this.size; j++) {
                if (this.fields[i][j].mark()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Find all B and W territory. Sets the territory flags to 0, 1 or -1. -2 is
     * an intermediate state for unchecked points.
     */
    public void getterritory() {
        int i, j, ii, jj;
        for (i = 0; i < this.size; i++) {
            for (j = 0; j < this.size; j++) {
                this.fields[i][j].territory(-2);
            }
        }
        for (i = 0; i < this.size; i++) {
            for (j = 0; j < this.size; j++) {
                if (this.fields[i][j].getColor() == 0) {
                    if (this.fields[i][j].territory() == -2) {
                        if (!this.markgrouptest(i, j, 1)) {
                            for (ii = 0; ii < this.size; ii++) {
                                for (jj = 0; jj < this.size; jj++) {
                                    if (this.fields[ii][jj].mark()) {
                                        this.fields[ii][jj].territory(-1);
                                    }
                                }
                            }
                        } else if (!this.markgrouptest(i, j, -1)) {
                            for (ii = 0; ii < this.size; ii++) {
                                for (jj = 0; jj < this.size; jj++) {
                                    if (this.fields[ii][jj].mark()) {
                                        this.fields[ii][jj].territory(1);
                                    }
                                }
                            }
                        } else {
                            this.markgroup(i, j);
                            for (ii = 0; ii < this.size; ii++) {
                                for (jj = 0; jj < this.size; jj++) {
                                    if (this.fields[ii][jj].mark()) {
                                        this.fields[ii][jj].territory(0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    boolean haslabel(int i, int j) {
        return this.fields[i][j].havelabel();
    }

    String label(int i, int j) {
        return this.fields[i][j].label();
    }

    int letter(int i, int j) {
        return this.fields[i][j].letter();
    }

    void letter(int i, int j, int l) {
        this.fields[i][j].letter(l);
    }

    // Interface to determine field marks.
    boolean marked(int i, int j) {
        return this.fields[i][j].mark();
    }

    int marker(int i, int j) {
        return this.fields[i][j].marker();
    }

    void marker(int i, int j, int f) {
        this.fields[i][j].marker(f);
    }

    /**
     * This method clears all other marks, then mark a group starting from
     * point (i, j).
     */
    public void markgroup(int n, int m) {
        this.unmarkall();
        // recursively do the marking
        this.markrek(n, m, this.fields[n][m].getColor());
    }

    /**
     * Test if the group at (n,m) has a neighbor of state ct. If yes, mark all
     * elements of the group. Else return false.
     */
    public boolean markgrouptest(int n, int m, int ct) {
        this.unmarkall();
        return this.markrektest(n, m, this.fields[n][m].getColor(), ct);
    }

    /**
     * This method mark all the points of the same color as the starting
     * point(i, j).
     * The third parameter is for recursive usage.
     *
     * @param i
     * @param j
     * @param c
     */
    void markrek(int i, int j, int c) {
        if (this.fields[i][j].mark() || this.fields[i][j].getColor() != c) {
            return;
        }
        this.fields[i][j].mark(true);
        if (i > 0) {
            this.markrek(i - 1, j, c);
        }
        if (j > 0) {
            this.markrek(i, j - 1, c);
        }
        if (i < this.size - 1) {
            this.markrek(i + 1, j, c);
        }
        if (j < this.size - 1) {
            this.markrek(i, j + 1, c);
        }
    }

    /**
     * Recursively mark a group of state c starting from (i,j) with the main
     * goal to determine, if there is a neighbor of state ct to this group. If
     * yes abandon the mark and return true.
     */
    boolean markrektest(int i, int j, int c, int ct) {
        // To mark a point is to make sure recursive calls will not
        // infinitely repeat on same points.
        if (this.fields[i][j].mark()) {
            return false;
        }
        if (this.fields[i][j].getColor() != c) {
            if (this.fields[i][j].getColor() == ct) {
                // This means if current point is of state ct, and ct != c,
                // this method would return true:
                return true;
            } else {
                return false;
            }
        }
        this.fields[i][j].mark(true);
        if (i > 0) {
            if (this.markrektest(i - 1, j, c, ct)) {
                return true;
            }
        }
        if (j > 0) {
            if (this.markrektest(i, j - 1, c, ct)) {
                return true;
            }
        }
        if (i < this.size - 1) {
            if (this.markrektest(i + 1, j, c, ct)) {
                return true;
            }
        }
        if (j < this.size - 1) {
            if (this.markrektest(i, j + 1, c, ct)) {
                return true;
            }
        }
        return false;
    }

    int number(int i, int j) {
        return this.fields[i][j].number();
    }

    void number(int i, int j, int n) {
        this.fields[i][j].number(n);
    }

    void setlabel(int i, int j, String s) {
        this.fields[i][j].setlabel(s);
    }

    int getSize() {
        return this.size;
    }

    void setSize(int size) {
        this.size = size;
    }

    int territory(int i, int j) {
        return this.fields[i][j].territory();
    }

    // Interfact to variation trees
    TreeNode tree(int i, int j) {
        return this.fields[i][j].tree();
    }

    void tree(int i, int j, TreeNode t) {
        this.fields[i][j].tree(t);
    }

    /**
     * cancel all markings
     */
    public void unmarkall() {
        int i, j;
        for (i = 0; i < this.size; i++) {
            for (j = 0; j < this.size; j++) {
                this.fields[i][j].mark(false);
            }
        }
    }
}
