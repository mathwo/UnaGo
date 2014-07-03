package rene.util.xml;

import rene.util.SimpleByteBuffer;
import rene.util.SimpleStringBuffer;
import rene.util.list.ListElement;

import java.io.*;

public class XmlReader {
    /**
     * A test program.
     */
    public static void main(String args[]) {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream("rene\\util\\xml\\test.xml"), "UTF8"));
            final XmlReader reader = new XmlReader(in);
            final XmlTree tree = reader.scan();
            in.close();
            XmlReader.print(tree);
        } catch (final XmlReaderException e) {
            System.out.println(e.toString() + "\n" + e.getLine() + "\n"
                    + "Position : " + e.getPos());
        } catch (final IOException e) {
            System.out.println(e);
        }
    }

    public static void print(XmlTree t) {
        final XmlTag tag = t.getTag();
        System.out.print("<" + tag.name());
        for (int i = 0; i < tag.countParams(); i++) {
            System.out.print(" " + tag.getParam(i) + "=\"" + tag.getValue(i)
                    + "\"");
        }
        System.out.println(">");
        ListElement el = t.children().first();
        while (el != null) {
            XmlReader.print((XmlTree) (el.content()));
            el = el.next();
        }
        System.out.println("</" + tag.name() + ">");
    }

    public static boolean testXml(String s) {
        int i = 0;
        while (i < s.length()) {
            final char c = s.charAt(i);
            if (c == '<') {
                break;
            }
            i++;
        }
        if (i >= s.length()) {
            return false;
        }
        if (s.substring(i).startsWith("<?xml")) {
            return true;
        }
        return false;
    }

    BufferedReader In;

    SimpleStringBuffer buf = new SimpleStringBuffer(10000);

    String Line = null;

    int LinePos;

    public XmlReader() {
        this.In = null;
    }

    public XmlReader(BufferedReader in) {
        this.In = in;
    }

    public XmlReader(InputStream in) throws XmlReaderException {
        try { // read the file into a buffer
            final BufferedInputStream rin = new BufferedInputStream(in);
            final SimpleByteBuffer bb = new SimpleByteBuffer(10000);
            while (true) {
                final int k = rin.read();
                if (k < 0) {
                    break;
                }
                bb.append((byte) k);
            }
            rin.close();
            final byte b[] = bb.getByteArray();

            // Try to open an ASCII stream, or a default stream
            ByteArrayInputStream bin = new ByteArrayInputStream(b);
            XmlReader R = null;
            try {
                R = new XmlReader(new BufferedReader(new InputStreamReader(bin,
                        "ASCII")));
            } catch (final UnsupportedEncodingException ex) {
                R = new XmlReader(
                        new BufferedReader(new InputStreamReader(bin)));
            }

            // Determine the encoding
            String Encoding = null;
            while (true) {
                while (true) {
                    final int c = R.read();
                    if (c == -1) {
                        throw new Exception("<?xml> tag not found");
                    }
                    if (c == '<') {
                        break;
                    }
                }
                if (R.found("?xml")) {
                    String s = R.scanFor("?>");
                    if (s == null) {
                        throw new Exception("<?xml> tag error");
                    }
                    int n = s.indexOf("encoding=\"");
                    if (n >= 0) {
                        n += "encoding=\"".length();
                        s = s.substring(n);
                        final int m = s.indexOf('\"');
                        if (m < 0) {
                            throw new Exception("Closing bracket missing");
                        }
                        Encoding = s.substring(0, m).toUpperCase();
                        if (Encoding.equals("UTF-8")) {
                            Encoding = "UTF8";
                        }
                        // for IE5 !
                        break;
                    }
                    break;
                }
            }

            // Open a stream with this encoding
            bin = new ByteArrayInputStream(b);
            if (Encoding == null) {
                this.In = new BufferedReader(new InputStreamReader(bin));
            } else {
                try {
                    this.In = new BufferedReader(new InputStreamReader(bin,
                            Encoding));
                } catch (final UnsupportedEncodingException e) {
                    try {
                        this.In = new BufferedReader(new InputStreamReader(bin,
                                "ASCII"));
                    } catch (final UnsupportedEncodingException ex) {
                        this.In = new BufferedReader(new InputStreamReader(bin));
                    }
                }
            }
        } catch (final Exception e) {
            throw new XmlReaderException(e.toString());
        }
    }

    public boolean empty(String s) {
        final int n = s.length();
        for (int i = 0; i < n; i++) {
            final char c = s.charAt(i);
            if (c != ' ' && c != '\n' && c != '\t') {
                return false;
            }
        }
        return true;
    }

    public void exception(String s) throws XmlReaderException {
        throw new XmlReaderException(s, this.Line, this.LinePos);
    }

    /**
     * @return If the string is at the current line position.
     */
    public boolean found(String s) {
        final int n = s.length();
        if (this.LinePos + n > this.Line.length()) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (s.charAt(i) != this.Line.charAt(this.LinePos + i)) {
                return false;
            }
        }
        return true;
    }

    public void init(InputStream in) throws XmlReaderException {
        try { // read the file into a buffer
            final BufferedInputStream rin = new BufferedInputStream(in);
            final SimpleByteBuffer bb = new SimpleByteBuffer(10000);
            while (true) {
                final int k = rin.read();
                if (k < 0) {
                    break;
                }
                bb.append((byte) k);
            }
            rin.close();
            final byte b[] = bb.getByteArray();

            // Try to open an ASCII stream, or a default stream
            ByteArrayInputStream bin = new ByteArrayInputStream(b);
            XmlReader R = null;
            try {
                R = new XmlReader(new BufferedReader(new InputStreamReader(bin,
                        "ASCII")));
            } catch (final UnsupportedEncodingException ex) {
                R = new XmlReader(
                        new BufferedReader(new InputStreamReader(bin)));
            }

            // Determine the encoding
            String Encoding = null;
            while (true) {
                while (true) {
                    final int c = R.read();
                    if (c == -1) {
                        throw new Exception("<?xml> tag not found");
                    }
                    if (c == '<') {
                        break;
                    }
                }
                if (R.found("?xml")) {
                    String s = R.scanFor("?>");
                    if (s == null) {
                        throw new Exception("<?xml> tag error");
                    }
                    int n = s.indexOf("encoding=\"");
                    if (n >= 0) {
                        n += "encoding=\"".length();
                        s = s.substring(n);
                        final int m = s.indexOf('\"');
                        if (m < 0) {
                            throw new Exception("Closing bracket missing");
                        }
                        Encoding = s.substring(0, m).toUpperCase();
                        if (Encoding.equals("UTF-8")) {
                            Encoding = "UTF8";
                        }
                        // for IE5 !
                        break;
                    }
                    break;
                }
            }

            // Open a stream with this encoding
            bin = new ByteArrayInputStream(b);
            if (Encoding == null) {
                this.In = new BufferedReader(new InputStreamReader(bin));
            } else {
                try {
                    this.In = new BufferedReader(new InputStreamReader(bin,
                            Encoding));
                } catch (final UnsupportedEncodingException e) {
                    try {
                        this.In = new BufferedReader(new InputStreamReader(bin,
                                "ASCII"));
                    } catch (final UnsupportedEncodingException ex) {
                        this.In = new BufferedReader(new InputStreamReader(bin));
                    }
                }
            }
        } catch (final Exception e) {
            throw new XmlReaderException(e.toString());
        }
    }

    public int read() throws XmlReaderException {
        try {
            if (this.Line == null) {
                this.Line = this.In.readLine();
                this.LinePos = 0;
                // System.out.println("Read --> "+Line);
            }
            if (this.Line == null) {
                return -1;
            }
            if (this.LinePos >= this.Line.length()) {
                this.Line = null;
                return '\n';
            }
            return this.Line.charAt(this.LinePos++);
        } catch (final Exception e) {
            return -1;
        }
    }

    /**
     * Scan an xml file. This function reads, until <?xml is found. then it
     * skips this declaration and scans the rest of the items.
     */
    public XmlTree scan() throws XmlReaderException {
        while (true) {
            while (true) {
                final int c = this.read();
                if (c == -1) {
                    return null;
                }
                if (c == '<') {
                    break;
                }
            }
            if (this.found("?xml")) {
                final String s = this.scanFor("?>");
                if (s == null) {
                    return null;
                }
                final XmlTree t = new XmlTree(new XmlTagRoot());
                t.addchild(new XmlTree(new XmlTagPI(s)));
                this.scanContent(t);
                return t;
            }
        }
    }

    public void scanContent(XmlTree t) throws XmlReaderException { // System.out.println("Sanning for "+t.getTag().name()+" ("+
        // t.getTag().countParams()+")");
        while (true) {
            String s = this.scanFor('<');
            if (s == null) {
                if (t.getTag() instanceof XmlTagRoot) {
                    return;
                }
                this.exception("File ended surprisingly");
            }
            if (!this.empty(s)) {
                t.addchild(new XmlTree(new XmlTagText(XmlTranslator.toText(s))));
            }
            if (this.found("!--")) {
                s = this.scanFor("-->");
                continue;
            }
            if (this.found("!")) {
                s = this.scanTagFor('>');
                continue;
            }
            if (this.found("?")) {
                s = this.scanTagFor("?>");
                t.addchild(new XmlTree(new XmlTagPI(s)));
                continue;
            }
            s = this.scanTagFor('>');
            if (s == null) {
                this.exception("> missing");
            }
            if (s.startsWith("/")) {
                if (s.substring(1).equals(t.getTag().name())) {
                    return;
                } else {
                    this.exception("End tag without start tag");
                }
            }
            if (s.endsWith("/")) {
                t.addchild(new XmlTree(new XmlTag(
                        s.substring(0, s.length() - 1))));
            } else {
                final XmlTree t0 = new XmlTree(new XmlTag(s));
                this.scanContent(t0);
                t.addchild(t0);
            }
        }
    }

    /**
     * Scan for an end character.
     *
     * @return String between the current position and the end character, or
     *         null.
     */
    public String scanFor(char end) throws XmlReaderException {
        this.buf.clear();
        int c = this.read();
        while (c != end) {
            this.buf.append((char) c);
            c = this.read();
            if (c < 0) {
                return null;
            }
        }
        return this.buf.toString();
    }

    /**
     * Scan for a specific string.
     *
     * @return String between the current position and the string.
     */
    public String scanFor(String s) throws XmlReaderException {
        this.buf.clear();
        while (!this.found(s)) {
            final int c = this.read();
            if (c < 0) {
                return null;
            }
            this.buf.append((char) c);
        }
        for (int i = 0; i < s.length(); i++) {
            this.read();
        }
        return this.buf.toString();
    }

    /**
     * Scan tag for an end character (interpreting " and ')
     *
     * @return String between the current position and the end character, or
     *         null.
     */
    public String scanTagFor(char end) throws XmlReaderException {
        this.buf.clear();
        int c = this.read();
        while (c != end) {
            if (c == '\"') {
                this.buf.append((char) c);
                while (true) {
                    c = this.read();
                    if (c < 0) {
                        return null;
                    }
                    if (c == '\"') {
                        break;
                    }
                    this.buf.append((char) c);
                }
                this.buf.append((char) c);
            } else if (c == '\'') {
                this.buf.append((char) c);
                while (true) {
                    c = this.read();
                    if (c < 0) {
                        return null;
                    }
                    if (c == '\'') {
                        break;
                    }
                    this.buf.append((char) c);
                }
                this.buf.append((char) c);
            } else {
                this.buf.append((char) c);
            }
            c = this.read();
            if (c < 0) {
                return null;
            }
        }
        return this.buf.toString();
    }

    /**
     * Scan tag for a specific string (interpreting " and ')
     *
     * @return String between the current position and the string.
     */
    public String scanTagFor(String s) throws XmlReaderException {
        this.buf.clear();
        while (!this.found(s)) {
            int c = this.read();
            if (c < 0) {
                return null;
            }
            if (c == '\"') {
                this.buf.append((char) c);
                while (true) {
                    c = this.read();
                    if (c < 0) {
                        return null;
                    }
                    if (c == '\"') {
                        break;
                    }
                    this.buf.append((char) c);
                }
                this.buf.append((char) c);
            } else if (c == '\'') {
                this.buf.append((char) c);
                while (true) {
                    c = this.read();
                    if (c < 0) {
                        return null;
                    }
                    if (c == '\'') {
                        break;
                    }
                    this.buf.append((char) c);
                }
                this.buf.append((char) c);
            } else {
                this.buf.append((char) c);
            }
        }
        for (int i = 0; i < s.length(); i++) {
            this.read();
        }
        return this.buf.toString();
    }

    /**
     * Skip Blanks.
     *
     * @return Non-blank character or -1 for EOF.
     */
    public int skipBlanks() throws XmlReaderException {
        while (true) {
            final int c = this.read();
            if (c == ' ' || c == '\t' || c == '\n') {
                continue;
            } else {
                return c;
            }
        }
    }
}
