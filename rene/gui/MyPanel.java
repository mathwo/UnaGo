package rene.gui;

import java.awt.*;

public class MyPanel extends Panel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MyPanel() {
        if (Global.ControlBackground != null) {
            this.setBackground(Global.ControlBackground);
        }
        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        this.getToolkit().sync();
    }
}
