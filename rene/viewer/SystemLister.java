package rene.viewer;

import java.awt.*;

public class SystemLister extends Lister {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    List L;
    static Font F = null;

    public SystemLister() {
        super("dummy");
        this.setLayout(new BorderLayout());
        this.add("Center", this.L = new java.awt.List());
        if (SystemLister.F != null) {
            this.L.setFont(SystemLister.F);
        }
    }

    @Override
    public void add(String s) {
        this.L.add(s);
    }

    @Override
    public void add(String s, Color c) {
        this.add(s);
    }

    @Override
    public void appendLine(String s) {
        this.L.add(s);
    }

    @Override
    public String getSelectedItem() {
        return this.L.getSelectedItem();
    }

    @Override
    public void setPopupMenu(PopupMenu pm) {
    }

    @Override
    public void setText(String s) {
        if (s.equals("")) {
            this.L.removeAll();
        } else {
            this.L.add(s);
        }
    }
}
