package rene.gui;

import rene.dialogs.Warning;
import rene.util.FileName;
import rene.util.parser.StringParser;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * The Global class.
 * <p>
 * This class will load a resource bundle with local support. It will set
 * various things from this resource file.
 */

public class Global {
    public static synchronized void clearProperties() {
        Global.P = new Properties();
    }

    public static double convert(String s) {
        try {
            return Integer.parseInt(s);
        } catch (final Exception e) {
            return 0;
        }
    }

    static public void createEmbeddedFonts(int defsize)
    // Works only, if regular.ttf and bold.ttf are in the main directory of the
    // project.
    {
        try {
            InputStream in = new Object().getClass().getResourceAsStream(
                    "/regular.ttf");
            Font f = Font.createFont(Font.TRUETYPE_FONT, in);
            in.close();
            Global.FixedFont = f.deriveFont((float) defsize);
            in = new Object().getClass().getResourceAsStream("/bold.ttf");
            f = Font.createFont(Font.TRUETYPE_FONT, in);
            in.close();
            Global.BoldFont = f.deriveFont((float) defsize);
        } catch (final Exception e) {
        }
    }

    static public Font createfont(String name, String def, int defsize,
            boolean bold) {
        final String fontname = Global.getParameter(name + ".name", def);
        final String mode = Global.getParameter(name + ".mode", "plain");
        if (bold || mode.equals("bold")) {
            return new Font(fontname, Font.BOLD, Global.getParameter(name
                    + ".size", defsize));
        } else if (mode.equals("italic")) {
            return new Font(fontname, Font.ITALIC, Global.getParameter(name
                    + ".size", defsize));
        } else {
            return new Font(fontname, Font.PLAIN, Global.getParameter(name
                    + ".size", defsize));
        }
    }

    public static synchronized void exit(int i) {
        synchronized (Global.ExitBlock) {
            System.exit(i);
        }
    }

    public static double getJavaVersion() {
        final String version = System.getProperty("java.version");
        if (version == null) {
            return 0.0;
        }
        double v = 0.0;
        final StringTokenizer t = new StringTokenizer(version, ".");
        if (t.hasMoreTokens()) {
            v = Global.convert(t.nextToken());
        } else {
            return v;
        }
        if (t.hasMoreTokens()) {
            v = v + Global.convert(t.nextToken()) / 10;
        } else {
            return v;
        }
        if (t.hasMoreTokens()) {
            v = v + Global.convert(t.nextToken()) / 100;
        }
        return v;
    }

    public static synchronized boolean getParameter(String key, boolean def) {
        try {
            final String s = Global.P.getProperty(key);
            if (s.equals("true")) {
                return true;
            } else if (s.equals("false")) {
                return false;
            }
            return def;
        } catch (final Exception e) {
            return def;
        }
    }

    static public synchronized Color getParameter(String key, Color c) {
        final String s = Global.getParameter(key, "");
        if (s.equals("")) {
            return c;
        }
        int red = 0, green = 0, blue = 0;
        if (s.startsWith("#") && s.length() == 7) {
            red = Integer.parseInt(s.substring(1, 3), 16);
            green = Integer.parseInt(s.substring(3, 5), 16);
            blue = Integer.parseInt(s.substring(5, 7), 16);
        } else {
            final StringParser p = new StringParser(s);
            p.replace(',', ' ');
            red = p.parseint();
            green = p.parseint();
            blue = p.parseint();
        }
        try {
            return new Color(red, green, blue);
        } catch (final RuntimeException e) {
            return c;
        }
    }

    public static synchronized double getParameter(String key, double def) {
        try {
            return new Double(Global.getParameter(key, "")).doubleValue();
        } catch (final Exception e) {
            return def;
        }
    }

    public static synchronized int getParameter(String key, int def) {
        try {
            return Integer.parseInt(Global.getParameter(key, ""));
        } catch (final Exception e) {
            try {
                final double x = new Double(Global.getParameter(key, ""))
                .doubleValue();
                return (int) x;
            } catch (final Exception ex) {
            }
            return def;
        }
    }

    static public synchronized Color getParameter(String key, int red,
            int green, int blue) {
        final String s = Global.getParameter(key, "");
        if (s.equals("")) {
            return new Color(red, green, blue);
        }
        final StringParser p = new StringParser(s);
        p.replace(',', ' ');
        red = p.parseint();
        green = p.parseint();
        blue = p.parseint();
        try {
            return new Color(red, green, blue);
        } catch (final RuntimeException e) {
            return Color.black;
        }
    }

    public static synchronized String getParameter(String key, String def) {
        String res = def;
        try {
            res = Global.P.getProperty(key);
        } catch (final Exception e) {
        }
        if (res != null) {
            if (res.startsWith("$")) {
                res = res.substring(1);
            }
            return res;
        } else {
            return def;
        }
    }

    public static synchronized String getUserDir() {
        final String dir = System.getProperty("user.dir");
        return FileName.canonical(dir);
    }

    public static double getVersion() {
        String s = Global.getParameter("program.version", "0");
        final int pos = s.indexOf(" ");
        if (pos > 0) {
            s = s.substring(0, pos);
        }
        try {
            return new Double(s).doubleValue();
        } catch (final Exception e) {
        }
        return 0;
    }

    /**
     * @return if I have such a parameter.
     */
    public static synchronized boolean haveParameter(String key) {
        try {
            final String res = Global.P.getProperty(key);
            if (res == null) {
                return false;
            }
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    public static void initBundle(String file) {
        Global.initBundle(file, false);
    }

    public static void initBundle(String file, boolean language) {
        try {
            Global.B = ResourceBundle.getBundle(file);
            String lang = Global.getParameter("language", "");
            if (language && !lang.equals("") && !lang.equals("default")) {
                String langsec = "";
                if (lang.length() > 3 && lang.charAt(2) == '_') {
                    langsec = lang.substring(3);
                    lang = lang.substring(0, 2);
                }
                Locale.setDefault(new Locale(lang, langsec));
                Global.initBundle(file, false);
            }
        } catch (final RuntimeException e) {
            Global.B = null;
        }
    }

    static public boolean isApplet() {
        return Global.IsApplet;
    }

    public static synchronized void loadProperties(InputStream in) {
        try {
            Global.P = new Properties();
            Global.P.load(in);
            in.close();
        } catch (final Exception e) {
            Global.P = new Properties();
        }
    }

    public static synchronized boolean loadProperties(String filename) {
        Global.ConfigName = filename;
        try {
            final FileInputStream in = new FileInputStream(filename);
            Global.P = new Properties();
            Global.P.load(in);
            in.close();
        } catch (final Exception e) {
            Global.P = new Properties();
            return false;
        }
        return true;
    }

    public static synchronized void loadProperties(String dir, String filename) {
        try {
            final Properties p = System.getProperties();
            Global.ConfigName = dir + p.getProperty("file.separator")
                    + filename;
            Global.loadProperties(Global.ConfigName);
        } catch (final Exception e) {
            Global.P = new Properties();
        }
    }

    public static synchronized boolean loadPropertiesFromResource(
            String filename) {
        try {
            final Object G = new Object();
            final InputStream in = G.getClass().getResourceAsStream(filename);
            Global.P = new Properties();
            Global.P.load(in);
            in.close();
        } catch (final Exception e) {
            Global.P = new Properties();
            return false;
        }
        Global.ConfigName = filename;
        return true;
    }

    public static synchronized void loadPropertiesInHome(String filename) {
        try {
            final Properties p = System.getProperties();
            Global.loadProperties(p.getProperty("user.home"), filename);
        } catch (final Exception e) {
            Global.P = new Properties();
        }
    }

    public static void main(String args[]) {
        try {
            System.out.println(new Color(4, 5, 600));
        } catch (final RuntimeException e) {
        }
    }

    static public void makeColors() {
        if (Global.haveParameter("color.background")) {
            Global.Background = Global.getParameter("color.background",
                    Color.gray.brighter());
        } else {
            Global.Background = SystemColor.window;
        }
        if (Global.haveParameter("color.control")) {
            Global.ControlBackground = Global.getParameter("color.control",
                    Color.gray.brighter());
        } else {
            Global.ControlBackground = SystemColor.control;
        }
    }

    static public void makeFonts() {
        Global.NormalFont = Global.createfont("normalfont", "SansSerif", 12,
                false);
        Global.FixedFont = Global.createfont("fixedfont", "Monospaced", 12,
                false);
        Global.BoldFont = Global
                .createfont("fixedfont", "Monospaced", 12, true);
    }

    public static String name(String tag) {
        return Global.name(tag, tag.substring(tag.lastIndexOf(".") + 1));
    }

    public static String name(String tag, String def) {
        String s;
        if (Global.B == null) {
            return def;
        }
        try {
            s = Global.B.getString(tag);
        } catch (final Exception e) {
            s = def;
        }
        return s;
    }

    public static Enumeration names() {
        if (Global.B != null) {
            return Global.B.getKeys();
        } else {
            return null;
        }
    }

    public static synchronized Enumeration properties() {
        return Global.P.keys();
    }

    /**
     * Remove all Parameters that start with the string.
     */
    public static synchronized void removeAllParameters(String start) {
        final Enumeration e = Global.P.keys();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            if (key.startsWith(start)) {
                Global.P.remove(key);
            }
        }
    }

    /**
     * Remove a specific Paramater.
     */
    public static synchronized void removeParameter(String key) {
        Global.P.remove(key);
    }

    public static void resetDefaults() {
        Global.resetDefaults("default.");
    }

    /**
     * Set default values for parameters resetDefaults("default.") is the same
     * as: setParameter("xxx",getParameter("default.xxx","")); if "default.xxx"
     * has a value.
     *
     * @param defaults
     */
    public static synchronized void resetDefaults(String defaults) {
        final Enumeration e = Global.P.keys();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            if (key.startsWith(defaults)) {
                Global.setParameter(key.substring(defaults.length()),
                        Global.getParameter(key, ""));
            }
        }
    }

    public static synchronized void saveProperties(String text) {
        try {
            final FileOutputStream out = new FileOutputStream(Global.ConfigName);
            Global.P.store(out, text);
            out.close();
        } catch (final Exception e) {
        }
    }

    public static void saveProperties(String text, String filename) {
        Global.ConfigName = filename;
        Global.saveProperties(text);
    }

    static public void setApplet(boolean flag) {
        Global.IsApplet = flag;
    }

    public static synchronized void setParameter(String key, boolean value) {
        if (Global.P == null) {
            return;
        }
        if (value) {
            Global.P.put(key, "true");
        } else {
            Global.P.put(key, "false");
        }
    }

    public static synchronized void setParameter(String key, Color c) {
        Global.setParameter(key,
                "" + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
    }

    public static synchronized void setParameter(String key, double value) {
        Global.setParameter(key, "" + value);
    }

    public static synchronized void setParameter(String key, int value) {
        Global.setParameter(key, "" + value);
    }

    public static synchronized void setParameter(String key, String value) {
        if (Global.P == null) {
            return;
        }
        if (value.length() > 0 && Character.isSpaceChar(value.charAt(0))) {
            value = "$" + value;
        }
        Global.P.put(key, value);
    }

    // Warnings

    public static void warning(Frame f, String s) {
        final Warning W = new Warning(f, s, Global.name("warning"), false);
        W.center(f);
        W.setVisible(true);
    }

    public static void warning(String s) {
        if (Global.F == null) {
            Global.F = new Frame();
        }
        final Warning W = new Warning(Global.F, s, Global.name("warning"),
                false);
        W.center();
        W.setVisible(true);
    }

    // Fonts:
    static public Font NormalFont = null, FixedFont = null, BoldFont = null;

    static {
        Global.makeFonts();
    }

    static public Color Background = null, ControlBackground = null;

    static {
        Global.makeColors();
    }

    // Resources:
    static protected ResourceBundle B;

    // Java Version

    // Properties:
    static Properties P = new Properties();

    static String ConfigName;

    static Frame F = null;

    // Clipboard for applets
    static public String AppletClipboard = null;

    static public boolean IsApplet = false;

    public static Object ExitBlock = new Object();
}
