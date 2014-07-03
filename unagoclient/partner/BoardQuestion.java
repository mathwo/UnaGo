package unagoclient.partner;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;
import java.awt.*;

/**
 * Question to accept or decline a game with received parameters.
 */

public class BoardQuestion extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int BoardSize, Handicap, TotalTime, ExtraTime, ExtraMoves;
    String ColorChoice;
    PartnerFrame PF;

    public BoardQuestion(PartnerFrame pf, int s, String c, int h, int tt,
            int et, int em) {
        super(pf, Global.resourceString("Game_Setup"), true);
        this.PF = pf;
        this.BoardSize = s;
        this.Handicap = h;
        this.TotalTime = tt;
        this.ExtraTime = et;
        this.ExtraMoves = em;
        this.ColorChoice = c;
        final JPanel pa = new MyPanel();
        this.add("Center", new Panel3D(pa));
        JTextField t;
        pa.setLayout(new GridLayout(0, 2));
        pa.add(new MyLabel(Global.resourceString("Board_size")));
        pa.add(t = new FormTextField("" + s));
        t.setEditable(false);
        pa.add(new MyLabel(Global.resourceString("Partners_color")));
        pa.add(t = new FormTextField(c));
        t.setEditable(false);
        pa.add(new MyLabel(Global.resourceString("Handicap")));
        pa.add(t = new FormTextField("" + h));
        t.setEditable(false);
        pa.add(new MyLabel(Global.resourceString("Total_Time__min_")));
        pa.add(t = new FormTextField("" + tt));
        t.setEditable(false);
        pa.add(new MyLabel(Global.resourceString("Extra_Time__min_")));
        pa.add(t = new FormTextField("" + et));
        t.setEditable(false);
        pa.add(new MyLabel(Global.resourceString("Moves_per_Extra_Time")));
        pa.add(t = new FormTextField("" + em));
        t.setEditable(false);
        final JPanel pb = new MyPanel();
        pb.add(new ButtonAction(this, Global.resourceString("Accept")));
        pb.add(new ButtonAction(this, Global.resourceString("Decline")));
        this.add("South", new Panel3D(pb));
        Global.setpacked(this, "boardquestion", 300, 400, pf);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "boardquestion");
        if (Global.resourceString("Accept").equals(o)) {
            this.PF.doboard(this.BoardSize, this.ColorChoice, this.Handicap,
                    this.TotalTime, this.ExtraTime, this.ExtraMoves);
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Decline").equals(o)) {
            this.PF.declineboard();
            this.setVisible(false);
            this.dispose();
        } else {
            super.doAction(o);
        }
    }
}
