package unagoclient.partner;

import unagoclient.Global;
import unagoclient.dialogs.Question;

/**
 * Question to undo a move, or decline the undo request.
 */

public class UndoQuestion extends Question {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    PartnerFrame G;

    public UndoQuestion(PartnerFrame g) {
        super(g, Global.resourceString("Partner_request_undo__Accept_"), Global
                .resourceString("Undo"), g, true);
        this.G = g;
        this.setVisible(true);
    }

    @Override
    public boolean close() {
        this.G.declineundo();
        return true;
    }

    @Override
    public void tell(Question q, Object o, boolean f) {
        q.setVisible(false);
        q.dispose();
        if (f) {
            this.G.doundo();
        } else {
            this.G.declineundo();
        }
    }
}
