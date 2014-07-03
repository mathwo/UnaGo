package unagoclient.gui;

import javax.swing.*;

/**
 * A TextField with a background and font as specified in the Global class.
 */

public class GrayTextField extends JTextField {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public GrayTextField() {
        super(25);
        // setFont(Global.SansSerif);
    }

    public GrayTextField(int n) {
        super(n);
        // setFont(Global.SansSerif);
    }

    public GrayTextField(String s) {
        super(s, 25);
        // setFont(Global.SansSerif);
    }
}
