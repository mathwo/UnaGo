package unagoclient.gui;

import unagoclient.Global;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import java.awt.*;
import java.io.PrintWriter;

/**
 * A text area that takes care of the maximal length imposed by Windows and
 * other OSs. This should be replaced by unagoclient.viewer.Viewer
 * <p>
 * The class works much like TextArea, but takes care of its length.
 *
 * @see unagoclient.viewer.Viewer
 */

public class MyTextArea extends TextArea {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ListClass L;
    public int MaxLength;
    int Length = 0;

    public MyTextArea() {
        this.setFont(Global.Monospaced);
        this.L = new ListClass();
        this.MaxLength = rene.gui.Global.getParameter("maxlength", 10000);
    }

    public MyTextArea(String s, int x, int y, int f) {
        super(s, x, y, f);
        this.setFont(Global.Monospaced);
        this.L = new ListClass();
        this.MaxLength = rene.gui.Global.getParameter("maxlength", 10000);
        this.setText(s);
    }

    @Override
    public void append(String s) {
        this.Length += s.length();
        this.L.append(new ListElement(s));
        if (this.Length > this.MaxLength) {
            this.setVisible(false);
            super.setText("");
            ListElement e = this.L.last();
            this.Length = 0;
            while (this.Length < this.MaxLength / 4) {
                this.Length += ((String) e.content()).length();
                if (e.previous() == null) {
                    break;
                }
                e = e.previous();
            }
            while (e != null) {
                super.append((String) e.content());
                e = e.next();
            }
            this.setVisible(true);
        } else {
            super.append(s);
        }
    }

    public void save(PrintWriter s) {
        ListElement e = this.L.first();
        while (e != null) {
            s.print((String) e.content());
            e = e.next();
        }
    }

    @Override
    public void setEditable(boolean flag) {
        super.setEditable(flag);
    }

    @Override
    public void setText(String s) {
        this.Length = s.length();
        super.setText(s);
        this.L = new ListClass();
        this.L.append(new ListElement(s));
    }
}
