package unagoclient.gui;

import unagoclient.Global;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A dialog, which can be closed by clicking on the close window field (a cross
 * on the top right corner in Windows 95). This dialog does also simplify event
 * processing by implementing a DoActionListener. The "Close" resource is
 * reserved to close the dialog. The escape key will close the dialog too.
 */
public class CloseDialog extends JDialog implements WindowListener,
ActionListener, DoActionListener, KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CloseDialog(Frame f, String s, boolean modal) {
        super(f, "", modal);
        this.addWindowListener(this);
        this.setTitle(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.doAction(e.getActionCommand());
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

    /**
     * May be overwritten to ask for permission to close this dialog.
     *
     * @return true if the dialog is allowed to close.
     */
    public boolean close() {
        return true;
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Close").equals(o) && this.close()) {
            this.setVisible(false);
            this.dispose();
        } else if ("Close".equals(o) && this.close()) {
            this.setVisible(false);
            this.dispose();
        }
    }

    public boolean escape() {
        return this.close();
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && this.close()) {
            this.dispose();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
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
            this.setVisible(false);
            this.dispose();
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
