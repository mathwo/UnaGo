package rene.gui;

import java.awt.*;

public class MyMenuItem extends MenuItem {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MyMenuItem(String s) {
        super(s);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
    }
}
