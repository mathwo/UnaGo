package unagoclient.igs;

import unagoclient.Global;
import unagoclient.dialogs.Help;
import unagoclient.gui.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;

/**
 * Contains a text area and a text field for anwers.
 *
 * @see unagoclient.igs.ChannelDistributor
 */
public class ChannelDialog extends CloseFrame implements CloseListener,
KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    PrintWriter Out;
    TextArea T;
    ConnectionFrame CF;
    ChannelDistributor MDis;
    int N;
    HistoryTextField Answer;

    public ChannelDialog(ConnectionFrame cf, PrintWriter out, int n,
            ChannelDistributor mdis) {
        super(Global.resourceString("Channel"));
        this.CF = cf;
        this.MDis = mdis;
        this.N = n;
        this.CF.addCloseListener(this);
        this.setLayout(new BorderLayout());
        final MenuBar M = new MenuBar();
        final Menu help = new MyMenu(Global.resourceString("Help"));
        help.add(new MenuItem(Global.resourceString("Channels")));
        M.setHelpMenu(help);
        this.add("North", new MyLabel(Global.resourceString("Channel_") + n));
        final MyPanel pm = new MyPanel();
        pm.setLayout(new BorderLayout());
        pm.add("Center", this.T = new MyTextArea("", 0, 0,
                TextArea.SCROLLBARS_VERTICAL_ONLY));
        pm.add("South", this.Answer = new HistoryTextField(this, "Answer"));
        this.add("Center", pm);
        final MyPanel pb = new MyPanel();
        pb.add(new ButtonAction(this, Global.resourceString("Close")));
        this.add("South", new Panel3D(pb));
        this.Out = out;
        this.seticon("iwho.gif");
        Global.setwindow(this, "channeldialog", 500, 400);
        this.validate();
        this.setVisible(true);
    }

    public void append(String s) {
        this.T.append(s + "\n");
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Channels").equals(o)) {
            new Help("channels");
        } else if ("Answer".equals(o)) {
            if (!this.Answer.getText().equals("")) {
                this.Out.println("; " + this.Answer.getText());
                this.T.append("---> " + this.Answer.getText() + "\n");
                this.Answer.setText("");
            }
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        this.MDis.CD = null;
        Global.notewindow(this, "channeldialog");
        super.doclose();
    }

    public boolean escape() {
        return false;
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
        this.Answer.requestFocus();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
