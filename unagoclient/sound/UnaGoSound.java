package unagoclient.sound;

import rene.util.sound.SoundList;

public class UnaGoSound {
    static public void play(String file) {
        if (UnaGoSound.SL.busy()) {
            return;
        }
        UnaGoSound.play(file, "wip", false);
    }

    static synchronized public void play(String file, String simplefile,
            boolean beep) {
        if (rene.gui.Global.getParameter("nosound", true)) {
            return;
        }
        if (rene.gui.Global.getParameter("beep", true)) {
            if (beep) {
                SoundList.beep();
            }
            return;
        }
        if (rene.gui.Global.getParameter("simplesound", true)) {
            file = simplefile;
        }
        if (file.equals("")) {
            return;
        }
        if (UnaGoSound.SL.busy()) {
            return;
        }
        if (rene.gui.Global.getJavaVersion() >= 1.3) {
            UnaGoSound.SL.play("/unagoclient/au/" + file + ".wav");
        } else {
            UnaGoSound.SL.play("/unagoclient/au/" + file + ".au");
        }
    }

    static SoundList SL = new SoundList();
}
