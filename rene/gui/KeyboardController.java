package rene.gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardController implements KeyListener {
    /**
     * Macro and programs key pressed
     */
    boolean Escape = false;
    /**
     * Note, if the next entry should be ignored or not
     */
    boolean IgnoreTyped = false;
    /**
     * The type of the recent command key (1..5)
     */
    int CommandType = 0;
    /**
     * The component, which we are listening to.
     */
    Component C = null;
    /**
     * The primary and secondary KeyboardInterface
     */
    KeyboardInterface Primary = null, Secondary = null;

    boolean scaled = false; // scaled already

    long scale; // the scaling in

    // milliseconds

    public void keyboardChar(KeyEvent e, char c) { // System.out.println(""+c);
        if (this.Primary == null || !this.Primary.keyboardChar(e, c)) {
            if (this.Secondary != null) {
                this.Secondary.keyboardChar(e, c);
            }
        }
    }

    public void keyboardCommand(KeyEvent e, String command) { // System.out.println(command);
        if (this.Primary == null || !this.Primary.keyboardCommand(e, command)) {
            if (this.Secondary != null) {
                this.Secondary.keyboardCommand(e, command);
            }
        }
    }

    public void keyboardEscape(KeyEvent e, char c) { // System.out.println("escape "+c);
        if (this.Primary == null || !this.Primary.keyboardEscape(e, c)) {
            if (this.Secondary != null) {
                this.Secondary.keyboardEscape(e, c);
            }
        }
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            return;
        }
        if (this.old(e)) {
            return;
        }
        final String s = Keyboard.findKey(e, this.CommandType);
        // System.out.println(Escape+" "+CommandType+" "+s);
        this.IgnoreTyped = false;
        if (s.startsWith("command.")) {
            if (s.equals("command.escape")) {
                this.Escape = !this.Escape;
            } else {
                try {
                    this.CommandType = Integer.parseInt(s.substring(8));
                    this.Escape = false;
                } catch (final Exception ex) {
                    this.CommandType = 0;
                }
            }
            this.IgnoreTyped = true;
        } else if (s.startsWith("charkey.")) {
            this.keyboardCommand(e, s);
            this.IgnoreTyped = true;
            this.Escape = false;
            this.CommandType = 0;
        } else if (this.Escape) {
            final char c = e.getKeyChar();
            this.IgnoreTyped = true;
            this.keyboardEscape(e, c);
            this.Escape = false;
        } else if (!s.equals("")) {
            this.keyboardCommand(e, s);
            this.IgnoreTyped = false;
            this.Escape = false;
            this.CommandType = 0;
        } else if (!e.isActionKey()) {
            if (!Global.getParameter("keyboard.compose", true)) {
                this.keyboardChar(e, e.getKeyChar());
                this.Escape = false;
                this.CommandType = 0;
            } else {
                this.Escape = false;
                this.CommandType = 0;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (!Global.getParameter("keyboard.compose", true)) {
            return;
        }
        // System.out.println("key typed "+IgnoreTyped+" "+e);
        if (this.IgnoreTyped) {
            return;
        }
        this.IgnoreTyped = false;
        this.keyboardChar(e, e.getKeyChar());
        this.Escape = false;
        this.CommandType = 0;
    }

    public void listenTo(Component c) {
        if (this.C != null) {
            this.C.removeKeyListener(this);
        }
        this.C = c;
        if (this.C != null) {
            this.C.addKeyListener(this);
        }
    }

    /**
     * Test for old keys. This algorithm uses the difference between event time
     * and system time. However, one needs to scale this first, since in Linux
     * both timers do not agree.
     */
    boolean old(KeyEvent e) {
        if (!this.scaled) {
            this.scaled = true;
            this.scale = System.currentTimeMillis() - e.getWhen();
            return false;
        }
        final long delay = System.currentTimeMillis() - e.getWhen()
                - this.scale;
        if (delay > 10000) {
            return false; // function does not work!
        }
        return (delay > 200);
    }

    public void setPrimary(KeyboardInterface i) {
        this.Primary = i;
    }

    public void setSecondary(KeyboardInterface i) {
        this.Secondary = i;
    }
}
