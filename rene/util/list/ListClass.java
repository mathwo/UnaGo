package rene.util.list;

/**
 * A class for a list of things. The list is forward and backward chained.
 *
 * @see rene.list.ListElement
 */

public class ListClass {
    ListElement First, Last; // Pointer to start and end of list.

    /**
     * Generate an empty list.
     */
    public ListClass() {
        this.First = null;
        this.Last = null;
    }

    /**
     * Append a node to the list
     */
    public void append(ListElement l) {
        if (this.Last == null) {
            this.init(l);
        } else {
            this.Last.next(l);
            l.previous(this.Last);
            this.Last = l;
            l.next(null);
            l.list(this);
        }
    }

    /**
     * @return First ListElement.
     */
    public ListElement first() {
        return this.First;
    }

    /**
     * initialize the list with a single element.
     */
    public void init(ListElement l) {
        this.Last = this.First = l;
        l.previous(null);
        l.next(null);
        l.list(this);
    }

    /*
     * @param l ListElement to be inserted.
     *
     * @param after If null, it works like prepend.
     */
    public void insert(ListElement l, ListElement after) {
        if (after == this.Last) {
            this.append(l);
        } else if (after == null) {
            this.prepend(l);
        } else {
            after.next().previous(l);
            l.next(after.next());
            after.next(l);
            l.previous(after);
            l.list(this);
        }
    }

    /**
     * @return Last ListElement.
     */
    public ListElement last() {
        return this.Last;
    }

    public void prepend(ListElement l)
    // prepend a node to the list
    {
        if (this.First == null) {
            this.init(l);
        } else {
            this.First.previous(l);
            l.next(this.First);
            this.First = l;
            l.previous(null);
            l.list(this);
        }
    }

    /**
     * Remove a node from the list. The node really should be in the list, which
     * is not checked.
     */
    public void remove(ListElement l) {
        if (this.First == l) {
            this.First = l.next();
            if (this.First != null) {
                this.First.previous(null);
            } else {
                this.Last = null;
            }
        } else if (this.Last == l) {
            this.Last = l.previous();
            if (this.Last != null) {
                this.Last.next(null);
            } else {
                this.First = null;
            }
        } else {
            l.previous().next(l.next());
            l.next().previous(l.previous());
        }
        l.next(null);
        l.previous(null);
        l.list(null);
    }

    /**
     * remove everything after e
     */
    public void removeAfter(ListElement e) {
        e.next(null);
        this.Last = e;
    }

    /**
     * Empty the list.
     */
    public void removeall() {
        this.First = null;
        this.Last = null;
    }

    /**
     * Prints the class
     */
    @Override
    public String toString() {
        ListElement e = this.First;
        String s = "";
        while (e != null) {
            s = s + e.content().toString() + ", ";
            e = e.next();
        }
        return s;
    }

}
