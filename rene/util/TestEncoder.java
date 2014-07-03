package rene.util;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * TestEncoder creates a PNG file that shows an analog clock displaying the
 * current time of day, optionally with an alpha channel, and with the specified
 * filter.
 * <p>
 * The file name is in the format:
 * <p>
 * clockHHMM_fNC alphaclockDHHMM_fNC
 * <p>
 * HH = hours (24-hour clock), MM = minutes,
 * <p>
 * N = filter level (0, 1, or 2) C = compression level (0 to 9)
 * <p>
 * "alpha" is prefixed to the name if alpha encoding was used.
 * <p>
 * This test program was written in a burning hurry, so it is not a model of
 * efficient, or even good, code. Comments and bug fixes should be directed to:
 * <p>
 * david@catcode.com
 *
 * @author J. David Eisenberg
 * @version 1.3, 6 April 2000
 */

public class TestEncoder extends Frame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        int i;

        final TestEncoder te = new TestEncoder("Test PNG Alpha/Filter Encoder");
        i = 0;
        te.encodeAlpha = false;
        te.filter = 0;
        te.pixelDepth = 24;
        te.compressionLevel = 1;
        while (i < args.length) {
            if (args[i].equals("-alpha")) {
                te.encodeAlpha = true;
                i++;
            } else if (args[i].equals("-filter")) {
                if (i != args.length - 1) {
                    try {
                        te.filter = Integer.parseInt(args[i + 1]);
                    } catch (final Exception e) {
                        TestEncoder.usage();
                        break;
                    }
                }
                i += 2;
            } else if (args[i].equals("-compress")) {
                if (i != args.length - 1) {
                    try {
                        te.compressionLevel = Integer.parseInt(args[i + 1]);
                    } catch (final Exception e) {
                        TestEncoder.usage();
                        break;
                    }
                }
                i += 2;
            } else {
                TestEncoder.usage();
                break;
            }
        }
        if (te.pixelDepth == 8) {
            te.encodeAlpha = false;
        }
        te.doYourThing();
    }

    protected static void usage() {
        System.out.print("Usage: TestEncoder -alpha -filter n -compress c");
        System.out.println("-alpha means to use alpha encoding (default none)");
        System.out.println("n is filter number 0=none (default), 1=sub, 2=up");
        System.out.println("c is compression factor (0-9); 1 default");
        System.exit(0);
    }

    String message;
    String timeStr;
    Image clockImage = null;
    int hour, minute;
    boolean encodeAlpha;
    int filter;
    int compressionLevel;
    int pixelDepth;

    String filename;

    boolean fileSaved = false;

    public TestEncoder(String s) {
        super(s);
        this.setSize(200, 200);
    }

    public void addAlphaToImage() {
        final int width = 100;
        final int height = 100;
        int alphaMask = 0;
        final int[] pixels = new int[width * height];

        final PixelGrabber pg = new PixelGrabber(this.clockImage, 0, 0, width,
                height, pixels, 0, width);
        try {
            pg.grabPixels();
        } catch (final InterruptedException e) {
            System.err.println("interrupted waiting for pixels!");
            return;
        }
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("image fetch aborted or errored");
            return;
        }
        for (int i = 0; i < width * height; i++) {
            if ((i % width) == 0) {
                alphaMask = (alphaMask >> 24) & 0xff;
                alphaMask += 2;
                if (alphaMask > 255) {
                    alphaMask = 255;
                }
                alphaMask = (alphaMask << 24) & 0xff000000;
            }
            pixels[i] = (pixels[i] & 0xffffff) | alphaMask;
        }
        this.clockImage = this.createImage(new MemoryImageSource(width, height,
                pixels, 0, width));
    }

    public void doYourThing() {

        // The resultant PNG data will go into this array...

        final Calendar cal = Calendar.getInstance();

        this.hour = cal.get(Calendar.HOUR);
        if (cal.get(Calendar.AM_PM) == 1) {
            this.hour += 12;
        }
        this.hour %= 24;
        this.minute = cal.get(Calendar.MINUTE);

        /*
         * format the time to a string of the form hhmm for use in the filename
         */

        this.timeStr = Integer.toString(this.minute);
        if (this.minute < 10) {
            this.timeStr = "0" + this.timeStr;
        }
        this.timeStr = Integer.toString(this.hour) + this.timeStr;
        if (this.hour < 10) {
            this.timeStr = "0" + this.timeStr;
        }

        this.filename = (this.encodeAlpha) ? "alphaclock" : "clock";
        this.filename += this.timeStr + "_f" + this.filter
                + this.compressionLevel + ".png";

        this.message = "File: " + this.filename;

        final WindowListener l = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
        this.addWindowListener(l);
        this.setVisible(true);
    }

    public void drawClockImage(int hour, int minute) {
        // variables used for drawing hands of clock
        Graphics g;
        final Font smallFont = new Font("Helvetica", Font.PLAIN, 9);
        FontMetrics fm;

        int x0, y0, x1, y1;
        double angle;

        g = this.clockImage.getGraphics();
        g.setFont(smallFont);
        fm = g.getFontMetrics();

        // draw the clock face; yellow for AM, blue for PM
        if (hour < 12) {
            g.setColor(new Color(255, 255, 192));
        } else {
            g.setColor(new Color(192, 192, 255));
        }
        g.fillOval(10, 10, 80, 80);
        g.setColor(Color.black);
        g.drawOval(10, 10, 80, 80);
        g.drawOval(48, 48, 4, 4);

        /* draw the 12 / 3 / 6/ 9 numerals */
        g.setFont(smallFont);
        g.drawString("12", 50 - fm.stringWidth("12") / 2, 11 + fm.getAscent());
        g.drawString("3", 88 - fm.stringWidth("3"), 50 + fm.getAscent() / 2);
        g.drawString("6", 50 - fm.stringWidth("6") / 2, 88);
        g.drawString("9", 12, 50 + fm.getAscent() / 2);

        x0 = 50;
        y0 = 50;

        /* draw the hour hand */
        hour %= 12;
        angle = -(hour * 30 + minute / 2) + 90;
        angle = angle * (Math.PI / 180.0);

        x1 = (int) (x0 + 28 * (Math.cos(angle)));
        y1 = (int) (y0 - 28 * (Math.sin(angle)));
        g.drawLine(x0, y0, x1, y1);

        /* and the minute hand */
        angle = -(minute * 6) + 90;
        angle = angle * Math.PI / 180.0;
        x1 = (int) (x0 + 35 * (Math.cos(angle)));
        y1 = (int) (y0 - 35 * (Math.sin(angle)));
        g.drawLine(x0, y0, x1, y1);
    }

    @Override
    public void paint(Graphics g) {
        if (this.clockImage == null) {
            this.clockImage = this.createImage(100, 100);
        }
        if (this.clockImage != null) {
            if (!this.fileSaved) {
                this.drawClockImage(this.hour, this.minute);
                if (this.encodeAlpha) {
                    this.addAlphaToImage();
                }
                this.saveClockImage();
                this.fileSaved = true;
            }
            g.drawImage(this.clockImage, 50, 20, null);
        }
        if (this.message != null) {
            g.drawString(this.message, 10, 140);
        }
    }

    public void saveClockImage() {
        byte[] pngbytes;
        final PngEncoder png = new PngEncoder(this.clockImage,
                (this.encodeAlpha) ? PngEncoder.ENCODE_ALPHA
                        : PngEncoder.NO_ALPHA, this.filter,
                        this.compressionLevel);

        try {
            final FileOutputStream outfile = new FileOutputStream(this.filename);
            pngbytes = png.pngEncode();
            if (pngbytes == null) {
                System.out.println("Null image");
            } else {
                outfile.write(pngbytes);
            }
            outfile.flush();
            outfile.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
