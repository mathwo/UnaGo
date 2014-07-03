package unagoclient.igs;

import unagoclient.Global;
import unagoclient.gui.*;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A message filter can be either positive or negative. It is used to either
 * block messages, or see messages, even when there source is blocked by global
 * flags.
 * <p>
 * The filter is determined by a start string, a string it must contain or an
 * end string. Filters are loaded at program start from filter.cfg. This is done
 * by a call to the load method.
 * <p>
 * The MessageFilter class has a list of SingleMessageFilter to check the
 * message against.
 */

public class MessageFilter {
    ListClass F;
    public static final int BLOCK_COMPLETE = 2;
    public static final int BLOCK_POPUP = 1;

    public MessageFilter() {
        this.F = new ListClass();
        this.load();
    }

    public int blocks(String s) {
        ListElement e = this.F.first();
        while (e != null) {
            final SingleMessageFilter f = (SingleMessageFilter) e.content();
            if (!f.positive() && f.matches(s)) {
                if (f.BlockComplete) {
                    return MessageFilter.BLOCK_COMPLETE;
                } else {
                    return MessageFilter.BLOCK_POPUP;
                }
            }
            e = e.next();
        }
        return 0;
    }

    public void edit() {
        new MessageFilterEdit(this.F);
    }

    /**
     * Load the message filters from filter.cfg.
     */
    public void load() {
        try {
            final BufferedReader in = Global.getStream(".filter.cfg");
            while (true) {
                String name = in.readLine();
                if (name == null || name.equals("")) {
                    break;
                }
                boolean pos = false;
                if (name.startsWith("+++++")) {
                    pos = true;
                    name = name.substring(5);
                    if (name.equals("")) {
                        break;
                    }
                }
                final String start = in.readLine();
                if (start == null) {
                    break;
                }
                final String end = in.readLine();
                if (end == null) {
                    break;
                }
                final String contains = in.readLine();
                if (contains == null) {
                    break;
                }
                final String blockcomplete = in.readLine();
                if (blockcomplete == null) {
                    break;
                }
                this.F.append(new ListElement(
                        new SingleMessageFilter(name, start, end, contains,
                                blockcomplete.equals("true"), pos)));
            }
            in.close();
        } catch (final Exception e) {
            return;
        }
    }

    public boolean posfilter(String s) {
        ListElement e = this.F.first();
        while (e != null) {
            final SingleMessageFilter f = (SingleMessageFilter) e.content();
            if (f.positive() && f.matches(s)) {
                return true;
            }
            e = e.next();
        }
        return false;
    }

    public void save() {
        if (Global.isApplet()) {
            return;
        }
        try {
            final PrintWriter out = new PrintWriter(new FileOutputStream(
                    Global.home() + ".filter.cfg"));
            ListElement l = this.F.first();
            while (l != null) {
                final SingleMessageFilter p = (SingleMessageFilter) l.content();
                if (p.positive()) {
                    out.println("+++++" + p.Name);
                } else {
                    out.println(p.Name);
                }
                out.println(p.Start);
                out.println(p.End);
                out.println(p.Contains);
                out.println(p.BlockComplete);
                l = l.next();
            }
            out.close();
        } catch (final IOException e) {
            return;
        }
    }
}

class MessageFilterEdit extends CloseFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ListClass F;
    java.awt.List L;

    public MessageFilterEdit(ListClass f) {
        super(Global.resourceString("Message_Filter"));
        final MenuBar mb = new MenuBar();
        this.setMenuBar(mb);
        final Menu m = new MyMenu(Global.resourceString("Options"));
        m.add(new MenuItemAction(this, Global.resourceString("Close")));
        mb.add(m);
        this.F = f;
        this.L = new java.awt.List();
        this.L.setFont(Global.SansSerif);
        this.add("Center", new Panel3D(this.L));
        ListElement e = this.F.first();
        while (e != null) {
            this.L.add(((SingleMessageFilter) e.content()).Name);
            e = e.next();
        }
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Edit")));
        p.add(new ButtonAction(this, Global.resourceString("New")));
        p.add(new ButtonAction(this, Global.resourceString("Delete")));
        p.add(new MyLabel(" "));
        p.add(new ButtonAction(this, Global.resourceString("OK")));
        this.add("South", new Panel3D(p));
        this.seticon("iunago.gif");
        Global.setwindow(this, "filteredit", 300, 300);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Edit").equals(o)) {
            new SingleFilterEdit(this, this.F, this.selected());
        } else if (Global.resourceString("New").equals(o)) {
            new SingleFilterEdit(this, this.F, null);
        } else if (Global.resourceString("Delete").equals(o)) {
            this.removeselected();
        } else if (Global.resourceString("OK").equals(o)) {
            Global.saveMessageFilter();
            this.doclose();
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        Global.notewindow(this, "filteredit");
        super.doclose();
    }

    void removeselected() {
        final String s = this.L.getSelectedItem();
        if (s == null) {
            return;
        }
        ListElement e = this.F.first();
        while (e != null) {
            final SingleMessageFilter f = (SingleMessageFilter) e.content();
            if (f.Name.equals(s)) {
                this.F.remove(e);
                this.updatelist();
                return;
            }
            e = e.next();
        }
    }

    SingleMessageFilter selected() {
        final String s = this.L.getSelectedItem();
        if (s == null) {
            return null;
        }
        ListElement e = this.F.first();
        while (e != null) {
            final SingleMessageFilter f = (SingleMessageFilter) e.content();
            if (f.Name.equals(s)) {
                return f;
            }
            e = e.next();
        }
        return null;
    }

    void updatelist() {
        this.L.removeAll();
        ListElement e = this.F.first();
        while (e != null) {
            this.L.add(((SingleMessageFilter) e.content()).Name);
            e = e.next();
        }
    }
}

class SingleFilterEdit extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    SingleMessageFilter MF;
    ListClass F;
    JTextField N, S, E, C;
    Checkbox BC;
    MessageFilterEdit MFE;
    boolean isnew;
    Checkbox CB;

    public SingleFilterEdit(MessageFilterEdit fr, ListClass f,
            SingleMessageFilter mf) {
        super(fr, Global.resourceString("Edit_Filter"), false);
        this.F = f;
        this.MF = mf;
        this.MFE = fr;
        if (this.MF == null) {
            this.isnew = true;
            this.MF = new SingleMessageFilter(Global.resourceString("Name"),
                    Global.resourceString("Starts_with"),
                    Global.resourceString("Ends_With"),
                    Global.resourceString("Contains"), false, false);
        } else {
            this.isnew = false;
        }
        this.CB = new Checkbox(Global.resourceString("Positive_Filter"));
        this.CB.setState(this.MF.Positive);
        this.CB.setFont(Global.SansSerif);
        this.add("North", this.CB);
        final JPanel p = new MyPanel();
        p.setLayout(new GridLayout(0, 2));
        p.add(new MyLabel(Global.resourceString("Name")));
        p.add(this.N = new FormTextField(this.MF.Name));
        p.add(new MyLabel(Global.resourceString("Starts_with")));
        p.add(this.S = new FormTextField(this.MF.Start));
        p.add(new MyLabel(Global.resourceString("Ends_With")));
        p.add(this.E = new FormTextField(this.MF.End));
        p.add(new MyLabel(Global.resourceString("Contains")));
        p.add(this.C = new FormTextField(this.MF.Contains));
        p.add(new MyLabel(Global.resourceString("Block_completely")));
        p.add(this.BC = new Checkbox());
        this.BC.setState(this.MF.BlockComplete);
        this.add("Center", p);
        final JPanel bp = new MyPanel();
        bp.add(new ButtonAction(this, Global.resourceString("OK")));
        bp.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.add("South", bp);
        Global.setpacked(this, "singlefilteredit", 300, 300);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "singlefilteredit");
        if (Global.resourceString("OK").equals(o)
                && !this.N.getText().equals("")) {
            this.MF.Name = this.N.getText();
            this.MF.Start = this.S.getText();
            this.MF.End = this.E.getText();
            this.MF.Contains = this.C.getText();
            this.MF.BlockComplete = this.BC.getState();
            this.MF.Positive = this.CB.getState();
            if (this.isnew) {
                this.F.append(new ListElement(this.MF));
            }
            this.MFE.updatelist();
        }
        this.setVisible(false);
        this.dispose();
    }

}

class SingleMessageFilter {
    public String Name, Start, End, Contains;
    public boolean BlockComplete, Positive;

    public SingleMessageFilter(String n, String s, String e, String c,
            boolean bc, boolean pos) {
        this.Name = n;
        this.Start = s;
        this.End = e;
        this.Contains = c;
        this.BlockComplete = bc;
        this.Positive = pos;
    }

    public boolean matches(String s) {
        if (!this.Start.equals("") && !s.startsWith(this.Start)) {
            return false;
        }
        if (!this.End.equals("") && !s.endsWith(this.End)) {
            return false;
        }
        if (!this.Contains.equals("") && s.indexOf(this.Contains) < 0) {
            return false;
        }
        return true;
    }

    public boolean positive() {
        return this.Positive;
    }
}
