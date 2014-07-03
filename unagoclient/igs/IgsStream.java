package unagoclient.igs;

import unagoclient.Dump;
import unagoclient.Global;
import unagoclient.dialogs.Message;
import unagoclient.sound.UnaGoSound;
import rene.util.list.ListClass;
import rene.util.list.ListElement;
import rene.util.parser.StringParser;

import java.io.*;

/**
 * This class handles the line by line input from the server, parsing it for
 * command numbers and sub-numbers.
 * <p>
 * Furthermore, it will filter the input and distribute it to the distributors.
 * The class initializes a list of distributors. All distributors must chain
 * themselves to this list. They can unchain themselves, if they do no longer
 * wish to receive input.
 * <p>
 * The input is done via a BufferedReader, which does the encoding stuff.
 */

public class IgsStream {
    String Line;
    char C[];
    final int linesize = 4096;
    int L;
    int Number;
    String Command;
    ListClass DistributorList;
    PrintWriter Out;
    BufferedReader In;
    ConnectionFrame CF;

    int lastcr = 0, lastcommand = 0;

    /**
     * The in and out streams to the server are opened by the ConnectionFrame.
     * However, the input stream is used for a BufferedReader, which does local
     * decoding. The output stream is already assumed to be using the correct
     * encoding.
     *
     * @see unagoclient.igs.ProxyIgsStream
     */
    public IgsStream(ConnectionFrame cf, InputStream in, PrintWriter out) {
        this.CF = cf;
        this.Out = out;
        this.Line = "";
        this.L = 0;
        this.Number = 0;
        Dump.println("--> IgsStream opened");
        this.DistributorList = new ListClass();
        this.C = new char[this.linesize];
        this.initstream(in);
    }

    /**
     * test, if there is another character waiting
     */
    public boolean available() throws IOException {
        return this.In.ready();
    }

    public void close() throws IOException {
        this.In.close();
    }

    public String command() {
        return this.Command;
    }

    public int commandnumber() {
        try {
            return Integer.parseInt(this.Command, 10);
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Chaines a new distributor to the distributor list.
     */
    public void distributor(Distributor o) {
        synchronized (this.DistributorList) {
            this.DistributorList.append(new ListElement(o));
        }
    }

    public Distributor findDistributor(int n) {
        synchronized (this.DistributorList) {
            ListElement l = this.DistributorList.first();
            Distributor dis;
            while (l != null) {
                dis = (Distributor) l.content();
                if (dis.number() == n) {
                    return dis;
                }
                l = l.next();
            }
            return null;
        }
    }

    public Distributor findDistributor(int n, int g) {
        synchronized (this.DistributorList) {
            ListElement l = this.DistributorList.first();
            Distributor dis;
            while (l != null) {
                dis = (Distributor) l.content();
                if (dis.number() == n) {
                    if (dis.game() == g) {
                        return dis;
                    }
                }
                l = l.next();
            }
            return null;
        }
    }

    /**
     * Seeks a game distributor, which waits for that game.
     */
    public boolean gamewaiting(int g) {
        synchronized (this.DistributorList) {
            ListElement l = this.DistributorList.first();
            Distributor dis;
            while (l != null) {
                dis = (Distributor) l.content();
                if (dis.number() == 15) {
                    if (dis.game() == g) {
                        return true;
                    }
                }
                l = l.next();
            }
            return false;
        }
    }

    public BufferedReader getInputStream() {
        return this.In;
    }

    /**
     * This initializes a BufferedReader to do the decoding.
     */
    public void initstream(InputStream in) {
        try {
            InputStream ina;
            String encoding = this.CF.Encoding;
            if (encoding.startsWith("!")) {
                ina = in;
                encoding = encoding.substring(1);
            } else {
                ina = new TelnetStream(this.CF, in, this.Out);
            }
            if (encoding.equals("")) {
                this.In = new BufferedReader(new InputStreamReader(ina));
            } else {
                this.In = new BufferedReader(new InputStreamReader(ina,
                        encoding));
            }
        } catch (final UnsupportedEncodingException e) {
            this.CF.append(e.toString() + "\n");
            this.In = new BufferedReader(new InputStreamReader(in));
        } catch (final IllegalArgumentException e) {
            this.CF.append(e.toString() + "\n");
            this.In = new BufferedReader(new InputStreamReader(in));
        }
    }

    public String line() {
        return this.Line;
    }

    public int number() {
        return this.Number;
    }

    public void out(String s) {
        if (s.startsWith("observe") || s.startsWith("moves")
                || s.startsWith("status")) {
            return;
        }
        this.Out.println(s);
        this.Out.flush();
        Dump.println("Sending: " + s);
    }

    public char read() throws IOException {
        while (true) {
            final int c = this.In.read();
            if (c == -1 || c == 255 && this.lastcommand == 255) {
                Dump.println("Received character " + c);
                throw new IOException();
            }
            this.lastcommand = c;
            if (c == 10) {
                if (this.lastcr == 13) {
                    this.lastcr = 0;
                    continue;
                }
                this.lastcr = 10;
                return '\n';
            } else if (c == 13) {
                if (this.lastcr == 10) {
                    this.lastcr = 0;
                    continue;
                }
                this.lastcr = 13;
                return '\n';
            } else {
                this.lastcr = 0;
            }
            return (char) c;
        }
    }

    /**
     * The most important method of this class.
     * <p>
     * This method reads input from the server line by line, filtering out and
     * answering Telnet protocol characters. If it receives a full line it will
     * interpret it and return true. Otherwise, it will return false. The line
     * can be read from the Line variable. Incomplete lines happen only at the
     * start of the connection during login.
     * <p>
     * Interpreting a lines means determining its command number and an eventual
     * sub-command number. Both are used to determine the right distributor for
     * this command, if there is one. Otherwise, the function returns true and
     * InputThread handles the command.
     * <p>
     * The protocol is not very logic, nor is the structure of this method. It
     * has cases for several distributors. Probably, this code should be a
     * static method of the distributor.
     *
     * @see unagoclient.igs.InputThread
     */
    public boolean readline() throws IOException {
        boolean full;
        StringParser sp;
        outerloop: while (true) {
            full = false;
            char b = 0;
            b = this.read();
            while (true) {
                if (this.L >= this.linesize) {
                    Dump.println("IGS : Buffer overflow");
                    throw new IOException("Buffer Overflow");
                }
                if (b == '\n') {
                    full = true;
                    break;
                }
                this.C[this.L++] = b;
                if (!this.available()) {
                    break;
                }
                b = this.read();
            }
            // Dump.println(L+" characters received from server");
            this.Line = new String(this.C, 0, this.L);
            Dump.println("IGS sent: " + this.Line);
            this.Number = 0;
            this.Command = "";
            if (full) {
                int i;
                for (i = 0; i < this.L; i++) {
                    if (this.C[i] < '0' || this.C[i] > '9') {
                        break;
                    }
                }
                if (i > 0 && this.C[i] == ' ') {
                    try {
                        this.Number = Integer.parseInt(
                                new String(this.C, 0, i), 10);
                    } catch (final Exception e) {
                        break;
                    }
                    this.Command = new String(this.C, i + 1, this.L - (i + 1));
                } else {
                    this.Number = 100;
                    this.Command = new String(this.C, 0, this.L);
                }
                this.L = 0;
                loop1: while (true) {
                    Dump.println("loop1 with " + this.Number + " "
                            + this.Command);
                    if (this.Number == 21
                            && (this.Command.startsWith("{Game") || this.Command
                                    .startsWith("{ Game"))) {
                        sp = new StringParser(this.Command);
                        sp.skip("{");
                        sp.skipblanks();
                        sp.skip("Game");
                        sp.skipblanks();
                        if (!sp.isint()) {
                            continue outerloop;
                        }
                        final int G = sp.parseint(':');
                        final Distributor dis = this.findDistributor(15, G);
                        if (dis != null) {
                            Dump.println("Sending comment to game " + G);
                            dis.send(this.Command);
                            this.L = 0;
                            continue outerloop;
                        }
                        if (rene.gui.Global.getParameter("reducedoutput", true)
                                && !Global.posfilter(this.Command)) {
                            continue outerloop;
                        }
                    } else if (this.Number == 21 && !this.CF.Waitfor.equals("")
                            && this.Command.indexOf(this.CF.Waitfor) >= 0) {
                        new Message(this.CF, this.Command);
                    } else if (this.Number == 21
                            && this.Command.startsWith("{")
                            && rene.gui.Global.getParameter("reducedoutput",
                                    true) && !Global.posfilter(this.Command)) {
                        continue outerloop;
                    } else if (this.Number == 21
                            && Global.posfilter(this.Command)) {
                        UnaGoSound.play("message", "wip", true);
                    } else if (this.Number == 11
                            && this.Command.startsWith("Kibitz")) {
                        sp = new StringParser(this.Command);
                        sp.upto('[');
                        if (sp.error()) {
                            continue outerloop;
                        }
                        sp.upto(']');
                        if (sp.error()) {
                            continue outerloop;
                        }
                        sp.upto('[');
                        if (sp.error()) {
                            continue outerloop;
                        }
                        sp.skip("[");
                        sp.skipblanks();
                        if (!sp.isint()) {
                            continue outerloop;
                        }
                        final int G = sp.parseint(']');
                        final Distributor dis = this.findDistributor(15, G);
                        if (dis != null) {
                            Dump.println("Sending kibitz to game " + G);
                            dis.send(this.Command);
                            this.sendall("Kibitz-> ", dis);
                            continue loop1;
                        }
                    } else if (this.Number == 9
                            && this.Command.startsWith("Removing @")) {
                        sp = new StringParser(this.Command);
                        sp.skip("Removing @");
                        final Distributor dis = this.findDistributor(15);
                        if (dis != null) {
                            Dump.println("Got " + this.Command);
                            dis.send(this.Command);
                            this.sendall(dis);
                            continue loop1;
                        }
                        continue outerloop;
                    } else if (this.Number == 9
                            && !this.Command.startsWith("File")) {
                        final Distributor dis = this.findDistributor(9);
                        if (dis != null) {
                            Dump.println("Sending information");
                            dis.send(this.Command);
                            this.sendall(dis);
                            Dump.println("End of information");
                            continue loop1;
                        }
                    } else if (this.Number == 15
                            && this.Command.startsWith("Game")) {
                        sp = new StringParser(this.Command);
                        sp.skip("Game");
                        sp.skipblanks();
                        if (!sp.isint()) {
                            continue outerloop;
                        }
                        final int G = sp.parseint();
                        Distributor dis = this.findDistributor(15, G);
                        if (dis != null) {
                            Dump.println("Sending to game " + G);
                            dis.send(this.Command);
                            this.sendall(dis);
                            continue loop1;
                        }
                        dis = this.findDistributor(15, -1);
                        if (dis != null) {
                            Dump.println("Game " + G + " started");
                            dis.game(G);
                            dis.send(this.Command);
                            this.sendall(dis);
                            continue loop1;
                        }
                        continue outerloop;
                    } else if (this.Number == 32) {
                        sp = new StringParser(this.Command);
                        final int G = sp.parseint(':');
                        sp.skip(":");
                        sp.skipblanks();
                        if (G == 0) {
                            this.Number = 9;
                            continue loop1;
                        }
                        final Distributor dis = this.findDistributor(32, G);
                        if (dis != null) {
                            Dump.println("Sending to channel " + G);
                            dis.send(sp.upto((char) 0));
                            continue outerloop;
                        }
                        final ChannelDistributor cd = new ChannelDistributor(
                                this.CF, this, this.Out, G);
                        cd.send(sp.upto((char) 0));
                        continue outerloop;
                    } else if (this.Number == 22) {
                        final Distributor dis = this.findDistributor(22);
                        if (dis != null) {
                            dis.send(this.Command);
                            this.sendall(dis);
                            continue loop1;
                        }
                        final IgsGoFrame gf = new IgsGoFrame(this.CF,
                                "Peek game");
                        gf.setVisible(true);
                        gf.repaint();
                        final Status s = new Status(gf, this, this.Out);
                        s.PD.send(this.Command);
                        this.sendall(s.PD);
                    } else if (this.Number == 9
                            && this.Command.startsWith("-- ")) {
                        final Distributor dis = this.findDistributor(32);
                        if (dis != null) {
                            Dump.println("Sending to channel " + dis.game());
                            dis.send(this.Command);
                            continue outerloop;
                        }
                        continue outerloop;
                    } else if (this.Number == 1
                            && (this.Command.startsWith("8")
                                    || this.Command.startsWith("5") || this.Command
                                    .startsWith("6"))) {
                        Dump.println("1 received " + this.Command);
                    } else if (this.Number != 9) {
                        final Distributor dis = this
                                .findDistributor(this.Number);
                        if (dis != null) {
                            dis.send(this.Command);
                            this.sendall(dis);
                            continue loop1;
                        } else {
                            Dump.println("Distributor " + this.Number
                                    + " not found");
                        }
                    }
                    break;
                }
                this.L = 0;
            }
            break;
        }
        return full;
    }

    /**
     * This reads a complete line from the server.
     */
    void readlineprim() throws IOException {
        char b = this.read();
        this.L = 0;
        while (true) {
            if (this.L >= this.linesize || b == '\n') {
                if (this.L >= this.linesize) {
                    Dump.println("IGS : Buffer overflow");
                }
                break;
            }
            this.C[this.L++] = b;
            b = this.read();
        }
        this.Number = 0;
        this.Command = "";
        int i;
        for (i = 0; i < this.L; i++) {
            if (this.C[i] < '0' || this.C[i] > '9') {
                break;
            }
        }
        if (i > 0) {
            this.Number = Integer.parseInt(new String(this.C, 0, i), 10);
            this.Command = new String(this.C, i + 1, this.L - (i + 1));
        } else {
            this.Number = 0;
            this.Command = new String(this.C, 0, this.L);
        }
        this.L = 0;
    }

    /**
     * Removes all distributors from the distributor list.
     */
    public void removeall() {
        synchronized (this.DistributorList) {
            ListElement l = this.DistributorList.first();
            while (l != null) {
                ((Distributor) l.content()).remove();
                l = l.next();
            }
        }
    }

    /**
     * Processes the input, until there is a line, which is not suited for the
     * specified distributor.
     */
    void sendall(Distributor dis) throws IOException {
        while (true) {
            this.readlineprim();
            Dump.println("IGS:(" + this.Number + " for " + dis.number() + ") "
                    + this.Command);
            if (this.Number == dis.number()) {
                dis.send(this.Command);
            } else if (this.Number == 9) // information got in other content
            {
                final Distributor dist = this.findDistributor(9);
                if (dist != null) {
                    Dump.println("Sending information");
                    dist.send(this.Command);
                    // sendall(dist); // logical error
                }
            } else {
                if (dis.once()) {
                    this.unchain(dis);
                    dis.finished();
                    Dump.println("Distributor " + dis.number() + " finished");
                }
                break;
            }
        }
        Dump.println("sendall() for " + dis.number() + " finished");
        dis.allsended();
    }

    /**
     * Same as above, but the distrubutor get the input with the String
     * prepended.
     */
    void sendall(String s, Distributor dis) throws IOException {
        while (true) {
            this.readlineprim();
            Dump.println("IGS: " + this.Command);
            if (this.Number == 11) {
                dis.send(s + this.Command);
            } else {
                if (dis.once()) {
                    this.unchain(dis);
                    dis.finished();
                }
                break;
            }
        }
        dis.allsended();
    }

    public void unchain(Distributor o) {
        try {
            synchronized (this.DistributorList) {
                ListElement l = this.DistributorList.first();
                while (l != null) {
                    if ((Distributor) l.content() == o) {
                        this.DistributorList.remove(l);
                    }
                    l = l.next();
                }
            }
        } catch (final Exception e) {
        }
    }

}
