package rene.gui;

import java.awt.*;

/**
 * A Label with a midifyable Font.
 */

public class MyLabel extends Label {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MyLabel(String s) {
        super(s);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
    }

    public MyLabel(String s, int allign) {
        super(s, allign);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
    }

    /**
     * This is for Java 1.2 on Windows.
     */
    @Override
    public void paint(Graphics g) {
        final Container c = this.getParent();
        if (c != null) {
            this.setBackground(c.getBackground());
        }
        super.paint(g);
    }
}
