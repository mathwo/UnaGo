package unagoclient.dialogs;

import unagoclient.Global;
import unagoclient.gui.ButtonAction;
import unagoclient.gui.CloseDialog;
import unagoclient.gui.MyLabel;
import unagoclient.gui.MyPanel;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * The Question dialog displays a question and yes/no buttons. It can be modal
 * or non-modal. If the dialog is used modal, there is no need to subclass it.
 * The result can be asked after the show method returns (which must be called
 * from the creating method). If the dialog is non-modal, it should be
 * subclassed and the tell method needs to be redefined to do something useful.
 */

public class Question extends CloseDialog implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Object O;
    Frame F;
    public boolean Result = false;

    /**
     * @param o
     *            an object to be passed to the tell method (may be null)
     * @param flag
     *            determines, if the dialog is modal or not
     */
    public Question(Frame f, String c, String title, Object o, boolean flag) {
        super(f, title, flag);
        this.F = f;
        final MyPanel pc = new MyPanel();
        final FlowLayout fl = new FlowLayout();
        pc.setLayout(fl);
        fl.setAlignment(FlowLayout.CENTER);
        pc.add(new MyLabel(" " + c + " "));
        this.add("Center", pc);
        final MyPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Yes")));
        p.add(new ButtonAction(this, Global.resourceString("No")));
        this.add("South", p);
        this.O = o;
        if (flag) {
            Global.setpacked(this, "question", 300, 150, f);
        } else {
            Global.setpacked(this, "question", 300, 150);
        }
        this.validate();
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "question");
        if (Global.resourceString("Yes").equals(o)) {
            this.tell(this, this.O, true);
            this.Result = true;
        } else if (Global.resourceString("No").equals(o)) {
            this.tell(this, this.O, false);
        }
        this.setVisible(false);
        this.dispose();
    }

    /**
     * to get the result of the question
     */
    public boolean result() {
        return this.Result;
    }

    /**
     * callback for non-modal dialogs
     */
    public void tell(Question q, Object o, boolean f) {
    }
}
