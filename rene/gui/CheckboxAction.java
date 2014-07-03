package rene.gui;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * A Checkbox with modifyable font.
 * <p>
 * To be used in DoActionListener interfaces.
 */

public class CheckboxAction extends Checkbox {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CheckboxAction(DoActionListener c, String s) {
        super(s);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        this.addItemListener(new CheckboxActionTranslator(this, c, s));
    }

    public CheckboxAction(DoActionListener c, String s, String h) {
        super(s);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
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
