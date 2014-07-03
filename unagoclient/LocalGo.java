package unagoclient;

import unagoclient.board.GoFrame;
import unagoclient.board.LocalGoFrame;
import unagoclient.sound.UnaGoSound;

import java.awt.*;

/**
 * Similar to Go. The main() method starts a GoFrame.
 *
 * @see unagoclient.Go
 * @see unagoclient.board.GoFrame
 */

public class LocalGo {
    public static void main(String args[]) {
        boolean homefound = false;
        String localgame = "";
        int na = 0;
        int move = 0;
        while (args.length > na) {
            if (args.length - na >= 2 && args[na].startsWith("-h")) {
                Global.home(args[na + 1]);
                na += 2;
                homefound = true;
            } else if (args[na].startsWith("-d")) {
                Dump.open("dump.dat");
                na++;
            } else {
                localgame = args[na];
                na++;
                if (args.length > na) {
                    try {
                        move = Integer.parseInt(args[na]);
                        na++;
                    } catch (final Exception e) {
                    }
                }
                break;
            }
        }
        Global.setApplet(false);
        if (!homefound) {
            Global.home(System.getProperty("user.home"));
        }
        Global.readparameter(".go.cfg");
        Global.createfonts();
        Global.frame(new Frame());
        UnaGoSound.play("high", "", true);
        if (!localgame.equals("")) {
            LocalGo.openlocal(localgame, move);
        } else {
            LocalGo.GF = new LocalGoFrame(new Frame(),
                    Global.resourceString("Local_Viewer"));
        }
        Global.setcomponent(LocalGo.GF);
    }

    static void openlocal(String file, int move) {
        LocalGo.GF = new LocalGoFrame(new Frame(),
                Global.resourceString("Local_Viewer"));
        LocalGo.GF.load(file, move);
    }

    static GoFrame GF;
}
