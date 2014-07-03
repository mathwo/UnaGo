package unagoclient.gui;

import java.awt.*;

/**
 * A panel with two components side by side. The size of the components is
 * determined by the weights.
 */

public class SimplePanel extends Panel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GridBagLayout gridbag;

    public SimplePanel(Component L1, double w1, Component L2, double w2) {
        this.setLayout(this.gridbag = new GridBagLayout());
        this.add(L1);
        this.constrain(L1, 0, 0, 1, 1, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER, w1, 100);
        this.add(L2);
        this.constrain(L2, 1, 0, 1, 1, GridBagConstraints.BOTH,
                GridBagConstraints.CENTER, w2, 100);
    }

    // simplifies the gridbag layout
    void constrain(Component c, int gx, int gy, int gw, int gh, int fill,
            int anchor, double wx, double wy) {
        final GridBagConstraints g = new GridBagConstraints();
        g.gridx = gx;
        g.gridy = gy;
        g.gridwidth = gw;
        g.gridheight = gh;
        g.fill = fill;
        g.anchor = anchor;
        g.weightx = wx;
        g.weighty = wy;
        this.gridbag.setConstraints(c, g);
    }
}
