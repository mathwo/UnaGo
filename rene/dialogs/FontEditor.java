package rene.dialogs;

import rene.gui.*;

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
    FontEditor GFS;

    public ExampleCanvas(FontEditor gfs) {
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
 * stored as a Global Parameter.
 */

public class FontEditor extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String FontTag;
    TextField FontName;
    IntField FontSize, FontSpacing;
    Choice Fonts, Mode;
    Canvas Example;
    String E = Global.name("fonteditor.sample");
    Checkbox Smooth;

    /**
     * @param fonttag
     *            ,fontdef the font name resource tag and its default value
     * @param sizetag
     *            ,sizedef the font size resource tag and its default value
     */
    public FontEditor(Frame f, String fonttag, String fontdef, int sizedef) {
        super(f, Global.name("fonteditor.title"), true);
        this.FontTag = fonttag;
        this.setLayout(new BorderLayout());
        final Panel p = new MyPanel();
        p.setLayout(new GridLayout(0, 2));
        p.add(new MyLabel(Global.name("fonteditor.name")));
        p.add(this.FontName = new TextFieldAction(this, "FontName"));
        this.FontName.setText(Global.getParameter(fonttag + ".name", fontdef));
        p.add(new MyLabel(Global.name("fonteditor.available")));
        p.add(this.Fonts = new ChoiceAction(this, "Fonts"));
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
        this.Fonts.select(this.FontName.getText());
        p.add(new MyLabel(Global.name("fonteditor.mode")));
        p.add(this.Mode = new ChoiceAction(this, "Mode"));
        this.Mode.add(Global.name("fonteditor.plain"));
        this.Mode.add(Global.name("fonteditor.bold"));
        this.Mode.add(Global.name("fonteditor.italic"));
        final String name = Global.getParameter(fonttag + ".mode", "plain");
        if (name.startsWith("bold")) {
            this.Mode.select(1);
        } else if (name.startsWith("italic")) {
            this.Mode.select(2);
        } else {
            this.Mode.select(0);
        }
        p.add(new MyLabel(Global.name("fonteditor.size")));
        p.add(this.FontSize = new IntField(this, "FontSize", Global
                .getParameter(fonttag + ".size", sizedef)));
        p.add(new MyLabel(Global.name("fonteditor.spacing")));
        p.add(this.FontSpacing = new IntField(this, "FontSpacing", Global
                .getParameter(fonttag + ".spacing", 0)));
        p.add(new MyLabel(Global.name("fonteditor.antialias")));
        p.add(this.Smooth = new CheckboxAction(this, "", "Smooth"));
        this.Smooth.setState(Global.getParameter("font.smooth", true));
        this.add("North", new Panel3D(p));
        this.Example = new ExampleCanvas(this);
        this.add("Center", new Panel3D(this.Example));
        final Panel bp = new MyPanel();
        bp.add(new ButtonAction(this, Global.name("OK"), "OK"));
        bp.add(new ButtonAction(this, Global.name("close"), "Close"));
        this.add("South", new Panel3D(bp));
        this.pack();
    }

    @Override
    public void doAction(String o) {
        if ("OK".equals(o)) {
            Global.setParameter(this.FontTag + ".name", this.FontName.getText());
            String s = "plain";
            if (this.mode() == Font.BOLD) {
                s = "bold";
            } else if (this.mode() == Font.ITALIC) {
                s = "Italic";
            }
            Global.setParameter(this.FontTag + ".mode", s);
            Global.setParameter(this.FontTag + ".size",
                    this.FontSize.value(3, 50));
            Global.setParameter(this.FontTag + ".spacing",
                    this.FontSpacing.value(-10, 10));
            Global.setParameter("font.smooth", this.Smooth.getState());
            this.doclose();
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
        final int d = this.FontSpacing.value(-10, 10);
        for (int i = 1; i <= 4; i++) {
            g.drawString(
                    this.E,
                    5,
                    5 + d + i * d + fm.getLeading() + fm.getAscent() + i
                    * fm.getHeight());
        }
    }

    @Override
    public void itemAction(String s, boolean flag) {
        this.FontName.setText(this.Fonts.getSelectedItem());
        this.Example.repaint();
    }

    int mode() {
        if (this.Mode.getSelectedItem().equals(Global.name("fonteditor.bold"))) {
            return Font.BOLD;
        } else if (this.Mode.getSelectedItem().equals(
                Global.name("fonteditor.italic"))) {
            return Font.ITALIC;
        } else {
            return Font.PLAIN;
        }
    }
}
