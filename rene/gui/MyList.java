package rene.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MyList extends java.awt.List implements KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MyList(int n) {
        super(n);
        if (Global.NormalFont != null) {
            this.setFont(Global.NormalFont);
        }
        if (Global.Background != null) {
            this.setBackground(Global.Background);
        }
        this.addKeyListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            this.processActionEvent(new ActionEvent(this,
                    ActionEvent.ACTION_PERFORMED, ""));
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
