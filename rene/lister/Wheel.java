/*
 * Created on 14.01.2006
 *
 */
package rene.lister;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

class Wheel implements MouseWheelListener {
    WheelListener V;

    public Wheel(WheelListener v) {
        this.V = v;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            if (e.getWheelRotation() > 0) {
                this.V.pageUp();
            } else {
                this.V.pageDown();
            }
        } else {
            final int n = e.getScrollAmount();
            if (e.getWheelRotation() > 0) {
                this.V.up(n);
            } else {
                this.V.down(n);
            }
        }
    }
}