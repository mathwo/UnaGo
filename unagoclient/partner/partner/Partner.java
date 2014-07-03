package unagoclient.partner.partner;

import rene.util.parser.StringParser;

import java.io.PrintWriter;

public class Partner {
    public String Name, Server;
    public int Port;
    public boolean Valid, Trying;
    public int State = Partner.PRIVATE;
    public static final int SILENT = 0, PRIVATE = 1, LOCAL = 2, PUBLIC = 3;

    public Partner(String line) {
        final StringParser p = new StringParser(line);
        this.Valid = false;
        this.Trying = false;
        p.skip("[");
        this.Name = p.upto(']');
        p.skip("]");
        p.skipblanks();
        if (p.error()) {
            return;
        }
        p.skip("[");
        this.Server = p.parseword(']');
        p.skip("]");
        p.skipblanks();
        if (p.error()) {
            return;
        }
        p.skip("[");
        this.Port = p.parseint(']');
        p.skip("]");
        p.skipblanks();
        if (p.skip("[")) {
            final int s = p.parseint(']');
            if (!p.error()) {
                this.State = s;
            }
        }
        this.Valid = true;
    }

    public Partner(String name, String server, int port, int state) {
        this.Valid = true;
        this.Trying = false;
        this.Name = name;
        this.Server = server;
        this.Port = port;
        this.State = state;
    }

    public boolean valid() {
        return this.Valid;
    }

    public void write(PrintWriter out) {
        out.println("[" + this.Name + "] [" + this.Server + "] [" + this.Port
                + "] [" + this.State + "]");
    }
}
