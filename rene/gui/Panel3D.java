package rene.gui;

import java.awt.*;

/**
 * Panel3D extends the Panel class with a 3D look.
 */

public class Panel3D extends Panel implements LayoutManager {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
        final CloseFrame f = new CloseFrame("Test");
        f.add("Center", new Panel3D(new MyPanel()));
        f.setSize(400, 400);
        f.setLocation(100, 100);
        f.setVisible(true);
    }

    Component C;

    /**
     * Adds the component to the panel. This component is resized to leave 5
     * pixel on each side.
     */
    public Panel3D(Component c) {
        this.C = c;
        this.setLayout(this);
        this.add(this.C);
        this.setBackground(this.C.getBackground());
    }

    public Panel3D(Component c, Color background) {
        this.C = c;
        this.setLayout(this);
        this.add(this.C);
        this.setBackground(background);
    }

    @Override
    public void addLayoutComponent(String arg0, Component arg1) {
        this.C = arg1;
    }

    @Override
    public Dimension getMinimumSize() {
        return this.getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        if (this.C != null) {
            return new Dimension(this.C.getPreferredSize().width + 10,
                    this.C.getPreferredSize().height + 10);
        }
        return new Dimension(10, 10);
    }

    @Override
    public void layoutContainer(Container arg0) {
        if (this.C == null) {
            return;
        }
        this.C.setLocation(5, 5);
        this.C.setSize(this.getSize().width - 10, this.getSize().height - 10);
    }

    @Override
    public Dimension minimumLayoutSize(Container arg0) {
        if (this.C != null) {
            return new Dimension(this.C.getMinimumSize().width + 10,
                    this.C.getMinimumSize().height + 10);
        }
        return new Dimension(10, 10);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(this.getBackground());
        if (this.getSize().width > 0 && this.getSize().height > 0) {
            g.fill3DRect(0, 0, this.getSize().width, this.getSize().height,
                    true);
            // C.repaint(); // probably not necessary, but Mac OSX bug ?!?
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container arg0) {
        if (this.C != null) {
            return new Dimension(this.C.getPreferredSize().width + 10,
                    this.C.getPreferredSize().height + 10);
        }
        return new Dimension(10, 10);
    }

    @Override
    public void removeLayoutComponent(Component arg0) {
        this.C = null;
    }
}
