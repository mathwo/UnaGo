package rene.dialogs;

import rene.gui.*;
import rene.lister.Lister;
import rene.util.FileName;
import rene.util.ftp.FTP;
import rene.util.regexp.RegExp;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class FTPFileDialog extends CloseDialog implements Runnable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    HistoryTextField Server, Path, User, Password;
    boolean Result = false;
    Lister L;
    String Separator = Global.getParameter("ftp.separator", "/");
    String FtpDir = "";
    ButtonAction Dir, OK;
    boolean Active;

    public FTPFileDialog(Frame f, String title, String prompt) {
        super(f, title, true);
        this.setLayout(new BorderLayout());
        final Panel p = new MyPanel();
        p.setLayout(new GridLayout(0, 2));
        p.add(new MyLabel(Global.name("ftpdialog.server", "Server")));
        p.add(this.Server = new HistoryTextField(this, "", 32));
        this.Server.loadHistory("ftp.field.server");
        p.add(new MyLabel(Global.name("ftpdialog.filepath", "File Path")));
        p.add(this.Path = new HistoryTextField(this, "Path", 32));
        this.Path.loadHistory("ftp.field.path");
        p.add(new MyLabel(Global.name("ftpdialog.user", "User")));
        p.add(this.User = new HistoryTextField(this, "", 32));
        this.User.loadHistory("ftp.field.user");
        p.add(new MyLabel(Global.name("ftpdialog.password", "Password")));
        p.add(this.Password = new HistoryTextField(this, "Password", 32));
        this.Password.setEchoChar('*');
        this.add("North", new Panel3D(p));
        this.L = new Lister();
        if (Global.FixedFont != null) {
            this.L.setFont(Global.FixedFont);
        }
        this.L.setMode(false, false, false, false);
        this.L.addActionListener(this);
        this.add("Center", new Panel3D(this.L));
        final Panel ps = new MyPanel();
        ps.add(this.Dir = new ButtonAction(this, Global.name("ftpdialog.dir"),
                "Dir"));
        ps.add(this.OK = new ButtonAction(this, prompt, "OK"));
        ps.add(new ButtonAction(this, Global.name("abort", "Abort"), "Close"));
        this.add("South", new Panel3D(ps));
        this.setSize("ftpdialog");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.L) {
            if (this.Active) {
                return;
            }
            String s = this.L.getSelectedItem();
            if (s.equals("..")) {
                if (this.FtpDir.endsWith(this.Separator)) {
                    this.FtpDir = this.FtpDir.substring(0,
                            this.FtpDir.length() - 1);
                }
                this.Path.setText(FileName.path(this.FtpDir));
                this.doAction("Dir");
                return;
            }
            final RegExp r = new RegExp(Global.getParameter("ftp.regexp",
                    "^([dl]*).* ([^[:white:]]+)$"), false);
            if (!r.match(s)) {
                return;
            }
            try {
                final String d = r.expand(Global.getParameter("ftp.regexp.dir",
                        "(0)"));
                s = r.expand(Global.getParameter("ftp.regexp.file", "(1)"));
                if (d.equals("") || d.equals("l")) {
                    if (!this.FtpDir.endsWith(this.Separator)) {
                        this.FtpDir = this.FtpDir + this.Separator;
                    }
                    this.Path.setText(this.FtpDir + s);
                } else {
                    if (!this.FtpDir.endsWith(this.Separator)) {
                        this.FtpDir = this.FtpDir + this.Separator;
                    }
                    this.Path.setText(this.FtpDir + s + this.Separator);
                    this.doAction("Dir");
                }
            } catch (final Exception ex) {
            }
        } else {
            super.actionPerformed(e);
        }
    }

    @Override
    public void doAction(String o) {
        if (this.Active) {
            return;
        }
        this.noteSize("ftpdialog");
        if (o.equals("OK")) {
            this.Server.remember();
            this.Server.saveHistory("ftp.field.server");
            this.Path.remember();
            this.Path.saveHistory("ftp.field.path");
            this.User.remember();
            this.User.saveHistory("ftp.field.user");
            this.Result = !this.Password.equals("");
            this.doclose();
        } else if (o.equals("Dir") && !this.Password.equals("")) {
            this.Active = true;
            this.Dir.setEnabled(false);
            this.OK.setEnabled(false);
            new Thread(this).start();
        } else if (o.equals("Path") || o.equals("Password")) {
            final String path = this.Path.getText();
            if (path.endsWith("/") || path.endsWith("\\") || path.equals("")
                    || path.equals(".")) {
                this.doAction("Dir");
            } else {
                this.doAction("OK");
            }
        } else {
            super.doAction(o);
        }
    }

    public FTP getFTP() throws IOException, UnknownHostException {
        final FTP ftp = new FTP(this.getServer());
        ftp.open(this.getUser(), this.Password.getText());
        return ftp;
    }

    public String getPassword() {
        return this.Password.getText();
    }

    public String getPath() {
        return this.Path.getText();
    }

    public boolean getResult() {
        return this.Result;
    }

    public String getServer() {
        return this.Server.getText();
    }

    public String getUser() {
        return this.User.getText();
    }

    @Override
    public void run() {
        this.L.clear();
        this.L.addElement("..");
        if (this.Path.getText().equals("")) {
            this.Path.setText(".");
        }
        try {
            final FTP ftp = new FTP(this.getServer());
            ftp.open(this.getUser(), this.Password.getText());
            String path = this.Path.getText();
            this.FtpDir = path;
            if (path.endsWith(this.Separator)) {
                path = path + ".";
            }
            final Enumeration e = ftp.getDirectory(path).elements();
            while (e.hasMoreElements()) {
                final String s = (String) e.nextElement();
                if (s.startsWith("d")) {
                    this.L.addElement(s, Color.green.darker().darker());
                } else if (s.startsWith("l")) {
                    this.L.addElement(s, Color.blue.darker());
                } else {
                    this.L.addElement(s);
                }
            }
            this.L.updateDisplay();
        } catch (final Exception e) {
            this.L.clear();
            this.L.addElement(e.toString());
            this.L.updateDisplay();
        }
        this.Active = false;
        this.Dir.setEnabled(true);
        this.OK.setEnabled(true);
    }

    public void setPassword(String s) {
        this.Password.setText(s);
    }

    public void setPath(String s) {
        this.Path.setText(s);
        this.Path.remember();
    }

    public void setServer(String s) {
        this.Server.setText(s);
        this.Server.remember();
    }

    public void setUser(String s) {
        this.User.setText(s);
        this.User.remember();
    }
}
