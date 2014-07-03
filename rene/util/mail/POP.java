package rene.util.mail;

import java.io.*;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Implements a POP3 client.
 */

public class POP {
    public static void main(String args[]) {
        try {
            final POP pop = new POP(args[0]);
            pop.open();
            pop.login(args[1], args[2]);
            final int n = pop.getNumberOfMessages();
            System.out.println(n + " Messages!");
            Hashtable h = new Hashtable();
            pop.saveUIDL(h);
            h = new Hashtable();
            final int ids[] = pop.getNewMessages(h);
            for (int i = 0; i < ids.length; i++) {
                System.out.println("----- New Message :");
                final MailMessage m = pop.getMessageHeader(ids[i]);
                if (m != null) {
                    System.out.println("Last Message:");
                    System.out.println(m.from() + ", " + m.date());
                    System.out.println(m.subject());
                }
            }
            pop.close();
        } catch (final Exception e) {
            System.out.println(e);
        }
    }

    BufferedReader In;
    PrintWriter Out;
    Socket S;
    String Server;
    int Port;
    String Answer;

    int TotalSize;

    public POP(String server) {
        this(server, 110);
    }

    /**
     * Open the connection to the server, default is port 110.
     */
    public POP(String server, int port) {
        this.Server = server;
        this.Port = port;
    }

    /**
     * Close the connection.
     */
    public void close() throws IOException {
        this.In.close();
        this.Out.close();
    }

    /**
     * Get an answer from the server, ignore everything but +OK and -ERR. Store
     * the answer string in Answer.
     *
     * @return True, if answer is +OK
     */
    boolean expectAnswer() throws IOException {
        while (true) {
            final String s = this.In.readLine();
            if (s == null) {
                throw new IOException("Connection closed");
            }
            if (s.startsWith("+OK")) {
                this.Answer = this.stripAnswer(s, 3);
                return true;
            } else if (s.startsWith("-ERR")) {
                this.Answer = this.stripAnswer(s, 4);
                return false;
            }
        }
    }

    /**
     * @return The answer of the last command
     */
    public String getAnswer() {
        return this.Answer;
    }

    /**
     * Get the message (header only) with number i.
     *
     * @param i
     *            Number of message
     */
    public MailMessage getMessageHeader(int i) throws IOException, POPException {
        this.send("TOP " + i + " " + 0);
        if (!this.expectAnswer()) {
            this.send("RETR " + i);
            if (!this.expectAnswer()) {
                throw new POPException("retr");
            }
        }
        return this.getMessageText();
    }

    MailMessage getMessageText() throws IOException, POPException {
        final MailMessage m = new MailMessage();
        while (true) {
            final String s = this.In.readLine();
            if (s == null) {
                throw new POPException("retr");
            }
            if (s.equals(".")) {
                break;
            }
            m.addLine(s);
        }
        return m;
    }

    /**
     * Return an integer array of new message ids
     */
    public int[] getNewMessages(Hashtable h) throws POPException, IOException {
        this.send("UIDL");
        if (!this.expectAnswer()) {
            throw new POPException("uidl");
        }
        final MailMessage m = this.getMessageText();
        final Enumeration e = m.getMessage();
        final Vector V = new Vector();
        while (e.hasMoreElements()) {
            final String s = (String) e.nextElement();
            final int i = s.indexOf(' ');
            if (h.get(s.substring(i).trim()) == null) {
                int id;
                try {
                    id = Integer.parseInt(s.substring(0, i));
                } catch (final Exception ex) {
                    throw new POPException("uidl");
                }
                V.addElement(new Integer(id));
            }
        }
        final int ids[] = new int[V.size()];
        for (int i = 0; i < V.size(); i++) {
            ids[i] = ((Integer) V.elementAt(i)).intValue();
        }
        return ids;
    }

    /**
     * Get the number of messages and the total size. The size is stored in
     * TotalSize.
     *
     * @return Number of messages.
     */
    public int getNumberOfMessages() throws IOException, POPException {
        this.send("STAT");
        if (!this.expectAnswer()) {
            throw new POPException("status");
        }
        final StringTokenizer s = new StringTokenizer(this.Answer, " ");
        if (!s.hasMoreTokens()) {
            throw new POPException("status");
        }
        int n;
        try {
            n = Integer.parseInt(s.nextToken());
        } catch (final Exception e) {
            throw new POPException("status");
        }
        if (!s.hasMoreTokens()) {
            throw new POPException("status");
        }
        try {
            this.TotalSize = Integer.parseInt(s.nextToken());
        } catch (final Exception e) {
            throw new POPException("status");
        }
        return n;
    }

    /**
     * Only available after getNumberOfMessages()!
     *
     * @return Total size of messages.
     */
    public int getTotalSize() {
        return this.TotalSize;
    }

    /**
     * Log into the server.
     *
     * @return successful login.
     */
    public boolean login(String user, String password) throws IOException {
        this.send("USER " + user);
        if (!this.expectAnswer()) {
            return false;
        }
        this.send("PASS " + password);
        if (!this.expectAnswer()) {
            return false;
        }
        return true;
    }

    /**
     * Open a connection and wait for a positive answer.
     */
    public void open() throws IOException {
        this.S = new Socket(this.Server, this.Port);
        this.In = new BufferedReader(new InputStreamReader(new DataInputStream(
                this.S.getInputStream())));
        this.Out = new PrintWriter(this.S.getOutputStream());
        if (!this.expectAnswer()) {
            throw new IOException("Could not connect!");
        }
    }

    /**
     * Save the UDILs of the messages to the Hash table.
     */
    public void saveUIDL(Hashtable h) throws POPException, IOException {
        this.send("UIDL");
        if (!this.expectAnswer()) {
            throw new POPException("uidl");
        }
        final MailMessage m = this.getMessageText();
        final Enumeration e = m.getMessage();
        while (e.hasMoreElements()) {
            final String s = (String) e.nextElement();
            final int i = s.indexOf(' ');
            if (i >= 0) {
                h.put(s.substring(i).trim(), s);
            }
        }
    }

    /**
     * Send a text to the server.
     */
    public void send(String s) throws IOException {
        this.Out.println(s);
        this.Out.flush();
    }

    /**
     * Strip the answer string.
     */
    public String stripAnswer(String s, int pos) {
        return s.substring(pos).trim();
    }
}

class POPException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public POPException(String s) {
        super(s);
    }
}
