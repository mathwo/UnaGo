/*
 * Created on 15.01.2006
 *
 */
package rene.lister;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public class ListerMouseEvent extends ActionEvent {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static int ID = 0;
    MouseEvent E;

    public ListerMouseEvent(Object o, String name, MouseEvent e) {
        super(o, ListerMouseEvent.ID++, name);
        this.E = e;
    }

    public int clickCount() {
        return this.E.getClickCount();
    }

    public MouseEvent getEvent() {
        return this.E;
    }

    public String getName() {
        return this.E.paramString();
    }

    public boolean rightMouse() {
        return this.E.isMetaDown();
    }
}
