package rene.gui;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * A TextField with a modifyable background and font. This is used in
 * DoActionListener interfaces.
 */

public class TextFieldAction extends TextField implements FocusListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected ActionTranslator T;
    String S;

    public TextFieldAction(DoActionListener t, String name) {
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        if (Global.Background != null) {
            this.setBackground(Global.Background);
        }
        this.T = new ActionTranslator(t, name);
        this.addActionListener(this.T);
        this.addFocusListener(this);
    }

    public TextFieldAction(DoActionListener t, String name, int n) {
        super(n);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        if (Global.Background != null) {
            this.setBackground(Global.Background);
        }
        this.T = new ActionTranslator(t, name);
        this.addActionListener(this.T);
        this.addFocusListener(this);
    }

    public TextFieldAction(DoActionListener t, String name, String s) {
        super(s);
        this.S = s;
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        if (Global.Background != null) {
            this.setBackground(Global.Background);
        }
        this.T = new ActionTranslator(t, name);
        this.addActionListener(this.T);
        this.addFocusListener(this);
    }

    public TextFieldAction(DoActionListener t, String name, String s, int n) {
        super(s, n);
        this.S = s;
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        if (Global.Background != null) {
            this.setBackground(Global.Background);
        }
        this.T = new ActionTranslator(t, name);
        this.addActionListener(this.T);
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

    public String getOldText() {
        return this.S;
    }

    public boolean isChanged() {
        return !this.S.equals(this.getText());
    }

    @Override
    public void setText(String s) {
        super.setText(s);
        this.S = s;
    }

    public void triggerAction() {
        this.T.trigger();
    }
}
