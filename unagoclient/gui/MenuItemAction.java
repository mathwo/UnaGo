package unagoclient.gui;

import unagoclient.Global;

import java.awt.*;

/**
 * A menu item with a specified font.
 */

public class MenuItemAction extends MenuItem {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ActionTranslator AT;

    public MenuItemAction(DoActionListener c, String s) {
        this(c, s, s);
    }

    public MenuItemAction(DoActionListener c, String s, String name) {
        super(s);
        this.addActionListener(this.AT = new ActionTranslator(c, name));
        this.setFont(Global.SansSerif);
    }

    public void setString(String s) {
        this.AT.setString(s);
    }
}
