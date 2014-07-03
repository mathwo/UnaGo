package unagoclient.board;

import unagoclient.Global;

import java.awt.*;

/**
 * A GoFrame for local boards (not connected). <b>Note:</b> This will exit the
 * program, when closed.
 */

public class LocalGoFrame extends GoFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public LocalGoFrame(Frame f, String s) {
        super(f, s);
    }

    @Override
    public void doclose() {
        super.doclose();
        Global.writeparameter(".go.cfg");
        if (!Global.isApplet()) {
            System.exit(0);
        }
    }
}
