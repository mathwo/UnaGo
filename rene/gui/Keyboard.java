package rene.gui;

/*
 This file contains a keyboard translater. It translates key strokes
 into text strings. The strings are menu descriptions and key
 descriptions. Menu descriptions are used to call menu entries in
 EditorFrame, and key descriptions are used in TextDisplay.
 <p>
 JE supports up to 5 command keys, which may be prepended to other
 keys. Those keys are mapped to command.X, where X is from 1 to 5.
 There is also a special escape command key, mapped to command.escape.
 <p>
 Some strings are contained in the properties, others may be defined by
 the user, and are contained in the parameter file "je.cfg". There is
 also an editor for the keystrokes, which uses the ItemEditor dialog.
 */

import rene.dialogs.ItemEditor;
import rene.dialogs.MyFileDialog;
import rene.util.sort.Sorter;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * A static class, which contains tranlations for key events. It keeps the
 * translations in a vector of KeyboardItem.
 * <p>
 * The class will statically generate the list of translations from the default
 * local properties and the JE configuration.
 */

public class Keyboard {
    /**
     * Find a shortcut for the command.
     */
    public static String commandShortcut(int type) {
        final Object o = Keyboard.Hmenu.get("command." + type);
        if (o == null) {
            return "";
        }
        return ((KeyboardItem) o).shortcut();
    }

    /**
     * Edit the translations.
     */
    public static void edit(Frame f) {
        final KeyboardItem keys[] = new KeyboardItem[Keyboard.V.size()];
        Keyboard.V.copyInto(keys);
        Sorter.sort(keys);
        final Vector v = new Vector();
        for (int i = 0; i < keys.length; i++) {
            v.addElement(keys[i]);
        }
        final KeyboardPanel p = new KeyboardPanel();
        final ItemEditor d = new ItemEditor(f, p, v, "keyeditor",
                Global.name("keyeditor.prompt"), true, false, true,
                Global.name("keyeditor.clearall"));
        p.setItemEditor(d);
        p.makeCommandChoice();
        d.center(f);
        d.setVisible(true);
        if (d.isAborted()) {
            return;
        }
        Global.removeAllParameters("key.");
        Keyboard.V = d.getElements();
        Enumeration e = Keyboard.V.elements();
        while (e.hasMoreElements()) {
            final KeyboardItem k = (KeyboardItem) e.nextElement();
            if (!k.getCharKey().equals("default")) {
                final String keytag = "key." + k.getMenuString();
                final String description = k.keyDescription();
                if (!Global.name(keytag).toLowerCase().equals(description)) {
                    Global.setParameter(keytag, description);
                }
            }
        }
        Keyboard.makeKeys();
        if (d.getAction() == ItemEditor.SAVE) {
            final Properties parameters = new Properties();
            e = Global.properties();
            while (e.hasMoreElements()) {
                final String key = (String) e.nextElement();
                if (key.startsWith("key.")) {
                    parameters.put(key, Global.getParameter(key, "default"));
                }
            }
            final MyFileDialog save = new MyFileDialog(f, Global.name("save"),
                    Global.name("save"), true);
            save.setPattern("*.keys");
            save.center(f);
            save.update();
            save.setVisible(true);
            if (save.isAborted()) {
                return;
            }
            final String filename = save.getFilePath();
            if (filename.equals("")) {
                return; // aborted dialog!
            }
            try {
                final FileOutputStream o = new FileOutputStream(filename);
                parameters.store(o, "JE Keyboard Definition");
            } catch (final Exception ex) {
            }
        } else if (d.getAction() == ItemEditor.LOAD) {
            final Properties parameters = new Properties();
            final MyFileDialog load = new MyFileDialog(f, Global.name("load"),
                    Global.name("load"), true);
            load.setPattern("*.keys");
            load.center(f);
            load.update();
            load.setVisible(true);
            if (load.isAborted()) {
                return;
            }
            final String filename = load.getFilePath();
            if (filename.equals("")) {
                return; // aborted dialog!
            }
            try {
                final FileInputStream in = new FileInputStream(filename);
                parameters.load(in);
            } catch (final Exception ex) {
            }
            Global.removeAllParameters("key.");
            e = parameters.keys();
            while (e.hasMoreElements()) {
                final String key = (String) e.nextElement();
                Global.setParameter(key, (String) parameters.get(key));
            }
            Keyboard.makeKeys();
        }
    }

    /**
     * See, if the key event matches any of my translations, and get the menu
     * entry.
     */
    public static String findKey(KeyEvent event, int type) {
        final Object o = Keyboard.Hcharkey.get(Keyboard.toCharKey(event, type));
        if (o == null) {
            return "";
        }
        String s = ((KeyboardItem) o).getMenuString();
        while (s.endsWith("*")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * Find a menu string in the key definitions
     */
    public static KeyboardItem findMenu(String menu) {
        final Object o = Keyboard.Hmenu.get(menu);
        if (o == null) {
            return null;
        } else {
            return (KeyboardItem) o;
        }
    }

    /**
     * Append a list of keyboard shortcuts to a text area.
     */
    public static Vector getKeys() {
        final Vector keys = new Vector();
        Sorter.sort(Keyboard.V);
        final Enumeration e = Keyboard.V.elements();
        while (e.hasMoreElements()) {
            final KeyboardItem k = (KeyboardItem) e.nextElement();
            if (!k.getCharKey().equals("none")) {
                String shortcut = k.shortcut();
                final int n = shortcut.length();
                for (int i = 0; i < 30 - n; i++) {
                    shortcut = shortcut + " ";
                }
                keys.addElement(shortcut + " = " + k.getActionName());
            }
        }
        return keys;
    }

    /**
     * Read the keys from the Global names and parameters, and put into a vector
     * and two hash tables for easy access.
     */
    public static void makeKeys() {
        Keyboard.V = new Vector();
        Keyboard.Hmenu = new Hashtable();
        Keyboard.Hcharkey = new Hashtable();
        // collect all predefined keys
        Enumeration e = Global.names();
        if (e == null) {
            return;
        }
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            if (key.startsWith("key.")) {
                final String menu = key.substring(4);
                String charkey = Global.getParameter(key, "default");
                final String normalcharkey = Global.name(key);
                if (charkey.equals("default")) {
                    charkey = normalcharkey;
                }
                final KeyboardItem k = new KeyboardItem(menu, charkey);
                Keyboard.V.addElement(k);
                Keyboard.Hmenu.put(menu, k);
                Keyboard.Hcharkey.put(charkey, k);
            }
        }
        // collect all user defined (double defined) keys
        e = Global.properties();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            if (key.startsWith("key.")) {
                final String menu = key.substring(4);
                if (Keyboard.findMenu(menu) != null) {
                    continue;
                }
                final String charkey = Global.getParameter(key, "default");
                if (charkey.equals("default")) {
                    continue;
                }
                final KeyboardItem k = new KeyboardItem(menu, charkey);
                Keyboard.V.addElement(k);
                Keyboard.Hmenu.put(menu, k);
                Keyboard.Hcharkey.put(charkey, k);
            }
        }
    }

    /**
     * Generate a shortcut for the menu item.
     */
    public static String shortcut(String tag) {
        final Enumeration e = Keyboard.V.elements();
        while (e.hasMoreElements()) {
            final KeyboardItem item = (KeyboardItem) e.nextElement();
            if (item.getMenuString().equals(tag)) {
                String shortcut = item.shortcut();
                if (!shortcut.equals("")) {
                    shortcut = " (" + shortcut + ")";
                }
                return shortcut;
            }
        }
        return "";
    }

    /**
     * Make a keychar string from the event.
     */
    public static String toCharKey(KeyEvent e, int type) {
        String s = "";
        if (type > 0) {
            s = s + "esc" + type + ".";
        }
        if (e.isShiftDown()) {
            s = s + "shift.";
        }
        if (e.isControlDown()) {
            s = s + "control.";
        }
        if (e.isAltDown()) {
            s = s + "alt.";
        }
        return s + KeyDictionary.translate(e.getKeyCode()).toLowerCase();
    }

    static Vector V;

    static Hashtable Hmenu, Hcharkey;

    static {
        Keyboard.makeKeys();
    }
}
