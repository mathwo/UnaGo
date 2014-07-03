package rene.gui;

import java.awt.event.KeyEvent;
import java.util.Hashtable;

/**
 * This servers as a dictionary to make sure that the key translation will work
 * on localized systems too. The key recognition depends on the text translation
 * in KeyEvent. For user defined keyboards this will not matter, but this class
 * makes sure that it does not matter for the default keyboard.
 */

class KeyDictionary {
    static void put(int code, String name) {
        KeyDictionary.H.put(new Integer(code), name);
    }

    static String translate(int code) {
        final Object o = KeyDictionary.H.get(new Integer(code));
        if (o != null) {
            return (String) o;
        }
        return KeyEvent.getKeyText(code);
    }

    static Hashtable H;

    static {
        KeyDictionary.H = new Hashtable(100);
        KeyDictionary.put(KeyEvent.VK_F1, "f1");
        KeyDictionary.put(KeyEvent.VK_F2, "f2");
        KeyDictionary.put(KeyEvent.VK_F3, "f3");
        KeyDictionary.put(KeyEvent.VK_F4, "f4");
        KeyDictionary.put(KeyEvent.VK_F5, "f5");
        KeyDictionary.put(KeyEvent.VK_F6, "f6");
        KeyDictionary.put(KeyEvent.VK_F7, "f7");
        KeyDictionary.put(KeyEvent.VK_F8, "f8");
        KeyDictionary.put(KeyEvent.VK_F9, "f9");
        KeyDictionary.put(KeyEvent.VK_F10, "f10");
        KeyDictionary.put(KeyEvent.VK_F11, "f11");
        KeyDictionary.put(KeyEvent.VK_F12, "f12");
        KeyDictionary.put(KeyEvent.VK_LEFT, "left");
        KeyDictionary.put(KeyEvent.VK_RIGHT, "right");
        KeyDictionary.put(KeyEvent.VK_DOWN, "down");
        KeyDictionary.put(KeyEvent.VK_UP, "up");
        KeyDictionary.put(KeyEvent.VK_PAGE_DOWN, "page down");
        KeyDictionary.put(KeyEvent.VK_PAGE_UP, "page up");
        KeyDictionary.put(KeyEvent.VK_DELETE, "delete");
        KeyDictionary.put(KeyEvent.VK_BACK_SPACE, "backspace");
        KeyDictionary.put(KeyEvent.VK_INSERT, "insert");
        KeyDictionary.put(KeyEvent.VK_HOME, "home");
        KeyDictionary.put(KeyEvent.VK_END, "end");
        KeyDictionary.put(KeyEvent.VK_ESCAPE, "escape");
        KeyDictionary.put(KeyEvent.VK_TAB, "tab");
        KeyDictionary.put(KeyEvent.VK_ENTER, "enter");
    }
}
