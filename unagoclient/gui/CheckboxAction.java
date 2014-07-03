package unagoclient.gui;

import unagoclient.Global;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Similar to ChoiceAction, but for checkboxes.
 *
 * @see unagoclient.gui.ChoiceAction
 */

public class CheckboxAction extends Checkbox {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CheckboxAction(DoActionListener c, String s) {
        super(s);
        this.addItemListener(new CheckboxActionTranslator(this, c, s));
        this.setFont(Global.SansSerif);
    }

    public CheckboxAction(DoActionListener c, String s, String h) {
        super(s);
        this.addItemListener(new CheckboxActionTranslator(this, c, h));
    }
}

class CheckboxActionTranslator implements ItemListener {
    DoActionListener C;
    String S;
    public Checkbox CB;

    public CheckboxActionTranslator(Checkbox cb, DoActionListener c, String s) {
        this.C = c;
        this.S = s;
        this.CB = cb;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        this.C.itemAction(this.S, this.CB.getState());
    }
}
