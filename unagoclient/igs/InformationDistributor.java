package unagoclient.igs;

import unagoclient.Global;

import java.io.PrintWriter;

/**
 * A Distributor to display informations from the server (type 9). It will open
 * a new InformationDialog or append to an old one.
 */

public class InformationDistributor extends Distributor {
    ConnectionFrame CF;
    PrintWriter Out;
    String S;
    public InformationDialog infodialog;
    int Lines;

    public InformationDistributor(ConnectionFrame cf, IgsStream in,
            PrintWriter out) {
        super(in, 9, 0, false);
        this.CF = cf;
        this.Out = out;
        this.S = new String("");
        this.Lines = 0;
        this.infodialog = null;
    }

    @Override
    public void allsended() {
        if (this.S.equals("")) {
            return;
        }
        if (this.S.startsWith("Match") && this.S.indexOf("requested") > 0) {
            new MatchDialog(this.CF, this.S, this.Out, this);
            this.S = "";
            this.Lines = 0;
            return;
        }
        if (Global.blocks(this.S) != MessageFilter.BLOCK_COMPLETE) {
            this.CF.append(this.S);
        }
        if ((Global.blocks(this.S) == 0 && this.CF.wantsinformation())
                || Global.posfilter(this.S)) {
            if (this.infodialog == null) {
                this.infodialog = new InformationDialog(this.CF, this.S + "\n",
                        this.Out, this);
            } else {
                this.infodialog.append(this.S + "\n");
            }
        }
        this.S = "";
        this.Lines = 0;
    }

    @Override
    public void remove() {
        this.infodialog = null;
    }

    @Override
    public void send(String C) {
        if (this.Lines > 0) {
            this.S = this.S + "\n" + C;
        } else {
            this.S = this.S + C;
        }
        this.Lines++;
    }
}
