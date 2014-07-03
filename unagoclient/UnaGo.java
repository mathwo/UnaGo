package unagoclient;

import java.applet.Applet;
import java.awt.*;

/**
 * An applet to start a UnaGo frame. Does about the same things that Go.main
 * does, only applet specific. Basically, it will create a MainFrame and set a
 * Go applet into it. Then it will display the MainFrame.
 * <p>
 * When this applet is on the go server, it should contain a "server" applet
 * parameter pointing to the server and a "port" paramter for the server port.
 *
 * @see unagoclient.Go
 */

public class UnaGo extends Applet {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    MainFrame F;
    Go go;
    boolean Started = false;

    @Override
    synchronized public void init() {
        if (this.Started) {
            return; // the applet need only start once
        }
        this.Started = true;
        // intialize Global things
        Global.url(this.getCodeBase());
        Global.readparameter(".go.cfg");
        Global.createfonts();
        Global.frame(new Frame());
        Global.loadmessagefilter();
        // see, if there is a specific IGS server
        String Server = this.getParameter("server");
        if (Server == null) {
            Server = "";
        }
        int port;
        final String Port = this.getParameter("port");
        try {
            port = Integer.parseInt(Port);
        } catch (final Exception e) {
            port = 6969;
        }
        String Encoding = this.getParameter("encoding");
        if (Encoding == null) {
            Encoding = "";
        }
        String MoveStyle = this.getParameter("movestyle");
        if (MoveStyle == null) {
            MoveStyle = "";
        }
        // create a MainFrame
        this.F = new MainFrame(
                Server.equals("") ? Global.resourceString("UnaGo")
                        : Global.resourceString("UnaGo_Applet"));
        // add a Go applet to the frame
        if (Server.equals("")) {
            this.F.add("Center", this.go = new Go());
        } else {
            this.F.add("Center", this.go = new Go(Server, port, MoveStyle,
                    Encoding));
        }
        Go.F = this.F;
        this.go.init();
        this.go.start();
        // display the frame
        this.F.setVisible(true);
    }
}
