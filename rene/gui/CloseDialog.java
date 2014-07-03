package rene.gui;

import rene.dialogs.InfoDialog;

import java.awt.*;
import java.awt.event.*;

/**
 * A dialog, which can be closed by clicking on the close window field (a cross
 * on the top right corner in Windows 95), or by pressing the escape key.
 * <p>
 * Moreover, the dialog is a DoActionListener, which makes it possible to use
 * the simplified TextFieldAction etc.
 */

public class CloseDialog extends Dialog implements WindowListener,
ActionListener, DoActionListener, KeyListener, FocusListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    static public void center(Frame f, Dialog dialog) {
        final Dimension si = f.getSize(), d = dialog.getSize(), dscreen = f
                .getToolkit().getScreenSize();
        final Point lo = f.getLocation();
        int x = lo.x + si.width / 2 - d.width / 2;
        int y = lo.y + si.height / 2 - d.height / 2;
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
        dialog.setLocation(x, y);
    }

    boolean Dispose = true;
    public boolean Aborted = false;
    Frame F;

    public String Subject = "";

    public ActionEvent E;

    public CloseDialog(Frame f, String s, boolean modal) {
        super(f, s, modal);
        this.F = f;
        if (Global.ControlBackground != null) {
            this.setBackground(Global.ControlBackground);
        }
        this.addWindowListener(this);
        this.addKeyListener(this);
        this.addFocusListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.E = e;
        this.doAction(e.getActionCommand());
    }

    /**
     * To add a help button to children.
     *
     * @param p
     * @param subject
     */
    public void addHelp(Panel p, String subject) {
        p.add(new MyLabel(""));
        p.add(new ButtonAction(this, Global.name("help"), "Help"));
        this.Subject = subject;
    }

    public void center() {
        final Dimension d = this.getSize(), dscreen = this.getToolkit()
                .getScreenSize();
        this.setLocation((dscreen.width - d.width) / 2,
                (dscreen.height - d.height) / 2);
    }

    public void center(Frame f) {
        final Dimension si = f.getSize(), d = this.getSize(), dscreen = this
                .getToolkit().getScreenSize();
        final Point lo = f.getLocation();
        int x = lo.x + si.width / 2 - d.width / 2;
        int y = lo.y + si.height / 2 - d.height / 2;
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
     * @return true if the dialog is closed.
     */
    public boolean close() {
        return true;
    }

    @Override
    public void doAction(String o) {
        if ("Close".equals(o) && this.close()) {
            this.Aborted = true;
            this.doclose();
        } else if (o.equals("Help")) {
            this.showHelp();
        }
    }

    /**
     * Closes the dialog. This may be used in subclasses to do some action. Then
     * call super.doclose()
     */
    public void doclose() {
        this.setVisible(false);
        // Because of a bug in Linux Java 1.4.2 etc.
        // dispose in a separate thread.
        final Thread t = new Thread() {
            @Override
            public void run() {
                if (CloseDialog.this.Dispose) {
                    CloseDialog.this.dispose();
                }
            }
        };
        t.start();
    }

    /**
     * Calls close(), when the escape key is pressed.
     *
     * @return true if the dialog may close.
     */
    public boolean escape() {
        return this.close();
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

    public boolean isAborted() {
        return this.Aborted;
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && this.escape()) {
            this.doclose();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
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
    }

    /**
     * Note window size in Global.
     */
    public void noteSize(String name) {
        final Dimension d = this.getSize();
        Global.setParameter(name + ".w", d.width);
        Global.setParameter(name + ".h", d.height);
    }

    /**
     * This inihibits dispose(), when the dialog is closed.
     */
    public void setDispose(boolean flag) {
        this.Dispose = flag;
    }

    /**
     * Set window position and size.
     */
    public void setPosition(String name) {
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

    /**
     * Set window size.
     */
    public void setSize(String name) {
        if (!Global.haveParameter(name + ".w")) {
            this.pack();
        } else {
            final Dimension d = this.getSize();
            final int w = Global.getParameter(name + ".w", d.width);
            final int h = Global.getParameter(name + ".h", d.height);
            this.setSize(w, h);
        }
    }

    public void showHelp() {
        InfoDialog.Subject = this.Subject;
        new InfoDialog(this.F);
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
