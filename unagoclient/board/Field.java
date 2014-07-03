package unagoclient.board;

// ************** Field **************

/**
 * A class to hold a single field in the game board. Contains data for labels,
 * numbers, marks etc. and of course the color of the stone on the board.
 * <p>
 * It may contain a reference to a tree, which is a variation starting at this
 * (empty) board position.
 * <p>
 * The Mark field is used for several purposes, like marking a group of stones
 * or a territory.
 */

public class Field {
    /**
     * return a string containing the coordinates in SGF
     */
    static String coordinate(int i, int j, int s) {
        if (s > 25) {
            return (i + 1) + "," + (s - j);
        } else {
            if (i >= 8) {
                i++;
            }
            return "" + (char) ('A' + i) + (s - j);
        }
    }

    /**
     * get the first coordinate from the SGF string
     */
    static int i(String s) {
        if (s.length() < 2) {
            return -1;
        }
        final char c = s.charAt(0);
        if (c < 'a') {
            return c - 'A' + Field.az + 1;
        }
        return c - 'a';
    }

    /**
     * get the second coordinate from the SGF string
     */
    static int j(String s) {
        if (s.length() < 2) {
            return -1;
        }
        final char c = s.charAt(1);
        if (c < 'a') {
            return c - 'A' + Field.az + 1;
        }
        return c - 'a';
    }

    /**
     * return a string containing the coordinates in SGF
     */
    static String string(int i, int j) {
        final char[] a = new char[2];
        if (i >= Field.az) {
            a[0] = (char) ('A' + i - Field.az - 1);
        } else {
            a[0] = (char) ('a' + i);
        }
        if (j >= Field.az) {
            a[1] = (char) ('A' + j - Field.az - 1);
        } else {
            a[1] = (char) ('a' + j);
        }
        return new String(a);
    }

    // the state of the field (-1, 0, 1). 1 is Black.
    int color;

    boolean Mark; // for several purposes (see
    // Position.markgroup)
    TreeNode T; // Tree that starts this variation
    int Letter; // Letter to be displayed
    String LabelLetter; // Strings from the LB tag.
    boolean HaveLabel; // flag to indicate there is a label
    int Territory; // For Territory counting
    int Marker; // emphasized field
    static final int NONE = 0;
    static final int CROSS = 1;

    static final int SQUARE = 2;

    static final int TRIANGLE = 3;

    static final int CIRCLE = 4;

    int Number;

    final static int az = 'z' - 'a';

    // ** set the field to 0 initially */
    public Field() {
        this.color = 0;
        this.T = null;
        this.Letter = 0;
        this.HaveLabel = false;
        this.Number = 0;
    }

    void clearlabel() {
        this.HaveLabel = false;
    }

    /**
     * return its state
     */
    int getColor() {
        return this.color;
    }

    /**
     * set its state
     */
    void setColor(int c) {
        this.color = c;
        this.Number = 0;
    }

    boolean havelabel() {
        return this.HaveLabel;
    }

    String label() {
        return this.LabelLetter;
    }

    int letter() {
        return this.Letter;
    }

    void letter(int l) {
        this.Letter = l;
    }

    // access functions:
    boolean mark() {
        return this.Mark;
    } // ask Mark

    // modifiers:
    void mark(boolean f) {
        this.Mark = f;
    } // set Mark

    int marker() {
        return this.Marker;
    }

    void marker(int f) {
        this.Marker = f;
    }

    int number() {
        return this.Number;
    }

    void number(int n) {
        this.Number = n;
    }

    void setlabel(String s) {
        this.HaveLabel = true;
        this.LabelLetter = s;
    }

    int territory() {
        return this.Territory;
    }

    void territory(int c) {
        this.Territory = c;
    }

    TreeNode tree() {
        return this.T;
    }

    void tree(TreeNode t) {
        this.T = t;
    } // set Tree
}
