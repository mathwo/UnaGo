package unagoclient.dialogs;

import unagoclient.Global;
import unagoclient.gui.ButtonAction;
import unagoclient.gui.CloseDialog;
import unagoclient.gui.MyPanel;
import unagoclient.gui.Panel3D;
import rene.viewer.SystemViewer;
import rene.viewer.Viewer;

import java.awt.*;

/**
 * This class is used to display messages from the go server. The message can
 * have several lines.
 */

public class Message extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Viewer T;

    public Message(Frame f, String m) {
        super(f, Global.resourceString("Message"), false);
        this.add(
                "Center",
                this.T = rene.gui.Global.getParameter("systemviewer", false) ? new SystemViewer()
                : new Viewer());
        this.T.setFont(Global.Monospaced);
        final MyPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("OK")));
        this.add("South", new Panel3D(p));
        Global.setwindow(this, "message", 300, 150);
        this.validate();
        this.setVisible(true);
        this.T.setText(m);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "message");
        if (Global.resourceString("OK").equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else {
            super.doAction(o);
        }
    }
}
