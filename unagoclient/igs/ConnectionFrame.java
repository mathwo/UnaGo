package unagoclient.igs;

import unagoclient.CloseConnection;
import unagoclient.Dump;
import unagoclient.Global;
import unagoclient.dialogs.GetParameter;
import unagoclient.dialogs.Help;
import unagoclient.dialogs.Message;
import unagoclient.dialogs.Question;
import unagoclient.gui.*;
import unagoclient.igs.connection.Connection;
import unagoclient.igs.games.GamesFrame;
import unagoclient.igs.who.WhoFrame;
import rene.util.list.ListClass;
import rene.util.list.ListElement;
import rene.viewer.SystemViewer;
import rene.viewer.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

class CloseConnectionQuestion extends Question {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CloseConnectionQuestion(ConnectionFrame g) {
        super(g, Global.resourceString("This_will_close_your_connection_"),
                Global.resourceString("Close"), g, true);
        this.setVisible(true);
    }
}

/**
 * This frame contains a menu, a text area for the server output, a text area to
 * send commands to the server and buttons to call who, games etc.
 */

public class ConnectionFrame extends CloseFrame implements KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GridBagLayout girdbag;
    Viewer Output;
    HistoryTextField Input;
    GridBagLayout gridbag;
    public WhoFrame Who;
    public GamesFrame Games;

    Socket Server;
    PrintWriter Out;
    public DataOutputStream Outstream;
    String Encoding;
    IgsStream In;
    ReceiveThread RT;
    String Dir;
    JTextField Game;
    CheckboxMenuItem CheckInfo, CheckMessages, CheckErrors, ReducedOutput,
            AutoReply;
    public int MoveStyle = Connection.MOVE;
    JTextField WhoRange; // Kyu/Dan
    // range
    // for
    // the
    // who
    // command.
    String Waitfor; // Pop
    // a
    // a
    // message,
    // when
    // this
    // player
    // connects.
    ListClass OL; // List
    // of
    // Output-Listeners
    String Reply;

    public boolean hasClosed = false; // note

    // that
    // the
    // user
    // closed
    // the
    // window

    public ConnectionFrame(String Name, String encoding) {
        super(Name);
        this.Encoding = encoding;
        this.Waitfor = "";
        this.OL = new ListClass();
        this.setLayout(new BorderLayout());
        // Menu
        final MenuBar M = new MenuBar();
        this.setMenuBar(M);
        final Menu file = new MyMenu(Global.resourceString("File"));
        M.add(file);
        file.add(new MenuItemAction(this, Global.resourceString("Save")));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global.resourceString("Clear")));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global.resourceString("Close")));
        final Menu options = new MyMenu(Global.resourceString("Options"));
        M.add(options);
        options.add(this.AutoReply = new CheckboxMenuItemAction(this, Global
                .resourceString("Auto_Reply")));
        this.AutoReply.setState(false);
        options.add(new MenuItemAction(this, Global.resourceString("Set_Reply")));
        this.Reply = rene.gui.Global.getParameter("autoreply",
                "I am busy! Please, try later.");
        options.addSeparator();
        options.add(this.CheckInfo = new CheckboxMenuItemAction(this, Global
                .resourceString("Show_Information")));
        this.CheckInfo.setState(rene.gui.Global.getParameter("showinformation",
                false));
        options.add(this.CheckMessages = new CheckboxMenuItemAction(this,
                Global.resourceString("Show_Messages")));
        this.CheckMessages.setState(rene.gui.Global.getParameter(
                "showmessages", true));
        options.add(this.CheckErrors = new CheckboxMenuItemAction(this, Global
                .resourceString("Show_Errors")));
        this.CheckErrors.setState(rene.gui.Global.getParameter("showerrors",
                false));
        options.add(this.ReducedOutput = new CheckboxMenuItemAction(this,
                Global.resourceString("Reduced_Output")));
        this.ReducedOutput.setState(rene.gui.Global.getParameter(
                "reducedoutput", true));
        options.add(new MenuItemAction(this, Global
                .resourceString("Wait_for_Player")));
        final Menu help = new MyMenu(Global.resourceString("Help"));
        help.add(new MenuItemAction(this, Global
                .resourceString("Terminal_Window")));
        help.add(new MenuItemAction(this, Global.resourceString("Server_Help")));
        help.add(new MenuItemAction(this, Global.resourceString("Channels")));
        help.add(new MenuItemAction(this, Global
                .resourceString("Observing_Playing")));
        help.add(new MenuItemAction(this, Global.resourceString("Teaching")));
        M.setHelpMenu(help);
        final MyPanel center = new MyPanel();
        center.setLayout(new BorderLayout());
        // Text
        this.Output = rene.gui.Global.getParameter("systemviewer", false) ? new SystemViewer()
        : new Viewer();
        this.Output.setFont(Global.Monospaced);
        // Output.setBackground(Global.gray);
        center.add("Center", this.Output);
        // Input
        this.Input = new HistoryTextField(this, "Input");
        this.Input.loadHistory("input.history");
        center.add("South", this.Input);
        this.add("Center", center);
        // Buttons:
        final MyPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Who")));
        p.add(this.WhoRange = new HistoryTextField(this, "WhoRange", 5));
        this.WhoRange.setText(rene.gui.Global
                .getParameter("whorange", "20k-8d"));
        p.add(new ButtonAction(this, Global.resourceString("Games")));
        p.add(new MyLabel(" "));
        p.add(new ButtonAction(this, Global.resourceString("Peek")));
        p.add(new ButtonAction(this, Global.resourceString("Status")));
        p.add(new ButtonAction(this, Global.resourceString("Observe")));
        this.Game = new GrayTextField(4);
        p.add(this.Game);
        this.add("South", new Panel3D(p));
        //
        this.Dir = new String("");
        this.seticon("iconn.gif");
        this.addKeyListener(this);
        this.Input.addKeyListener(this);
    }

    public void addOutputListener(OutputListener l) {
        this.OL.append(new ListElement(l));
    }

    public void append(String s) {
        this.append(s, Color.blue.darker());
    }

    public void append(String s, Color c) {
        this.Output.append(s + "\n", c);
        ListElement e = this.OL.first();
        while (e != null) {
            final OutputListener ol = (OutputListener) e.content();
            ol.append(s, c);
            e = e.next();
        }
    }

    @Override
    public boolean close() {
        if (this.RT.isAlive()) {
            if (rene.gui.Global.getParameter("confirmations", true)) {
                return new CloseConnectionQuestion(this).result();
            }
        }
        return true;
    }

    public void command(String os) {
        if (os.startsWith(" ")) {
            os = os.trim();
        } else {
            this.append(os, Color.green.darker());
        }
        if (rene.gui.Global.getParameter("gameswindow", true)
                && os.toLowerCase().startsWith("games")) {
            this.Input.setText("");
            this.doAction(Global.resourceString("Games"));
        } else if (rene.gui.Global.getParameter("whowindow", true)
                && os.toLowerCase().startsWith("who")) {
            this.Input.setText("");
            if (os.length() > 4) {
                os = os.substring(4).trim();
                if (!os.equals("")) {
                    this.WhoRange.setText(os);
                }
            }
            this.doAction(Global.resourceString("Who"));
        } else if (os.toLowerCase().startsWith("observe")) {
            this.Input.setText("");
            if (os.length() > 7) {
                os = os.substring(7).trim();
                if (!os.equals("")) {
                    this.Game.setText(os);
                }
                this.doAction(Global.resourceString("Observe"));
            } else {
                this.append("Observe needs a game number", Color.red);
            }
        } else if (os.toLowerCase().startsWith("peek")) {
            this.Input.setText("");
            if (os.length() > 5) {
                os = os.substring(5).trim();
                if (!os.equals("")) {
                    this.Game.setText(os);
                }
                this.doAction(Global.resourceString("Peek"));
            } else {
                this.append("Peek needs a game number", Color.red);
            }
        } else if (os.toLowerCase().startsWith("status")) {
            this.Input.setText("");
            if (os.length() > 6) {
                os = os.substring(6).trim();
                if (!os.equals("")) {
                    this.Game.setText(os);
                }
                this.doAction(Global.resourceString("Status"));
            } else {
                this.append("Status needs a game number", Color.red);
            }
        } else if (os.toLowerCase().startsWith("moves")) {
            new Message(this,
                    Global.resourceString("Do_not_enter_this_command_here_"));
        } else {
            if (!this.Input.getText().startsWith(" ")) {
                this.Input.remember(os);
            }
            this.Out.println(os);
            this.Input.setText("");
        }
    }

    /**
     * Tries to connect to the server using IgsStream. Upon success, it starts
     * the ReceiveThread, which handles the login and then all input from the
     * server, scanned by IgsStream.
     * <p>
     * Then it starts some default distributors, shows itself and returns true.
     *
     * @returns success of failure
     * @see unagoclient.igs.IgsStream
     * @see unagoclient.igs.ReceiveThread
     */
    public boolean connect(String server, int port, String user,
            String password, boolean proxy) {
        try {
            this.Server = new Socket(server, port);
            String encoding = this.Encoding;
            if (encoding.startsWith("!")) {
                encoding = encoding.substring(1);
            }
            if (encoding.equals("")) {
                this.Out = new RefreshWriter(new OutputStreamWriter(
                        this.Outstream = new DataOutputStream(
                                this.Server.getOutputStream())), true);
            } else {
                this.Out = new RefreshWriter(new OutputStreamWriter(
                        this.Outstream = new DataOutputStream(
                                this.Server.getOutputStream()), encoding), true);
            }
        } catch (final UnsupportedEncodingException e) {
            try {
                this.Out = new RefreshWriter(new OutputStreamWriter(
                        this.Outstream = new DataOutputStream(
                                this.Server.getOutputStream())), true);
            } catch (final Exception ex) {
                return false;
            }
        } catch (final IllegalArgumentException e) {
            try {
                this.Out = new RefreshWriter(new OutputStreamWriter(
                        this.Outstream = new DataOutputStream(
                                this.Server.getOutputStream())), true);
            } catch (final Exception ex) {
                return false;
            }
        } catch (final IOException e) {
            return false;
        }
        try { /*
         * if (proxy) In= new
         * ProxyIgsStream(this,Server.getInputStream(),Out); else
         */
            this.In = new IgsStream(this, this.Server.getInputStream(),
                    this.Out);
        } catch (final Exception e) {
            return false;
        }
        this.setVisible(true);
        this.RT = new ReceiveThread(this.Output, this.In, this.Out, user,
                password, proxy, this);
        this.RT.start();
        new PlayDistributor(this, this.In, this.Out);
        new MessageDistributor(this, this.In, this.Out);
        new ErrorDistributor(this, this.In, this.Out);
        new InformationDistributor(this, this.In, this.Out);
        new SayDistributor(this, this.In, this.Out);
        return true;
    }

    public boolean connectvia(String server, int port, String user,
            String password, String relayserver, int relayport) {
        try {
            this.Server = new Socket(relayserver, relayport);
            String encoding = this.Encoding;
            if (encoding.startsWith("!")) {
                encoding = encoding.substring(1);
            }
            if (encoding.equals("")) {
                this.Out = new RefreshWriter(new OutputStreamWriter(
                        this.Outstream = new DataOutputStream(
                                this.Server.getOutputStream())), true);
            } else {
                this.Out = new RefreshWriter(new OutputStreamWriter(
                        this.Outstream = new DataOutputStream(
                                this.Server.getOutputStream()), encoding), true);
            }
        } catch (final UnsupportedEncodingException e) {
            try {
                this.Out = new RefreshWriter(new OutputStreamWriter(
                        this.Outstream = new DataOutputStream(
                                this.Server.getOutputStream())), true);
            } catch (final Exception ex) {
                return false;
            }
        } catch (final IllegalArgumentException e) {
            try {
                this.Out = new RefreshWriter(new OutputStreamWriter(
                        this.Outstream = new DataOutputStream(
                                this.Server.getOutputStream())), true);
            } catch (final Exception ex) {
                return false;
            }
        } catch (final IOException e) {
            return false;
        }
        try {
            this.In = new IgsStream(this, this.Server.getInputStream(),
                    this.Out);
        } catch (final Exception e) {
            return false;
        }
        this.Out.println(server);
        this.Out.println("" + port);
        this.setVisible(true);
        this.RT = new ReceiveThread(this.Output, this.In, this.Out, user,
                password, false, this);
        this.RT.start();
        new PlayDistributor(this, this.In, this.Out);
        new MessageDistributor(this, this.In, this.Out);
        new ErrorDistributor(this, this.In, this.Out);
        new InformationDistributor(this, this.In, this.Out);
        new SayDistributor(this, this.In, this.Out);
        return true;
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Close").equals(o)) {
            if (this.close()) {
                this.doclose();
            }
        } else if (Global.resourceString("Clear").equals(o)) {
            this.Output.setText("");
        } else if (Global.resourceString("Save").equals(o)) {
            final FileDialog fd = new FileDialog(this,
                    Global.resourceString("Save_Game"), FileDialog.SAVE);
            if (!this.Dir.equals("")) {
                fd.setDirectory(this.Dir);
            }
            fd.setFile("*.txt");
            fd.setVisible(true);
            final String fn = fd.getFile();
            if (fn == null) {
                return;
            }
            this.Dir = fd.getDirectory();
            try {
                PrintWriter fo;
                if (this.Encoding.equals("")) {
                    fo = new PrintWriter(new OutputStreamWriter(
                            new FileOutputStream(fd.getDirectory() + fn)), true);
                } else {
                    fo = new PrintWriter(new OutputStreamWriter(
                            new FileOutputStream(fd.getDirectory() + fn),
                            this.Encoding), true);
                }
                this.Output.save(fo);
                fo.close();
            } catch (final IOException ex) {
                System.err.println(Global.resourceString("Error_on__") + fn);
            }
        } else if (Global.resourceString("Who").equals(o)) {
            this.goclient();
            if (rene.gui.Global.getParameter("whowindow", true)) {
                if (this.Who != null) {
                    this.Who.refresh();
                    this.Who.requestFocus();
                    return;
                }
                this.Who = new WhoFrame(this, this.Out, this.In,
                        this.WhoRange.getText());
                Global.setwindow(this.Who, "who", 300, 400);
                this.Who.setVisible(true);
                this.Who.refresh();
            } else {
                if (this.WhoRange.getText().equals("")) {
                    this.command("who");
                } else {
                    this.command("who " + this.WhoRange.getText());
                }
            }
        } else if (Global.resourceString("Games").equals(o)) {
            this.goclient();
            if (rene.gui.Global.getParameter("gameswindow", true)) {
                if (this.Games != null) {
                    this.Games.refresh();
                    this.Games.requestFocus();
                    return;
                }
                this.Games = new GamesFrame(this, this.Out, this.In);
                Global.setwindow(this.Games, "games", 500, 400);
                this.Games.setVisible(true);
                this.Games.refresh();
            } else {
                this.command("games");
            }
        } else if (Global.resourceString("Peek").equals(o)) {
            this.goclient();
            int n;
            try {
                n = Integer.parseInt(this.Game.getText());
                this.peek(n);
            } catch (final NumberFormatException ex) {
                return;
            }
        } else if (Global.resourceString("Status").equals(o)) {
            this.goclient();
            int n;
            try {
                n = Integer.parseInt(this.Game.getText());
                this.status(n);
            } catch (final NumberFormatException ex) {
                return;
            }
        } else if (Global.resourceString("Observe").equals(o)) {
            this.goclient();
            int n;
            try {
                n = Integer.parseInt(this.Game.getText());
                this.observe(n);
            } catch (final NumberFormatException ex) {
                return;
            }
        } else if (Global.resourceString("Terminal_Window").equals(o)) {
            new Help("terminal");
        } else if (Global.resourceString("Server_Help").equals(o)) {
            new Help("server");
        } else if (Global.resourceString("Channels").equals(o)) {
            new Help("channels");
        } else if (Global.resourceString("Observing_Playing").equals(o)) {
            new Help("obsplay");
        } else if (Global.resourceString("Teaching").equals(o)) {
            new Help("teaching");
        } else if ("Input".equals(o)) {
            final String os = this.Input.getText();
            this.command(os);
        } else if (Global.resourceString("Wait_for_Player").equals(o)) {
            new GetWaitfor(this);
        } else if (Global.resourceString("Set_Reply").equals(o)) {
            new GetReply(this);
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        Global.notewindow(this, "connection");
        this.hasClosed = true;
        this.Input.saveHistory("input.history");
        if (this.In != null) {
            this.In.removeall();
        }
        this.Out.println("quit");
        this.Out.close();
        Dump.println("doclose() called in connection");
        new CloseConnection(this.Server, this.In.getInputStream());
        this.inform();
        super.doclose();
    }

    public void goclient() {
        this.RT.goclient();
    }

    @Override
    public void itemAction(String o, boolean flag) {
        if (Global.resourceString("Show_Information").equals(o)) {
            rene.gui.Global.setParameter("showinformation", flag);
        } else if (Global.resourceString("Show_Messages").equals(o)) {
            rene.gui.Global.setParameter("showmessages", flag);
        } else if (Global.resourceString("Show_Errors").equals(o)) {
            rene.gui.Global.setParameter("showerrors", flag);
        } else if (Global.resourceString("Reduced_Output").equals(o)) {
            rene.gui.Global.setParameter("reducedoutput", flag);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        final String s = Global.getFunctionKey(e.getKeyCode());
        if (s.equals("")) {
            return;
        }
        this.Input.setText(s);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    void movestyle(int style) {
        this.MoveStyle = style;
    }

    public void observe(int n) {
        if (this.In.gamewaiting(n)) {
            new Message(
                    this,
                    Global.resourceString("There_is_already_a_board_for_this_game_"));
            return;
        }
        final IgsGoFrame gf = new IgsGoFrame(this,
                Global.resourceString("Observe_game"));
        gf.setVisible(true);
        gf.repaint();
        new GoObserver(gf, this.In, this.Out, n);
    }

    public void out(String s) {
        if (s.startsWith("observe") || s.startsWith("status")
                || s.startsWith("moves")) {
            return;
        }
        Dump.println("---> " + s);
        this.Out.println(s);
    }

    public void peek(int n) {
        if (this.In.gamewaiting(n)) {
            new Message(
                    this,
                    Global.resourceString("There_is_already_a_board_for_this_game_"));
            return;
        }
        final IgsGoFrame gf = new IgsGoFrame(this,
                Global.resourceString("Peek_game"));
        gf.setVisible(true);
        gf.repaint();
        new Peeker(gf, this.In, this.Out, n);
    }

    public void removeOutputListener(OutputListener l) {
        ListElement e = this.OL.first();
        while (e != null) {
            final OutputListener ol = (OutputListener) e.content();
            if (ol == l) {
                this.OL.remove(e);
                return;
            }
            e = e.next();
        }
    }

    String reply() {
        if (this.AutoReply.getState()) {
            return this.Reply;
        } else {
            return "";
        }
    }

    public void status(int n) {
        final IgsGoFrame gf = new IgsGoFrame(this,
                Global.resourceString("Peek_game"));
        gf.setVisible(true);
        gf.repaint();
        new Status(gf, this.In, this.Out, n);
    }

    public boolean wantserrors() {
        return this.CheckErrors.getState() && Global.Silent <= 0;
    }

    public boolean wantsinformation() {
        return this.CheckInfo.getState() && Global.Silent <= 0;
    }

    public boolean wantsmessages() {
        return this.CheckMessages.getState() && Global.Silent <= 0;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        this.Input.requestFocus();
    }

}

class GetReply extends GetParameter {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ConnectionFrame CF;

    public GetReply(ConnectionFrame cf) {
        super(cf, Global.resourceString("Automatic_Reply_"), Global
                .resourceString("Auto_Reply"), cf, true);
        this.CF = cf;
        this.set(this.CF.Reply);
        this.setVisible(true);
    }

    @Override
    public boolean tell(Object o, String s) {
        this.CF.Reply = s;
        rene.gui.Global.setParameter("autoreply", s);
        return true;
    }
}

class GetWaitfor extends GetParameter {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ConnectionFrame CF;

    public GetWaitfor(ConnectionFrame cf) {
        super(cf, Global.resourceString("Wait_for_"), Global
                .resourceString("Wait_for_Player"), cf, true);
        this.CF = cf;
        this.set(this.CF.Waitfor);
        this.setVisible(true);
    }

    @Override
    public boolean tell(Object o, String s) {
        this.CF.Waitfor = s;
        return true;
    }
}

class RefreshWriter extends PrintWriter {
    Thread T;
    boolean NeedsRefresh;
    boolean Stop = false;

    public RefreshWriter(OutputStreamWriter out, boolean flag) {
        super(out, flag);
        if (rene.gui.Global.getParameter("refresh", true)) {
            Dump.println("Refresh Thread started.");
            this.T = new Thread() {
                @Override
                public void run() {
                    RefreshWriter.this.runAYT();
                }
            };
            this.T.start();
        }
    }

    @Override
    public void close() {
        super.close();
        this.NeedsRefresh = false;
        this.Stop = true;
    }

    @Override
    public void print(String s) {
        super.print(s);
        Dump.println("Out ---> " + s);
        this.NeedsRefresh = false;
    }

    public void printLn(String s) {
        super.println(s);
        Dump.println("Out ---> " + s);
        this.NeedsRefresh = false;
    }

    public void runAYT() {
        while (!this.Stop) {
            this.NeedsRefresh = true;
            try {
                Thread.sleep(300000);
            } catch (final Exception e) {
            }
            if (this.Stop) {
                break;
            }
            if (this.NeedsRefresh) {
                this.println("ayt");
                // write(254);
                Dump.println("ayt sent!");
            }
        }
    }
}
