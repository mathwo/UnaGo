package unagoclient.gui;

import java.awt.*;

/**
 * This is thread is used to wait a while, before a component requests a focus.
 * Otherwise, flicker may occur because of a race for focus.
 */

public class FocusRequester extends Thread {
    Component C;
    static boolean Waiting = false;

    public FocusRequester(Component c) {
        this.C = c;
        if (!FocusRequester.Waiting) {
            this.start();
        }
    }

    @Override
    public void run() {
        FocusRequester.Waiting = true;
        this.C.requestFocus();
        try {
            Thread.sleep(1000);
        } catch (final Exception e) {
        }
        FocusRequester.Waiting = false;
    }
}