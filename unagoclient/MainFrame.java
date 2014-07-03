package unagoclient;

import unagoclient.board.GoFrame;
import unagoclient.dialogs.*;
import unagoclient.gmp.GMPConnection;
import unagoclient.gui.*;
import unagoclient.partner.Server;
import unagoclient.sound.UnaGoSound;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * Get some advanced options.
 */

class AdvancedOptionsEdit extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Checkbox Pack, SetIcon, UseSystemViewer, UseSystemLister, UseConfirmation,
            WhoWindow, GamesWindow, KoRule;

    /**
     * Initialize all dialog items. The main layout is a Nx1 grid with check
     * boxes.
     */
    public AdvancedOptionsEdit(Frame f) {
        super(f, Global.resourceString("Advanced_Options"), true);
        this.setLayout(new BorderLayout());
        final JPanel p = new MyPanel();
        p.setLayout(new GridLayout(0, 1));
        p.add(this.UseConfirmation = new Checkbox(Global
                .resourceString("Confirmations")));
        this.UseConfirmation.setState(rene.gui.Global.getParameter(
                "confirmations", true));
        this.UseConfirmation.setFont(Global.SansSerif);
        p.add(this.KoRule = new Checkbox(Global.resourceString("Obey_Ko_Rule")));
        this.KoRule.setState(rene.gui.Global.getParameter("korule", true));
        this.KoRule.setFont(Global.SansSerif);
        p.add(this.WhoWindow = new Checkbox(Global
                .resourceString("Show_Who_Window")));
        this.WhoWindow
        .setState(rene.gui.Global.getParameter("whowindow", true));
        this.WhoWindow.setFont(Global.SansSerif);
        p.add(this.GamesWindow = new Checkbox(Global
                .resourceString("Show_Games_Window")));
        this.GamesWindow.setState(rene.gui.Global.getParameter("gameswindow",
                true));
        this.GamesWindow.setFont(Global.SansSerif);
        p.add(this.Pack = new Checkbox(Global
                .resourceString("Pack_some_of_the_dialogs")));
        this.Pack.setState(rene.gui.Global.getParameter("pack", true));
        this.Pack.setFont(Global.SansSerif);
        p.add(this.SetIcon = new Checkbox(Global
                .resourceString("Set_own_icon__buggy_in_windows__")));
        this.SetIcon.setState(rene.gui.Global.getParameter("icons", false));
        this.SetIcon.setFont(Global.SansSerif);
        p.add(this.UseSystemViewer = new Checkbox(Global
                .resourceString("Use_AWT_TextArea")));
        this.UseSystemViewer.setState(rene.gui.Global.getParameter(
                "systemviewer", false));
        this.UseSystemViewer.setFont(Global.SansSerif);
        p.add(this.UseSystemLister = new Checkbox(Global
                .resourceString("Use_AWT_List")));
        this.UseSystemLister.setState(rene.gui.Global.getParameter(
                "systemlister", false));
        this.UseSystemLister.setFont(Global.SansSerif);
        this.add("Center", new Panel3D(p));
        final JPanel ps = new MyPanel();
        ps.add(new ButtonAction(this, Global.resourceString("OK")));
        ps.add(new ButtonAction(this, Global.resourceString("Cancel")));
        ps.add(new MyLabel(" "));
        ps.add(new ButtonAction(this, Global.resourceString("Help")));
        this.add("South", new Panel3D(ps));
        Global.setpacked(this, "advancedoptionsedit", 300, 150, f);
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        if (o.equals(Global.resourceString("OK"))) {
            this.setVisible(false);
            this.dispose();
            rene.gui.Global.setParameter("pack", this.Pack.getState());
            rene.gui.Global.setParameter("icons", this.SetIcon.getState());
            rene.gui.Global.setParameter("systemviewer",
                    this.UseSystemViewer.getState());
            rene.gui.Global.setParameter("systemlister",
                    this.UseSystemLister.getState());
            rene.gui.Global.setParameter("confirmations",
                    this.UseConfirmation.getState());
            rene.gui.Global
            .setParameter("whowindow", this.WhoWindow.getState());
            rene.gui.Global.setParameter("gameswindow",
                    this.GamesWindow.getState());
            rene.gui.Global.setParameter("korule", this.KoRule.getState());
        } else if (o.equals(Global.resourceString("Cancel"))) {
            this.setVisible(false);
            this.dispose();
        } else if (o.equals(Global.resourceString("Help"))) {
            new Help("advanced");
        }
    }
}

/**
 * Get the background color.
 */

class BackgroundColorEdit extends ColorEdit {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public BackgroundColorEdit(Frame f, String s, Color c) {
        super(f, s, c, false);
        this.setVisible(true);
    }

    @Override
    public void addbutton(MyPanel p) {
        p.add(new ButtonAction(this, Global.resourceString("System")));
    }

    @Override
    public void doAction(String o) {
        if (o.equals(Global.resourceString("System"))) {
            rene.gui.Global.removeParameter("color.control");
            rene.gui.Global.removeParameter("color.background");
            rene.gui.Global.makeColors();
            super.doAction(Global.resourceString("OK"));
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void tell(Color C) {
        rene.gui.Global.setParameter("color.background", C);
        rene.gui.Global.setParameter("color.control", C);
        rene.gui.Global.makeColors();
    }
}

/**
 * Get the languate locale
 */

class GetLanguage extends GetParameter {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public boolean done = false;

    public GetLanguage(MainFrame gcf) {
        super(gcf, "Your Locale (leave empty for default)", "Language", gcf,
                true, "language");
        final String S = "Your Locale";
        final String T = Global.resourceString(S);
        if (!S.equals(T)) {
            this.Prompt.setText(T + " (" + S + ")");
        }
        this.set(Locale.getDefault().toString());
        this.setVisible(true);
    }

    @Override
    public boolean tell(Object o, String S) {
        rene.gui.Global.setParameter("language", S);
        this.done = true;
        return true;
    }
}

/**
 * Get the port for the partner server.
 */

class GetPort extends GetParameter {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    MainFrame GCF;

    public GetPort(MainFrame gcf, int port) {
        super(gcf, Global.resourceString("Server_Port"), Global
                .resourceString("Port"), gcf, true, "port");
        this.set("" + port);
        this.GCF = gcf;
        this.setVisible(true);
    }

    @Override
    public boolean tell(Object o, String S) {
        int port = 6970;
        try {
            port = Integer.parseInt(S);
            rene.gui.Global.setParameter("serverport", port);
        } catch (final NumberFormatException ex) {
        }
        return true;
    }
}

/**
 * Get IP name of the relay server, if used.
 */

class GetRelayServer extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    JTextField Server;
    IntField Port;

    public GetRelayServer(Frame F) {
        super(F, Global.resourceString("Relay_Server"), true);
        final JPanel p = new MyPanel();
        p.setLayout(new GridLayout(0, 2));
        p.add(new MyLabel(Global.resourceString("Server")));
        p.add(this.Server = new GrayTextField());
        this.Server.setText(rene.gui.Global.getParameter("relayserver",
                "localhost"));
        p.add(new MyLabel(Global.resourceString("Port")));
        p.add(this.Port = new IntField(this, Global.resourceString("Port"),
                rene.gui.Global.getParameter("relayport", 6971)));
        this.add("Center", new Panel3D(p));
        final JPanel bp = new MyPanel();
        bp.add(new ButtonAction(this, Global.resourceString("OK")));
        bp.add(new ButtonAction(this, Global.resourceString("Cancel")));
        bp.add(new MyLabel(" "));
        bp.add(new ButtonAction(this, Global.resourceString("Help")));
        this.add("South", new Panel3D(bp));
        Global.setpacked(this, "getrelay", 300, 200, F);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "getrelay");
        if (Global.resourceString("OK").equals(o)) {
            rene.gui.Global.setParameter("relayserver", this.Server.getText());
            rene.gui.Global.setParameter("relayport", this.Port.value());
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Cancel").equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else if (o.equals(Global.resourceString("Help"))) {
            new Help("relayserver");
        } else {
            super.doAction(o);
        }
    }
}

/**
 * The MainFrame contains menus to edit some options, get help and set some
 * things. A card layout with the server and partner connections is added to
 * this frame later.
 */

public class MainFrame extends CloseFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    CheckboxMenuItem StartPublicServer, TimerInTitle, BigTimer,
    ExtraInformation, ExtraSendField, DoSound, SimpleSound, BeepOnly,
    Warning, RelayCheck, Automatic, EveryMove, FineBoard, Navigation;
    MenuItem StartServer;
    public Server S = null;

    public MainFrame(String c) {
        super(c + " " + Global.resourceString("Version"));
        this.seticon("iunago.gif");
        final boolean constrainedapplet = c.equals(Global
                .resourceString("UnaGo_Applet"));
        // Menu :
        final MenuBar menu = new MenuBar();
        this.setMenuBar(menu);
        // Actions
        final Menu local = new MyMenu(Global.resourceString("Actions"));
        local.add(new MenuItemAction(this, Global.resourceString("Local_Board")));
        local.addSeparator();
        local.add(new MenuItemAction(this, Global.resourceString("Play_Go")));
        local.addSeparator();
        local.add(new MenuItemAction(this, Global.resourceString("Close")));
        menu.add(local);
        // Server Options
        final Menu soptions = new MyMenu(Global.resourceString("Go_Server"));
        if (!Global.isApplet()) {
            soptions.add(this.Automatic = new CheckboxMenuItemAction(this,
                    Global.resourceString("Automatic_Login")));
            this.Automatic.setState(rene.gui.Global.getParameter("automatic",
                    true));
            soptions.addSeparator();
        }
        soptions.add(new MenuItemAction(this, Global.resourceString("Filter")));
        soptions.add(new MenuItemAction(this, Global
                .resourceString("Function_Keys")));
        menu.add(soptions);
        // Partner Options
        if (!constrainedapplet) {
            final Menu poptions = new MyMenu(Global.resourceString("Partner"));
            poptions.add(this.StartServer = new MenuItemAction(this, Global
                    .resourceString("Start_Server")));
            poptions.add(new MenuItemAction(this, Global
                    .resourceString("Server_Port")));
            poptions.add(new MenuItemAction(this, Global
                    .resourceString("Your_Name")));
            poptions.add(this.StartPublicServer = new CheckboxMenuItemAction(
                    this, Global.resourceString("Public")));
            this.StartPublicServer.setState(rene.gui.Global.getParameter(
                    "publicserver", true));
            menu.add(poptions);
        }
        // Options
        final Menu options = new MyMenu(Global.resourceString("Options"));
        final Menu bo = new MyMenu(Global.resourceString("Board_Options"));
        bo.add(this.Navigation = new CheckboxMenuItemAction(this, Global
                .resourceString("Navigation_Tree")));
        this.Navigation.setState(rene.gui.Global.getParameter(
                "shownavigationtree", true));
        bo.add(this.TimerInTitle = new CheckboxMenuItemAction(this, Global
                .resourceString("Timer_in_Title")));
        this.TimerInTitle.setState(rene.gui.Global.getParameter("timerintitle",
                true));
        bo.add(this.BigTimer = new CheckboxMenuItemAction(this, Global
                .resourceString("Big_Timer")));
        this.BigTimer.setState(rene.gui.Global.getParameter("bigtimer", true));
        bo.add(this.ExtraInformation = new CheckboxMenuItemAction(this, Global
                .resourceString("Extra_Information")));
        this.ExtraInformation.setState(rene.gui.Global.getParameter(
                "extrainformation", true));
        bo.add(this.ExtraSendField = new CheckboxMenuItemAction(this, Global
                .resourceString("Extra_Send_Field")));
        this.ExtraSendField.setState(rene.gui.Global.getParameter(
                "extrasendfield", true));
        bo.add(this.FineBoard = new CheckboxMenuItemAction(this, Global
                .resourceString("Fine_Board")));
        this.FineBoard
        .setState(rene.gui.Global.getParameter("fineboard", true));
        options.add(bo);
        options.add(new MenuItemAction(this, Global
                .resourceString("Advanced_Options")));
        options.addSeparator();
        final Menu fonts = new MyMenu(Global.resourceString("Fonts"));
        fonts.add(new MenuItemAction(this, Global.resourceString("Board_Font")));
        fonts.add(new MenuItemAction(this, Global.resourceString("Fixed_Font")));
        fonts.add(new MenuItemAction(this, Global.resourceString("Big_Font")));
        fonts.add(new MenuItemAction(this, Global.resourceString("Normal_Font")));
        options.add(fonts);
        options.add(new MenuItemAction(this, Global
                .resourceString("Background_Color")));
        options.addSeparator();
        options.add(this.DoSound = new CheckboxMenuItemAction(this, Global
                .resourceString("Sound_on")));
        this.DoSound.setState(!rene.gui.Global.getParameter("nosound", true));
        options.add(this.BeepOnly = new CheckboxMenuItemAction(this, Global
                .resourceString("Beep_only")));
        this.BeepOnly.setState(rene.gui.Global.getParameter("beep", true));
        final Menu sound = new MyMenu(Global.resourceString("Sound_Options"));
        sound.add(this.SimpleSound = new CheckboxMenuItemAction(this, Global
                .resourceString("Simple_sound")));
        this.SimpleSound.setState(rene.gui.Global.getParameter("simplesound",
                true));
        sound.add(this.EveryMove = new CheckboxMenuItemAction(this, Global
                .resourceString("Every_move")));
        this.EveryMove.setState(rene.gui.Global.getParameter("sound.everymove",
                true));
        sound.add(this.Warning = new CheckboxMenuItemAction(this, Global
                .resourceString("Timeout_warning")));
        this.Warning.setState(rene.gui.Global.getParameter("warning", true));
        sound.add(new MenuItemAction(this, Global.resourceString("Test_Sound")));
        options.add(sound);
        options.addSeparator();
        options.add(this.RelayCheck = new CheckboxMenuItemAction(this, Global
                .resourceString("Use_Relay")));
        this.RelayCheck.setState(rene.gui.Global
                .getParameter("userelay", false));
        options.add(new MenuItemAction(this, Global
                .resourceString("Relay_Server")));
        options.addSeparator();
        options.add(new MenuItemAction(this, Global
                .resourceString("Set_Language")));
        options.add(new MenuItemAction(this, "Close and Use English",
                "CloseEnglish"));
        menu.add(options);
        // Help
        final Menu help = new MyMenu(Global.resourceString("Help"));
        help.add(new MenuItemAction(this, Global.resourceString
                ("About_UnaGo")));
        help.add(new MenuItemAction(this, Global.resourceString("Overview")));
        help.addSeparator();
        help.add(new MenuItemAction(this, Global
                .resourceString("Using_Windows")));
        help.add(new MenuItemAction(this, Global
                .resourceString("Configuring_Connections")));
        help.add(new MenuItemAction(this, Global
                .resourceString("Partner_Connections")));
        help.add(new MenuItemAction(this, Global.resourceString("About_Sounds")));
        help.add(new MenuItemAction(this, Global
                .resourceString("About_Smart_Go_Format_SGF")));
        help.add(new MenuItemAction(this, Global.resourceString("About_Filter")));
        help.add(new MenuItemAction(this, Global
                .resourceString("About_Function_Keys")));
        help.add(new MenuItemAction(this, Global
                .resourceString("Overcoming_Firewalls")));
        help.add(new MenuItemAction(this, Global.resourceString("Play_Go_Help")));
        help.addSeparator();
        help.add(new MenuItemAction(this, Global.resourceString("About_Help")));
        help.addSeparator();
        help.add(new MenuItemAction(this, Global
                .resourceString("On_line_Version_Information")));
        menu.setHelpMenu(help);
        this.pack();
        Global.setwindow(this, "mainframe", 300, 300);
    }

    @Override
    public boolean close() {
        if (rene.gui.Global.getParameter("confirmations", true)) {
            final CloseMainQuestion CMQ = new CloseMainQuestion(this);
            if (CMQ.Result) {
                this.doclose();
            }
            return false;
        } else {
            this.doclose();
            return false;
        }
    }

    @Override
    public void doAction(String o) {
        if ("CloseEnglish".equals(o)) {
            rene.gui.Global.setParameter("language", "en");
            if (this.close()) {
                this.doclose();
            }
        } else if (Global.resourceString("Overview").equals(o)) {
            new Help("overview");
        } else if (Global.resourceString("Using_Windows").equals(o)) {
            new Help("windows");
        } else if (Global.resourceString("Configuring_Connections").equals(o)) {
            new Help("configure");
        } else if (Global.resourceString("Partner_Connections").equals(o)) {
            new Help("confpartner");
        } else if (Global.resourceString("About_UnaGo").equals(o)) {
            new Help("about");
        } else if (Global.resourceString("About_Help").equals(o)) {
            new Help("help");
        } else if (Global.resourceString("About_Sounds").equals(o)) {
            new Help("sound");
        } else if (Global.resourceString("About_Smart_Go_Format_SGF").equals(o)) {
            new Help("sgf");
        } else if (Global.resourceString("About_Filter").equals(o)) {
            new Help("filter");
        } else if (Global.resourceString("About_Function_Keys").equals(o)) {
            new Help("fkeys");
        } else if (Global.resourceString("Overcoming_Firewalls").equals(o)) {
            new Help("firewall");
        } else if (Global.resourceString("Play_Go_Help").equals(o)) {
            new Help("gmp");
        } else if (Global.resourceString("On_line_Version_Information").equals(
                o)) {
            new Help("version");
        } else if (Global.resourceString("Local_Board").equals(o)) {
            new GoFrame(new Frame(), Global.resourceString("Local_Viewer"));
        } else if (Global.resourceString("Play_Go").equals(o)) {
            new GMPConnection(this);
        } else if (Global.resourceString("Server_Port").equals(o)) {
            new GetPort(this, rene.gui.Global.getParameter("serverport", 6970));
        } else if (Global.resourceString("Set_Language").equals(o)) {
            final GetLanguage d = new GetLanguage(this);
            if (d.done && this.close()) {
                this.doclose();
            }
        } else if (Global.resourceString("Board_Font").equals(o)) {
            new GetFontSize("boardfontname", rene.gui.Global.getParameter(
                    "boardfontname", "SansSerif"), "boardfontsize",
                    rene.gui.Global.getParameter("boardfontsize", 10), false)
            .setVisible(true);
        } else if (Global.resourceString("Normal_Font").equals(o)) {
            new GetFontSize("sansserif", rene.gui.Global.getParameter(
                    "sansserif", "SansSerif"), "ssfontsize",
                    rene.gui.Global.getParameter("ssfontsize", 11), false)
            .setVisible(true);
        } else if (Global.resourceString("Fixed_Font").equals(o)) {
            new GetFontSize("monospaced", rene.gui.Global.getParameter(
                    "monospaced", "Monospaced"), "msfontsize",
                    rene.gui.Global.getParameter("msfontsize", 11), false)
            .setVisible(true);
        } else if (Global.resourceString("Big_Font").equals(o)) {
            new GetFontSize("bigmonospaced", rene.gui.Global.getParameter(
                    "bigmonospaced", "BoldMonospaced"), "bigmsfontsize",
                    rene.gui.Global.getParameter("bigmsfontsize", 22), false)
            .setVisible(true);
        } else if (Global.resourceString("Your_Name").equals(o)) {
            new YourNameQuestion(this);
        } else if (Global.resourceString("Filter").equals(o)) {
            Global.MF.edit();
        } else if (Global.resourceString("Function_Keys").equals(o)) {
            new FunctionKeyEdit();
        } else if (Global.resourceString("Relay_Server").equals(o)) {
            new GetRelayServer(this);
        } else if (Global.resourceString("Test_Sound").equals(o)) {
            UnaGoSound.play("high", "wip", true);
        } else if (Global.resourceString("Start_Server").equals(o)) {
            if (Global.Busy) {
                Dump.println("Server started on "
                        + rene.gui.Global.getParameter("serverport", 6970));
                if (this.S == null) {
                    this.S = new Server(rene.gui.Global.getParameter(
                            "serverport", 6970), rene.gui.Global.getParameter(
                                    "publicserver", true));
                }
                this.S.open();
                try {
                    this.StartServer.setLabel(Global
                            .resourceString("Stop_Server"));
                } catch (final Exception e) {
                    System.err.println("Motif error with setLabel");
                }
            } else {
                this.S.close();
                this.StartServer
                .setLabel(Global.resourceString("Start_Server"));
            }
        } else if (Global.resourceString("Stop_Server").equals(o)) {
            this.S.close();
            this.StartServer.setLabel(Global.resourceString("Start_Server"));
        } else if (Global.resourceString("Advanced_Options").equals(o)) {
            new AdvancedOptionsEdit(this);
        } else if (Global.resourceString("Background_Color").equals(o)) {
            new BackgroundColorEdit(this, "globalgray", Color.gray);
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        Global.notewindow(this, "mainframe");
        super.doclose();
        Global.writeparameter(".go.cfg");
        if (this.S != null) {
            this.S.close();
        }
        if (!Global.isApplet()) {
            System.exit(0);
        }
    }

    @Override
    public void itemAction(String o, boolean flag) {
        if (Global.resourceString("Public").equals(o)) {
            rene.gui.Global.setParameter("publicserver", flag);
        } else if (Global.resourceString("Timer_in_Title").equals(o)) {
            rene.gui.Global.setParameter("timerintitle", flag);
        } else if (Global.resourceString("Navigation_Tree").equals(o)) {
            rene.gui.Global.setParameter("shownavigationtree", flag);
        } else if (Global.resourceString("Fine_Board").equals(o)) {
            rene.gui.Global.setParameter("fineboard", flag);
        } else if (Global.resourceString("Big_Timer").equals(o)) {
            rene.gui.Global.setParameter("bigtimer", flag);
        } else if (Global.resourceString("Extra_Information").equals(o)) {
            rene.gui.Global.setParameter("extrainformation", flag);
        } else if (Global.resourceString("Extra_Send_Field").equals(o)) {
            rene.gui.Global.setParameter("extrasendfield", flag);
        } else if (Global.resourceString("Sound_on").equals(o)) {
            rene.gui.Global.setParameter("nosound", !flag);
        } else if (Global.resourceString("Simple_sound").equals(o)) {
            rene.gui.Global.setParameter("simplesound", flag);
        } else if (Global.resourceString("Every_move").equals(o)) {
            rene.gui.Global.setParameter("sound.everymove", flag);
        } else if (Global.resourceString("Beep_only").equals(o)) {
            rene.gui.Global.setParameter("beep", flag);
        } else if (Global.resourceString("Timeout_warning").equals(o)) {
            rene.gui.Global.setParameter("warning", flag);
        } else if (Global.resourceString("Automatic_Login").equals(o)) {
            rene.gui.Global.setParameter("automatic", flag);
        } else if (Global.resourceString("Use_Relay").equals(o)) {
            rene.gui.Global.setParameter("userelay", flag);
        }
    }

}

/**
 * Get the name for the partner client.
 */

class YourNameQuestion extends GetParameter {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public YourNameQuestion(MainFrame gcf) {
        super(gcf, Global.resourceString("Name"), Global
                .resourceString("Your_Name"), gcf, true, "yourname");
        this.set(rene.gui.Global.getParameter("yourname", "Your Name"));
        this.setVisible(true);
    }

    @Override
    public boolean tell(Object o, String S) {
        if (!S.equals("")) {
            rene.gui.Global.setParameter("yourname", S);
        }
        return true;
    }
}
