package unagoclient.partner;

import unagoclient.Global;
import unagoclient.dialogs.Question;

/**
 * Question to end the game, or decline that.
 */

public class EndGameQuestion extends Question {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    PartnerFrame G;

    public EndGameQuestion(PartnerFrame g) {
        super(g, Global.resourceString("End_the_game_"), Global
                .resourceString("End"), g, true);
        this.G = g;
        this.setVisible(true);
    }

    @Override
    public boolean close() {
        this.G.declineendgame();
        return true;
    }

    @Override
    public void tell(Question q, Object o, boolean f) {
        q.setVisible(false);
        q.dispose();
        if (f) {
            this.G.doendgame();
        } else {
            this.G.declineendgame();
        }
    }
}
