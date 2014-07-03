package unagoclient.datagram;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Vector;

/**
 * This class may be used to send a datagram to some internet address or to
 * receive datagrams from some internet address. The datagram contains text
 * lines, which are converted to and from byte arrays.
 */

public class DatagramMessage {
    byte B[];
    final int MAX = 4096;
    int Used;

    public DatagramMessage() {
        this.B = new byte[this.MAX];
        this.Used = 0;
    }

    /**
     * Add a new line to the datagram.
     */
    public void add(String s) {
        try {
            final byte B1[] = s.getBytes();
            for (int i = 0; i < s.length(); i++) {
                this.B[this.Used + i] = B1[i];
            }
            this.Used += s.length();
            this.B[this.Used] = 0;
            this.Used++;
        } catch (final Exception e) {
        }
    }

    /**
     * @return a vector of lines, containing the received datagram.
     */
    public Vector receive(int port) {
        final Vector v = new Vector();
        try {
            final DatagramPacket dp = new DatagramPacket(this.B, this.MAX);
            final DatagramSocket ds = new DatagramSocket(port);
            ds.receive(dp);
            final int l = dp.getLength();
            int i = 0;
            while (i < l) {
                final int j = i;
                while (this.B[i] != 0) {
                    i++;
                }
                if (i > j) {
                    v.addElement(new String(this.B, j, i - j));
                } else {
                    v.addElement(new String(""));
                }
                i++;
            }
            ds.close();
        } catch (final Exception e) {
            return v;
        }
        return v;
    }

    /**
     * Send the datagram to the specified address and port.
     */
    public void send(String adr, int port) {
        if (this.Used == 0) {
            return;
        }
        try {
            final InetAddress ia = InetAddress.getByName(adr);
            final DatagramPacket dp = new DatagramPacket(this.B, this.Used, ia,
                    port);
            final DatagramSocket ds = new DatagramSocket();
            ds.send(dp);
            ds.close();
        } catch (final Exception e) {
        }
    }
}
