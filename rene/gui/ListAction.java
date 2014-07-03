package rene.gui;

import java.awt.*;

/**
 * A List class with a specified font and background. Designed for
 * DoActionListener objects.
 */

public class ListAction extends List {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ListAction(DoActionListener c, String name) {
        this.addActionListener(new ActionTranslator(c, name));
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        if (Global.Background != null) {
            this.setBackground(Global.Background);
        }
    }

    public ListAction(DoActionListener c, String name, int n) {
        super(n);
        this.addActionListener(new ActionTranslator(c, name));
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        if (Global.Background != null) {
            this.setBackground(Global.Background);
        }
    }
}
