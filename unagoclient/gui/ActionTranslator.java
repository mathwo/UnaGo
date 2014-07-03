package unagoclient.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ActionTranslator implements ActionListener {
    String Name;
    DoActionListener C;

    public ActionTranslator(DoActionListener c, String name) {
        this.Name = name;
        this.C = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.C.doAction(this.Name);
    }

    public void setString(String s) {
        this.Name = s;
    }
}
