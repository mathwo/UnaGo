package unagoclient.igs;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;

/**
 * This dialog may be opened by the MessageDistributor.
 */

class MessageDialog extends CloseFrame implements CloseListener, KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    PrintWriter Out;
    JTextField Answer;
    TextArea T;
    ConnectionFrame CF;
    MessageDistributor MDis;
    Choice UserChoice;

    public MessageDialog(ConnectionFrame cf, String user, String m,
            PrintWriter out, MessageDistributor mdis) {
        super(Global.resourceString("_Message_"));
        this.seticon("iwho.gif");
        cf.addCloseListener(this);
        this.CF = cf;
        this.MDis = mdis;
        // CF.append("From "+user+": "+m);
        final JPanel pm = new MyPanel();
        pm.setLayout(new BorderLayout());
        pm.add("Center", this.T = new TextArea("", 0, 0,
                TextArea.SCROLLBARS_VERTICAL_ONLY));
        this.T.setFont(Global.Monospaced);
        this.T.setEditable(false);
        this.UserChoice = new Choice();
        this.UserChoice.setFont(Global.SansSerif);
        this.UserChoice.add(user);
        this.Answer = new HistoryTextField(this,
                Global.resourceString("Answer"));
        this.Answer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                final String s = Global.getFunctionKey(e.getKeyCode());
                if (s.equals("")) {
                    return;
                }
                MessageDialog.this.T.setText(s);
            }
        });
        final SimplePanel AnswerPanel = new SimplePanel(this.UserChoice, 1e-1,
                this.Answer, 9e-1);
        pm.add("South", AnswerPanel);
        this.add("Center", pm);
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Close")));
        p.add(new ButtonAction(this, Global.resourceString("Answer")));
        p.add(new ButtonAction(this, Global.resourceString("Send_as_Command")));
        this.add("South", new Panel3D(p));
        this.Out = out;
        this.MDis.MD = this;
        Global.setwindow(this, "messagedialog", 400, 200);
        this.validate();
        this.setVisible(true);
        this.T.setText("<<<< " + user + "\n");
        this.T.append(m);
        this.Answer.addKeyListener(this);
    }

    public void append(String user, String s) {
        this.T.append("\n<<<< " + user + "\n" + s);
        final int n = this.UserChoice.getItemCount();
        final String a = this.CF.reply();
        if (!a.equals("")) {
            this.T.append("\n" + Global.resourceString("_____Auto_reply_to_")
                    + user + "\n");
            this.T.append(a);
            this.CF.append(Global.resourceString("Auto_reply_sent_to_") + user);
            this.Out.println("tell " + user + " " + a);
        }
        for (int i = 0; i < n; i++) {
            if (this.UserChoice.getItem(i).equals(user)) {
                return;
            }
        }
        this.UserChoice.add(user);
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "messagedialog");
        if (Global.resourceString("Close").equals(o)) {
            this.doclose();
        } else if (Global.resourceString("Answer").equals(o)) {
            final String User = this.UserChoice.getSelectedItem();
            if (User != null && !this.Answer.getText().equals("")) {
                this.Out.println("tell " + User + " " + this.Answer.getText());
                this.CF.append(Global.resourceString("Answer_") + User + ": "
                        + this.Answer.getText());
                this.T.append("\n" + Global.resourceString("_____Answer_to_")
                        + User + "\n");
                this.T.append(this.Answer.getText());
                this.Answer.setText("");
            }
        } else if (Global.resourceString("Send_as_Command").equals(o)) {
            if (!this.Answer.getText().equals("")) {
                this.CF.command(this.Answer.getText());
                this.Answer.setText("");
            }
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        this.MDis.MD = null;
        this.CF.removeCloseListener(this);
        super.doclose();
    }

    @Override
    public void isClosed() {
        this.doclose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && this.close()) {
            this.doclose();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        final String s = Global.getFunctionKey(e.getKeyCode());
        if (s.equals("")) {
            return;
        }
        this.Answer.setText(s);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void paint(Graphics g) {
        if (this.MDis.MD == null) {
            this.doclose();
            return;
        }
        super.paint(g);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        this.Answer.requestFocus();
    }
}
