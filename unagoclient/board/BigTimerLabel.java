package unagoclient.board;

import unagoclient.Global;
import unagoclient.gui.BigLabel;

import java.awt.*;

public class BigTimerLabel extends BigLabel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    int White = 0, Black = 0, Col = 0, MWhite = -1, MBlack = -1;

    static char a[] = new char[32];

    public BigTimerLabel() {
        super(Global.BigMonospaced);
    }

    @Override
    public void drawString(Graphics g0, int x, int y, FontMetrics fm) {
        final int delta = fm.charWidth('m') / 4;

        final Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        if (Global.BigMonospaced != null) {
            g.setFont(Global.BigMonospaced);
        }
        if (this.White < 0) {
            g.setColor(Color.blue);
        } else if (this.White < 30 && this.Col < 0) {
            g.setColor(Color.red);
        } else if (this.White < 60 && this.Col < 0) {
            g.setColor(Color.red.darker());
        } else if (this.Col < 0) {
            g.setColor(Color.green.darker());
        } else {
            g.setColor(Color.black);
        }
        int n = OutputFormatter.formtime(BigTimerLabel.a, this.White);
        g.drawChars(BigTimerLabel.a, 0, n, x, y);
        x += fm.charsWidth(BigTimerLabel.a, 0, n) + delta;
        g.setFont(Global.Monospaced);
        if (this.MWhite >= 0) {
            BigTimerLabel.a[0] = (char) ('0' + this.MWhite % 100 / 10);
            BigTimerLabel.a[1] = (char) ('0' + this.MWhite % 10);
        } else {
            BigTimerLabel.a[0] = BigTimerLabel.a[1] = ' ';
        }
        g.setColor(Color.black);
        g.drawChars(BigTimerLabel.a, 0, 2, x, y);
        x += fm.charsWidth(BigTimerLabel.a, 0, 2) + delta;
        if (Global.BigMonospaced != null) {
            g.setFont(Global.BigMonospaced);
        }
        if (this.Black < 0) {
            g.setColor(Color.blue);
        } else if (this.Black < 30 && this.Col > 0) {
            g.setColor(Color.red);
        } else if (this.Black < 60 && this.Col > 0) {
            g.setColor(Color.red.darker());
        } else if (this.Col > 0) {
            g.setColor(Color.green.darker());
        } else {
            g.setColor(Color.black);
        }
        n = OutputFormatter.formtime(BigTimerLabel.a, this.Black);
        g.drawChars(BigTimerLabel.a, 0, n, x, y);
        x += fm.charsWidth(BigTimerLabel.a, 0, n) + delta;
        g.setFont(Global.Monospaced);
        if (this.MBlack >= 0) {
            BigTimerLabel.a[0] = (char) ('0' + this.MBlack % 100 / 10);
            BigTimerLabel.a[1] = (char) ('0' + this.MBlack % 10);
        } else {
            BigTimerLabel.a[0] = BigTimerLabel.a[1] = ' ';
        }
        g.setColor(Color.black);
        g.drawChars(BigTimerLabel.a, 0, 2, x, y);
        if (Global.BigMonospaced != null) {
            g.setFont(Global.BigMonospaced);
        }
    }

    public void setTime(int w, int b, int mw, int mb, int col) {
        this.White = w;
        this.Black = b;
        this.MWhite = mw;
        this.MBlack = mb;
        this.Col = col;
    }
}
