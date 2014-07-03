package rene.gui;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * A text Button with a midifyable Font. The button may also be triggered by a
 * keyboard return.
 * <p>
 * This button class is used in DoActionListener interfaces.
 */

public class ButtonAction extends Button {
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
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        if (Global.ControlBackground != null) {
            this.setBackground(Global.ControlBackground);
        }
    }

    public ActionEvent getAction() {
        return this.AT.E;
    }
}
