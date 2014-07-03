package unagoclient.gmp;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.StringTokenizer;

public class GMPConnection extends CloseFrame implements GMPInterface {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int Handicap = GMPConnector.EVEN;
    int MyColor = GMPConnector.WHITE;
    int Rules = GMPConnector.JAPANESE;
    int BoardSize = 19;

    JTextField Program;
    IntField HandicapField, BoardSizeField;
    Checkbox White;

    GMPConnector C;

    GMPGoFrame F;
    GMPConnection Co = this;
    OkAdapter Ok = null;

    public int I, J;

    public GMPConnection(Frame f) {
        super(Global.resourceString("Play_Go"));
        this.setLayout(new BorderLayout());
        final MyPanel center = new MyPanel();
        center.setLayout(new GridLayout(0, 2));
        center.add(new MyLabel(Global.resourceString("Go_Protocol_Server")));
        center.add(this.Program = new TextFieldAction(this, "", rene.gui.Global
                .getParameter("gmpserver", "gnugo.exe"), 16));
        center.add(new MyLabel(Global.resourceString("Board_size")));
        center.add(this.BoardSizeField = new IntField(this, "BoardSize",
                rene.gui.Global.getParameter("gmpboardsize", 19)));
        center.add(new MyLabel(Global.resourceString("Handicap")));
        center.add(this.HandicapField = new IntField(this, "Handicap",
                rene.gui.Global.getParameter("gmphandicap", 9)));
        center.add(new MyLabel(Global.resourceString("Play_White")));
        center.add(this.White = new CheckboxAction(this, ""));
        this.White.setState(rene.gui.Global.getParameter("gmpwhite", true));
        this.add("Center", new Panel3D(center));
        final MyPanel south = new MyPanel();
        south.add(new ButtonAction(this, Global.resourceString("Play")));
        this.add("South", new Panel3D(south));
        Global.setpacked(this, "gmpconnection", 300, 150, f);
        this.seticon("iboard.gif");
        this.setVisible(true);
    }

    public boolean askUndo() {
        this.setOk(new OkAdapter() {
            @Override
            public void gotOk() {
                GMPConnection.this.F.doundo(2);
            }
        });
        try {
            this.C.send(6, 2);
        } catch (final Exception e) {
        }
        return false;
    }

    @Override
    public void doAction(String o) {
        if (o.equals(Global.resourceString("Play"))) {
            final String text = this.Program.getText();
            StringTokenizer t;
            if (text.startsWith("\"")) {
                t = new StringTokenizer(this.Program.getText(), "\"");
            } else {
                t = new StringTokenizer(this.Program.getText(), " ");
            }
            if (!t.hasMoreTokens()) {
                return;
            }
            final String s = t.nextToken();
            final File f = new File(s);
            if (!f.exists()) {
                final rene.dialogs.Warning w = new rene.dialogs.Warning(this,
                        "Program not found!", "Warning", true);
                w.center(this);
                w.setVisible(true);
                return;
            }
            this.C = new GMPConnector(this.Program.getText());
            rene.gui.Global.setParameter("gmpserver", this.Program.getText());
            this.C.setGMPInterface(this);
            this.Handicap = this.HandicapField.value(0, 9);
            rene.gui.Global.setParameter("gmphandicap", this.Handicap);
            if (this.Handicap == 0) {
                this.Handicap = 1;
            }
            this.BoardSize = this.BoardSizeField.value(5, 19);
            rene.gui.Global.setParameter("gmpboardsize", this.BoardSize);
            this.MyColor = this.White.getState() ? GMPConnector.WHITE
                    : GMPConnector.BLACK;
            rene.gui.Global.setParameter("gmpwhite", this.White.getState());
            try {
                this.setOk(new OkAdapter() {
                    @Override
                    public void gotOk() {
                        GMPConnection.this.F = new GMPGoFrame(
                                GMPConnection.this.Co,
                                GMPConnection.this.BoardSize,
                                GMPConnection.this.White.getState() ? 1 : -1);
                        GMPConnection.this.F.setVisible(true);
                        GMPConnection.this.Co.setVisible(false);
                        GMPConnection.this.Co.dispose();
                        if (GMPConnection.this.Handicap > 1) {
                            GMPConnection.this
                            .handicap(GMPConnection.this.Handicap);
                        }
                    }
                });
                this.C.connect();
                new GMPWait(this);
                this.Ok = null;
            } catch (final Exception e) {
                final rene.dialogs.Warning w = new rene.dialogs.Warning(this,
                        "Error : " + e.toString(), "Warning", true);
                w.center(this);
                w.setVisible(true);
            }
        }
    }

    @Override
    public void doclose() {
        if (this.C != null) {
            this.C.doclose();
        }
        super.doclose();
    }

    @Override
    public int getBoardSize() {
        return this.BoardSize;
    }

    @Override
    public int getColor() {
        return this.MyColor;
    }

    @Override
    public int getHandicap() {
        return this.Handicap;
    }

    @Override
    public int getRules() {
        return this.Rules;
    }

    @Override
    public void gotAnswer(int a) {
    }

    @Override
    public void gotMove(int color, int pos) {
        pos--;
        final int i = pos % this.BoardSize;
        final int j = pos / this.BoardSize;
        if (i < 0 || j < 0) {
            this.F.gotPass(color);
        }
        if (color == this.MyColor) {
            this.F.gotSet(color, i, this.BoardSize - j - 1);
        } else {
            this.F.gotMove(color, i, this.BoardSize - j - 1);
        }
    }

    @Override
    public void gotOk() {
        if (this.Ok != null) {
            this.Ok.gotOk();
        }
        this.Ok = null;
    }

    public void handicap(int n) {
        final int S = this.BoardSize;
        final int h = S < 13 ? 3 : 4;
        if (n > 5) {
            this.setblack(h - 1, S / 2);
            this.setblack(S - h, S / 2);
        }
        if (n > 7) {
            this.setblack(S / 2, h - 1);
            this.setblack(S / 2, S - h);
        }
        switch (n) {
            case 9:
            case 7:
            case 5:
                this.setblack(S / 2, S / 2);
            case 8:
            case 6:
            case 4:
                this.setblack(S - h, S - h);
            case 3:
                this.setblack(h - 1, h - 1);
            case 2:
                this.setblack(h - 1, S - h);
            case 1:
                this.setblack(S - h, h - 1);
        }
        this.F.color(GMPConnector.WHITE);
    }

    public void moveset(int i, int j) {
        final int pos = (this.BoardSize - j - 1) * this.BoardSize + i + 1;
        this.I = i;
        this.J = j;
        try {
            this.setOk(new OkAdapter() {
                @Override
                public void gotOk() {
                    GMPConnection.this.F.gotMove(GMPConnection.this.MyColor,
                            GMPConnection.this.I, GMPConnection.this.J);
                }
            });
            this.C.move(this.MyColor, pos);
        } catch (final Exception e) {
        }
    }

    public void pass() {
        try {
            this.C.move(this.MyColor, 0);
        } catch (final Exception e) {
        }
    }

    public void setblack(int i, int j) {
        this.F.gotSet(GMPConnector.BLACK, i, j);
    }

    public void setOk(OkAdapter ok) {
        this.Ok = ok;
    }

    public void undo() {
        try {
            this.C.takeback(2);
        } catch (final Exception e) {
        }
    }
}

class GMPWait extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public GMPWait(GMPConnection f) {
        super(f, Global.resourceString("Play_Go"), true);
        this.setLayout(new BorderLayout());
        this.add("Center",
                new MyLabel(Global.resourceString("Negotiating_with_Program")));
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Abort")));
        this.add("South", p);
        Global.setpacked(this, "gmpwait", 300, 150, f);
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        this.setVisible(false);
        this.dispose();
    }
}

class OkAdapter {
    public void gotOk() {
    }
}
