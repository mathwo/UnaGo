package unagoclient.board;

/**
 * Holds position changes at one field.
 */

public class Change {
    public int I, J, C;
    public int N;

    public Change(int i, int j, int c) {
        this(i, j, c, 0);
    }

    /**
     * Board position i,j changed from c.
     */
    public Change(int i, int j, int c, int n) {
        this.I = i;
        this.J = j;
        this.C = c;
        this.N = n;
    }
}
