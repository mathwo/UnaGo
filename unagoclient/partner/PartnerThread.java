package unagoclient.partner;

import unagoclient.Dump;
import unagoclient.Global;
import rene.viewer.Viewer;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A thrad to expect input from a partner. The input is checked here for
 * commands (starting with @@).
 */

public class PartnerThread extends Thread {
    BufferedReader In;
    PrintWriter Out;
    Viewer T;
    PartnerFrame PF;
    JTextField Input;

    public PartnerThread(BufferedReader in, PrintWriter out, JTextField input,
            Viewer t, PartnerFrame pf) {
        this.In = in;
        this.Out = out;
        this.T = t;
        this.PF = pf;
        this.Input = input;
    }

    @Override
    public void run() {
        try {
            while (true) {
                final String s = this.In.readLine();
                if (s == null || s.equals("@@@@end")) {
                    throw new IOException();
                }
                Dump.println("From Partner: " + s);
                if (s.startsWith("@@busy")) {
                    this.T.append(Global
                            .resourceString("____Server_is_busy____"));
                    return;
                } else if (s.startsWith("@@")) {
                    this.PF.interpret(s);
                } else {
                    this.T.append(s + "\n");
                    this.Input.requestFocus();
                }
            }
        } catch (final IOException e) {
            this.T.append(Global.resourceString("_____Connection_Error") + "\n");
        }
    }
}
