package rene.gui;

/**
 * A TextField, which holds an integer number with minimal and maximal range.
 */

public class IntField extends TextFieldAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IntField(DoActionListener l, String name, int v) {
        super(l, name, "" + v);
    }

    public IntField(DoActionListener l, String name, int v, int cols) {
        super(l, name, "" + v, cols);
    }

    public void set(int v) {
        this.setText("" + v);
    }

    public boolean valid() {
        try {
            Integer.parseInt(this.getText());
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    public int value() {
        try {
            return Integer.parseInt(this.getText());
        } catch (final NumberFormatException e) {
            this.setText("" + 0);
            return 0;
        }
    }

    public int value(int min, int max) {
        int n;
        try {
            n = Integer.parseInt(this.getText());
        } catch (final NumberFormatException e) {
            this.setText("" + min);
            return min;
        }
        if (n < min) {
            n = min;
            this.setText("" + min);
        }
        if (n > max) {
            n = max;
            this.setText("" + max);
        }
        return n;
    }
}
