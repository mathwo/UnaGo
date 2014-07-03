package unagoclient.igs;

import unagoclient.Global;
import unagoclient.board.ConnectedGoFrame;
import unagoclient.board.GoTimer;
import unagoclient.board.OutputFormatter;
import unagoclient.board.TimedBoard;
import unagoclient.gui.CheckboxMenuItemAction;
import unagoclient.sound.UnaGoSound;
import rene.gui.IconBar;
import rene.util.parser.StringParser;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

/**
 * This is a ConnectedGoFrame, which is used to display boards on the server
 * (status, observing or playing). It takes care of the timer and interprets
 * menu actions (like send etc.)
 * <p>
 * The board is connected to a distributor (e.g. PlayDistributor), which
 * communicates with the IgsStream. The distributor will normally invoke a
 * second object, which parses server input and sends it to this frame.
 * <p>
 * Note that there is a timer to count down the remaining seconds.
 */

public class IgsGoFrame extends ConnectedGoFrame implements TimedBoard,
OutputListener, KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Distributor Dis; // Distributor for this
                     // frame
    String BlackName = "?", WhiteName = "?";
    int BlackTime = 0, WhiteTime = 0, BlackMoves, WhiteMoves;
    int BlackRun = 0, WhiteRun = 0;
    int GameNumber;
    GoTimer Timer;
    long CurrentTime;
    public CheckboxMenuItem Playing, Terminal, ShortLines;
    public ConnectionFrame CF;
    String Title = "";
    boolean HaveTime = false;

    String OldS = "";

    char form[] = new char[32];

    int lastbeep = 0;

    public IgsGoFrame(ConnectionFrame f, String s) {
        super(s, 19, Global.resourceString("Remove_groups"), Global
                .resourceString("Send_done"), true, true);
        this.Dis = null;
        this.CF = f;
        this.Timer = new GoTimer(this, 1000);
        this.FileMenu.addSeparator();
        this.FileMenu.add(this.Playing = new CheckboxMenuItemAction(this,
                Global.resourceString("Play")));
        this.Options.addSeparator();
        this.Options.add(this.Terminal = new CheckboxMenuItemAction(this,
                Global.resourceString("Display_Terminal_Output")));
        // Fix for Linux Java 1.5:
        try {
            this.Terminal.setState(rene.gui.Global.getParameter("getterminal",
                    true));
        } catch (final Exception e) {
        }
        this.setterminal();
        this.Options.add(this.ShortLines = new CheckboxMenuItemAction(this,
                Global.resourceString("Short_Lines_only")));
        try {
            this.ShortLines.setState(rene.gui.Global.getParameter(
                    "shortlinesonly", true));
        } catch (final Exception e) {
        }
        this.addKeyListener(this);
        this.B.addKeyListener(this);
        if (this.ExtraSendField) {
            this.SendField.addKeyListener(this);
            this.SendField.loadHistory("sendfield.history");
        }
    }

    @Override
    public void addSendForward(IconBar I) {
        I.addLeft("sendforward");
    }

    @Override
    public void alarm() {
        final long now = System.currentTimeMillis();
        if (this.B.maincolor() > 0) {
            this.BlackRun = (int) ((now - this.CurrentTime) / 1000);
            if (this.B.myColor > 0) {
                this.beep(this.BlackTime - this.BlackRun);
            }
        } else {
            this.WhiteRun = (int) ((now - this.CurrentTime) / 1000);
            if (this.B.myColor < 0) {
                this.beep(this.WhiteTime - this.WhiteRun);
            }
        }
        this.settitle1();
    }

    @Override
    public void append(String s) {
        if (s.startsWith("Board is restored to what it was when you started scoring")) {
            this.B.clearremovals();
            s = Global.resourceString("Opponent_undid_removals_");
        }
        if (this.ShortLines.getState() && s.length() > 100) {
            return;
        }
        if (s.startsWith("*")) {
            this.addComment(s);
        } else {
            this.addtoallcomments(s);
        }
    }

    @Override
    public void append(String s, Color c) {
        this.append(s);
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

    /**
     * an IgsGoFrame is blocked, when there is not Distributor left
     */
    @Override
    public boolean blocked() {
        if (this.Dis != null) {
            return this.Dis.blocked();
        } else {
            return false;
        }
    }

    public void distributor(Distributor o) {
        this.Dis = o;
        try {
            if (this.Dis != null) {
                this.Playing.setState(this.Dis.Playing);
            }
        } catch (final Exception e) {
        }
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Send").equals(o) && this.Dis != null) {
            new SendQuestion(this, this.Dis);
        } else if (this.ExtraSendField && "SendField".equals(o)
                && this.Dis != null) {
            final String s = this.SendField.getText();
            this.addComment("---> " + s);
            this.Dis.out(s);
            this.SendField.remember(s);
            this.SendField.setText("");
        } else if (Global.resourceString("Refresh").equals(o)
                && this.Dis != null) {
            this.B.deltree();
            this.Dis.refresh();
        } else if (Global.resourceString("Remove_groups").equals(o)) {
            this.B.score();
        } else if (Global.resourceString("Send_done").equals(o)) {
            if (this.B.canfinish() && this.Dis != null && this.Dis.wantsmove()) {
                this.Dis.out("done");
                this.addComment("--> done <--");
            }
        } else if (Global.resourceString("Undo").equals(o)) {
            this.B.undo();
        } else if (Global.resourceString("Load_Teaching_Game").equals(o)) {
            if (this.Teaching.getState()) {
                super.doAction(Global.resourceString("Load"));
            }
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        if (this.Dis != null && !this.Dis.once()) {
            this.Dis.remove();
        }
        this.CF.removeOutputListener(this);
        if (this.Timer != null && this.Timer.isAlive()) {
            this.Timer.stopit();
        }
        if (this.ExtraSendField) {
            this.SendField.saveHistory("sendfield.history");
        }
        super.doclose();
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

    @Override
    public void iconPressed(String s) {
        if (s.equals("sendforward") && this.Dis != null) {
            this.Dis.out(">");
        } else {
            super.iconPressed(s);
        }
    }

    @Override
    public void itemAction(String o, boolean flag) {
        if (Global.resourceString("Play").equals(o)) {
            this.Dis.Playing = flag;
        } else if (Global.resourceString("Display_Terminal_Output").equals(o)) {
            this.setterminal();
        } else if (Global.resourceString("Short_Lines_only").equals(o)) {
            rene.gui.Global.setParameter("shortlinesonly", flag);
        }
        super.itemAction(o, flag);
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        String s;
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            s = "";
        } else {
            s = Global.getFunctionKey(e.getKeyCode());
            if (s.equals("") || !this.ExtraSendField) {
                return;
            }
        }
        this.SendField.setText(s);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void movepass() {
        if (this.Dis != null) {
            this.Dis.pass();
        }
    }

    @Override
    public boolean moveset(int i, int j) {
        if (this.Dis != null) {
            if (this.B.maincolor() > 0) {
                this.Dis.set(i, j, this.BlackRun);
            } else {
                this.Dis.set(i, j, this.WhiteRun);
            }
        }
        return true;
    }

    /**
     * Called from player to set the board information. This is passed to the
     * board, which stores this information in the SGF tree (root node).
     */
    public void setinformation(String black, String blackrank, String white,
            String whiterank, String komi, String handicap) {
        this.B.setinformation(black, blackrank, white, whiterank, komi,
                handicap);
    }

    public void setterminal() {
        if (this.Terminal.getState()) {
            this.CF.addOutputListener(this);
        } else {
            this.CF.removeOutputListener(this);
        }
        rene.gui.Global.setParameter("getterminal", this.Terminal.getState());
    }

    /**
     * This is called by Player to determine the time from the move information.
     *
     * @see unagoclient.igs.Player
     */
    public void settime(String s) {
        final StringParser p = new StringParser(s);
        if (!p.skip("Game")) {
            return;
        }
        final int g = p.parseint();
        if (p.error()) {
            return;
        }
        p.skipblanks();
        if (!p.skip("I:")) {
            return;
        }
        final String w = p.parseword();
        p.skipblanks();
        if (!p.skip("(")) {
            return;
        }
        p.parseint();
        final int w2 = p.parseint();
        final int w3 = p.parseint(')');
        p.skip(")");
        if (p.error()) {
            return;
        }
        p.skipblanks();
        if (!p.skip("vs")) {
            return;
        }
        final String b = p.parseword();
        p.skipblanks();
        if (!p.skip("(")) {
            return;
        }
        p.parseint();
        final int b2 = p.parseint();
        final int b3 = p.parseint(')');
        if (!p.skip(")")) {
            return;
        }
        this.BlackName = b;
        this.WhiteName = w;
        this.BlackTime = b2;
        this.BlackMoves = b3;
        this.WhiteTime = w2;
        this.WhiteMoves = w3;
        this.GameNumber = g;
        this.BlackRun = 0;
        this.WhiteRun = 0;
        this.CurrentTime = System.currentTimeMillis();
        this.HaveTime = true;
        this.settitle1();
    }

    /**
     * called by Player to set the game title
     */
    void settitle() {
        this.HaveTime = true;
        this.settitle1();
    }

    void settitle(String s) {
        this.Title = s;
        this.setTitle(s);
    }

    void settitle1() {
        String S;
        if (this.BigTimer) {
            S = Global.resourceString("Game_") + this.GameNumber + ": "
                    + this.WhiteName + " " + this.formmoves(this.WhiteMoves)
                    + " - " + this.BlackName + " "
                    + this.formmoves(this.BlackMoves);
        } else {
            S = Global.resourceString("Game_") + this.GameNumber + ": "
                    + this.WhiteName + " "
                    + this.formtime(this.WhiteTime - this.WhiteRun) + " "
                    + this.formmoves(this.WhiteMoves) + " - " + this.BlackName
                    + " " + this.formtime(this.BlackTime - this.BlackRun) + " "
                    + this.formmoves(this.BlackMoves);
        }
        if (rene.gui.Global.getParameter("extrainformation", true)) {
            S = S + " " + this.B.extraInformation();
        }
        if (!S.equals(this.OldS)) {
            if (!this.TimerInTitle) {
                this.TL.setText(S);
            } else {
                this.setTitle(S);
            }
            this.OldS = S;
        }
        if (this.BigTimer && this.HaveTime) {
            this.BL.setTime(this.WhiteTime - this.WhiteRun, this.BlackTime
                    - this.BlackRun, this.WhiteMoves, this.BlackMoves,
                    this.B.myColor);
            this.BL.repaint();
        }
    }

    @Override
    public void undo() {
        if (this.Dis != null) {
            this.Dis.out("undo");
        }
    }

    @Override
    public boolean wantsmove() {
        if (this.Dis != null) {
            return this.Dis.wantsmove();
        } else {
            return false;
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        if (this.SendField != null) {
            this.SendField.requestFocus();
        }
    }

    /**
     * called from the board to sound an alarm
     */
    @Override
    public void yourMove(boolean notinpos) {
        if (this.Dis == null || !this.Dis.started()) {
            return;
        }
        if (notinpos) {
            if (this.Dis.wantsmove()) {
                UnaGoSound.play("yourmove", "stone", true);
            } else {
                UnaGoSound.play("stone", "click", false);
            }
        } else if (rene.gui.Global.getParameter("sound.everymove", true)) {
            UnaGoSound.play("stone", "click", true);
        } else if (this.Dis.Playing || this.Dis.newmove()) {
            UnaGoSound.play("click", "", false);
        }
    }
}
