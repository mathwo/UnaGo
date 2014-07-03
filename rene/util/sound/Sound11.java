package rene.util.sound;

import sun.audio.AudioData;
import sun.audio.AudioDataStream;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.InputStream;

public class Sound11 implements Sound {
    public static void main(String args[]) {
        final Sound11 s = new Sound11("/unagoclient/au/message.au");
        s.start();
        s.start();
        try {
            Thread.sleep(5000);
        } catch (final Exception e) {
        }
    }

    AudioData Data;
    AudioDataStream A;
    String Name;

    int Length;

    public Sound11(String name) {
        this.setName(name);
        this.load(name);
    }

    @Override
    public String getName() {
        return this.Name;
    }

    @Override
    public void load(InputStream in) {
        try {
            @SuppressWarnings("resource")
            final AudioStream AS = new AudioStream(in);
            this.Length = AS.available();
            this.Data = AS.getData();
        } catch (final Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void load(String file) {
        try {
            final InputStream in = this.getClass().getResourceAsStream(file);
            this.load(in);
        } catch (final Exception e) {
            System.out.println(e);
            this.Data = null;
        }
    }

    @Override
    public void setName(String name) {
        this.Name = name;
    }

    @Override
    public void start() {
        if (this.Data != null) {
            try {
                if (this.A == null) {
                    this.A = new AudioDataStream(this.Data);
                } else {
                    this.A.reset();
                }
                AudioPlayer.player.start(this.A);
                Thread.sleep(this.Length * 1000 / 8000 + 500);
            } catch (final Exception e) {
                System.out.println(e);
            }
        }
    }
}
