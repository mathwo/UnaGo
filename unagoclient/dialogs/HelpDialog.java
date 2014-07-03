package unagoclient.dialogs;

import unagoclient.Global;
import unagoclient.gui.ButtonAction;
import unagoclient.gui.CloseDialog;
import unagoclient.gui.MyPanel;
import unagoclient.gui.Panel3D;
import rene.viewer.SystemViewer;
import rene.viewer.Viewer;

import java.awt.*;
import java.io.BufferedReader;

/**
 * The same as Help.java but as a dialog. This is for giving help in modal
 * dialogs.
 *
 * @see unagoclient.dialogs.Help
 */

public class HelpDialog extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Viewer V; // The viewer
    Frame F;

    /**
     * Display the help from subject.txt,Global.url()/subject.txt or from the
     * ressource /subject.txt.
     */
    public HelpDialog(Frame f, String subject) {
        super(f, Global.resourceString("Help"), true);
        this.F = f;
        this.V = rene.gui.Global.getParameter("systemviewer", false) ? new SystemViewer()
        : new Viewer();
        this.V.setFont(Global.Monospaced);
        try {
            BufferedReader in;
            String s;
            try {
                in = Global.getStream("unagoclient/helptexts/" + subject
                        + Global.resourceString("HELP_SUFFIX") + ".txt");
                s = in.readLine();
            } catch (final Exception e) {
                try {
                    in = Global.getStream(subject
                            + Global.resourceString("HELP_SUFFIX") + ".txt");
                    s = in.readLine();
                } catch (final Exception ex) {
                    in = Global.getStream("unagoclient/helptexts/" + subject
                            + ".txt");
                    s = in.readLine();
                }
            }
            while (s != null) {
                this.V.appendLine(s);
                s = in.readLine();
            }
            in.close();
        } catch (final Exception e) {
            new Message(Global.frame(),
                    Global.resourceString("Could_not_find_the_help_file_"));
            this.doclose();
            return;
        }
        this.display();
    }

    void display() {
        Global.setwindow(this, "help", 500, 400);
        this.add("Center", this.V);
        final MyPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Close")));
        this.add("South", new Panel3D(p));
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "help");
        super.doAction(o);
    }

    public void doclose() {
        this.setVisible(false);
        this.dispose();
    }
}
