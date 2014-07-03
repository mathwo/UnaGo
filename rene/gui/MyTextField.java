package rene.gui;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * A TextField with a modifyable background and font.
 */

public class MyTextField extends TextField implements FocusListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MyTextField() {
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        this.addFocusListener(this);
    }

    public MyTextField(String s) {
        super(s);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        this.addFocusListener(this);
    }

    public MyTextField(String s, int n) {
        super(s, n);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        this.addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        this.setSelectionStart(0);
    }

    @Override
    public void focusLost(FocusEvent e) {
        this.setSelectionStart(0);
        this.setSelectionEnd(0);
    }
}
