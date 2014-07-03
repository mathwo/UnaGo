package unagoclient.partner;

import unagoclient.Global;
import unagoclient.dialogs.Message;
import unagoclient.gui.*;

import javax.swing.*;
import java.awt.*;

/**
 * Question to start a game with user definable paramters (handicap etc.)
 */

public class GameQuestion extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    FormTextField BoardSize, Handicap, TotalTime, ExtraTime, ExtraMoves;
    Choice ColorChoice;
    PartnerFrame PF;

    public GameQuestion(PartnerFrame pf) {
        super(pf, Global.resourceString("Game_Setup"), true);
        this.PF = pf;
        final JPanel pa = new MyPanel();
        this.add("Center", new Panel3D(pa));
        pa.setLayout(new GridLayout(0, 2));
        pa.add(new MyLabel(Global.resourceString("Board_size")));
        pa.add(this.BoardSize = new FormTextField("19"));
        pa.add(new MyLabel(Global.resourceString("Your_color")));
        pa.add(this.ColorChoice = new Choice());
        this.ColorChoice.setFont(Global.SansSerif);
        this.ColorChoice.add(Global.resourceString("Black"));
        this.ColorChoice.add(Global.resourceString("White"));
        this.ColorChoice.select(0);
        pa.add(new MyLabel(Global.resourceString("Handicap")));
        pa.add(this.Handicap = new FormTextField("0"));
        pa.add(new MyLabel(Global.resourceString("Total_Time__min_")));
        pa.add(this.TotalTime = new FormTextField("10"));
        pa.add(new MyLabel(Global.resourceString("Extra_Time__min_")));
        pa.add(this.ExtraTime = new FormTextField("10"));
        pa.add(new MyLabel(Global.resourceString("Moves_per_Extra_Time")));
        pa.add(this.ExtraMoves = new FormTextField("24"));
        final JPanel pb = new MyPanel();
        pb.add(new ButtonAction(this, Global.resourceString("Request")));
        pb.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.add("South", new Panel3D(pb));
        Global.setpacked(this, "gamequestion", 300, 400, pf);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "gamequestion");
        if (Global.resourceString("Request").equals(o)) {
            int s, h, tt, et, em;
            try {
                s = Integer.parseInt(this.BoardSize.getText());
                h = Integer.parseInt(this.Handicap.getText());
                tt = Integer.parseInt(this.TotalTime.getText());
                et = Integer.parseInt(this.ExtraTime.getText());
                em = Integer.parseInt(this.ExtraMoves.getText());
            } catch (final NumberFormatException ex) {
                new Message(this.PF,
                        Global.resourceString("Illegal_Number_Format_"));
                return;
            }
            if (s < 5) {
                s = 5;
            }
            if (s > 29) {
                s = 29;
            }
            if (h > 9) {
                h = 9;
            }
            if (h < 0) {
                h = 0;
            }
            if (tt < 1) {
                tt = 1;
            }
            if (et < 0) {
                et = 0;
            }
            if (em < 1) {
                em = 1;
            }
            String col = "b";
            if (this.ColorChoice.getSelectedIndex() != 0) {
                col = "w";
            }
            this.PF.dorequest(s, col, h, tt, et, em);
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
