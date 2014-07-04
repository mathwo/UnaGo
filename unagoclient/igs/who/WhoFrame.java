package unagoclient.igs.who;

import unagoclient.Global;
import unagoclient.dialogs.Help;
import unagoclient.gui.*;
import unagoclient.igs.ConnectionFrame;
import unagoclient.igs.IgsStream;
import rene.util.list.ListClass;
import rene.util.list.ListElement;
import rene.util.parser.StringParser;
import rene.viewer.Lister;
import rene.viewer.SystemLister;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.util.Arrays;

class EditButtons extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    JTextField Labels[], Texts[];

    public EditButtons(Frame f) {
        super(f, Global.resourceString("Edit_Buttons"), true);
        this.setLayout(new BorderLayout());
        final MyPanel center = new MyPanel();
        this.Labels = new JTextField[3];
        this.Texts = new JTextField[3];
        center.setLayout(new GridLayout(0, 2));
        for (int i = 0; i < 3; i++) {
            this.Labels[i] = new FormTextField(rene.gui.Global.getParameter(
                    "who.button" + (i + 1) + ".label", ""));
            center.add(this.Labels[i]);
            this.Texts[i] = new FormTextField(rene.gui.Global.getParameter(
                    "who.button" + (i + 1) + ".text", ""));
            center.add(this.Texts[i]);
        }
        this.add("Center", new Panel3D(center));
        final MyPanel south = new MyPanel();
        south.add(new ButtonAction(this, Global.resourceString("OK"), "OK"));
        south.add(new ButtonAction(this, Global.resourceString("Cancel"),
                "Close"));
        this.add("South", new Panel3D(south));
        Global.setpacked(this, "editbuttons", 300, 300, f);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        if (o.equals("OK")) {
            for (int i = 0; i < 3; i++) {
                rene.gui.Global.setParameter("who.button" + (i + 1) + ".label",
                        this.Labels[i].getText());
                rene.gui.Global.setParameter("who.button" + (i + 1) + ".text",
                        this.Texts[i].getText());
            }
            this.setVisible(false);
            this.dispose();
        } else {
            super.doAction(o);
        }
    }
}

/**
 * Displays a frame with the player list. The list may be sorted by rank or
 * name.
 */

public class WhoFrame extends CloseFrame implements CloseListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    IgsStream In;
    PrintWriter Out;
    Lister T;
    ConnectionFrame CF;
    WhoDistributor GD;
    ListClass L;
    boolean SortName;
    boolean Closed = false;
    String Range;
    JTextField WhoRange;

    CheckboxMenuItem OmitX, OmitQ, Looking, FriendsOnly;

    /**
     * @param cf
     *            the ConnectionFrame, which calls this WhoFrame (used as
     *            CloseListener)
     * @param out
     *            the output stream to the IGS server.
     * @param in
     *            the input stream from the IGS server.
     * @param range
     *            a range string to be used in the IGS who command.
     */
    public WhoFrame(ConnectionFrame cf, PrintWriter out, IgsStream in,
            String range) {
        super(Global.resourceString("_Who_"));
        cf.addCloseListener(this);
        this.Range = range;
        this.In = in;
        this.Out = out;
        final MenuBar m = new MenuBar();
        final Menu sort = new MyMenu(Global.resourceString("Options"));
        sort.add(new MenuItemAction(this, Global.resourceString("Sort_by_Name")));
        sort.add(new MenuItemAction(this, Global.resourceString("Sort_by_Rank")));
        sort.addSeparator();
        m.add(sort);
        sort.add(this.OmitX = new CheckboxMenuItemAction(this, Global
                .resourceString("Omit_X")));
        this.OmitX.setState(rene.gui.Global.getParameter("omitx", true));
        sort.add(this.OmitQ = new CheckboxMenuItemAction(this, Global
                .resourceString("Omit_Q")));
        this.OmitQ.setState(rene.gui.Global.getParameter("omitq", false));
        sort.add(this.Looking = new CheckboxMenuItemAction(this, Global
                .resourceString("__only")));
        this.Looking.setState(false);
        sort.add(this.FriendsOnly = new CheckboxMenuItemAction(this, Global
                .resourceString("Friends_only")));
        this.FriendsOnly.setState(false);
        final Menu actions = new MyMenu(Global.resourceString("Button_Actions"));
        this.addmenu(actions, Global.resourceString("Tell"), "Tell");
        actions.addSeparator();
        this.addmenu(actions, Global.resourceString("Add_Friend"), "Add_Friend");
        this.addmenu(actions, Global.resourceString("Remove_Friend"),
                "Remove_Friend");
        this.addmenu(actions, Global.resourceString("Mark_Player"), "Mark");
        this.addmenu(actions, Global.resourceString("Unmark_Player"), "Unmark");
        actions.addSeparator();
        this.addmenu(actions, Global.resourceString("Match"), "Match");
        this.addmenu(actions, Global.resourceString("Stats"), "Stats");
        this.addmenu(
                actions,
                rene.gui.Global.getParameter("who.button1.label",
                        Global.resourceString("Suggest")), "Button1");
        this.addmenu(
                actions,
                rene.gui.Global.getParameter("who.button2.label",
                        Global.resourceString("Results")), "Button2");
        this.addmenu(
                actions,
                rene.gui.Global.getParameter("who.button3.label",
                        Global.resourceString("Stored")), "Button3");
        actions.addSeparator();
        actions.add(new MenuItemAction(this, Global
                .resourceString("Edit_Buttons")));
        m.add(actions);
        final Menu help = new MyMenu(Global.resourceString("Help"));
        help.add(new MenuItemAction(this, Global
                .resourceString("About_this_Window")));
        m.add(help);
        this.setMenuBar(m);
        this.setLayout(new BorderLayout());
        this.T = rene.gui.Global.getParameter("systemlister", false) ? new SystemLister()
        : new Lister();
        this.T.setFont(Global.Monospaced);
        this.T.setText(Global.resourceString("Loading"));
        this.add("Center", this.T);
        final MyPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Refresh")));
        p.add(this.WhoRange = new HistoryTextField(this, Global
                .resourceString("WhoRange"), 5));
        this.WhoRange.setText(this.Range);
        p.add(new ButtonAction(this, Global.resourceString("Close")));
        p.add(new ButtonAction(this, Global.resourceString("Toggle_Looking")));
        p.add(new ButtonAction(this, Global.resourceString("Toggle_Friends")));
        this.add("South", new Panel3D(p));
        this.CF = cf;
        this.GD = null;
        this.SortName = false;
        this.seticon("iwho.gif");
        final PopupMenu pop = new PopupMenu();
        this.addpop(pop, Global.resourceString("Tell"), "Tell");
        this.addpop(pop, Global.resourceString("Match"), "Match");
        this.addpop(pop, Global.resourceString("Stats"), "Stats");
        pop.addSeparator();
        this.addpop(pop, Global.resourceString("Add_Friend"), "Add_Friend");
        this.addpop(pop, Global.resourceString("Remove_Friend"),
                "Remove_Friend");
        this.addpop(pop, Global.resourceString("Mark_Player"), "Mark");
        this.addpop(pop, Global.resourceString("Unmark_Player"), "Unmark");
        pop.addSeparator();
        this.addpop(
                pop,
                rene.gui.Global.getParameter("who.button1.label",
                        Global.resourceString("Suggest")), "Button1");
        rene.gui.Global.setParameter(
                "who.button1.label",
                rene.gui.Global.getParameter("who.button1.label",
                        Global.resourceString("Suggest")));
        rene.gui.Global.setParameter(
                "who.button1.text",
                rene.gui.Global.getParameter("who.button1.text",
                        Global.resourceString("suggest %%")));
        this.addpop(
                pop,
                rene.gui.Global.getParameter("who.button2.label",
                        Global.resourceString("Results")), "Button2");
        rene.gui.Global.setParameter(
                "who.button2.label",
                rene.gui.Global.getParameter("who.button2.label",
                        Global.resourceString("Results")));
        rene.gui.Global.setParameter(
                "who.button2.text",
                rene.gui.Global.getParameter("who.button2.text",
                        Global.resourceString("results %%")));
        this.addpop(
                pop,
                rene.gui.Global.getParameter("who.button3.label",
                        Global.resourceString("Stored")), "Button3");
        rene.gui.Global.setParameter(
                "who.button3.label",
                rene.gui.Global.getParameter("who.button3.label",
                        Global.resourceString("Stored")));
        rene.gui.Global.setParameter(
                "who.button3.text",
                rene.gui.Global.getParameter("who.button3.text",
                        Global.resourceString("stored %%")));
        if (this.T instanceof Lister) {
            this.T.setPopupMenu(pop);
        }
    }

    public void addmenu(Menu pop, String label, String action) {
        final MenuItem mi = new MenuItemAction(this, label, action);
        pop.add(mi);
    }

    public void addpop(PopupMenu pop, String label, String action) {
        final MenuItem mi = new MenuItemAction(this, label, action);
        pop.add(mi);
    }

    /**
     * The distributor told me that all players have been receved. Now unchain
     * the distributor, parse and sort the output and display.
     */
    synchronized void allsended() {
        if (this.GD != null) {
            this.GD.unchain();
        }
        this.GD = null;
        if (this.Closed) {
            return;
        }
        ListElement p = this.L.first();
        int i, n = 0;
        while (p != null) {
            n++;
            p = p.next();
        }
        if (n > 2) {
            final WhoObject v[] = new WhoObject[n];
            p = this.L.first();
            for (i = 0; i < n; i++) {
                v[i] = new WhoObject((String) p.content(), this.SortName);
                p = p.next();
            }
            Arrays.sort(v);
            this.T.setText("");
            this.T.appendLine0(
                    Global.resourceString("_Info_______Name_______Idle___Rank"));
            final Color FC = Color.green.darker(), CM = Color.red.darker();
            for (i = 0; i < n; i++) {
                if (!(this.Looking.getState() && !v[i].looking()
                        || this.OmitX.getState() && v[i].silent()
                        || this.OmitQ.getState() && v[i].quiet()
                        || this.FriendsOnly.getState() && !v[i].friend())) {
                    this.T.appendLine0(v[i].who(),
                            v[i].friend() ? FC : v[i].marked() ? CM
                                    : Color.black);
                }
            }
            this.T.doUpdate(false);
        } else {
            p = this.L.first();
            while (p != null) {
                this.T.appendLine((String) p.content());
                p = p.next();
            }
            this.T.doUpdate(false);
        }
    }

    /**
     * When this dialog closes it will unchain itself from the distributor,
     * which created it.
     */
    @Override
    synchronized public boolean close() {
        if (this.GD != null) {
            this.GD.unchain();
        }
        this.GD = null;
        rene.gui.Global.setParameter("whorange", this.WhoRange.getText());
        this.Closed = true;
        this.CF.removeCloseListener(this);
        this.CF.Who = null;
        Global.notewindow(this, "who");
        return true;
    }

    @Override
    public void doAction(String o) {
        if (Global.resourceString("Refresh").equals(o)) {
            this.refresh();
        } else if ("Tell".equals(o)) {
            final String user = this.getuser();
            if (user.equals("")) {
                return;
            }
            new TellQuestion(this, this.CF, user);
        } else if ("Match".equals(o)) {
            final String user = this.getuser();
            if (user.equals("")) {
                return;
            }
            new MatchQuestion(this, this.CF, user);
        } else if ("Add_Friend".equals(o)) {
            final String friends = rene.gui.Global.getParameter("friends", "");
            final String user = " " + this.getuser();
            if (friends.indexOf(user) >= 0) {
                return;
            }
            rene.gui.Global.setParameter("friends", friends + user);
            this.allsended();
        } else if ("Remove_Friend".equals(o)) {
            String friends = rene.gui.Global.getParameter("friends", "");
            final String user = " " + this.getuser();
            final int n = friends.indexOf(user);
            if (friends.indexOf(user) < 0) {
                return;
            }
            friends = friends.substring(0, n)
                    + friends.substring(n + user.length());
            rene.gui.Global.setParameter("friends", friends);
            this.allsended();
        } else if ("Mark".equals(o)) {
            final String friends = rene.gui.Global.getParameter("marked", "");
            final String user = " " + this.getuser();
            if (friends.indexOf(user) >= 0) {
                return;
            }
            rene.gui.Global.setParameter("marked", friends + user);
            this.allsended();
        } else if ("Unmark".equals(o)) {
            String friends = rene.gui.Global.getParameter("marked", "");
            final String user = " " + this.getuser();
            final int n = friends.indexOf(user);
            if (friends.indexOf(user) < 0) {
                return;
            }
            friends = friends.substring(0, n)
                    + friends.substring(n + user.length());
            rene.gui.Global.setParameter("marked", friends);
            this.allsended();
        } else if ("Stats".equals(o)) {
            if (this.getuser().equals("")) {
                return;
            }
            this.CF.out("stats " + this.getuser());
        } else if ("Button1".equals(o)) {
            if (this.getuser().equals("")) {
                return;
            }
            this.CF.out(this.replace(rene.gui.Global.getParameter(
                    "who.button1.text", "suggest %%")));
        } else if ("Button2".equals(o)) {
            if (this.getuser().equals("")) {
                return;
            }
            this.CF.out(this.replace(rene.gui.Global.getParameter(
                    "who.button2.text", "results %%")));
        } else if ("Button3".equals(o)) {
            if (this.getuser().equals("")) {
                return;
            }
            this.CF.out(this.replace(rene.gui.Global.getParameter(
                    "who.button3.text", "stored %%")));
        } else if (Global.resourceString("Sort_by_Name").equals(o)) {
            this.SortName = true;
            this.allsended();
        } else if (Global.resourceString("Sort_by_Rank").equals(o)) {
            this.SortName = false;
            this.allsended();
        } else if (Global.resourceString("About_this_Window").equals(o)) {
            new Help("who");
        } else if (o.equals(Global.resourceString("Edit_Buttons"))) {
            new EditButtons(this);
        } else if (o.equals(Global.resourceString("Toggle_Friends"))) {
            this.FriendsOnly.setState(!this.FriendsOnly.getState());
            this.allsended();
        } else if (o.equals(Global.resourceString("Toggle_Looking"))) {
            this.Looking.setState(!this.Looking.getState());
            this.allsended();
        } else {
            super.doAction(o);
        }
    }

    public String getuser() {
        final String s = this.T.getSelectedItem();
        if (s == null || s.length() < 14) {
            return "";
        }
        final StringParser p = new StringParser(s.substring(12));
        return p.parseword();
    }

    @Override
    public void isClosed() {
        if (rene.gui.Global.getParameter("menuclose", true)) {
            this.setMenuBar(null);
        }
        this.setVisible(false);
        this.dispose();
    }

    @Override
    public void itemAction(String o, boolean f) {
        if (o.equals(Global.resourceString("__only"))) {
            rene.gui.Global.setParameter("looking", f);
            this.allsended();
        } else if (o.equals(Global.resourceString("Omit_X"))) {
            rene.gui.Global.setParameter("omitx", f);
            this.allsended();
        } else if (o.equals(Global.resourceString("Omit_Q"))) {
            rene.gui.Global.setParameter("omitq", f);
            this.allsended();
        } else if (o.equals(Global.resourceString("Friends_only"))) {
            rene.gui.Global.setParameter("friendsonly", f);
            this.allsended();
        }
    }

    /**
     * receive a single line of players (two players)
     */
    synchronized void receive(String s) {
        if (this.Closed) {
            return;
        }
        StringParser p = new StringParser(s);
        p.skipblanks();
        if (p.skip("Info")) {
            return;
        }
        if (p.skip("****")) {
            return;
        }
        p = new StringParser(s);
        s = p.upto('|');
        this.L.append(new ListElement(s));
        if (!p.skip("| ")) {
            return;
        }
        s = p.upto('|');
        this.L.append(new ListElement(s));
    }

    /**
     * Will send a new who command to the IGS server. To receive the IGS output
     * it will create a who distributor, which will register with the IGSStream
     * and send input to this who frame.
     */
    synchronized public void refresh() {
        this.L = new ListClass();
        this.T.setText(Global.resourceString("Loading"));
        if (this.GD != null) {
            this.GD.unchain();
        }
        this.GD = null;
        this.GD = new WhoDistributor(this.In, this);
        this.Out.println("who " + this.WhoRange.getText());
    }

    public String replace(String replacement) {
        final int pos = replacement.indexOf("%%");
        if (pos < 0) {
            return replacement;
        }
        String begin = "";
        if (pos > 0) {
            begin = replacement.substring(0, pos);
        }
        String end = "";
        if (pos + 2 < replacement.length()) {
            end = replacement.substring(pos + 2);
        }
        return begin + this.getuser() + end;
    }

}
