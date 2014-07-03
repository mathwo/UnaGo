package unagoclient.partner;

import unagoclient.Global;
import unagoclient.Go;
import unagoclient.StopThread;
import unagoclient.gui.*;
import unagoclient.partner.partner.Partner;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import javax.swing.*;
import java.awt.*;

/**
 * This is a frame, which displays a list of all open partner servers. It
 * contains buttons to connect to one of one of them and to refresh the list.
 */

public class OpenPartnerFrame extends CloseFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Go G;
    java.awt.List L;
    OpenPartnerFrameUpdate OPFU;

    public OpenPartnerFrame(Go go) {
        super(Global.resourceString("Open_Partners"));
        this.G = go;
        final MenuBar mb = new MenuBar();
        this.setMenuBar(mb);
        final Menu m = new MyMenu(Global.resourceString("Options"));
        m.add(new MenuItemAction(this, Global.resourceString("Close")));
        mb.add(m);
        this.setLayout(new BorderLayout());
        this.L = new java.awt.List();
        this.L.setFont(Global.SansSerif);
        this.refresh();
        this.add("Center", this.L);
        final JPanel bp = new MyPanel();
        bp.add(new ButtonAction(this, Global.resourceString("Connect")));
        bp.add(new ButtonAction(this, Global.resourceString("Refresh")));
        bp.add(new MyLabel(" "));
        bp.add(new ButtonAction(this, Global.resourceString("Close")));
        this.add("South", bp);
        Global.setwindow(this, "openpartner", 300, 200);
        this.seticon("iunago.gif");
        this.setVisible(true);
        this.OPFU = new OpenPartnerFrameUpdate(this);
    }

    public void connect() {
        ListElement le = Global.OpenPartnerList.first();
        final String s = this.L.getSelectedItem();
        while (le != null) {
            final Partner p = (Partner) le.content();
            if (p.Name.equals(s)) {
                final PartnerFrame cf = new PartnerFrame(
                        Global.resourceString("Connection_to_") + p.Name, false);
                Global.setwindow(cf, "partner", 500, 400);
                new ConnectPartner(p, cf);
                return;
            }
            le = le.next();
        }
    }

    @Override
    public void doAction(String o) {
        if (o.equals(Global.resourceString("Refresh"))) {
            this.refresh();
        } else if (o.equals(Global.resourceString("Close"))) {
            this.doclose();
        } else if (o.equals(Global.resourceString("Connect"))) {
            this.connect();
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        this.G.OPF = null;
        this.OPFU.stopit();
        Global.notewindow(this, "openpartner");
        super.doclose();
    }

    public void refresh() {
        final ListClass PL = Global.OpenPartnerList;
        this.L.removeAll();
        if (PL == null) {
            return;
        }
        ListElement le = PL.first();
        while (le != null) {
            this.L.add(((Partner) le.content()).Name);
            le = le.next();
        }
    }
}

class OpenPartnerFrameUpdate extends StopThread {
    OpenPartnerFrame OPF;

    public OpenPartnerFrameUpdate(OpenPartnerFrame f) {
        this.OPF = f;
        this.start();
    }

    @Override
    public void run() {
        while (this.stopped()) {
            try {
                Thread.sleep(30000);
            } catch (final Exception e) {
            }
            this.OPF.refresh();
        }
    }
}
