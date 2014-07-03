package unagoclient.igs.connection;

import unagoclient.Global;
import rene.util.parser.StringParser;

import java.io.PrintWriter;

/**
 * A class, which holds a connection and can be initialized with a string from
 * "server.cfg".
 */
public class Connection {
    public String Name = "", Server = "", User = "", Password = "";
    public int Port;
    public boolean Valid, Trying;
    public static final int MOVE = 0, MOVE_TIME = 1, MOVE_N_TIME = 2;
    public int MoveStyle = Connection.MOVE;
    public String Encoding;

    public Connection(String line) {
        if (!Global.isApplet()) {
            this.Encoding = System.getProperty("file.encoding");
        }
        if (this.Encoding == null) {
            this.Encoding = "";
        }
        final StringParser p = new StringParser(line);
        this.Valid = true;
        this.Trying = false;
        p.skip("[");
        this.Name = p.upto(']');
        p.skip("]");
        p.skipblanks();
        if (p.error()) {
            return;
        }
        p.skip("[");
        if (p.error()) {
            return;
        }
        this.Server = p.parseword(']');
        p.skip("]");
        p.skipblanks();
        if (p.error()) {
            return;
        }
        p.skip("[");
        if (p.error()) {
            return;
        }
        this.Port = p.parseint(']');
        p.skip("]");
        p.skipblanks();
        if (p.error()) {
            return;
        }
        p.skip("[");
        if (p.error()) {
            return;
        }
        this.User = p.parseword(']');
        p.skip("]");
        p.skipblanks();
        if (p.error()) {
            return;
        }
        p.skip("[");
        if (p.error()) {
            return;
        }
        this.Password = p.parseword(']');
        p.skip("]");
        p.skipblanks();
        if (p.error()) {
            return;
        }
        p.skip("[");
        if (p.error()) {
            return;
        }
        this.MoveStyle = p.parseint(']');
        p.skip("]");
        p.skipblanks();
        if (p.error()) {
            return;
        }
        p.skip("[");
        if (p.error()) {
            return;
        }
        this.Encoding = p.parseword(']');
    }

    public boolean valid() {
        return this.Valid;
    }

    public void write(PrintWriter out) {
        out.println("[" + this.Name + "] [" + this.Server + "] [" + this.Port
                + "] [" + this.User + "] [" + this.Password + "] ["
                + this.MoveStyle + "] [" + this.Encoding + "]");
    }
}
