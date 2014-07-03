package rene.dialogs;

import rene.gui.*;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * This is a simple yes/no question. May be used as modal or non-modal dialog.
 * Modal Question dialogs must be overriden to do something sensible with the
 * tell method. In any case setVible(true) must be called in the calling
 * program.
 * <p>
 * The static YesString and NoString may be overriden for foreign languages.
 */

public class Question extends CloseDialog implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int Result;
    Object O;
    Frame F;
    public static int NO = 0, YES = 1, ABORT = -1;

    public Question(Frame f, String c, String title) {
        this(f, c, title, null, true, true);
    }

    public Question(Frame f, String c, String title, boolean abort) {
        this(f, c, title, null, abort, true);
    }

    public Question(Frame f, String c, String title, Object o, boolean flag) {
        this(f, c, title, o, true, flag);
    }

    public Question(Frame f, String c, String title, Object o, boolean abort,
            boolean flag) {
        super(f, title, flag);
        this.F = f;
        final Panel pc = new MyPanel();
        final FlowLayout fl = new FlowLayout();
        pc.setLayout(fl);
        fl.setAlignment(FlowLayout.CENTER);
        pc.add(new MyLabel(" " + c + " "));
        this.add("Center", pc);
        final Panel p = new MyPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT));
        p.add(new ButtonAction(this, Global.name("yes"), "Yes"));
        p.add(new ButtonAction(this, Global.name("no"), "No"));
        if (abort) {
            p.add(new ButtonAction(this, Global.name("abort"), "Abort"));
        }
        this.add("South", p);
        this.O = o;
        this.pack();
    }

    @Override
    public void doAction(String o) {
        if (o.equals("Yes")) {
            this.tell(this, this.O, Question.YES);
        } else if (o.equals("No")) {
            this.tell(this, this.O, Question.NO);
        } else if (o.equals("Abort")) {
            this.tell(this, this.O, Question.ABORT);
            this.Aborted = true;
        }
    }

    public int getResult() {
        return this.Result;
    }

    /**
     * Needs to be overriden for modal usage. Should dispose the dialog.
     */
    public void tell(Question q, Object o, int f) {
        this.Result = f;
        this.doclose();
    }

    /**
     * @return if the user pressed yes.
     */
    public boolean yes() {
        return this.Result == Question.YES;
    }
}
