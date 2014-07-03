package unagoclient.igs;

import unagoclient.Global;
import unagoclient.dialogs.Message;

import java.awt.*;
import java.io.PrintWriter;

/**
 * This distributor receives and handles error messages from the server. It will
 * always open a new dialog box (a Message) to display the error.
 *
 * @see unagoclient.igs.Message
 */

public class ErrorDistributor extends Distributor {
    ConnectionFrame CF;
    PrintWriter Out;
    String S;

    public ErrorDistributor(ConnectionFrame cf, IgsStream in, PrintWriter out) {
        super(in, 5, 0, false);
        this.CF = cf;
        this.Out = out;
        this.S = new String("");
    }

    @Override
    public void allsended() {
        if (Global.blocks(this.S) == 0 && this.CF.wantserrors()) {
            new Message(this.CF, Global.resourceString("Error:\n") + this.S);
        }
        this.CF.append("Error\n" + this.S, Color.red.darker());
        this.S = "";
    }

    @Override
    public void send(String C) {
        if (this.S.equals("")) {
            this.S = this.S + C;
        } else {
            this.S = this.S + "\n" + C;
        }
    }
}
