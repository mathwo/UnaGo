package unagoclient.gmp;

import unagoclient.Dump;
import unagoclient.Global;
import unagoclient.board.ConnectedGoFrame;
import unagoclient.board.GoTimer;
import unagoclient.board.TimedBoard;
import unagoclient.dialogs.Message;

public class GMPGoFrame extends ConnectedGoFrame implements TimedBoard {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GMPConnection C;
    boolean WantMove = true;
    int BlackTime = 0, WhiteTime = 0;
    long CurrentTime;
    GoTimer Timer = null;
    int MyColor;

    public GMPGoFrame(GMPConnection c, int size, int color) {
        super(Global.resourceString("Play_Go"), size, Global
                .resourceString("Remove_groups"), Global.resourceString(""),
                false, false);
        this.MyColor = color;
        this.C = c;
        this.Timer = new GoTimer(this, 1000);
        this.CurrentTime = System.currentTimeMillis();
        this.setVisible(true);
        this.repaint();
    }

    @Override
    public void alarm() {
        final long now = System.currentTimeMillis();
        int BlackRun = this.BlackTime;
        int WhiteRun = this.WhiteTime;
        if (this.B.maincolor() > 0) {
            BlackRun += (int) ((now - this.CurrentTime) / 1000);
        } else {
            WhiteRun += (int) ((now - this.CurrentTime) / 1000);
        }
        if (this.BigTimer) {
            this.BL.setTime(WhiteRun, BlackRun, 0, 0, 0);
            this.BL.repaint();
        }
    }

    @Override
    public void color(int c) {
        if (c == GMPConnector.WHITE) {
            super.color(-1);
        } else {
            super.color(1);
        }
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Remove_groups").equals(o)) {
            this.WantMove = false;
            this.B.score();
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        this.C.doclose();
        if (this.Timer != null && this.Timer.isAlive()) {
            this.Timer.stopit();
        }
        super.doclose();
    }

    public void doundo(int n) {
        this.B.undo(n);
    }

    public void gotMove(int color, int i, int j) {
        synchronized (this.B) {
            if (this.B.maincolor() == color) {
                return;
            }
            this.updateTime();
            if (color == GMPConnector.WHITE) {
                this.white(i, j);
            } else {
                this.black(i, j);
            }
            this.B.showinformation();
            this.B.copy();
        }
    }

    public void gotPass(int color) {
        this.updateTime();
        Dump.println("Opponent passed");
        this.B.setpass();
        new Message(this, Global.resourceString("Pass"));
    }

    public void gotSet(int color, int i, int j) {
        this.updateTime();
        if (color == GMPConnector.WHITE) {
            this.setwhite(i, j);
        } else {
            this.setblack(i, j);
        }
    }

    @Override
    public boolean moveset(int i, int j) {
        if (this.B.maincolor() == this.MyColor) {
            return false;
        }
        this.updateTime();
        Dump.println("Move at " + i + " " + j);
        this.C.moveset(i, j);
        this.updateTime();
        return true;
    }

    @Override
    public void notepass() {
        if (this.B.maincolor() == this.MyColor) {
            return;
        }
        this.updateTime();
        Dump.println("I pass");
        this.C.pass();
        this.B.setpass();
    }

    @Override
    public void undo() {
        if (this.B.maincolor() == this.MyColor) {
            return;
        }
        this.C.undo();
        this.doundo(2);
    }

    public void updateTime() {
        final long now = System.currentTimeMillis();
        if (this.B.maincolor() > 0) {
            this.BlackTime += (int) ((now - this.CurrentTime) / 1000);
        } else {
            this.WhiteTime += (int) ((now - this.CurrentTime) / 1000);
        }
        this.CurrentTime = now;
        this.alarm();
    }

    @Override
    public boolean wantsmove() {
        return this.WantMove;
    }

}
