package rene.util.ftp;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * An FTP protocol handler. See main for an example.
 */

public class SFTP {
    public static SSLSocket getSSLSocket(String server, int port)
            throws IOException, UnknownHostException {
        final SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory
                .getDefault();
        final SSLSocket S = (SSLSocket) ssf.createSocket(server, port);
        S.startHandshake();
        return S;
    }

    static public void main(String args[]) {
        try {
            final SFTP ftp = new SFTP(args[0]);
            ftp.open(args[1], args[2]);
            final Enumeration e = ftp.getCurrentDirectory().elements();
            while (e.hasMoreElements()) {
                System.out.println((String) e.nextElement());
            }
            ftp.close();
        } catch (final Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    String Server;
    int Port;
    SSLSocket S;
    BufferedReader In;

    PrintWriter Out;

    Answer A;

    SSLSocket DSocket;

    public SFTP(String server) {
        this(server, 22);
    }

    /**
     * Just denote the server and port (default is 21). The connection has to be
     * opened and closed!
     */
    public SFTP(String server, int port) {
        this.Server = server;
        this.Port = port;
    }

    /**
     * This returns the Answer to the previous command. The meanings of
     * Answer.code() are:
     * <ul>
     * <li>1xx - Another reply will follow
     * <li>2xx - The answer is positive
     * <li>3xx - The answer is positive, but needs more action
     * <li>4xx - The answer is negative, but one can try again
     * <li>5xx - The answer is negative
     * </ul>
     * A.text() will contain the text, the server sent. In case of multiline
     * text, this will be separated by \n.
     */
    public Answer answer() {
        return this.A;
    }

    /**
     * Cange the directory to the specified one.
     */
    public void changeDirectory(String dir) throws IOException {
        if (!this.command("CWD " + dir)) {
            throw new IOException("Directory change failed.");
        }
    }

    /**
     * Close the connection to the server and the control data streams.
     */
    public void close() throws IOException {
        this.send("QUIT");
        this.In.close();
        this.Out.close();
        this.S.close();
    }

    /**
     * Send a command to the server and wait for a direct reply. This will set
     * the Answer field for checking.
     *
     * @return true if the command succeeded immediately.
     */
    public boolean command(String s) throws IOException {
        this.send(s);
        return (this.getreply() / 100 < 4);
    }

    /**
     * Close the data socket after a get command and wait for the transfer
     * complete message.
     */
    public void getClose() throws IOException {
        this.DSocket.close();
        while (true) {
            this.getreply();
            if (this.A.code() == 226) {
                return;
            }
            if (this.A.code() >= 400) {
                throw new IOException("Put failed.");
            }
        }
    }

    public Vector getCurrentDirectory() throws IOException {
        return this.getDirectory(".");
    }

    /**
     * Get an input stream to the list. getClose() must be called!
     */
    public InputStream getDir(String path) throws IOException,
    UnknownHostException {
        this.DSocket = this.passive();
        if (!this.command("TYPE A")) {
            throw new IOException("Type A not supported?");
        }
        if (!path.equals("")) {
            this.send("LIST " + path);
        } else {
            this.send("LIST");
        }
        this.getreply();
        if (this.A.code() / 100 >= 4) {
            throw new IOException("ls failed.");
        }
        return this.DSocket.getInputStream();
    }

    public Vector getDirectory(String dir) throws IOException {
        final Vector v = new Vector();
        try {
            final BufferedReader In = new BufferedReader(new InputStreamReader(
                    this.getDir(dir)));
            while (true) {
                final String s = In.readLine();
                if (s == null) {
                    break;
                }
                v.addElement(s);
            }
            In.close();
        } catch (final Exception e) {
            throw new IOException("Directory list failed.");
        }
        this.getClose();
        return v;
    }

    /**
     * Get an input stream to the file. getClose() must be called!
     */
    public InputStream getFile(String file) throws IOException,
    UnknownHostException {
        this.DSocket = this.passive();
        if (!this.command("TYPE I")) {
            throw new IOException("Type I not supported?");
        }
        this.send("RETR " + file);
        this.getreply();
        if (this.A.code() / 100 >= 4) {
            throw new IOException("Get failed.");
        }
        return this.DSocket.getInputStream();
    }

    /**
     * Get an input stream to the list. getClose() must be called!
     */
    public InputStream getLs(String path) throws IOException,
    UnknownHostException {
        this.DSocket = this.passive();
        if (!this.command("TYPE A")) {
            throw new IOException("Type A not supported?");
        }
        if (!path.equals("")) {
            this.send("NLST " + path);
        } else {
            this.send("NLST");
        }
        this.getreply();
        if (this.A.code() / 100 >= 4) {
            throw new IOException("ls failed.");
        }
        return this.DSocket.getInputStream();
    }

    /**
     * Wait for reply from the server.
     *
     * @return the reply code (1xx,2xx,3xx are OK).
     */
    public int getreply() throws IOException {
        this.A = new Answer();
        this.A.get(this.In);
        // System.out.println(A.code());
        // System.out.println(A.text());
        return this.A.code();
    }

    /**
     * Open the connection to the server, getting the control data streams.
     */
    public void open() throws IOException, UnknownHostException {
        this.S = SFTP.getSSLSocket(this.Server, this.Port);
        this.In = new BufferedReader(new InputStreamReader(new DataInputStream(
                this.S.getInputStream())));
        this.Out = new PrintWriter(this.S.getOutputStream());
        if (this.getreply() / 100 != 2) {
            throw new IOException("Illegal reply.");
        }
    }

    /**
     * Open the server and connect as user with the password.
     */
    public void open(String user, String password) throws IOException,
    UnknownHostException {
        this.open();
        if (!this.command("USER " + user)) {
            throw new IOException("User not accepted.");
        }
        if (!this.command("PASS " + password)) {
            throw new IOException("Wrong Password");
        }
    }

    /**
     * Tell the server to wait for a data connection on a port of his
     * discretion. The return of the server is scanned for the IP and the port
     * and a Socket is generated.
     *
     * @return Socket for the data connection to the server
     */
    public SSLSocket passive() throws IOException, UnknownHostException,
    NumberFormatException {
        if (!this.command("PASV")) {
            throw new IOException("Passive mode not supported.");
        }
        final StringTokenizer p = new StringTokenizer(this.A.text(), "(,)");
        if (!p.hasMoreTokens()) {
            throw new IOException("Wrong answer from server.");
        } else {
            p.nextToken();
        }
        final int N[] = new int[4];
        for (int i = 0; i < 4; i++) {
            N[i] = Integer.parseInt(p.nextToken());
        }
        final int k = Integer.parseInt(p.nextToken());
        final int P = k * 256 + Integer.parseInt(p.nextToken());
        final String server = N[0] + "." + N[1] + "." + N[2] + "." + N[3];
        return SFTP.getSSLSocket(server, P);
    }

    /**
     * Close the date socket after a put command and wait for the transfer
     * complete message.
     */
    public void putClose() throws IOException {
        this.DSocket.close();
        while (true) {
            this.getreply();
            if (this.A.code() == 226) {
                return;
            }
            if (this.A.code() >= 400) {
                throw new IOException("Put failed.");
            }
        }
    }

    /**
     * Get an output stream to the file. putClose() must be called!
     */
    public OutputStream putFile(String file) throws IOException,
    UnknownHostException {
        this.DSocket = this.passive();
        if (!this.command("TYPE I")) {
            throw new IOException("Type I not supported?");
        }
        this.send("STOR " + file);
        this.getreply();
        if (this.A.code() / 100 >= 4) {
            throw new IOException("Put failed.");
        }
        return this.DSocket.getOutputStream();
    }

    /**
     * Send a command to the server.
     */
    public void send(String s) throws IOException {
        this.Out.println(s);
        this.Out.flush();
    }

}
