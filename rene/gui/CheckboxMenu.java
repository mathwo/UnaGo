package rene.gui;

import java.awt.*;
import java.util.Vector;

public class CheckboxMenu {
    Vector V;

    public CheckboxMenu() {
        this.V = new Vector();
    }

    public void add(CheckboxMenuItem i, String tag) {
        this.V.addElement(new CheckboxMenuElement(i, tag));
    }

    public void set(String tag) {
        int i;
        for (i = 0; i < this.V.size(); i++) {
            final CheckboxMenuElement e = (CheckboxMenuElement) this.V
                    .elementAt(i);
            if (tag.equals(e.Tag)) {
                e.Item.setState(true);
            } else {
                e.Item.setState(false);
            }
        }
    }
}

class CheckboxMenuElement {
    public String Tag;
    public CheckboxMenuItem Item;

    public CheckboxMenuElement(CheckboxMenuItem i, String tag) {
        this.Item = i;
        this.Tag = tag;
    }
}
