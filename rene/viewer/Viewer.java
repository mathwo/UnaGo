package rene.viewer;

import rene.gui.Global;
import rene.gui.Panel3D;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;

/**
 * This is a read-only TextArea, removing the memory restriction in some OS's.
 * Component usage is like a Panel. Use appendLine to append a line of text. You
 * can give each line a different color. Moreover, you can save the file to a
 * PrintWriter. You can mark blocks with the right mouse button. Dragging and
 * scrolling is not supported in this version.
 */

public class Viewer extends Panel implements AdjustmentListener, MouseListener,
MouseMotionListener, ActionListener, KeyListener, WheelListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
        final Frame f = new Frame();
        f.setLayout(new BorderLayout());
        final Viewer v = new Viewer(true, false);
        f.add("Center", v);
        f.setSize(300, 300);
        f.setVisible(true);
        v.append("test test test test test test test");
        v.appendLine("test test test test test test test");
        v.appendLine("test test test test test test test");
        v.appendLine("test test test test test test test");
    }

    TextDisplay TD;
    Scrollbar Vertical, Horizontal;
    TextPosition Start, End;
    PopupMenu PM;
    int X, Y;

    Panel P3D;

    boolean Dragging = false;

    public Viewer() {
        this(true, true);
    }

    public Viewer(boolean vs, boolean hs) {
        this.TD = new TextDisplay(this);
        this.setLayout(new BorderLayout());
        this.add("Center", this.P3D = new Panel3D(this.TD));
        if (vs) {
            this.add("East", this.Vertical = new Scrollbar(Scrollbar.VERTICAL,
                    0, 100, 0, 1100));
            this.Vertical.addAdjustmentListener(this);
        }
        if (hs) {
            this.add("South", this.Horizontal = new Scrollbar(
                    Scrollbar.HORIZONTAL, 0, 100, 0, 1100));
            this.Horizontal.addAdjustmentListener(this);
        }
        this.TD.addMouseListener(this);
        this.TD.addMouseMotionListener(this);
        this.Start = this.End = null;
        this.PM = new PopupMenu();
        MenuItem mi = new MenuItem(Global.name("block.copy", "Copy"));
        mi.addActionListener(this);
        this.PM.add(mi);
        this.PM.addSeparator();
        mi = new MenuItem(Global.name("block.begin", "Begin Block"));
        mi.addActionListener(this);
        this.PM.add(mi);
        mi = new MenuItem(Global.name("block.end", "End Block"));
        mi.addActionListener(this);
        this.PM.add(mi);
        this.add(this.PM);
        final Wheel W = new Wheel(this);
        this.addMouseWheelListener(W);
    }

    public Viewer(String dummy) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String o = e.getActionCommand();
        if (o.equals(Global.name("block.copy", "Copy"))) {
            this.TD.copy(this.Start, this.End);
        } else if (o.equals(Global.name("block.begin", "Begin Block"))) {
            this.TD.unmark(this.Start, this.End);
            this.Start = this.TD.getposition(this.X, this.Y);
            this.Start.oneleft();
            if (this.End == null && this.TD.L.last() != null) {
                this.End = this.TD.lastpos();
            }
            this.TD.mark(this.Start, this.End);
        } else if (o.equals(Global.name("block.end", "End Block"))) {
            this.TD.unmark(this.Start, this.End);
            this.End = this.TD.getposition(this.X, this.Y);
            if (this.Start == null && this.TD.L.first() != null) {
                this.Start = new TextPosition(this.TD.L.first(), 0, 0);
            }
            this.TD.mark(this.Start, this.End);
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == this.Vertical) {
            switch (e.getAdjustmentType()) {
                case AdjustmentEvent.UNIT_INCREMENT:
                    this.TD.verticalUp();
                    break;
                case AdjustmentEvent.UNIT_DECREMENT:
                    this.TD.verticalDown();
                    break;
                case AdjustmentEvent.BLOCK_INCREMENT:
                    this.TD.verticalPageUp();
                    break;
                case AdjustmentEvent.BLOCK_DECREMENT:
                    this.TD.verticalPageDown();
                    break;
                default:
                    final int v = this.Vertical.getValue();
                    this.Vertical.setValue(v);
                    this.TD.setVertical(v);
                    return;
            }
            this.setVerticalScrollbar();
        } else if (e.getSource() == this.Horizontal) {
            this.Horizontal.setValue(this.TD.setHorizontal(this.Horizontal
                    .getValue()));
        }
    }

    public void append(String s) {
        this.append(s, Color.black);
    }

    public void append(String s, Color c) {
        this.TD.append(s, c);
    }

    public void appendLine(String s) {
        this.TD.appendLine0(s);
    }

    public void appendLine(String s, Color c) {
        this.TD.appendLine0(s, c);
    }

    public void appendLine0(String s) {
        this.TD.appendLine0(s);
    }

    public void appendLine0(String s, Color c) {
        this.TD.appendLine0(s, c);
    }

    public void doUpdate(boolean showlast) {
        this.TD.doUpdate(showlast);
        this.setVerticalScrollbar();
    }

    @Override
    public void down(int n) {
        for (int i = 0; i < n; i++) {
            this.TD.verticalDown();
        }
        this.setVerticalScrollbar();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(150, 200);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(150, 200);
    }

    @Override
    public boolean hasFocus() {
        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C
                && this.Start != null && this.End != null) {
            this.TD.copy(this.Start, this.End);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.TD.unmark(this.Start, this.End);
        final TextPosition h = this.TD.getposition(e.getX(), e.getY());
        if (h != null) {
            this.End = h;
        }
        this.TD.mark(this.Start, this.End);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger() || e.isMetaDown()) {
            this.PM.show(e.getComponent(), e.getX(), e.getY());
            this.X = e.getX();
            this.Y = e.getY();
        } else {
            this.TD.unmark(this.Start, this.End);
            this.Start = this.TD.getposition(e.getX(), e.getY());
            this.Start.oneleft();
            this.End = null;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.Dragging = false;
    }

    @Override
    public void pageDown() {
        this.TD.verticalPageDown();
        this.setVerticalScrollbar();
    }

    @Override
    public void pageUp() {
        this.TD.verticalPageUp();
        this.setVerticalScrollbar();
    }

    public void resized() {
    }

    public void save(PrintWriter fo) {
        this.TD.save(fo);
    }

    @Override
    public void setBackground(Color c) {
        this.TD.setBackground(c);
        this.P3D.setBackground(c);
        super.setBackground(c);
    }

    @Override
    public void setFont(Font f) {
        this.TD.init(f);
    }

    public void setTabWidth(int t) {
        this.TD.setTabWidth(t);
    }

    public void setText(String S) {
        this.TD.unmark();
        this.Start = this.End = null;
        this.TD.setText(S);
        this.setVerticalScrollbar();
    }

    public void setVerticalScrollbar() {
        if (this.Vertical == null) {
            return;
        }
        final int h = this.TD.computeVerticalSize();
        this.Vertical.setValues(this.TD.computeVertical(), h, 0, 1000 + h);
    }

    public void showFirst() {
        this.TD.showFirst();
        this.setVerticalScrollbar();
        this.TD.repaint();
    }

    public void showLast() {
        this.TD.showlast();
        this.setVerticalScrollbar();
        this.TD.repaint();
    }

    @Override
    public void up(int n) {
        for (int i = 0; i < n; i++) {
            this.TD.verticalUp();
        }
        this.setVerticalScrollbar();
    }

}
