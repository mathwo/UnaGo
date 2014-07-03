/*
 * Created on 14.01.2006
 *
 */
package rene.lister;

import rene.gui.Global;
import rene.gui.MyPanel;
import rene.util.MyVector;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

public class ListerPanel extends MyPanel implements WheelListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final MyVector V; // Vector
                              // of
                              // listed
    // Elements
    int Top; // Top
             // Element

    Image I; // Buffer
             // Image
    int W, H; // width
              // and
              // height
    // of
    // current panel
    // and image
    Graphics IG; // Graphics
                 // for the
    // image
    Font F; // current
            // font
    FontMetrics FM; // current
                    // font
    // metrics
    int Leading, Height, Ascent, Descent; // font
                                          // stuff
    int PageSize; // numbers
                  // of
                  // lines
    // per
    // page
    int HOffset; // horizontal
    // offset of
    // display
    boolean ShowLast; // Show
                      // last on
    // next
    // redraw

    Lister LD;
    String Name;

    public Color ListingBackground = null;

    public boolean MultipleSelection = true; // Allow
                                             // multiple
    // selection
    public boolean EasyMultipleSelection = false; // Multiple
                                                  // select
    // without right
    // mouse
    public boolean ReportSingleClick = false; // Report
                                              // single
    // clicks
    // also
    public boolean RightMouseClick = false; // Report
                                            // right
    // mouse
    // clicks also

    int State = 0;

    Vector VAL = new Vector(); // Vector
                               // of
                               // action
    // listener

    MyVector Selected = new MyVector(); // currently

    // selected items

    public ListerPanel(Lister ld) {
        this(ld, "");
    }

    public ListerPanel(Lister ld, String name) {
        this.LD = ld;
        this.Name = name;
        this.V = new MyVector();
        this.Top = 0;
        final Wheel W = new Wheel(this);
        this.addMouseWheelListener(W);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ListerPanel.this.clicked(e);
            }
        });
    }

    /**
     * Add a new line of type rene.lister.Element
     *
     * @param e
     */
    public synchronized void add(Element e) {
        this.V.addElement(e);
    }

    /**
     * Add an action listener for all actions of this panel.
     *
     * @param al
     */
    public void addActionListener(ActionListener al) {
        this.VAL.addElement(al);
    }

    /**
     * Set Anti-Aliasing on or off, if in Java 1.2 or better and the Parameter
     * "font.smooth" is switched on.
     *
     * @param flag
     */
    public void antialias(boolean flag) {
        if (Global.getParameter("font.smooth", true)) {
            this.IG = this.IG;
            ((Graphics2D) this.IG).setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    flag ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                            : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
    }

    /**
     * Delete all items from the panel.
     */
    public synchronized void clear() {
        this.Selected.removeAllElements();
        this.V.removeAllElements();
        this.Top = 0;
    }

    /**
     * React on mouse clicks (single or double, or right click). single: select
     * the item (according multiple mode) cause change action. double: select
     * only this item and cause action. right: popup menu, if possible. In any
     * case, report the result to the action listeners.
     *
     * @param e
     */
    public void clicked(MouseEvent e) {
        final int n = e.getY() / (this.Leading + this.Height);
        if (e.isMetaDown() && this.RightMouseClick) {
            final Enumeration en = this.VAL.elements();
            while (en.hasMoreElements()) {
                ((ActionListener) (en.nextElement()))
                .actionPerformed(new ListerMouseEvent(this.LD,
                        this.Name, e));
            }
        } else {
            if (this.Top + n >= this.V.size()) {
                return;
            }
            final int sel = n + this.Top;
            if (e.getClickCount() >= 2) {
                if (!this.MultipleSelection) {
                    this.Selected.removeAllElements();
                }
                this.select(sel);
            } else if (this.MultipleSelection
                    && (e.isControlDown() || this.EasyMultipleSelection || e
                            .isShiftDown())) {
                if (e.isControlDown() || this.EasyMultipleSelection) {
                    this.toggleSelect(sel);
                } else if (e.isShiftDown()) {
                    this.expandSelect(sel);
                }
            } else {
                this.Selected.removeAllElements();
                this.Selected.addElement(new Integer(sel));
            }
            final Graphics g = this.getGraphics();
            this.paint(g);
            g.dispose();
            if (e.getClickCount() >= 2 || this.ReportSingleClick) {
                final Enumeration en = this.VAL.elements();
                while (en.hasMoreElements()) {
                    ((ActionListener) (en.nextElement()))
                    .actionPerformed(new ListerMouseEvent(this.LD,
                            this.Name, e));
                }
            }
        }
    }

    // Used by the mouse wheel or external programs:

    /**
     * Paint the current text lines on the image.
     *
     * @param g
     */
    public synchronized void dopaint(Graphics g) {
        if (this.ShowLast) {
            this.Top = this.V.size() - this.PageSize + 1;
            if (this.Top < 0) {
                this.Top = 0;
            }
            this.ShowLast = false;
        }
        if (this.ListingBackground != null) {
            g.setColor(this.ListingBackground);
        } else {
            g.setColor(this.getBackground());
        }
        g.fillRect(0, 0, this.W, this.H);
        g.setColor(Color.black);
        int h = this.Leading + this.Ascent;
        this.getSize();
        int line = this.Top;
        if (line < 0) {
            return;
        }
        while (line - this.Top < this.PageSize && line < this.V.size()) {
            final Element el = (Element) this.V.elementAt(line);
            if (this.isSelected(line)) {
                g.setColor(this.getBackground().darker());
                g.fillRect(0, h - this.Ascent, this.W, this.Height);
                g.setColor(Color.black);
            }
            final Color col = el.getElementColor();
            if (col != null) {
                g.setColor(col);
            } else {
                g.setColor(Color.black);
            }
            g.drawString(el.getElementString(this.State), 2 - this.HOffset, h);
            h += this.Leading + this.Height;
            line++;
        }
    }

    @Override
    public synchronized void down(int n) {
        this.Top -= n;
        if (this.Top < 0) {
            this.Top = 0;
        }
        this.repaint();
    }

    /**
     * Expand the selection to include sel and all elements in between.
     *
     * @param sel
     */
    public synchronized void expandSelect(int sel) { // compute maximal selected
        // index below sel.
        int max = -1;
        Enumeration e = this.Selected.elements();
        while (e.hasMoreElements()) {
            final int i = ((Integer) e.nextElement()).intValue();
            if (i > max && i < sel) {
                max = i;
            }
        }
        if (max >= 0) {
            for (int i = max + 1; i <= sel; i++) {
                this.select(i);
            }
            return;
        }
        int min = this.V.size();
        e = this.Selected.elements();
        while (e.hasMoreElements()) {
            final int i = ((Integer) e.nextElement()).intValue();
            if (i < min && i > sel) {
                min = i;
            }
        }
        if (min < this.V.size()) {
            for (int i = sel; i <= min; i++) {
                this.select(i);
            }
        }
    }

    public synchronized Element getElementAt(int n) {
        return (Element) this.V.elementAt(n);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 300);
    }

    /**
     * Initialize the font stuff and set the background of the panel.
     */
    synchronized void init() {
        this.F = this.getFont();
        this.FM = this.getFontMetrics(this.F);
        this.Leading = this.FM.getLeading()
                + Global.getParameter("fixedfont.spacing", -1);
        this.Height = this.FM.getHeight();
        this.Ascent = this.FM.getAscent();
        this.Descent = this.FM.getDescent();
        if (Global.Background != null) {
            this.setBackground(Global.Background);
        }
        if (this.Height + this.Leading > 0) {
            this.PageSize = this.H / (this.Height + this.Leading);
        } else {
            this.PageSize = 10;
        }
        this.antialias(true);
        this.Top = 0;
    }

    /**
     * Determine if line sel is selected
     *
     * @param sel
     * @return selected or not
     */
    public synchronized boolean isSelected(int sel) {
        final Enumeration e = this.Selected.elements();
        while (e.hasMoreElements()) {
            final int n = ((Integer) e.nextElement()).intValue();
            if (n == sel) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void pageDown() {
        this.down(this.PageSize - 1);
        this.repaint();
    }

    // Mouse routines:

    @Override
    public synchronized void pageUp() {
        this.up(this.PageSize - 1);
        this.repaint();
    }

    /**
     * Paint routine. Simply sets up the buffer image, calls dopaint and paints
     * the image to the screen.
     */
    @Override
    public synchronized void paint(Graphics g) {
        final Dimension d = this.getSize();
        if (this.I == null || this.I.getWidth(this) != d.width
                || this.I.getHeight(this) != d.height) {
            this.I = this.createImage(this.W = d.width, this.H = d.height);
            if (this.I == null) {
                return;
            }
            this.IG = this.I.getGraphics();
            this.init();
        }
        this.dopaint(this.IG);
        g.drawImage(this.I, 0, 0, this.W, this.H, this);
        double vp, vs, hp, hs;
        if (this.V.size() > 1) {
            vp = (double) this.Top / this.V.size();
        } else {
            vp = 0;
        }
        if (this.V.size() > 2 * this.PageSize) {
            vs = (double) this.PageSize / this.V.size();
        } else {
            vs = 0.5;
        }
        if (this.HOffset < 10 * this.W) {
            hp = (double) this.HOffset / (10 * this.W);
        } else {
            hp = 0.9;
        }
        hs = 0.1;
        this.LD.setScrollbars(vp, vs, hp, hs);
    }

    /**
     * Remove an action listener
     *
     * @param al
     */
    public void removeActionListener(ActionListener al) {
        this.VAL.removeElement(al);
    }

    public synchronized void save(PrintWriter o) {
        final Enumeration e = this.V.elements();
        while (e.hasMoreElements()) {
            final Element el = (Element) e.nextElement();
            o.println(el.getElementString());
        }
    }

    /**
     * Selecte an item by number sel.
     *
     * @param sel
     */
    public synchronized void select(int sel) {
        if (!this.isSelected(sel)) {
            this.Selected.addElement(new Integer(sel));
        }
    }

    /**
     * Set the horizontal offset.
     *
     * @param x
     *            ofset in percent of 10 times the screen width
     */
    public synchronized void setHorizontalPos(double x) {
        this.HOffset = (int) (x * 10 * this.W);
        this.repaint();
    }

    public void setListingBackground(Color c) {
        this.ListingBackground = c;
    }

    public void setState(int s) {
        this.State = s;
    }

    /**
     * Set the vertical position. Used by the scrollbars in the Lister.
     *
     * @param x
     *            percentage of text
     */
    public synchronized void setVerticalPos(double x) {
        this.Top = (int) (x * this.V.size());
        if (this.Top >= this.V.size()) {
            this.Top = this.V.size() - 1;
        }
        this.repaint();
    }

    /**
     * Make sure, the last elment displays.
     */
    public synchronized void showLast() {
        this.ShowLast = true;
    }

    /**
     * Toggle the line sel to be selected or not.
     *
     * @param sel
     */
    public synchronized void toggleSelect(int sel) {
        final Enumeration e = this.Selected.elements();
        while (e.hasMoreElements()) {
            final Integer i = (Integer) e.nextElement();
            if (i.intValue() == sel) {
                this.Selected.removeElement(i);
                return;
            }
        }
        this.Selected.addElement(new Integer(sel));
    }

    @Override
    public synchronized void up(int n) {
        this.Top += n;
        if (this.Top >= this.V.size()) {
            this.Top = this.V.size() - 1;
        }
        if (this.Top < 0) {
            this.Top = 0;
        }
        this.repaint();
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }
}
