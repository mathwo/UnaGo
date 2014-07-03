package unagoclient.board;

import java.awt.*;

/**
 * This panel contains two panels aside. The left panel is kept square.
 */

class CommentNavigationPanel extends Panel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Component C1, C2;

    public CommentNavigationPanel(Component c1, Component c2) {
        this.C1 = c1;
        this.C2 = c2;
        this.add(this.C1);
        this.add(this.C2);
    }

    @Override
    public void doLayout() {
        final int w = this.getSize().width, h = this.getSize().height;
        int h2 = w;
        if (h2 > h / 2) {
            h2 = h / 2;
        }
        this.C1.setSize(w, h - h2);
        this.C1.setLocation(0, 0);
        this.C2.setSize(w, h2);
        this.C2.setLocation(0, h - h2);
        this.C1.doLayout();
        this.C2.doLayout();
    }
}
