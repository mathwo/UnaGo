package rene.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuItemAction extends MyMenuItem {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    MenuItemActionTranslator MIT;

    public MenuItemAction(DoActionListener c, String s) {
        this(c, s, s);
    }

    public MenuItemAction(DoActionListener c, String s, String st) {
        super(s);
        this.addActionListener(this.MIT = new MenuItemActionTranslator(c, st));
    }

    public void setString(String s) {
        this.MIT.S = s;
    }
}

/**
 * A MenuItem with modifyable font.
 * <p>
 * This it to be used in DoActionListener interfaces.
 */

class MenuItemActionTranslator implements ActionListener {
    String S;
    DoActionListener C;

    public MenuItemActionTranslator(DoActionListener c, String s) {
        this.S = s;
        this.C = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.C.doAction(this.S);
    }
}
