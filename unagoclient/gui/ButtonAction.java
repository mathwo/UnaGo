package unagoclient.gui;

import javax.swing.*;

/**
 * Similar to ChoiceAction but for buttons.
 *
 * @see unagoclient.gui.ChoiceAction
 */

public class ButtonAction extends JButton {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    DoActionListener C;
    String Name;
    ActionTranslator AT;

    public ButtonAction(DoActionListener c, String s) {
        this(c, s, s);
    }

    public ButtonAction(DoActionListener c, String s, String name) {
        super(s);
        this.C = c;
        this.Name = name;
        this.addActionListener(this.AT = new ActionTranslator(c, name));
        // setFont(Global.SansSerif);
    }

}
