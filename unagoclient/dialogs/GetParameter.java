package unagoclient.dialogs;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;
import java.awt.*;

/**
 * A general dialog to get a string parameter. Contains a simple text field. The
 * modal flag is handled as in the Question class. Again, show has to be called
 * externally.
 *
 * @see unagoclient.dialogs.Question
 */

public class GetParameter extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public boolean Result;
    Object O;
    Frame F;
    JTextField T;
    String Helpfile;
    protected MyLabel Prompt;

    public GetParameter(Frame f, String c, String title, Object o,
            boolean modalflag) {
        this(f, c, title, o, modalflag, "");
    }

    public GetParameter(Frame f, String c, String title, Object o,
            boolean modalflag, String help) {
        super(f, title, modalflag);
        this.F = f;
        this.Helpfile = help;

        final JPanel n = new MyPanel();
        n.setLayout(new BorderLayout());
        n.add("North", this.Prompt = new MyLabel(c));
        n.add("Center", this.T = new TextFieldAction(this, "Input", 25));
        this.add("Center", new Panel3D(n));

        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("OK")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        if (!help.equals("")) {
            p.add(new MyLabel(" "));
            p.add(new ButtonAction(this, Global.resourceString("Help")));
        }
        this.add("South", new Panel3D(p));

        this.O = o;

        if (modalflag) {
            Global.setpacked(this, "getparameter", 300, 150, f);
        } else {
            Global.setpacked(this, "getparameter", 300, 150);
        }

        this.validate();
        this.T.addKeyListener(this);
    }

    public GetParameter(Frame f, String c, String title, Object o, char echo,
            boolean modalflag) {
        this(f, c, title, o, echo, modalflag, "");
    }

    public GetParameter(Frame f, String c, String title, Object o, char echo,
            boolean modalflag, String help) {
        super(f, title, modalflag);
        this.F = f;
        this.Helpfile = help;
        final JPanel n = new MyPanel();
        n.setLayout(new BorderLayout());
        n.add("North", new MyLabel(c));
        n.add("Center", this.T = new JPasswordField("", 25));
        this.add("Center", new Panel3D(n));
        this.add("North", new MyLabel(c));
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("OK")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        if (!help.equals("")) {
            p.add(new MyLabel(" "));
            p.add(new ButtonAction(this, Global.resourceString("Help")));
        }
        this.add("South", new Panel3D(p));
        if (modalflag) {
            Global.setpacked(this, "getparameter", 300, 150, f);
        } else {
            Global.setpacked(this, "getparameter", 300, 150);
        }
        this.validate();
        this.T.addKeyListener(this);
        this.O = o;
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "getparameter");
        if (Global.resourceString("Cancel").equals(o)) {
            this.close();
            this.setVisible(false);
            this.dispose();
        } else if (o.equals("Input") || o.equals(Global.resourceString("OK"))) {
            if (this.tell(this.O, this.T.getText())) {
                this.setVisible(false);
                this.dispose();
            }
        } else if (o.equals(Global.resourceString("Help"))) {
            new HelpDialog(this.F, this.Helpfile);
        } else {
            super.doAction(o);
        }
    }

    /**
     * This is to be used in the modal case after the show method returns.
     *
     * @return the text you asked for
     */
    public String getText() {
        return this.T.getText();
    }

    public void set(String s) {
        this.T.setText(s);
    }

    /**
     * This is called, when the dialog is finished with a valid entry (User
     * pressed OK).
     */
    public boolean tell(Object o, String S) {
        return true;
    }
}
