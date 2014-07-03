package unagoclient.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A simplified card panel. The panel has a south component, which displays
 * buttons, which switch the center component.
 */

public class CardPanel extends Panel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    MyPanel P, Bp;
    CardLayout CL;

    public CardPanel() {
        this.setLayout(new BorderLayout());
        this.P = new MyPanel();
        this.P.setLayout(this.CL = new CardLayout());
        this.add("Center", new Panel3D(this.P));
        this.Bp = new MyPanel();
        this.add("South", new Panel3D(this.Bp));
    }

    /**
     * Adds a component to the card panel. The name is used to create a button
     * with this label.
     */
    public void add(Component c, String name) {
        this.P.add(name, c);
        this.Bp.add(new CardPanelButton(name, this.CL, name, this.P));
    }
}

class CardPanelButton extends JButton implements ActionListener, KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String Name;
    JPanel P;
    CardLayout CL;

    public CardPanelButton(String text, CardLayout cl, String name, JPanel p) {
        super(text);
        this.Name = name;
        this.P = p;
        this.CL = cl;
        this.addActionListener(this);
        this.addKeyListener(this);
        // setFont(Global.SansSerif);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.CL.show(this.P, this.Name);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            this.CL.show(this.P, this.Name);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
