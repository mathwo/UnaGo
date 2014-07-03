package rene.dialogs;

import rene.gui.*;

import java.awt.*;
import java.awt.event.*;

/**
 * A dialog to edit a color. The result is stored in the Global parameters under
 * the specified name string.
 *
 * @see rene.gui.Global
 */
public class ColorEditor extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    static public Color[] getSomeColors() {
        return ColorPanel.getSomeColors();
    }

    /**
     * Main program for tests. Shows the dialog, when it is closed.
     *
     * @param args
     */
    public static void main(String args[]) {
        ColorEditor.f = new CloseFrame("Color Test") {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                final Dimension d = this.getSize();
                g.setColor(ColorEditor.cf);
                g.fillRect(0, 0, d.width, d.height);
            }
        };
        ColorEditor.f.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final ColorEditor ce = new ColorEditor(ColorEditor.f, "",
                        ColorEditor.cf, ColorEditor.FixedC, ColorEditor.UserC);
                ce.center(ColorEditor.f);
                ce.setVisible(true);
                ColorEditor.cf = ce.getColor();
                ColorEditor.f.repaint();
            }
        });
        ColorEditor.f.setSize(500, 500);
        ColorEditor.f.center();
        ColorEditor.f.setVisible(true);
    }

    ColorScrollbar Red, Green, Blue, Hue, Saturation, Brightness;
    Label RedLabel, GreenLabel, BlueLabel;
    ColorPanel CP;

    String Name;

    Color Cret;

    static CloseFrame f;

    static Color cf = Color.red;

    static Color FixedC[] = { Color.white, Color.black, Color.red, Color.blue,
            Color.green };

    static Color UserC[] = ColorPanel.getSomeColors();

    public ColorEditor(Frame F, String s, Color c) {
        this(F, s, c, null, null);
    }

    /**
     * Initialize the dialog. The color is read from the global settings of
     * parameter s (color name) with default c.
     *
     * @param F
     *            Calling Frame
     * @param s
     *            Color name in Global
     * @param c
     *            Default color
     */
    public ColorEditor(Frame F, String s, Color c, Color fixedc[],
            Color userc[]) {
        super(F, Global.name("coloreditor.title"), true);

        this.Name = s;
        Color C = Global.getParameter(s, c);
        if (C == null) {
            C = new Color(255, 255, 255);
        }

        this.CP = new ColorPanel(C, this, fixedc, userc);
        this.add("North", new Panel3D(this.CP));

        final Panel pc = new MyPanel();
        pc.setLayout(new GridLayout(0, 1));
        Panel p = new MyPanel();
        p.setLayout(new GridLayout(0, 1));
        p.add(this.Hue = new ColorScrollbar(this, Global
                .name("coloreditor.hue"), (int) (ColorPanel.getHue(C) * 360),
                360));
        p.add(this.Saturation = new ColorScrollbar(this, Global
                .name("coloreditor.saturation"), (int) (ColorPanel
                        .getSaturation(C) * 100), 100));
        p.add(this.Brightness = new ColorScrollbar(this, Global
                .name("coloreditor.brightness"), (int) (ColorPanel
                        .getBrightness(C) * 100), 100));
        pc.add(new Panel3D(p));
        p = new MyPanel();
        p.setLayout(new GridLayout(0, 1));
        p.add(this.Red = new ColorScrollbar(this, Global
                .name("coloreditor.red"), C.getRed(), 255));
        p.add(this.Green = new ColorScrollbar(this, Global
                .name("coloreditor.green"), C.getGreen(), 255));
        p.add(this.Blue = new ColorScrollbar(this, Global
                .name("coloreditor.blue"), C.getBlue(), 255));
        pc.add(new Panel3D(p));
        this.add("Center", new Panel3D(pc));

        final Panel pb = new MyPanel();
        pb.add(new ButtonAction(this, Global.name("OK"), "OK"));
        pb.add(new ButtonAction(this, Global.name("abort"), "Close"));
        this.add("South", new Panel3D(pb));

        this.pack();
    }

    /**
     * On OK, save the color to the global parameter s.
     */
    @Override
    public void doAction(String o) {
        if ("OK".equals(o)) {
            Global.setParameter(this.Name, this.CP.getColor());
            this.Aborted = false;
            this.doclose();
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        this.Cret = this.CP.getColor();
        super.doclose();
    }

    public Color getColor() {
        return this.Cret;
    }

    /**
     * Called from the color panel, if the user scrolled the hue.
     *
     * @param C
     */
    public void setcolor(Color C) {
        this.Red.set(C.getRed());
        this.Green.set(C.getGreen());
        this.Blue.set(C.getBlue());
        this.Hue.set((int) (ColorPanel.getHue(C) * 360));
        this.Brightness.set((int) (ColorPanel.getBrightness(C) * 100));
        this.Saturation.set((int) (ColorPanel.getSaturation(C) * 100));
    }

    /**
     * Called from the scroll bars.
     *
     * @param cs
     */
    public void setcolor(ColorScrollbar cs) {
        if (cs == this.Red || cs == this.Green || cs == this.Blue) {
            Color C = new Color(this.Red.value(), this.Green.value(),
                    this.Blue.value());
            this.CP.setNewColor(C);
            C = this.CP.getColor();
            this.Hue.set((int) (ColorPanel.getHue(C) * 360));
            this.Brightness.set((int) (ColorPanel.getBrightness(C) * 100));
            this.Saturation.set((int) (ColorPanel.getSaturation(C) * 100));
        } else {
            this.CP.setNewColor(this.Hue.value() / 180.0 * Math.PI,
                    this.Brightness.value() / 100.0,
                    this.Saturation.value() / 100.0);
            final Color C = this.CP.getColor();
            this.Red.set(C.getRed());
            this.Green.set(C.getGreen());
            this.Blue.set(C.getBlue());
        }
    }
}

/**
 * Displays the colors (color wheel, selected and old color)
 *
 * @author Rene Grothmann
 */
class ColorPanel extends MyPanel implements MouseListener, MouseMotionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Compute the brightness of a color.
     *
     * @param C
     * @return
     */
    public static double getBrightness(Color C) {
        final double r = C.getRed() / 255.0, g = C.getGreen() / 255.0, b = C
                .getBlue() / 255.0;
        return (Math.max(r, Math.max(g, b)) + Math.min(r, Math.min(g, b))) / 2;
    }

    /**
     * Compute a fully saturated color with the specified hue.
     *
     * @param a
     * @return
     */
    public static Color getColor(double a) {
        final double c = Math.cos(a), s = Math.sin(a);
        double x1 = ColorPanel.v1 * c + ColorPanel.w1 * s, x2 = ColorPanel.v2
                * c + ColorPanel.w2 * s, x3 = ColorPanel.v3 * c + ColorPanel.w3
                * s;
        final double max = Math.max(Math.max(x1, x2), x3);
        final double min = Math.min(Math.min(x1, x2), x3);
        x1 = -1 + 2 * (x1 - min) / (max - min);
        x2 = -1 + 2 * (x2 - min) / (max - min);
        x3 = -1 + 2 * (x3 - min) / (max - min);
        return new Color((float) (0.5 + x1 * 0.5), (float) (0.5 + x2 * 0.5),
                (float) (0.5 + x3 * 0.5));
    }

    /**
     * Compute the hue of a color.
     *
     * @param C
     * @return
     */
    public static double getHue(Color C) {
        final double r = C.getRed() / 255.0, g = C.getGreen() / 255.0, b = C
                .getBlue() / 255.0;
        final double m = (r + g + b) / 3;
        final double c = ((r - m) * ColorPanel.v1 + (g - m) * ColorPanel.v2 + (b - m)
                * ColorPanel.v3);
        final double s = ((r - m) * ColorPanel.w1 + (g - m) * ColorPanel.w2 + (b - m)
                * ColorPanel.w3);
        double a = Math.atan2(s, c) / (2 * Math.PI);
        if (a < 0) {
            a += 1;
        }
        if (a > 1) {
            a -= 1;
        }
        return a;
    }

    /**
     * Compute the saturation of a color.
     *
     * @param C
     * @return
     */
    public static double getSaturation(Color C) {
        final double r = C.getRed() / 255.0, g = C.getGreen() / 255.0, b = C
                .getBlue() / 255.0;
        final double m = (r + g + b) / 3;
        return Math.sqrt((r - m) * (r - m) + (g - m) * (g - m) + (b - m)
                * (b - m))
                / Math.sqrt(2.0 / 3.0);
    }

    /**
     * Return 32 nice user colors.
     *
     * @return
     */
    static public Color[] getSomeColors() {
        final Color c[] = new Color[32];
        for (int i = 0; i < 32; i++) {
            c[i] = ColorPanel.getColor(2 * Math.PI / 32 * i);
        }
        return c;
    }

    Color C, OldC;

    ;
    Color FixedC[], UserC[];

    int EditUser = -1;

    int W, H, D;

    static double v1, v2, v3, w1, w2, w3;

    static {
        double x = Math.sqrt(2);
        ColorPanel.v1 = -1 / x;
        ColorPanel.v2 = 1 / x;
        ColorPanel.v3 = 0;
        x = Math.sqrt(6);
        ColorPanel.w1 = 1 / x;
        ColorPanel.w2 = 1 / x;
        ColorPanel.w3 = -2 / x;
    }

    ColorEditor CE;

    Image I = null, IH = null;

    /**
     * Initialize with color editor and start color.
     *
     * @param c
     * @param ce
     */
    public ColorPanel(Color c, ColorEditor ce, Color fixedc[], Color userc[]) {
        this.C = this.OldC = c;
        this.CE = ce;
        this.FixedC = fixedc;
        this.UserC = userc;
        this.W = 400;
        this.D = 20;
        int k = 0;
        if (this.FixedC != null) {
            k += (this.FixedC.length - 1) / 16 + 1;
        }
        if (this.UserC != null) {
            k += (this.UserC.length - 1) / 16 + 1;
        }
        this.H = 3 * this.D + this.D / 2 + k * (this.D + this.D / 2);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    /**
     * Selected color.
     *
     * @return
     */
    public Color getColor() {
        return this.C;
    }

    @Override
    public Dimension getMinimumSize() {
        return this.getPreferredSize();
    }

    /**
     * Necessary to get the right size
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.W, this.H);
    }

    /**
     * Check if one of the user or fixed colors was clicked.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        final int x = e.getX(), y = e.getY();
        int yr = 3 * this.D + this.D / 2;
        if (this.FixedC != null) {
            int kc = 0;
            for (int i = 0; i < this.FixedC.length; i++) {
                Color col = this.FixedC[i];
                if (col == null) {
                    col = Color.gray;
                }
                if (y > yr && y < yr + this.D
                        && x > this.D / 2 + kc * (this.D + this.D / 2)
                        && x < this.D / 2 + kc * (this.D + this.D / 2) + this.D) {
                    this.setNewColor(col);
                    this.CE.doAction("OK");
                    this.EditUser = -1;
                    return;
                }
                kc++;
                if (kc == 16) {
                    kc = 0;
                    yr += this.D + this.D / 2;
                }
            }
            if (kc > 0) {
                yr += this.D + this.D / 2;
            }
        }
        if (this.UserC != null) {
            int kc = 0;
            for (int i = 0; i < this.UserC.length; i++) {
                Color col = this.UserC[i];
                if (col == null) {
                    col = Color.gray;
                }
                if (y > yr && y < yr + this.D
                        && x > this.D / 2 + kc * (this.D + this.D / 2)
                        && x < this.D / 2 + kc * (this.D + this.D / 2) + this.D) {
                    this.setNewColor(col);
                    if (this.EditUser == i) {
                        this.CE.doAction("OK");
                        this.EditUser = -1;
                    } else {
                        this.EditUser = i;
                        this.repaint();
                    }
                    return;
                }
                kc++;
                if (kc == 16) {
                    kc = 0;
                    yr += this.D + this.D / 2;
                }
            }
            if (kc > 0) {
                yr += this.D + this.D / 2;
            }
        }
        this.EditUser = -1;
        this.repaint();
        return;
    }

    /**
     * User dragged the slider in the color wheel.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        final double x = e.getX(), y = e.getY();
        if (y < 2 * this.D || y > 3 * this.D || x <= this.D / 2
                || x >= this.W - this.D / 2) {
            return;
        }
        this.C = ColorPanel.getColor(2 * Math.PI * (x - this.D / 2)
                / (this.W - this.D));
        this.CE.setcolor(this.C);
        if (this.EditUser >= 0) {
            this.UserC[this.EditUser] = this.C;
        }
        this.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    /**
     * Repaint the colors, and the images, if necessary.
     */
    @Override
    public void paint(Graphics g) { // Check, if image buffer is not up to date
        if (this.I == null || this.I.getWidth(this) != this.W
                || this.I.getHeight(this) != this.H) {
            this.I = this.createImage(this.W, this.H);
        }
        final Graphics ig = this.I.getGraphics();
        // clear rectangle
        ig.clearRect(0, 0, this.W, this.H);
        // draw old and new colors
        ig.setColor(this.OldC);
        ig.fillRect(this.D / 2, this.D / 2, this.W / 2 - this.D, this.D);
        ig.setColor(this.C);
        ig.fillRect(this.W / 2 + this.D / 2, this.D / 2, this.W / 2 - this.D,
                this.D);
        // check if color wheel is up to date
        if (this.IH == null || this.IH.getWidth(this) != this.W - this.D) {
            this.IH = this.createImage(this.W - this.D, 20);
            final Graphics igh = this.IH.getGraphics();
            igh.clearRect(0, 0, this.W - this.D, this.D);
            for (int i = 0; i < this.W - this.D; i++) {
                igh.setColor(ColorPanel.getColor(2 * Math.PI * (i)
                        / (this.W - this.D)));
                igh.drawLine(i, 0, i, this.D - 1);
            }
        }
        // draw the color wheel
        ig.drawImage(this.IH, this.D / 2, 2 * this.D, this);
        // draw the current hue
        final int k = this.D / 2
                + (int) (ColorPanel.getHue(this.C) * (this.W - this.D));
        ig.setColor(Color.black);
        ig.drawLine(k, 2 * this.D, k, 3 * this.D - 1);
        // draw the fixed colors
        int yr = 3 * this.D + this.D / 2;
        if (this.FixedC != null) {
            int kc = 0;
            for (int i = 0; i < this.FixedC.length; i++) {
                Color col = this.FixedC[i];
                if (col == null) {
                    col = Color.gray;
                }
                ig.setColor(this.getBackground());
                ig.fill3DRect(this.D / 2 + kc * (this.D + this.D / 2), yr,
                        this.D, this.D, true);
                ig.setColor(col);
                ig.fillRect(this.D / 2 + kc * (this.D + this.D / 2) + 4,
                        yr + 4, this.D - 8, this.D - 8);
                kc++;
                if (kc == 16) {
                    kc = 0;
                    yr += this.D + this.D / 2;
                }
            }
            if (kc > 0) {
                yr += this.D + this.D / 2;
            }
        }
        if (this.UserC != null) {
            int kc = 0;
            for (int i = 0; i < this.UserC.length; i++) {
                Color col = this.UserC[i];
                if (col == null) {
                    col = Color.gray;
                }
                if (this.EditUser == i) {
                    ig.setColor(this.getBackground());
                    ig.fill3DRect(this.D / 2 + kc * (this.D + this.D / 2), yr,
                            this.D, this.D, true);
                    ig.setColor(col);
                    ig.fillRect(this.D / 2 + kc * (this.D + this.D / 2) + 4,
                            yr + 4, this.D - 8, this.D - 8);
                } else {
                    ig.setColor(col);
                    ig.fillRect(this.D / 2 + kc * (this.D + this.D / 2), yr,
                            this.D, this.D);
                }
                kc++;
                if (kc == 16) {
                    kc = 0;
                    yr += this.D + this.D / 2;
                }
            }
        }
        // draw the image buffer
        g.drawImage(this.I, 0, 0, this);
    }

    /**
     * Set the color from external, e.g., from the color sliders.
     *
     * @param c
     */
    public void setNewColor(Color c) {
        this.C = c;
        this.repaint();
    }

    /**
     * Set the color via hue, brightness and saturation from external.
     *
     * @param a
     * @param br
     * @param sat
     */
    public void setNewColor(double a, double br, double sat) {
        final double c = Math.cos(a), s = Math.sin(a);
        double x1 = ColorPanel.v1 * c + ColorPanel.w1 * s, x2 = ColorPanel.v2
                * c + ColorPanel.w2 * s, x3 = ColorPanel.v3 * c + ColorPanel.w3
                * s;
        final double max = Math.max(Math.max(x1, x2), x3);
        final double min = Math.min(Math.min(x1, x2), x3);
        x1 = -1 + 2 * (x1 - min) / (max - min);
        x2 = -1 + 2 * (x2 - min) / (max - min);
        x3 = -1 + 2 * (x3 - min) / (max - min);
        final double f = sat * Math.min(1 - br, br);
        this.C = new Color((float) (br + x1 * f), (float) (br + x2 * f),
                (float) (br + x3 * f));
        if (this.EditUser > 0) {
            this.UserC[this.EditUser] = this.C;
        }
        this.repaint();
    }

    /**
     * Overrides update
     */
    @Override
    public void update(Graphics g) {
        this.paint(g);
    }
}

/**
 * A scroll bar together with a numeric input.
 *
 * @author Rene Grothmann
 */
class ColorScrollbar extends Panel implements AdjustmentListener,
DoActionListener, FocusListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int Value;
    ColorEditor CE;
    Scrollbar SB;
    IntField L;
    int Max, SL;

    /**
     * Initialize with color editor, and string for prompt. The maximal value
     * for colors is 255.
     *
     * @param ce
     * @param s
     * @param value
     * @param max
     */
    public ColorScrollbar(ColorEditor ce, String s, int value, int max) {
        this.CE = ce;
        this.setLayout(new GridLayout(1, 0));
        this.Value = value;
        this.Max = max;
        this.SL = max / 10;

        final Panel p = new MyPanel();
        p.setLayout(new GridLayout(1, 0));
        p.add(new MyLabel(s));
        p.add(this.L = new IntField(this, "L", this.Value, 4));
        this.add(p);
        this.add(this.SB = new Scrollbar(Scrollbar.HORIZONTAL, value, this.SL,
                0, this.Max + this.SL));
        this.SB.addAdjustmentListener(this);
        this.L.addFocusListener(this);
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        this.Value = this.SB.getValue();
        this.L.set(this.Value);
        this.SB.setValue(this.Value);
        this.CE.setcolor(this);
    }

    /**
     * Check for return in the text field.
     */
    @Override
    public void doAction(String o) {
        if ("L".equals(o)) {
            this.Value = this.L.value(0, this.Max);
            this.SB.setValue(this.Value);
            this.CE.setcolor(this);
        }
    }

    @Override
    public void focusGained(FocusEvent arg0) {
    }

    /**
     * if the text field lost focus, set the value.
     */
    @Override
    public void focusLost(FocusEvent arg0) {
        this.doAction("L");
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }

    public void set(int v) {
        this.L.set(v);
        this.SB.setValue(v);
    }

    public int value() {
        return this.L.value(0, this.Max);
    }
}
