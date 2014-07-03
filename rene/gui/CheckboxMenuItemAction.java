package rene.gui;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * A CheckboxMenuItem with modifyable font.
 * <p>
 * This is to be used in DoActionListener interfaces.
 */

public class CheckboxMenuItemAction extends CheckboxMenuItem {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CheckboxMenuItemAction(DoActionListener c, String s) {
        this(c, s, s);
    }

    public CheckboxMenuItemAction(DoActionListener c, String s, String st) {
        super(s);
        this.addItemListener(new CheckboxTranslator(this, c, st));
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
    }
}

class CheckboxTranslator implements ItemListener {
    DoActionListener C;
    String S;
    public CheckboxMenuItem CB;

    public CheckboxTranslator(CheckboxMenuItem cb, DoActionListener c, String s) {
        this.C = c;
        this.S = s;
        this.CB = cb;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        this.C.itemAction(this.S, this.CB.getState());
    }
}
