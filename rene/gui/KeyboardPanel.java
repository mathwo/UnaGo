package rene.gui;

import rene.dialogs.ItemEditor;
import rene.dialogs.ItemEditorElement;
import rene.dialogs.ItemPanel;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This is used as the display panel for the keyboard editor. It displays
 * information about the selected keyboard item.
 */

public class KeyboardPanel extends ItemPanel implements KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    TextField MenuString, ActionName, CharKey;
    Checkbox Shift, Control, Alt;
    String Name = "";
    Choice C;
    ItemEditor E;

    public KeyboardPanel() {
        this.setLayout(new BorderLayout());

        final Panel center = new Panel();
        center.setLayout(new GridLayout(0, 1));
        // the menu item
        center.add(this.MenuString = new MyTextField("", 30));
        this.MenuString.setEditable(false);
        // the description
        center.add(this.ActionName = new MyTextField());
        this.ActionName.setEditable(false);
        // the key
        center.add(this.CharKey = new MyTextField());
        this.CharKey.setEditable(false);
        this.CharKey.addKeyListener(this);
        // modifiers
        center.add(this.Shift = new Checkbox(Global.name("keyeditor.shift")));
        center.add(this.Control = new Checkbox(Global.name("keyeditor.control")));
        center.add(this.Alt = new Checkbox(Global.name("keyeditor.alt")));
        this.add("Center", center);

        final Panel south = new Panel();
        south.setLayout(new BorderLayout());
        final Panel c = new Panel();
        // the choice of command keys
        this.C = new Choice();
        if (Global.NormalFont != null) {
            this.C.setFont(Global.NormalFont);
        }
        c.add(this.C);
        this.C.add("-------------");
        south.add("Center", c);
        // default and undefine buttons
        final Panel buttons = new Panel();
        buttons.add(new ButtonAction(this, Global.name("keyeditor.default"),
                "Default"));
        buttons.add(new ButtonAction(this, Global.name("keyeditor.none"),
                "None"));
        south.add("South", buttons);
        this.add("South", south);
    }

    /**
     * The the command shortcut number i.
     */
    public String commandShortcut(int i) {
        final String s = "command." + i;
        final Enumeration e = this.E.getElements().elements();
        while (e.hasMoreElements()) {
            final KeyboardItem k = (KeyboardItem) e.nextElement();
            if (k.getMenuString().equals(s)) {
                return k.shortcut();
            }
        }
        return "";
    }

    /**
     * Display this element on the panel.
     */
    @Override
    public void display(ItemEditorElement e) {
        final KeyboardItem k = (KeyboardItem) e;
        this.Name = k.getName();
        this.MenuString.setText(k.getMenuString());
        this.ActionName.setText(k.getActionName());
        this.CharKey.setText(k.getCharKey());
        this.MenuString.setText(k.getMenuString());
        this.Shift.setState(k.isShift());
        this.Control.setState(k.isControl());
        this.Alt.setState(k.isAlt());
        this.C.select(k.getCommandType());
    }

    /**
     * React on the Default and None buttons.
     */
    @Override
    public void doAction(String o) {
        if (o.equals("Default")) {
            final String s = this.MenuString.getText();
            final KeyboardItem k = new KeyboardItem(s, Global.name("key." + s));
            this.CharKey.setText(k.getCharKey());
            this.Shift.setState(k.isShift());
            this.Control.setState(k.isControl());
            this.Alt.setState(k.isAlt());
        } else if (o.equals("None")) {
            this.CharKey.setText("none");
            this.Shift.setState(false);
            this.Control.setState(false);
            this.Alt.setState(false);
        } else {
            super.doAction(o);
        }
    }

    /**
     * User wishes to clear all keyboard definitions.
     */
    @Override
    public boolean extra(Vector v) {
        v.removeAllElements();
        return true;
    }

    /**
     * Create a new keyboard element from the panel entries.
     */
    @Override
    public ItemEditorElement getElement() {
        final int type = this.C.getSelectedIndex();
        return new KeyboardItem(this.CharKey.getText(),
                this.MenuString.getText(), this.ActionName.getText(),
                this.Shift.getState(), this.Control.getState(),
                this.Alt.getState(), type);
    }

    @Override
    public String getName() {
        return this.Name;
    }

    /*
     * Override methods of ItemPanel
     */

    /**
     * Set the key, if one is pressed inside the CharKey textfield.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        this.Shift.setState(e.isShiftDown());
        this.Control.setState(e.isControlDown());
        this.Alt.setState(e.isAltDown());
        this.CharKey.setText(KeyDictionary.translate(e.getKeyCode())
                .toLowerCase());
        this.C.select(0);
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Build a list of available command keys.
     */
    public void makeCommandChoice() {
        this.C.removeAll();
        this.C.add("");
        for (int i = 1; i <= 5; i++) {
            final String s = this.commandShortcut(i);
            this.C.add(i + ": " + s);
        }
        this.C.select(0);
    }

    /**
     * Test on doublicate keys, and undefine them.
     */
    @Override
    public void notifyChange(Vector v, int item) {
        final KeyboardItem changed = (KeyboardItem) v.elementAt(item);
        final String descr = changed.keyDescription();
        for (int i = 0; i < v.size(); i++) {
            if (i == item) {
                continue;
            }
            final KeyboardItem k = (KeyboardItem) v.elementAt(i);
            if (k.keyDescription().equals(descr)) {
                v.setElementAt(new KeyboardItem(k.getMenuString(), "none"), i);
            }
        }
        if (changed.getMenuString().startsWith("command.")) {
            this.makeCommandChoice();
        }
    }

    public void setItemEditor(ItemEditor e) {
        this.E = e;
    }

    @Override
    public void setName(String s) {
        this.Name = s;
        this.MenuString.setText(s);
    }
}
