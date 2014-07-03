package rene.dialogs;

import rene.gui.*;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * This is a simple warning dialog. May be used as modal or non-modal dialog.
 */

public class Warning extends CloseDialog implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public boolean Result;
    Frame F;

    public Warning(Frame f, String c, String title) {
        this(f, c, title, true, "");
    }

    public Warning(Frame f, String c, String title, boolean flag) {
        this(f, c, title, flag, "");
    }

    public Warning(Frame f, String c, String title, boolean flag, String help) {
        super(f, title, flag);
        this.F = f;
        final Panel pc = new MyPanel();
        final FlowLayout fl = new FlowLayout();
        pc.setLayout(fl);
        fl.setAlignment(FlowLayout.CENTER);
        pc.add(new MyLabel(" " + c + " "));
        this.add("Center", pc);
        final Panel p = new MyPanel();
        p.add(new ButtonAction(this, Global.name("close"), "Close"));
        if (help != null && !help.equals("")) {
            this.addHelp(p, help);
        }
        this.add("South", p);
        this.pack();
    }

    public Warning(Frame f, String c1, String c2, String title) {
        this(f, c1, c2, title, true, "");
    }

    public Warning(Frame f, String c1, String c2, String title, boolean flag) {
        this(f, c1, c2, title, flag, "");
    }

    public Warning(Frame f, String c1, String c2, String title, boolean flag,
            String help) {
        super(f, title, flag);
        this.F = f;
        final Panel pc = new MyPanel();
        pc.setLayout(new GridLayout(0, 1));
        pc.add(new MyLabel(" " + c1 + " "));
        pc.add(new MyLabel(" " + c2 + " "));
        this.add("Center", pc);
        final Panel p = new MyPanel();
        p.add(new ButtonAction(this, Global.name("close"), "Close"));
        if (help != null && !help.equals("")) {
            this.addHelp(p, help);
        }
        this.add("South", p);
        this.pack();
    }
}
