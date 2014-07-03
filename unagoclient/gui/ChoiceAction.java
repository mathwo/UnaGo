package unagoclient.gui;

import unagoclient.Global;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This is a choice item, which sets a specified font and translates events into
 * strings, which are passed to the doAction method of the DoActionListener.
 *
 * @see unagoclient.gui.CloseFrame#doAction
 * @see unagoclient.gui.CloseDialog#doAction
 */

public class ChoiceAction extends Choice {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ChoiceAction(DoActionListener c, String s) {
        this.addItemListener(new ChoiceTranslator(this, c, s));
        this.setFont(Global.SansSerif);
    }
}

class ChoiceTranslator implements ItemListener {
    DoActionListener C;
    String S;
    public Choice Ch;

    public ChoiceTranslator(Choice ch, DoActionListener c, String s) {
        this.C = c;
        this.S = s;
        this.Ch = ch;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        this.C.itemAction(this.S, e.getStateChange() == ItemEvent.SELECTED);
    }
}
