package unagoclient.gui;

import unagoclient.Global;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Similar to ChoiceAction, but for checkboxes in menus.
 *
 * @see unagoclient.gui.ChoiceAction
 */

public class CheckboxMenuItemAction extends CheckboxMenuItem {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CheckboxMenuItemAction(DoActionListener c, String s) {
        super(s);
        this.addItemListener(new CheckboxTranslator(this, c, s));
        this.setFont(Global.SansSerif);
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
