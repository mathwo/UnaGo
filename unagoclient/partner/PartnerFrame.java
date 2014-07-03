package unagoclient.partner;

import unagoclient.CloseConnection;
import unagoclient.Dump;
import unagoclient.Global;
import unagoclient.dialogs.Help;
import unagoclient.dialogs.Message;
import unagoclient.dialogs.Question;
import unagoclient.gui.*;
import rene.util.list.ListClass;
import rene.util.list.ListElement;
import rene.util.parser.StringParser;
import rene.viewer.SystemViewer;
import rene.viewer.Viewer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * The partner frame contains a simple chat dialog and a button to start a game
 * or restore an old game. This class contains an interpreters for the partner
 * commands.
 */

public class PartnerFrame extends CloseFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    BufferedReader In;
    PrintWriter Out;
    Viewer Output;
    HistoryTextField Input;
    Socket Server;
    PartnerThread PT;
    public PartnerGoFrame PGF;
    boolean Serving;
    boolean Block;
    ListClass Moves;
    String Dir;

    public PartnerFrame(String name, boolean serving) {
        super(name);
        final JPanel p = new MyPanel();
        this.Serving = serving;
        final MenuBar menu = new MenuBar();
        this.setMenuBar(menu);
        final Menu help = new MyMenu(Global.resourceString("Help"));
        help.add(new MenuItemAction(this, Global
                .resourceString("Partner_Connection")));
        menu.setHelpMenu(help);
        p.setLayout(new BorderLayout());
        p.add("Center",
                this.Output = rene.gui.Global.getParameter("systemviewer",
                        false) ? new SystemViewer() : new Viewer());
        this.Output.setFont(Global.Monospaced);
        p.add("South", this.Input = new HistoryTextField(this, "Input"));
        this.add("Center", p);
        final JPanel p1 = new MyPanel();
        p1.add(new ButtonAction(this, Global.resourceString("Game")));
        p1.add(new ButtonAction(this, Global.resourceString("Restore_Game")));
        this.add("South", new Panel3D(p1));
        this.PGF = null;
        this.Block = false;
        this.Dir = "";
        this.Moves = new ListClass();
        this.seticon("iconn.gif");
    }

    void acceptrestore() {
        this.Out.println("@@!restore");
    }

    public void adjourn() {
        new Message(this,
                Global.resourceString("Your_Partner_closed_the_board_"));
        this.savemoves();
        this.PGF = null;
    }

    public void boardclosed(PartnerGoFrame pgf) {
        if (this.PGF == pgf) {
            this.Out.println("@@adjourn");
            this.savemoves();
        }
    }

    @Override
    public boolean close() {
        if (this.PT.isAlive()) {
            new ClosePartnerQuestion(this);
            return false;
        } else {
            return true;
        }
    }

    public boolean connect(String s, int p) {
        Dump.println("Starting partner connection");
        try {
            this.Server = new Socket(s, p);
            this.Out = new PrintWriter(new DataOutputStream(
                    this.Server.getOutputStream()), true);
            this.In = new BufferedReader(new InputStreamReader(
                    new DataInputStream(this.Server.getInputStream())));
        } catch (final Exception e) {
            return false;
        }
        this.PT = new PartnerThread(this.In, this.Out, this.Input, this.Output,
                this);
        this.PT.start();
        this.Out.println("@@name "
                + rene.gui.Global.getParameter("yourname", "No Name"));
        this.setVisible(true);
        return true;
    }

    public boolean connectvia(String server, int port, String relayserver,
            int relayport) {
        try {
            this.Server = new Socket(relayserver, relayport);
            this.Out = new PrintWriter(new DataOutputStream(
                    this.Server.getOutputStream()), true);
            this.In = new BufferedReader(new InputStreamReader(
                    new DataInputStream(this.Server.getInputStream())));
        } catch (final Exception e) {
            return false;
        }
        this.Out.println(server);
        this.Out.println("" + port);
        this.PT = new PartnerThread(this.In, this.Out, this.Input, this.Output,
                this);
        this.PT.start();
        this.Out.println("@@name "
                + rene.gui.Global.getParameter("yourname", "No Name"));
        this.setVisible(true);
        return true;
    }

    public void declineboard() {
        this.Out.println("@@-board");
    }

    public void declineendgame() {
        this.Out.println("@@-endgame");
        this.Block = false;
    }

    void declinerestore() {
        this.Out.println("@@-restore");
    }

    public void declineresult() {
        this.Out.println("@@-result");
        this.Block = false;
    }

    public void declineundo() {
        this.Out.println("@@-undo");
        this.Block = false;
    }

    @Override
    public void doAction(String o) {
        if ("Input".equals(o)) {
            this.Out.println(this.Input.getText());
            this.Input.remember(this.Input.getText());
            this.Output.append(this.Input.getText() + "\n",
                    Color.green.darker());
            this.Input.setText("");
        } else if (Global.resourceString("Game").equals(o)) {
            new GameQuestion(this);
        } else if (Global.resourceString("Restore_Game").equals(o)) {
            if (!this.Block) {
                if (this.PGF == null) {
                    this.Out.println("@@restore");
                    this.Block = true;
                } else {
                    new Message(this,
                            Global.resourceString("You_have_already_a_game_"));
                }
            }

        } else if (Global.resourceString("Partner_Connection").equals(o)) {
            new Help("partner");
        } else {
            super.doAction(o);
        }
    }

    public void doboard(int Size, String C, int Handicap, int TotalTime,
            int ExtraTime, int ExtraMoves) {
        this.PGF = new PartnerGoFrame(this, "Partner Game", C.equals("b") ? -1
                : 1, Size, TotalTime * 60, ExtraTime * 60, ExtraMoves, Handicap);
        if (C.equals("b")) {
            this.Out.println("@@!board b" + " " + Size + " " + TotalTime + " "
                    + ExtraTime + " " + ExtraMoves + " " + Handicap);
        } else {
            this.Out.println("@@!board w" + " " + Size + " " + TotalTime + " "
                    + ExtraTime + " " + ExtraMoves + " " + Handicap);
        }
        this.Moves = new ListClass();
        this.Moves.append(new ListElement(new PartnerMove("board", C
                .equals("b") ? -1 : 1, Size, TotalTime, ExtraTime, ExtraMoves,
                        Handicap)));
    }

    @Override
    public void doclose() {
        Global.notewindow(this, "partner");
        this.Out.println("@@@@end");
        this.Out.close();
        new CloseConnection(this.Server, this.In);
        super.doclose();
    }

    public void doendgame() {
        this.Out.println("@@!endgame");
        this.PGF.doscore();
        this.Block = false;
    }

    public void dorequest(int s, String c, int h, int tt, int et, int em) {
        this.Out.println("@@board " + c + " " + s + " " + tt + " " + et + " "
                + em + " " + h);
        this.Block = true;
    }

    void dorestore() {
        if (this.PGF != null) {
            return;
        }
        final FileDialog fd = new FileDialog(this,
                Global.resourceString("Load_Game"), FileDialog.LOAD);
        if (!this.Dir.equals("")) {
            fd.setDirectory(this.Dir);
        }
        fd.setFile("*.sto");
        fd.setVisible(true);
        String fn = fd.getFile();
        if (fn == null) {
            return;
        }
        this.Dir = fd.getDirectory();
        if (fn.endsWith(".*.*")) // Windows 95 JDK bug
        {
            fn = fn.substring(0, fn.length() - 4);
        }
        try
        // print out using the board class
        {
            final BufferedReader fi = new BufferedReader(new InputStreamReader(
                    new DataInputStream(new FileInputStream(fd.getDirectory()
                            + fn))));
            this.Moves = new ListClass();
            while (true) {
                final String s = fi.readLine();
                if (s == null) {
                    break;
                }
                new StringParser(s);
                this.Out.println("@@@" + s);
                this.moveinterpret(s, true);
            }
            if (this.PGF != null) {
                this.Out.println("@@start");
            }
            fi.close();
        } catch (final IOException ex) {
        }
    }

    public void doresult(int b, int w) {
        this.Out.println("@@!result " + b + " " + w);
        this.Output.append(Global.resourceString("Game_Result__B_") + b
                + Global.resourceString("__W_") + w + "\n",
                Color.green.darker());
        this.Block = false;
    }

    public void dosave() {
        final FileDialog fd = new FileDialog(this,
                Global.resourceString("Save_Game"), FileDialog.SAVE);
        if (!this.Dir.equals("")) {
            fd.setDirectory(this.Dir);
        }
        fd.setFile("*.sto");
        fd.setVisible(true);
        String fn = fd.getFile();
        if (fn == null) {
            return;
        }
        this.Dir = fd.getDirectory();
        if (fn.endsWith(".*.*")) // Windows 95 JDK bug
        {
            fn = fn.substring(0, fn.length() - 4);
        }
        try
        // print out using the board class
        {
            final PrintWriter fo = new PrintWriter(new FileOutputStream(
                    fd.getDirectory() + fn), true);
            ListElement lm = this.Moves.first();
            while (lm != null) {
                final PartnerMove m = (PartnerMove) lm.content();
                fo.println(m.Type + " " + m.P1 + " " + m.P2 + " " + m.P3 + " "
                        + m.P4 + " " + m.P5 + " " + m.P6);
                lm = lm.next();
            }
            fo.close();
        } catch (final IOException ex) {
        }
    }

    public void doundo() {
        this.Out.println("@@!undo");
        this.PGF.undo(2);
        this.Moves.remove(this.Moves.last());
        this.Moves.remove(this.Moves.last());
        this.Block = false;
        this.PGF.addtime(30);
    }

    public void endgame() {
        if (this.Block) {
            return;
        }
        this.Block = true;
        this.Out.println("@@endgame");
    }

    /**
     * The interpreter for the partner commands (all start with @@).
     */
    public void interpret(String s) {
        if (s.startsWith("@@name")) {
            final StringParser p = new StringParser(s);
            p.skip("@@name");
            this.setTitle(Global.resourceString("Connection_to_") + p.upto('!'));
        } else if (s.startsWith("@@board")) {
            if (this.PGF != null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skip("@@board");
            final String color = p.parseword();
            if (color.equals("b")) {
            } else {
            }
            final int Size = p.parseint();
            final int TotalTime = p.parseint();
            final int ExtraTime = p.parseint();
            final int ExtraMoves = p.parseint();
            final int Handicap = p.parseint();
            new BoardQuestion(this, Size, color, Handicap, TotalTime,
                    ExtraTime, ExtraMoves);
        } else if (s.startsWith("@@!board")) {
            if (this.PGF != null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skip("@@!board");
            final String color = p.parseword();
            int C;
            if (color.equals("b")) {
                C = 1;
            } else {
                C = -1;
            }
            final int Size = p.parseint();
            final int TotalTime = p.parseint();
            final int ExtraTime = p.parseint();
            final int ExtraMoves = p.parseint();
            final int Handicap = p.parseint();
            this.PGF = new PartnerGoFrame(this,
                    Global.resourceString("Partner_Game"), C, Size,
                    TotalTime * 60, ExtraTime * 60, ExtraMoves, Handicap);
            this.Out.println("@@start");
            this.Block = false;
            this.Moves = new ListClass();
            this.Moves.append(new ListElement(new PartnerMove("board", C, Size,
                    TotalTime, ExtraTime, ExtraMoves, Handicap)));
        } else if (s.startsWith("@@-board")) {
            new Message(this,
                    Global.resourceString("Partner_declines_the_game_"));
            this.Block = false;
        } else if (s.startsWith("@@start")) {
            if (this.PGF == null) {
                return;
            }
            this.PGF.start();
            this.Out.println("@@!start");
        } else if (s.startsWith("@@!start")) {
            if (this.PGF == null) {
                return;
            }
            this.PGF.start();
        } else if (s.startsWith("@@move")) {
            if (this.PGF == null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skip("@@move");
            final String color = p.parseword();
            final int i = p.parseint(), j = p.parseint();
            final int bt = p.parseint(), bm = p.parseint();
            final int wt = p.parseint(), wm = p.parseint();
            Dump.println("Move of " + color + " at " + i + "," + j);
            if (color.equals("b")) {
                if (this.PGF.maincolor() < 0) {
                    return;
                }
                this.PGF.black(i, j);
                this.Moves.append(new ListElement(new PartnerMove("b", i, j,
                        bt, bm, wt, wm)));
            } else {
                if (this.PGF.maincolor() > 0) {
                    return;
                }
                this.PGF.white(i, j);
                this.Moves.append(new ListElement(new PartnerMove("w", i, j,
                        bt, bm, wt, wm)));
            }
            this.PGF.settimes(bt, bm, wt, wm);
            this.Out.println("@@!move " + color + " " + i + " " + j + " " + bt
                    + " " + bm + " " + wt + " " + wm);
        } else if (s.startsWith("@@!move")) {
            if (this.PGF == null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skip("@@!move");
            final String color = p.parseword();
            final int i = p.parseint(), j = p.parseint();
            final int bt = p.parseint(), bm = p.parseint();
            final int wt = p.parseint(), wm = p.parseint();
            Dump.println("Move of " + color + " at " + i + "," + j);
            if (color.equals("b")) {
                if (this.PGF.maincolor() < 0) {
                    return;
                }
                this.PGF.black(i, j);
                this.Moves.append(new ListElement(new PartnerMove("b", i, j,
                        bt, bm, wt, wm)));
            } else {
                if (this.PGF.maincolor() > 0) {
                    return;
                }
                this.PGF.white(i, j);
                this.Moves.append(new ListElement(new PartnerMove("w", i, j,
                        bt, bm, wt, wm)));
            }
            this.PGF.settimes(bt, bm, wt, wm);
        } else if (s.startsWith("@@pass")) {
            if (this.PGF == null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skip("@@pass");
            final int bt = p.parseint(), bm = p.parseint();
            final int wt = p.parseint(), wm = p.parseint();
            Dump.println("Pass");
            this.PGF.dopass();
            this.PGF.settimes(bt, bm, wt, wm);
            this.Moves.append(new ListElement(new PartnerMove("pass", bt, bm,
                    wt, wm)));
            this.Out.println("@@!pass " + bt + " " + bm + " " + wt + " " + wm);
        } else if (s.startsWith("@@!pass")) {
            if (this.PGF == null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skip("@@!pass");
            final int bt = p.parseint(), bm = p.parseint();
            final int wt = p.parseint(), wm = p.parseint();
            Dump.println("Pass");
            this.PGF.dopass();
            this.Moves.append(new ListElement(new PartnerMove("pass", bt, bm,
                    wt, wm)));
            this.PGF.settimes(bt, bm, wt, wm);
        } else if (s.startsWith("@@endgame")) {
            if (this.PGF == null) {
                return;
            }
            new EndGameQuestion(this);
            this.Block = true;
        } else if (s.startsWith("@@!endgame")) {
            if (this.PGF == null) {
                return;
            }
            this.PGF.doscore();
            this.Block = false;
        } else if (s.startsWith("@@-endgame")) {
            if (this.PGF == null) {
                return;
            }
            new Message(this, "Partner declines game end!");
            this.Block = false;
        } else if (s.startsWith("@@result")) {
            if (this.PGF == null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skip("@@result");
            final int b = p.parseint();
            final int w = p.parseint();
            Dump.println("Result " + b + " " + w);
            new ResultQuestion(this, Global.resourceString("Accept_result__B_")
                    + b + Global.resourceString("__W_") + w + "?", b, w);
            this.Block = true;
        } else if (s.startsWith("@@!result")) {
            if (this.PGF == null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skip("@@!result");
            final int b = p.parseint();
            final int w = p.parseint();
            Dump.println("Result " + b + " " + w);
            this.Output.append(Global.resourceString("Game_Result__B_") + b
                    + Global.resourceString("__W_") + w + "\n",
                    Color.green.darker());
            new Message(this, Global.resourceString("Result__B_") + b
                    + Global.resourceString("__W_") + w + " was accepted!");
            this.Block = false;
        } else if (s.startsWith("@@-result")) {
            if (this.PGF == null) {
                return;
            }
            new Message(this, Global.resourceString("Partner_declines_result_"));
            this.Block = false;
        } else if (s.startsWith("@@undo")) {
            if (this.PGF == null) {
                return;
            }
            new UndoQuestion(this);
            this.Block = true;
        } else if (s.startsWith("@@-undo")) {
            if (this.PGF == null) {
                return;
            }
            new Message(this, Global.resourceString("Partner_declines_undo_"));
            this.Block = false;
        } else if (s.startsWith("@@!undo")) {
            if (this.PGF == null) {
                return;
            }
            this.PGF.undo(2);
            this.Moves.remove(this.Moves.last());
            this.Moves.remove(this.Moves.last());
            this.PGF.addothertime(30);
            this.Block = false;
        } else if (s.startsWith("@@adjourn")) {
            this.adjourn();
        } else if (s.startsWith("@@restore")) {
            final Question Q = new Question(
                    this,
                    Global.resourceString("Your_partner_wants_to_restore_a_game_"),
                    Global.resourceString("Accept"), null, true);
            Q.setVisible(true);
            if (Q.result()) {
                this.acceptrestore();
            } else {
                this.declinerestore();
            }
        } else if (s.startsWith("@@-restore")) {
            new Message(this,
                    Global.resourceString("Partner_declines_restore_"));
            this.Block = false;
        } else if (s.startsWith("@@!restore")) {
            this.dorestore();
            this.Block = false;
        } else if (s.startsWith("@@@")) {
            this.moveinterpret(s.substring(3), false);
        }
    }

    void moveinterpret(String s, boolean fromhere) {
        final StringParser p = new StringParser(s);
        final String c = p.parseword();
        final int p1 = p.parseint();
        final int p2 = p.parseint();
        final int p3 = p.parseint();
        final int p4 = p.parseint();
        final int p5 = p.parseint();
        final int p6 = p.parseint();
        if (c.equals("board")) {
            this.PGF = new PartnerGoFrame(this,
                    Global.resourceString("Partner_Game"), fromhere ? p1 : -p1,
                            p2, p3 * 60, p4 * 60, p5, p6);
            this.PGF.setHandicap();
        } else if (this.PGF != null && c.equals("b")) {
            this.PGF.black(p1, p2);
            this.PGF.settimes(p3, p4, p5, p6);
        } else if (this.PGF != null && c.equals("w")) {
            this.PGF.white(p1, p2);
            this.PGF.settimes(p3, p4, p5, p6);
        } else if (this.PGF != null && c.equals("pass")) {
            this.PGF.pass();
            this.PGF.settimes(p1, p2, p3, p4);
        }
        this.Moves.append(new ListElement(new PartnerMove(c, p1, p2, p3, p4,
                p5, p6)));
    }

    public boolean moveset(String c, int i, int j, int bt, int bm, int wt,
            int wm) {
        if (this.Block) {
            return false;
        }
        this.Out.println("@@move " + c + " " + i + " " + j + " " + bt + " "
                + bm + " " + wt + " " + wm);
        return true;
    }

    public void open(Socket server) {
        Dump.println("Starting partner server");
        this.Server = server;
        try {
            this.Out = new PrintWriter(new DataOutputStream(
                    this.Server.getOutputStream()), true);
            this.In = new BufferedReader(new InputStreamReader(
                    new DataInputStream(this.Server.getInputStream())));
        } catch (final Exception e) {
            Dump.println("---> no connection");
            new Message(this, Global.resourceString("Got_no_Connection_"));
            return;
        }
        this.PT = new PartnerThread(this.In, this.Out, this.Input, this.Output,
                this);
        this.PT.start();
    }

    public void out(String s) {
        this.Out.println(s);
    }

    public void pass(int bt, int bm, int wt, int wm) {
        this.Out.println("@@pass " + bt + " " + bm + " " + wt + " " + wm);
    }

    public void refresh() {
    }

    public void savemoves() {
        final Question Q = new Question(this,
                Global.resourceString("Save_this_game_for_reload_"),
                Global.resourceString("Yes"), null, true);
        Q.setVisible(true);
        if (Q.result()) {
            this.dosave();
        }
    }

    public void set(int i, int j) {
    }

    public void undo() {
        if (this.Block) {
            return;
        }
        this.Block = true;
        this.Out.println("@@undo");
    }
}

class PartnerMove {
    public String Type;
    public int P1, P2, P3, P4, P5, P6;

    public PartnerMove(String type, int p1, int p2, int p3, int p4) {
        this.Type = type;
        this.P1 = p1;
        this.P2 = p2;
        this.P3 = p3;
        this.P4 = p4;
    }

    public PartnerMove(String type, int p1, int p2, int p3, int p4, int p5,
            int p6) {
        this.Type = type;
        this.P1 = p1;
        this.P2 = p2;
        this.P3 = p3;
        this.P4 = p4;
        this.P5 = p5;
        this.P6 = p6;
    }
}
