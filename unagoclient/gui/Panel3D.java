package unagoclient.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel3D extends the Panel class with a 3D look.
 */

public class Panel3D extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Component C;

    /**
     * An empty 3D panel.
     */
    public Panel3D() {
        this.C = null;
    }

    /**
     * Adds the component to the panel. This component is resized to leave 5
     * pixel on each side.
     */
    public Panel3D(Component c) {
        this.C = c;
        this.add(this.C);
        if (rene.gui.Global.ControlBackground != null) {
            this.setBackground(rene.gui.Global.ControlBackground);
        }
    }

    public Panel3D(Component c, Color background) {
        this.C = c;
        this.add(this.C);
        this.setBackground(background);
    }

    @Override
    public void doLayout() {
        if (this.C != null) {
            this.C.setLocation(5, 5);
            this.C.setSize(this.getSize().width - 10,
                    this.getSize().height - 10);
            this.C.doLayout();
        } else {
            super.doLayout();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(this.getBackground());
        g.fill3DRect(0, 0, this.getSize().width - 1, this.getSize().height - 1,
                true);
        this.C.repaint();
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }
}
