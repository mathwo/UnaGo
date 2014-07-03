package rene.gui;

/**
 * A text field, which can transfer focus to the next text field, when return is
 * pressed.
 */

public class FormTextField extends MyTextField implements DoActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FormTextField(String s) {
        super();
        final TextFieldActionListener T = new TextFieldActionListener(this, "");
        this.addActionListener(T);
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
