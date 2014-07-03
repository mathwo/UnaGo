package unagoclient;

import unagoclient.igs.MessageFilter;
import rene.util.list.ListClass;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;

/**
 * This class stores global parameters. It is equivalent to global parameters in
 * a non-OO programming environment.
 * <p>
 * The most important class of parameters is a list of keys and values. It is
 * implemented as a hash table. Paremeters can be strings, integers, boolean
 * values and colors. Function keys are stored as strings with key f1 etc.
 * <p>
 * Another global variable is the start directory of UnaGo, where the program
 * expects its help files, unless the -home parameter is used. Also the URL for
 * the WWW applet version is stored here. The parameters are retrieved from that
 * URL, if applicable.
 * <p>
 * There is static RessourceBundle, which contains the label names etc. in local
 * versions. The file is "UnaGoResource.properties" (or
 * "UnaGoResource_de.properties" and similar).
 */

public class Global extends rene.gui.Global {
    /**
     * look up, if that string filters as blocking
     */
    public static int blocks(String s) {
        return Global.MF.blocks(s);
    }

    static Font createboldfont(String name, String def, String size, int sdef) {
        name = rene.gui.Global.getParameter(name, def);
        return new Font(name, Font.BOLD, rene.gui.Global.getParameter(size,
                sdef));
    }

    static Font createfont(String name, String def, String size, int sdef) {
        name = rene.gui.Global.getParameter(name, def);
        if (name.startsWith("Bold")) {
            return new Font(name.substring(4), Font.BOLD,
                    rene.gui.Global.getParameter(size, sdef));
        } else if (name.startsWith("Italic")) {
            return new Font(name.substring(5), Font.ITALIC,
                    rene.gui.Global.getParameter(size, sdef));
        } else {
            return new Font(name, Font.PLAIN, rene.gui.Global.getParameter(
                    size, sdef));
        }
    }

    /**
     * create the user chosen fonts
     */
    public static void createfonts() {
        Global.SansSerif = Global.createfont("sansserif", "SansSerif",
                "ssfontsize", 12);
        Global.Monospaced = Global.createfont("monospaced", "Monospaced",
                "msfontsize", 12);
        Global.BigMonospaced = Global.createfont("bigmonospaced",
                "BoldMonospaced", "bigmsfontsize", 22);
        Global.BoardFont = Global.createboldfont("boardfontname", "SansSerif",
                "boardfontsize", 11);
    }

    /**
     * create an image
     */
    public static Image createImage(int w, int h) {
        return Global.C.createImage(w, h);
    }

    /**
     * get the current directory
     */
    public static String dir() {
        return Global.Dir;
    }

    /**
     * set the current directory
     */
    public static void dir(String dir) {
        if (Global.isApplet()) {
            Global.Dir = dir + "\\";
        } else {
            Global.Dir = dir + System.getProperty("file.separator");
        }
    }

    /**
     * get the default frame
     */
    public static Frame frame() {
        if (Global.F == null) {
            Global.F = new Frame();
        }
        return Global.F;
    }

    /**
     * set a default invisible frame
     */
    public static void frame(Frame f) {
        Global.F = f;
    }

    /**
     * getParameter for colors
     */
    public static Color getColor(String a, Color c) {
        return rene.gui.Global.getParameter(a, c);
    }

    /**
     * getParameter for color values
     */
    public static Color getColor(String a, int red, int green, int blue) {
        return rene.gui.Global.getParameter(a, red, green, blue);
    }

    /**
     * Helper function for correctly open a stream to either an URL or a file in
     * the current directory. URLs are used, when the applet starts from a
     * server. If the opening fails, a ressource in the / (root) directory is
     * tried. This allows for overwriting resources with local files.
     */
    public static InputStream getDataStream(String filename) {
        try {
            if (Global.useurl()) {
                return new URL(Global.url(), filename).openStream();
            } else {
                return new FileInputStream(Global.home() + filename);
            }
        } catch (final Exception e) {
            final Object G = new GlobalObject();
            return G.getClass().getResourceAsStream("/" + filename);
        }
    }

    public static BufferedReader getEncodedStream(String filename) { // String
        // encoding=getParameter("HELP_ENCODING","");
        final String encoding = Global.resourceString("HELP_ENCODING");
        if (encoding.equals("")) {
            return Global.getStream(filename);
        } else {
            return Global.getStream(filename, encoding);
        }
    }

    /**
     * get the string belonging to a function key
     */
    public static String getFunctionKey(int key) {
        int i = 0;
        switch (key) {
            case KeyEvent.VK_F1:
                i = 1;
                break;
            case KeyEvent.VK_F2:
                i = 2;
                break;
            case KeyEvent.VK_F3:
                i = 3;
                break;
            case KeyEvent.VK_F4:
                i = 4;
                break;
            case KeyEvent.VK_F5:
                i = 5;
                break;
            case KeyEvent.VK_F6:
                i = 6;
                break;
            case KeyEvent.VK_F7:
                i = 7;
                break;
            case KeyEvent.VK_F8:
                i = 8;
                break;
            case KeyEvent.VK_F9:
                i = 9;
                break;
            case KeyEvent.VK_F10:
                i = 10;
                break;
        }
        if (i == 0) {
            return "";
        }
        return rene.gui.Global.getParameter("f" + i, "");
    }

    public static BufferedReader getStream(String filename) {
        return new BufferedReader(new InputStreamReader(
                Global.getDataStream(filename)));
    }

    public static BufferedReader getStream(String filename, String encoding) {
        try {
            return new BufferedReader(new InputStreamReader(
                    Global.getDataStream(filename), encoding));
        } catch (final UnsupportedEncodingException e) {
            return Global.getStream(filename);
        }
    }

    /**
     * get the home directory
     */
    public static String home() {
        return Global.Home;
    }

    /**
     * set the home directory
     */
    public static void home(String dir) {
        if (Global.isApplet()) {
            Global.Home = dir + "\\";
        } else {
            Global.Home = dir + System.getProperty("file.separator");
        }
    }

    public static boolean isApplet() {
        return Global.IsApplet;
    }

    /**
     * load the message filters
     */
    public static void loadmessagefilter() {
        Global.MF = new MessageFilter();
    }

    /**
     * note the size of a window in go.cfg.
     */
    public static void notewindow(Component c, String name) {
        rene.gui.Global.setParameter(name + "width", c.getSize().width);
        rene.gui.Global.setParameter(name + "height", c.getSize().height);
        rene.gui.Global.setParameter(name + "xpos", c.getLocation().x);
        rene.gui.Global.setParameter(name + "ypos", c.getLocation().y);
    }

    /**
     * look up, if that string filters as positive filter
     */
    public static boolean posfilter(String s) {
        return Global.MF.posfilter(s);
    }

    /**
     * Read the paramters from a file (normally go.cfg). This method uses
     * getStream to either open an URL, a local file or a resource.
     * <p>
     * If there is the language parameter, a new resource bundle with that
     * locale is loaded.
     */
    public static void readparameter(String filename) {
        new File(Global.home() + filename);
        rene.gui.Global.loadProperties(Global.getDataStream(filename));
        String lang = rene.gui.Global.getParameter("language", "");
        if (!lang.equals("")) {
            String langsec = "";
            if (lang.length() > 3 && lang.charAt(2) == '_') {
                langsec = lang.substring(3);
                lang = lang.substring(0, 2);
            }
            Locale.setDefault(new Locale(lang, langsec));
            rene.gui.Global.initBundle("unagoclient/foreign/UnaGoResource");
            if (rene.gui.Global.B == null) {
                rene.gui.Global.initBundle("unagoclient/UnaGoResource");
            }
            if (rene.gui.Global.B == null) {
                rene.gui.Global.initBundle("UnaGoResource");
            }
        }
    }

    /**
     * Get the national translation fot the string s. The resource strings
     * contain _ instead of blanks. If the resource is not found, the strings s
     * (with _ replaced by blanks) will be used.
     */
    public static String resourceString(String s) {
        String res;
        s = s.replace(' ', '_');
        res = rene.gui.Global.name(s, "???");
        if (res.equals("???")) {
            res = s.replace('_', ' ');
            if (res.endsWith(" n")) {
                res = res.substring(0, res.length() - 2) + "\n";
            }
        }
        return res;
    }

    public static void saveMessageFilter() {
        if (Global.MF != null) {
            Global.MF.save();
        }
    }

    public static void setApplet(boolean flag) {
        Global.IsApplet = flag;
    }

    /**
     * setParameter for colors
     */
    public static void setColor(String a, Color c) {
        rene.gui.Global.setParameter(a, c);
    }

    /**
     * sets a global component for getting images and such
     */
    public static void setcomponent(Component c) {
        Global.C = c;
    }

    /**
     * Same as setwindow(Dialog,...), but packs the dialog.
     */
    public static void setpacked(Dialog c, String name, int w, int h, Frame f) {
        int x = f.getLocation().x + f.getSize().width / 2 - c.getSize().width
                / 2, y = f.getLocation().y + f.getSize().height / 2
                - c.getSize().height / 2;
        w = rene.gui.Global.getParameter(name + "width", w);
        h = rene.gui.Global.getParameter(name + "height", h);
        final Dimension d = c.getToolkit().getScreenSize();
        if (w > d.width) {
            w = d.width;
        }
        if (h > d.height) {
            h = d.height;
        }
        if (x + w > d.width) {
            x = d.width - w;
        }
        if (x < 0) {
            x = 0;
        }
        if (y + h > d.height) {
            y = d.height - h;
        }
        if (y < 0) {
            y = 0;
        }
        if (rene.gui.Global.getParameter("pack", true)) {
            c.pack();
        } else {
            c.setSize(w, h);
        }
        c.setLocation(x, y);
    }

    /**
     * Same as setwindow(Dialog,...), but packs the dialog.
     */
    public static void setpacked(Frame c, String name, int w, int h, Frame f) {
        int x = f.getLocation().x + f.getSize().width / 2 - c.getSize().width
                / 2, y = f.getLocation().y + f.getSize().height / 2
                - c.getSize().height / 2;
        w = rene.gui.Global.getParameter(name + "width", w);
        h = rene.gui.Global.getParameter(name + "height", h);
        final Dimension d = c.getToolkit().getScreenSize();
        if (w > d.width) {
            w = d.width;
        }
        if (h > d.height) {
            h = d.height;
        }
        if (x + w > d.width) {
            x = d.width - w;
        }
        if (x < 0) {
            x = 0;
        }
        if (y + h > d.height) {
            y = d.height - h;
        }
        if (y < 0) {
            y = 0;
        }
        if (rene.gui.Global.getParameter("pack", true)) {
            c.pack();
        } else {
            c.setSize(w, h);
        }
        c.setLocation(x, y);
    }

    /**
     * Same as setwindow, but the window will be packed, if the pack parameter
     * is set (advanced options).
     */
    public static void setpacked(Window c, String name, int w, int h) {
        final Dimension d = c.getToolkit().getScreenSize();
        int x = d.width / 2, y = d.height / 2;
        x = rene.gui.Global.getParameter(name + "xpos", 100);
        y = rene.gui.Global.getParameter(name + "ypos", 100);
        w = rene.gui.Global.getParameter(name + "width", w);
        h = rene.gui.Global.getParameter(name + "height", h);
        if (w > d.width) {
            w = d.width;
        }
        if (h > d.height) {
            h = d.height;
        }
        if (x + w > d.width) {
            x = d.width - w;
        }
        if (x < 0) {
            x = 0;
        }
        if (y + h > d.height) {
            y = d.height - h;
        }
        if (y < 0) {
            y = 0;
        }
        if (rene.gui.Global.getParameter("pack", true)) {
            c.pack();
            c.setLocation(x, y);
        } else {
            c.setBounds(x, y, w, h);
        }
    }

    /**
     * Places a dialog nicely centered with a frame.
     */
    public static void setwindow(Dialog c, String name, int w, int h, Frame f) {
        int x = f.getLocation().x + f.getSize().width / 2 - c.getSize().width
                / 2, y = f.getLocation().y + f.getSize().height / 2
                - c.getSize().height / 2;
        w = rene.gui.Global.getParameter(name + "width", w);
        h = rene.gui.Global.getParameter(name + "height", h);
        final Dimension d = c.getToolkit().getScreenSize();
        if (w > d.width) {
            w = d.width;
        }
        if (h > d.height) {
            h = d.height;
        }
        if (x + w > d.width) {
            x = d.width - w;
        }
        if (x < 0) {
            x = 0;
        }
        if (y + h > d.height) {
            y = d.height - h;
        }
        if (y < 0) {
            y = 0;
        }
        c.setBounds(x, y, w, h);
    }

    public static void setwindow(Window c, String name, int w, int h) {
        Global.setwindow(c, name, w, h, true);
    }

    /**
     * Set the window sizes as read from go.cfg. The paramter tags are made from
     * the window name and "ypos", "xpos", "width" or "height".
     */
    public static void setwindow(Window c, String name, int w, int h,
            boolean minsize) {
        int x = rene.gui.Global.getParameter(name + "xpos", 100), y = rene.gui.Global
                .getParameter(name + "ypos", 100);
        w = rene.gui.Global.getParameter(name + "width", w);
        h = rene.gui.Global.getParameter(name + "height", h);
        if (minsize) {
            c.pack();
            final Dimension dmin = c.getSize();
            if (dmin.width > w) {
                w = dmin.width;
            }
            if (dmin.height > h) {
                h = dmin.height;
            }
        }
        final Dimension d = c.getToolkit().getScreenSize();
        if (w > d.width) {
            w = d.width;
        }
        if (h > d.height) {
            h = d.height;
        }
        if (x + w > d.width) {
            x = d.width - w;
        }
        if (x < 0) {
            x = 0;
        }
        if (y + h > d.height) {
            y = d.height - h;
        }
        if (y < 0) {
            y = 0;
        }
        c.pack();
        c.setBounds(x, y, w, h);
    }

    /**
     * @return the used URL
     */
    public static URL url() {
        return Global.Url;
    }

    /**
     * set the used url
     */
    public static void url(URL url) {
        Global.Url = url;
        Global.UseUrl = true;
        Global.IsApplet = true;
    }

    /**
     * @return Flag, if URL is used
     */
    public static boolean useurl() {
        return Global.UseUrl;
    }

    public static void version51() {
        Global.version51handle("go.cfg");
        Global.version51handle("server.cfg");
        Global.version51handle("partner.cfg");
        Global.version51handle("filter.cfg");
    }

    public static void version51handle(String s) {
        final File f = new File(Global.home() + s);
        final File f1 = new File(Global.home() + "." + s);
        if (f.exists()) {
            if (f1.exists()) {
                f.delete();
            } else {
                f.renameTo(f1);
            }
        }
    }

    /**
     * write the parmeter to the parameter file (normally go.cfg)
     */
    public static void writeparameter(String filename) {
        if (Global.isApplet()) {
            return;
        }
        rene.gui.Global.saveProperties("UnaGo Properties", Global.home()
                + ".go.cfg");
    }

    public static Component C;

    public static String Dir, Home;

    public static Frame F;

    public static MessageFilter MF = null;

    public static ListClass PartnerList = null;

    public static ListClass OpenPartnerList = null;

    public static boolean UseUrl = false;

    public static boolean IsApplet = false;

    public static URL Url;

    public static boolean Busy = true;

    public static Font SansSerif, Monospaced, BigMonospaced, BoardFont;

    public static Hashtable WindowList;

    public static int Silent = 0;

    /** initialze the UnaGo Ressource bundle */
    static {
        Global.WindowList = new Hashtable();
        Global.Dir = "";
        Global.Home = "";
        rene.gui.Global.initBundle("unagoclient/foreign/UnaGoResource");
        if (rene.gui.Global.B == null) {
            rene.gui.Global.initBundle("unagoclient/UnaGoResource");
        }
        if (rene.gui.Global.B == null) {
            rene.gui.Global.initBundle("UnaGoResource");
        }
    }
}

class GlobalObject {
}
