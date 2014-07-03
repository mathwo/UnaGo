package unagoclient.board;

import rene.util.list.Tree;

/**
 * This is a child class of Tree, with some help functions for the content type
 * Node.
 *
 * @see unagoclient.list.Tree
 * @see unagoclient.board.Node
 */

public class TreeNode extends Tree {
    /**
     * initialize with an empty node with the specified number
     */
    public TreeNode(int number) {
        super(new Node(number));
    }

    /**
     * initialize with a given Node
     */
    public TreeNode(Node n) {
        super(n);
    }

    /**
     * add this action to the node
     */
    public void addaction(Action a) {
        this.node().addaction(a);
    }

    public TreeNode firstChild() {
        return (TreeNode) this.firstchild();
    }

    /**
     * get the value of the action of this type
     */
    public String getaction(String type) {
        return this.node().getaction(type);
    }

    /**
     * @return true if it the last node in the main tree
     */
    public boolean isLastMain() {
        return !this.haschildren() && this.isMain();
    }

    /**
     * @return true if it is a main node
     */
    public boolean isMain() {
        return this.node().main();
    }

    public TreeNode lastChild() {
        return (TreeNode) this.lastchild();
    }

    /**
     * set the main flag in the node
     */
    public void main(boolean flag) {
        this.node().main(flag);
    }

    public Node node() {
        return ((Node) this.content());
    }

    public TreeNode parentPos() {
        return (TreeNode) this.parent();
    }

    public void setaction(String type, String s) {
        this.node().setaction(type, s);
    }

    /**
     * Set the action type in the node to the string s.
     *
     * @param flag
     *            determines, if the action is to be added, even of s is emtpy.
     */
    public void setaction(String type, String s, boolean flag) {
        this.node().setaction(type, s, flag);
    }
}
