package unagoclient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * A class to generate debug information in a dump file. It is a class with all
 * static members.
 */

public class Dump {
    /**
     * close the dump file
     */
    public static void close() {
        if (Dump.Out != null) {
            Dump.Out.close();
        }
    }

    /**
     * Open a dump file. If this is not called there will be no file dumps.
     */
    public static void open(String file) {
        try {
            Dump.Out = new PrintWriter(new FileOutputStream(file), true);
            Dump.Out.println("Locale: " + Locale.getDefault() + "\n");
        } catch (final IOException e) {
            Dump.Out = null;
        }
    }

    /**
     * dump a string without linefeed
     */
    public static void print(String s) {
        if (Dump.Out != null) {
            Dump.Out.print(s);
        }
        if (Dump.Terminal) {
            System.out.print(s);
        }
    }

    /**
     * dump a string in a line
     */
    public static void println(String s) {
        if (Dump.Out != null) {
            Dump.Out.println(s);
        }
        if (Dump.Terminal) {
            System.out.println(s);
        }
    }

    /**
     * determine terminal dumps or not
     */
    public static void terminal(boolean flag) {
        Dump.Terminal = flag;
    }

    static PrintWriter Out = null;

    static boolean Terminal = false;
}
