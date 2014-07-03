package rene.viewer;

import rene.util.list.ListElement;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;

public class Lister extends Viewer implements MouseListener, ItemSelectable,
KeyListener, FocusListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
        final Frame f = new rene.gui.CloseFrame() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void doclose() {
                final String s[] = Lister.v.getSelectedItems();
                for (int i = 0; i < s.length; i++) {
                    System.out.println(s[i]);
                }
                super.doclose();
                System.exit(0);
            }
        };
        f.setLayout(new BorderLayout());
        Lister.v = new Lister(true, false);
        f.add("Center", Lister.v);
        f.setSize(300, 300);
        f.setVisible(true);
        Lister.v.add("test", Color.black);
        Lister.v.addItem("test");
        for (int i = 0; i < 10; i++) {
            Lister.v.add("test " + i, new Color((float) Math.random() / 2,
                    (float) Math.random() / 2, (float) Math.random() / 2));
        }
        Lister.v.setMultipleMode(true);
    }

    ListElement Chosen = null;
    Vector AL, IL;
    PopupMenu PM = null;
    public boolean FocusTraversable = true;
    boolean Focus = false;

    boolean Multiple = false;

    public static Lister v;

    public Lister() {
        this(true, true);
    }

    /*
     * public boolean isFocusTraversable () { return FocusTraversable; }
     */

    public Lister(boolean vs, boolean hs) {
        super(vs, hs);
        this.AL = new Vector();
        this.IL = new Vector();
        this.addKeyListener(this);
        this.addFocusListener(this);
    }

    public Lister(String dummy) {
        super(dummy);
    }

    public void add(String s) {
        this.appendLine(s);
    }

    public void add(String s, Color c) {
        this.appendLine(s, c);
    }

    public void addActionListener(ActionListener l) {
        this.AL.addElement(l);
    }

    public void addItem(String s) {
        this.add(s);
    }

    @Override
    public void addItemListener(ItemListener l) {
        this.IL.addElement(l);
    }

    @Override
    public void focusGained(FocusEvent e) {
        this.Focus = true;
        this.TD.repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        this.Focus = false;
        this.TD.repaint();
    }

    public int getSelectedIndex() {
        final int k[] = this.getSelectedIndexes();
        if (k.length == 0) {
            return -1;
        } else {
            return k[0];
        }
    }

    public int[] getSelectedIndexes() {
        int n = 0;
        ListElement le = this.TD.L.first();
        while (le != null) {
            final Line l = (Line) le.content();
            if (l.chosen()) {
                n++;
            }
            le = le.next();
        }
        final int s[] = new int[n];
        n = 0;
        int i = 0;
        le = this.TD.L.first();
        while (le != null) {
            final Line l = (Line) le.content();
            if (l.chosen()) {
                s[n++] = i;
            }
            le = le.next();
            i++;
        }
        return s;
    }

    public String getSelectedItem() {
        if (this.Chosen == null) {
            return null;
        }
        return new String(((Line) this.Chosen.content()).a);
    }

    public String[] getSelectedItems() {
        int n = 0;
        ListElement le = this.TD.L.first();
        while (le != null) {
            final Line l = (Line) le.content();
            if (l.chosen()) {
                n++;
            }
            le = le.next();
        }
        final String s[] = new String[n];
        n = 0;
        le = this.TD.L.first();
        while (le != null) {
            final Line l = (Line) le.content();
            if (l.chosen()) {
                s[n++] = new String(l.a);
            }
            le = le.next();
        }
        return s;
    }

    @Override
    public Object[] getSelectedObjects() {
        return this.getSelectedItems();
    }

    @Override
    public boolean hasFocus() {
        return this.Focus;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (this.Multiple) {
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER
                || e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (this.Chosen == null) {
                return;
            }
            if (e.isControlDown()) {
                final Enumeration en = this.IL.elements();
                while (en.hasMoreElements()) {
                    final ItemListener li = (ItemListener) en.nextElement();
                    li.itemStateChanged(new ItemEvent(this, 0, this
                            .getSelectedItem(), ItemEvent.ITEM_STATE_CHANGED));
                }
            }
            final Enumeration en = this.AL.elements();
            while (en.hasMoreElements()) {
                final ActionListener li = (ActionListener) en.nextElement();
                li.actionPerformed(new ActionEvent(this, 0, this
                        .getSelectedItem()));
            }
            return;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN
                || e.getKeyCode() == KeyEvent.VK_UP) {
            if (this.Chosen == null) {
                this.Chosen = this.TD.L.first();
                ((Line) this.Chosen.content()).chosen(true);
                if (this.Chosen == null) {
                    return;
                }
                this.TD.showLine(this.Chosen);
                this.TD.repaint();
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                ((Line) this.Chosen.content()).chosen(false);
                this.Chosen = this.Chosen.next();
                if (this.Chosen == null) {
                    this.Chosen = this.TD.L.first();
                }
                ((Line) this.Chosen.content()).chosen(true);
                this.TD.showLine(this.Chosen);
                this.TD.repaint();
            } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                ((Line) this.Chosen.content()).chosen(false);
                this.Chosen = this.Chosen.previous();
                if (this.Chosen == null) {
                    this.Chosen = this.TD.L.last();
                }
                ((Line) this.Chosen.content()).chosen(true);
                this.TD.showLine(this.Chosen);
                this.TD.repaint();
            }
            final Enumeration en = this.IL.elements();
            while (en.hasMoreElements()) {
                final ItemListener li = (ItemListener) en.nextElement();
                li.itemStateChanged(new ItemEvent(this, 0, this
                        .getSelectedItem(), ItemEvent.ITEM_STATE_CHANGED));
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getClickCount() >= 2 || e.isControlDown()) {
            if (e.isControlDown()) {
                final Enumeration en = this.IL.elements();
                while (en.hasMoreElements()) {
                    final ItemListener li = (ItemListener) en.nextElement();
                    li.itemStateChanged(new ItemEvent(this, 0, this
                            .getSelectedItem(), ItemEvent.ITEM_STATE_CHANGED));
                }
            }
            final Enumeration en = this.AL.elements();
            while (en.hasMoreElements()) {
                final ActionListener li = (ActionListener) en.nextElement();
                li.actionPerformed(new ActionEvent(this, 0, this
                        .getSelectedItem()));
            }
            return;
        }
        this.requestFocus();
        final ListElement le = this.TD.getline(e.getY());
        if (le == null) {
            return;
        }
        final Line l = (Line) le.content();
        if (!this.Multiple && this.Chosen != null) {
            ((Line) this.Chosen.content()).chosen(false);
        }
        this.Chosen = le;
        l.chosen(!l.chosen());
        this.TD.paint(this.TD.getGraphics());
        if (this.Multiple) {
            return;
        }
        final Enumeration en = this.IL.elements();
        while (en.hasMoreElements()) {
            final ItemListener li = (ItemListener) en.nextElement();
            li.itemStateChanged(new ItemEvent(this, 0, this.getSelectedItem(),
                    ItemEvent.ITEM_STATE_CHANGED));
        }
        if (e.isPopupTrigger() || e.isMetaDown() && this.PM != null) {
            this.PM.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void removeAll() {
        this.setText("");
    }

    @Override
    public void removeItemListener(ItemListener l) {
        this.IL.removeElement(l);
    }

    public void select(int n) {
        ListElement le = this.TD.L.first();
        int i = 0;
        while (le != null) {
            final Line l = (Line) le.content();
            if (i == n) {
                l.chosen(true);
                this.Chosen = le;
                this.TD.repaint();
                return;
            }
            le = le.next();
            i++;
        }
    }

    public void select(String s) {
        ListElement le = this.TD.L.first();
        while (le != null) {
            final Line l = (Line) le.content();
            if (s.equals(l.a)) {
                l.chosen(true);
                this.Chosen = le;
                this.TD.repaint();
                return;
            }
            le = le.next();
        }
    }

    @Override
    public void setBackground(Color c) {
        this.TD.setBackground(c);
        super.setBackground(c);
    }

    public void setMultipleMode(boolean flag) {
        this.Multiple = flag;
    }

    public void setPopupMenu(PopupMenu pm) {
        this.PM = pm;
        this.add(this.PM);
    }

    @Override
    public void setText(String s) {
        this.Chosen = null;
        super.setText(s);
    }

}
