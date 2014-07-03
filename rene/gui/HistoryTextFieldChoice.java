package rene.gui;

import rene.util.FileName;
import rene.util.MyVector;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class HistoryTextFieldChoice extends MyChoice implements ItemListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    HistoryTextField T;
    DoActionListener AL;
    MyVector V = new MyVector();
    public int MaxLength = 32;

    public HistoryTextFieldChoice(HistoryTextField t) {
        this.T = t;
        this.addItemListener(this);
    }

    public String getRecent() {
        if (this.V.size() > 1) {
            return (String) this.V.elementAt(1);
        } else {
            return "";
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        final int n = this.getSelectedIndex();
        final String s = (String) this.V.elementAt(n);
        if (s.equals("   ")) {
            return;
        }
        if (this.AL != null) {
            this.AL.doAction(s);
        } else {
            this.T.doAction(s);
        }
    }

    public void setDoActionListener(DoActionListener al) {
        this.AL = al;
    }

    public void update() {
        this.removeAll();
        this.V.removeAllElements();
        final ListClass l = this.T.getHistory();
        ListElement e = l.last();
        if (e == null || ((String) e.content()).equals("")) {
            this.V.addElement("   ");
            this.add("   ");
        }
        while (e != null) {
            final String s = (String) e.content();
            if (!s.equals("")) {
                this.V.addElement(s);
                this.add(FileName.chop(s, this.MaxLength));
            }
            e = e.previous();
        }
    }
}
