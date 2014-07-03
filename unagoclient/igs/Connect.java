package unagoclient.igs;

import unagoclient.Global;
import unagoclient.dialogs.Message;
import unagoclient.igs.connection.Connection;

/**
 * A thread, which tries to connect to a server. It will open a ConnectionFrame
 * to display the connection on success.
 * <p>
 * If it fails, it will display an error message for 10 seconds.
 */

public class Connect extends Thread {
    Connection C;
    ConnectionFrame CF;
    String S;

    public Connect(Connection c, ConnectionFrame cf) {
        this.C = c;
        this.CF = cf;
        this.S = "";
        this.start();
    }

    public Connect(Connection c, String s, ConnectionFrame cf) {
        this.C = c;
        this.CF = cf;
        this.S = s;
        this.start();
    }

    @Override
    public void run() {
        this.C.Trying = true;
        this.CF.movestyle(this.C.MoveStyle);
        if (rene.gui.Global.getParameter("userelay", false)) {
            if (!this.CF.connectvia(this.C.Server, this.C.Port, this.C.User,
                    this.S.equals("") ? this.C.Password : this.S,
                            rene.gui.Global.getParameter("relayserver", "localhost"),
                            rene.gui.Global.getParameter("relayport", 6971))) {
                this.CF.setVisible(false);
                this.CF.dispose();
                new Message(Global.frame(),
                        Global.resourceString("No_connection_to_")
                        + this.C.Server + "!");
                try {
                    Thread.sleep(10000);
                } catch (final Exception e) {
                    this.C.Trying = false;
                }
            }
        } else if (!this.CF.connect(this.C.Server, this.C.Port, this.C.User,
                this.S.equals("") ? this.C.Password : this.S,
                        this.C.Port == 23 ? true : false)) {
            this.CF.setVisible(false);
            this.CF.dispose();
            new Message(Global.frame(),
                    Global.resourceString("No_connection_to_") + this.C.Server
                    + "!");
            try {
                Thread.sleep(10000);
            } catch (final Exception e) {
                this.C.Trying = false;
            }
        }
        this.C.Trying = false;
    }
}
