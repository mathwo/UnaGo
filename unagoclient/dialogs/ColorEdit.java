package unagoclient.dialogs;

import unagoclient.Global;
import unagoclient.gui.*;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * A dialog to edit a color. The result is stored in the Global parameters under
 * the specified name string. Modality is handled as in the Question dialog.
 *
 * @see unagoclient.Global
 * @see unagoclient.dialogs.Question
 */

public class ColorEdit extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ColorScrollbar Red, Green, Blue;
    Label RedLabel, GreenLabel, BlueLabel;
    Color C;
    MyPanel CP;
    String Name;

    public ColorEdit(Frame F, String s, Color C, boolean flag) {
        this(F, s, C.getRed(), C.getGreen(), C.getBlue(), flag);
    }

    public ColorEdit(Frame F, String s, int red, int green, int blue,
            boolean flag) {
        super(F, Global.resourceString("Edit_Color"), flag);
        this.Name = s;
        this.C = Global.getColor(s, red, green, blue);
        final MyPanel p = new MyPanel();
        p.setLayout(new GridLayout(0, 1));
        p.add(this.Red = new ColorScrollbar(this, Global.resourceString("Red"),
                this.C.getRed()));
        p.add(this.Green = new ColorScrollbar(this, Global
                .resourceString("Green"), this.C.getGreen()));
        p.add(this.Blue = new ColorScrollbar(this, Global
                .resourceString("Blue"), this.C.getBlue()));
        this.add("Center", new Panel3D(p));
        final MyPanel pb = new MyPanel();
        pb.add(new ButtonAction(this, Global.resourceString("OK")));
        pb.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.addbutton(pb);
        this.add("South", new Panel3D(pb));
        this.CP = new MyPanel();
        this.CP.add(new MyLabel(Global.resourceString("Your_Color")));
        this.CP.setBackground(this.C);
        this.add("North", new Panel3D(this.CP));
        Global.setpacked(this, "coloredit", 350, 150);
        this.validate();
    }

    public void addbutton(MyPanel p) {
    }

    public Color color() {
        return this.C;
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "coloredit");
        if (Global.resourceString("Cancel").equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("OK").equals(o)) {
            Global.setColor(this.Name, this.C);
            this.tell(this.C);
            this.setVisible(false);
            this.dispose();
        }
    }

    public void setcolor() {
        this.C = new Color(this.Red.value(), this.Green.value(),
                this.Blue.value());
        this.CP.setBackground(this.C);
        this.CP.repaint();
    }

    public void tell(Color C) {
    }
}

class ColorScrollbar extends Panel implements AdjustmentListener,
DoActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int Value;
    ColorEdit CE;
    Scrollbar SB;
    IntField L;

    public ColorScrollbar(ColorEdit ce, String s, int value) {
        this.CE = ce;
        this.setLayout(new GridLayout(1, 0));
        this.Value = value;
        final MyPanel p = new MyPanel();
        p.setLayout(new GridLayout(1, 0));
        p.add(new MyLabel(s));
        p.add(this.L = new IntField(this, "L", this.Value, 4));
        this.add(p);
        this.add(this.SB = new Scrollbar(Scrollbar.HORIZONTAL, value, 40, 0,
                295));
        this.SB.addAdjustmentListener(this);
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        this.Value = this.SB.getValue();
        this.L.set(this.Value);
        this.SB.setValue(this.Value);
        this.CE.setcolor();
    }

    @Override
    public void doAction(String o) {
        if ("L".equals(o)) {
            this.Value = this.L.value(0, 255);
            this.SB.setValue(this.Value);
            this.CE.setcolor();
        }
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }

    public int value() {
        return this.Value;
    }
}
