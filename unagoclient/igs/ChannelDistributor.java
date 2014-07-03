package unagoclient.igs;

import java.io.PrintWriter;

/**
 * This distributor opens a ChannelDialog for channel n. IgsStream sorts out the
 * channels and calls the correct distributor.
 *
 * @see unagoclient.igs.ChannelDialog
 */
public class ChannelDistributor extends Distributor {
    ConnectionFrame CF;
    PrintWriter Out;
    ChannelDialog CD;

    public ChannelDistributor(ConnectionFrame cf, IgsStream in,
            PrintWriter out, int n) {
        super(in, 32, n, false);
        this.CF = cf;
        this.Out = out;
        this.CD = new ChannelDialog(this.CF, this.Out, this.game(), this);
    }

    @Override
    public void send(String C) {
        if (this.CD == null) {
            this.CD = new ChannelDialog(this.CF, this.Out, this.game(), this);
        }
        this.CD.append(C);
    }
}
