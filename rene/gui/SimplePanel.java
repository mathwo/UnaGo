package rene.gui;

import java.awt.*;

/**
 * A panel with two components side by side. The size of the components is
 * determined by the weights.
 */

public class SimplePanel extends MyPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Component C1, C2;
    double W;
    int IX = 0, IY = 0;

    public SimplePanel(Component c1, double w1, Component c2, double w2) {
        this.C1 = c1;
        this.C2 = c2;
        this.W = w1 / (w1 + w2);
        this.add(this.C1);
        this.add(this.C2);
    }

    @Override
    public void doLayout() {
        final int w = (int) (this.getSize().width * this.W - 3 * this.IX);
        this.C1.setSize(w, this.getSize().height - 2 * this.IY);
        this.C1.setLocation(this.IX, this.IY);
        this.C2.setSize(this.getSize().width - 3 * this.IX - w,
                this.getSize().height - 2 * this.IX);
        this.C2.setLocation(w + 2 * this.IX, this.IY);
        this.C1.doLayout();
        this.C2.doLayout();
    }

    @Override
    public Dimension getPreferredSize() {
        final Dimension d1 = this.C1.getPreferredSize(), d2 = this.C2
                .getPreferredSize();
        return new Dimension(d1.width + d2.width,
                Math.max(d1.height, d2.height));
    }

    public void setInsets(int x, int y) {
        this.IX = x;
        this.IY = y;
    }
}
