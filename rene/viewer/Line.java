package rene.viewer;

import java.awt.*;

public class Line {
    TextDisplay TD;
    boolean Chosen;
    int Pos, Posend;
    int Block; // block state
    static final int NONE = 0, START = 1, END = 2, FULL = 4; // block states
    // (logically
    // or'ed)
    Color C, IC; // Color of line
    // and high key
    // color of the
    // line
    char a[]; // Contains the

    // characters of
    // this line

    public Line(String s, TextDisplay td) {
        this(s, td, Color.black);
    }

    /**
     * Generate a line containing s in the textdisplay td. Display color is c.
     *
     * @param s
     * @param td
     * @param c
     */
    public Line(String s, TextDisplay td, Color c) {
        this.TD = td;
        this.C = c;
        // Create a color that is very bright, but resembles the line color
        this.IC = new Color(this.C.getRed() / 4 + 192,
                this.C.getGreen() / 4 + 192, this.C.getBlue() / 4 + 192);
        this.Block = Line.NONE;
        this.a = s.toCharArray();
    }

    void append(String s) {
        this.a = (new String(this.a) + s).toCharArray();
    }

    void block(int pos, int mode) {
        switch (mode) {
            case NONE:
                this.Block = Line.NONE;
                break;
            case FULL:
                this.Block = Line.FULL;
                break;
            case START:
                this.Block |= Line.START;
                this.Pos = pos;
                break;
            case END:
                this.Block |= Line.END;
                this.Posend = pos;
                break;
        }
    }

    public boolean chosen() {
        return this.Chosen;
    }

    void chosen(boolean f) {
        this.Chosen = f;
    }

    /**
     * Draw a line. If the line is in a block, draw it white on black or on dark
     * gray, depending on the focus. Drawing in blocks does not use antialias.
     *
     * @param g
     * @param x
     * @param y
     */
    public void draw(Graphics g, int x, int y) {
        x -= this.TD.Offset * this.TD.FM.charWidth(' ');
        if (this.Chosen) // Complete line is chosen (in Lister)
        { // To see, if the display has the focus:
            if (this.TD.hasFocus()) {
                g.setColor(Color.darkGray);
            } else {
                g.setColor(Color.gray);
            }
            g.fillRect(0, y - this.TD.Ascent, this.TD.getSize().width,
                    this.TD.Height);
            g.setColor(this.IC); // draw in light color
            this.TD.antialias(false);
            g.drawChars(this.a, 0, this.a.length, x, y);
            this.TD.antialias(true);
        } else if ((this.Block & Line.FULL) != 0) // Line in full block (in
            // Viewer)
        {
            g.setColor(Color.darkGray);
            g.fillRect(x, y - this.TD.Ascent,
                    this.TD.FM.charsWidth(this.a, 0, this.a.length),
                    this.TD.Height);
            g.setColor(Color.white);
            this.TD.antialias(false);
            g.drawChars(this.a, 0, this.a.length, x, y);
            this.TD.antialias(true);
        } else if ((this.Block & Line.START) != 0) {
            if (this.Pos > 0) // Draw text before block
            {
                g.setColor(this.C);
                g.drawChars(this.a, 0, this.Pos, x, y);
                x += this.TD.FM.charsWidth(this.a, 0, this.Pos);
            }
            if ((this.Block & Line.END) != 0) // draw text in block
            {
                if (this.Posend > this.Pos) {
                    final int h = this.TD.FM.charsWidth(this.a, this.Pos,
                            this.Posend - this.Pos);
                    g.setColor(Color.darkGray);
                    g.fillRect(x, y - this.TD.Ascent, h, this.TD.Height);
                    g.setColor(Color.white);
                    this.TD.antialias(false);
                    g.drawChars(this.a, this.Pos, this.Posend - this.Pos, x, y);
                    this.TD.antialias(true);
                    g.setColor(this.C);
                    x += h;
                    if (this.a.length > this.Posend) {
                        g.drawChars(this.a, this.Posend, this.a.length
                                - this.Posend, x, y);
                    }
                } else {
                    g.drawChars(this.a, this.Pos, this.a.length - this.Pos, x,
                            y);
                }
            } else // draw the rest of the line in block
            {
                final int h = this.TD.FM.charsWidth(this.a, this.Pos,
                        this.a.length - this.Pos);
                g.setColor(Color.darkGray);
                g.fillRect(x, y - this.TD.Ascent, h, this.TD.Height);
                g.setColor(Color.white);
                this.TD.antialias(false);
                g.drawChars(this.a, this.Pos, this.a.length - this.Pos, x, y);
                this.TD.antialias(true);
            }
        } else if ((this.Block & Line.END) != 0) {
            final int h = this.TD.FM.charsWidth(this.a, 0, this.Posend);
            g.setColor(Color.darkGray);
            g.fillRect(x, y - this.TD.Ascent, h, this.TD.Height);
            g.setColor(Color.white);
            this.TD.antialias(false);
            g.drawChars(this.a, 0, this.Posend, x, y);
            this.TD.antialias(true);
            g.setColor(this.C);
            x += h;
            if (this.a.length > this.Posend) {
                g.drawChars(this.a, this.Posend, this.a.length - this.Posend,
                        x, y);
            }
        } else {
            g.setColor(this.C);
            g.drawChars(this.a, 0, this.a.length, x, y);
        }
    }

    public void expandTabs(int tabwidth) {
        int pos = 0;
        for (int i = 0; i < this.a.length; i++) {
            pos++;
            if (this.a[i] == '\t') {
                pos = (pos / tabwidth + 1) * tabwidth;
            }
        }
        final char b[] = new char[pos];
        pos = 0;
        for (int i = 0; i < this.a.length; i++) {
            if (this.a[i] == '\t') {
                final int newpos = ((pos + 1) / tabwidth + 1) * tabwidth;
                for (int k = pos; k < newpos; k++) {
                    b[k] = ' ';
                }
                pos = newpos;
            } else {
                b[pos++] = this.a[i];
            }
        }
        this.a = b;
    }

    String getblock() {
        if (this.Block == Line.FULL) {
            return new String(this.a, 0, this.a.length);
        } else if ((this.Block & Line.START) != 0) {
            if ((this.Block & Line.END) != 0) {
                return new String(this.a, this.Pos, this.Posend - this.Pos);
            } else {
                return new String(this.a, this.Pos, this.a.length - this.Pos);
            }
        } else if ((this.Block & Line.END) != 0) {
            return new String(this.a, 0, this.Posend);
        } else {
            return "";
        }
    }

    int getpos(int x, int offset) {
        final int l[] = this.TD.getwidth(this.a);
        int h = offset - this.TD.Offset * this.TD.FM.charWidth(' ');
        if (x < h) {
            return 0;
        }
        int i = 0;
        while (x > h && i < this.a.length) {
            h += l[i];
            i++;
        }
        return i;
    }

    int length() {
        return this.a.length;
    }
}
