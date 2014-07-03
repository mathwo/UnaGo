package unagoclient.igs;

import unagoclient.Global;
import unagoclient.sound.UnaGoSound;
import rene.util.parser.StringParser;

import java.awt.*;
import java.io.PrintWriter;

/**
 * This is used to parse messages (code 24) from the server. It will open a
 * MessageDialog, unless this is checked off. The reply method of the
 * ConnectionFrame is checked for auto replies.
 */

public class MessageDistributor extends Distributor {
    ConnectionFrame CF;
    PrintWriter Out;
    MessageDialog MD;
    String LastUser;

    public MessageDistributor(ConnectionFrame cf, IgsStream in, PrintWriter out) {
        super(in, 24, 0, false);
        this.CF = cf;
        this.Out = out;
        this.MD = null;
    }

    @Override
    public void remove() {
        this.MD = null;
    }

    /**
     * got a message
     */
    @Override
    public void send(String C) {
        if (C.equals("")) {
            return;
        }
        final StringParser p = new StringParser(C);
        if (!p.skip("*")) {
            return;
        }
        final String user = p.upto('*');
        if (!p.skip("*:")) {
            return;
        }
        p.skipblanks();
        if (p.error()) {
            return;
        }
        this.CF.append(C, Color.red.darker());
        final String a = this.CF.reply();
        if (!a.equals("")) // autoreply on
        {
            if (this.LastUser == null || !this.LastUser.equals(user)) {
                this.CF.append(Global.resourceString("Auto_reply_sent_to_")
                        + user);
                this.Out.println("tell " + user + " " + a);
                this.LastUser = user;
            }
        }
        // no autoreply
        else if (Global.blocks(C) != 0) {
            return;
        } else if (this.CF.wantsmessages() || Global.posfilter(C)) {
            if (this.MD != null) {
                this.MD.append(user, p.upto((char) 0));
                UnaGoSound.play("wip", "wip", true);
            } else {
                this.MD = new MessageDialog(this.CF, user, p.upto((char) 0),
                        this.Out, this);
                if (Global.blocks(C) == 0) {
                    UnaGoSound.play("message", "wip", true);
                }
            }
        } else {
            UnaGoSound.play("wip", "wip", true);
        }
    }
}
