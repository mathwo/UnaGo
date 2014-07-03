package rene.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * A Frame, which can be closed with the close button in the window frame.
 * <p>
 * This frame may set an icon. The icon file must be a GIF with 16x16 dots in
 * 256 colors. We use the simple method, which does not work in the Netscape
 * browser.
 * <p>
 * This Frame is a DoActionListener. Thus it is possible to use TextFieldAction
 * etc. in it. Override doAction(String) and itemAction(String,boolean) to react
 * on events.
 * <p>
 * Sometimes the Frame wants to set the focus to a certain text field. To
 * support this, override focusGained().
 */

public class CloseFrame extends Frame implements WindowListener,
ActionListener, DoActionListener, FocusListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // the icon things
    static Hashtable Icons = new Hashtable();

    public CloseFrame() {
        this.addWindowListener(this);
        this.addFocusListener(this);
    }

    public CloseFrame(String s) {
        super(s);
        this.addWindowListener(this);
        this.addFocusListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.doAction(e.getActionCommand());
    }

    public void center() {
        final Dimension dscreen = this.getToolkit().getScreenSize();
        final Dimension d = this.getSize();
        this.setLocation((dscreen.width - d.width) / 2,
                (dscreen.height - d.height) / 2);
    }

    public void centerOut(Frame f) {
        final Dimension si = f.getSize(), d = this.getSize(), dscreen = this
                .getToolkit().getScreenSize();
        final Point lo = f.getLocation();
        int x = lo.x + si.width - this.getSize().width + 20;
        int y = lo.y + si.height / 2 + 40;
        if (x + d.width > dscreen.width) {
            x = dscreen.width - d.width - 10;
        }
        if (x < 10) {
            x = 10;
        }
        if (y + d.height > dscreen.height) {
            y = dscreen.height - d.height - 10;
        }
        if (y < 10) {
            y = 10;
        }
        this.setLocation(x, y);
    }

    /**
     * @return if the frame should close now.
     */
    public boolean close() {
        return true;
    }

    @Override
    public void doAction(String o) {
        if ("Close".equals(o) && this.close()) {
            this.doclose();
        }
    }

    /**
     * Closes the frame. Override, if necessary, and call super.doclose().
     */
    public void doclose() {
        this.setMenuBar(null); // for Linux ?!
        this.setVisible(false);
        // Because of a bug in Linux Java 1.4.2 etc.
        // dispose in a separate thread.
        final Thread t = new Thread() {
            @Override
            public void run() {
                CloseFrame.this.dispose();
            }
        };
        t.start();
    }

    /**
     * Override to set the focus somewhere.
     */
    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    public void front() {
        new ToFrontDelay(this);
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }

    /**
     * Note window position in Global.
     */
    public void notePosition(String name) {
        final Point l = this.getLocation();
        final Dimension d = this.getSize();
        Global.setParameter(name + ".x", l.x);
        Global.setParameter(name + ".y", l.y);
        Global.setParameter(name + ".w", d.width);
        if (d.height - Global.getParameter(name + ".h", 0) != 19) {
            // works around a bug in Windows
            Global.setParameter(name + ".h", d.height);
        }
        if ((this.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0) {
            Global.setParameter(name + ".maximized", true);
        } else {
            Global.removeParameter(name + ".maximized");
        }
    }

    public void seticon(String file) {
        try {
            final Object o = CloseFrame.Icons.get(file);
            if (o == null) {
                Image i;
                final InputStream in = this.getClass().getResourceAsStream(
                        "/" + file);
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

    /**
     * Set window position and size.
     */
    public void setPosition(String name) {
        if (Global.getParameter(name + ".maximized", false)) {
            this.setExtendedState(Frame.MAXIMIZED_BOTH);
            return;
        }
        final Point l = this.getLocation();
        final Dimension d = this.getSize();
        final Dimension dscreen = this.getToolkit().getScreenSize();
        int x = Global.getParameter(name + ".x", l.x);
        int y = Global.getParameter(name + ".y", l.y);
        int w = Global.getParameter(name + ".w", d.width);
        int h = Global.getParameter(name + ".h", d.height);
        if (w > dscreen.width) {
            w = dscreen.width;
        }
        if (h > dscreen.height) {
            h = dscreen.height;
        }
        if (x < 0) {
            x = 0;
        }
        if (x + w > dscreen.width) {
            x = dscreen.width - w;
        }
        if (y < 0) {
            y = 0;
        }
        if (y + h > dscreen.height) {
            y = dscreen.height - h;
        }
        this.setLocation(x, y);
        this.setSize(w, h);
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

class ToFrontDelay extends Thread {
    CloseFrame F;
    final int Delay = 500;

    public ToFrontDelay(CloseFrame f) {
        this.F = f;
        this.start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.Delay);
        } catch (final Exception e) {
        }
        this.F.toFront();
        this.F.requestFocus();
    }
}
