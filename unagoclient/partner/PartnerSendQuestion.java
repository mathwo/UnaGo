package unagoclient.partner;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;

/**
 * Displays a send dialog, when the partner presses "Send" in the GoFrame. The
 * message is appended to the PartnerFrame.
 */

public class PartnerSendQuestion extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    PartnerGoFrame F;
    JTextField T;
    PartnerFrame PF;

    public PartnerSendQuestion(PartnerGoFrame f, PartnerFrame pf) {
        super(f, Global.resourceString("Send"), false);
        this.F = f;
        this.PF = pf;
        this.add("North", new MyLabel(Global.resourceString("Message_")));
        this.add("Center", this.T = new GrayTextField(25));
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Say")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.add("South", new Panel3D(p));
        Global.setpacked(this, "partnersend", 200, 150);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "partnersend");
        if (Global.resourceString("Say").equals(o)) {
            if (!this.T.getText().equals("")) {
                this.PF.out(this.T.getText());
                this.F.addComment(Global.resourceString("Said__")
                        + this.T.getText());
            }
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString(Global.resourceString("Cancel"))
                .equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else {
            super.doAction(o);
        }
    }
}
