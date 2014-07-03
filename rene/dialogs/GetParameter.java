package rene.dialogs;

import rene.gui.*;

import java.awt.*;

/**
 * A simple dialog to scan for a parameter.
 */

public class GetParameter extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    HistoryTextField Input;
    static public int InputLength = 32;
    String Result = "";
    boolean Aborted = true;

    public GetParameter(Frame f, String title, String prompt, String action) {
        this(f, title, prompt, action, false);
    }

    public GetParameter(Frame f, String title, String prompt, String action,
            boolean help) {
        super(f, title, true);
        this.Input = new HistoryTextField(this, "Action",
                GetParameter.InputLength);
        this.Input.addKeyListener(this);
        this.init(f, title, prompt, action, help);
    }

    public GetParameter(Frame f, String title, String prompt, String action,
            String subject) {
        super(f, title, true);
        this.Subject = subject;
        this.Input = new HistoryTextField(this, "Action",
                GetParameter.InputLength);
        this.Input.addKeyListener(this);
        this.init(f, title, prompt, action, true);
    }

    public boolean aborted() {
        return this.Aborted;
    }

    @Override
    public void doAction(String o) {
        if (o.equals("Abort")) {
            this.doclose();
        } else if (o.equals("Action")) {
            this.Result = this.Input.getText();
            this.doclose();
            this.Aborted = false;
        } else {
            super.doAction(o);
        }
    }

    public String getResult() {
        return this.Result;
    }

    void init(Frame f, String title, String prompt, String action, boolean help) {
        this.setLayout(new BorderLayout());
        final Panel center = new MyPanel();
        center.setLayout(new GridLayout(0, 1));
        center.add(new MyLabel(prompt));
        center.add(this.Input);
        this.add("Center", new Panel3D(center));
        final Panel south = new MyPanel();
        south.setLayout(new FlowLayout(FlowLayout.RIGHT));
        south.add(new ButtonAction(this, action, "Action"));
        south.add(new ButtonAction(this, Global.name("abort"), "Abort"));
        if (help) {
            south.add(new ButtonAction(this, Global.name("help", "Help"),
                    "Help"));
        }
        this.add("South", new Panel3D(south));
        this.pack();
    }

    public void set(String s) {
        this.Input.setText(s);
    }

}
