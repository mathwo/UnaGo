package rene.dialogs;

import rene.gui.DoActionListener;

import java.awt.*;
import java.util.Vector;

public class ItemPanel extends Panel implements DoActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public void display(ItemEditorElement e) {
    }

    @Override
    public void doAction(String o) {
    }

    /**
     * Called, when the extra Button was pressed.
     *
     * @return If the panel should be closed immediately.
     * @v The vector of KeyboardItem.
     */
    public boolean extra(Vector v) {
        return false;
    }

    public ItemEditorElement getElement() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    public void help() {
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }

    public void newElement() {
    }

    /**
     * Called, whenever an item is redefined.
     *
     * @param v
     *            The vector of KeyboardItem.
     * @param item
     *            The currently changed item number.
     */
    public void notifyChange(Vector v, int item) {
    }

    @Override
    public void setName(String name) {
    }
}
