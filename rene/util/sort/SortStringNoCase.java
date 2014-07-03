package rene.util.sort;

public class SortStringNoCase extends SortString {
    String Orig;

    public SortStringNoCase(String s) {
        super(s.toLowerCase());
        this.Orig = s;
    }

    @Override
    public String toString() {
        return this.Orig;
    }
}
