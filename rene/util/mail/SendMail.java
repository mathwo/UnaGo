package rene.util.mail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.Socket;

/**
 * This mail class sends a message via SMTP. For an applet, the smtp server must
 * be the web server for security reasons.
 */

public class SendMail implements Runnable {
    public String result = "";
    String lastLine;
    // DataInputStream in;
    BufferedReader bufferedReader;
    String mailHost, from, to;

    String subject, message;

    MailCallback mailCallback;

    /**
     * @param smtp
     *            The server name.
     * @param to
     *            The email address of the recipient
     * @param from
     *            The email address of the sender
     */
    public SendMail(String smtp, String to, String from) {
        this.mailHost = smtp;
        from = from;
        to = to;
    }

    public void expect(String expected, String msg) throws Exception {
        this.lastLine = this.bufferedReader.readLine();
        if (!this.lastLine.startsWith(expected)) {
            throw new Exception(msg + ":" + this.lastLine);
        }
        while (this.lastLine.startsWith(expected + "-")) {
            this.lastLine = this.bufferedReader.readLine();
        }
    }

    @Override
    public void run() {
        Socket s = null;
        try {
            s = new Socket(this.mailHost, 25);
            final PrintStream p = new PrintStream(s.getOutputStream(), true);
            // in = new DataInputStream(s.getInputStream());
            this.bufferedReader = new BufferedReader(new InputStreamReader(
                    s.getInputStream()));
            this.expect("220", "greetings");
            p.println("HELO " + "helohost");
            this.expect("250", "helo");
            p.println("MAIL FROM: " + this.from);
            this.expect("250", "mail from");
            p.println("RCPT TO: " + this.to);
            this.expect("250", "rcpt to");
            p.println("DATA");
            this.expect("354", "data");
            p.println("Subject: " + this.subject);
            // DataInputStream is = new DataInputStream(new
            // StringBufferInputStream(Message));
            final BufferedReader br = new BufferedReader(new StringReader(
                    this.message));
            try {
                while (true) {
                    String ln = br.readLine();
                    if (ln == null) {
                        break;
                    }
                    if (ln.equals(".")) {
                        ln = "..";
                    }
                    p.println(ln);
                }
            } catch (final Exception e) {
            }
            p.println("");
            p.println(".");
            this.expect("250", "end of data");
            p.println("QUIT");
            this.expect("221", "quit");
        } catch (final Exception e) {
            this.result = e.getMessage();
            this.mailCallback.result(false, "Send error!");
            return;
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
            } catch (final Exception e) {
                this.result = e.getMessage();
            }
        }
        this.mailCallback.result(true, "Mail sent successfully!");
    }

    /**
     * Send a message. The MailCallback is called, when the message is
     * delivered, or if they are problems.
     *
     * @param subject
     *            The message subject.
     * @param message
     *            The message body.
     * @param cb
     *            , that implements the MailCallback interface.
     * @see rene.util.mail.MailCallback
     */
    public void send(String subject, String message, MailCallback cb) {
        subject = subject;
        message = message;
        this.mailCallback = cb;
        new Thread(this).start();
    }
}
