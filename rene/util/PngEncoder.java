package rene.util;

/**
 * PngEncoder takes a Java Image object and creates a byte string which can be saved as a PNG file.
 * The Image is presumed to use the DirectColorModel.
 *
 * Thanks to Jay Denny at KeyPoint Software
 *    http://www.keypoint.com/
 * who let me develop this code on company time.
 *
 * You may contact me with (probably very-much-needed) improvements,
 * comments, and bug fixes at:
 *
 *   david@catcode.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * A copy of the GNU LGPL may be found at
 * http://www.gnu.org/copyleft/lesser.html,
 *
 * @author J. David Eisenberg
 * @version 1.4, 31 March 2000
 */

import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class PngEncoder extends Object {
    /**
     * Constant specifying that alpha channel should be encoded.
     */
    public static final boolean ENCODE_ALPHA = true;
    /**
     * Constant specifying that alpha channel should not be encoded.
     */
    public static final boolean NO_ALPHA = false;
    /**
     * Constants for filters
     */
    public static final int FILTER_NONE = 0;
    public static final int FILTER_SUB = 1;
    public static final int FILTER_UP = 2;
    public static final int FILTER_LAST = 2;

    protected byte[] pngBytes;
    protected byte[] priorRow;
    protected byte[] leftBytes;
    protected Image image;
    protected int width, height;
    protected int bytePos, maxPos;
    protected int hdrPos, dataPos, endPos;
    protected CRC32 crc = new CRC32();
    protected long crcValue;
    protected boolean encodeAlpha;
    protected int filter;
    protected int bytesPerPixel;
    protected int compressionLevel;
    protected double DPI = 300;

    /**
     * Class constructor
     */
    public PngEncoder() {
        this(null, false, PngEncoder.FILTER_NONE, 0);
    }

    /**
     * Class constructor specifying Image to encode, with no alpha channel
     * encoding.
     *
     * @param image
     *            A Java Image object which uses the DirectColorModel
     * @see java.awt.Image
     */
    public PngEncoder(Image image) {
        this(image, false, PngEncoder.FILTER_NONE, 0);
    }

    /**
     * Class constructor specifying Image to encode, and whether to encode
     * alpha.
     *
     * @param image
     *            A Java Image object which uses the DirectColorModel
     * @param encodeAlpha
     *            Encode the alpha channel? false=no; true=yes
     * @see java.awt.Image
     */
    public PngEncoder(Image image, boolean encodeAlpha) {
        this(image, encodeAlpha, PngEncoder.FILTER_NONE, 0);
    }

    /**
     * Class constructor specifying Image to encode, whether to encode alpha,
     * and filter to use.
     *
     * @param image
     *            A Java Image object which uses the DirectColorModel
     * @param encodeAlpha
     *            Encode the alpha channel? false=no; true=yes
     * @param whichFilter
     *            0=none, 1=sub, 2=up
     * @see java.awt.Image
     */
    public PngEncoder(Image image, boolean encodeAlpha, int whichFilter) {
        this(image, encodeAlpha, whichFilter, 0);
    }

    /**
     * Class constructor specifying Image source to encode, whether to encode
     * alpha, filter to use, and compression level.
     *
     * @param image
     *            A Java Image object
     * @param encodeAlpha
     *            Encode the alpha channel? false=no; true=yes
     * @param whichFilter
     *            0=none, 1=sub, 2=up
     * @param compLevel
     *            0..9
     * @see java.awt.Image
     */
    public PngEncoder(Image image, boolean encodeAlpha, int whichFilter,
            int compLevel) {
        this.image = image;
        this.encodeAlpha = encodeAlpha;
        this.setFilter(whichFilter);
        if (compLevel >= 0 && compLevel <= 9) {
            this.compressionLevel = compLevel;
        }
    }

    /**
     * Perform "sub" filtering on the given row. Uses temporary array leftBytes
     * to store the original values of the previous pixels. The array is 16
     * bytes long, which will easily hold two-byte samples plus two-byte alpha.
     *
     * @param pixels
     *            The array holding the scan lines being built
     * @param startPos
     *            Starting position within pixels of bytes to be filtered.
     * @param width
     *            Width of a scanline in pixels.
     */
    protected void filterSub(byte[] pixels, int startPos, int width) {
        int i;
        final int offset = this.bytesPerPixel;
        final int actualStart = startPos + offset;
        final int nBytes = width * this.bytesPerPixel;
        int leftInsert = offset;
        int leftExtract = 0;
        for (i = actualStart; i < startPos + nBytes; i++) {
            this.leftBytes[leftInsert] = pixels[i];
            pixels[i] = (byte) ((pixels[i] - this.leftBytes[leftExtract]) % 256);
            leftInsert = (leftInsert + 1) % 0x0f;
            leftExtract = (leftExtract + 1) % 0x0f;
        }
    }

    /**
     * Perform "up" filtering on the given row. Side effect: refills the prior
     * row with current row
     *
     * @param pixels
     *            The array holding the scan lines being built
     * @param startPos
     *            Starting position within pixels of bytes to be filtered.
     * @param width
     *            Width of a scanline in pixels.
     */
    protected void filterUp(byte[] pixels, int startPos, int width) {
        int i, nBytes;
        byte current_byte;

        nBytes = width * this.bytesPerPixel;

        for (i = 0; i < nBytes; i++) {
            current_byte = pixels[startPos + i];
            pixels[startPos + i] = (byte) ((pixels[startPos + i] - this.priorRow[i]) % 256);
            this.priorRow[i] = current_byte;
        }
    }

    /**
     * Retrieve compression level
     *
     * @return int in range 0-9
     */
    public int getCompressionLevel() {
        return this.compressionLevel;
    }

    /**
     * Retrieve alpha encoding status.
     *
     * @return boolean false=no, true=yes
     */
    public boolean getEncodeAlpha() {
        return this.encodeAlpha;
    }

    /**
     * Retrieve filtering scheme
     *
     * @return int (see constant list)
     */
    public int getFilter() {
        return this.filter;
    }

    /**
     * Creates an array of bytes that is the PNG equivalent of the current
     * image. Alpha encoding is determined by its setting in the constructor.
     *
     * @return an array of bytes, or null if there was a problem
     */
    public byte[] pngEncode() {
        return this.pngEncode(this.encodeAlpha);
    }

    /**
     * Creates an array of bytes that is the PNG equivalent of the current
     * image, specifying whether to encode alpha or not.
     *
     * @param encodeAlpha
     *            boolean false=no alpha, true=encode alpha
     * @return an array of bytes, or null if there was a problem
     */
    public byte[] pngEncode(boolean encodeAlpha) {
        final byte[] pngIdBytes = { -119, 80, 78, 71, 13, 10, 26, 10 };
        if (this.image == null) {
            return null;
        }
        this.width = this.image.getWidth(null);
        this.height = this.image.getHeight(null);

        /*
         * start with an array that is big enough to hold all the pixels (plus
         * filter bytes), and an extra 200 bytes for header info
         */
        this.pngBytes = new byte[((this.width + 1) * this.height * 3) + 200];

        /*
         * keep track of largest byte written to the array
         */
        this.maxPos = 0;

        this.bytePos = this.writeBytes(pngIdBytes, 0);
        this.hdrPos = this.bytePos;
        this.writeHeader();
        this.writePhys();
        this.dataPos = this.bytePos;
        if (this.writeImageData()) {
            this.writeEnd();
            this.pngBytes = this.resizeByteArray(this.pngBytes, this.maxPos);
        } else {
            this.pngBytes = null;
        }
        return this.pngBytes;
    }

    /**
     * Increase or decrease the length of a byte array.
     *
     * @param array
     *            The original array.
     * @param newLength
     *            The length you wish the new array to have.
     * @return Array of newly desired length. If shorter than the original, the
     *         trailing elements are truncated.
     */
    protected byte[] resizeByteArray(byte[] array, int newLength) {
        final byte[] newArray = new byte[newLength];
        final int oldLength = array.length;

        System.arraycopy(array, 0, newArray, 0, Math.min(oldLength, newLength));
        return newArray;
    }

    /**
     * Set the compression level to use
     *
     * @param level
     *            0 through 9
     */
    public void setCompressionLevel(int level) {
        if (level >= 0 && level <= 9) {
            this.compressionLevel = level;
        }
    }

    public void setDPI(double dpi) {
        this.DPI = dpi;
    }

    /**
     * Set the alpha encoding on or off.
     *
     * @param encodeAlpha
     *            false=no, true=yes
     */
    public void setEncodeAlpha(boolean encodeAlpha) {
        this.encodeAlpha = encodeAlpha;
    }

    /**
     * Set the filter to use
     *
     * @param whichFilter
     *            from constant list
     */
    public void setFilter(int whichFilter) {
        this.filter = PngEncoder.FILTER_NONE;
        if (whichFilter <= PngEncoder.FILTER_LAST) {
            this.filter = whichFilter;
        }
    }

    /**
     * Set the image to be encoded
     *
     * @param image
     *            A Java Image object which uses the DirectColorModel
     * @see java.awt.Image
     * @see java.awt.image.DirectColorModel
     */
    public void setImage(Image image) {
        this.image = image;
        this.pngBytes = null;
    }

    /**
     * Write a single byte into the pngBytes array at a given position.
     *
     * @param n
     *            The integer to be written into pngBytes.
     * @param offset
     *            The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeByte(int b, int offset) {
        final byte[] temp = { (byte) b };
        return this.writeBytes(temp, offset);
    }

    /**
     * Write an array of bytes into the pngBytes array. Note: This routine has
     * the side effect of updating maxPos, the largest element written in the
     * array. The array is resized by 1000 bytes or the length of the data to be
     * written, whichever is larger.
     *
     * @param data
     *            The data to be written into pngBytes.
     * @param offset
     *            The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeBytes(byte[] data, int offset) {
        this.maxPos = Math.max(this.maxPos, offset + data.length);
        if (data.length + offset > this.pngBytes.length) {
            this.pngBytes = this.resizeByteArray(this.pngBytes,
                    this.pngBytes.length + Math.max(1000, data.length));
        }
        System.arraycopy(data, 0, this.pngBytes, offset, data.length);
        return offset + data.length;
    }

    /**
     * Write an array of bytes into the pngBytes array, specifying number of
     * bytes to write. Note: This routine has the side effect of updating
     * maxPos, the largest element written in the array. The array is resized by
     * 1000 bytes or the length of the data to be written, whichever is larger.
     *
     * @param data
     *            The data to be written into pngBytes.
     * @param nBytes
     *            The number of bytes to be written.
     * @param offset
     *            The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeBytes(byte[] data, int nBytes, int offset) {
        this.maxPos = Math.max(this.maxPos, offset + nBytes);
        if (nBytes + offset > this.pngBytes.length) {
            this.pngBytes = this.resizeByteArray(this.pngBytes,
                    this.pngBytes.length + Math.max(1000, nBytes));
        }
        System.arraycopy(data, 0, this.pngBytes, offset, nBytes);
        return offset + nBytes;
    }

    /**
     * Write a PNG "IEND" chunk into the pngBytes array.
     */
    protected void writeEnd() {
        this.bytePos = this.writeInt4(0, this.bytePos);
        this.bytePos = this.writeString("IEND", this.bytePos);
        this.crc.reset();
        this.crc.update("IEND".getBytes());
        this.crcValue = this.crc.getValue();
        this.bytePos = this.writeInt4((int) this.crcValue, this.bytePos);
    }

    /**
     * Write a PNG "IHDR" chunk into the pngBytes array.
     */
    protected void writeHeader() {
        int startPos;

        startPos = this.bytePos = this.writeInt4(13, this.bytePos);
        this.bytePos = this.writeString("IHDR", this.bytePos);
        this.width = this.image.getWidth(null);
        this.height = this.image.getHeight(null);
        this.bytePos = this.writeInt4(this.width, this.bytePos);
        this.bytePos = this.writeInt4(this.height, this.bytePos);
        this.bytePos = this.writeByte(8, this.bytePos); // bit depth
        this.bytePos = this.writeByte((this.encodeAlpha) ? 6 : 2, this.bytePos); // direct
        // model
        this.bytePos = this.writeByte(0, this.bytePos); // compression method
        this.bytePos = this.writeByte(0, this.bytePos); // filter method
        this.bytePos = this.writeByte(0, this.bytePos); // no interlace
        this.crc.reset();
        this.crc.update(this.pngBytes, startPos, this.bytePos - startPos);
        this.crcValue = this.crc.getValue();
        this.bytePos = this.writeInt4((int) this.crcValue, this.bytePos);
    }

    /**
     * Write the image data into the pngBytes array. This will write one or more
     * PNG "IDAT" chunks. In order to conserve memory, this method grabs as many
     * rows as will fit into 32K bytes, or the whole image; whichever is less.
     *
     * @return true if no errors; false if error grabbing pixels
     */
    protected boolean writeImageData() {
        int rowsLeft = this.height; // number of rows remaining to write
        int startRow = 0; // starting row to process this time through
        int nRows; // how many rows to grab at a time

        byte[] scanLines; // the scan lines to be compressed
        int scanPos; // where we are in the scan lines
        int startPos; // where this line's actual pixels start (used for
        // filtering)

        byte[] compressedLines; // the resultant compressed lines
        int nCompressed; // how big is the compressed area?

        PixelGrabber pg;

        this.bytesPerPixel = (this.encodeAlpha) ? 4 : 3;

        final Deflater scrunch = new Deflater(this.compressionLevel);
        final ByteArrayOutputStream outBytes = new ByteArrayOutputStream(1024);

        final DeflaterOutputStream compBytes = new DeflaterOutputStream(
                outBytes, scrunch);
        try {
            nRows = (64 * 32768 - 1) / (this.width * (this.bytesPerPixel + 1));

            final int[] pixels = new int[this.width * nRows];

            while (rowsLeft > 0) {
                if (nRows >= rowsLeft) {
                    nRows = rowsLeft;
                }

                pg = new PixelGrabber(this.image, 0, startRow, this.width,
                        nRows, pixels, 0, this.width);
                try {
                    pg.grabPixels();
                } catch (final Exception e) {
                    System.err.println("interrupted waiting for pixels!");
                    return false;
                }
                if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                    System.err.println("image fetch aborted or errored");
                    return false;
                }

                /*
                 * Create a data chunk. scanLines adds "nRows" for the filter
                 * bytes.
                 */
                scanLines = new byte[this.width * nRows * this.bytesPerPixel
                                     + nRows];

                if (this.filter == PngEncoder.FILTER_SUB) {
                    this.leftBytes = new byte[16];
                }
                if (this.filter == PngEncoder.FILTER_UP) {
                    this.priorRow = new byte[this.width * this.bytesPerPixel];
                }

                scanPos = 0;
                startPos = 1;
                for (int i = 0; i < this.width * nRows; i++) {
                    if (i % this.width == 0) {
                        scanLines[scanPos++] = (byte) this.filter;
                        startPos = scanPos;
                    }
                    scanLines[scanPos++] = (byte) ((pixels[i] >> 16) & 0xff);
                    scanLines[scanPos++] = (byte) ((pixels[i] >> 8) & 0xff);
                    scanLines[scanPos++] = (byte) ((pixels[i]) & 0xff);
                    if (this.encodeAlpha) {
                        scanLines[scanPos++] = (byte) ((pixels[i] >> 24) & 0xff);
                    }
                    if ((i % this.width == this.width - 1)
                            && (this.filter != PngEncoder.FILTER_NONE)) {
                        if (this.filter == PngEncoder.FILTER_SUB) {
                            this.filterSub(scanLines, startPos, this.width);
                        }
                        if (this.filter == PngEncoder.FILTER_UP) {
                            this.filterUp(scanLines, startPos, this.width);
                        }
                    }
                }

                /*
                 * Write these lines to the output area
                 */
                compBytes.write(scanLines, 0, scanPos);

                startRow += nRows;
                rowsLeft -= nRows;
            }
            compBytes.close();

            /*
             * Write the compressed bytes
             */
            compressedLines = outBytes.toByteArray();
            nCompressed = compressedLines.length;

            this.crc.reset();
            this.bytePos = this.writeInt4(nCompressed, this.bytePos);
            this.bytePos = this.writeString("IDAT", this.bytePos);
            this.crc.update("IDAT".getBytes());
            this.bytePos = this.writeBytes(compressedLines, nCompressed,
                    this.bytePos);
            this.crc.update(compressedLines, 0, nCompressed);

            this.crcValue = this.crc.getValue();
            this.bytePos = this.writeInt4((int) this.crcValue, this.bytePos);
            scrunch.finish();
            return true;
        } catch (final IOException e) {
            System.err.println(e.toString());
            return false;
        }
    }

    /**
     * Write a two-byte integer into the pngBytes array at a given position.
     *
     * @param n
     *            The integer to be written into pngBytes.
     * @param offset
     *            The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeInt2(int n, int offset) {
        final byte[] temp = { (byte) ((n >> 8) & 0xff), (byte) (n & 0xff) };
        return this.writeBytes(temp, offset);
    }

    /**
     * Write a four-byte integer into the pngBytes array at a given position.
     *
     * @param n
     *            The integer to be written into pngBytes.
     * @param offset
     *            The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeInt4(int n, int offset) {
        final byte[] temp = { (byte) ((n >> 24) & 0xff),
                (byte) ((n >> 16) & 0xff), (byte) ((n >> 8) & 0xff),
                (byte) (n & 0xff) };
        return this.writeBytes(temp, offset);
    }

    /**
     * Write a PNG "pHYs" chunk into the pngBytes array.
     */
    protected void writePhys() {
        int startPos;

        startPos = this.bytePos = this.writeInt4(9, this.bytePos);
        this.bytePos = this.writeString("pHYs", this.bytePos);
        final int dots = (int) (this.DPI * 39.37);
        this.bytePos = this.writeInt4(dots, this.bytePos);
        this.bytePos = this.writeInt4(dots, this.bytePos);
        this.bytePos = this.writeByte(1, this.bytePos); // bit depth
        this.crc.reset();
        this.crc.update(this.pngBytes, startPos, this.bytePos - startPos);
        this.crcValue = this.crc.getValue();
        this.bytePos = this.writeInt4((int) this.crcValue, this.bytePos);
    }

    /**
     * Write a string into the pngBytes array at a given position. This uses the
     * getBytes method, so the encoding used will be its default.
     *
     * @param n
     *            The integer to be written into pngBytes.
     * @param offset
     *            The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     * @see java.lang.String#getBytes()
     */
    protected int writeString(String s, int offset) {
        return this.writeBytes(s.getBytes(), offset);
    }
}
