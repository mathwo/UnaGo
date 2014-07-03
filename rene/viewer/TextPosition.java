package rene.viewer;

import rene.util.list.ListElement;

class TextPosition {
    ListElement L;
    int LCount;
    int LPos;

    public TextPosition(ListElement l, int lcount, int lpos) {
        this.L = l;
        this.LCount = lcount;
        this.LPos = lpos;
    }

    boolean before(TextPosition p) {
        return p.LCount > this.LCount
                || (p.LCount == this.LCount && p.LPos > this.LPos);
    }

    boolean equal(TextPosition p) {
        return p.LCount == this.LCount && p.LPos == this.LPos;
    }

    void oneleft() {
        if (this.LPos > 0) {
            this.LPos--;
        }
    }
}
