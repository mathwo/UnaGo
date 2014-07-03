package unagoclient.igs.who;

import unagoclient.Global;
import unagoclient.gui.*;
import unagoclient.igs.ConnectionFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Ask to match the chosen user.
 */

public class MatchQuestion extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ConnectionFrame F;
    JTextField T;
    JTextField User;
    IntField BoardSize, TotalTime, ExtraTime;
    Choice ColorChoice;

    /**
     * @param f
     *            the ConnectionFrame, which holds the connection.
     */
    public MatchQuestion(Frame fr, ConnectionFrame f, String user) {
        super(fr, Global.resourceString("Match"), false);
        this.F = f;
        final MyPanel pp = new MyPanel();
        pp.setLayout(new GridLayout(0, 2));
        pp.add(new MyLabel(Global.resourceString("Opponent")));
        pp.add(this.User = new GrayTextField(user));
        pp.add(new MyLabel(Global.resourceString("Board_Size")));
        pp.add(this.BoardSize = new IntField(this, "BoardSize", 19));
        pp.add(new MyLabel(Global.resourceString("Your_Color")));
        pp.add(this.ColorChoice = new Choice());
        this.ColorChoice.setFont(Global.SansSerif);
        this.ColorChoice.add(Global.resourceString("Black"));
        this.ColorChoice.add(Global.resourceString("White"));
        this.ColorChoice.select(0);
        pp.add(new MyLabel(Global.resourceString("Time__min_")));
        pp.add(this.TotalTime = new IntField(this, "TotalTime", rene.gui.Global
                .getParameter("totaltime", 10)));
        pp.add(new MyLabel(Global.resourceString("Extra_Time")));
        pp.add(this.ExtraTime = new IntField(this, "ExtraTime", rene.gui.Global
                .getParameter("extratime", 10)));
        this.add("Center", new Panel3D(pp));
        final MyPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Match")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.add("South", new Panel3D(p));
        Global.setpacked(this, "match", 200, 150, f);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        rene.gui.Global.setParameter("matchwidth", this.getSize().width);
        rene.gui.Global.setParameter("matchheight", this.getSize().height);
        if (Global.resourceString("Match").equals(o)) {
            String s = "b";
            if (this.ColorChoice.getSelectedIndex() == 1) {
                s = "w";
            }
            this.F.out("match " + this.User.getText() + " " + s + " "
                    + this.BoardSize.value(5, 29) + " "
                    + this.TotalTime.value(0, 6000) + " "
                    + this.ExtraTime.value(0, 6000));
            rene.gui.Global.setParameter("totaltime",
                    this.TotalTime.value(0, 6000));
            rene.gui.Global.setParameter("extratime",
                    this.ExtraTime.value(0, 6000));
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Cancel").equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else {
            super.doAction(o);
        }
    }
}
