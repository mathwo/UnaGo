package unagoclient.gui;

import unagoclient.Global;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * A Frame, which can be closed with the close button in the window. Moreover,
 * event handling is simplified with the DoActionListnener interface. There is
 * also a method for setting the icon of this window.
 */
public class CloseFrame extends Frame implements WindowListener,
ActionListener, DoActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    ListClass L;

    // the icon things
    static Hashtable Icons = new Hashtable();

    public CloseFrame(String s) {
        super("");
        this.L = new ListClass();
        this.addWindowListener(this);
        this.setTitle(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.doAction(e.getActionCommand());
    }

    public void addCloseListener(CloseListener cl) {
        this.L.append(new ListElement(cl));
    }

    public boolean close() {
        return true;
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Close").equals(o) && this.close()) {
            this.doclose();
        }
    }

    public void doclose() {
        if (rene.gui.Global.getParameter("menuclose", true)) {
            this.setMenuBar(null);
        }
        this.setVisible(false);
        this.dispose();
    }

    public void inform() {
        ListElement e = this.L.first();
        while (e != null) {
            try {
                ((CloseListener) e.content()).isClosed();
            } catch (final Exception ex) {
            }
            e = e.next();
        }
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }

    public void removeCloseListener(CloseListener cl) {
        ListElement e = this.L.first();
        while (e != null) {
            final CloseListener l = (CloseListener) e.content();
            if (l == cl) {
                this.L.remove(e);
                break;
            }
            e = e.next();
        }
    }

    public void seticon(String file) {
        try {
            final Object o = CloseFrame.Icons.get(file);
            if (o == null) {
                Image i;
                final InputStream in = this.getClass().getResourceAsStream(
                        "/unagoclient/gifs/" + file);
                int pos = 0;
                int n = in.available();
                final byte b[] = new byte[20000];
                while (n > 0) {
                    final int k = in.read(b, pos, n);
                    if (k < 0) {
                        break;
                    }
                    pos += k;
                    n = in.available();
                }
                i = Toolkit.getDefaultToolkit().createImage(b, 0, pos);
                final MediaTracker T = new MediaTracker(this);
                T.addImage(i, 0);
                T.waitForAll();
                CloseFrame.Icons.put(file, i);
                this.setIconImage(i);
            } else {
                this.setIconImage((Image) o);
            }
        } catch (final Exception e) {
        }
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (this.close()) {
            this.doclose();
        }
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }
}
