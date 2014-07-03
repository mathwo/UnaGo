package rene.gui;

import java.awt.*;

public class MyChoice extends Choice {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MyChoice() {
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        if (Global.Background != null) {
            this.setBackground(Global.Background);
        }
    }
}
