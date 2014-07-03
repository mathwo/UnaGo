package unagoclient.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This is a callback class to act as an ActionListener, which calls back a
 * DoActionListener on any action passing the string name to its doAction
 * method.
 *
 * @see DoActionListener
 * @see TextFieldAction
 * @see unagoclient.gui.CloseFrame#doAction
 * @see unagoclient.gui.CloseDialog#doAction
 */

class TextFieldActionListener implements ActionListener {
    DoActionListener C;
    String Name;

    public TextFieldActionListener(DoActionListener c, String name) {
        this.C = c;
        this.Name = name;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.C.doAction(this.Name);
    }
}
