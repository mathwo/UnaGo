package unagoclient.board;

import unagoclient.Global;
import unagoclient.gui.*;
import rene.gui.IconBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

/**
 * A subclass of GoFrame, which has a different menu layout. Moreover, it
 * contains a method to add a comment from an external source (a distributor).
 */

public class ConnectedGoFrame extends GoFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected boolean TimerInTitle, ExtraSendField, BigTimer;
    protected HistoryTextField SendField;
    protected OutputLabel TL;
    protected BigTimerLabel BL;
    protected Menu FileMenu, Options;
    protected JPanel CommentPanel;
    protected TextArea AllComments;
    CardLayout CommentPanelLayout;
    protected CheckboxMenuItem KibitzWindow, Teaching, Silent;

    /**
     * Will modify the menues of the GoFrame. "endgame" is used for for the menu
     * entry to end a game (e.g. "remove groups"). "count" is the string to
     * count a game (e.g. "send done").
     * <p>
     * Optionally, the comment uses a card layout and a second text area to hold
     * the Kibitz window.
     */
    public ConnectedGoFrame(String s, int si, String endgame, String count,
            boolean kibitzwindow, boolean canteach) {
        super(s);
        this.setLayout(new BorderLayout());
        this.TimerInTitle = rene.gui.Global.getParameter("timerintitle", true);
        this.ExtraSendField = rene.gui.Global.getParameter("extrasendfield",
                true);
        this.BigTimer = rene.gui.Global.getParameter("bigtimer", true);
        // Menu
        final MenuBar M = new MenuBar();
        this.setMenuBar(M);
        final Menu file = new MyMenu(Global.resourceString("File"));
        this.FileMenu = file;
        M.add(file);
        file.add(new MenuItemAction(this, Global.resourceString("Save")));
        file.addSeparator();
        file.add(this.UseXML = new CheckboxMenuItemAction(this, Global
                .resourceString("Use_XML")));
        this.UseXML.setState(rene.gui.Global.getParameter("xml", false));
        file.add(this.UseSGF = new CheckboxMenuItemAction(this, Global
                .resourceString("Use_SGF")));
        this.UseSGF.setState(!rene.gui.Global.getParameter("xml", false));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global
                .resourceString("Copy_to_Clipboard")));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global.resourceString("Mail")));
        file.add(new MenuItemAction(this, Global.resourceString("Ascii_Mail")));
        file.add(new MenuItemAction(this, Global.resourceString("Print")));
        file.add(new MenuItemAction(this, Global.resourceString("Save_Bitmap")));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global.resourceString("Refresh")));
        file.addSeparator();
        file.add(this.ShowButtons = new CheckboxMenuItemAction(this, Global
                .resourceString("Show_Buttons")));
        this.ShowButtons.setState(rene.gui.Global.getParameter(
                "showbuttonsconnected", true));
        /*
         * if (canteach) { file.addSeparator(); Menu teaching=new
         * MyMenu(Global.resourceString("Teaching")); teaching.add(Teaching=new
         * CheckboxMenuItemAction(this,Global.resourceString("Teaching_On")));
         * Teaching.setState(false); teaching.add(new
         * MenuItemAction(this,Global.resourceString("Load_Teaching_Game")));
         * file.add(teaching); }
         */
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
        final Menu var = new MyMenu(Global.resourceString("Variations"));
        var.add(new MenuItemAction(this, Global.resourceString("Insert_Node")));
        var.add(new MenuItemAction(this, Global
                .resourceString("Insert_Variation")));
        var.addSeparator();
        var.add(new MenuItemAction(this, Global.resourceString("Next_Game")));
        var.add(new MenuItemAction(this, Global.resourceString("Previous_Game")));
        var.addSeparator();
        var.add(new MenuItemAction(this, Global
                .resourceString("Set_Search_String")));
        var.add(new MenuItemAction(this, Global
                .resourceString("Search_Comment")));
        var.addSeparator();
        var.add(new MenuItemAction(this, Global.resourceString("Node_Name")));
        M.add(var);
        final Menu score = new MyMenu(Global.resourceString("Finish_Game"));
        M.add(score);
        if (!endgame.equals("")) {
            score.add(new MenuItemAction(this, endgame));
        }
        score.add(new MenuItemAction(this, Global.resourceString("Local_Count")));
        score.addSeparator();
        score.add(new MenuItemAction(this, Global
                .resourceString("Prisoner_Count")));
        if (!count.equals("")) {
            score.add(new MenuItemAction(this, count));
        }
        score.addSeparator();
        score.add(new MenuItemAction(this, Global
                .resourceString("Game_Information")));
        score.add(new MenuItemAction(this, Global
                .resourceString("Game_Copyright")));
        final Menu options = new MyMenu(Global.resourceString("Options"));
        this.Options = options;
        options.add(this.Silent = new CheckboxMenuItemAction(this, Global
                .resourceString("Silent")));
        this.Silent
        .setState(rene.gui.Global.getParameter("boardsilent", false));
        if (this.Silent.getState()) {
            Global.Silent++;
        }
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
                "lowerrightcoordinates", false));
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
        fonts.add(new MenuItemAction(this, Global.resourceString("Fixed_Font")));
        fonts.add(new MenuItemAction(this, Global.resourceString("Board_Font")));
        fonts.add(new MenuItemAction(this, Global.resourceString("Normal_Font")));
        options.add(fonts);
        final Menu variations = new MyMenu(
                Global.resourceString("Variation_Display"));
        variations.add(this.VHide = new CheckboxMenuItemAction(this, Global
                .resourceString("Hide")));
        this.VHide.setState(rene.gui.Global.getParameter("vhide", false));
        variations.add(this.VCurrent = new CheckboxMenuItemAction(this, Global
                .resourceString("To_Current")));
        this.VCurrent.setState(rene.gui.Global.getParameter("vcurrent", true));
        variations.add(this.VChild = new CheckboxMenuItemAction(this, Global
                .resourceString("To_Child")));
        this.VChild.setState(!rene.gui.Global.getParameter("vcurrent", true));
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
        if (kibitzwindow) {
            options.addSeparator();
            options.add(this.KibitzWindow = new CheckboxMenuItemAction(this,
                    Global.resourceString("Kibitz_Window")));
            this.KibitzWindow.setState(rene.gui.Global.getParameter(
                    "kibitzwindow", true));
            options.add(new MenuItemAction(this, Global
                    .resourceString("Clear_Kibitz_Window")));
        }
        M.add(options);
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
        M.setHelpMenu(help);
        // Board
        this.L = new OutputLabel(Global.resourceString("New_Game"));
        this.Lm = new OutputLabel("--");
        this.Comment = new MyTextArea("", 0, 0,
                TextArea.SCROLLBARS_VERTICAL_ONLY);
        this.Comment.setFont(Global.SansSerif);
        this.CommentPanel = new MyPanel();
        if (kibitzwindow) {
            this.AllComments = new MyTextArea("", 0, 0,
                    TextArea.SCROLLBARS_VERTICAL_ONLY);
            this.AllComments.setEditable(false);
            this.CommentPanel
            .setLayout(this.CommentPanelLayout = new CardLayout());
            this.CommentPanel.add("Comment", this.Comment);
            this.CommentPanel.add("AllComments", this.AllComments);
        } else {
            this.CommentPanel.setLayout(new BorderLayout());
            this.CommentPanel.add("Center", this.Comment);
        }
        this.B = new ConnectedBoard(si, this);
        final JPanel BP = new MyPanel();
        BP.setLayout(new BorderLayout());
        BP.add("Center", this.B);
        // Add the label
        final JPanel bpp = new MyPanel();
        bpp.setLayout(new GridLayout(0, 1));
        final SimplePanel sp = new SimplePanel(this.L, 80, this.Lm, 20);
        bpp.add(sp);
        // Add the timer label
        if (!this.TimerInTitle) {
            final SimplePanel btl = new SimplePanel(this.TL = new OutputLabel(
                    Global.resourceString("Start")), 80, new OutputLabel(""),
                    20);
            bpp.add(btl);
        }
        BP.add("South", bpp);
        // Text Area
        final JPanel cp = new MyPanel();
        cp.setLayout(new BorderLayout());
        cp.add("Center", this.CommentPanel);
        if (kibitzwindow && this.KibitzWindow.getState()) {
            this.CommentPanelLayout.show(this.CommentPanel, "AllComments");
        }
        // Add the extra send field
        if (this.ExtraSendField) {
            this.SendField = new HistoryTextField(this, "SendField");
            cp.add("South", this.SendField);
        }
        final JPanel bcp = new BoardCommentPanel(new Panel3D(BP), new Panel3D(
                cp), this.B);
        this.add("Center", bcp);
        // Add the big timer label
        if (this.BigTimer) {
            this.BL = new BigTimerLabel();
            cp.add("North", this.BL);
        }
        // Navigation panel
        this.IB = this.createIconBar();
        this.ButtonP = new Panel3D(this.IB);
        if (rene.gui.Global.getParameter("showbuttonsconnected", true)) {
            this.add("South", this.ButtonP);
        }
        // Directory for FileDialog
        this.Dir = new String("");
        Global.setwindow(this, "board", 500, 450, false);
        this.validate();
        this.Show = true;
        this.B.addKeyListener(this);
    }

    @Override
    public void activate() {
    }

    @Override
    public void addComment(String s)
    // add a comment to the board (called from external sources)
    {
        this.B.addcomment(s);
        if (this.AllComments != null) {
            this.AllComments.append(s + "\n");
        }
    }

    public void addSendForward(IconBar I) {
    }

    public void addtoallcomments(String s)
    // add something to the allcomments window
    {
        if (this.AllComments != null) {
            this.AllComments.append(s + "\n");
        }
    }

    @Override
    public IconBar createIconBar() {
        final IconBar I = new IconBar(this);
        I.Resource = "/unagoclient/icons/";
        I.addLeft("undo");
        this.addSendForward(I);
        I.addSeparatorLeft();
        I.addLeft("allback");
        I.addLeft("fastback");
        I.addLeft("back");
        I.addLeft("forward");
        I.addLeft("fastforward");
        I.addLeft("allforward");
        I.addSeparatorLeft();
        I.addLeft("variationback");
        I.addLeft("variationstart");
        I.addLeft("variationforward");
        I.addLeft("main");
        I.addLeft("mainend");
        I.addSeparatorLeft();
        I.addLeft("send");
        I.setIconBarListener(this);
        return I;
    }

    @Override
    public void doAction(String o) {
        if (o.equals(Global.resourceString("Clear_Kibitz_Window"))) {
            this.AllComments.setText("");
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        Global.Silent--;
        super.doclose();
    }

    @Override
    public void iconPressed(String s) {
        if (s.equals("send")) {
            this.doAction(Global.resourceString("Send"));
        } else {
            super.iconPressed(s);
        }
    }

    @Override
    public void itemAction(String o, boolean flag) {
        if (Global.resourceString("Kibitz_Window").equals(o)) {
            if (this.KibitzWindow.getState()) {
                this.CommentPanelLayout.show(this.CommentPanel, "AllComments");
            } else {
                this.CommentPanelLayout.show(this.CommentPanel, "Comment");
            }
            rene.gui.Global.setParameter("kibitzwindow",
                    this.KibitzWindow.getState());
        } else if (Global.resourceString("Silent").equals(o)) {
            if (flag) {
                Global.Silent++;
            } else {
                Global.Silent--;
            }
            rene.gui.Global.setParameter("boardsilent", flag);
        } else {
            super.itemAction(o, flag);
        }
    }

    public boolean wantsmove() {
        return false;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        if (this.ExtraSendField) {
            this.SendField.requestFocus();
        }
    }
}
