package unagoclient.igs;

import unagoclient.Dump;
import unagoclient.Global;
import unagoclient.dialogs.Message;
import rene.viewer.Viewer;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * This thread uses the output of IgsStream to login to server. After the login
 * it will fall into a loop, and echo all input to the Output text area of the
 * ConnectionFrame. Some elements get filtered by this process. But most
 * filtering is done by IgsStream itself.
 * <p>
 * Moreover, it will toggle to client mode, if necessary.
 *
 * @see unagoclient.igs.IgsStream
 */

public class ReceiveThread extends Thread {
    IgsStream In;
    PrintWriter Out;
    Viewer Output;
    String User, Password;
    boolean FileMode, Proxy;
    boolean TriedClient = false;
    ConnectionFrame CF;

    public ReceiveThread(Viewer output, IgsStream in, PrintWriter out,
            String user, String password, boolean proxy, ConnectionFrame cf) {
        this.In = in;
        this.Out = out;
        this.Output = output;
        this.User = user;
        this.Password = password;
        this.FileMode = false;
        this.Proxy = proxy;
        this.CF = cf;
    }

    /**
     * Called from outside to go to client mode.
     */
    public void goclient() {
        if (this.TriedClient) {
            return;
        }
        this.Output.append(Global.resourceString("____toggle_client_true_n"));
        this.Out.println("toggle client true");
        this.TriedClient = true;
    }

    @Override
    public void run() {
        final boolean Auto = rene.gui.Global.getParameter("automatic", true)
                && !this.User.equals("");
        try {
            if (!this.Proxy) {
                this.Out.println("");
            }
            // try to auto-login into the server
            if (Auto && !this.Proxy) {
                while (true) {
                    if (this.In.readline()) {
                        this.Output.append(this.In.line() + "\n");
                    }
                    if (this.In.line().startsWith("Login")) {
                        this.Output.append(Global
                                .resourceString("_____logging_in_n"));
                        this.Out.println(this.User);
                        break;
                    }
                }
                Dump.println("--- Leaving Login section ---");
                while (true) {
                    if (this.In.readline()) {
                        if (this.In.number() == 1
                                && this.In.commandnumber() == 1) {
                            this.Out.println(this.Password);
                            this.Output.append(Global
                                    .resourceString("_____sending_password_n"));
                            break;
                        } else if (this.In.number() == 1) {
                            this.Output.append(Global
                                    .resourceString("_____enter_commands__n"));
                            break;
                        } else {
                            this.Output.append(this.In.line() + "\n");
                        }
                    } else {
                        if (this.In.line().startsWith("#>")) {
                            this.goclient();
                        } else if (this.In.line().startsWith("Password")) {
                            this.Out.println(this.Password);
                            this.Output.append(Global
                                    .resourceString("_____sending_password_n"));
                        }
                    }
                }
                Dump.println("--- Leaving Password section ---");
            }
            // end of autologin and start of loop
            boolean AskPassword = true;
            while (true) {
                try {
                    if (this.In.readline()) {
                        if (this.FileMode && this.In.number() != 100) {
                            this.FileMode = false;
                        } else if (this.In.command().equals("File")) {
                            this.FileMode = true;
                        }
                        switch (this.In.number()) {
                            case 1:
                                this.Proxy = false;
                                if (!Auto && this.In.commandnumber() == 1
                                        && AskPassword) {
                                    this.Output
                                    .append(Global
                                            .resourceString("____Enter_Password_____n"));
                                    AskPassword = false;
                                    break;
                                } else if (!Auto
                                        && this.In.commandnumber() == 0) {
                                    this.Output
                                    .append(Global
                                            .resourceString("____Enter_Login_____n"));
                                    AskPassword = true;
                                    break;
                                }
                            case 40:
                            case 22:
                            case 2:
                                break;
                            default:
                                if (this.FileMode
                                        || !this.In.command().equals("")) {
                                    this.CF.append(this.In.command());
                                }
                        }
                        if (!Auto && this.In.command().startsWith("Login")) {
                            AskPassword = true;
                            this.Output.append(Global
                                    .resourceString("____Enter_Login_____n"));
                        }
                        if (this.In.command().startsWith("#>")) {
                            this.goclient();
                        }
                    } else if (!Auto && this.In.line().startsWith("Login")) {
                        this.Output.append(Global
                                .resourceString("____Enter_Login_____n"));
                    } else if (this.In.command().startsWith("#>")) {
                        this.goclient();
                    } else if (this.Proxy) {
                        this.Output.append(this.In.line());
                    }
                } catch (final IOException e) {
                    throw e;
                } catch (final Exception e) {
                    System.out
                    .println("Exception (please report to the author)\n"
                            + e.toString() + "\n");
                    e.printStackTrace();
                }
            }
        } catch (final IOException ex) // server has closed connection
        {
            if (!this.CF.hasClosed) {
                this.Output
                .append(Global
                        .resourceString("_____connection_error__n")
                        + ex + "\n");
                new Message(Global.frame(),
                        Global.resourceString("Lost_Connection"));
                try {
                    Thread.sleep(10000);
                } catch (final Exception e) {
                }
                return;
            }
        }
    }

}
