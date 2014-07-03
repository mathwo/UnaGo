package unagoclient.board;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;
import java.awt.*;

/**
 * A GoFrame for local boards in Applets.
 */

public class UnaGoGameFrame extends GoFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UnaGoGameFrame(Frame f, String s)
    // Constructur for local board menus.
    {
        super(s);
        // Colors
        this.setcolors();
        this.seticon("iboard.gif");
        this.setLayout(new BorderLayout());
        // Menu
        final MenuBar M = new MenuBar();
        this.setMenuBar(M);
        final Menu file = new MyMenu(Global.resourceString("File"));
        file.add(new MenuItemAction(this, Global
                .resourceString("Prisoner_Count")));
        file.addSeparator();
        file.add(this.ShowButtons = new CheckboxMenuItemAction(this, Global
                .resourceString("Show_Buttons")));
        this.ShowButtons.setState(rene.gui.Global.getParameter("showbuttons",
                true));
        file.addSeparator();
        file.add(new MenuItemAction(this, Global.resourceString("Close")));
        M.add(file);
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
        M.add(options);
        // Board
        this.Comment = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        this.Comment.setFont(Global.SansSerif);
        this.L = new OutputLabel(Global.resourceString("New_Game"));
        this.Lm = new OutputLabel("--");
        this.B = new Board(19, this);
        final JPanel BP = new MyPanel();
        BP.setLayout(new BorderLayout());
        BP.add("Center", this.B);
        // Add the label
        final SimplePanel sp = new SimplePanel(this.L, 80, this.Lm, 20);
        BP.add("South", sp);
        // Text Area
        final JPanel bcp = new BoardCommentPanel(new Panel3D(BP), new Panel3D(
                this.Comment), this.B);
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
        this.setVisible(true);
        this.repaint();
    }

}
