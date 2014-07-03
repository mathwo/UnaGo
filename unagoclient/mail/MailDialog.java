package unagoclient.mail;

import unagoclient.Global;
import unagoclient.dialogs.Message;
import unagoclient.gui.*;
import rene.util.mail.MailCallback;
import rene.util.mail.SendMail;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

/**
 * This dialog is used to send mail somewhere. The user must enter the SMTP
 * host, the address and his own address, the subject and the message.
 */

public class MailDialog extends CloseDialog implements MailCallback {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String Message;
    TextArea T;
    JTextField From, To, Subject, Host;
    Frame F;

    /**
     * @param s
     *            the message (may be edited by the user)
     */
    public MailDialog(Frame f, String s) {
        super(f, Global.resourceString("Mail_Game"), false);
        this.Message = s;
        this.F = f;
        this.setLayout(new BorderLayout());
        final JPanel p = new MyPanel();
        p.setLayout(new GridLayout(0, 2));
        p.add(new MyLabel("From : "));
        p.add(this.From = new FormTextField(rene.gui.Global.getParameter(
                "from", Global.resourceString("Your_EMail_Address"))));
        p.add(new MyLabel("To : "));
        p.add(this.To = new FormTextField(rene.gui.Global.getParameter("to",
                Global.resourceString("Destination_EMail_Address"))));
        p.add(new MyLabel("Subject : "));
        p.add(this.Subject = new FormTextField(Global
                .resourceString("SGF_File")));
        p.add(new MyLabel(Global.resourceString("SMTP_host___")));
        p.add(this.Host = new GrayTextField(rene.gui.Global.getParameter(
                "smtp", "SMTP host")));
        p.add(new MyLabel(Global.resourceString("This_host___")));
        try {
            p.add(new MyLabel(InetAddress.getByName("localhost").getHostName()));
        } catch (final Exception e) {
            p.add(new MyLabel(Global.resourceString("Unknown_host")));
        }
        this.add("North", p);
        this.add("Center", this.T = new TextArea());
        this.T.setFont(Global.Monospaced);
        this.T.setText(s);
        final JPanel bp = new MyPanel();
        bp.add(new ButtonAction(this, Global.resourceString("Send")));
        bp.add(new ButtonAction(this, Global.resourceString("Close")));
        this.add("South", bp);
        Global.setwindow(this, "mail", 400, 400);
        this.validate();
        this.setVisible(true);
    }

    @Override
    public void doAction(String o) {
        Global.notewindow(this, "mail");
        if (Global.resourceString("Send").equals(o)) {
            rene.gui.Global.setParameter("from", this.From.getText());
            rene.gui.Global.setParameter("to", this.To.getText());
            rene.gui.Global.setParameter("smtp", this.Host.getText());
            final SendMail s = new SendMail(this.Host.getText(),
                    this.To.getText(), this.From.getText());
            s.send(this.Subject.getText(), this.Message, this);
        } else if (Global.resourceString("Close").equals(o)) {
            this.setVisible(false);
            this.dispose();
        } else {
            super.doAction(o);
        }
    }

    @Override
    public void result(boolean flag, String s) {
        new Message(Global.frame(), s);
    }
}
