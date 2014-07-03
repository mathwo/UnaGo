package rene.util.mail;

import java.util.Enumeration;
import java.util.Vector;

public class MailMessage {
    Vector V;

    public MailMessage() {
        this.V = new Vector();
    }

    public void addLine(String s) {
        this.V.addElement(s);
    }

    public String date() {
        return this.find("date:");
    }

    public String find(String a) {
        for (int i = 0; i < this.V.size(); i++) {
            final String s = (String) this.V.elementAt(i);
            if (s.toLowerCase().startsWith(a)) {
                return s.substring(a.length()).trim();
            }
        }
        return "???";
    }

    public String from() {
        return this.find("from:");
    }

    Enumeration getMessage() {
        return this.V.elements();
    }

    public String subject() {
        return this.find("subject:");
    }
}
