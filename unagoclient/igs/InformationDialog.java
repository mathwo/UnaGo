package unagoclient.igs;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

/**
 * This dialog is opened by an InformatioDistributor.
 */

public class InformationDialog extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    PrintWriter Out;
    JTextField Answer;
    TextArea T;
    ConnectionFrame CF;
    InformationDistributor ID;

    public InformationDialog(ConnectionFrame cf, String m, PrintWriter out,
            InformationDistributor id) {
        super(Global.frame(), Global.resourceString("_Information_"), false);
        this.CF = cf;
        this.ID = id;
        this.add("North", new MyLabel(Global.resourceString("Information")));
        final JPanel pm = new MyPanel();
        pm.setLayout(new BorderLayout());
        pm.add("Center", this.T = new TextArea());
        this.T.setEditable(false);
        this.T.setFont(Global.Monospaced);
        this.T.setText(m);
        pm.add("South", this.Answer = new TextFieldAction(this, "Answer"));
        this.add("Center", pm);
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Close")));
        p.add(new ButtonAction(this, Global.resourceString("Send")));
        this.add("South", p);
        this.Out = out;
        Global.setwindow(this, "info", 300, 400);
        this.validate();
        this.ID.infodialog = this;
        this.setVisible(true);
    }

    public void append(String s) {
        this.T.append(s);
    }

    @Override
    public boolean close() {
        this.ID.infodialog = null;
        return true;
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "info");
        if (Global.resourceString("Close").equals(o)) {
            this.ID.infodialog = null;
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Send").equals(o)
                || "Answer".equals(o)) {
            if (!this.Answer.getText().equals("")) {
                this.CF.command(this.Answer.getText());
                this.Answer.setText("");
            }
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void paint(Graphics g) {
        if (this.ID.infodialog == null) {
            this.setVisible(false);
            this.dispose();
            return;
        }
        super.paint(g);
    }
}
