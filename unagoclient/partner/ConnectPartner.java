package unagoclient.partner;

import unagoclient.Global;
import unagoclient.dialogs.Message;
import unagoclient.partner.partner.Partner;

/**
 * A thread, which will try to connect to a go partner. If it is successfull, a
 * Partner Frame will open. Otherwise, an error message will appear.
 */

public class ConnectPartner extends Thread {
    Partner P;
    PartnerFrame PF;

    public ConnectPartner(Partner p, PartnerFrame pf) {
        this.P = p;
        this.PF = pf;
        this.start();
    }

    @Override
    public void run() {
        this.P.Trying = true;
        if (rene.gui.Global.getParameter("userelay", false)) {
            if (!this.PF.connectvia(this.P.Server, this.P.Port,
                    rene.gui.Global.getParameter("relayserver", "localhost"),
                    rene.gui.Global.getParameter("relayport", 6971))) {
                this.PF.setVisible(false);
                this.PF.dispose();
                new Message(Global.frame(),
                        Global.resourceString("No_connection_to_")
                        + this.P.Server);
                try {
                    Thread.sleep(10000);
                } catch (final Exception e) {
                    this.P.Trying = false;
                }
            }
        } else if (!this.PF.connect(this.P.Server, this.P.Port)) {
            this.PF.setVisible(false);
            this.PF.dispose();
            new Message(Global.frame(),
                    Global.resourceString("No_connection_to_") + this.P.Server);
            try {
                Thread.sleep(10000);
            } catch (final Exception e) {
                this.P.Trying = false;
            }
        }
        this.P.Trying = false;
    }
}
