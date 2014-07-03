package unagoclient.gui;

import javax.swing.*;

/**
 * A label in a specified font.
 */

public class MyLabel extends JLabel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MyLabel(String s) {
        super(s);
        // setFont(Global.SansSerif);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

}
