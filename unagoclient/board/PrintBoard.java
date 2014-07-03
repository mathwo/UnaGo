package unagoclient.board;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.Properties;

/**
 * Prints the Board in a separate Thread. This class does gets a copy of the
 * board so that is can print asynchronously. However, it will display the print
 * dialog in its constructor (i.e. blocking the event handling), but do the
 * printing in in a separate thread.
 * <p>
 * As to Java 1.1 the printing is not very beautiful.
 */

class PrintBoard extends Thread implements ImageObserver {
    Graphics g;
    PrintJob job;
    int S, Range;
    Position P;
    Font font;
    FontMetrics fontmetrics;
    Frame F;

    public PrintBoard(Position P1, int Range1, Frame f) {
        this.F = f;
        final Toolkit toolkit = this.F.getToolkit();
        final Properties printrefs = new Properties();
        this.job = toolkit.getPrintJob(this.F, "UnaGo", printrefs);
        this.S = P1.getSize();
        this.P = P1;
        this.Range = Range1;
        if (this.job != null) {
            this.g = this.job.getGraphics();
            this.start();
        }
    }

    void hand1(Graphics g, int x, int y, int D) {
        int s = D / 10;
        if (s < 2) {
            s = 2;
        }
        g.fillRect(x + D / 2 - s, y + D / 2 - s, 2 * s + 1, 2 * s + 1);
    }

    @Override
    public boolean imageUpdate(Image i, int f, int x, int y, int w, int h) {
        if ((f & ImageObserver.ALLBITS) != 0) {
            this.notify();
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        final int W = this.job.getPageDimension().width;
        this.job.getPageDimension();
        int D = W * 2 / 3 / this.S;
        final int O = W / 6;
        if (D % 2 != 0) {
            D++;
        }
        this.font = new Font("SansSerif", Font.BOLD, D / 2);
        this.g.setFont(this.font);
        this.fontmetrics = this.g.getFontMetrics(this.font);
        this.g.setColor(Color.black);
        int i, j;
        // Draw lines
        int y = O;
        final int h = this.fontmetrics.getAscent() / 2 - 1;
        for (i = 0; i < this.S; i++) {
            final String s = "" + (this.S - i);
            final int w = this.fontmetrics.stringWidth(s) / 2;
            this.g.drawString(s, O + this.S * D + D / 2 - w, y + D / 2 + h);
            y += D;
        }
        int x = O;
        final char a[] = new char[1];
        for (i = 0; i < this.S; i++) {
            j = i;
            if (j > 7) {
                j++;
            }
            a[0] = (char) ('A' + j);
            final String s = new String(a);
            final int w = this.fontmetrics.stringWidth(s) / 2;
            this.g.drawString(s, x + D / 2 - w, O + this.S * D + D / 2 + h);
            x += D;
        }
        for (i = 0; i < this.S; i++) {
            for (j = 0; j < this.S; j++) {
                this.update1(this.g, O + D * i, O + D * j, i, j, D);
            }
        }
        this.g.dispose();
        this.job.end();
    }

    /**
     * update the field (i,j) in the print. in dependance of the board position
     * P. display the last move mark, if applicable.
     */
    public void update1(Graphics g, int x, int y, int i, int j, int D) {
        String hs;
        final char c[] = new char[1];
        g.setColor(Color.black);
        if (i > 0) {
            g.drawLine(x + 0, y + D / 2, x + D / 2, y + D / 2);
        }
        if (i < this.S - 1) {
            g.drawLine(x + D / 2, y + D / 2, x + D, y + D / 2);
        }
        if (j > 0) {
            g.drawLine(x + D / 2, y, x + D / 2, y + D / 2);
        }
        if (j < this.S - 1) {
            g.drawLine(x + D / 2, y + D / 2, x + D / 2, y + D);
        }
        int i1, j1;
        if (this.S == 19) // handicap markers
        {
            final int k = this.S / 2 - 3;
            for (i1 = 3; i1 < this.S; i1 += k) {
                for (j1 = 3; j1 < this.S; j1 += k) {
                    if (i == i1 && j == j1) {
                        this.hand1(g, x, y, D);
                    }
                }
            }
        } else if (this.S >= 11) // handicap markers
        {
            if (this.S >= 15 && this.S % 2 == 1) {
                final int k = this.S / 2 - 3;
                for (i1 = 3; i1 < this.S; i1 += k) {
                    for (j1 = 3; j1 < this.S; j1 += k) {
                        if (i == i1 && j == j1) {
                            this.hand1(g, x, y, D);
                        }
                    }
                }
            } else {
                if (i == 3 && j == 3) {
                    this.hand1(g, x, y, D);
                }
                if (i == this.S - 4 && j == 3) {
                    this.hand1(g, x, y, D);
                }
                if (i == 3 && j == this.S - 4) {
                    this.hand1(g, x, y, D);
                }
                if (i == this.S - 4 && j == this.S - 4) {
                    this.hand1(g, x, y, D);
                }
            }
        }
        if (this.P.getColor(i, j) > 0) {
            g.setColor(Color.black);
            g.fillOval(x + 1, y + 1, D - 3, D - 3);
        } else if (this.P.getColor(i, j) < 0) {
            g.setColor(Color.white);
            g.fillOval(x + 1, y + 1, D - 3, D - 3);
            g.setColor(Color.black);
            g.drawOval(x + 1, y + 1, D - 3, D - 3);
        }
        if (this.P.marker(i, j) != Field.NONE) {
            if (this.P.getColor(i, j) > 0) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.black);
            }
            final int h = D / 4;
            switch (this.P.marker(i, j)) {
                case Field.CIRCLE:
                    g.drawOval(x + D / 2 - h, y + D / 2 - h, 2 * h, 2 * h);
                    break;
                case Field.CROSS:
                    g.drawLine(x + D / 2 - h, y + D / 2 - h, x + D / 2 + h, y
                            + D / 2 + h);
                    g.drawLine(x + D / 2 + h, y + D / 2 - h, x + D / 2 - h, y
                            + D / 2 + h);
                    break;
                case Field.TRIANGLE:
                    g.drawLine(x + D / 2, y + D / 2 - h, x + D / 2 - h, y + D
                            / 2 + h);
                    g.drawLine(x + D / 2, y + D / 2 - h, x + D / 2 + h, y + D
                            / 2 + h);
                    g.drawLine(x + D / 2 - h, y + D / 2 + h, x + D / 2 + h, y
                            + D / 2 + h);
                    break;
                default:
                    g.drawRect(x + D / 2 - h, y + D / 2 - h, 2 * h, 2 * h);
            }
        }
        if (this.P.letter(i, j) != 0) {
            if (this.P.getColor(i, j) > 0) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.black);
            }
            c[0] = (char) ('a' + this.P.letter(i, j) - 1);
            hs = new String(c);
            final int w = this.fontmetrics.stringWidth(hs) / 2;
            // int h=fontMetrics.getAscent()/2-1;
            final int h = D / 4;
            g.drawString(hs, x + D / 2 - w, y + D / 2 + h);
        }
        if (this.P.haslabel(i, j)) {
            if (this.P.getColor(i, j) > 0) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.black);
            }
            hs = this.P.label(i, j);
            final int w = this.fontmetrics.stringWidth(hs) / 2;
            // int h=fontMetrics.getAscent()/2-1;
            final int h = D / 4;
            g.drawString(hs, x + D / 2 - w, y + D / 2 + h);
        }
        if (this.P.getColor(i, j) != 0 && this.Range >= 0
                && this.P.number(i, j) > this.Range) {
            if (this.P.getColor(i, j) > 0) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.black);
            }
            hs = "" + (this.P.number(i, j) % 100);
            final int w = this.fontmetrics.stringWidth(hs) / 2;
            // int h=fontMetrics.getAscent()/2-1;
            final int h = D / 4;
            g.drawString(hs, x + D / 2 - w, y + D / 2 + h);
        }
    }

}
