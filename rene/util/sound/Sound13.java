package rene.util.sound;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Sound13 implements Sound {
    public static void main(String args[]) {
        final Sound13 s = new Sound13("/unagoclient/au/message.wav");
        s.start();
        s.start();
        System.exit(0);
    }

    String Name;
    byte Buffer[];

    AudioFormat AFormat;

    public Sound13(String name) {
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
            byte buffer[] = new byte[10000];
            int n = 0, size = 10000;
            while (true) {
                final int c = in.read();
                if (c < 0) {
                    break;
                }
                if (n >= size) {
                    final byte b[] = new byte[size * 2];
                    System.arraycopy(buffer, 0, b, 0, size);
                    buffer = b;
                    size = size * 2;
                }
                buffer[n++] = (byte) c;
            }
            in = new ByteArrayInputStream(buffer);
            final AudioInputStream audio = AudioSystem.getAudioInputStream(in);
            this.AFormat = audio.getFormat();
            this.Buffer = new byte[audio.available()];
            audio.read(this.Buffer);
            audio.close();
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
        }
    }

    @Override
    public void setName(String name) {
        this.Name = name;
    }

    @Override
    public void start() {
        try {
            final DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                    this.AFormat);
            final SourceDataLine line = (SourceDataLine) AudioSystem
                    .getLine(info);
            line.open(this.AFormat);
            line.start();
            line.write(this.Buffer, 0, this.Buffer.length);
            line.drain();
            line.close();
        } catch (final Exception e) {
            System.out.println(e);
        }
    }
}
