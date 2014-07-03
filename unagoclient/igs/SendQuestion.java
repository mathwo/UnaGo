package unagoclient.igs;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;

/**
 * This dialog is opened by IgsGoFrame, when the "Send" button is pressed.
 */

public class SendQuestion extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    IgsGoFrame F;
    JTextField T;
    Distributor Dis;

    public SendQuestion(IgsGoFrame f, Distributor dis) {
        super(f, Global.resourceString("Send"), false);
        this.F = f;
        this.add("North", new MyLabel(Global.resourceString("Message_")));
        this.add("Center", this.T = new GrayTextField(40));
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Kibitz")));
        if (dis instanceof PlayDistributor) {
            p.add(new ButtonAction(this, Global.resourceString("Say")));
        }
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.add("South", new Panel3D(p));
        Global.setpacked(this, "send", 200, 150);
        this.validate();
        Global.setpacked(this, "sendquestion", 300, 150, f);
        this.setVisible(true);
        this.Dis = dis;
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "send");
        if (Global.resourceString("Kibitz").equals(o)) {
            if (!this.T.getText().equals("")) {
                this.Dis.out("kibitz " + this.Dis.game() + " "
                        + this.T.getText());
                this.F.addComment("Kibitz: " + this.T.getText());
            }
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Say").equals(o)) {
            if (!this.T.getText().equals("")) {
                this.Dis.out("say " + this.T.getText());
                this.F.addComment("Say: " + this.T.getText());
            }
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Cancel").equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else {
            super.doAction(o);
        }
    }
}
