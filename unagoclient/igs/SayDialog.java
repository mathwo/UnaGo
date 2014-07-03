package unagoclient.igs;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;

/**
 * The SayDialog is opened by the SayDistributor in response to a say.
 */

public class SayDialog extends CloseFrame implements CloseListener, KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    PrintWriter Out;
    JTextField Answer;
    TextArea T;
    SayDistributor SD;
    ConnectionFrame CF;

    public SayDialog(ConnectionFrame cf, SayDistributor sd, String m,
            PrintWriter out) {
        super(Global.resourceString("Say"));
        this.seticon("iunago.gif");
        cf.addCloseListener(this);
        this.SD = sd;
        this.CF = cf;
        this.add("North", new MyLabel(Global.resourceString("Opponent_said_")));
        final JPanel pm = new MyPanel();
        pm.setLayout(new BorderLayout());
        pm.add("Center", this.T = new TextArea());
        this.T.setFont(Global.Monospaced);
        this.T.setEditable(false);
        pm.add("South", this.Answer = new HistoryTextField(this, "Answer", 40));
        this.add("Center", pm);
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Close")));
        p.add(new ButtonAction(this, Global.resourceString("Send_Answer")));
        this.add("South", new Panel3D(p));
        this.Out = out;
        this.SD.MD = this;
        Global.setwindow(this, "say", 400, 200);
        this.validate();
        this.setVisible(true);
        this.T.setText(m);
        this.Answer.addKeyListener(this);
    }

    public void append(String s) {
        this.T.append("\n" + s);
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "say");
        if (Global.resourceString("Close").equals(o)) {
            this.close();
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Send_Answer").equals(o)
                || "Answer".equals(o)) {
            if (!this.Answer.getText().equals("")) {
                this.Out.println("say " + this.Answer.getText());
                this.CF.append("say: " + this.Answer.getText());
                this.Answer.setText("");
            }
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        this.SD.MD = null;
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
        if (this.SD.MD == null) {
            this.CF.removeCloseListener(this);
            this.setVisible(false);
            this.dispose();
            return;
        }
        super.paint(g);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        this.Answer.requestFocus();
    }
}
