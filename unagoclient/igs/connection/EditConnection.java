package unagoclient.igs.connection;

import unagoclient.Global;
import unagoclient.Go;
import unagoclient.dialogs.HelpDialog;
import unagoclient.gui.*;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import javax.swing.*;
import java.awt.*;

public class EditConnection extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ListClass CList;
    Connection C;
    JTextField Name, Server, Port, User, Password, Encoding;
    Go G;
    Choice MChoice;
    Frame F;

    public EditConnection(CloseFrame f, ListClass clist, Connection c, Go go) {
        super(f, Global.resourceString("Edit_Connection"), true);
        this.G = go;
        this.F = f;
        this.CList = clist;
        this.C = c;
        final JPanel p1 = new MyPanel();
        p1.setLayout(new GridLayout(0, 2));
        p1.add(new MyLabel(Global.resourceString("Name")));
        p1.add(this.Name = new FormTextField("" + this.C.Name));
        p1.add(new MyLabel(Global.resourceString("Server")));
        p1.add(this.Server = new FormTextField(this.C.Server));
        p1.add(new MyLabel(Global.resourceString("Port__Use_23_for_Telnet_")));
        p1.add(this.Port = new FormTextField("" + this.C.Port));
        p1.add(new MyLabel(Global
                .resourceString("User__empty_for_manual_login_")));
        p1.add(this.User = new FormTextField("" + this.C.User));
        p1.add(new MyLabel(Global.resourceString("Password__empty_for_prompt_")));
        p1.add(this.Password = new JPasswordField("" + this.C.Password));
        p1.add(new MyLabel(Global
                .resourceString("Move_Style__move__if_unknown_")));
        p1.add(this.MChoice = new Choice());
        this.MChoice.setFont(Global.SansSerif);
        this.MChoice.add(Global.resourceString("move"));
        this.MChoice.add(Global.resourceString("move_number_time"));
        this.MChoice.add(Global.resourceString("move_time"));
        switch (this.C.MoveStyle) {
            case Connection.MOVE:
                this.MChoice.select(Global.resourceString("move"));
                break;
            case Connection.MOVE_N_TIME:
                this.MChoice.select(Global.resourceString("move_number_time"));
                break;
            case Connection.MOVE_TIME:
                this.MChoice.select(Global.resourceString("move_time"));
                break;
        }
        p1.add(new MyLabel(Global.resourceString("Encoding")));
        p1.add(this.Encoding = new FormTextField("" + this.C.Encoding));
        this.add("Center", new Panel3D(p1));
        final MyPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Set")));
        p.add(new ButtonAction(this, Global.resourceString("Add")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        p.add(new MyLabel(" "));
        p.add(new ButtonAction(this, Global.resourceString("Help")));
        this.add("South", new Panel3D(p));
        Global.setpacked(this, "edit", 300, 200, this.F);
        this.validate();
        this.setVisible(true);
        this.Name.requestFocus();
    }

    public EditConnection(CloseFrame F, ListClass clist, Go go) {
        super(F, Global.resourceString("Edit_Connection"), true);
        this.G = go;
        this.CList = clist;
        final MyPanel p1 = new MyPanel();
        p1.setLayout(new GridLayout(0, 2));
        p1.add(new MyLabel(Global.resourceString("Name")));
        p1.add(this.Name = new FormTextField(Global
                .resourceString("Server_shortcut__IGS_")));
        p1.add(new MyLabel(Global.resourceString("Server")));
        p1.add(this.Server = new FormTextField(Global
                .resourceString("Server_name__igs_nuri_net_")));
        p1.add(new MyLabel(Global.resourceString("Port__Use_23_for_Telnet_")));
        p1.add(this.Port = new FormTextField(Global
                .resourceString("Port__6969_")));
        p1.add(new MyLabel(Global
                .resourceString("User__empty_for_manual_login_")));
        p1.add(this.User = new FormTextField(Global
                .resourceString("User_name__kingkong_")));
        p1.add(new MyLabel(Global.resourceString("Password__empty_for_prompt_")));
        p1.add(this.Password = new JPasswordField(""));
        p1.add(new MyLabel(Global
                .resourceString("Move_Style__move__if_unknown_")));
        p1.add(this.MChoice = new Choice());
        this.MChoice.setFont(Global.SansSerif);
        this.MChoice.add(Global.resourceString("move"));
        this.MChoice.add(Global.resourceString("move_number_time"));
        this.MChoice.add(Global.resourceString("move_time"));
        this.MChoice.select(Global.resourceString("move"));
        this.add("Center", new Panel3D(p1));
        p1.add(new MyLabel(Global.resourceString("Encoding")));
        p1.add(this.Encoding = new FormTextField(Global.isApplet() ? "ASCII"
                : System.getProperty("file.encoding")));
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Add")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        p.add(new MyLabel(" "));
        p.add(new ButtonAction(this, Global.resourceString("Help")));
        this.add("South", new Panel3D(p));
        Global.setpacked(this, "edit", 300, 200, F);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "edit");
        if (Global.resourceString("Set").equals(o)) {
            this.C.Name = this.Name.getText();
            this.C.Server = this.Server.getText();
            try {
                this.C.Port = Integer.parseInt(this.Port.getText());
            } catch (final NumberFormatException ex) {
                this.C.Port = 6969;
            } finally {
                this.C.User = this.User.getText();
                this.C.Password = this.Password.getText();
                switch (this.MChoice.getSelectedIndex()) {
                    case 0:
                        this.C.MoveStyle = Connection.MOVE;
                        break;
                    case 1:
                        this.C.MoveStyle = Connection.MOVE_N_TIME;
                        break;
                    case 2:
                        this.C.MoveStyle = Connection.MOVE_TIME;
                        break;
                }
                this.C.Encoding = this.Encoding.getText();
                this.G.updatelist();
                this.setVisible(false);
                this.dispose();
            }
        } else if (Global.resourceString("Add").equals(o)) {
            final Connection C = new Connection("[?] [?] [?] [?] [?] [?]");
            C.Name = this.Name.getText();
            C.Server = this.Server.getText();
            try {
                C.Port = Integer.parseInt(this.Port.getText());
            } catch (final NumberFormatException ex) {
                C.Port = 6969;
            } finally {
                C.User = this.User.getText();
                C.Password = this.Password.getText();
                switch (this.MChoice.getSelectedIndex()) {
                    case 0:
                        C.MoveStyle = Connection.MOVE;
                        break;
                    case 1:
                        C.MoveStyle = Connection.MOVE_N_TIME;
                        break;
                    case 2:
                        C.MoveStyle = Connection.MOVE_TIME;
                        break;
                }
                C.Encoding = this.Encoding.getText();
                if (this.G.find(C.Name) != null) {
                    C.Name = C.Name + " DUP";
                }
                this.CList.append(new ListElement(C));
                this.G.updatelist();
                this.setVisible(false);
                this.dispose();
            }
        } else if (Global.resourceString("Cancel").equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Help").equals(o)) {
            new HelpDialog(this.F, "configure");
        } else {
            super.doAction(o);
        }
    }
}
