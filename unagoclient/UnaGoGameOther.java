package unagoclient;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

/**
 * This applet (containing a button only) is used to start the display of a
 * game, when it is pressed. The display is done via a UnaGoGame applet, which
 * must be on the same page. The game to be displayed is chosen via a Game
 * applet parameter.
 *
 * @see UnaGoGame
 */

public class UnaGoGameOther extends Applet implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Button B;
    String Game;

    @Override
    public void actionPerformed(ActionEvent e) {
        final String Game = this.getParameter("Game");
        final Enumeration n = this.getAppletContext().getApplets();
        try {
            while (n.hasMoreElements()) {
                final Object a = n.nextElement();
                if (a instanceof UnaGoGame) {
                    final UnaGoGame j = (UnaGoGame) a;
                    j.load(Game);
                    break;
                }
            }
        } catch (final Exception ex) {
        }
    }

    @Override
    synchronized public void init() {
        this.setLayout(new BorderLayout());
        this.add("Center", this.B = new Button(Global.resourceString("Load")));
        this.B.addActionListener(this);
    }
}