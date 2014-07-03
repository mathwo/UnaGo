package unagoclient;

import unagoclient.board.UnaGoGameFrame;
import rene.gui.MyPanel;
import rene.gui.Panel3D;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

/**
 * This applet starts a game viewer. It can either start a specific game or can
 * display a list of games. OtherUnaGoGame applets on the same page can use this
 * applet to start a viewer.
 * <p>
 * If the applet is to display a single game, it will show a button "Load" to
 * start the game display. The game is determined by the Game applet parameter,
 * which must contain a valid URL to a game.
 * <p>
 * If it is to display a list of games, it expects an URL to this list file as a
 * Games applet parameter. The list file consists pairs of lines with the game
 * name in the first line and the game URL in the next.
 */

public class UnaGoGame extends Applet implements ActionListener, Runnable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    UnaGoGameFrame GF;
    java.awt.List L = null;
    Button B;
    String Game, Games;
    Vector Urls;

    @Override
    public void actionPerformed(ActionEvent e) {
        this.GF = new UnaGoGameFrame(new Frame(),
                Global.resourceString("UnaGo_Viewer"));
        Global.setcomponent(this.GF);
        if (this.L != null) {
            if (this.L.getSelectedIndex() < 0) {
                return;
            }
            this.Game = (String) this.Urls.elementAt(this.L.getSelectedIndex());
        } else {
            this.Game = this.getParameter("Game");
        }
        new Thread(this).start();
    }

    /**
     * Initialize the applet depending on wether there is a "Game" or a "Games"
     * applet parameter.
     */
    @Override
    synchronized public void init() {
        this.Game = this.getParameter("Game");
        this.Games = this.getParameter("Games");
        this.setLayout(new BorderLayout());
        if (this.Games != null && !this.Games.equals("")) {
            this.L = new java.awt.List();
            this.add("Center", this.L);
            this.Urls = new Vector();
            try {
                BufferedReader in = null;
                if (this.Games.startsWith("http")) {
                    in = new BufferedReader(new InputStreamReader(
                            new DataInputStream(
                                    new URL(this.Games).openStream())));
                } else {
                    in = new BufferedReader(new InputStreamReader(
                            new DataInputStream(new URL(this.getDocumentBase(),
                                    this.Games).openStream())));
                }
                while (true) {
                    String name, value;
                    name = in.readLine();
                    if (name == null) {
                        break;
                    }
                    value = in.readLine();
                    if (value == null) {
                        break;
                    }
                    this.L.add(name);
                    this.Urls.addElement(value);
                }
            } catch (final Exception ex) {
            }
            final Panel p = new MyPanel();
            p.add(this.B = new Button(Global.resourceString("Load")));
            this.add("South", new Panel3D(p));
        } else {
            this.add("Center",
                    this.B = new Button(Global.resourceString("Load")));
        }
        this.B.addActionListener(this);
        Global.url(this.getCodeBase());
        Global.readparameter(".go.cfg");
        Global.createfonts();
    }

    public void load(String game) {
        this.Game = game;
        this.GF = new UnaGoGameFrame(new Frame(),
                Global.resourceString("UnaGo_Viewer"));
        new Thread(this).start();
    }

    /**
     * This is called from UnaGoGame Other
     *
     * @see UnaGoGameOther
     */
    @Override
    public void run() {
        this.GF.setVisible(false);
        try {
            if (this.Game != null) {
                if (this.Game.startsWith("http")) {
                    this.GF.load(new URL(this.Game));
                } else {
                    this.GF.load(new URL(this.getDocumentBase(), this.Game));
                }
            }
        } catch (final Exception ex) {
        }
        this.GF.activate();
        this.GF.setVisible(true);
    }
}
