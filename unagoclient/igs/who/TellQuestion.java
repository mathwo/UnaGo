package unagoclient.igs.who;

import unagoclient.Global;
import unagoclient.gui.*;
import unagoclient.igs.ConnectionFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

/**
 * Ask to tell the chosen user something, using the IGS tell command.
 */

public class TellQuestion extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ConnectionFrame F;
    JTextField T;
    JTextField User;

    /**
     * @param f
     *            the connection frame, which is used to send the output to IGS.
     * @param user
     *            the user name of the person, which is to receive the message.
     */
    public TellQuestion(Frame fr, ConnectionFrame f, String user) {
        super(fr, Global.resourceString("Tell"), false);
        this.F = f;
        this.add("North",
                new SimplePanel(new MyLabel(Global.resourceString("To_")), 0.4,
                        this.User = new GrayTextField(user), 0.6));
        this.add(
                "Center",
                this.T = new TextFieldAction(this, Global
                        .resourceString("Tell")));
        this.T.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                final String s = Global.getFunctionKey(e.getKeyCode());
                if (s.equals("")) {
                    return;
                }
                TellQuestion.this.T.setText(s);
            }
        });
        final MyPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Tell")));
        p.add(new ButtonAction(this, Global.resourceString("Message")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.add("South", new Panel3D(p));
        Global.setpacked(this, "tell", 200, 150, f);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "tell");
        if (Global.resourceString("Tell").equals(o)) {
            if (!this.T.getText().equals("")) {
                this.F.out("tell " + this.User.getText() + " "
                        + this.T.getText());
            }
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Message").equals(o)) {
            if (!this.T.getText().equals("")) {
                this.F.out("message " + this.User.getText() + " "
                        + this.T.getText());
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

    @Override
    public void windowOpened(WindowEvent e) {
        this.T.requestFocus();
    }
}
