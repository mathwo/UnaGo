package unagoclient.board;

import javax.swing.*;
import java.awt.*;

/**
 * This panel contains two panels aside. The left panel is kept square.
 */

class BoardCommentPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Component C1, C2;
    Board B;

    public BoardCommentPanel(Component c1, Component c2, Board b) {
        this.C1 = c1;
        this.C2 = c2;
        this.B = b;
        this.add(this.C1);
        this.add(this.C2);
    }

    @Override
    public void doLayout() {
        this.C1.setSize(this.getSize().width, this.getSize().height);
        this.C1.doLayout();
        this.C1.setSize(this.B.getSize().height, this.getSize().height);
        this.C1.setLocation(0, 0);
        this.C2.setSize(this.getSize().width - this.B.getSize().height,
                this.getSize().height);
        this.C2.setLocation(this.B.getSize().height, 0);
        this.C1.doLayout();
        this.C2.doLayout();
    }
}
