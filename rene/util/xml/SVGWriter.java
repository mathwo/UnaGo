package rene.util.xml;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class SVGWriter extends XmlWriter {
    public static void main(String args[]) throws Exception {
        final SVGWriter out = new SVGWriter(new PrintWriter(
                new FileOutputStream("test.svg")), "", 300, 300);
        out.text("Hallo Welt", 10, 95);
        out.startTagStart("path");
        out.printArg("d", "M 150 150 A 50 50 0 1 0 100 200");
        out.printArg("style", "fill:none;stroke-width:1;stroke:black");
        out.finishTagNewLine();
        out.close();
    }

    int W, H;

    public SVGWriter(PrintWriter o) {
        super(o);
    }

    public SVGWriter(PrintWriter o, String enc, int w, int h) {
        super(o);
        this.printEncoding(enc);
        this.W = w;
        this.H = h;
        this.startTagStart("svg");
        this.printArg("width", "" + w);
        this.printArg("height", "" + h);
        this.startTagEndNewLine();
    }

    @Override
    public void close() {
        this.endTag("svg");
        super.close();
    }

    public void coord(int x, int y) {
        this.printArg("x", "" + x);
        this.printArg("y", "" + y);
    }

    public void startSVG(int w, int h) {
        this.printEncoding("utf-8");
        this.Out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"");
        this.Out.println("\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
        this.startTagStart("svg");
        this.printArg("xmlns", "http://www.w3.org/2000/svg");
        this.printArg("width", "" + w);
        this.printArg("height", "" + h);
        this.startTagEndNewLine();
    }

    public void text(String text, int x, int y) {
        this.startTagStart("text");
        this.coord(x, y);
        this.startTagEnd();
        this.print(text);
        this.endTagNewLine("text");
    }
}
