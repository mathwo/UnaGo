package unagoclient.board;

import unagoclient.gui.MyPanel;

import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;

public class NavigationPanel extends MyPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Board B;
    final int size = 5;
    int w, h;
    boolean OverflowRequest, Overflow;
    boolean Adjust;
    int AdjustX;
    Vector Parents;
    Color BoardColor;

    public NavigationPanel(Board b) {
        this.B = b;
        this.BoardColor = rene.gui.Global.ControlBackground;
    }

    boolean inParents(TreeNode pos) {
        final Enumeration e = this.Parents.elements();
        while (e.hasMoreElements()) {
            if ((TreeNode) e.nextElement() == pos) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void paint(Graphics g) {
        this.Overflow = this.Adjust = false;
        this.AdjustX = 0;
        this.Parents = new Vector();
        int currentline = 0;
        this.w = this.getSize().width;
        this.h = this.getSize().height;
        g.setColor(this.BoardColor);
        g.fillRect(0, 0, this.w, this.h);
        final TreeNode Pos = this.B.positionNode;
        this.Parents.addElement(Pos);
        TreeNode ParentPos = Pos.parentPos();
        TreeNode StartPos = Pos;
        final int x = this.size * 2, y = this.size * 3;
        if (ParentPos != null) {
            this.Parents.addElement(ParentPos);
            currentline++;
            for (int i = 0; i < this.h / (3 * this.size) / 3; i++) {
                if (ParentPos.parentPos() == null) {
                    break;
                }
                ParentPos = ParentPos.parentPos();
                this.Parents.addElement(ParentPos);
                currentline++;
            }
            if (ParentPos.parentPos() != null) {
                g.setColor(Color.black);
                g.drawLine(this.size * 2, this.size * 2, this.size * 2,
                        this.size);
            }
            StartPos = ParentPos;
        }
        this.paint(g, StartPos, x, y, Pos, 0, currentline);
        if (this.OverflowRequest) {
            this.Overflow = true;
            g.clearRect(0, 0, this.w, this.h);
            this.paint(g, StartPos, x, y, Pos, 0, currentline);
            if (this.Adjust) {
                g.clearRect(0, 0, this.w, this.h);
                this.paint(g, StartPos, x - this.AdjustX, y, Pos, 0,
                        currentline);
            }
        }
    }

    public int paint(Graphics g, TreeNode pos, int x, int y, TreeNode current,
            int line, int currentline) {
        if (!this.Overflow && x > this.w) {
            this.OverflowRequest = true;
            return x;
        }
        if (pos == current) {
            g.setColor(Color.red.darker());
            g.fillRect(x - this.size, y - this.size, this.size * 2,
                    this.size * 2);
            if (this.Overflow && !this.Adjust && x > this.w) {
                this.Adjust = true;
                this.AdjustX = x - this.w / 2;
                return x;
            }
        } else if (pos.node().main()) {
            g.setColor(this.BoardColor);
            g.fillRect(x - this.size, y - this.size, this.size * 2,
                    this.size * 2);
        } else {
            g.setColor(Color.gray);
            g.fillRect(x - this.size, y - this.size, this.size * 2,
                    this.size * 2);
        }
        g.setColor(Color.black);
        g.drawRect(x - this.size, y - this.size, this.size * 2, this.size * 2);
        if (!pos.haschildren()) {
            return x;
        }
        if (y + 2 * this.size >= this.h) {
            g.setColor(Color.black);
            g.drawLine(x, y + this.size, x, y + 2 * this.size);
            return x;

        }
        TreeNode p = pos.firstChild();
        if (this.Overflow && !this.inParents(pos)) {
            g.setColor(Color.black);
            g.drawLine(x, y + this.size, x, y + 2 * this.size);
            final int x0 = x;
            x = this.paint(g, p, x, y + 3 * this.size, current, line + 1,
                    currentline);
            if (Board.getNext(p) != null) {
                g.setColor(Color.black);
                g.drawLine(x0, y + this.size * 3 / 2, x0 + this.size, y
                        + this.size * 3 / 2);
            }
        } else {
            int i = 0;
            int x0 = x;
            while (p != null) {
                if (i == 0) {
                    g.setColor(Color.black);
                    g.drawLine(x, y + this.size, x, y + 2 * this.size);
                    x = this.paint(g, p, x, y + 3 * this.size, current,
                            line + 1, currentline);
                } else {
                    g.setColor(Color.black);
                    g.drawLine(x0, y + this.size * 3 / 2, x + 3 * this.size, y
                            + this.size * 3 / 2);
                    x0 = x;
                    g.drawLine(x + 3 * this.size, y + this.size * 3 / 2, x + 3
                            * this.size, y + 2 * this.size);
                    x = this.paint(g, p, x + 3 * this.size, y + 3 * this.size,
                            current, line + 1, currentline);
                }
                p = Board.getNext(p);
                i++;
            }
        }
        return x;
    }
}
