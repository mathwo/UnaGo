package unagoclient.gui;

import javax.swing.*;

/**
 * A TextField. The background and the font are set from global properties. The
 * class uses a TextFieldActionListener to listen to returns and notify the
 * DoActionListener passing the a string (name) to its doAction method.
 *
 * @see TextFieldActionListener
 */

public class TextFieldAction extends JTextField {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    TextFieldActionListener T;

    public TextFieldAction(DoActionListener t, String name) {
        // setBackground(Global.gray);
        // setFont(Global.SansSerif);
        this.T = new TextFieldActionListener(t, name);
        this.addActionListener(this.T);
    }

    public TextFieldAction(DoActionListener t, String name, int n) {
        super(n);
        // setBackground(Global.gray);
        // setFont(Global.SansSerif);
        this.T = new TextFieldActionListener(t, name);
        this.addActionListener(this.T);
    }

    public TextFieldAction(DoActionListener t, String name, String s) {
        super();
        // setBackground(Global.gray);
        // setFont(Global.SansSerif);
        this.T = new TextFieldActionListener(t, name);
        this.addActionListener(this.T);
        this.setText(s);
    }

    public TextFieldAction(DoActionListener t, String name, String s, int n) {
        super(s, n);
        // setFont(Global.SansSerif);
        // setBackground(Global.gray);
        this.T = new TextFieldActionListener(t, name);
        this.addActionListener(this.T);
    }
}
