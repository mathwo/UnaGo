package unagoclient.gui;

import java.awt.*;

public class BigLabel extends Panel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Image I = null;
    Graphics GI;
    FontMetrics FM;
    int Offset;
    int W, H;
    Font F;

    public BigLabel(Font f) {
        this.F = f;
        if (f != null) {
            this.setFont(f);
        }
        this.FM = this.getFontMetrics(f);
    }

    public void drawString(Graphics g, int x, int y, FontMetrics fm) {
    }

    @Override
    public Dimension getMinimumSize() {
        return this.getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.getSize().width,
                (this.FM.getAscent() + this.FM.getDescent()) * 3 / 2);
    }

    @Override
    public void paint(Graphics g) {
        final Dimension d = this.getSize();
        final int w = d.width, h = d.height;
        if (this.I == null || w != this.W || h != this.H) {
            this.W = w;
            this.H = h;
            this.I = this.createImage(this.W, this.H);
            if (this.I == null) {
                return;
            }
            this.GI = this.I.getGraphics();
            if (this.F != null) {
                this.GI.setFont(this.F);
            }
            this.FM = this.GI.getFontMetrics();
            this.Offset = this.FM.charWidth('m') / 2;
        }
        this.GI.setColor(rene.gui.Global.ControlBackground);
        this.GI.fillRect(0, 0, this.W, this.H);
        this.GI.setColor(Color.black);
        this.drawString(this.GI, this.Offset,
                (this.H + this.FM.getAscent() - this.FM.getDescent()) / 2,
                this.FM);
        g.drawImage(this.I, 0, 0, this.W, this.H, this);
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }
}
