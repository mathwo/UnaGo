package unagoclient;

import unagoclient.dialogs.Question;

public class CloseMainQuestion extends Question {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    MainFrame G;

    public CloseMainQuestion(MainFrame g) {
        super(g, Global.resourceString("End_Application_"), Global
                .resourceString("Exit"), g, true);
        this.G = g;
        this.setVisible(true);
    }

    @Override
    public void tell(Question q, Object o, boolean f) {
        q.setVisible(false);
        q.dispose();
    }
}
