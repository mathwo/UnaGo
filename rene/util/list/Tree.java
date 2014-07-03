package rene.util.list;

/**
 * A node with a list of children trees.
 */

public class Tree {
    ListClass Children; // list of children, each with Tree as content
    Object Content; // content
    ListElement Le; // the listelement containing the tree
    Tree Parent; // the parent tree

    /**
     * initialize with an object and no children
     */
    public Tree(Object o) {
        this.Content = o;
        this.Children = new ListClass();
        this.Le = null;
        this.Parent = null;
    }

    /**
     * add a child tree
     */
    public void addchild(Tree t) {
        final ListElement p = new ListElement(t);
        this.Children.append(p);
        t.Le = p;
        t.Parent = this;
    }

    public ListClass children() {
        return this.Children;
    }

    public Object content() {
        return this.Content;
    }

    public void content(Object o) {
        this.Content = o;
    }

    public Tree firstchild() {
        return (Tree) this.Children.first().content();
    }

    // Access Methods:
    public boolean haschildren() {
        return this.Children.first() != null;
    }

    /**
     * insert a child tree
     */
    public void insertchild(Tree t) {
        if (!this.haschildren()) // simple case
        {
            this.addchild(t);
            return;
        }
        // give t my children
        t.Children = this.Children;
        // make t my only child
        this.Children = new ListClass();
        final ListElement p = new ListElement(t);
        this.Children.append(p);
        t.Le = p;
        t.Parent = this;
        // fix the parents of all grandchildren
        ListElement le = t.Children.first();
        while (le != null) {
            final Tree h = (Tree) (le.content());
            h.Parent = t;
            le = le.next();
        }
    }

    public Tree lastchild() {
        return (Tree) this.Children.last().content();
    }

    public ListElement listelement() {
        return this.Le;
    }

    public Tree parent() {
        return this.Parent;
    }

    /**
     * remove the specific child tree (must be in the tree!!!)
     */
    public void remove(Tree t) {
        if (t.parent() != this) {
            return;
        }
        this.Children.remove(t.Le);
    }

    /**
     * remove all children
     */
    public void removeall() {
        this.Children.removeall();
    }
}
