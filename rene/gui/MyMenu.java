package rene.gui;

import java.awt.*;

public class MyMenu extends Menu {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MyMenu(String s) {
        super(s);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
    }
}
