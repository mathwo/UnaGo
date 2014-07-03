package rene.util.regexp;

/**
 * This is to mark the found ranges.
 */

public class PositionRange {
    int Start, End;

    public PositionRange(int start, int end) {
        this.Start = start;
        this.End = end;
    }

    public int end() {
        return this.End;
    }

    public int start() {
        return this.Start;
    }
}
