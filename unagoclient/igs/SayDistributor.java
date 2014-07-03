package unagoclient.igs;

import java.awt.*;
import java.io.PrintWriter;

/**
 * The SayDistributor is for say input from the server (19). It will open a
 * SayDialog to answer, unless messages are not turned off.
 */

public class SayDistributor extends Distributor {
    PrintWriter Out;
    public SayDialog MD;
    ConnectionFrame CF;

    public SayDistributor(ConnectionFrame cf, IgsStream in, PrintWriter out) {
        super(in, 19, 0, false);
        this.CF = cf;
        this.Out = out;
        this.MD = null;
    }

    @Override
    public void remove() {
        this.MD = null;
    }

    @Override
    public void send(String C) {
        this.CF.append(C, Color.red.darker());
        if (this.CF.wantsmessages()) {
            if (this.MD != null) {
                this.MD.append(C);
            } else {
                this.MD = new SayDialog(this.CF, this, C, this.Out);
            }
        }
    }
}
