package rene.dialogs;

import rene.gui.*;
import rene.util.FileList;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.Enumeration;

class FileListFinder extends FileList {
    String Res;

    public FileListFinder(String dir, String pattern, boolean recurse) {
        super(dir, pattern, recurse);
    }

    @Override
    public boolean file(File file) {
        try {
            this.Res = file.getCanonicalPath();
        } catch (final Exception e) {
        }
        return false;
    }

    public String getResult() {
        return this.Res;
    }
}

/**
 * This is a dialog to search a subtree for a specific file. The user can enter
 * a directory and a file pattern containing * and ?. He can choose between
 * immediate search and open, or search/select/open. Abort will result in an
 * empty string. The calling routine checks the result file name with
 * getResult().
 * <p>
 * You need to specify the following properties
 * <p>
 *
 * <pre>
 * searchfile.title=Search File
 * searchfile.directory=Directory
 * searchfile.pattern=Pattern
 * searchfile.search=Search
 * searchfile.searchrek=Search Subdirectories
 * </pre>
 */

public class SearchFileDialog extends CloseDialog implements Runnable,
Enumeration {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    HistoryTextField Dir, Pattern;
    MyList L;
    static public int ListNumber = Global.getParameter("searchfile.number", 10);
    String Result = null;
    Button ActionButton, CloseButton, SearchButton, SearchrekButton;
    public FileList F = null;
    public boolean Abort = true;
    Checkbox Mod;

    Thread Run;

    String S[];

    int Sn;

    public SearchFileDialog(Frame f, String action) {
        this(f, action, "", false);
    }

    public SearchFileDialog(Frame f, String action, String modify,
            boolean modifystate) {
        super(f, Global.name("searchfile.title"), true);
        this.setLayout(new BorderLayout());
        final Panel north = new MyPanel();
        north.setLayout(new BorderLayout());
        final Panel northa = new MyPanel();
        northa.setLayout(new BorderLayout());
        final Panel north1 = new MyPanel();
        north1.setLayout(new GridLayout(0, 2));
        north1.add(new MyLabel(Global.name("searchfile.directory")));
        north1.add(this.Dir = new HistoryTextField(this, "Dir", 20));
        this.Dir.setText(".");
        north1.add(new MyLabel(Global.name("searchfile.pattern")));
        north1.add(this.Pattern = new HistoryTextField(this, "TextAction", 20));
        northa.add("Center", north1);
        final Panel north2 = new MyPanel();
        north2.add(this.SearchButton = new ButtonAction(this, Global
                .name("searchfile.search"), "Search"));
        north2.add(this.SearchrekButton = new ButtonAction(this, Global
                .name("searchfile.searchrek"), "SearchRek"));
        northa.add("South", north2);
        north.add("North", northa);
        this.add("North", new Panel3D(north));
        this.add("Center", new Panel3D(this.L = new MyList(
                SearchFileDialog.ListNumber)));
        this.L.addActionListener(this);
        this.L.setMultipleMode(true);
        final Panel south = new MyPanel();
        south.setLayout(new FlowLayout(FlowLayout.RIGHT));
        if (!modify.equals("")) {
            south.add(this.Mod = new CheckboxAction(this, modify, ""));
            this.Mod.setState(modifystate);
        }
        south.add(this.ActionButton = new ButtonAction(this, action, "Action"));
        south.add(this.CloseButton = new ButtonAction(this, Global
                .name("abort"), "Close"));
        this.add("South", new Panel3D(south));
        this.pack();
        this.Dir.loadHistory("searchfile.dir");
        this.Pattern.loadHistory("searchfile.pattern");

        // size
        this.setSize("searchfiledialog");
        this.addKeyListener(this);
        this.Dir.addKeyListener(this);
        this.Pattern.addKeyListener(this);
    }

    public void action() {
        this.saveHistory();
        if (this.Run != null && this.Run.isAlive()) {
            return;
        }
        this.Run = new Thread(this);
        this.Run.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.L) {
            this.action();
        } else {
            super.actionPerformed(e);
        }
    }

    @Override
    public boolean close() {
        this.Abort = true;
        return true;
    }

    public void deselectAll() {
        for (int i = this.L.getItemCount() - 1; i >= 0; i--) {
            this.L.deselect(i);
        }
    }

    @Override
    public void doAction(String o) {
        this.Result = null;
        if (o.equals("SearchRek")) {
            this.search(true);
        } else if (o.equals("Search")) {
            this.search(false);
        } else if (o.equals("TextAction")) {
            this.L.removeAll();
            this.action();
        } else if (o.equals("Action")) {
            this.action();
        } else if (o.equals("Help")) {
            this.help();
        } else if (o.equals("Close")) {
            this.Abort = true;
            this.doclose();
        }
    }

    @Override
    public void doclose() {
        if (this.F != null) {
            this.F.stopIt();
        }
        this.Dir.saveHistory("searchfile.dir");
        this.Pattern.saveHistory("searchfile.pattern");
        this.noteSize("searchfiledialog");
        super.doclose();
    }

    public void enableButtons(boolean f) {
        this.Pattern.setEnabled(f);
        this.SearchButton.setEnabled(f);
        this.SearchrekButton.setEnabled(f);
        this.ActionButton.setEnabled(f);
    }

    @Override
    public void focusGained(FocusEvent e) {
        this.Pattern.requestFocus();
    }

    /**
     * Get an enumeration of selected files. Should check for an aborted dialog
     * before.
     */
    public Enumeration getFiles() {
        this.S = this.L.getSelectedItems();
        this.Sn = 0;
        return this;
    }

    public String getResult() {
        return this.Result;
    }

    @Override
    public boolean hasMoreElements() {
        return this.Sn < this.S.length;
    }

    public void help() {
    }

    @Override
    public boolean isAborted() {
        return this.Abort;
    }

    public boolean isModified() {
        return this.Mod.getState();
    }

    @Override
    public Object nextElement() {
        if (this.Sn >= this.S.length) {
            return null;
        }
        final String s = this.S[this.Sn];
        this.Sn++;
        return s;
    }

    @Override
    public void run() {
        this.Result = null;
        this.enableButtons(false);
        if (this.L.getItemCount() > 0) {
            final int i = this.L.getSelectedIndex();
            if (i > 0) {
                this.Result = this.L.getItem(i);
            } else {
                this.Result = this.L.getItem(0);
            }
        } else {
            final FileListFinder f = new FileListFinder(this.Dir.getText(),
                    this.Pattern.getText(), true);
            this.F = f;
            f.search();
            this.Result = f.getResult();
        }
        this.enableButtons(true);
        this.Abort = false;
        this.doclose();
    }

    public void saveHistory() {
        this.Dir.remember();
        this.Pattern.remember();
    }

    public void search(boolean recurse) {
        this.saveHistory();
        if (this.Run != null && this.Run.isAlive()) {
            return;
        }
        this.Run = new SearchFileThread(this, this.L, this.Dir.getText(),
                this.Pattern.getText(), recurse);
        this.Run.start();
    }

    public void setPattern(String s) {
        this.Pattern.setText(s);
    }

    @Override
    public void setVisible(boolean flag) {
        if (flag) {
            this.enableButtons(true);
        }
        super.setVisible(flag);
    }
}

class SearchFileThread extends Thread {
    SearchFileDialog D;
    MyList L;
    String Dir, Pattern;
    boolean Recurse;

    public SearchFileThread(SearchFileDialog dialog, MyList l, String d,
            String p, boolean r) {
        this.D = dialog;
        this.L = l;
        this.Dir = d;
        this.Pattern = p;
        this.Recurse = r;
    }

    @Override
    public void run() {
        this.D.enableButtons(false);
        this.L.removeAll();
        this.L.setEnabled(false);
        final FileList f = new FileList(this.Dir, this.Pattern, this.Recurse);
        this.D.F = f;
        f.search();
        f.sort();
        final Enumeration e = f.files();
        while (e.hasMoreElements()) {
            try {
                this.L.add(((File) e.nextElement()).getCanonicalPath());
            } catch (final Exception ex) {
            }
        }
        this.L.setEnabled(true);
        this.D.enableButtons(true);
    }
}
