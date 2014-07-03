package unagoclient.board;

import unagoclient.BMPFile;
import unagoclient.Global;
import unagoclient.dialogs.*;
import unagoclient.gui.*;
import unagoclient.mail.MailDialog;
import rene.gui.IconBar;
import rene.gui.IconBarListener;
import rene.util.FileName;
import rene.util.xml.XmlReader;
import rene.util.xml.XmlReaderException;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URL;

/**
 * Ask, if a node is to be inserted and the tree thus changed.
 */

class AskInsertQuestion extends Question {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public boolean Result = false;

    public AskInsertQuestion(Frame f) {
        super(f, Global.resourceString("Change_Game_Tree_"), Global
                .resourceString("Change_Game_Tree"), f, true);
        this.setVisible(true);
    }

    @Override
    public void tell(Question q, Object o, boolean f) {
        q.setVisible(false);
        q.dispose();
        this.Result = f;
    }
}

/**
 * Ask, if a complete subtree is to be deleted.
 */

class AskUndoQuestion extends Question {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public boolean Result = false;

    public AskUndoQuestion(Frame f) {
        super(f, Global.resourceString("Delete_all_subsequent_moves_"), Global
                .resourceString("Delete_Tree"), f, true);
        this.setVisible(true);
    }

    @Override
    public void tell(Question q, Object o, boolean f) {
        q.setVisible(false);
        q.dispose();
        this.Result = f;
    }
}

/**
 * Let the user edit the board colors (stones, shines and board). Redraw the
 * board, when done with OK.
 */

class BoardColorEdit extends ColorEdit {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GoFrame GF;

    public BoardColorEdit(GoFrame F, String s, Color c) {
        super(F, s, c.getRed(), c.getGreen(), c.getBlue(), true);
        this.GF = F;
        this.setVisible(true);
    }

    public BoardColorEdit(GoFrame F, String s, int red, int green, int blue) {
        super(F, s, red, green, blue, true);
        this.GF = F;
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        super.doAction(o);
        if (Global.resourceString("OK").equals(o)) {
            this.GF.updateall();
        }
    }
}

/**
 * Let the user edit the board fong Redraw the board, when done with OK.
 */

class BoardGetFontSize extends GetFontSize {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GoFrame GF;

    public BoardGetFontSize(GoFrame F, String fontname, String deffontname,
            String fontsize, int deffontsize, boolean flag) {
        super(fontname, deffontname, fontsize, deffontsize, flag);
        this.GF = F;
    }

    @Override
    public void doAction(String o) {
        super.doAction(o);
        if (Global.resourceString("OK").equals(o)) {
            Global.createfonts();
            this.GF.updateall();
        }
    }
}

/**
 * Ask the user for permission to close the board frame.
 */

class CloseQuestion extends Question {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GoFrame GF;
    boolean Result = false;

    public CloseQuestion(GoFrame g) {
        super(g, Global.resourceString("Really_trash_this_board_"), Global
                .resourceString("Close_Board"), g, true);
        this.GF = g;
        this.setVisible(true);
    }

    @Override
    public void tell(Question q, Object o, boolean f) {
        q.setVisible(false);
        q.dispose();
        this.Result = f;
    }
}

/**
 * Display a dialog to edit game copyright and user.
 */

class EditCopyright extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    TextArea Copyright;
    JTextField User;
    Node N;

    public EditCopyright(GoFrame f, Node n) {
        super(f, Global.resourceString("Copyright_of_Game"), false);
        final JPanel p1 = new MyPanel();
        this.N = n;
        p1.setLayout(new GridLayout(0, 2));
        p1.add(new MyLabel(Global.resourceString("User")));
        p1.add(this.User = new GrayTextField(n.getaction("US")));
        this.add("North", p1);
        final JPanel p2 = new MyPanel();
        p2.setLayout(new BorderLayout());
        p2.add("North", new MyLabel(Global.resourceString("Copyright")));
        p2.add("Center", this.Copyright = new TextArea("", 0, 0,
                TextArea.SCROLLBARS_VERTICAL_ONLY));
        this.add("Center", p2);
        final JPanel pb = new MyPanel();
        pb.add(new ButtonAction(this, Global.resourceString("OK")));
        pb.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.add("South", pb);
        Global.setwindow(this, "editcopyright", 350, 400);
        this.setVisible(true);
        this.Copyright.setText(n.getaction("CP"));
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "editcopyright");
        if (Global.resourceString("OK").equals(o)) {
            this.N.setaction("US", this.User.getText());
            this.N.setaction("CP", this.Copyright.getText());
        }
        this.setVisible(false);
        this.dispose();
    }
}

/**
 * Display a dialog to edit game information.
 */

class EditInformation extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Node N;
    JTextField Black, White, BlackRank, WhiteRank, Date, Time, Komi, Result,
            Handicap, GameName;
    GoFrame F;

    public EditInformation(GoFrame f, Node n) {
        super(f, Global.resourceString("Game_Information"), false);
        this.N = n;
        this.F = f;
        final JPanel p = new MyPanel();
        p.setLayout(new GridLayout(0, 2));
        p.add(new MyLabel(Global.resourceString("Game_Name")));
        p.add(this.GameName = new FormTextField(n.getaction("GN")));
        p.add(new MyLabel(Global.resourceString("Date")));
        p.add(this.Date = new FormTextField(n.getaction("DT")));
        p.add(new MyLabel(Global.resourceString("Black")));
        p.add(this.Black = new FormTextField(n.getaction("PB")));
        p.add(new MyLabel(Global.resourceString("Black_Rank")));
        p.add(this.BlackRank = new FormTextField(n.getaction("BR")));
        p.add(new MyLabel(Global.resourceString("White")));
        p.add(this.White = new FormTextField(n.getaction("PW")));
        p.add(new MyLabel(Global.resourceString("White_Rank")));
        p.add(this.WhiteRank = new FormTextField(n.getaction("WR")));
        p.add(new MyLabel(Global.resourceString("Result")));
        p.add(this.Result = new FormTextField(n.getaction("RE")));
        p.add(new MyLabel(Global.resourceString("Time")));
        p.add(this.Time = new FormTextField(n.getaction("TM")));
        p.add(new MyLabel(Global.resourceString("Komi")));
        p.add(this.Komi = new FormTextField(n.getaction("KM")));
        p.add(new MyLabel(Global.resourceString("Handicap")));
        p.add(this.Handicap = new FormTextField(n.getaction("HA")));
        this.add("Center", p);
        final JPanel pb = new MyPanel();
        pb.add(new ButtonAction(this, Global.resourceString("OK")));
        pb.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.add("South", pb);
        Global.setpacked(this, "editinformation", 350, 450);
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "editinformation");
        if (Global.resourceString("OK").equals(o)) {
            this.N.setaction("GN", this.GameName.getText());
            this.N.setaction("PB", this.Black.getText());
            this.N.setaction("PW", this.White.getText());
            this.N.setaction("BR", this.BlackRank.getText());
            this.N.setaction("WR", this.WhiteRank.getText());
            this.N.setaction("DT", this.Date.getText());
            this.N.setaction("TM", this.Time.getText());
            this.N.setaction("KM", this.Komi.getText());
            this.N.setaction("RE", this.Result.getText());
            this.N.setaction("HA", this.Handicap.getText());
            if (!this.GameName.getText().equals("")) {
                this.F.setTitle(this.GameName.getText());
            }
        }
        this.setVisible(false);
        this.dispose();
    }
}

/**
 * A dialog to get the present encoding.
 */

class GetEncoding extends GetParameter {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GoFrame GCF;

    public GetEncoding(GoFrame gcf) {
        super(gcf, Global.resourceString("Encoding__empty__default_"), Global
                .resourceString("Encoding"), gcf, true, "encoding");
        if (!Global.isApplet()) {
            this.set(rene.gui.Global.getParameter("encoding",
                    System.getProperty("file.encoding")));
        }
        this.GCF = gcf;
        this.setVisible(true);
    }

    @Override
    public boolean tell(Object o, String S) {
        if (S.equals("")) {
            rene.gui.Global.removeParameter("encoding");
        } else {
            rene.gui.Global.setParameter("encoding", S);
        }
        return true;
    }
}

class GetSearchString extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GoFrame GF;
    TextFieldAction T;
    static boolean Active = false;

    public GetSearchString(GoFrame gf) {
        super(gf, Global.resourceString("Search"), false);
        if (GetSearchString.Active) {
            return;
        }
        this.add("North", new MyLabel(Global.resourceString("Search_String")));
        this.add("Center", this.T = new TextFieldAction(this, "Input", 25));
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Search")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        this.add("South", p);
        Global.setpacked(this, "getparameter", 300, 150);
        this.validate();
        this.T.addKeyListener(this);
        this.T.setText(rene.gui.Global.getParameter("searchstring", "++"));
        this.GF = gf;
        this.setVisible(true);
        GetSearchString.Active = true;
    }

    @Override
    public void dispose() {
        GetSearchString.Active = false;
        super.dispose();
    }

    @Override
    public void doAction(String s) {
        if (s.equals(Global.resourceString("Search")) || s.equals("Input")) {
            rene.gui.Global.setParameter("searchstring", this.T.getText());
            this.GF.search();
        } else if (s.equals(Global.resourceString("Cancel"))) {
            this.setVisible(false);
            this.dispose();
            GetSearchString.Active = false;
        }
    }
}

/**
 * The GoFrame class is a frame, which contains the board, the comment window
 * and the navigation buttons (at least).
 * <p>
 * This class implements BoardInterface. This is done to make clear what
 * routines are called from the board and to give the board a beans appearance.
 * <p>
 * The layout is a panel of class BoardCommentPanel, containing two panels for
 * the board (BoardPanel) and for the comments (plus the ExtraSendField in
 * ConnectedGoFrame). Below is a 3D panel for the buttons. The BoardCommentPanel
 * takes care of the layout for its components.
 * <p>
 * This class handles all actions in it, besides the mouse actions on the board,
 * which are handled by Board.
 * <p>
 * Note that the Board class modifies the appearance of buttons and takes care
 * of the comment window, the next move label and the board position label.
 * <p>
 * Several private classes in GoFrame.java contain dialogs to enter game
 * information, copyright, text marks, etc.
 *
 * @see unagoclient.board.Board
 */

// The parent class for a frame containing the board, navigation buttons
// and menus.
// This board has a constructor, which initiates menus to be used as a local
// board. For Partner of IGS games there is the ConnectedGoFrame child, which
// uses another menu structure.
// Furthermore, it has methods to handle lots of user actions.
public class GoFrame extends CloseFrame implements FilenameFilter, KeyListener,
BoardInterface, ClipboardOwner, IconBarListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public OutputLabel L, Lm; // For
                              // board
                              // informations
    TextArea Comment; // For
                      // comments
    String Dir; // FileDialog
                // directory
    public Board B; // The
                    // board
                    // itself
    // menu check items:
    CheckboxMenuItem SetBlack, SetWhite, Black, White, Mark, Letter, Hide,
            Square, Cross, Circle, Triangle, TextMark;
    public Color BoardColor, BlackColor, BlackSparkleColor, WhiteColor,
            WhiteSparkleColor, MarkerColor, LabelColor;
    CheckboxMenuItem Coordinates, UpperLeftCoordinates, LowerRightCoordinates;
    CheckboxMenuItem PureSGF, CommentSGF, DoSound, BeepOnly, TrueColor, Alias,
            TrueColorStones, SmallerStones, MenuLastNumber, MenuTarget,
            Shadows, BlackOnly, UseXML, UseSGF;
    public boolean BWColor = false, LastNumber = false, ShowTarget = false;
    CheckboxMenuItem MenuBWColor, ShowButtons;
    CheckboxMenuItem VHide, VCurrent, VChild, VNumbers;
    String Text = Global.getParameter("textmark", "A");
    boolean Show;
    TextMarkQuestion TMQ;
    IconBar IB;
    JPanel ButtonP;
    String DefaultTitle = "";
    NavigationPanel Navigation;

    InputStreamReader LaterLoad = null;

    boolean LaterLoadXml;

    int LaterMove = 0;

    String LaterFilename = "";

    boolean Activated = false;

    public GoFrame(Frame f, String s)
    // Constructur for local board menus.
    {
        super(s);
        this.DefaultTitle = s;
        // Colors
        this.setcolors();
        this.seticon("iboard.gif");
        this.setLayout(new BorderLayout());
        // Menu
        final MenuBar M = new MenuBar();
        this.setMenuBar(M);
        final Menu file = new MyMenu(Global.resourceString("File"));
        M.add(file);
        file.add(new MenuItemAction(this, Global.resourceString("New")));
        file.add(new MenuItemAction(this, Global.resourceString("Load")));
        file.add(new MenuItemAction(this, Global.resourceString("Save")));
        file.add(new MenuItemAction(this, Global
                .resourceString("Save_Position")));
        file.addSeparator();
        file.add(this.UseXML = new CheckboxMenuItemAction(this, Global
                .resourceString("Use_XML")));
        this.UseXML.setState(rene.gui.Global.getParameter("xml", false));
        file.add(this.UseSGF = new CheckboxMenuItemAction(this, Global
                .resourceString("Use_SGF")));
        this.UseSGF.setState(!rene.gui.Global.getParameter("xml", false));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global
                .resourceString("Load_from_Clipboard")));
        file.add(new MenuItemAction(this, Global
                .resourceString("Copy_to_Clipboard")));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global.resourceString("Mail")));
        file.add(new MenuItemAction(this, Global.resourceString("Ascii_Mail")));
        file.add(new MenuItemAction(this, Global.resourceString("Print")));
        file.add(new MenuItemAction(this, Global.resourceString("Save_Bitmap")));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global.resourceString("Board_size")));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global.resourceString("Add_Game")));
        file.add(new MenuItemAction(this, Global.resourceString("Remove_Game")));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global.resourceString("Close")));
        final Menu set = new MyMenu(Global.resourceString("Set"));
        M.add(set);
        set.add(this.Mark = new CheckboxMenuItemAction(this, Global
                .resourceString("Mark")));
        set.add(this.Letter = new CheckboxMenuItemAction(this, Global
                .resourceString("Letter")));
        set.add(this.Hide = new CheckboxMenuItemAction(this, Global
                .resourceString("Delete")));
        final Menu mark = new MyMenu(Global.resourceString("Special_Mark"));
        mark.add(this.Square = new CheckboxMenuItemAction(this, Global
                .resourceString("Square")));
        mark.add(this.Circle = new CheckboxMenuItemAction(this, Global
                .resourceString("Circle")));
        mark.add(this.Triangle = new CheckboxMenuItemAction(this, Global
                .resourceString("Triangle")));
        mark.add(this.Cross = new CheckboxMenuItemAction(this, Global
                .resourceString("Cross")));
        mark.addSeparator();
        mark.add(this.TextMark = new CheckboxMenuItemAction(this, Global
                .resourceString("Text")));
        set.add(mark);
        set.addSeparator();
        set.add(new MenuItemAction(this, Global
                .resourceString("Resume_playing")));
        set.addSeparator();
        set.add(new MenuItemAction(this, Global.resourceString("Pass")));
        set.addSeparator();
        set.add(this.SetBlack = new CheckboxMenuItemAction(this, Global
                .resourceString("Set_Black")));
        set.add(this.SetWhite = new CheckboxMenuItemAction(this, Global
                .resourceString("Set_White")));
        set.addSeparator();
        set.add(this.Black = new CheckboxMenuItemAction(this, Global
                .resourceString("Black_to_play")));
        set.add(this.White = new CheckboxMenuItemAction(this, Global
                .resourceString("White_to_play")));
        set.addSeparator();
        set.add(new MenuItemAction(this, Global
                .resourceString("Undo_Adding_Removing")));
        set.add(new MenuItemAction(this, Global
                .resourceString("Clear_all_marks")));
        final Menu var = new MyMenu(Global.resourceString("Nodes"));
        var.add(new MenuItemAction(this, Global.resourceString("Insert_Node")));
        var.add(new MenuItemAction(this, Global
                .resourceString("Insert_Variation")));
        var.addSeparator();
        var.add(new MenuItemAction(this, Global.resourceString("Next_Game")));
        var.add(new MenuItemAction(this, Global.resourceString("Previous_Game")));
        var.addSeparator();
        var.add(new MenuItemAction(this, Global.resourceString("Search")));
        var.add(new MenuItemAction(this, Global.resourceString("Search_Again")));
        var.addSeparator();
        var.add(new MenuItemAction(this, Global.resourceString("Node_Name")));
        var.add(new MenuItemAction(this, Global
                .resourceString("Goto_Next_Name")));
        var.add(new MenuItemAction(this, Global
                .resourceString("Goto_Previous_Name")));
        M.add(var);
        final Menu score = new MyMenu(Global.resourceString("Finish_Game"));
        M.add(score);
        score.add(new MenuItemAction(this, Global
                .resourceString("Remove_groups")));
        score.add(new MenuItemAction(this, Global.resourceString("Score")));
        score.addSeparator();
        score.add(new MenuItemAction(this, Global
                .resourceString("Game_Information")));
        score.add(new MenuItemAction(this, Global
                .resourceString("Game_Copyright")));
        score.addSeparator();
        score.add(new MenuItemAction(this, Global
                .resourceString("Prisoner_Count")));
        final Menu options = new MyMenu(Global.resourceString("Options"));
        final Menu mc = new MyMenu(Global.resourceString("Coordinates"));
        mc.add(this.Coordinates = new CheckboxMenuItemAction(this, Global
                .resourceString("On")));
        this.Coordinates.setState(rene.gui.Global.getParameter("coordinates",
                true));
        mc.add(this.UpperLeftCoordinates = new CheckboxMenuItemAction(this,
                Global.resourceString("Upper_Left")));
        this.UpperLeftCoordinates.setState(rene.gui.Global.getParameter(
                "upperleftcoordinates", true));
        mc.add(this.LowerRightCoordinates = new CheckboxMenuItemAction(this,
                Global.resourceString("Lower_Right")));
        this.LowerRightCoordinates.setState(rene.gui.Global.getParameter(
                "lowerrightcoordinates", true));
        options.add(mc);
        options.addSeparator();
        final Menu colors = new MyMenu(Global.resourceString("Colors"));
        colors.add(new MenuItemAction(this, Global
                .resourceString("Board_Color")));
        colors.add(new MenuItemAction(this, Global
                .resourceString("Black_Color")));
        colors.add(new MenuItemAction(this, Global
                .resourceString("Black_Sparkle_Color")));
        colors.add(new MenuItemAction(this, Global
                .resourceString("White_Color")));
        colors.add(new MenuItemAction(this, Global
                .resourceString("White_Sparkle_Color")));
        colors.add(new MenuItemAction(this, Global
                .resourceString("Label_Color")));
        colors.add(new MenuItemAction(this, Global
                .resourceString("Marker_Color")));
        options.add(colors);
        options.add(this.MenuBWColor = new CheckboxMenuItemAction(this, Global
                .resourceString("Use_B_W_marks")));
        this.MenuBWColor.setState(rene.gui.Global
                .getParameter("bwcolor", false));
        this.BWColor = this.MenuBWColor.getState();
        options.add(this.PureSGF = new CheckboxMenuItemAction(this, Global
                .resourceString("Save_pure_SGF")));
        this.PureSGF.setState(rene.gui.Global.getParameter("puresgf", false));
        options.add(this.CommentSGF = new CheckboxMenuItemAction(this, Global
                .resourceString("Use_SGF_Comments")));
        this.CommentSGF.setState(rene.gui.Global.getParameter("sgfcomments",
                false));
        options.addSeparator();
        final Menu fonts = new MyMenu(Global.resourceString("Fonts"));
        fonts.add(new MenuItemAction(this, Global.resourceString("Board_Font")));
        fonts.add(new MenuItemAction(this, Global.resourceString("Fixed_Font")));
        fonts.add(new MenuItemAction(this, Global.resourceString("Normal_Font")));
        options.add(fonts);
        final Menu variations = new MyMenu(
                Global.resourceString("Variation_Display"));
        variations.add(this.VCurrent = new CheckboxMenuItemAction(this, Global
                .resourceString("To_Current")));
        this.VCurrent.setState(rene.gui.Global.getParameter("vcurrent", true));
        variations.add(this.VChild = new CheckboxMenuItemAction(this, Global
                .resourceString("To_Child")));
        this.VChild.setState(!rene.gui.Global.getParameter("vcurrent", true));
        variations.add(this.VHide = new CheckboxMenuItemAction(this, Global
                .resourceString("Hide")));
        this.VHide.setState(rene.gui.Global.getParameter("vhide", false));
        variations.addSeparator();
        variations.add(this.VNumbers = new CheckboxMenuItemAction(this, Global
                .resourceString("Continue_Numbers")));
        this.VNumbers.setState(rene.gui.Global.getParameter("variationnumbers",
                false));
        options.add(variations);
        options.addSeparator();
        options.add(this.MenuTarget = new CheckboxMenuItemAction(this, Global
                .resourceString("Show_Target")));
        this.MenuTarget.setState(rene.gui.Global.getParameter("showtarget",
                true));
        this.ShowTarget = this.MenuTarget.getState();
        options.add(this.MenuLastNumber = new CheckboxMenuItemAction(this,
                Global.resourceString("Last_Number")));
        this.MenuLastNumber.setState(rene.gui.Global.getParameter("lastnumber",
                false));
        this.LastNumber = this.MenuLastNumber.getState();
        options.add(new MenuItemAction(this, Global.resourceString("Last_50")));
        options.add(new MenuItemAction(this, Global.resourceString("Last_100")));
        options.addSeparator();
        options.add(this.TrueColor = new CheckboxMenuItemAction(this, Global
                .resourceString("True_Color_Board")));
        this.TrueColor.setState(rene.gui.Global.getParameter("beauty", true));
        options.add(this.TrueColorStones = new CheckboxMenuItemAction(this,
                Global.resourceString("True_Color_Stones")));
        this.TrueColorStones.setState(rene.gui.Global.getParameter(
                "beautystones", true));
        options.add(this.Alias = new CheckboxMenuItemAction(this, Global
                .resourceString("Anti_alias_Stones")));
        this.Alias.setState(rene.gui.Global.getParameter("alias", true));
        options.add(this.Shadows = new CheckboxMenuItemAction(this, Global
                .resourceString("Shadows")));
        this.Shadows.setState(rene.gui.Global.getParameter("shadows", true));
        options.add(this.SmallerStones = new CheckboxMenuItemAction(this,
                Global.resourceString("Smaller_Stones")));
        this.SmallerStones.setState(rene.gui.Global.getParameter(
                "smallerstones", false));
        options.add(this.BlackOnly = new CheckboxMenuItemAction(this, Global
                .resourceString("Black_Only")));
        this.BlackOnly.setState(rene.gui.Global
                .getParameter("blackonly", false));
        options.addSeparator();
        options.add(new MenuItemAction(this, Global
                .resourceString("Set_Encoding")));
        options.add(this.ShowButtons = new CheckboxMenuItemAction(this, Global
                .resourceString("Show_Buttons")));
        this.ShowButtons.setState(rene.gui.Global.getParameter("showbuttons",
                true));
        final Menu help = new MyMenu(Global.resourceString("Help"));
        help.add(new MenuItemAction(this, Global.resourceString("Board_Window")));
        help.add(new MenuItemAction(this, Global.resourceString("Making_Moves")));
        help.add(new MenuItemAction(this, Global
                .resourceString("Keyboard_Shortcuts")));
        help.add(new MenuItemAction(this, Global
                .resourceString("About_Variations")));
        help.add(new MenuItemAction(this, Global
                .resourceString("Playing_Games")));
        help.add(new MenuItemAction(this, Global
                .resourceString("Mailing_Games")));
        M.add(options);
        M.setHelpMenu(help);
        // Board
        this.L = new OutputLabel(Global.resourceString("New_Game"));
        this.Lm = new OutputLabel("--");
        this.B = new Board(19, this);
        final MyPanel BP = new MyPanel();
        BP.setLayout(new BorderLayout());
        BP.add("Center", this.B);
        // Add the label
        final SimplePanel sp = new SimplePanel(this.L, 80, this.Lm, 20);
        BP.add("South", sp);
        // Text Area
        this.Comment = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        this.Comment.setFont(Global.SansSerif);
        JPanel bcp;
        if (rene.gui.Global.getParameter("shownavigationtree", true)) {
            this.Navigation = new NavigationPanel(this.B);
            bcp = new BoardCommentPanel(new Panel3D(BP),
                    new CommentNavigationPanel(new Panel3D(this.Comment),
                            new Panel3D(this.Navigation)), this.B);
        } else {
            bcp = new BoardCommentPanel(new Panel3D(BP), new Panel3D(
                    this.Comment), this.B);
        }
        this.add("Center", bcp);
        // Navigation panel
        this.IB = this.createIconBar();
        this.ButtonP = new Panel3D(this.IB);
        if (rene.gui.Global.getParameter("showbuttons", true)) {
            this.add("South", this.ButtonP);
        }
        // Directory for FileDialog
        this.Dir = new String("");
        Global.setwindow(this, "board", 500, 450, false);
        this.validate();
        this.Show = true;
        this.B.addKeyListener(this);
        if (this.Navigation != null) {
            this.Navigation.addKeyListener(this.B);
        }
        this.addmenuitems();
        this.setVisible(true);
        this.repaint();
    }

    public GoFrame(String s)
    // For children, who set up their own menus
    {
        super(s);
        this.DefaultTitle = s;
        this.seticon("iboard.gif");
        this.setcolors();
    }

    /**
     * tests, if a name is accepted as a SGF file name
     */
    @Override
    public boolean accept(File dir, String name) {
        if (name.endsWith("."
                + rene.gui.Global.getParameter("extension", "sgf"))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void activate() {
        this.Activated = true;
        if (this.LaterLoad != null) {
            if (this.LaterLoadXml) {
                this.doloadXml(this.LaterLoad);
            } else {
                this.doload(this.LaterLoad);
            }
        }
        this.LaterLoad = null;
    }

    /**
     * see, if the board is already acrive
     */
    public void active(boolean f) {
        this.B.active(f);
    }

    // This can be used to set a board position
    // The board is updated directly, if it is at the
    // last move.

    /**
     * add a comment to the board (called from external sources)
     */
    @Override
    public void addComment(String s) {
        this.B.addcomment(s);
    }

    public void addmenuitems()
    // for children to add menu items (because of bug in Linux Java 1.5)
    {
    }

    /**
     * Called from the board to advance the text mark.
     */
    @Override
    public void advanceTextmark() {
        if (this.TMQ != null) {
            this.TMQ.advance();
        }
    }

    /**
     * Called from outside to append something to the comment text area (e.g.
     * from a Distributor).
     */
    @Override
    public void appendComment(String s) {
        this.Comment.append(s);
    }

    /**
     * Called from the board, when a node is to be inserted. Opens a dialog
     * asking for permission.
     */
    @Override
    public boolean askInsert() {
        return new AskInsertQuestion(this).Result;
    }

    /**
     * Opens a dialog to ask for deleting of game trees. This is called from the
     * Board, if the node has grandchildren.
     */
    @Override
    public boolean askUndo() {
        return new AskUndoQuestion(this).Result;
    }

    @Override
    public Color backgroundColor() {
        return Color.gray;
    }

    /**
     * set a black move at i,j
     */
    public void black(int i, int j) {
        this.B.black(i, j);
    }

    @Override
    public Color blackColor() {
        return this.BlackColor;
    }

    @Override
    public boolean blackOnly() {
        if (this.BlackOnly != null) {
            return this.BlackOnly.getState();
        }
        return false;
    }

    @Override
    public Color blackSparkleColor() {
        return this.BlackSparkleColor;
    }

    /**
     * A blocked board cannot react to the user.
     */
    @Override
    public boolean blocked() {
        return false;
    }

    // The following are used from external board
    // drivers to set stones, handicap etc. (like
    // distributors for IGS commands)

    @Override
    public Color boardColor() {
        return this.BoardColor;
    }

    @Override
    public Font boardFont() {
        return Global.BoardFont;
    }

    @Override
    public boolean boardShowing() {
        return this.Show;
    }

    /**
     * called by menu action, opens a SizeQuestion dialog
     */
    public void boardsize() {
        new SizeQuestion(this);
    }

    @Override
    public boolean bwColor() {
        return this.BWColor;
    }

    /**
     * Sets the name of the current board name in a dialog (called from the
     * menu)
     */
    public void callInsert() {
        new NodeNameEdit(this, this.B.getname());
    }

    public void center(FileDialog d) {
        final Point lo = this.getLocation();
        final Dimension di = this.getSize();
        d.setLocation(lo.x + di.width / 2 - 100, lo.y + di.height / 2 - 100);
    }

    @Override
    public boolean close() // try to close
    {
        if (rene.gui.Global.getParameter("confirmations", true)) {
            final CloseQuestion CQ = new CloseQuestion(this);
            if (CQ.Result) {
                Global.notewindow(this, "board");
                this.doclose();
            }
            return false;
        } else {
            Global.notewindow(this, "board");
            this.doclose();
            return false;
        }
    }

    /**
     * Next to move
     */
    public void color(int c) {
        if (c == -1) {
            this.B.white();
        } else {
            this.B.black();
        }
    }

    public IconBar createIconBar() {
        rene.gui.Global.setParameter("iconsize", 32);
        final IconBar I = new IconBar(this);
        I.Resource = "/unagoclient/icons/";
        I.addLeft("undo");
        I.addSeparatorLeft();
        I.addLeft("allback");
        I.addLeft("fastback");
        I.addLeft("back");
        I.addLeft("forward");
        I.addLeft("fastforward");
        I.addLeft("allforward");
        I.addSeparatorLeft();
        I.addLeft("variationback");
        I.addLeft("variationforward");
        I.addLeft("variationstart");
        I.addLeft("main");
        I.addLeft("mainend");
        I.addSeparatorLeft();
        final String icons[] = { "mark", "square", "triangle", "circle",
                "letter", "text", "", "black", "white", "", "setblack",
                "setwhite", "delete" };
        I.addToggleGroupLeft(icons);
        I.addSeparatorLeft();
        I.addLeft("deletemarks");
        I.addLeft("play");
        I.setIconBarListener(this);
        return I;
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Undo").equals(o)) {
            this.B.undo();
        } else if (Global.resourceString("Close").equals(o)) {
            this.close();
        } else if (Global.resourceString("Board_size").equals(o)) {
            this.boardsize();
        } else if ("<".equals(o)) {
            this.B.back();
        } else if (">".equals(o)) {
            this.B.forward();
        } else if (">>".equals(o)) {
            this.B.fastforward();
        } else if ("<<".equals(o)) {
            this.B.fastback();
        } else if ("I<<".equals(o)) {
            this.B.allback();
        } else if (">>I".equals(o)) {
            this.B.allforward();
        } else if ("<V".equals(o)) {
            this.B.varleft();
        } else if ("V>".equals(o)) {
            this.B.varright();
        } else if ("V".equals(o)) {
            this.B.varup();
        } else if ("**".equals(o)) {
            this.B.varmaindown();
        } else if ("*".equals(o)) {
            this.B.varmain();
        } else if (Global.resourceString("Pass").equals(o)) {
            this.B.pass();
            this.notepass();
        } else if (Global.resourceString("Resume_playing").equals(o)) {
            this.B.resume();
        } else if (Global.resourceString("Clear_all_marks").equals(o)) {
            this.B.clearmarks();
        } else if (Global.resourceString("Undo_Adding_Removing").equals(o)) {
            this.B.clearremovals();
        } else if (Global.resourceString("Remove_groups").equals(o)) {
            this.B.score();
        } else if (Global.resourceString("Score").equals(o)) {
            final String s = this.B.done();
            if (s != null) {
                new Message(this, s);
            }
        } else if (Global.resourceString("Local_Count").equals(o)) {
            new Message(this, this.B.docount());
        } else if (Global.resourceString("New").equals(o)) {
            this.B.deltree();
            this.B.copy();
            this.setTitle(this.DefaultTitle);
        } else if (Global.resourceString("Mail").equals(o)) // mail the game
        {
            final ByteArrayOutputStream ba = new ByteArrayOutputStream(50000);
            try {
                if (rene.gui.Global.getParameter("xml", false)) {
                    final PrintWriter po = new PrintWriter(
                            new OutputStreamWriter(ba, "UTF8"), true);
                    this.B.saveXML(po, "utf-8");
                    po.close();
                } else {
                    final PrintWriter po = new PrintWriter(ba, true);
                    this.B.save(po);
                    po.close();
                }
            } catch (final Exception ex) {
            }
            new MailDialog(this, ba.toString());
            return;
        } else if (Global.resourceString("Ascii_Mail").equals(o))
        // ascii dump of the game
        {
            final ByteArrayOutputStream ba = new ByteArrayOutputStream(10000);
            final PrintWriter po = new PrintWriter(ba, true);
            try {
                this.B.asciisave(po);
            } catch (final Exception ex) {
            }
            new MailDialog(this, ba.toString());
            return;
        } else if (Global.resourceString("Print").equals(o)) // print the game
        {
            this.B.print(Global.frame());
        } else if (Global.resourceString("Save").equals(o)) // save the game
        { // File dialog handling
            final FileDialog fd = new FileDialog(this,
                    Global.resourceString("Save"), FileDialog.SAVE);
            if (!this.Dir.equals("")) {
                fd.setDirectory(this.Dir);
            }
            final String s = this.B.firstnode().getaction("GN");
            if (s != null && !s.equals("")) {
                fd.setFile(s
                        + "."
                        + rene.gui.Global
                        .getParameter("extension", rene.gui.Global
                                .getParameter("xml", false) ? "xml"
                                        : "sgf"));
            } else {
                fd.setFile("*."
                        + rene.gui.Global
                        .getParameter("extension", rene.gui.Global
                                .getParameter("xml", false) ? "xml"
                                        : "sgf"));
            }
            fd.setFilenameFilter(this);
            this.center(fd);
            fd.setVisible(true);
            final String fn = fd.getFile();
            if (fn == null) {
                return;
            }
            this.setGameTitle(FileName.purefilename(fn));
            this.Dir = fd.getDirectory();
            try
            // print out using the board class
            {
                PrintWriter fo;
                if (rene.gui.Global.getParameter("xml", false)) {
                    if (Global.isApplet()) {
                        fo = new PrintWriter(new OutputStreamWriter(
                                new FileOutputStream(fd.getDirectory() + fn),
                                "UTF8"));
                        this.B.saveXML(fo, "utf-8");
                    } else {
                        String Encoding = rene.gui.Global
                                .getParameter("encoding",
                                        System.getProperty("file.encoding"))
                                        .toUpperCase();
                        if (Encoding.equals("")) {
                            fo = new PrintWriter(
                                    new OutputStreamWriter(
                                            new FileOutputStream(
                                                    fd.getDirectory() + fn),
                                            "UTF8"));
                            this.B.saveXML(fo, "utf-8");
                        } else {
                            String XMLEncoding = "";
                            if (Encoding.equals("CP1252")
                                    || Encoding.equals("ISO8859_1")) {
                                Encoding = "ISO8859_1";
                                XMLEncoding = "iso-8859-1";
                            } else {
                                Encoding = "UTF8";
                                XMLEncoding = "utf-8";
                            }
                            final FileOutputStream fos = new FileOutputStream(
                                    fd.getDirectory() + fn);
                            try {
                                fo = new PrintWriter(new OutputStreamWriter(
                                        fos, Encoding));
                            } catch (final Exception e) {
                                Encoding = "UTF8";
                                XMLEncoding = "utf-8";
                                fo = new PrintWriter(new OutputStreamWriter(
                                        fos, Encoding));
                            }
                            this.B.saveXML(fo, XMLEncoding);
                        }
                    }
                } else {
                    if (Global.isApplet()) {
                        fo = new PrintWriter(new OutputStreamWriter(
                                new FileOutputStream(fd.getDirectory() + fn),
                                rene.gui.Global.getParameter("encoding",
                                        "ASCII")));
                    } else {
                        fo = new PrintWriter(new OutputStreamWriter(
                                new FileOutputStream(fd.getDirectory() + fn),
                                rene.gui.Global.getParameter("encoding",
                                        System.getProperty("file.encoding"))));
                    }
                    this.B.save(fo);
                }
                fo.close();
            } catch (final IOException ex) {
                new Message(this, Global.resourceString("Write_error_") + "\n"
                        + ex.toString());
                return;
            }
        } else if (Global.resourceString("Save_Position").equals(o)) // save the
        // position
        { // File dialog handling
            final FileDialog fd = new FileDialog(this,
                    Global.resourceString("Save Position"), FileDialog.SAVE);
            if (!this.Dir.equals("")) {
                fd.setDirectory(this.Dir);
            }
            final String s = this.B.firstnode().getaction("GN");
            if (s != null && !s.equals("")) {
                fd.setFile(s
                        + "."
                        + rene.gui.Global
                        .getParameter("extension", rene.gui.Global
                                .getParameter("xml", false) ? "xml"
                                        : "sgf"));
            } else {
                fd.setFile("*."
                        + rene.gui.Global
                        .getParameter("extension", rene.gui.Global
                                .getParameter("xml", false) ? "xml"
                                        : "sgf"));
            }
            fd.setFilenameFilter(this);
            this.center(fd);
            fd.setVisible(true);
            final String fn = fd.getFile();
            if (fn == null) {
                return;
            }
            this.Dir = fd.getDirectory();
            try
            // print out using the board class
            {
                PrintWriter fo;
                if (rene.gui.Global.getParameter("xml", false)) {
                    if (Global.isApplet()) {
                        fo = new PrintWriter(new OutputStreamWriter(
                                new FileOutputStream(fd.getDirectory() + fn),
                                "UTF8"));
                        this.B.saveXML(fo, "utf-8");
                    } else {
                        String Encoding = rene.gui.Global
                                .getParameter("encoding",
                                        System.getProperty("file.encoding"))
                                        .toUpperCase();
                        if (Encoding.equals("")) {
                            fo = new PrintWriter(
                                    new OutputStreamWriter(
                                            new FileOutputStream(
                                                    fd.getDirectory() + fn),
                                            "UTF8"));
                            this.B.saveXMLPos(fo, "utf-8");
                        } else {
                            String XMLEncoding = "";
                            if (Encoding.equals("CP1252")
                                    || Encoding.equals("ISO8859_1")) {
                                Encoding = "ISO8859_1";
                                XMLEncoding = "iso-8859-1";
                            } else {
                                Encoding = "UTF8";
                                XMLEncoding = "utf-8";
                            }
                            final FileOutputStream fos = new FileOutputStream(
                                    fd.getDirectory() + fn);
                            try {
                                fo = new PrintWriter(new OutputStreamWriter(
                                        fos, Encoding));
                            } catch (final Exception e) {
                                Encoding = "UTF8";
                                XMLEncoding = "utf-8";
                                fo = new PrintWriter(new OutputStreamWriter(
                                        fos, Encoding));
                            }
                            this.B.saveXMLPos(fo, XMLEncoding);
                        }
                    }
                } else {
                    if (Global.isApplet()) {
                        fo = new PrintWriter(new OutputStreamWriter(
                                new FileOutputStream(fd.getDirectory() + fn),
                                rene.gui.Global.getParameter("encoding",
                                        "ASCII")));
                    } else {
                        fo = new PrintWriter(new OutputStreamWriter(
                                new FileOutputStream(fd.getDirectory() + fn),
                                rene.gui.Global.getParameter("encoding",
                                        System.getProperty("file.encoding"))));
                    }
                    this.B.savePos(fo);
                }
                fo.close();
            } catch (final IOException ex) {
                new Message(this, Global.resourceString("Write_error_") + "\n"
                        + ex.toString());
                return;
            }
        } else if (Global.resourceString("Save_Bitmap").equals(o)) // save the
        // game
        { // File dialog handling
            final FileDialog fd = new FileDialog(this,
                    Global.resourceString("Save_Bitmap"), FileDialog.SAVE);
            if (!this.Dir.equals("")) {
                fd.setDirectory(this.Dir);
            }
            final String s = this.B.firstnode().getaction("GN");
            if (s != null && !s.equals("")) {
                fd.setFile(s + "."
                        + rene.gui.Global.getParameter("extension", "bmp"));
            } else {
                fd.setFile("*."
                        + rene.gui.Global.getParameter("extension", "bmp"));
            }
            fd.setFilenameFilter(this);
            this.center(fd);
            fd.setVisible(true);
            final String fn = fd.getFile();
            if (fn == null) {
                return;
            }
            this.Dir = fd.getDirectory();
            try
            // print out using the board class
            {
                final BMPFile F = new BMPFile();
                final Dimension d = this.B.getBoardImageSize();
                F.saveBitmap(fd.getDirectory() + fn, this.B.getBoardImage(),
                        d.width, d.height);
            } catch (final Exception ex) {
                new Message(this, Global.resourceString("Write_error_") + "\n"
                        + ex.toString());
                return;
            }
        } else if (Global.resourceString("Load").equals(o)) // load a game
        { // File dialog handling
            final FileDialog fd = new FileDialog(this,
                    Global.resourceString("Load_Game"), FileDialog.LOAD);
            if (!this.Dir.equals("")) {
                fd.setDirectory(this.Dir);
            }
            fd.setFilenameFilter(this);
            fd.setFile("*."
                    + rene.gui.Global.getParameter("extension", rene.gui.Global
                            .getParameter("xml", false) ? "xml" : "sgf"));
            this.center(fd);
            fd.setVisible(true);
            final String fn = fd.getFile();
            if (fn == null) {
                return;
            }
            this.Dir = fd.getDirectory();
            try
            // print out using the board class
            {
                if (rene.gui.Global.getParameter("xml", false)) {
                    final InputStream in = new FileInputStream(
                            fd.getDirectory() + fn);
                    try {
                        this.B.loadXml(new XmlReader(in));
                    } catch (final XmlReaderException e) {
                        new Message(this, "Error in file!\n" + e.getText());
                    }
                    in.close();
                } else {
                    BufferedReader fi;
                    if (Global.isApplet()) {
                        fi = new BufferedReader(new InputStreamReader(
                                new FileInputStream(fd.getDirectory() + fn),
                                rene.gui.Global.getParameter("encoding", "")));
                    } else {
                        fi = new BufferedReader(new InputStreamReader(
                                new FileInputStream(fd.getDirectory() + fn),
                                rene.gui.Global.getParameter("encoding",
                                        System.getProperty("file.encoding"))));
                    }
                    try {
                        this.B.load(fi);
                    } catch (final IOException e) {
                        new Message(this, "Error in file!");
                    }
                    fi.close();
                }
            } catch (final IOException ex) {
                new Message(this, Global.resourceString("Read_error_") + "\n"
                        + ex.toString());
                return;
            }
            final String s = this.B.firstnode().getaction("GN");
            if (s != null && !s.equals("")) {
                this.setTitle(s);
            } else {
                this.B.firstnode().setaction("GN", FileName.purefilename(fn));
                this.setTitle(FileName.purefilename(fn));
            }
            if (fn.toLowerCase().indexOf("kogo") >= 0) {
                this.B.setVariationStyle(false, false);
            }
        } else if (Global.resourceString("Load_from_Clipboard").equals(o)) {
            this.loadClipboard();
        } else if (Global.resourceString("Copy_to_Clipboard").equals(o)) {
            this.saveClipboard();
        } else if (Global.resourceString("Board_Window").equals(o)) {
            new Help("board");
        } else if (Global.resourceString("Making_Moves").equals(o)) {
            new Help("moves");
        } else if (Global.resourceString("Keyboard_Shortcuts").equals(o)) {
            new Help("keyboard");
        } else if (Global.resourceString("Playing_Games").equals(o)) {
            new Help("playing");
        } else if (Global.resourceString("About_Variations").equals(o)) {
            new Help("variations");
        } else if (Global.resourceString("Mailing_Games").equals(o)) {
            new Help("mail");
        } else if (Global.resourceString("Insert_Node").equals(o)) {
            this.B.insertnode();
        } else if (Global.resourceString("Insert_Variation").equals(o)) {
            this.B.insertvariation();
        } else if (Global.resourceString("Game_Information").equals(o)) {
            new EditInformation(this, this.B.firstnode());
        } else if (Global.resourceString("Game_Copyright").equals(o)) {
            new EditCopyright(this, this.B.firstnode());
        } else if (Global.resourceString("Prisoner_Count").equals(o)) {
            final String s = Global.resourceString("Black__") + this.B.Pw
                    + Global.resourceString("__White__") + this.B.Pb + "\n"
                    + Global.resourceString("Komi") + " " + this.B.getKomi();
            new Message(this, s);
        } else if (Global.resourceString("Board_Color").equals(o)) {
            new BoardColorEdit(this, "boardcolor", this.BoardColor);
        } else if (Global.resourceString("Black_Color").equals(o)) {
            new BoardColorEdit(this, "blackcolor", this.BlackColor);
        } else if (Global.resourceString("Black_Sparkle_Color").equals(o)) {
            new BoardColorEdit(this, "blacksparklecolor",
                    this.BlackSparkleColor);
        } else if (Global.resourceString("White_Color").equals(o)) {
            new BoardColorEdit(this, "whitecolor", this.WhiteColor);
        } else if (Global.resourceString("White_Sparkle_Color").equals(o)) {
            new BoardColorEdit(this, "whitesparklecolor",
                    this.WhiteSparkleColor);
        } else if (Global.resourceString("Label_Color").equals(o)) {
            new BoardColorEdit(this, "labelcolor", this.LabelColor);
        } else if (Global.resourceString("Marker_Color").equals(o)) {
            new BoardColorEdit(this, "markercolor", this.MarkerColor);
        } else if (Global.resourceString("Board_Font").equals(o)) {
            new BoardGetFontSize(this, "boardfontname",
                    rene.gui.Global.getParameter("boardfontname", "SansSerif"),
                    "boardfontsize", rene.gui.Global.getParameter(
                            "boardfontsize", 10), true).setVisible(true);
            this.updateall();
        } else if (Global.resourceString("Normal_Font").equals(o)) {
            new BoardGetFontSize(this, "sansserif",
                    rene.gui.Global.getParameter("sansserif", "SansSerif"),
                    "ssfontsize",
                    rene.gui.Global.getParameter("ssfontsize", 11), true)
            .setVisible(true);
            this.updateall();
        } else if (Global.resourceString("Fixed_Font").equals(o)) {
            new BoardGetFontSize(this, "monospaced",
                    rene.gui.Global.getParameter("monospaced", "Monospaced"),
                    "msfontsize",
                    rene.gui.Global.getParameter("msfontsize", 11), true)
            .setVisible(true);
            this.updateall();
        } else if (Global.resourceString("Last_50").equals(o)) {
            this.B.lastrange(50);
        } else if (Global.resourceString("Last_100").equals(o)) {
            this.B.lastrange(100);
        } else if (Global.resourceString("Node_Name").equals(o)) {
            this.callInsert();
        } else if (Global.resourceString("Goto_Next_Name").equals(o)) {
            this.B.gotonext();
        } else if (Global.resourceString("Goto_Previous_Name").equals(o)) {
            this.B.gotoprevious();
        } else if (Global.resourceString("Next_Game").equals(o)) {
            this.B.gotonextmain();
        } else if (Global.resourceString("Previous_Game").equals(o)) {
            this.B.gotopreviousmain();
        } else if (Global.resourceString("Add_Game").equals(o)) {
            this.B.addnewgame();
        } else if (Global.resourceString("Remove_Game").equals(o)) {
            this.B.removegame();
        } else if (Global.resourceString("Set_Encoding").equals(o)) {
            new GetEncoding(this);
        } else if (Global.resourceString("Search_Again").equals(o)) {
            this.search();
        } else if (Global.resourceString("Search").equals(o)) {
            new GetSearchString(this);
        } else {
            super.doAction(o);
        }
    }

    /**
     * Called from the BoardsizeQuestion dialog.
     *
     * @param s
     *            the size of the board.
     */
    public void doboardsize(int s) {
        this.B.setsize(s);
    }

    /**
     * Actually do the loading, when the board is ready.
     */
    public void doload(Reader file) {
        this.validate();
        try {
            this.B.load(new BufferedReader(file));
            file.close();
            this.setGameTitle(this.LaterFilename);
            this.B.gotoMove(this.LaterMove);
        } catch (final Exception ex) {
            final rene.dialogs.Warning w = new rene.dialogs.Warning(this,
                    ex.toString(), "Warning", true);
            w.center(this);
            w.setVisible(true);
        }
    }

    /**
     * Actually do the loading, when the board is ready.
     */
    public void doloadXml(Reader file) {
        this.validate();
        try {
            final XmlReader xml = new XmlReader(new BufferedReader(file));
            this.B.loadXml(xml);
            file.close();
            this.setGameTitle(this.LaterFilename);
        } catch (final Exception ex) {
            final rene.dialogs.Warning w = new rene.dialogs.Warning(this,
                    ex.toString(), "Warning", true);
            w.center(this);
            w.setVisible(true);
        }
    }

    public Frame frame() {
        return Global.frame();
    }

    /**
     * Determine the board size (for external purpose)
     *
     * @return the board size
     */
    public int getboardsize() {
        return this.B.getboardsize();
    }

    @Override
    public Color getColor(String S, int r, int g, int b) {
        return Global.getColor(S, r, g, b);
    }

    /**
     * Called from the Board to read the comment text area.
     */
    @Override
    public String getComment() {
        return this.Comment.getText();
    }

    @Override
    public boolean getParameter(String s, boolean f) {
        return rene.gui.Global.getParameter(s, f);
    }

    /**
     * Set a handicap to the Board.
     *
     * @param n
     *            number of stones
     */
    public void handicap(int n) {
        this.B.handicap(n);
    }

    @Override
    public void iconPressed(String s) {
        if (s.equals("undo")) {
            this.doAction(Global.resourceString("Undo"));
        } else if (s.equals("allback")) {
            this.doAction("I<<");
        } else if (s.equals("fastback")) {
            this.doAction("<<");
        } else if (s.equals("back")) {
            this.doAction("<");
        } else if (s.equals("forward")) {
            this.doAction(">");
        } else if (s.equals("fastforward")) {
            this.doAction(">>");
        } else if (s.equals("allforward")) {
            this.doAction(">>I");
        } else if (s.equals("variationback")) {
            this.doAction("<V");
        } else if (s.equals("variationstart")) {
            this.doAction("V");
        } else if (s.equals("variationforward")) {
            this.doAction("V>");
        } else if (s.equals("main")) {
            this.doAction("*");
        } else if (s.equals("mainend")) {
            this.doAction("**");
        } else if (s.equals("mark")) {
            this.B.mark();
        } else if (s.equals("mark")) {
            this.B.mark();
        } else if (s.equals("square")) {
            this.B.specialmark(Field.SQUARE);
        } else if (s.equals("triangle")) {
            this.B.specialmark(Field.TRIANGLE);
        } else if (s.equals("circle")) {
            this.B.specialmark(Field.CIRCLE);
        } else if (s.equals("letter")) {
            this.B.letter();
        } else if (s.equals("text")) {
            this.B.textmark(this.Text);
            if (this.TMQ == null) {
                this.TMQ = new TextMarkQuestion(this, this.Text);
            }
        } else if (s.equals("black")) {
            this.B.black();
        } else if (s.equals("white")) {
            this.B.white();
        } else if (s.equals("setblack")) {
            this.B.setblack();
        } else if (s.equals("setwhite")) {
            this.B.setwhite();
        } else if (s.equals("delete")) {
            this.B.deletestones();
        } else if (s.equals("deletemarks")) {
            this.B.clearmarks();
        } else if (s.equals("play")) {
            this.B.resume();
        }
    }

    @Override
    public void itemAction(String o, boolean flag) {
        if (Global.resourceString("Save_pure_SGF").equals(o)) {
            rene.gui.Global.setParameter("puresgf", flag);
        } else if (Global.resourceString("Use_SGF_Comments").equals(o)) {
            rene.gui.Global.setParameter("sgfcomments", flag);
        } else if (Global.resourceString("On").equals(o)) {
            rene.gui.Global.setParameter("coordinates", flag);
            this.updateall();
        } else if (Global.resourceString("Upper_Left").equals(o)) {
            rene.gui.Global.setParameter("upperleftcoordinates", flag);
            this.updateall();
        } else if (Global.resourceString("Lower_Right").equals(o)) {
            rene.gui.Global.setParameter("lowerrightcoordinates", flag);
            this.updateall();
        } else if (Global.resourceString("Set_Black").equals(o)) {
            this.B.setblack();
        } else if (Global.resourceString("Set_White").equals(o)) {
            this.B.setwhite();
        } else if (Global.resourceString("Black_to_play").equals(o)) {
            this.B.black();
        } else if (Global.resourceString("White_to_play").equals(o)) {
            this.B.white();
        } else if (Global.resourceString("Mark").equals(o)) {
            this.B.mark();
        } else if (Global.resourceString("Text").equals(o)) {
            this.B.textmark(this.Text);
            if (this.TMQ == null) {
                this.TMQ = new TextMarkQuestion(this, this.Text);
            }
        } else if (Global.resourceString("Square").equals(o)) {
            this.B.specialmark(Field.SQUARE);
        } else if (Global.resourceString("Triangle").equals(o)) {
            this.B.specialmark(Field.TRIANGLE);
        } else if (Global.resourceString("Cross").equals(o)) {
            this.B.specialmark(Field.CROSS);
        } else if (Global.resourceString("Circle").equals(o)) {
            this.B.specialmark(Field.CIRCLE);
        } else if (Global.resourceString("Letter").equals(o)) {
            this.B.letter();
        } else if (Global.resourceString("Delete").equals(o)) {
            this.B.deletestones();
        } else if (Global.resourceString("True_Color_Board").equals(o)) {
            rene.gui.Global.setParameter("beauty", flag);
            this.updateall();
        } else if (Global.resourceString("True_Color_Stones").equals(o)) {
            rene.gui.Global.setParameter("beautystones", flag);
            this.updateall();
        } else if (Global.resourceString("Anti_alias_Stones").equals(o)) {
            rene.gui.Global.setParameter("alias", flag);
            this.updateall();
        } else if (Global.resourceString("Shadows").equals(o)) {
            rene.gui.Global.setParameter("shadows", flag);
            this.updateall();
        } else if (Global.resourceString("Smaller_Stones").equals(o)) {
            rene.gui.Global.setParameter("smallerstones", flag);
            this.updateall();
        } else if (Global.resourceString("Black_Only").equals(o)) {
            rene.gui.Global.setParameter("blackonly", flag);
            this.updateall();
        } else if (Global.resourceString("Use_B_W_marks").equals(o)) {
            this.BWColor = flag;
            rene.gui.Global.setParameter("bwcolor", this.BWColor);
            this.updateall();
        } else if (Global.resourceString("Last_Number").equals(o)) {
            this.LastNumber = flag;
            rene.gui.Global.setParameter("lastnumber", this.LastNumber);
            this.B.updateall();
        } else if (Global.resourceString("Show_Target").equals(o)) {
            this.ShowTarget = flag;
            rene.gui.Global.setParameter("showtarget", this.ShowTarget);
        } else if (Global.resourceString("Show_Buttons").equals(o)) {
            if (flag) {
                this.add("South", this.ButtonP);
            } else {
                this.remove(this.ButtonP);
            }
            if (this instanceof ConnectedGoFrame) {
                rene.gui.Global.setParameter("showbuttonsconnected", flag);
            } else {
                rene.gui.Global.setParameter("showbuttons", flag);
            }
            this.setVisible(true);
            this.validate();
            this.doLayout();
            this.setVisible(true);
        } else if (Global.resourceString("Use_XML").equals(o)) {
            this.UseXML.setState(true);
            this.UseSGF.setState(false);
            rene.gui.Global.setParameter("xml", true);
        } else if (Global.resourceString("Use_SGF").equals(o)) {
            this.UseSGF.setState(true);
            this.UseXML.setState(false);
            rene.gui.Global.setParameter("xml", false);
        } else if (Global.resourceString("Hide").equals(o)) {
            this.B.setVariationStyle(flag, this.VCurrent.getState());
            rene.gui.Global.setParameter("vhide", flag);
        } else if (Global.resourceString("To_Current").equals(o)) {
            this.VCurrent.setState(true);
            this.VChild.setState(false);
            this.B.setVariationStyle(this.VHide.getState(), true);
        } else if (Global.resourceString("To_Child").equals(o)) {
            this.VCurrent.setState(false);
            this.VChild.setState(true);
            this.B.setVariationStyle(this.VHide.getState(), false);
        } else if (Global.resourceString("Continue_Numbers").equals(o)) {
            rene.gui.Global.setParameter("variationnumbers", flag);
        }
    }

    /**
     * Process the insert key, which set the node name by opening the
     * correspinding dialog.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isActionKey()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_INSERT:
                    this.callInsert();
                    break;
            }
        } else {
            switch (e.getKeyChar()) {
                case 'f':
                case 'F':
                    this.B.search(rene.gui.Global.getParameter("searchstring",
                            "++"));
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public Color labelColor(int color) {
        switch (color) {
            case 1:
                return this.LabelColor.brighter().brighter();
            case -1:
                return this.LabelColor.darker().darker();
            default:
                return this.LabelColor.brighter();
        }
    }

    @Override
    public boolean lastNumber() {
        return this.LastNumber;
    }

    public void load(String file) {
        this.load(file, 0);
    }

    /**
     * Note that the board must only load a file, when it is ready. This is to
     * interpret a command line argument SGF filename.
     */
    public synchronized void load(String file, int move) {
        this.LaterFilename = FileName.purefilename(file);
        this.LaterMove = move;
        try {
            if (file.endsWith(".xml")) {
                this.LaterLoad = new InputStreamReader(
                        new FileInputStream(file), "UTF8");
                this.LaterLoadXml = true;
            } else {
                this.LaterLoad = new InputStreamReader(
                        new FileInputStream(file));
                this.LaterLoadXml = false;
            }
        } catch (final Exception e) {
            this.LaterLoad = null;
        }
        if (this.LaterLoad != null && this.Activated) {
            this.activate();
        }
    }

    /**
     * Note that the board must load a file, when it is ready. This is to
     * interpret a command line argument SGF filename.
     */
    public void load(URL file) {
        this.LaterFilename = file.toString();
        try {
            if (file.toExternalForm().endsWith(".xml")) {
                this.LaterLoad = new InputStreamReader(file.openStream(),
                        "UTF8");
                this.LaterLoadXml = true;
            } else {
                this.LaterLoad = new InputStreamReader(file.openStream());
                this.LaterLoadXml = false;
            }
        } catch (final Exception e) {
            this.LaterLoad = null;
        }
    }

    public void loadClipboard() {
        try {
            final Clipboard clip = this.getToolkit().getSystemClipboard();
            final Transferable t = clip.getContents(this);
            final String S = (String) t
                    .getTransferData(DataFlavor.stringFlavor);
            this.LaterFilename = "Clipboard Content";
            if (XmlReader.testXml(S)) {
                this.doloadXml(new StringReader(S));
            } else {
                this.doload(new StringReader(S));
            }
        } catch (final Exception e) {
        }
    }

    @Override
    public void lostOwnership(Clipboard b, Transferable s) {
    }

    @Override
    public Color markerColor(int color) {
        switch (color) {
            case 1:
                return this.MarkerColor.brighter().brighter();
            case -1:
                return this.MarkerColor.darker().darker();
            default:
                return this.MarkerColor;
        }
    }

    /**
     * pass (only proceeded from ConnectedGoFrame)
     */
    public void movepass() {
    }

    /**
     * set a move at i,j (called from Board)
     */
    public boolean moveset(int i, int j) {
        return true;
    }

    /**
     * Notify about pass
     */
    public void notepass() {
    }

    /**
     * pass the Board
     */
    public void pass() {
        this.B.pass();
    }

    /**
     * Remove a group at i,j in the board.
     */
    public void remove(int i, int j) {
        this.B.remove(i, j);
    }

    @Override
    public String resourceString(String S) {
        return Global.resourceString(S);
    }

    @Override
    public void result(int b, int w) {
    }

    public void saveClipboard() {
        try {
            final ByteArrayOutputStream ba = new ByteArrayOutputStream(50000);
            try {
                if (rene.gui.Global.getParameter("xml", false)) {
                    final PrintWriter po = new PrintWriter(
                            new OutputStreamWriter(ba, "UTF8"), true);
                    this.B.saveXML(po, "utf-8");
                    po.close();
                } else {
                    final PrintWriter po = new PrintWriter(ba, true);
                    this.B.save(po);
                    po.close();
                }
            } catch (final Exception ex) {
            }
            final String S = ba.toString();
            final Clipboard clip = this.getToolkit().getSystemClipboard();
            final StringSelection sel = new StringSelection(S);
            clip.setContents(sel, this);
        } catch (final Exception e) {
        }
    }

    public void search() {
        this.B.search(rene.gui.Global.getParameter("searchstring", "++"));
    }

    /**
     * set a black stone at i,j
     */
    public void setblack(int i, int j) {
        this.B.setblack(i, j);
    }

    void setcolors() { // Take colors from Global parameters.
        this.BoardColor = Global.getColor("boardcolor", 170, 120, 70);
        this.BlackColor = Global.getColor("blackcolor", 30, 30, 30);
        this.BlackSparkleColor = Global.getColor("blacksparklecolor", 120, 120,
                120);
        this.WhiteColor = Global.getColor("whitecolor", 210, 210, 210);
        this.WhiteSparkleColor = Global.getColor("whitesparklecolor", 250, 250,
                250);
        this.MarkerColor = Global.getColor("markercolor", Color.blue);
        this.LabelColor = Global.getColor("labelcolor", Color.pink.darker());
        Global.setColor("boardcolor", this.BoardColor);
        Global.setColor("blackcolor", this.BlackColor);
        Global.setColor("blacksparklecolor", this.BlackSparkleColor);
        Global.setColor("whitecolor", this.WhiteColor);
        Global.setColor("whitesparklecolor", this.WhiteSparkleColor);
        Global.setColor("markercolor", this.MarkerColor);
        Global.setColor("labelcolor", this.LabelColor);
    }

    /**
     * Called from the board to set the comment of a board in the Comment text
     * area.
     */
    @Override
    public void setComment(String s) {
        this.Comment.setText(s);
        this.Comment.append("");
    }

    public void setGameTitle(String filename) {
        final String s = this.B.firstnode().getaction("GN");
        if (s != null && !s.equals("")) {
            this.setTitle(s);
        } else {
            this.B.firstnode().addaction(new Action("GN", filename));
            this.setTitle(filename);
        }
    }

    /**
     * Called from the board to set the Label below the board.
     */
    @Override
    public void setLabel(String s) {
        this.L.setText(s);
        if (this.Navigation != null) {
            this.Navigation.repaint();
        }
    }

    /**
     * Called from the board to set the label for the cursor position.
     */
    @Override
    public void setLabelM(String s) {
        this.Lm.setText(s);
    }

    /**
     * Called from board to check the proper menu for markers.
     *
     * @param i
     *            the number of the marker type.
     */
    @Override
    public void setMarkState(int i) {
        this.setState(0);
        switch (i) {
            case Field.SQUARE:
                this.Square.setState(true);
                break;
            case Field.TRIANGLE:
                this.Triangle.setState(true);
                break;
            case Field.CROSS:
                this.Cross.setState(true);
                break;
            case Field.CIRCLE:
                this.Circle.setState(true);
                break;
        }
        switch (i) {
            case Field.SQUARE:
                this.IB.setState("square", true);
                break;
            case Field.TRIANGLE:
                this.IB.setState("triangle", true);
                break;
            case Field.CROSS:
                this.IB.setState("mark", true);
                break;
            case Field.CIRCLE:
                this.IB.setState("circle", true);
                break;
        }
    }

    /**
     * Sets the name of the Board (called from a Distributor)
     *
     * @see unagoclient.igs.Distributor
     */
    public void setname(String s) {
        this.B.setname(s);
    }

    public void setpass() {
        this.B.setpass();
    }

    /**
     * This called from the board to set the menu checks according to the
     * current state.
     *
     * @param i
     *            the number of the state the Board is in.
     */
    @Override
    public void setState(int i) {
        this.Black.setState(false);
        this.White.setState(false);
        this.SetBlack.setState(false);
        this.SetWhite.setState(false);
        this.Mark.setState(false);
        this.Letter.setState(false);
        this.Hide.setState(false);
        this.Circle.setState(false);
        this.Cross.setState(false);
        this.Triangle.setState(false);
        this.Square.setState(false);
        this.TextMark.setState(false);
        switch (i) {
            case 1:
                this.Black.setState(true);
                break;
            case 2:
                this.White.setState(true);
                break;
            case 3:
                this.SetBlack.setState(true);
                break;
            case 4:
                this.SetWhite.setState(true);
                break;
            case 5:
                this.Mark.setState(true);
                break;
            case 6:
                this.Letter.setState(true);
                break;
            case 7:
                this.Hide.setState(true);
                break;
            case 10:
                this.TextMark.setState(true);
                break;
        }
        switch (i) {
            case 1:
                this.IB.setState("black", true);
                break;
            case 2:
                this.IB.setState("white", true);
                break;
            case 3:
                this.IB.setState("setblack", true);
                break;
            case 4:
                this.IB.setState("setwhite", true);
                break;
            case 5:
                this.IB.setState("mark", true);
                break;
            case 6:
                this.IB.setState("letter", true);
                break;
            case 7:
                this.IB.setState("delete", true);
                break;
            case 10:
                this.IB.setState("text", true);
                break;
        }
    }

    /**
     * Called from board to enable and disable navigation buttons.
     *
     * @param i
     *            the number of the button
     * @param f
     *            enable/disable the button
     */
    @Override
    public void setState(int i, boolean f) {
        switch (i) {
            case 1:
                this.IB.setEnabled("variationback", f);
                break;
            case 2:
                this.IB.setEnabled("variationforward", f);
                break;
            case 3:
                this.IB.setEnabled("variationstart", f);
                break;
            case 4:
                this.IB.setEnabled("main", f);
                break;
            case 5:
                this.IB.setEnabled("fastforward", f);
                this.IB.setEnabled("forward", f);
                this.IB.setEnabled("allforward", f);
                break;
            case 6:
                this.IB.setEnabled("fastback", f);
                this.IB.setEnabled("back", f);
                this.IB.setEnabled("allback", f);
                break;
            case 7:
                this.IB.setEnabled("mainend", f);
                this.IB.setEnabled("sendforward", !f);
                break;
        }
    }

    /**
     * Called from the edit marker label dialog, when its text has been entered
     * by the user.
     *
     * @param s
     *            the marker to be used by the board
     */
    void setTextmark(String s) {
        this.B.textmark(s);
    }

    /**
     * set a black stone at i,j
     */
    public void setwhite(int i, int j) {
        this.B.setwhite(i, j);
    }

    @Override
    public boolean showTarget() {
        return this.ShowTarget;
    }

    /**
     * mark the field at i,j as territory
     */
    public void territory(int i, int j) {
        this.B.territory(i, j);
    }

    // interface routines for the BoardInterface

    /**
     * undo (only processed from ConnectedGoFrame)
     */
    public void undo() {
    }

    /**
     * Undo moves on the board (called from a distributor e.g.)
     *
     * @param n
     *            numbers of moves to undo.
     */
    public void undo(int n) {
        this.B.undo(n);
    }

    /**
     * Repaint the board, when color or font changes.
     */
    public void updateall() {
        this.setcolors();
        this.B.updateboard();
    }

    @Override
    public String version() {
        return "Version " + this.resourceString("Version");
    }

    /**
     * set a white move at i,j
     */
    public void white(int i, int j) {
        this.B.white(i, j);
    }

    @Override
    public Color whiteColor() {
        return this.WhiteColor;
    }

    @Override
    public Color whiteSparkleColor() {
        return this.WhiteSparkleColor;
    }

    @Override
    public void yourMove(boolean notinpos) {
    }
}

/**
 * // Get/Set the name of the current node
 */

class NodeNameEdit extends GetParameter {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NodeNameEdit(GoFrame g, String s) {
        super(g, Global.resourceString("Name"), Global
                .resourceString("Node_Name"), g, true);
        this.set(s);
        this.setVisible(true);
    }

    @Override
    public boolean tell(Object o, String s) {
        ((GoFrame) o).setname(s);
        return true;
    }
}

class SizeQuestion extends GetParameter
// Ask the board size.
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SizeQuestion(GoFrame g) {
        super(g, Global.resourceString("Size_between_5_and_29"), Global
                .resourceString("Board_size"), g, true);
        this.setVisible(true);
    }

    @Override
    public boolean tell(Object o, String s) {
        int n;
        try {
            n = Integer.parseInt(s);
            if (n < 5 || n > 59) {
                return false;
            }
        } catch (final NumberFormatException e) {
            return false;
        }
        ((GoFrame) o).doboardsize(n);
        return true;
    }
}

/**
 * Ask the user for permission to close the board frame.
 */

class TextMarkQuestion extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GoFrame G;
    JTextField T;
    Checkbox C;

    public TextMarkQuestion(GoFrame g, String t) {
        super(g, Global.resourceString("Text_Mark"), false);
        this.G = g;
        this.setLayout(new BorderLayout());
        this.add("Center",
                new SimplePanel(new MyLabel(Global.resourceString("String")),
                        1, this.T = new TextFieldAction(this, t), 2));
        this.T.setText(t);
        final JPanel ps = new MyPanel();
        ps.add(this.C = new CheckboxAction(this, Global
                .resourceString("Auto_Advance")));
        this.C.setState(rene.gui.Global.getParameter("autoadvance", true));
        ps.add(new ButtonAction(this, Global.resourceString("Set")));
        ps.add(new ButtonAction(this, Global.resourceString("Close")));
        this.add("South", ps);
        Global.setpacked(this, "gettextmarkquestion", 300, 150);
        this.setVisible(true);
    }

    public void advance() {
        if (!this.C.getState()) {
            return;
        }
        final String s = this.T.getText();
        if (s.length() == 1) {
            char c = s.charAt(0);
            c++;
            this.T.setText("" + c);
            this.G.setTextmark(this.T.getText());
        } else {
            try {
                int n = Integer.parseInt(s);
                n = n + 1;
                this.T.setText("" + n);
                this.G.setTextmark(this.T.getText());
            } catch (final Exception e) {
            }
        }
    }

    @Override
    public boolean close() {
        this.G.TMQ = null;
        return true;
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "gettextmarkquestion");
        rene.gui.Global.setParameter("autoadvance", this.C.getState());
        if (o.equals(Global.resourceString("Set"))) {
            this.G.setTextmark(this.T.getText());
            rene.gui.Global.setParameter("textmark", this.T.getText());
        } else if (o.equals(Global.resourceString("Close"))) {
            this.close();
            this.setVisible(false);
            this.dispose();
        }
    }
}
