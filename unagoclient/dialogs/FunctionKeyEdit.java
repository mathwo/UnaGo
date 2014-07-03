package unagoclient.dialogs;

import unagoclient.Global;
import unagoclient.gui.*;

import java.awt.*;

class FunctionKey extends GrayTextField {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FunctionKey(int i) {
        super(rene.gui.Global.getParameter("f" + i, ""));
    }
}

/**
 * A dialog, which lets the user edit all function keys. Contains an array of 10
 * text fields.
 * <p>
 * The function keys are stored as global parameters.
 */
public class FunctionKeyEdit extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    FunctionKey FK[];

    public FunctionKeyEdit() {
        super(Global.frame(), Global.resourceString("Function_Keys"), false);
        final MyPanel p = new MyPanel();
        p.setLayout(new GridLayout(0, 2));
        this.FK = new FunctionKey[10];
        for (int i = 0; i < 10; i++) {
            p.add(new MyLabel("F" + (i + 1)));
            p.add(this.FK[i] = new FunctionKey(i + 1));
        }
        this.add("Center", new Panel3D(p));
        final MyPanel bp = new MyPanel();
        bp.add(new ButtonAction(this, Global.resourceString("Close")));
        this.add("South", new Panel3D(bp));
        Global.setpacked(this, "functionkeys", 300, 400);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "functionkeys");
        for (int i = 0; i < 10; i++) {
            rene.gui.Global.setParameter("f" + (i + 1), this.FK[i].getText());
        }
        this.setVisible(false);
        this.dispose();
    }
}
