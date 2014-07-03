package unagoclient.dialogs;

import unagoclient.Global;
import unagoclient.gui.ButtonAction;
import unagoclient.gui.CloseFrame;
import unagoclient.gui.MyPanel;
import unagoclient.gui.Panel3D;
import rene.viewer.SystemViewer;
import rene.viewer.Viewer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * <p>
 * A dialog class for displaying help texts. The help texts are in ASCII text
 * files with ending .txt in the root resource directory. If the parameter
 * HELP_SUFFIX is in the properties file, it will appended for local language
 * help (such as about_de.txt).
 * <p>
 * The text will either be loaded from a file, from an URL or a ressource using
 * the getStream method of Global.
 *
 * @see unagoclient.Global#getStream
 */

public class Help extends CloseFrame implements Runnable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Viewer V; // The viewer

    /**
     * This constructor is used to get the about.txt file from our homeserver
     * (address is hard coded into the run method). A thread is used to wait for
     * connection.
     */
    public Help() {
        super(Global.resourceString("Help"));
        this.seticon("ihelp.gif");
        this.V = rene.gui.Global.getParameter("systemviewer", false) ? new SystemViewer()
        : new Viewer();
        this.V.setFont(Global.Monospaced);
        new Thread(this).start();
    }

    /**
     * Display the help from subject.txt,Global.url()/subject.txt or from the
     * ressource /subject.txt.
     */
    public Help(String subject) {
        super(Global.resourceString("Help"));
        this.seticon("ihelp.gif");
        this.V = rene.gui.Global.getParameter("systemviewer", false) ? new SystemViewer()
        : new Viewer();
        // V.setFont(Global.Monospaced);
        // V.setBackground(Global.gray);
        try {
            BufferedReader in;
            String s;
            try {
                in = Global.getEncodedStream("unagoclient/helptexts/" + subject
                        + Global.resourceString("HELP_SUFFIX") + ".txt");
                s = in.readLine();
            } catch (final Exception e) {
                try {
                    in = Global.getEncodedStream(subject
                            + Global.resourceString("HELP_SUFFIX") + ".txt");
                    s = in.readLine();
                } catch (final Exception ex) {
                    in = Global.getEncodedStream("unagoclient/helptexts/"
                            + subject + ".txt");
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

    @Override
    public void run() {
        try {
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(new DataInputStream(
                    new URL("http://sites.google.com/site/unagoban/home/about-txt")
                    .openStream())));
            while (true) {
                final String s = in.readLine();
                if (s == null) {
                    break;
                }
                this.V.appendLine(s);
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
}
