package rene.gui;

import java.awt.*;

/**
 * Panel3D extends the Panel class with a 3D look.
 */

public class ShadowPanel extends Panel implements LayoutManager {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
        final CloseFrame f = new CloseFrame("Test");
        final MyPanel p = new MyPanel();
        // p.setBackground(Color.green);
        f.add("Center", new ShadowPanel(p, f.getBackground()));
        f.setSize(400, 400);
        f.setLocation(100, 100);
        f.setVisible(true);
    }

    Component C;

    public int Boundary = 6;

    /**
     * Adds the component to the panel. This component is resized to leave 5
     * pixel on each side.
     */
    public ShadowPanel(Component c) {
        this.C = c;
        this.setLayout(this);
        this.add(this.C);
        this.setBackground(this.C.getBackground());
    }

    public ShadowPanel(Component c, Color background) {
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
            return new Dimension(this.C.getPreferredSize().width + 2
                    * this.Boundary, this.C.getPreferredSize().height + 2
                    * this.Boundary);
        }
        return new Dimension(10, 10);
    }

    @Override
    public void layoutContainer(Container arg0) {
        if (this.C == null) {
            return;
        }
        this.C.setLocation(this.Boundary, this.Boundary);
        this.C.setSize(this.getSize().width - 2 * this.Boundary,
                this.getSize().height - 2 * this.Boundary);
    }

    @Override
    public Dimension minimumLayoutSize(Container arg0) {
        if (this.C != null) {
            return new Dimension(this.C.getMinimumSize().width + 2
                    * this.Boundary, this.C.getMinimumSize().height + 2
                    * this.Boundary);
        }
        return new Dimension(10, 10);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(this.getBackground());
        if (this.getSize().width > 0 && this.getSize().height > 0) {
            g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        }
        final int k = this.Boundary / 3;
        final Color cb = this.getBackground();
        final double red = cb.getRed() / 255.0, green = cb.getGreen() / 255.0, blue = cb
                .getBlue() / 255.0;
        for (int i = 0; i <= 2 * k; i++) {
            double x = (double) i / (2 * k);
            x = 1.0 - 0.5 * x;
            g.setColor(new Color((float) (red * x), (float) (green * x),
                    (float) (blue * x)));
            g.fillRect(i + 2 * k, i + 2 * k, this.getSize().width - (i + 2 * k)
                    - i, this.getSize().height - (i + 2 * k) - i);
        }
        // C.repaint(); // probably not necessary, but Mac OSX bug ?!?
    }

    @Override
    public Dimension preferredLayoutSize(Container arg0) {
        if (this.C != null) {
            return new Dimension(this.C.getPreferredSize().width + 2
                    * this.Boundary, this.C.getPreferredSize().height + 2
                    * this.Boundary);
        }
        return new Dimension(10, 10);
    }

    @Override
    public void removeLayoutComponent(Component arg0) {
        this.C = null;
    }
}
