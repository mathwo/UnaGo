package unagoclient.partner;

import unagoclient.Dump;
import unagoclient.Global;
import unagoclient.datagram.DatagramMessage;
import unagoclient.partner.partner.Partner;
import rene.util.list.ListElement;

import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is the server thread for partner connections. If anyone connects to the
 * server, a new PartnerFrame will open to handle the connection. If the server
 * starts, it will open a new PartnerServerThread, which checks for datagrams
 * that announce open partners.
 */

public class Server extends Thread {
    int Port;
    boolean Public;
    static public PartnerServerThread PST = null;
    ServerSocket SS;

    /**
     * @param p
     *            the server port
     * @param publ
     *            server is public or not
     */
    public Server(int p, boolean publ) {
        this.Port = p;
        this.Public = publ;
        this.start();
    }

    /**
     * This is called, when the server is closed. It will announce the closing
     * to known servers by a datagram.
     */
    public void close() {
        if (!this.Public) {
            return;
        }
        ListElement pe = Global.PartnerList.first();
        final DatagramMessage d = new DatagramMessage();
        d.add("close");
        d.add(rene.gui.Global.getParameter("yourname", "Unknown"));
        try {
            final String s = InetAddress.getLocalHost().toString();
            d.add(s.substring(s.lastIndexOf('/') + 1));
        } catch (final Exception e) {
            d.add("Unknown Host");
        }
        while (pe != null) {
            final Partner p = (Partner) pe.content();
            if (p.State > 0) {
                d.send(p.Server, p.Port + 2);
            }
            pe = pe.next();
        }
        Global.Busy = true;
    }

    /**
     * This is called, when the server is opened. It will announce the opening
     * to known servers by a datagram.
     */
    public void open() {
        if (this.Public) {
            ListElement pe = Global.PartnerList.first();
            while (pe != null) {
                final Partner p = (Partner) pe.content();
                if (p.State > 0) {
                    final DatagramMessage d = new DatagramMessage();
                    d.add("open");
                    d.add(rene.gui.Global.getParameter("yourname", "Unknown"));
                    try {
                        final String s = InetAddress.getLocalHost().toString();
                        d.add(s.substring(s.lastIndexOf('/') + 1));
                    } catch (final Exception e) {
                        d.add("Unknown Host");
                    }
                    d.add("" + rene.gui.Global.getParameter("serverport", 6970));
                    d.add("" + p.State);
                    d.send(p.Server, p.Port + 2);
                }
                pe = pe.next();
            }
        }
        Global.Busy = false;
    }

    @Override
    public void run() {
        if (Server.PST == null) {
            Server.PST = new PartnerServerThread(rene.gui.Global.getParameter(
                    "serverport", 6970) + 2);
        }
        try {
            Thread.sleep(1000);
        } catch (final Exception e) {
        }
        try {
            this.SS = new ServerSocket(this.Port);
            while (true) {
                final Socket S = this.SS.accept();
                if (Global.Busy) // user set the busy checkbox
                {
                    final PrintWriter o = new PrintWriter(new DataOutputStream(
                            S.getOutputStream()), true);
                    o.println("@@busy");
                    S.close();
                    continue;
                }
                final PartnerFrame cf = new PartnerFrame(
                        Global.resourceString("Server"), true);
                Global.setwindow(cf, "partner", 500, 400);
                cf.setVisible(true);
                cf.open(S);
            }
        } catch (final Exception e) {
            Dump.println("Server Error");
        }
    }
}
