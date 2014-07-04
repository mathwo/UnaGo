package rene.gui;

import rene.dialogs.ItemEditorElement;

import java.util.StringTokenizer;

/**
 * A keyboard item. Can be constructed from a menu string (like
 * editor.file.open) and a key string (like control.o). Can test a key event, if
 * it fits.
 */

public class KeyboardItem implements ItemEditorElement, Comparable<KeyboardItem> {
    boolean Shift, Control, Alt;
    String CharKey;
    String MenuString, ActionName;
    int CommandType = 0;

    /**
     * Copy constructor, for use in editing (clone).
     */
    public KeyboardItem(KeyboardItem item) {
        this.Shift = item.Shift;
        this.Control = item.Control;
        this.Alt = item.Alt;
        this.CharKey = item.CharKey;
        this.MenuString = item.MenuString;
        this.ActionName = item.ActionName;
        this.CommandType = item.CommandType;
    }

    /**
     * @param menu
     *            The menu string.
     * @param key
     *            The key description a la "esc1.shift.control.e"
     */
    public KeyboardItem(String menu, String key) {
        this.MenuString = menu;
        this.Shift = this.Control = this.Alt = false;
        this.CommandType = 0;
        this.CharKey = "";
        final StringTokenizer t = new StringTokenizer(key, ".");
        while (t.hasMoreTokens()) {
            final String token = t.nextToken();
            if (t.hasMoreTokens()) {
                if (token.equals("control")) {
                    this.Control = true;
                } else if (token.equals("shift")) {
                    this.Shift = true;
                } else if (token.equals("alt")) {
                    this.Alt = true;
                } else if (token.startsWith("esc"))
                // esc should be followed by a number
                {
                    try {
                        this.CommandType = Integer.parseInt(token.substring(3));
                    } catch (final Exception e) {
                    }
                } else {
                    return;
                }
            } else {
                if (key.equals("")) {
                    return;
                }
                this.CharKey = token.toLowerCase();
            }
        }
        this.ActionName = Global.name(this.getStrippedMenuString());
    }

    /**
     * @param charkey
     *            The keyboard descriptive character (like "page down")
     * @param menustring
     *            The menu item (may have some *s added)
     * @param actionname
     *            The description of the menu item.
     * @param shift
     *            ,control,alt Modifier flags.
     * @param commandtype
     *            The command key, that is needed (0 is none).
     */
    public KeyboardItem(String charkey, String menustring, String actionname,
            boolean shift, boolean control, boolean alt, int commandtype) {
        this.Shift = shift;
        this.Control = control;
        this.Alt = alt;
        this.CharKey = charkey.toLowerCase();
        this.MenuString = menustring;
        this.ActionName = actionname;
        this.CommandType = commandtype;
    }

    @Override
    public int compareTo(KeyboardItem o) {
        return this.getName().compareTo(o.getName());
    }

    public String getActionName() {
        return this.ActionName;
    }

    public String getCharKey() {
        return this.CharKey;
    }

    public int getCommandType() {
        return this.CommandType;
    }

    public String getMenuString() {
        return this.MenuString;
    }

    /**
     * Get the name of this KeyboardItem element.
     */
    @Override
    public String getName() {
        return this.MenuString;
    }

    /**
     * Get a menu string, which is stripped from stars.
     */
    public String getStrippedMenuString() {
        String s = this.MenuString;
        while (s.endsWith("*")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public boolean isAlt() {
        return this.Alt;
    }

    public boolean isControl() {
        return this.Control;
    }

    public boolean isShift() {
        return this.Shift;
    }

    /**
     * Return a key description for this item (to save as parameter). This
     * should be the same as the key parameter in the constructor.
     */
    public String keyDescription() {
        String s = this.CharKey.toLowerCase();
        if (s.equals("none") || s.equals("default")) {
            return s;
        }
        if (this.Alt) {
            s = "alt." + s;
        }
        if (this.Control) {
            s = "control." + s;
        }
        if (this.Shift) {
            s = "shift." + s;
        }
        if (this.CommandType > 0) {
            s = "esc" + this.CommandType + "." + s;
        }
        return s;
    }

    /**
     * Compute a visible shortcut to append after the menu items (like
     * "(Ctr O)". The modifiers depend on the language.
     */
    public String shortcut() {
        if (this.CharKey.equals("none")) {
            return "";
        }
        String s = this.CharKey.toUpperCase();
        if (this.Alt) {
            s = Global.name("shortcut.alt") + " " + s;
        }
        if (this.Control) {
            s = Global.name("shortcut.control") + " " + s;
        }
        if (this.Shift) {
            s = Global.name("shortcut.shift") + " " + s;
        }
        if (this.CommandType > 0) {
            s = Keyboard.commandShortcut(this.CommandType) + " " + s;
        }
        return s;
    }
}
