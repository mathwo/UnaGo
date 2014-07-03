package unagoclient.igs.games;

import unagoclient.Global;
import unagoclient.dialogs.Help;
import unagoclient.gui.*;
import unagoclient.igs.ConnectionFrame;
import unagoclient.igs.IgsStream;
import rene.util.list.ListClass;
import rene.util.list.ListElement;
import rene.util.parser.StringParser;
import rene.util.sort.Sorter;
import rene.viewer.Lister;
import rene.viewer.SystemLister;

import java.awt.*;
import java.io.PrintWriter;

/**
 * This frame displays the games on the server. It is opened by a
 * GamesDistributor. To sort the games it uses the GamesObject class, which is a
 * SortObject implementation and can be sorted via the Sorter quicksort
 * algorithm.
 *
 * @see unagoclient.sort.Sorter
 */

public class GamesFrame extends CloseFrame implements CloseListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    IgsStream In;
    PrintWriter Out;
    Lister T;
    ConnectionFrame CF;
    GamesDistributor GD;
    ListClass L;
    boolean Closed = false;
    int LNumber;

    public GamesFrame(ConnectionFrame cf, PrintWriter out, IgsStream in) {
        super(Global.resourceString("_Games_"));
        cf.addCloseListener(this);
        this.In = in;
        this.Out = out;
        final MenuBar mb = new MenuBar();
        this.setMenuBar(mb);
        final Menu m = new MyMenu(Global.resourceString("Options"));
        m.add(new MenuItemAction(this, Global.resourceString("Close")));
        mb.add(m);
        final Menu help = new MyMenu(Global.resourceString("Help"));
        help.add(new MenuItemAction(this, Global
                .resourceString("About_this_Window")));
        mb.add(help);
        this.setLayout(new BorderLayout());
        this.T = rene.gui.Global.getParameter("systemlister", false) ? new SystemLister()
        : new Lister();
        this.T.setFont(Global.Monospaced);
        this.T.setText(Global.resourceString("Loading"));
        this.add("Center", this.T);
        final MyPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Observe")));
        p.add(new ButtonAction(this, Global.resourceString("Peek")));
        p.add(new ButtonAction(this, Global.resourceString("Status")));
        p.add(new MyLabel(" "));
        p.add(new ButtonAction(this, Global.resourceString("Refresh")));
        p.add(new ButtonAction(this, Global.resourceString("Close")));
        this.add("South", new Panel3D(p));
        this.CF = cf;
        this.GD = null;
        this.seticon("igames.gif");
        final PopupMenu pop = new PopupMenu();
        this.addpop(pop, Global.resourceString("Observe"));
        this.addpop(pop, Global.resourceString("Peek"));
        this.addpop(pop, Global.resourceString("Status"));
        if (this.T instanceof Lister) {
            this.T.setPopupMenu(pop);
        }
    }

    public void addpop(PopupMenu pop, String label) {
        final MenuItem mi = new MenuItemAction(this, label, label);
        pop.add(mi);
    }

    /**
     * When the distributor has all games, it calls allsended and the sorting
     * will start.
     */
    public synchronized void allsended() {
        if (this.GD != null) {
            this.GD.unchain();
        }
        if (this.Closed) {
            return;
        }
        ListElement p = this.L.first();
        int i, n = 0;
        while (p != null) {
            n++;
            p = p.next();
        }
        if (n > 3) {
            final GamesObject v[] = new GamesObject[n - 1];
            p = this.L.first().next();
            for (i = 0; i < n - 1; i++) {
                v[i] = new GamesObject((String) p.content());
                p = p.next();
            }
            Sorter.sort(v);
            this.T.setText("");
            this.T.appendLine0(" " + (String) this.L.first().content());
            final Color FC = Color.green.darker().darker();
            for (i = 0; i < n - 1; i++) {
                this.T.appendLine0(v[i].game(), v[i].friend() ? FC
                        : Color.black);
            }
            this.T.doUpdate(false);
        } else {
            p = this.L.first();
            while (p != null) {
                this.T.appendLine((String) p.content());
                p = p.next();
            }
            this.T.doUpdate(false);
        }
    }

    @Override
    public synchronized boolean close() {
        if (this.GD != null) {
            this.GD.unchain();
        }
        this.CF.Games = null;
        this.CF.removeCloseListener(this);
        this.Closed = true;
        Global.notewindow(this, "games");
        return true;
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Refresh").equals(o)) {
            this.refresh();
        } else if (Global.resourceString("Peek").equals(o)) {
            final String s = this.T.getSelectedItem();
            if (s == null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skipblanks();
            if (!p.skip("[")) {
                return;
            }
            p.skipblanks();
            if (!p.isint()) {
                return;
            }
            this.CF.peek(p.parseint(']'));
        } else if (Global.resourceString("Status").equals(o)) {
            final String s = this.T.getSelectedItem();
            if (s == null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skipblanks();
            if (!p.skip("[")) {
                return;
            }
            p.skipblanks();
            if (!p.isint()) {
                return;
            }
            this.CF.status(p.parseint(']'));
        } else if (Global.resourceString("Observe").equals(o)) {
            final String s = this.T.getSelectedItem();
            if (s == null) {
                return;
            }
            final StringParser p = new StringParser(s);
            p.skipblanks();
            if (!p.skip("[")) {
                return;
            }
            p.skipblanks();
            if (!p.isint()) {
                return;
            }
            this.CF.observe(p.parseint(']'));
        } else if (Global.resourceString("About_this_Window").equals(o)) {
            new Help("games");
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void isClosed() {
        if (rene.gui.Global.getParameter("menuclose", true)) {
            this.setMenuBar(null);
        }
        this.setVisible(false);
        this.dispose();
    }

    public synchronized void receive(String s) {
        if (this.Closed) {
            return;
        }
        this.L.append(new ListElement(s));
        this.LNumber++;
        if (this.LNumber == 1) {
            this.T.setText(Global.resourceString("Receiving"));
        }
    }

    /**
     * Opens a new GamesDistributor to receive the games from the server. and
     * asks the server to send the games.
     */
    public synchronized void refresh() {
        this.L = new ListClass();
        this.LNumber = 0;
        this.T.setText(Global.resourceString("Loading"));
        if (this.GD != null) {
            this.GD.unchain();
        }
        this.GD = new GamesDistributor(this.In, this);
        this.Out.println("games");
    }
}
