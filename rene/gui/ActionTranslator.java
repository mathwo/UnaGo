package rene.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A translator for Actions.
 */

public class ActionTranslator implements ActionListener {
    String Name;
    DoActionListener C;
    ActionEvent E;

    public ActionTranslator(DoActionListener c, String name) {
        this.Name = name;
        this.C = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.E = e;
        this.C.doAction(this.Name);
    }

    public void trigger() {
        this.C.doAction(this.Name);
    }
}
