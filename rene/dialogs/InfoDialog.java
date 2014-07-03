/*
 * Created on 01.11.2004
 *
 */
package rene.dialogs;

import rene.gui.*;
import rene.viewer.ExtendedViewer;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Info class. Reads a file "info.txt" or "de_info.txt" etc. in its Base
 * directory. This file has the structure
 * <p>
 * .subject1 substitute1 substitute2 ... .related subject subject ... Header ...
 * <p>
 * .subject2 ...
 * <p>
 * and displays the text, starting from header, searching for a subject or any
 * of its substitutes. The headers of the related subjects are presented in a
 * choice list. The user can switch to any of it.
 * <p>
 * There is a history and a back button.
 * <p>
 * Moroever, there is a search button, that displays the first subject
 * containing a string and presents all other subjects containing the string in
 * the choice list.
 * <p>
 * This class is used in CloseDialog to provide help in dialogs. It is a
 * CloseDialog itself.
 *
 * @see: rene.gui.CloseDialog
 */

public class InfoDialog extends CloseDialog implements ItemListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ExtendedViewer V;
    Button Close, Back;
    public static String Subject = "start";
    String Search = null;

    MyChoice L;
    Vector Other = null;
    Vector History = new Vector();
    public static String Base = ""; // path to your
                                    // info.txt

    Frame F;

    /**
     * Sets up the dialog with title, search bar and text area. The size will be
     * remembered with "info" in the configuration (see: Global). The dialog
     * will be centered on its frame.
     *
     * @param f
     */
    public InfoDialog(Frame f) {
        super(f, Global.name("info.title"), true);
        this.F = f;
        this.V = new ExtendedViewer();
        if (Global.Background != null) {
            this.V.setBackground(Global.Background);
        }
        this.V.setFont(Global.NormalFont);
        this.setLayout(new BorderLayout());
        final Panel north = new MyPanel();
        north.setLayout(new GridLayout(0, 2));
        north.add(new MyLabel(Global.name("info.related")));
        this.L = new MyChoice();
        this.fill();
        north.add(this.L);
        this.add("North", north);
        this.add("Center", this.V);
        final Panel p = new MyPanel();
        p.add(new ButtonAction(this, Global.name("info.start"), "Start"));
        p.add(new ButtonAction(this, Global.name("info.search"), "Search"));
        p.add(new ButtonAction(this, Global.name("info.back"), "Back"));
        p.add(new MyLabel(""));
        p.add(new ButtonAction(this, Global.name("close", "Close"), "Close"));
        this.add("South", new Panel3D(p));
        this.L.addItemListener(this);
        this.pack();
        this.setSize("info");
        this.center(f);
        this.setVisible(true);
    }

    public String clear(String s) {
        s = s.replace('�', ' ');
        s = s.replaceAll("__", "");
        return s;
    }

    @Override
    public void doAction(String o) {
        if (o.equals("Close")) {
            super.doAction("Close");
        } else if (o.equals("Back")) {
            final int n = this.History.size();
            if (n < 2) {
                return;
            }
            this.History.removeElementAt(n - 1);
            InfoDialog.Subject = (String) this.History.elementAt(n - 2);
            this.History.removeElementAt(n - 2);
            this.fill();
        } else if (o.equals("Start")) {
            InfoDialog.Subject = "start";
            this.fill();
        } else if (o.equals("Search")) {
            GetParameter.InputLength = 50;
            final GetParameter g = new GetParameter(this.F,
                    Global.name("info.title"), Global.name("info.search"),
                    Global.name("info.search", "ok"));
            g.center(this.F);
            g.setVisible(true);
            if (!g.aborted()) {
                this.Search = g.getResult();
            }
            this.fill();
            this.Search = null;
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void doclose() {
        this.noteSize("info");
        super.doclose();
    }

    public void fill() {
        this.fill(Global.name("language", ""));
    }

    /**
     * A complicated function, that scans the current help file for topics,
     * related to the current subject. The proper language (e.g. "de_") are read
     * from the language property in Global. E.g., the file file is read from
     * "de_info.txt". The proper encoding of the file (e.g. "CP1552) is read
     * from the "codepage.help" property in Global.
     */
    public void fill(String language) {
        this.L.removeAll();
        this.V.setText("");
        this.V.setVisible(false);
        boolean Found = false, Appending = false;
        Vector Related = null;
        this.Other = new Vector();
        String pair[] = null, lastpair[] = null;
        String lang = language;
        Vector SearchResults = new Vector();
        String SearchResult = "";
        String FoundTopic = null;
        boolean FirstRun = true, FoundHeader = false;

        String Search1 = this.Search;
        if (this.Search != null && this.Search.length() > 0) {
            Search1 = this.Search.substring(0, 1).toUpperCase()
                    + this.Search.substring(1);
        }

        read: while (true) {
            try { // open the info file in the proper language and encoding.
                final String cp = Global.name("codepage.help", ""); // get
                // encoding.
                BufferedReader in = null;
                // System.out.println("Try "+Base+lang+"info.txt");
                if (cp.equals("")) {
                    in = new BufferedReader(new InputStreamReader(this
                            .getClass().getResourceAsStream(
                                    InfoDialog.Base + lang + "info.txt")));
                } else {
                    try {
                        in = new BufferedReader(new InputStreamReader(this
                                .getClass().getResourceAsStream(
                                        InfoDialog.Base + lang + "info.txt"),
                                        cp));
                    } catch (final Exception ex) {
                        in = new BufferedReader(new InputStreamReader(this
                                .getClass().getResourceAsStream(
                                        InfoDialog.Base + lang + "info.txt")));
                    }
                }
                // System.out.println("Opened "+Base+lang+"info.txt");
                // read through the file line by line:
                newline: while (true) {
                    String s = in.readLine();
                    if (s == null) {
                        break newline;
                    }
                    s = this.clear(s);
                    if (!s.startsWith(".")
                            && this.Search != null
                            && (s.indexOf(this.Search) >= 0 || s
                            .indexOf(Search1) >= 0)) {
                        if (lastpair != null && pair == null
                                && !SearchResult.equals(lastpair[0])) {
                            SearchResults.addElement(lastpair);
                            SearchResult = lastpair[0];
                            if (FoundTopic == null) {
                                FoundTopic = lastpair[0];
                            }
                        }
                    }
                    interpret: while (true) {
                        if (!Appending && s.startsWith(".")
                                && !s.startsWith(".related")) {
                            if (!Found) {
                                if (s.startsWith("." + InfoDialog.Subject)) {
                                    Found = true;
                                    Appending = true;
                                    continue newline;
                                }
                                final StringTokenizer t = new StringTokenizer(s);
                                while (t.hasMoreElements()) {
                                    final String name = t.nextToken();
                                    if (name.equals(InfoDialog.Subject)) {
                                        Found = true;
                                        Appending = true;
                                        continue newline;
                                    }
                                }
                            }
                            pair = new String[2];
                            s = s.substring(1);
                            final int n = s.indexOf(' ');
                            if (n > 0) {
                                s = s.substring(0, n);
                            }
                            pair[0] = s;
                            continue newline;
                        }
                        if (Appending) {
                            if (s.startsWith(".related")) {
                                s = s.substring(".related".length());
                                Related = new Vector();
                                final StringTokenizer t = new StringTokenizer(s);
                                while (t.hasMoreElements()) {
                                    Related.addElement(t.nextToken());
                                }
                                continue newline;
                            }
                            if (s.startsWith(".")) {
                                Appending = false;
                                continue interpret;
                            }
                            if (s.trim().equals("")) {
                                this.V.newLine();
                                this.V.appendLine("");
                            } else {
                                if (s.startsWith(" ")) {
                                    this.V.newLine();
                                }
                                this.V.append(s + " ");
                            }
                        } else if (pair != null && !s.startsWith(".")) {
                            pair[1] = s;
                            this.Other.addElement(pair);
                            lastpair = pair;
                            pair = null;
                            if (this.Search != null
                                    && (s.indexOf(this.Search) >= 0 || s
                                    .indexOf(Search1) >= 0)) {
                                if (!SearchResult.equals(lastpair[0])) {
                                    SearchResults.addElement(lastpair);
                                    SearchResult = lastpair[0];
                                    if (!FoundHeader) {
                                        FoundTopic = lastpair[0];
                                    }
                                    FoundHeader = true;
                                }
                            }
                        }
                        continue newline;
                    }
                }
                this.V.newLine();
                in.close();
            } catch (final Exception e) {
                if (!lang.equals("")) {
                    lang = "";
                    continue read;
                } else {
                    this.V.appendLine(Global.name("help.error",
                            "Could not find the help file!"));
                }
            }
            if (FoundTopic != null && FirstRun) {
                InfoDialog.Subject = FoundTopic;
                SearchResults = new Vector();
                SearchResult = "";
                pair = null;
                lastpair = null;
                Found = false;
                this.V.setText("");
                FirstRun = false;
                continue read;
            } else if (!Found && !lang.equals("")) {
                lang = "";
                continue read;
            } else {
                break read;
            }
        }

        if (this.Search != null) {
            if (SearchResults.size() > 0) {
                this.L.add(Global.name("info.searchresults"));
            } else {
                this.L.add(Global.name("info.noresults"));
            }
        } else {
            this.L.add(Global.name("info.select"));
        }

        if (this.Search == null && Related != null) {
            final Enumeration e = Related.elements();
            while (e.hasMoreElements()) {
                final String topic = (String) e.nextElement();
                final Enumeration ev = this.Other.elements();
                while (ev.hasMoreElements()) {
                    final String s[] = (String[]) ev.nextElement();
                    if (s[0].equals(topic)) {
                        this.L.add(s[1]);
                        break;
                    }
                }
            }
        }

        if (this.Search != null) {
            final Enumeration e = SearchResults.elements();
            while (e.hasMoreElements()) {
                final String s[] = (String[]) e.nextElement();
                this.L.add(s[1]);
            }
        }

        this.History.addElement(InfoDialog.Subject);
        this.V.update();
        this.V.setVisible(true);
        this.V.showFirst();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        final String s = this.L.getSelectedItem();
        final Enumeration ev = this.Other.elements();
        while (ev.hasMoreElements()) {
            final String p[] = (String[]) ev.nextElement();
            if (p[1].equals(s)) {
                InfoDialog.Subject = p[0];
                this.fill();
                break;
            }
        }
    }
}
