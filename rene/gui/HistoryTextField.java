package rene.gui;

import rene.util.FileName;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A TextField, which display the old input, when cursor up is pressed. The old
 * input is stored in a list. The class is derived from TextFieldAction.
 *
 * @see TextFieldAction
 */

public class HistoryTextField extends TextFieldAction implements KeyListener,
DoActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
        final CloseFrame f = new CloseFrame("test");
        final HistoryTextField t = new HistoryTextField(f, "Test", 30);
        t.remember("AAAA");
        t.remember("BBBB");
        t.remember("CCCC");
        t.remember("DDDD");
        f.setLayout(new BorderLayout());
        f.add("Center", t);
        f.add("South", new HistoryTextFieldChoice(t));
        f.pack();
        f.setVisible(true);
    }

    ListClass H;
    PopupMenu M = null;
    boolean Trigger = false;

    public int MaxLength = 48;

    String Last = "";

    public HistoryTextField(DoActionListener l, String name) {
        super(l, name);
        this.H = new ListClass();
        this.H.append(new ListElement(""));
        this.addKeyListener(this);
    }

    public HistoryTextField(DoActionListener l, String name, int s) {
        super(l, name, s);
        this.H = new ListClass();
        this.H.append(new ListElement(""));
        this.addKeyListener(this);
    }

    public void deleteFromHistory(String s) {
        ListElement e = this.H.first();
        while (e != null) {
            final String t = (String) e.content();
            final ListElement next = e.next();
            if (t.equals(s)) {
                this.H.remove(e);
                if (this.H.first() == null) {
                    this.H.append(new ListElement(""));
                }
            }
            e = next;
        }
    }

    @Override
    public void doAction(String o) {
        if (!o.equals("")) {
            this.setText(o);
            if (this.Trigger) {
                this.triggerAction();
            }
        }
    }

    public boolean filterHistory(String name) {
        return true;
    }

    public ListClass getHistory() {
        return this.H;
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }

    @Override
    public void keyPressed(KeyEvent ev) {
        switch (ev.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
                if (this.M == null) {
                    this.M = new PopupMenu();
                    ListElement e = this.H.last();
                    int i = 0;
                    final int n = Global.getParameter("history.length", 10);
                    while (e != null && i < n) {
                        final String t = (String) e.content();
                        if (!t.equals("")) {
                            final MenuItem item = new MenuItemAction(this,
                                    FileName.chop(t, this.MaxLength), t);
                            this.M.add(item);
                        }
                        e = e.previous();
                        i++;
                    }
                    this.add(this.M);
                }
                this.M.show(this, 10, 10);
                break;
            default:
                return;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void loadHistory(String name) {
        int i = 1;
        this.H = new ListClass();
        this.H.append(new ListElement(""));
        while (Global.haveParameter("history." + name + "." + i)) {
            final String s = Global.getParameter("history." + name + "." + i,
                    "");
            if (!s.equals("") && this.filterHistory(s)) {
                this.H.prepend(new ListElement(s));
            }
            i++;
        }
    }

    public void remember() {
        this.remember(this.getText());
    }

    public void remember(String s) {
        if (s.equals(this.Last)) {
            return;
        }
        this.deleteFromHistory(s);
        this.Last = s;
        this.H.last().content(s);
        this.H.append(new ListElement(""));
        this.M = null;
    }

    public void saveHistory(String name) {
        int i;
        final int n = Global.getParameter("history.length", 10);
        Global.removeAllParameters("history." + name);
        ListElement e = this.H.last();
        if (e == null) {
            return;
        }
        for (i = 0; i < n && e != null; e = e.previous()) {
            final String s = (String) e.content();
            if (!s.equals("")) {
                i++;
                Global.setParameter("history." + name + "." + i, s);
            }
        }
    }

    public void setTrigger(boolean f) {
        this.Trigger = f;
    }
}
