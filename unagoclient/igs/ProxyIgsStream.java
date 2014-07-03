package unagoclient.igs;

import unagoclient.Dump;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * This is a specified IGS stream, which reads byte over a telnet proxy. Thus
 * telnet commands must be filtered. Consequently, it must use a byte stream and
 * cannot translate into locales.
 */

public class ProxyIgsStream extends IgsStream {
    DataInputStream In;

    public ProxyIgsStream(ConnectionFrame cf, InputStream in, PrintWriter out) {
        super(cf, in, out);
    }

    @Override
    public boolean available() {
        try {
            return (this.In.available() > 0);
        } catch (final IOException e) {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        this.In.close();
    }

    @Override
    public void initstream(InputStream in) {
        this.In = new DataInputStream(in);
    }

    @Override
    public char read() throws IOException {
        while (true) {
            byte c = this.In.readByte();
            if (c == -1) // Telnet ??
            {
                c = this.In.readByte();
                Dump.println("Telnet received!" + (256 + c));
                if (c == -3) {
                    c = this.In.readByte();
                    this.CF.Outstream.write(255);
                    this.CF.Outstream.write(252);
                    this.CF.Outstream.write(c);
                    continue;
                } else if (c == -5) {
                    c = this.In.readByte();
                    continue;
                }
            }
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
            }
            return (char) c;
        }
    }

}
