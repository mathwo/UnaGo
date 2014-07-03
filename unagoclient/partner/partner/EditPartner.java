package unagoclient.partner.partner;

import unagoclient.Global;
import unagoclient.Go;
import unagoclient.dialogs.HelpDialog;
import unagoclient.gui.*;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import javax.swing.*;
import java.awt.*;

public class EditPartner extends CloseDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ListClass PList;
    Partner C;
    JTextField Name, Server, Port;
    Go G;
    Choice State;
    Frame F;

    public EditPartner(CloseFrame f, ListClass plist, Go go) {
        super(f, Global.resourceString("Edit_Connection"), true);
        this.G = go;
        this.F = f;
        this.PList = plist;
        final JPanel p1 = new MyPanel();
        p1.setLayout(new GridLayout(0, 2));
        p1.add(new MyLabel(Global.resourceString("Name")));
        p1.add(this.Name = new FormTextField(Global
                .resourceString("Name_for_list")));
        p1.add(new MyLabel(Global.resourceString("Server")));
        p1.add(this.Server = new FormTextField(Global
                .resourceString("Internet_server_name")));
        p1.add(new MyLabel(Global.resourceString("Port")));
        p1.add(this.Port = new FormTextField("Port (default 6970)"));
        p1.add(new MyLabel(Global.resourceString("State")));
        p1.add(this.State = new Choice());
        this.State.setFont(Global.SansSerif);
        this.State.add(Global.resourceString("silent"));
        this.State.add(Global.resourceString("private"));
        this.State.add(Global.resourceString("local"));
        this.State.add(Global.resourceString("public"));
        this.State.select(1);
        this.add("Center", new Panel3D(p1));
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Add")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        p.add(new MyLabel(" "));
        p.add(new ButtonAction(this, Global.resourceString("Help")));
        this.add("South", new Panel3D(p));
        Global.setpacked(this, "editp", 300, 200, this.F);
        this.validate();
        this.setVisible(true);
        this.Name.requestFocus();
    }

    public EditPartner(CloseFrame f, ListClass plist, Partner c, Go go) {
        super(f, Global.resourceString("Edit_Connection"), true);
        this.G = go;
        this.F = f;
        this.PList = plist;
        this.C = c;
        final JPanel p1 = new MyPanel();
        p1.setLayout(new GridLayout(0, 2));
        p1.add(new MyLabel(Global.resourceString("Name")));
        p1.add(this.Name = new FormTextField("" + this.C.Name));
        p1.add(new MyLabel(Global.resourceString("Server")));
        p1.add(this.Server = new FormTextField(this.C.Server));
        p1.add(new MyLabel(Global.resourceString("Port")));
        p1.add(this.Port = new FormTextField("" + this.C.Port));
        p1.add(new MyLabel(Global.resourceString("State")));
        p1.add(this.State = new Choice());
        this.State.setFont(Global.SansSerif);
        this.State.add(Global.resourceString("silent"));
        this.State.add(Global.resourceString("private"));
        this.State.add(Global.resourceString("local"));
        this.State.add(Global.resourceString("public"));
        this.State.select(this.C.State);
        this.add("Center", new Panel3D(p1));
        final JPanel p = new MyPanel();
        p.add(new ButtonAction(this, Global.resourceString("Set")));
        p.add(new ButtonAction(this, Global.resourceString("Add")));
        p.add(new ButtonAction(this, Global.resourceString("Cancel")));
        p.add(new MyLabel(" "));
        p.add(new ButtonAction(this, Global.resourceString("Help")));
        this.add("South", new Panel3D(p));
        Global.setpacked(this, "editp", 300, 200, this.F);
        this.validate();
        this.setVisible(true);
        this.Name.requestFocus();
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "editp");
        if (Global.resourceString("Set").equals(o)) {
            this.C.Name = this.Name.getText();
            this.C.Server = this.Server.getText();
            this.C.State = this.State.getSelectedIndex();
            try {
                this.C.Port = Integer.parseInt(this.Port.getText());
            } catch (final NumberFormatException ex) {
                this.C.Port = 6970;
            } finally {
                this.G.updateplist();
                this.setVisible(false);
                this.dispose();
            }
        } else if (Global.resourceString("Add").equals(o)) {
            final Partner C = new Partner("[?] [?] [?]");
            C.Name = this.Name.getText();
            C.Server = this.Server.getText();
            C.State = this.State.getSelectedIndex();
            try {
                C.Port = Integer.parseInt(this.Port.getText());
            } catch (final NumberFormatException ex) {
                C.Port = 6969;
            } finally {
                if (this.G.pfind(C.Name) != null) {
                    C.Name = C.Name + " DUP";
                }
                this.PList.append(new ListElement(C));
                this.G.updateplist();
                this.setVisible(false);
                this.dispose();
            }
        } else if (Global.resourceString("Cancel").equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else if (Global.resourceString("Help").equals(o)) {
            new HelpDialog(this.F, "confpartner");
        } else {
            super.doAction(o);
        }
    }
}
