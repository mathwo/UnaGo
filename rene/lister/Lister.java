/*
 * Created on 14.01.2006
 *
 * This is a display for lists of items, stored in rene.lister.Element.
 * The display has two optional scrollbars and a ListerPanel.
 */
package rene.lister;

import rene.gui.CloseFrame;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.PrintWriter;

public class Lister extends Panel implements AdjustmentListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
        final CloseFrame F = new CloseFrame("Test");
        F.setSize(300, 400);
        F.setLocation(200, 200);
        F.setLayout(new BorderLayout());
        final Lister L = new Lister(true, true);
        F.add("Center", L);
        for (int i = 0; i < 1000; i++) {
            L.getLister().add(
                    new StringElement("-------------- This is line number: "
                            + i, new Color(0, 0, i % 256)));
        }
        F.setVisible(true);
    }

    public ListerPanel L;

    Scrollbar Vertical, Horizontal;

    public Lister() {
        this(true, true);
    }

    /**
     * Initialize the display and the two optional scrollbars
     *
     * @param verticalscrollbar
     * @param horizontal
     *            scrollbar
     */
    public Lister(boolean vs, boolean hs) {
        this.L = new ListerPanel(this);
        this.setLayout(new BorderLayout());
        this.add("Center", this.L);
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
    }

    public void addActionListener(ActionListener al) {
        this.L.addActionListener(al);
    }

    public void addElement(Element el) {
        this.L.add(el);
    }

    public void addElement(String name) {
        this.addElement(new StringElement(name));
    }

    /**
     * Shortcut to add a string with a specific color.
     *
     * @param name
     * @param col
     */
    public void addElement(String name, Color col) {
        this.addElement(new StringElement(name, col));
    }

    /**
     * Called by the scrollbars.
     */
    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (this.Vertical != null && e.getSource() == this.Vertical) {
            switch (e.getAdjustmentType()) {
                case AdjustmentEvent.UNIT_INCREMENT:
                    this.L.up(1);
                    break;
                case AdjustmentEvent.UNIT_DECREMENT:
                    this.L.down(1);
                    break;
                case AdjustmentEvent.BLOCK_INCREMENT:
                    this.L.pageUp();
                    break;
                case AdjustmentEvent.BLOCK_DECREMENT:
                    this.L.pageDown();
                    break;
                default:
                    final int size = this.Vertical.getVisibleAmount();
                    final int max = this.Vertical.getMaximum();
                    final int pos = this.Vertical.getValue();
                    this.L.setVerticalPos((double) (pos) / (max - size));
            }
        } else if (this.Horizontal != null && e.getSource() == this.Horizontal) {
            int pos = this.Horizontal.getValue();
            switch (e.getAdjustmentType()) {
                case AdjustmentEvent.UNIT_INCREMENT:
                    pos += 10;
                    break;
                case AdjustmentEvent.UNIT_DECREMENT:
                    pos -= 10;
                    break;
                case AdjustmentEvent.BLOCK_INCREMENT:
                    pos += 50;
                    break;
                case AdjustmentEvent.BLOCK_DECREMENT:
                    pos -= 50;
                    break;
            }
            this.L.setHorizontalPos((double) (pos) / 1000);
            this.Horizontal.setValue(pos);
        }
    }

    public void clear() {
        this.L.clear();
    }

    /**
     * Return the lister for external use.
     *
     * @return lister panel
     */
    public ListerPanel getLister() {
        return this.L;
    }

    /**
     * Get the first selected index.
     *
     * @return index or -1
     */
    public int getSelectedIndex() {
        if (this.L.Selected.size() > 0) {
            return ((Integer) this.L.Selected.elementAt(0)).intValue();
        } else {
            return -1;
        }
    }

    /**
     * Get a vector of all selected indices.
     *
     * @return vector of indices
     */
    public int[] getSelectedIndices() {
        final int k[] = new int[this.L.Selected.size()];
        for (int i = 0; i < k.length; i++) {
            k[i] = ((Integer) this.L.Selected.elementAt(i)).intValue();
        }
        return k;
    }

    public String getSelectedItem() {
        final int n = this.getSelectedIndex();
        if (n < 0) {
            return null;
        }
        return this.L.getElementAt(n).getElementString();
    }

    public void removeActionListener(ActionListener al) {
        this.L.removeActionListener(al);
    }

    /**
     * Print the lines to the printwriter o.
     *
     * @param o
     */
    public void save(PrintWriter o) {
        this.L.save(o);
    }

    public void select(int sel) {
    }

    public void setListingBackground(Color c) {
        this.L.setListingBackground(c);
    }

    /**
     * Set the operations mode.
     *
     * @param multiple
     *            allows multiple clicks
     * @param easymultiple
     *            multiple selection without control
     * @param singleclick
     *            report single click events
     * @param rightmouse
     *            report right mouse clicks
     */
    public void setMode(boolean multiple, boolean easymultiple,
            boolean singleclick, boolean rightmouse) {
        this.L.MultipleSelection = multiple;
        this.L.EasyMultipleSelection = easymultiple;
        this.L.ReportSingleClick = singleclick;
        this.L.RightMouseClick = rightmouse;
    }

    /**
     * Calles by the lister to set the vertical scrollbars.
     *
     * @param vp
     *            vertical position
     * @param vs
     *            vertical size
     * @param hp
     *            horizontal position
     * @param hs
     *            horizontal size
     */
    public void setScrollbars(double vp, double vs, double hp, double hs) {
        if (this.Vertical != null) {
            final int size = (int) (vs * 1000);
            final int max = 1000 + size;
            final int pos = (int) (vp * 1000);
            this.Vertical.setValues(pos, size, 0, max);
        }
    }

    public void setState(int s) {
        this.L.setState(s);
    }

    /**
     * Make sure, the lister shows the last element.
     */
    public void showLast() {
        this.L.showLast();
    }

    public void updateDisplay() {
        this.L.repaint();
    }
}
