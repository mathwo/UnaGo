package rene.dialogs;

import rene.gui.*;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

/**
 * This is a general class to one in a list of items (like makros, plugins or
 * tools in the JE editor).
 */

public class ItemEditor extends CloseDialog implements ItemListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Frame F;
    /**
     * An AWT list at the left
     */
    MyList L;
    /**
     * Aborted?
     */
    boolean Aborted = true;
    /**
     * A vector of ItemEditorElement objects
     */
    Vector V;
    /**
     * A panel to display the element settings
     */
    ItemPanel P;
    /**
     * The name of this editor
     */
    String Name;
    /**
     * The displayed item
     */
    int Displayed = -1;
    /**
     * possible actions
     */
    public final static int NONE = 0, SAVE = 1, LOAD = 2;
    /**
     * save or load action
     */
    int Action = ItemEditor.NONE;

    /**
     * @param p
     *            The item editor panel
     * @param v
     *            The vector of item editor elements
     * @param prompt
     *            The prompt, displayed in the north
     */
    public ItemEditor(Frame f, ItemPanel p, Vector v, String name, String prompt) {
        this(f, p, v, name, prompt, true, true, false, "");
    }

    public ItemEditor(Frame f, ItemPanel p, Vector v, String name,
            String prompt, boolean allowChanges, boolean allowReorder,
            boolean allowSave, String extraButton) {
        super(f, Global.name(name + ".title"), true);
        this.Name = name;
        this.F = f;
        this.P = p;
        this.setLayout(new BorderLayout());

        // Title String:
        final Panel title = new MyPanel();
        title.add(new MyLabel(prompt));
        this.add("North", title);

        // Center panel:
        final Panel center = new MyPanel();
        center.setLayout(new BorderLayout(5, 5));

        // Element List:
        center.add("West", this.L = new MyList(10));
        this.L.addItemListener(this);

        // Editor Panel:
        final Panel cp = new MyPanel();
        cp.setLayout(new BorderLayout());
        cp.add("North", this.P);
        cp.add("Center", new MyPanel());
        center.add("Center", cp);

        this.add("Center", new Panel3D(center));

        // Buttons:
        final Panel buttons = new MyPanel();
        buttons.setLayout(new GridLayout(0, 1));

        if (allowChanges) {
            final Panel buttons1 = new MyPanel();
            buttons1.add(new ButtonAction(this, Global
                    .name("itemeditor.insert"), "Insert"));
            buttons1.add(new ButtonAction(this, Global.name("itemeditor.new"),
                    "New"));
            buttons1.add(new ButtonAction(this, Global
                    .name("itemeditor.delete"), "Delete"));
            buttons.add(buttons1);
        }

        if (allowReorder) {
            final Panel buttons2 = new MyPanel();
            buttons2.add(new ButtonAction(this, Global.name("itemeditor.down"),
                    "Down"));
            buttons2.add(new ButtonAction(this, Global.name("itemeditor.up"),
                    "Up"));
            buttons.add(buttons2);
        }

        final Panel buttons3 = new MyPanel();
        buttons3.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttons3.add(new ButtonAction(this, Global.name("OK"), "OK"));
        buttons3.add(new ButtonAction(this, Global.name("abort"), "Close"));
        buttons.add(buttons3);

        if (allowSave) {
            final Panel buttons4 = new MyPanel();
            buttons4.setLayout(new FlowLayout(FlowLayout.RIGHT));
            buttons4.add(new ButtonAction(this, Global.name("save"), "Save"));
            buttons4.add(new ButtonAction(this, Global.name("load"), "Load"));
            if (!extraButton.equals("")) {
                buttons4.add(new ButtonAction(this, extraButton, "Extra"));
            }
            buttons.add(buttons4);
        }

        this.add("South", new Panel3D(buttons));

        this.V = new Vector();
        for (int i = 0; i < v.size(); i++) {
            this.V.addElement(v.elementAt(i));
        }
        this.init();

        this.pack();
    }

    /**
     * Changes the currently selected item.
     */
    void define() {
        final int Selected = this.L.getSelectedIndex();
        if (Selected < 0) {
            return;
        }
        this.define(Selected);
        this.L.select(Selected);
    }

    /**
     * Changes an item.
     */
    void define(int Selected) {
        final String name = this.P.getName();
        if (name.equals("")) {
            return;
        }
        if (!this.L.getItem(Selected).equals(name)) {
            this.L.replaceItem(name, Selected);
        }
        this.V.setElementAt(this.P.getElement(), Selected);
        this.P.notifyChange(this.V, Selected);
    }

    /**
     * Delete the current selected item.
     */
    void delete() {
        int Selected = this.L.getSelectedIndex();
        if (Selected < 0) {
            return;
        }
        this.V.removeElementAt(Selected);
        this.L.remove(Selected);
        if (this.L.getItemCount() == 0) {
            return;
        }
        if (Selected >= this.L.getItemCount()) {
            Selected--;
        }
        this.L.select(Selected);
        this.select();
    }

    @Override
    public void doAction(String o) {
        if (o.equals("Delete")) {
            this.delete();
        } else if (o.equals("Insert")) {
            this.insert();
        } else if (o.equals("New")) {
            this.P.newElement();
        } else if (o.equals("Up")) {
            this.up();
        } else if (o.equals("Down")) {
            this.down();
        } else if (o.equals("OK")) {
            this.noteSize(this.Name);
            this.define();
            this.Aborted = false;
            this.doclose();
        } else if (o.equals("Help")) {
            this.P.help();
        } else if (o.equals("Save")) {
            this.define();
            this.Action = ItemEditor.SAVE;
            this.Aborted = false;
            this.doclose();
        } else if (o.equals("Load")) {
            this.define();
            this.Action = ItemEditor.LOAD;
            this.Aborted = false;
            this.doclose();
        } else if (o.equals("Extra")) {
            if (this.P.extra(this.V)) {
                this.Aborted = false;
                this.doclose();
            }
        } else {
            super.doAction(o);
        }
    }

    /**
     * Push the selected item one down.
     */
    void down() {
        this.define();
        int Selected = this.L.getSelectedIndex();
        if (Selected < 0 || Selected + 1 >= this.V.size()) {
            return;
        }
        final ItemEditorElement now = (ItemEditorElement) this.V
                .elementAt(Selected), next = (ItemEditorElement) this.V
                .elementAt(Selected + 1);
        this.V.setElementAt(next, Selected);
        this.V.setElementAt(now, Selected + 1);
        this.L.replaceItem(next.getName(), Selected);
        this.L.replaceItem(now.getName(), Selected + 1);
        Selected = Selected + 1;
        this.L.select(Selected);
        this.select();
    }

    /**
     * Find a plugin by name.
     *
     * @return true, if it exists.
     */
    boolean find(String name) {
        int i;
        for (i = 0; i < this.V.size(); i++) {
            final ItemEditorElement t = ((ItemEditorElement) this.V
                    .elementAt(i));
            if (t.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the action, if there is one
     *
     * @return NONE, LOAD, SAVE
     */
    public int getAction() {
        return this.Action;
    }

    /**
     * @return The list of item elements.
     */
    public Vector getElements() {
        return this.V;
    }

    /**
     * @param v
     *            A vector of item editor elements.
     */
    public void init() {
        for (int i = 0; i < this.V.size(); i++) {
            final ItemEditorElement e = (ItemEditorElement) this.V.elementAt(i);
            this.L.add(e.getName());
        }
        if (this.V.size() > 0) {
            this.L.select(0);
            this.select();
        }
    }

    /**
     * Insert the current element, renaming if necessary.
     */
    void insert() {
        String name = this.P.getName();
        int Selected = this.L.getSelectedIndex();
        if (Selected < 0) {
            Selected = 0;
        }
        while (this.find(name)) {
            name = name + "*";
        }
        this.P.setName(name);
        final ItemEditorElement e = this.P.getElement();
        this.L.add(e.getName(), Selected);
        this.L.select(Selected);
        this.V.insertElementAt(e, Selected);
    }

    /**
     * @return If aborted.
     */
    @Override
    public boolean isAborted() {
        return this.Aborted;
    }

    /**
     * React on Item changes.
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == this.L) {
            if (this.Displayed >= 0) {
                this.define(this.Displayed);
            }
            this.select();
        }
    }

    /**
     * Show the currently selected item on the item panel.
     */
    public void select() {
        final int i = this.L.getSelectedIndex();
        if (i < 0) {
            return;
        }
        this.P.display((ItemEditorElement) this.V.elementAt(i));
        this.Displayed = i;
    }

    /**
     * Push the selected item one up.
     */
    void up() {
        this.define();
        int Selected = this.L.getSelectedIndex();
        if (Selected <= 0) {
            return;
        }
        final ItemEditorElement now = (ItemEditorElement) this.V
                .elementAt(Selected), prev = (ItemEditorElement) this.V
                .elementAt(Selected - 1);
        this.V.setElementAt(prev, Selected);
        this.V.setElementAt(now, Selected - 1);
        this.L.replaceItem(prev.getName(), Selected);
        this.L.replaceItem(now.getName(), Selected - 1);
        Selected = Selected - 1;
        this.L.select(Selected);
        this.select();
    }
}
