package unagoclient.partner;

import unagoclient.Global;
import unagoclient.dialogs.Question;

/**
 * Question to accept a result or decline it.
 */

public class ResultQuestion extends Question {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int B, W;
    PartnerFrame G;

    /**
     * @param b
     *            ,w Black and White results
     */
    public ResultQuestion(PartnerFrame g, String m, int b, int w) {
        super(g, m, Global.resourceString("Result"), g, true);
        this.B = b;
        this.W = w;
        this.G = g;
        this.setVisible(true);
    }

    @Override
    public boolean close() {
        this.G.declineresult();
        return true;
    }

    @Override
    public void tell(Question q, Object o, boolean f) {
        q.setVisible(false);
        q.dispose();
        if (f) {
            this.G.doresult(this.B, this.W);
        } else {
            this.G.declineresult();
        }
    }
}
