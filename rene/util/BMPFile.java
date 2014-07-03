package rene.util;

import java.awt.*;
import java.awt.image.PixelGrabber;
import java.io.FileOutputStream;

/**
 * This is a bitmap writer I have somewhere from the Internet. I excuse with the
 * author of this to have lost his name. If he/she reads this, please mail me
 * for that information.
 * <p>
 * However, I had to fix this to make it work properly.
 * <p>
 * The saving can be done in a separate Thread, if needed.
 */

public class BMPFile implements Runnable {

    // --- Private constants
    private final static int BITMAPFILEHEADER_SIZE = 14;
    private final static int BITMAPINFOHEADER_SIZE = 40;

    // --- Private variable declaration

    private final byte bfType[] = { (byte) 'B', (byte) 'M' };
    private int bfSize = 0;
    private final int bfReserved1 = 0;
    private final int bfReserved2 = 0;
    private final int bfOffBits = BMPFile.BITMAPFILEHEADER_SIZE
            + BMPFile.BITMAPINFOHEADER_SIZE;

    private final int biSize = BMPFile.BITMAPINFOHEADER_SIZE;
    private int biWidth = 0;
    private int biHeight = 0;
    private final int biPlanes = 1;
    private final int biBitCount = 24;
    private final int biCompression = 0;
    private int biSizeImage = 0x030000;
    private final int biXPelsPerMeter = 0x0;
    private final int biYPelsPerMeter = 0x0;
    private final int biClrUsed = 0;
    private final int biClrImportant = 0;
    private int linepad = 0;

    // --- Bitmap raw data
    private int bitmap[];

    // --- File section
    private FileOutputStream fo;

    String parFilename;
    Image parImage;
    int parWidth, parHeight;

    public BMPFile(String parFilename, Image parImage, int parWidth,
            int parHeight) {
        this.parFilename = parFilename;
        this.parImage = parImage;
        this.parWidth = parWidth;
        this.parHeight = parHeight;
    }

    /*
     * convertImage converts the memory image to the bitmap format (BRG). It
     * also computes some information for the bitmap info header.
     */
    private boolean convertImage(Image parImage, int parWidth, int parHeight) {
        this.bitmap = new int[parWidth * parHeight];

        final PixelGrabber pg = new PixelGrabber(parImage, 0, 0, parWidth,
                parHeight, this.bitmap, 0, parWidth);

        try {
            pg.grabPixels();
        } catch (final InterruptedException e) {
            e.printStackTrace();
            return (false);
        }

        this.linepad = 4 - ((parWidth * 3) % 4);
        if (this.linepad == 4) {
            this.linepad = 0;
        }
        this.biSizeImage = (parWidth * 3 + this.linepad) * parHeight;
        this.bfSize = this.biSizeImage + BMPFile.BITMAPFILEHEADER_SIZE
                + BMPFile.BITMAPINFOHEADER_SIZE;
        this.biWidth = parWidth;
        this.biHeight = parHeight;

        return (true);
    }

    /*
     *
     * intToDWord converts an int to a double word, where the return value is
     * stored in a 4-byte array.
     */
    private byte[] intToDWord(int parValue) {

        final byte retValue[] = new byte[4];

        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x000000FF);
        retValue[2] = (byte) ((parValue >> 16) & 0x000000FF);
        retValue[3] = (byte) ((parValue >> 24) & 0x000000FF);

        return (retValue);

    }

    /*
     *
     * intToWord converts an int to a word, where the return value is stored in
     * a 2-byte array.
     */
    private byte[] intToWord(int parValue) {

        final byte retValue[] = new byte[2];

        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x00FF);

        return (retValue);

    }

    @Override
    public void run() {
        try {
            this.fo = new FileOutputStream(this.parFilename);
            this.save(this.parImage, this.parWidth, this.parHeight);
            this.fo.close();
        } catch (final Exception saveEx) {
            saveEx.printStackTrace();
        }
    }

    /*
     * The saveMethod is the main method of the process. This method will call
     * the convertImage method to convert the memory image to a byte array;
     * method writeBitmapFileHeader creates and writes the bitmap file header;
     * writeBitmapInfoHeader creates the information header; and writeBitmap
     * writes the image.
     */
    private void save(Image parImage, int parWidth, int parHeight) {

        try {
            this.convertImage(parImage, parWidth, parHeight);
            this.writeBitmapFileHeader();
            this.writeBitmapInfoHeader();
            this.writeBitmap();
        } catch (final Exception saveEx) {
            saveEx.printStackTrace();
        }
    }

    public void saveBitmap() {
        this.run();
    }

    /*
     *
     * writeBitmapInfoHeader writes the bitmap information header to the file.
     */

    /*
     * writeBitmap converts the image returned from the pixel grabber to the
     * format required. Remember: scan lines are inverted in a bitmap file!
     *
     * Each scan line must be padded to an even 4-byte boundary.
     */
    private void writeBitmap() {
        int i, j, k;
        int value;
        final byte rgb[] = new byte[3];

        try {
            for (i = this.biHeight - 1; i >= 0; i--) {
                for (j = 0; j < this.biWidth; j++) {
                    value = this.bitmap[i * this.biWidth + j];
                    rgb[0] = (byte) (value & 0x000000FF);
                    rgb[1] = (byte) ((value >> 8) & 0x000000FF);
                    rgb[2] = (byte) ((value >> 16) & 0x000000FF);
                    this.fo.write(rgb);
                }
                for (k = 0; k < this.linepad; k++) {
                    this.fo.write(0x00);
                }
            }
        } catch (final Exception wb) {
            wb.printStackTrace();
        }

    }

    /*
     * writeBitmapFileHeader writes the bitmap file header to the file.
     */
    private void writeBitmapFileHeader() {

        try {
            this.fo.write(this.bfType);
            this.fo.write(this.intToDWord(this.bfSize));
            this.fo.write(this.intToWord(this.bfReserved1));
            this.fo.write(this.intToWord(this.bfReserved2));
            this.fo.write(this.intToDWord(this.bfOffBits));

        } catch (final Exception wbfh) {
            wbfh.printStackTrace();
        }

    }

    private void writeBitmapInfoHeader() {

        try {
            this.fo.write(this.intToDWord(this.biSize));
            this.fo.write(this.intToDWord(this.biWidth));
            this.fo.write(this.intToDWord(this.biHeight));
            this.fo.write(this.intToWord(this.biPlanes));
            this.fo.write(this.intToWord(this.biBitCount));
            this.fo.write(this.intToDWord(this.biCompression));
            this.fo.write(this.intToDWord(this.biSizeImage));
            this.fo.write(this.intToDWord(this.biXPelsPerMeter));
            this.fo.write(this.intToDWord(this.biYPelsPerMeter));
            this.fo.write(this.intToDWord(this.biClrUsed));
            this.fo.write(this.intToDWord(this.biClrImportant));
        } catch (final Exception wbih) {
            wbih.printStackTrace();
        }

    }

}
