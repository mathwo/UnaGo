package rene.viewer;

import rene.gui.CloseFrame;
import rene.gui.Global;
import rene.gui.Panel3D;
import rene.util.MyVector;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * An extended Version of the Viewer. It is able to reformat lines, when the
 * area is resized. It has no vertical scrollbar. Text is stored into a separate
 * string buffer, and will be formatted on repaint.
 */

public class ExtendedViewer extends Viewer implements AdjustmentListener,
MouseListener, MouseMotionListener, ActionListener, KeyListener,
WheelListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
        final CloseFrame f = new CloseFrame();
        f.setLayout(new BorderLayout());
        final ExtendedViewer v = new ExtendedViewer();
        f.add("Center", v);
        f.setSize(300, 300);
        f.setVisible(true);
        v.append("test1 test test test test test test ");
        v.append("Donaudampfschifffahrtsgesellschaftskapit�n ");
        v.append("test2 test test test test test test ");
        v.append("test3 test test test test test test ");
        v.append("test4 test test test test test test ");
        v.append("test5 test test test test test test ");
        v.append("test6 test test test test test test ");
        v.append("test7 test test test test test test ");
        v.append("test8 test test test test test test ");
        v.appendLine("");
        v.appendLine("");
        v.appendLine("  affe affe affe affe affe affe affe test test test test test last");
        v.appendLine("");
        v.appendLine("test affe affe affe test test test test test last ");
        v.appendLine("  ");
        v.appendLine("test test test test affe affe affe test test last");
        v.repaint();
        v.resized();
    }

    TextDisplay TD;
    Scrollbar Vertical;
    TextPosition Start, End;
    PopupMenu PM;
    int X, Y;
    Panel P3D;
    MyVector V; // Vector of lines
    StringBuffer B; // Buffer for last line

    boolean Changed = false;

    boolean Dragging = false;

    public ExtendedViewer() {
        this.TD = new TextDisplay(this);
        this.setLayout(new BorderLayout());
        this.add("Center", this.P3D = new Panel3D(this.TD));
        this.add("East", this.Vertical = new Scrollbar(Scrollbar.VERTICAL, 0,
                100, 0, 1100));
        this.Vertical.addAdjustmentListener(this);
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
        this.V = new MyVector();
        this.B = new StringBuffer();
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
        }
    }

    @Override
    public void append(String s) {
        this.B.append(s);
    }

    @Override
    public void append(String s, Color c) {
        this.append(s);
    }

    @Override
    public void appendLine(String s) {
        this.B.append(s);
        this.V.addElement(this.B.toString());
        this.B.setLength(0);
        this.Changed = true;
    }

    @Override
    public void appendLine(String s, Color c) {
        this.appendLine(s);
    }

    @Override
    public void appendLine0(String s) {
        this.appendLine(s);
    }

    @Override
    public void appendLine0(String s, Color c) {
        this.appendLine(s);
    }

    public void doAppend(String s) {
        final char a[] = s.toCharArray();
        final int w[] = this.TD.getwidth(a);
        int start = 0, end = 0;
        final int W = this.TD.getSize().width;
        int goodbreak;
        while (start < a.length && a[start] == ' ') {
            start++;
        }
        if (start >= a.length) {
            this.TD.appendLine("");
            return;
        }
        int blanks = 0;
        String sblanks = "";
        int offset = 0;
        if (start > 0) {
            blanks = start;
            sblanks = new String(a, 0, blanks);
            offset = blanks + w[0];
        }
        while (start < a.length) {
            int tw = this.TD.Offset + offset;
            end = start;
            goodbreak = start;
            while (end < a.length && tw < W) {
                tw += w[end];
                if (a[end] == ' ') {
                    goodbreak = end;
                }
                end++;
            }
            if (tw < W) {
                goodbreak = end;
            }
            if (goodbreak == start) {
                goodbreak = end;
            }
            if (blanks > 0) {
                this.TD.appendLine(sblanks
                        + new String(a, start, goodbreak - start));
            } else {
                this.TD.appendLine(new String(a, start, goodbreak - start));
            }
            start = goodbreak;
            while (start < a.length && a[start] == ' ') {
                start++;
            }
        }
    }

    @Override
    public void doUpdate(boolean showlast) {
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

    public void mouseWheelMoved(MouseWheelEvent arg0) {
    }

    public void newLine() {
        this.V.addElement(this.B.toString());
        this.B.setLength(0);
        this.Changed = true;
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

    @Override
    public void paint(Graphics G) {
        super.paint(G);
    }

    @Override
    public synchronized void resized() {
        if (this.TD.getSize().width <= 0) {
            return;
        }
        this.TD.setText("");
        final Enumeration e = this.V.elements();
        while (e.hasMoreElements()) {
            final String s = (String) e.nextElement();
            this.doAppend(s);
        }
        this.TD.repaint();
    }

    @Override
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

    @Override
    public void setTabWidth(int t) {
        this.TD.setTabWidth(t);
    }

    @Override
    public void setText(String S) {
        this.TD.unmark();
        this.Start = this.End = null;
        this.TD.setText(S);
        this.V.removeAllElements();
        this.B.setLength(0);
        this.setVerticalScrollbar();
    }

    @Override
    public void setVerticalScrollbar() {
        if (this.Vertical == null) {
            return;
        }
        final int h = this.TD.computeVerticalSize();
        this.Vertical.setValues(this.TD.computeVertical(), h, 0, 1000 + h);
    }

    @Override
    public void showFirst() {
        this.TD.showFirst();
        this.setVerticalScrollbar();
        this.TD.repaint();
    }

    @Override
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

    public void update() {
        this.resized();
        this.showFirst();
    }

}
