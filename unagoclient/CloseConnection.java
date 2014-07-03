package unagoclient;

import java.io.BufferedReader;
import java.net.Socket;

public class CloseConnection extends Thread {
    Socket S;
    BufferedReader In;

    public CloseConnection(Socket s, BufferedReader in) {
        this.S = s;
        this.In = in;
        this.start();
    }

    @Override
    public void run() {
        try {
            if (this.S != null) {
                this.S.close();
            }
            if (this.In != null) {
                this.In.close();
            }
        } catch (final Exception e) {
        }
    }
}