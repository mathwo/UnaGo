package unagoclient.igs;

/**
 * The distributor for status reports (command number 22). Reports output to a
 * Status object.
 *
 * @see unagoclient.igs.Status
 */

public class StatusDistributor extends Distributor {
    Status P;

    public StatusDistributor(IgsStream in, Status p) {
        super(in, 22, 0, true);
        this.P = p;
    }

    @Override
    public void finished() {
        this.P.finished();
    }

    @Override
    public void send(String c) {
        this.P.receive(c);
    }
}
