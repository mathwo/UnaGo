package rene.dialogs;

import rene.gui.*;
import rene.lister.Lister;
import rene.lister.ListerMouseEvent;
import rene.util.FileList;
import rene.util.FileName;
import rene.util.MyVector;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Enumeration;

class DirFieldListener implements DoActionListener {
    MyFileDialog T;

    public DirFieldListener(MyFileDialog t) {
        this.T = t;
    }

    @Override
    public void doAction(String o) {
        this.T.setFile(o);
    }

    @Override
    public void itemAction(String o, boolean flag) {
    }
}

/**
 * This is a file dialog. It is easy to handle, remembers its position and size,
 * and performs pattern matching. The calls needs the rene.viewer.* class,
 * unless you replace Lister with List everywhere. Moreover it needs the
 * rene.gui.Global class to store field histories and determine the background
 * color. Finally, it uses the FileList class to get the list of files.
 * <p>
 * The dialog does never check for files to exists. This must be done by the
 * application.
 * <p>
 * There is a static main method, which demonstrates everything.
 */

public class MyFileDialog extends CloseDialog implements ItemListener,
FilenameFilter, MouseListener { // java.awt.List
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
        final Frame f = new CloseFrame() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void doclose() {
                System.exit(0);
            }
        };
        f.setSize(500, 500);
        f.setLocation(400, 400);
        f.setVisible(true);
        final MyFileDialog d = new MyFileDialog(f, "Title", "Save", false);
        d.center(f);
        d.update();
        d.setVisible(true);
    }

    // Dirs,Files;
    Lister Dirs, Files;
    HistoryTextField DirField, FileField, PatternField;
    HistoryTextFieldChoice DirHistory, FileHistory;
    TextField Chosen;
    String CurrentDir = ".";
    boolean Aborted = true;
    String DirAppend = "", PatternAppend = "", FileAppend = "";
    Button Home;

    Frame F;

    FileDialog FD;

    boolean HomeShiftControl = false;

    MyVector Undo = new MyVector();

    public MyFileDialog(Frame f, String title, boolean saving) {
        super(f, "", true);
        this.FD = new FileDialog(f, title, saving ? FileDialog.SAVE
                : FileDialog.LOAD);
    }

    public MyFileDialog(Frame f, String title, String action, boolean saving) {
        this(f, title, action, saving, false);
    }

    /**
     * @param title
     *            The dialog title.
     * @param action
     *            The button string for the main action (e.g. Load)
     * @param saving
     *            True, if this is a saving dialog.
     */
    public MyFileDialog(Frame f, String title, String action, boolean saving,
            boolean help) {
        super(f, title, true);
        this.F = f;
        this.setLayout(new BorderLayout());

        // title prompt
        this.add("North", new Panel3D(this.Chosen = new TextFieldAction(this,
                "")));
        this.Chosen.setEditable(false);

        // center panels
        final Panel center = new MyPanel();
        center.setLayout(new GridLayout(1, 2, 5, 0));
        this.Dirs = new Lister();
        if (Global.NormalFont != null) {
            this.Dirs.setFont(Global.NormalFont);
        }
        this.Dirs.addActionListener(this);
        this.Dirs.setMode(false, false, false, false);
        center.add(this.Dirs);
        this.Files = new Lister();
        if (Global.NormalFont != null) {
            this.Files.setFont(Global.NormalFont);
        }
        this.Files.addActionListener(this);
        this.Files.setMode(false, false, true, false);
        center.add(this.Files);
        this.add("Center", new Panel3D(center));

        // south panel
        final Panel south = new MyPanel();
        south.setLayout(new BorderLayout());

        final Panel px = new MyPanel();
        px.setLayout(new BorderLayout());

        final Panel p0 = new MyPanel();
        p0.setLayout(new GridLayout(0, 1));

        final Panel p1 = new MyPanel();
        p1.setLayout(new BorderLayout());
        p1.add("North", this.linePanel(
                new MyLabel(Global.name("myfiledialog.dir")),
                this.DirField = new HistoryTextField(this, "Dir", 32) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = 1L;

                    @Override
                    public boolean filterHistory(String name)
                    // avoid a Windows bug with C: instead of c:
                    {
                        if (name.length() < 2) {
                            return true;
                        }
                        if (name.charAt(1) == ':'
                                && Character.isUpperCase(name.charAt(0))) {
                            return false;
                        }
                        return true;
                    }
                }));
        this.DirField.setText(".");
        p1.add("South", this.linePanel(
                new MyLabel(Global.name("myfiledialog.olddirs", "")),
                this.DirHistory = new HistoryTextFieldChoice(this.DirField)));
        p0.add(new Panel3D(p1));

        final Panel p2 = new MyPanel();
        p2.setLayout(new BorderLayout());
        p2.add("North", this.linePanel(
                new MyLabel(Global.name("myfiledialog.file")),
                this.FileField = new HistoryTextField(this, "File")));
        p2.add("South", this.linePanel(
                new MyLabel(Global.name("myfiledialog.oldfiles", "")),
                this.FileHistory = new HistoryTextFieldChoice(this.FileField)));
        p0.add(new Panel3D(p2));

        px.add("Center", p0);

        px.add("South",
                new Panel3D(this.linePanel(
                        new MyLabel(Global.name("myfiledialog.pattern")),
                        this.PatternField = new HistoryTextField(this,
                                "Pattern"))));
        this.PatternField.setText("*");

        south.add("Center", px);

        final Panel buttons = new MyPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(this.Home = new ButtonAction(this, Global.name(
                "myfiledialog.home", "Home"), "Home"));
        buttons.add(new ButtonAction(this, Global.name("myfiledialog.mkdir",
                "Create Directory"), "Create"));
        buttons.add(new ButtonAction(this, Global.name("myfiledialog.back",
                "Back"), "Back"));
        buttons.add(new MyLabel(""));
        buttons.add(new ButtonAction(this, action, "Action"));
        buttons.add(new ButtonAction(this, Global.name("abort"), "Close"));
        if (help) {
            this.addHelp(buttons, "filedialog");
        }

        south.add("South", buttons);

        this.add("South", new Panel3D(south));

        // set sizes
        this.pack();
        this.setSize("myfiledialog");
        this.addKeyListener(this);
        this.DirField.addKeyListener(this);
        this.DirField.setTrigger(true);
        this.FileHistory.setDoActionListener(new DirFieldListener(this));
        this.PatternField.addKeyListener(this);
        this.PatternField.setTrigger(true);
        this.FileField.addKeyListener(this);
        this.FileField.setTrigger(true);
        this.Home.addMouseListener(this);
    }

    /**
     * Can be overwritten by instances to accept only some files.
     */
    @Override
    public boolean accept(File dir, String file) {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.Dirs) {
            final String s = this.Dirs.getSelectedItem();
            if (s == null) {
                return;
            }
            if (s.equals("..")) {
                this.dirup();
            } else {
                this.dirdown(s);
            }
        }
        if (e.getSource() == this.Files) {
            if (e instanceof ListerMouseEvent) {
                final ListerMouseEvent em = (ListerMouseEvent) e;
                if (em.clickCount() >= 2) {
                    this.leave();
                } else {
                    final String s = this.Files.getSelectedItem();
                    if (s != null) {
                        this.FileField.setText(s);
                    }
                }
            }
        } else {
            super.actionPerformed(e);
        }
    }

    /**
     * Note the directory in a history list.
     */
    public void addUndo(String dir) {
        if (this.Undo.size() > 0
                && ((String) this.Undo.elementAt(this.Undo.size() - 1))
                .equals(dir)) {
            return;
        }
        this.Undo.addElement(dir);
    }

    @Override
    public void center(Frame f) {
        if (this.FD != null) {
            CloseDialog.center(f, this.FD);
        } else {
            super.center(f);
        }
    }

    public void dirdown(String subdir) {
        this.DirField.setText(this.CurrentDir + File.separatorChar + subdir);
        if (this.updateDir()) {
            this.updateFiles();
        }
    }

    public void dirup() {
        this.DirField.setText(FileName.path(this.CurrentDir));
        if (this.DirField.getText().equals("")) {
            this.DirField.setText("" + File.separatorChar);
        }
        if (this.updateDir()) {
            this.updateFiles();
        }
    }

    @Override
    public void doAction(String o) {
        if (o.equals("Dir") || o.equals("Pattern")) {
            if (this.updateDir()) {
                this.updateFiles();
            }
            this.PatternField.remember(this.PatternField.getText());
        } else if (o.equals("File") || o.equals("Action")) {
            if (this.FileField.getText().equals("")) {
                return;
            }
            this.leave();
        } else if (o.equals("Home")) {
            if (this.HomeShiftControl) {
                final String s = Global
                        .getParameter("myfiledialog.homedir", "");
                if (s.equals("")) {
                    Global.setParameter("myfiledialog.homedir",
                            this.DirField.getText());
                } else {
                    Global.setParameter("myfiledialog.homedir", "");
                }
            }
            try {
                final String s = Global
                        .getParameter("myfiledialog.homedir", "");
                if (s.equals("")) {
                    final String s1 = System.getProperty("user.home");
                    final String s2 = Global.name("myfiledialog.windowshome",
                            "");
                    final String s3 = Global.name("myfiledialog.homedir", "");
                    final String s4 = Global.name("Documents");
                    final String sep = System.getProperty("file.separator");
                    if (new File(s1 + sep + s4 + sep + s3).exists()) {
                        this.DirField.setText(s1 + sep + s4 + sep + s3);
                    } else if (new File(s1 + sep + s2 + sep + s3).exists()) {
                        this.DirField.setText(s1 + sep + s2 + sep + s3);
                    } else if (new File(s1 + sep + s4).exists()) {
                        this.DirField.setText(s1 + sep + s4);
                    } else if (new File(s1 + sep + s2).exists()) {
                        this.DirField.setText(s1 + sep + s2);
                    } else if (new File(s1 + sep + s3).exists()) {
                        this.DirField.setText(s1 + sep + s3);
                    } else {
                        this.DirField.setText(s1);
                    }
                } else {
                    this.DirField.setText(s);
                }
                this.updateDir();
                this.updateFiles();
            } catch (final Exception e) {
            }
        } else if (o.equals("Create")) {
            try {
                final File f = new File(this.DirField.getText());
                if (!f.exists()) {
                    f.mkdir();
                }
                this.updateDir();
                this.updateFiles();
            } catch (final Exception e) {
            }
        } else if (o.equals("Back")) {
            final String dir = this.getUndo();
            if (!dir.equals("")) {
                this.DirField.setText(dir);
                this.updateDir();
                this.updateFiles();
            }
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        this.FileField.requestFocus();
    }

    /**
     * @return The file plus its path.
     */
    public String getFilePath() {
        if (this.FD != null) {
            if (this.FD.getFile() != null) {
                return this.FD.getDirectory() + this.FD.getFile();
            } else {
                return "";
            }
        }
        final String file = this.FileField.getText();
        if (!FileName.path(file).equals("")) {
            return file;
        } else {
            return this.CurrentDir + File.separatorChar
                    + this.FileField.getText();
        }
    }

    /**
     * Get the undo directory and remove it.
     */
    public String getUndo() {
        if (this.Undo.size() < 2) {
            return "";
        }
        final String s = (String) this.Undo.elementAt(this.Undo.size() - 2);
        this.Undo.truncate(this.Undo.size() - 1);
        return s;
    }

    /**
     * Check, if the dialog was aborted.
     */
    @Override
    public boolean isAborted() {
        if (this.FD != null) {
            return this.FD.getFile() == null || this.FD.getFile().equals("");
        } else {
            return this.Aborted;
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
    }

    /**
     * Leave the dialog and remember settings.
     */
    void leave() {
        if (this.FD != null) {
            return;
        }
        if (!this.FileField.getText().equals("")) {
            this.Aborted = false;
        }
        if (!this.Aborted) {
            this.noteSize("myfiledialog");
            this.DirField.remember(this.DirField.getText());
            this.DirField.saveHistory("myfiledialog.dir.history"
                    + this.DirAppend);
            this.PatternField.saveHistory("myfiledialog.pattern.history"
                    + this.PatternAppend);
            this.FileField.remember(this.getFilePath());
            this.FileField.saveHistory("myfiledialog.file.history"
                    + this.FileAppend);
        }
        this.doclose();
    }

    Panel linePanel(Component x, Component y) {
        final Panel p = new MyPanel();
        p.setLayout(new GridLayout(1, 0));
        p.add(x);
        p.add(y);
        return p;
    }

    public void loadHistories() {
        this.loadHistories(true);
    }

    public void loadHistories(boolean recent) {
        if (this.FD != null) {
            return;
        }
        this.DirField.loadHistory("myfiledialog.dir.history" + this.DirAppend);
        this.DirHistory.update();
        if (recent) {
            this.setDirectory(this.DirHistory.getRecent());
        }
        if (this.updateDir()) {
            this.updateFiles();
        }
        this.PatternField.loadHistory("myfiledialog.pattern.history"
                + this.PatternAppend);
        this.FileField.loadHistory("myfiledialog.file.history"
                + this.FileAppend);
        this.FileHistory.update();
    }

    /**
     * Loads the histories from the configuration file. If you want a unique
     * history for your instance, you need to give a string unique for your
     * instance. There are three types of histories.
     *
     * @param dir
     * @param pattern
     * @param file
     * @see loadHistories
     */
    public void loadHistories(String dir, String pattern, String file) {
        this.setAppend(dir, pattern, file);
        this.loadHistories();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.HomeShiftControl = e.isShiftDown() && e.isControlDown();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Histories are used for the directories, the files and the patterns. The
     * dialog can use different histories for each instance of this class. If
     * you want that, you need to determine the history for the instance with a
     * string, unique for the instance. If a string is empty, "default" is used.
     *
     * @param dir
     * @param pattern
     * @param file
     */
    public void setAppend(String dir, String pattern, String file) {
        if (this.FD != null) {
            return;
        }
        if (!dir.equals("")) {
            this.DirAppend = "." + dir;
        } else {
            this.DirAppend = ".default";
        }
        if (!pattern.equals("")) {
            this.PatternAppend = "." + pattern;
        } else {
            this.PatternAppend = ".default";
        }
        if (!file.equals("")) {
            this.FileAppend = "." + file;
        } else {
            this.FileAppend = ".default";
        }
    }

    public void setDirectory(String dir) {
        if (this.FD != null) {
            this.FD.setDirectory(dir);
        } else {
            this.DirField.setText(dir);
        }
    }

    public void setFile(String s) {
        this.DirField.setText(FileName.path(s));
        this.FileField.setText(FileName.filename(s));
        // System.out.println(s);
        this.update(false);
    }

    public void setFilePath(String file) {
        if (this.FD != null) {
            this.FD.setFile(file);
            return;
        }
        final String dir = FileName.path(file);
        if (!dir.equals("")) {
            this.DirField.setText(dir);
            this.FileField.setText(FileName.filename(file));
        } else {
            this.FileField.setText(file);
        }
    }

    public void setPattern(String pattern) {
        if (this.FD != null) {
            this.FD.setFilenameFilter(this); // does not work
            final String s = pattern.replace(' ', ';');
            this.FD.setFile(s);
        } else {
            this.PatternField.setText(pattern);
        }
    }

    @Override
    public void setVisible(boolean flag) {
        if (this.FD != null) {
            this.FD.setVisible(flag);
        } else {
            super.setVisible(flag);
        }
    }

    public void update() {
        this.update(true);
    }

    /**
     * This should be called at the start.
     */
    public void update(boolean recent) {
        if (this.FD != null) {
            return;
        }
        this.loadHistories(recent);
        this.setFilePath(this.FileField.getText());
        if (this.updateDir()) {
            this.updateFiles();
        }
        this.Aborted = true;
    }

    /**
     * Update the directory list.
     *
     * @return if the current content of DirField is indeed a directory.
     */
    public boolean updateDir() {
        if (this.FD != null) {
            return true;
        }
        final File dir = new File(this.DirField.getText() + File.separatorChar);
        if (!dir.isDirectory()) {
            return false;
        }
        try {
            final String s = FileName.canonical(dir.getCanonicalPath());
            this.addUndo(s);
            this.DirField.setText(s);
            this.Chosen.setText(FileName.chop(16, this.DirField.getText()
                    + File.separatorChar + this.PatternField.getText(), 48));
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Update the file list.
     */
    public void updateFiles() {
        if (this.FD != null) {
            return;
        }
        final File dir = new File(this.DirField.getText());
        if (!dir.isDirectory()) {
            return;
        }
        this.CurrentDir = this.DirField.getText();
        if (this.PatternField.getText().equals("")) {
            this.PatternField.setText("*");
        }
        try {
            this.Files.clear();
            this.Dirs.clear();
            final FileList l = new FileList(this.DirField.getText(),
                    this.PatternField.getText(), false);
            l.setCase(Global.getParameter("filedialog.usecaps", false));
            l.search();
            l.sort();
            Enumeration e = l.files();
            while (e.hasMoreElements()) {
                final File f = (File) e.nextElement();
                this.Files.addElement(FileName.filename(f.getCanonicalPath()));
            }
            this.Dirs.addElement("..");
            e = l.dirs();
            while (e.hasMoreElements()) {
                final File f = (File) e.nextElement();
                this.Dirs.addElement(FileName.filename(f.getCanonicalPath()));
            }
        } catch (final Exception e) {
        }
        this.Dirs.updateDisplay();
        this.Files.updateDisplay();
        this.Files.requestFocus();
    }
}
