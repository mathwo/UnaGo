package rene.util.xml;

import rene.util.parser.StringParser;

import java.io.PrintWriter;
import java.util.Vector;

public class XmlWriter {
    PrintWriter Out;

    public XmlWriter(PrintWriter o) {
        this.Out = o;
    }

    public void close() {
        this.Out.close();
    }

    public void endTag(String tag) {
        this.Out.print("</");
        this.Out.print(tag);
        this.Out.print(">");
    }

    public void endTagNewLine(String tag) {
        this.endTag(tag);
        this.Out.println();
    }

    public void finishTag() {
        this.Out.print("/>");
    }

    public void finishTag(String tag) {
        this.Out.print("<");
        this.Out.print(tag);
        this.Out.print("/>");
    }

    public void finishTag(String tag, String arg, String value) {
        this.Out.print("<");
        this.Out.print(tag);
        this.printArg(arg, value);
        this.Out.println("/>");
    }

    public void finishTagNewLine() {
        this.Out.println("/>");
    }

    public void finishTagNewLine(String tag) {
        this.Out.print("<");
        this.Out.print(tag);
        this.Out.println("/>");
    }

    public void print(String s) {
        this.Out.print(XmlTranslator.toXml(s));
    }

    public void printArg(String arg, String value) {
        this.Out.print(" ");
        this.print(arg);
        this.Out.print("=\"");
        this.print(value);
        this.Out.print("\"");
    }

    public void printDoctype(String top, String dtd) {
        this.Out.print("<!DOCTYPE ");
        this.Out.print(top);
        this.Out.print(" SYSTEM \"");
        this.Out.print(dtd);
        this.Out.println("\">");
    }

    public void printEncoding() {
        this.printEncoding("utf-8");
    }

    public void printEncoding(String s) {
        if (s.equals("")) {
            this.Out.println("<?xml version=\"1.0\"?>");
        } else {
            this.Out.println("<?xml version=\"1.0\" encoding=\"" + s + "\"?>");
        }
    }

    public void println() {
        this.Out.println();
    }

    public void println(String s) {
        this.Out.println(XmlTranslator.toXml(s));
    }

    public void printParagraphs(String s, int linelength) {
        final StringParser p = new StringParser(s);
        final Vector v = p.wrapwords(linelength);
        for (int i = 0; i < v.size(); i++) {
            this.startTag("P");
            s = (String) v.elementAt(i);
            final StringParser q = new StringParser(s);
            final Vector w = q.wraplines(linelength);
            for (int j = 0; j < w.size(); j++) {
                if (j > 0) {
                    this.println();
                }
                s = (String) w.elementAt(j);
                this.print(s);
            }
            this.endTagNewLine("P");
        }
    }

    public void printTag(String tag, String content) {
        this.startTag(tag);
        this.print(content);
        this.endTag(tag);
    }

    public void printTag(String tag, String arg, String value, String content) {
        this.startTag(tag, arg, value);
        this.print(content);
        this.endTag(tag);
    }

    public void printTagNewLine(String tag, String content) {
        this.printTag(tag, content);
        this.Out.println();
    }

    public void printTagNewLine(String tag, String arg, String value,
            String content) {
        this.printTag(tag, arg, value, content);
        this.Out.println();
    }

    public void printXls(String s) {
        this.Out.println("<?xml-stylesheet href=\"" + s
                + "\" type=\"text/xsl\"?>");
    }

    public void printXml() {
        this.printEncoding("");
    }

    public void startTag(String tag) {
        this.Out.print("<");
        this.Out.print(tag);
        this.Out.print(">");
    }

    public void startTag(String tag, String arg, String value) {
        this.Out.print("<");
        this.Out.print(tag);
        this.printArg(arg, value);
        this.Out.print(">");
    }

    public void startTagEnd() {
        this.Out.print(">");
    }

    public void startTagEndNewLine() {
        this.Out.println(">");
    }

    public void startTagNewLine(String tag) {
        this.startTag(tag);
        this.Out.println();
    }

    public void startTagNewLine(String tag, String arg, String value) {
        this.startTag(tag, arg, value);
        this.Out.println();
    }

    public void startTagStart(String tag) {
        this.Out.print("<");
        this.Out.print(tag);
    }
}
