package rene.gui;

import java.awt.*;

public class MyCheckboxMenuItem extends CheckboxMenuItem {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MyCheckboxMenuItem(String s) {
        super(s);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
    }
}
