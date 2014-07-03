package unagoclient.board;

import rene.util.list.ListClass;
import rene.util.list.ListElement;
import rene.util.list.Tree;
import rene.util.xml.XmlWriter;

import java.io.PrintWriter;

/**
 * A node has
 * <UL>
 * <LI>a list of actions and a number counter (the number is the number of the
 * next expected move in the game tree),
 * <LI>a flag, if the node is in the main game tree,
 * <LI>a list of changes in this node to be able to undo the node,
 * <LI>the changes in the prisoner count in this node.
 * </UL>
 *
 * @see unagoclient.board.Action
 * @see unagoclient.board.Change
 */

class Node {
    ListClass Actions; // actions and variations
    int N; // next exptected number
    boolean Main; // belongs to main variation
    ListClass Changes;
    public int Pw, Pb; // changes in prisoners in this node

    /**
     * initialize with the expected number
     */
    public Node(int n) {
        this.Actions = new ListClass();
        this.N = n;
        this.Main = false;
        this.Changes = new ListClass();
        this.Pw = this.Pb = 0;
    }

    // access methods:
    public ListElement actions() {
        return this.Actions.first();
    }

    /**
     * add an action (at end)
     */
    public void addaction(Action a) {
        this.Actions.append(new ListElement(a));
    }

    /**
     * add a new change to this node
     */
    public void addchange(Change c) {
        this.Changes.append(new ListElement(c));
    }

    public ListElement changes() {
        return this.Changes.first();
    }

    /**
     * clear the list of changes
     */
    public void clearchanges() {
        this.Changes.removeall();
    }

    /**
     * see if the list contains an action of type s
     */
    public boolean contains(String s) {
        return this.find(s) != null;
    }

    /**
     * find the action and a specified tag
     */
    public boolean contains(String s, String argument) {
        final ListElement p = this.find(s);
        if (p == null) {
            return false;
        }
        final Action a = (Action) p.content();
        return a.contains(argument);
    }

    /**
     * Copy an action from another node.
     */
    public void copyAction(Node n, String action) {
        if (n.contains(action)) {
            this.expandaction(new Action(action, n.getaction(action)));
        }
    }

    /**
     * expand an action of the same type as a, else generate a new action
     */
    public void expandaction(Action a) {
        final ListElement p = this.find(a.getType());
        if (p == null) {
            this.addaction(a);
        } else {
            final Action pa = (Action) p.content();
            pa.addargument(a.argument());
        }
    }

    /**
     * find the list element containing the action of type s
     */
    ListElement find(String s) {
        ListElement p = this.Actions.first();
        while (p != null) {
            final Action a = (Action) p.content();
            if (a.getType().equals(s)) {
                return p;
            }
            p = p.next();
        }
        return null;
    }

    /**
     * get the argument of this action (or "")
     */
    public String getaction(String type) {
        ListElement l = this.Actions.first();
        while (l != null) {
            final Action a = (Action) l.content();
            if (a.getType().equals(type)) {
                final ListElement la = a.arguments();
                if (la != null) {
                    return (String) la.content();
                } else {
                    return "";
                }
            }
            l = l.next();
        }
        return "";
    }

    /**
     * Insert an action after p. p <b>must</b> have content type action.
     */
    public void insertaction(Action a, ListElement p) {
        this.Actions.insert(new ListElement(a), p);
    }

    public ListElement lastaction() {
        return this.Actions.last();
    }

    public ListElement lastchange() {
        return this.Changes.last();
    }

    public boolean main() {
        return this.Main;
    }

    // modification methods:
    public void main(boolean m) {
        this.Main = m;
    }

    /**
     * Set the Main flag
     *
     * @param p is the tree, which contains this node on root.
     */
    public void main(Tree p) {
        this.Main = false;
        try {
            if (((Node) p.content()).main()) {
                this.Main = (this == ((Node) p.firstchild().content()));
            } else if (p.parent() == null) {
                this.Main = true;
            }
        } catch (final Exception e) {
        }
    }

    public int number() {
        return this.N;
    }

    public void number(int n) {
        this.N = n;
    }

    /**
     * add an action (at front)
     */
    public void prependaction(Action a) {
        this.Actions.prepend(new ListElement(a));
    }

    /**
     * Print the node in SGF.
     *
     * @see unagoclient.board.Action#print
     */
    public void print(PrintWriter o) {
        o.print(";");
        ListElement p = this.Actions.first();
        Action a;
        while (p != null) {
            a = (Action) p.content();
            a.print(o);
            p = p.next();
        }
        o.println("");
    }

    public void print(XmlWriter xml, int size) {
        int count = 0;
        Action ra = null, a;
        ListElement p = this.Actions.first();
        while (p != null) {
            a = (Action) p.content();
            if (a.isRelevant()) {
                count++;
                ra = a;
            }
            p = p.next();
        }
        if (count == 0 && !this.contains("C")) {
            xml.finishTagNewLine("Node");
            return;
        }
        int number = this.N - 1;
        if (count == 1) {
            if (ra.getType().equals("B") || ra.getType().equals("W")) {
                ra.printMove(xml, size, number, this);
                number++;
                if (this.contains("C")) {
                    a = ((Action) this.find("C").content());
                    a.print(xml, size, number);
                }
                return;
            }
        }
        xml.startTagStart("Node");
        if (this.contains("N")) {
            xml.printArg("name", this.getaction("N"));
        }
        if (this.contains("BL")) {
            xml.printArg("blacktime", this.getaction("BL"));
        }
        if (this.contains("WL")) {
            xml.printArg("whitetime", this.getaction("WL"));
        }
        xml.startTagEndNewLine();
        p = this.Actions.first();
        while (p != null) {
            a = (Action) p.content();
            a.print(xml, size, number);
            if (a.getType().equals("B") || a.getType().equals("W")) {
                number++;
            }
            p = p.next();
        }
        xml.endTagNewLine("Node");
    }

    /**
     * remove an action
     */
    public void removeaction(ListElement la) {
        this.Actions.remove(la);
    }

    /**
     * remove all actions
     */
    public void removeactions() {
        this.Actions = new ListClass();
    }

    /**
     * set the action of this type to this argument
     */
    public void setaction(String type, String arg) {
        this.setaction(type, arg, false);
    }

    /**
     * If there is an action of the type: Remove it, if arg is "", else set its
     * argument to arg. Else add a new action in front (if it is true)
     */
    public void setaction(String type, String arg, boolean front) {
        ListElement l = this.Actions.first();
        while (l != null) {
            final Action a = (Action) l.content();
            if (a.getType().equals(type)) {
                if (arg.equals("")) {
                    this.Actions.remove(l);
                    return;
                } else {
                    final ListElement la = a.arguments();
                    if (la != null) {
                        la.content(arg);
                    } else {
                        a.addargument(arg);
                    }
                }
                return;
            }
            l = l.next();
        }
        if (front) {
            this.prependaction(new Action(type, arg));
        } else {
            this.addaction(new Action(type, arg));
        }
    }

    /**
     * Expand an action of the same type as a, else generate a new action. If
     * the action is already present with the same argument, delete that
     * argument from the action.
     */
    public void toggleaction(Action a) {
        final ListElement p = this.find(a.getType());
        if (p == null) {
            this.addaction(a);
        } else {
            final Action pa = (Action) p.content();
            pa.toggleArgument(a.argument());
        }
    }
}
