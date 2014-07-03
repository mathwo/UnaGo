package unagoclient.gui;

import unagoclient.Global;

/**
 * A text field, which can transfer focus to the next text field, when return is
 * pressed.
 */

public class FormTextField extends GrayTextField implements DoActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FormTextField(String s) {
        super();
        final TextFieldActionListener T = new TextFieldActionListener(this, "");
        this.addActionListener(T);
        this.setFont(Global.SansSerif);
        this.setText(s);
    }

    @Override
    public void doAction(String o) {
        this.transferFocus();
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }
}