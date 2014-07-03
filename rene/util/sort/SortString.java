package rene.util.sort;

public class SortString implements SortObject {
    String S;

    public SortString(String s) {
        this.S = s;
    }

    @Override
    public int compare(SortObject o) {
        final SortString s = (SortString) o;
        return this.S.compareTo(s.S);
    }

    @Override
    public String toString() {
        return this.S;
    }
}
