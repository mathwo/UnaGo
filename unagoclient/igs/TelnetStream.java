package unagoclient.igs;

import unagoclient.Dump;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * This is a stream, which filters Telnet commands.
 */

public class TelnetStream extends FilterInputStream {
    InputStream In;
    PrintWriter Out;
    ConnectionFrame CF;

    int lastcr = 0;

    public TelnetStream(ConnectionFrame cf, InputStream in, PrintWriter out) {
        super(in);
        this.In = in;
        this.Out = out;
        this.CF = cf;
    }

    @Override
    public int read() throws IOException {
        while (true) {
            int c = this.In.read();
            if (c == 255) // Telnet ??
            {
                final int command = this.In.read();
                Dump.println("Telnet received!" + command);
                if (command == 253) {
                    c = this.In.read();
                    this.CF.Outstream.write(255);
                    this.CF.Outstream.write(252);
                    this.CF.Outstream.write(c);
                } else if (command == 246) {
                    this.CF.Outstream.write(255);
                    this.CF.Outstream.write(241);
                }
                if (c == -1) {
                    return c;
                }
                return 0;
            }
            if (c >= 0 && c <= 9) {
                return 0;
            }
            if (c == 12) {
                return 0;
            }
            return c;
        }
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int i = 0;
        int c = this.read();
        if (c == 0) {
            c = ' ';
        }
        b[off + i] = (byte) c;
        i++;
        if (c == -1) {
            return 1;
        }
        while (this.In.available() > 0 && i < len) {
            c = this.read();
            if (c == 0) {
                c = ' ';
            }
            b[off + i] = (byte) c;
            i++;
            if (c == -1) {
                break;
            }
        }
        return i;
    }

}
