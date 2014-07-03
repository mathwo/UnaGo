package unagoclient.dialogs;

import unagoclient.Global;
import unagoclient.gui.*;

import javax.swing.*;
import java.awt.*;

/**
 * A canvas to display a sample of the chosen font. The samples is drawn from
 * the GetFontSize dialog.
 */

class ExampleCanvas extends Canvas {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    GetFontSize GFS;

    public ExampleCanvas(GetFontSize gfs) {
        this.GFS = gfs;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 100);
    }

    @Override
    public void paint(Graphics g) {
        this.GFS.example(g, this.getSize().width, this.getSize().height);
    }
}

/**
 * A dialog to get the font size of the fixed font and its name. Both items are
 * stored as a global Parameter. The modal flag is handled as in the Question
 * dialg.
 *
 * @see Question
 */

public class GetFontSize extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String FontTag, SizeTag;
    JTextField FontName;
    IntField FontSize;
    Choice Fonts, Mode;
    Canvas Example;
    String E = Global.resourceString("10_good_letters__A_J_");

    /**
     * @param fonttag
     *            ,fontdef the font name resource tag and its default value
     * @param sizetag
     *            ,sizedef the font size resource tag and its default value
     */
    public GetFontSize(String fonttag, String fontdef, String sizetag,
            int sizedef, boolean flag) {
        super(Global.frame(), Global.resourceString("Font_Size"), flag);
        this.setLayout(new BorderLayout());
        final MyPanel p = new MyPanel();
        p.setLayout(new GridLayout(0, 2));
        p.add(new MyLabel(Global.resourceString("Font_name")));
        p.add(this.FontName = new TextFieldAction(this, "FontName"));
        this.FontName.setText(""
                + rene.gui.Global.getParameter(fonttag, fontdef));
        p.add(new MyLabel(Global.resourceString("Available_Fonts")));
        p.add(this.Fonts = new ChoiceAction(this, Global
                .resourceString("Fonts")));
        // String[] fonts = Toolkit.getDefaultToolkit().getFontList();
        final GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        final String fonts[] = ge.getAvailableFontFamilyNames();
        if (fonts != null) {
            for (int i = 0; i < fonts.length; i++) {
                this.Fonts.add(fonts[i]);
            }
        } else {
            this.Fonts.add("Dialog");
            this.Fonts.add("SansSerif");
            this.Fonts.add("Serif");
            this.Fonts.add("Monospaced");
            this.Fonts.add("DialogInput");
        }
        this.Fonts.add("Courier");
        this.Fonts.add("TimesRoman");
        this.Fonts.add("Helvetica");
        // Fonts.select(FontName.getText());
        final String name = rene.gui.Global.getParameter(fonttag, fontdef); // add
        this.Fonts.select(name); // add
        p.add(new MyLabel(Global.resourceString("Mode")));
        p.add(this.Mode = new ChoiceAction(this, Global.resourceString("Mode")));
        this.Mode.add(Global.resourceString("Plain"));
        this.Mode.add(Global.resourceString("Bold"));
        this.Mode.add(Global.resourceString("Italic"));
        // String name=FontName.getText();
        if (name.startsWith("Bold")) {
            this.FontName.setText(name.substring(4));
            this.Mode.select(1);
            this.Fonts.select(name.substring(4)); // add
        } else if (name.startsWith("Italic")) { // FontName.setText(name.substring(5));
            // Mode.select(2);
            this.FontName.setText(name.substring(6));
            this.Mode.select(2); // mod
            this.Fonts.select(name.substring(6)); // add
        } else {
            this.Mode.select(0);
        }
        p.add(new MyLabel(Global.resourceString("Font_size")));
        p.add(this.FontSize = new IntField(this, "FontSize", rene.gui.Global
                .getParameter(sizetag, sizedef)));
        this.add("North", p);
        this.Example = new ExampleCanvas(this);
        this.add("Center", this.Example);
        final MyPanel bp = new MyPanel();
        bp.add(new ButtonAction(this, Global.resourceString("OK")));
        this.add("South", bp);
        this.FontTag = fonttag;
        this.SizeTag = sizetag;
        Global.setpacked(this, "getfontsize", 200, 150);
        this.validate();
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "getfontsize");
        if (Global.resourceString("OK").equals(o)) {
            String s = this.FontName.getText();
            if (this.mode() == Font.BOLD) {
                s = "Bold" + s;
            } else if (this.mode() == Font.ITALIC) {
                s = "Italic" + s;
            }
            rene.gui.Global.setParameter(this.FontTag, s);
            rene.gui.Global.setParameter(this.SizeTag,
                    this.FontSize.value(3, 50));
            Global.createfonts();
            this.setVisible(false);
            this.dispose();
            this.tell();
        } else {
            super.doAction(o);
        }
        this.Example.repaint();
    }

    public void example(Graphics g, int w, int h) {
        final Font f = new Font(this.FontName.getText(), this.mode(),
                this.FontSize.value(3, 50));
        g.setFont(f);
        final FontMetrics fm = g.getFontMetrics();
        final int wi = fm.stringWidth(this.E);
        g.drawString(this.E, (w - wi) / 2, (h - fm.getAscent()) / 2 - 1);
    }

    @Override
    public void itemAction(String s, boolean flag) {
        this.FontName.setText(this.Fonts.getSelectedItem());
        this.Example.repaint();
    }

    int mode() {
        if (this.Mode.getSelectedItem().equals(Global.resourceString("Bold"))) {
            return Font.BOLD;
        } else if (this.Mode.getSelectedItem().equals(
                Global.resourceString("Italic"))) {
            return Font.ITALIC;
        } else {
            return Font.PLAIN;
        }
    }

    /**
     * to be overwritten in the non-modal case
     */
    public void tell() {
    }
}
