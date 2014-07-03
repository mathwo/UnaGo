package rene.util.sound;

import sun.audio.AudioData;
import sun.audio.AudioDataStream;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class Beep {
    byte B[];
    AudioDataStream A;

    public Beep() {
        this.B = null;
    }

    public Beep(int frequ, int dur) {
        this.init(frequ, dur);
    }

    public void beep(int frequ, int dur) {
        this.init(frequ, dur);
        this.play();
    }

    public void init(int frequ, int dur) {
        try {
            final int Offset = 24;
            final int Length = dur * 8;
            if (this.B == null || this.B.length < Offset + Length) {
                this.B = new byte[Offset + Length];
            }
            this.storeInt(this.B, 0, 0x2E736E64);
            this.storeInt(this.B, 4, Offset);
            this.storeInt(this.B, 8, Length);
            this.storeInt(this.B, 12, 1);
            this.storeInt(this.B, 16, 8000);
            this.storeInt(this.B, 20, 1);
            for (int i = 0; i < Length; i++) {
                this.store(
                        this.B,
                        Offset + i,
                        0.3
                        * Math.sin(i / 8000.0 * 2 * Math.PI * frequ)
                        + 0.2
                                * Math.sin(i / 8000.0 * 2 * Math.PI * 2 * frequ)
                        + 0.1
                                * Math.sin(i / 8000.0 * 2 * Math.PI * 3 * frequ));
            }
            final DataInputStream in = new DataInputStream(
                    new ByteArrayInputStream(this.B, 0, Offset + Length));
            @SuppressWarnings("resource")
            final AudioStream as = new AudioStream(in);
            final AudioData Data = as.getData();
            this.A = new AudioDataStream(Data);
        } catch (final Exception e) {
            System.out.println(e);
        }
    }

    public void play() {
        this.A.reset();
        AudioPlayer.player.start(this.A);
    }

    public void store(byte b[], int pos, double value) {
        if (value > 0) {
            b[pos] = (byte) (127 - (int) Math.floor((int) (value * 128)));
        } else {
            b[pos] = (byte) (0x00000080 | 127 - (int) Math
                    .floor((int) (-value * 128)));
        }
    }

    public void storeInt(byte[] b, int pos, int value) {
        b[pos] = (byte) ((value & 0xFF000000) >> 24);
        b[pos + 1] = (byte) ((value & 0x00FF0000) >> 16);
        b[pos + 2] = (byte) ((value & 0x0000FF00) >> 8);
        b[pos + 3] = (byte) (value & 0x000000FF);
    }

}
