package rene.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
