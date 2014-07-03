package rene.util.sound;

import rene.gui.Global;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import java.awt.*;

class SoundElement extends ListElement {
    Sound S;

    public SoundElement(String name) {
        super(name);
        this.S = null;
        this.S = new Sound13(name);
    }

    public String name() {
        return (String) this.content();
    }

    public void play() {
        this.S.start();
    }
}

/**
 * This is a Sound class to play and store sounds from resources. The class
 * keeps a list of loaded sounds.
 */

public class SoundList implements Runnable {
    static synchronized public void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    static public void main(String args[]) {
        System.out.println("Java Version " + Global.getJavaVersion());
        final String Sounds[] = { "high", "message", "click", "stone", "wip",
                "yourmove", "game" };
        final rene.gui.CloseFrame F = new rene.gui.CloseFrame() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void doAction(String o) {
                if (Global.getJavaVersion() >= 1.3) {
                    SoundList.L.play("/unagoclient/au/" + o + ".wav");
                } else {
                    SoundList.L.play("/unagoclient/au/" + o + ".au");
                }
            }

            @Override
            public void doclose() {
                System.exit(0);
            }
        };
        F.setLayout(new BorderLayout());
        final Panel p = new Panel();
        F.add("Center", p);
        for (int i = 0; i < Sounds.length; i++) {
            final Button b = new Button(Sounds[i]);
            b.addActionListener(F);
            p.add(b);
        }
        F.pack();
        F.setVisible(true);
    }

    ListClass SL;
    Thread T;

    boolean Busy;

    String Name, Queued;

    static SoundList L = new SoundList();

    public SoundList() {
        this.SL = new ListClass();
        this.T = new Thread(this);
        this.T.start();
        try {
            Thread.sleep(0);
        } catch (final Exception e) {
        }
    }

    public SoundElement add(String name) {
        final SoundElement e = new SoundElement(name);
        this.SL.append(e);
        return e;
    }

    public boolean busy() {
        return this.Busy;
    }

    public SoundElement find(String name) {
        SoundElement se = (SoundElement) this.SL.first();
        while (se != null) {
            if (se.name().equals(name)) {
                return se;
            }
            se = (SoundElement) se.next();
        }
        return null;
    }

    public synchronized void play(String name) {
        if (this.busy()) {
            synchronized (this) {
                this.Queued = name;
            }
            return;
        }
        this.Name = name;
        synchronized (this) {
            this.notify();
        }
    }

    public void playNow(String name) {
        SoundElement e = this.find(name);
        if (e == null) {
            e = this.add(name);
        }
        e.play();
    }

    @Override
    public void run() {
        this.Busy = false;
        while (true) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (final Exception e) {
                System.out.println(e);
            }
            this.Busy = true;
            while (this.Name != null) {
                this.playNow(this.Name);
                synchronized (this) {
                    this.Name = this.Queued;
                    this.Queued = null;
                }
            }
            this.Busy = false;
        }
    }
}
