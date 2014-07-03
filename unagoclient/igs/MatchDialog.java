package unagoclient.igs;

import unagoclient.Global;
import unagoclient.gui.*;
import unagoclient.sound.UnaGoSound;
import rene.util.parser.StringParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;

/**
 * This dialog is opened, when a match request is received. It will not parse
 * this match request but still provide a mechanism for automatic accept or
 * decline.
 */

class MatchDialog extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    PrintWriter Out;
    JTextField Answer;
    JTextArea T;
    ConnectionFrame CF;
    InformationDistributor ID;
    String Accept, Decline, User = "";

    public MatchDialog(ConnectionFrame cf, String m, PrintWriter out,
            InformationDistributor id) {
        super(cf, Global.resourceString("Message"), false);
        this.CF = cf;
        this.ID = id;
        this.add("North", new MyLabel(Global.resourceString("Match_Request")));
        final JPanel pm = new MyPanel();
        pm.setLayout(new BorderLayout());
        pm.add("Center", this.T = new JTextArea());
        // T.setFont(Global.SansSerif);
        // if (Global.Background != null) T.setBackground(Global.Background);
        this.T.setEditable(false);
        this.T.setText(m);
        pm.add("South",
                this.Answer = new TextFieldAction(this, Global
                        .resourceString("Send_Command")));
        this.Answer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                final String s = Global.getFunctionKey(e.getKeyCode());
                if (s.equals("")) {
                    return;
                }
                MatchDialog.this.T.setText(s);
            }
        });
        this.add("Center", pm);
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Accept")));
        p.add(new ButtonAction(this, Global.resourceString("Decline")));
        p.add(new ButtonAction(this, Global.resourceString("Status")));
        p.add(new MyLabel(" "));
        p.add(new ButtonAction(this, Global.resourceString("Close")));
        p.add(new MyLabel(" "));
        p.add(new ButtonAction(this, Global.resourceString("Send_Command")));
        this.add("South", p);
        this.Out = out;
        this.validate();
        Global.setwindow(this, "matchdialog", 300, 400);
        final StringParser mpa = new StringParser(m);
        mpa.upto('<');
        mpa.skip("<");
        this.Accept = mpa.upto('>');
        mpa.skip(">");
        mpa.upto('<');
        mpa.skip("<");
        this.Decline = mpa.upto('>');
        mpa.skip(">");
        if (this.Accept.equals("")) {
            final StringParser mpa2 = new StringParser(m);
            mpa2.upto('\"');
            mpa2.skip("\"");
            this.Accept = mpa2.upto('\"');
            mpa2.skip("\"");
            mpa2.upto('\"');
            mpa2.skip("\"");
            this.Decline = mpa2.upto('\"');
            mpa2.skip("\"");
        }
        if (this.Decline.startsWith("decline ")) {
            this.User = this.Decline.substring(8);
        }
        if (rene.gui.Global.getParameter("friends", "").indexOf(this.User) >= 0) {
            this.T.append("\n" + Global.resourceString("Opponent_is_a_friend_"));
        }
        if (rene.gui.Global.getParameter("marked", "").indexOf(this.User) >= 0) {
            this.T.append("\n" + Global.resourceString("Opponent_is_marked_"));
        }
        final String a = this.CF.reply();
        if (!a.equals("")) {
            this.CF.append(Global.resourceString("Auto_reply_sent_to_")
                    + this.User);
            this.Out.println("tell " + this.User + " " + a);
            this.Out.println("decline " + this.User);
        } else {
            this.setVisible(true);
            UnaGoSound.play("game", "wip", true);
        }
    }

    public void append(String s) {
        this.T.append(s);
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "matchdialog");
        if (Global.resourceString("Close").equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Send_Command").equals(o)) {
            if (!this.Answer.getText().equals("")) {
                this.CF.command(this.Answer.getText());
                this.Answer.setText("");
            }
        } else if (Global.resourceString("Accept").equals(o)) {
            this.Out.println(this.Accept);
            this.CF.append("--> " + this.Accept);
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Decline").equals(o)) {
            this.Out.println(this.Decline);
            this.CF.append("--> " + this.Decline);
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Status").equals(o)) {
            if (this.User != null) {
                this.Out.println("stats " + this.User);
            } else {
                this.Answer.setText("stats ");
            }
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        this.T.requestFocus();
    }
}
