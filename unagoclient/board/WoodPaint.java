package unagoclient.board;

import unagoclient.Global;
import unagoclient.StopThread;

import java.awt.*;

/**
 * This is a thread to create an empty board.
 *
 * @see unagoclient.board.EmptyPaint
 */

public class WoodPaint extends StopThread {
    int W, H, Ox, Oy, D;
    Color C;
    Frame F;
    boolean Shadows;

    public WoodPaint(Frame f) {
        this.F = f;
        this.setPriority(this.getPriority() - 1);
        this.start();
    }

    @Override
    public void run() {
        EmptyPaint.createwood(this, this.F,
                rene.gui.Global.getParameter("sboardwidth", 0),
                rene.gui.Global.getParameter("sboardheight", 0),
                Global.getColor("boardcolor", 170, 120, 70),
                rene.gui.Global.getParameter("shadows", true),
                rene.gui.Global.getParameter("sboardox", 5),
                rene.gui.Global.getParameter("sboardoy", 5),
                rene.gui.Global.getParameter("sboardd", 10));
    }
}
