package unagoclient.gui;

import unagoclient.Global;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A lightweight button for the navigation buttons (reacts quicker).
 */

public class NavigationButtonAction extends Panel implements MouseListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    DoActionListener C;
    String Name;
    String S;
    FontMetrics FM;
    int W, H;
    boolean Pressed = false;
    boolean Focus = false;
    boolean Enabled = true;
    Image I = null;
    Graphics IG;

    public NavigationButtonAction(DoActionListener c, String s) {
        this(c, s, s);
    }

    public NavigationButtonAction(DoActionListener c, String s, String name) {
        this.C = c;
        this.Name = name;
        this.S = s;
        this.addMouseListener(this);
        this.setFont(Global.SansSerif);
        this.FM = this.getFontMetrics(Global.SansSerif);
        final int sw1 = this.FM.stringWidth("<<<"), sw2 = this.FM
                .stringWidth(this.S);
        if (sw2 > sw1) {
            this.W = sw2 * 5 / 4 + 2;
        } else {
            this.W = sw1 * 5 / 4 + 2;
        }
        this.H = this.FM.getHeight() * 5 / 4 + 2;
        this.repaint();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(this.W, this.H);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.W, this.H);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.Pressed = true;
        this.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.Pressed = false;
        this.repaint();
        if (e.getX() >= 0 && e.getX() < this.W && e.getY() >= 0
                && e.getY() < this.H) {
            this.C.doAction(this.Name);
        }
    }

    @Override
    public void paint(Graphics g) {
        if (this.I == null) {
            this.I = this.createImage(this.W, this.H);
            this.IG = this.I.getGraphics();
            this.IG.setFont(this.getFont());
        }
        final int w = this.FM.stringWidth(this.S);
        this.IG.fill3DRect(0, 0, this.W, this.H, !this.Pressed);
        if (this.Enabled) {
            this.IG.setColor(Color.black);
        } else {
            this.IG.setColor(Color.gray);
        }
        this.IG.drawString(this.S, (this.W - w) / 2, this.H
                - ((this.H - this.FM.getHeight()) / 2 + this.FM.getDescent()));
        g.drawImage(this.I, 0, 0, this.W, this.H, this);
    }

    @Override
    public void setEnabled(boolean flag) {
        if (this.Enabled == flag) {
            return;
        }
        this.Enabled = flag;
        this.repaint();
        super.setEnabled(flag);
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }
}
