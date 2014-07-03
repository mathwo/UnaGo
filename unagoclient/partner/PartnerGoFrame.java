package unagoclient.partner;

import unagoclient.Global;
import unagoclient.board.ConnectedGoFrame;
import unagoclient.board.GoTimer;
import unagoclient.board.OutputFormatter;
import unagoclient.board.TimedBoard;
import unagoclient.dialogs.Message;
import unagoclient.sound.UnaGoSound;

/**
 * The go frame for partner connections.
 */

public class PartnerGoFrame extends ConnectedGoFrame implements TimedBoard {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String BlackName, WhiteName;
    int BlackTime, WhiteTime, BlackMoves, WhiteMoves;
    int BlackRun, WhiteRun;
    GoTimer Timer;
    long CurrentTime;
    PartnerFrame PF;
    int Col, TotalTime, ExtraTime, ExtraMoves;
    public boolean Started, Ended;
    int Handicap;

    String OldS = "";

    char form[] = new char[32];

    int lastbeep = 0;

    public PartnerGoFrame(PartnerFrame pf, String s, int col, int si, int tt,
            int et, int em, int ha) {
        super(s, si, Global.resourceString("End_Game"), Global
                .resourceString("Count"), false, false);
        this.PF = pf;
        this.Col = col;
        this.TotalTime = tt;
        this.ExtraTime = et;
        this.ExtraMoves = em;
        this.BlackTime = this.TotalTime;
        this.WhiteTime = this.TotalTime;
        this.Handicap = ha;
        this.BlackRun = 0;
        this.WhiteRun = 0;
        this.Started = false;
        this.Ended = false;
        if (this.Col == 1) {
            this.BlackName = Global.resourceString("You");
        } else {
            this.BlackName = Global.resourceString("Opponent");
        }
        if (this.Col == -1) {
            this.WhiteName = Global.resourceString("You");
        } else {
            this.WhiteName = Global.resourceString("Opponent");
        }
        this.setVisible(true);
        this.repaint();
    }

    public void addothertime(int s) {
        if (this.Col > 0) {
            this.WhiteTime += s;
        } else {
            this.BlackTime += s;
        }
        this.settitle();
    }

    public void addtime(int s) {
        if (this.Col > 0) {
            this.BlackTime += s;
        } else {
            this.WhiteTime += s;
        }
        this.settitle();
    }

    @Override
    public void alarm() {
        final long now = System.currentTimeMillis();
        if (this.B.maincolor() > 0) {
            this.BlackRun = (int) ((now - this.CurrentTime) / 1000);
        } else {
            this.WhiteRun = (int) ((now - this.CurrentTime) / 1000);
        }
        if (this.Col > 0 && this.BlackTime - this.BlackRun < 0) {
            if (this.BlackMoves < 0) {
                this.BlackMoves = this.ExtraMoves;
                this.BlackTime = this.ExtraTime;
                this.BlackRun = 0;
                this.CurrentTime = now;
            } else if (this.BlackMoves > 0) {
                new Message(this,
                        Global.resourceString("Black_looses_by_time_"));
                this.Timer.stopit();
            } else {
                this.BlackMoves = this.ExtraMoves;
                this.BlackTime = this.ExtraTime;
                this.BlackRun = 0;
                this.CurrentTime = now;
            }
        } else if (this.Col < 0 && this.WhiteTime - this.WhiteRun < 0) {
            if (this.WhiteMoves < 0) {
                this.WhiteMoves = this.ExtraMoves;
                this.WhiteTime = this.ExtraTime;
                this.WhiteRun = 0;
                this.CurrentTime = now;
            } else if (this.WhiteMoves > 0) {
                new Message(this,
                        Global.resourceString("White_looses_by_time_"));
                this.Timer.stopit();
            } else {
                this.WhiteMoves = this.ExtraMoves;
                this.WhiteTime = this.ExtraTime;
                this.WhiteRun = 0;
                this.CurrentTime = now;
            }
        }
        this.settitle();
    }

    public void beep(int s) {
        if (s < 0 || !rene.gui.Global.getParameter("warning", true)) {
            return;
        } else if (s < 31 && s != this.lastbeep) {
            if (s % 10 == 0) {
                this.getToolkit().beep();
                this.lastbeep = s;
            }
        }
    }

    @Override
    public boolean blocked() {
        return false;
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Send").equals(o)
                || (this.ExtraSendField && Global.resourceString(
                        "ExtraSendField").equals(o))) {
            if (this.ExtraSendField) {
                if (!this.SendField.getText().equals("")) {
                    this.PF.out(this.SendField.getText());
                    this.SendField.remember(this.SendField.getText());
                    this.SendField.setText("");
                }
                return;
            } else {
                new PartnerSendQuestion(this, this.PF);
                return;
            }
        } else if (Global.resourceString("End_Game").equals(o)) {
            if (this.Col != this.B.maincolor()) {
                return;
            }
            this.PF.endgame();
            return;
        } else if (Global.resourceString("Count").equals(o)) {
            if (this.Ended || !this.B.ismain()) {
                final String s = this.B.done();
                if (s != null) {
                    new Message(this, s);
                }
            }
            return;
        } else if (Global.resourceString("Undo").equals(o)) {
            if (this.Ended || !this.B.ismain()) {
                this.B.undo();
            } else {
                if (this.Col != this.B.maincolor()) {
                    return;
                }
                this.B.undo();
            }
            return;
        } else if (Global.resourceString("Undo_Adding_Removing").equals(o)) {
            this.B.clearremovals();
            return;
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        this.setVisible(false);
        this.dispose();
        this.PF.toFront();
        this.PF.boardclosed(this);
        this.PF.PGF = null;
    }

    public void dopass() {
        this.B.setpass();
    }

    void doscore() {
        this.B.score();
        this.Timer.stopit();
        this.Ended = true;
    }

    String formmoves(int m) {
        if (m < 0) {
            return "";
        }
        this.form[0] = '(';
        int n = OutputFormatter.formint(this.form, 1, m);
        this.form[n++] = ')';
        return new String(this.form, 0, n);
    }

    String formtime(int sec) {
        final int n = OutputFormatter.formtime(this.form, sec);
        return new String(this.form, 0, n);
    }

    int maincolor() {
        return this.B.maincolor();
    }

    @Override
    public void movepass() {
        if (!this.Started || this.Ended) {
            return;
        }
        if (this.B.maincolor() != this.Col) {
            return;
        }
        if (this.Timer.isAlive()) {
            this.alarm();
        }
        if (this.Col > 0) {
            if (this.BlackMoves > 0) {
                this.BlackMoves--;
            }
        } else {
            if (this.WhiteMoves > 0) {
                this.WhiteMoves--;
            }
        }
        this.PF.pass(this.BlackTime - this.BlackRun, this.BlackMoves,
                this.WhiteTime - this.WhiteRun, this.WhiteMoves);
    }

    @Override
    public boolean moveset(int i, int j) {
        if (!this.Started || this.Ended) {
            return false;
        }
        String color;
        if (this.B.maincolor() != this.Col) {
            return false;
        }
        if (this.B.maincolor() > 0) {
            color = "b";
        } else {
            color = "w";
        }
        if (this.Timer.isAlive()) {
            this.alarm();
        }
        final int bm = this.BlackMoves, wm = this.WhiteMoves;
        if (this.Col > 0) {
            if (this.BlackMoves > 0) {
                this.BlackMoves--;
            }
        } else {
            if (this.WhiteMoves > 0) {
                this.WhiteMoves--;
            }
        }
        if (!this.PF.moveset(color, i, j, this.BlackTime - this.BlackRun,
                this.BlackMoves, this.WhiteTime - this.WhiteRun,
                this.WhiteMoves)) {
            this.BlackMoves = bm;
            this.WhiteMoves = wm;
            if (this.Timer.isAlive()) {
                this.alarm();
            }
            return false;
        }
        return true;
    }

    @Override
    public void result(int b, int w) {
        this.PF.out("@@result " + b + " " + w);
    }

    void setHandicap() {
        if (this.Handicap > 0) {
            this.B.handicap(this.Handicap);
        }
        this.Handicap = 0;
    }

    public void settimes(int bt, int bm, int wt, int wm) {
        this.BlackTime = bt;
        this.BlackRun = 0;
        this.WhiteTime = wt;
        this.WhiteRun = 0;
        this.WhiteMoves = wm;
        this.BlackMoves = bm;
        this.CurrentTime = System.currentTimeMillis();
        this.settitle();
    }

    void settitle() {
        String S;
        if (this.BigTimer) {
            S = this.WhiteName + " " + this.formmoves(this.WhiteMoves) + " - "
                    + this.BlackName + " " + this.formmoves(this.BlackMoves);
        } else {
            S = this.WhiteName + " "
                    + this.formtime(this.WhiteTime - this.WhiteRun) + " "
                    + this.formmoves(this.WhiteMoves) + " - " + this.BlackName
                    + " " + this.formtime(this.BlackTime - this.BlackRun) + " "
                    + this.formmoves(this.BlackMoves);
        }
        if (!S.equals(this.OldS)) {
            if (!this.TimerInTitle) {
                this.TL.setText(S);
            } else {
                this.setTitle(S);
            }
            this.OldS = S;
        }
        if (this.BigTimer) {
            this.BL.setTime(this.WhiteTime - this.WhiteRun, this.BlackTime
                    - this.BlackRun, this.WhiteMoves, this.BlackMoves, this.Col);
            this.BL.repaint();
        }
        if (this.Col > 0 && this.B.maincolor() > 0) {
            this.beep(this.BlackTime - this.BlackRun);
        }
        if (this.Col < 0 && this.B.maincolor() < 0) {
            this.beep(this.WhiteTime - this.WhiteRun);
        }
    }

    void start() {
        this.Started = true;
        this.Ended = false;
        this.CurrentTime = System.currentTimeMillis();
        this.BlackRun = 0;
        this.WhiteRun = 0;
        this.BlackMoves = -1;
        this.WhiteMoves = -1;
        this.Timer = new GoTimer(this, 100);
        if (this.Handicap > 0) {
            this.B.handicap(this.Handicap);
        }
    }

    @Override
    public void undo() {
        this.PF.undo();
    }

    @Override
    public void undo(int n) {
        this.B.undo(n);
    }

    @Override
    public boolean wantsmove() {
        return true;
    }

    @Override
    public void yourMove(boolean notinpos) {
        if (notinpos) {
            UnaGoSound.play("yourmove", "stone", true);
        } else {
            UnaGoSound.play("stone", "click", false);
        }
    }
}
