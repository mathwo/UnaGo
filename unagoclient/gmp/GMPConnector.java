package unagoclient.gmp;

import unagoclient.Dump;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

class GMPCloser extends Thread {
    GMPConnector C;

    public GMPCloser(GMPConnector c) {
        this.C = c;
        this.start();
    }

    @Override
    public void run() {
        this.C.destroy();
    }
}

/**
 * <p>
 * This class opens a connection to an external program, and communicates with
 * using pipes.
 * </P>
 * <p>
 * The communication handling is a bit difficult to understand, for it is done
 * asynchronically in a separate thread. The external program sends command,
 * which are automatically handled or answered by this class. To be able to do
 * this, the class needs an GMPInterface object, which provides the necessary
 * informations to answer questions (such as the board size), and handles
 * commands (such as a move). The interface is set with setGMPInterface().
 * </P>
 * <p>
 * Of course, commands to be sent to the external program can be sent via the
 * methods send(), move() etc. directly and asynchronically.
 * </P>
 * <p>
 * The GMP protocol is a binary protocol, which is not human readable. Moreover,
 * it has design flaws, when the programs disagree about the board size,
 * handicap etc. In our case, we expect the external program to ask for these
 * values. If it does not ask, a problem will arise. The communication might
 * fail in this case.
 * </P>
 */

public class GMPConnector implements Runnable {
    static String format(int i) {
        String s = "";
        for (int k = 0; k < 8; k++) {
            if (i % 2 != 0) {
                s = "1" + s;
            } else {
                s = "0" + s;
            }
            i = i >> 1;
        }
        return s;
    }

    static String[] getcommand(String s) {
        final Vector<String> V = new Vector<String>();
        while (true) {
            s = s.trim();
            if (s.length() == 0) {
                break;
            }
            if (s.startsWith("\"")) {
                final int pos = s.indexOf("\"", 1);
                if (pos < 0) {
                    V.add(s.substring(1));
                    break;
                }
                V.add(s.substring(1, pos));
                s = s.substring(pos + 1);
            } else {
                final int pos = s.indexOf(" ", 1);
                if (pos < 0) {
                    V.add(s.substring(0));
                    break;
                }
                V.add(s.substring(0, pos));
                s = s.substring(pos + 1);
            }
        }
        final String S[] = new String[V.size()];
        V.toArray(S);
        return S;
    }

    /**
     * Test program "main", which tries to open a connection to gnugo.exe and
     * communicates with the process until it dies.
     */
    static public void main(String args[]) {
        Dump.terminal(true);
        try {
            final GMPConnector c = new GMPConnector("gnugo.exe");
            c.connect();
            c.P.waitFor();
        } catch (final Exception e) {
            System.out.println(e);
        }
    }

    String Program;

    Process P;

    InputStream In, Err;

    OutputStream Out;

    boolean Sequence = false;

    boolean End, MineAnswer = true, His = false;

    int Command, Argument;

    static public final int BLACK = 2, WHITE = 1;

    static public final int JAPANESE = 1, SST = 2;

    static public final int EVEN = 1;

    GMPInterface I = null;

    /**
     * @param program
     *            The GMP program's file name.
     */
    public GMPConnector(String program) {
        this.Program = program;
    }

    /**
     * <p>
     * Automatically answer the incomming command. These commands may be
     * questions, others, moves or OK.
     * </P>
     * <p>
     * Questions are answered in the following way: The game parameters are
     * taken from the GMPInterface interface, which needs to provide the
     * necessary information and is set by setGMPInterface. I have implemented
     * ony answers to the necessary commands. Everything else is answered with
     * OK.
     * </P>
     * <p>
     * A move or OK are sent directly to the GMPinterface.
     * </P>
     */
    public synchronized void answer() throws IOException {
        Dump.println(this.Command + " " + this.Argument);
        if (this.Command == 3) // Questions
        {
            switch (this.Argument) {
                case 7: // question for rule set
                    if (this.I != null) {
                        this.send(4, this.I.getRules());
                    } else {
                        this.send(4, 1);
                    }
                    break;
                case 9: // question for board size
                    if (this.I != null) {
                        this.send(4, this.I.getBoardSize());
                    } else {
                        this.send(4, 19);
                    }
                    break;
                case 8: // question for handicap
                    if (this.I != null) {
                        this.send(4, this.I.getHandicap());
                    } else {
                        this.send(4, 1);
                    }
                    break;
                case 11: // question for board color of myself
                    if (this.I != null) {
                        this.send(4, this.I.getColor());
                    } else {
                        this.send(4, GMPConnector.WHITE);
                    }
                    break;
                default:
                    this.send(4, 0);
                    break;
            }
        } else if (this.Command == 4) // Other commands
        {
            if (this.I != null) {
                this.I.gotAnswer(this.Argument);
            }
        } else if (this.Command == 5) // Got move
        {
            this.ok(); // acknowledge
            if (this.I != null) {
                final int pos = this.Argument & 0x01FF;
                if ((this.Argument & 0x0200) != 0) {
                    this.I.gotMove(GMPConnector.WHITE, pos);
                } else {
                    this.I.gotMove(GMPConnector.BLACK, pos);
                }
            }
        } else if (this.Command == 0) // OK
        {
            if (this.I != null) {
                this.I.gotOk();
            }
            Dump.println("Got OK");
        } else {
            this.ok();
        }
    }

    byte computeChecksum(int a, int b, int c) {
        final int cs = this.lower(a) + this.lower(b) + this.lower(c);
        return (byte) (0x0080 | cs);
    }

    /**
     * Connect to the GMP program and open pipe streams to it (mainly In and
     * Out). Then start the communication thread.
     */
    public void connect() throws IOException {
        final Runtime R = Runtime.getRuntime();
        this.P = R.exec(GMPConnector.getcommand(this.Program));
        this.In = this.P.getInputStream();
        this.Err = this.P.getErrorStream();
        this.Out = this.P.getOutputStream();
        new Thread(this).start();
    }

    /**
     * Destroy the program and close streams.
     */
    public void destroy() {
        try {
            this.P.destroy();
            this.P.waitFor();
            this.In.close();
            this.Out.close();
            this.Err.close();
        } catch (final Exception e) {
        }
    }

    public void doclose() {
        new GMPCloser(this);
    }

    /**
     * Waits for an answer by reading four bites and interpreting the input. The
     * received command number is stored in the Command variable, and the
     * argument in the Argument variable. The sequence bit is also stored. The
     * answer bit is checked if this is really an answer to my last command.
     */
    public boolean getAnswer() throws IOException {
        int b1 = this.read();
        while ((b1 & 0x0080) != 0) {
            b1 = this.In.read();
        }
        final int cs = this.read();
        final int b3 = this.read();
        final int b4 = this.read();
        Dump.println("rcvd " + GMPConnector.format(b1) + " "
                + GMPConnector.format(cs) + " " + GMPConnector.format(b3) + " "
                + GMPConnector.format(b4));
        if ((b1 & 0x0080) != 0 || (cs & 0x0080) == 0 || (b3 & 0x0080) == 0
                || (b4 & 0x0080) == 0) {
            return false;
        }
        if (this.computeChecksum((byte) b1, (byte) b3, (byte) b4) != (byte) cs) {
            return false;
        }
        this.Command = (b3 & 0x0070) >> 4;
        this.Argument = (b3 & 0x0007) << 7 | b4 & 0x007F;
        final boolean his = (b1 & 0x0001) != 0;
        if (his == this.His) {
            return false;
        }
        this.His = his;
        if (this.Command != 0) {
            this.MineAnswer = (b1 & 0x0002) != 0;
            if (this.MineAnswer != this.Sequence) {
                throw new IOException("no answer");
            }
        }
        return true;
    }

    int lower(int a) {
        return a & 0x007F;
    }

    byte makeCommandByte1(int c, int a) {
        a = a & 0x000003FF;
        return (byte) (0x0080 | c << 4 | a >> 7);
    }

    byte makeCommandByte2(int a) {
        return (byte) (0x0080 | a & 0x0000007F);
    }

    /**
     * Send a move.
     *
     * @param color
     *            The color of the move (BLACK,WHITE).
     * @param pos
     *            The board position from 0 to 360.
     */
    public void move(int color, int pos) throws IOException {
        int c;
        if (color == GMPConnector.BLACK) {
            c = 0;
        } else {
            c = 0x0200;
        }
        this.send(5, c | pos);
    }

    /**
     * Send OK.
     */
    public void ok() throws IOException {
        this.send(this.Sequence, this.His, 0, 0x03FF);
    }

    int read() throws IOException {
        final int i = this.In.read();
        if (i < 0) {
            throw new IOException("sudden death");
        }
        return i;
    }

    public void removeGMPInterface(GMPInterface i) {
        this.I = null;
    }

    /**
     * Start the IO thread. I.e., continually get something from the program,
     * and auto treat it in the answer() function.
     */
    @Override
    public void run() {
        try {
            while (true) {
                this.getAnswer();
                this.answer();
            }
        } catch (final Exception e) {
        }
    }

    /**
     * Send a single command to Out.
     *
     * @param mine
     *            See the send() function for this.
     * @param c
     *            The command number (see the answer() function).
     * @param a
     *            A command argument.
     */
    public synchronized void send(boolean mine, boolean his, int c, int a)
            throws IOException {
        int b1 = mine ? 1 : 0;
        b1 |= his ? 2 : 0;
        final int b3 = this.makeCommandByte1(c, a);
        final int b4 = this.makeCommandByte2(a);
        final int checksum = this.computeChecksum(b1, b3, b4);
        this.Out.write((byte) b1);
        this.Out.write((byte) checksum);
        this.Out.write((byte) b3);
        this.Out.write((byte) b4);
        this.Out.flush();
        Dump.println("sent " + GMPConnector.format(b1) + " "
                + GMPConnector.format(checksum) + " " + GMPConnector.format(b3)
                + " " + GMPConnector.format(b4) + " = " + c + "," + a);
    }

    /**
     * Send a single command. Switches the Sequence bit with each command. The
     * Sequence bit is resent in answers to the command.
     */
    public void send(int c, int a) throws IOException {
        this.Sequence = !this.Sequence;
        this.send(this.Sequence, this.His, c, a);
    }

    public void setGMPInterface(GMPInterface i) {
        this.I = i;
    }

    /**
     * Take back n moves.
     *
     * @param n
     *            Number of moves to take back.
     */
    public void takeback(int n) throws IOException {
        this.send(6, n);
    }
}
