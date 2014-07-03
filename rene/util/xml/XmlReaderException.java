package rene.util.xml;

public class XmlReaderException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String Line;
    int Pos;
    String S;

    public XmlReaderException(String s) {
        this(s, "", 0);
    }

    public XmlReaderException(String s, String line, int pos) {
        super(s);
        this.S = s;
        this.Line = line;
        this.Pos = pos;
    }

    public String getLine() {
        return this.Line;
    }

    public int getPos() {
        return this.Pos;
    }

    public String getText() {
        return this.S;
    }
}
