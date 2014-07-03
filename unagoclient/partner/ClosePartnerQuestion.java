package unagoclient.partner;

import unagoclient.Global;
import unagoclient.dialogs.Question;

class ClosePartnerQuestion extends Question {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ClosePartnerQuestion(PartnerFrame g) {
        super(g, Global.resourceString("This_will_close_your_connection_"),
                Global.resourceString("Close"), g, true);
        this.setVisible(true);
    }

    @Override
    public void tell(Question q, Object o, boolean f) {
        q.setVisible(false);
        q.dispose();
        if (f) {
            ((PartnerFrame) o).doclose();
        }
    }
}
