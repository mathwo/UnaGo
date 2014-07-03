package unagoclient.board;

import rene.util.list.ListClass;
import rene.util.list.ListElement;
import rene.util.parser.StringParser;
import rene.util.xml.XmlWriter;

import java.io.PrintWriter;
import java.util.Vector;

/**
 * Has a type and arguments (as in SGF, e.g. B[ih] of type "B" and Methods
 * include the printing on a PrintWriter.
 */
public class Action {
    String Type; // the type
    ListClass Arguments; // the list of argument strings

    /**
     * Initialize with type only
     */
    public Action(String s) {
        this.Type = s;
        this.Arguments = new ListClass();
    }

    /**
     * Initialize with type and one argument to that type tag.
     */
    public Action(String s, String arg) {
        this.Type = s;
        this.Arguments = new ListClass();
        this.addargument(arg);
    }

    public void addargument(String s)
    // add an argument ot the list (at end)
    {
        this.Arguments.append(new ListElement(s));
    }

    public String argument() {
        if (this.arguments() == null) {
            return "";
        } else {
            return (String) this.arguments().content();
        }
    }

    public ListElement arguments() {
        return this.Arguments.first();
    }

    /**
     * Find an argument
     */
    public boolean contains(String s) {
        ListElement ap = this.Arguments.first();
        while (ap != null) {
            final String t = (String) ap.content();
            if (t.equals(s)) {
                return true;
            }
            ap = ap.next();
        }
        return false;
    }

    public String getXMLMove(int size) {
        final ListElement ap = this.Arguments.first();
        return this.getXMLMove(ap, size);
    }

    /**
     * @return The readable coordinate version (Q16) of a move, stored in first
     *         argument.
     */
    public String getXMLMove(ListElement ap, int size) {
        if (ap == null) {
            return "";
        }
        final String s = (String) ap.content();
        if (s == null) {
            return "";
        }
        final int i = Field.i(s), j = Field.j(s);
        if (i < 0 || i >= size || j < 0 || j >= size) {
            return "";
        }
        return Field.coordinate(Field.i(s), Field.j(s), size);
    }

    /**
     * Test, if this action contains printed information
     */
    public boolean isRelevant() {
        if (this.Type.equals("GN") || this.Type.equals("AP")
                || this.Type.equals("FF") || this.Type.equals("GM")
                || this.Type.equals("N") || this.Type.equals("SZ")
                || this.Type.equals("PB") || this.Type.equals("BR")
                || this.Type.equals("PW") || this.Type.equals("WR")
                || this.Type.equals("HA") || this.Type.equals("KM")
                || this.Type.equals("RE") || this.Type.equals("DT")
                || this.Type.equals("TM") || this.Type.equals("US")
                || this.Type.equals("CP") || this.Type.equals("BL")
                || this.Type.equals("WL") || this.Type.equals("C")) {
            return false;
        } else {
            return true;
        }
    }

    public void print(PrintWriter o)
    // print the action
    {
        if (this.Arguments.first() == null
                || (this.Arguments.first() == this.Arguments.last() &&
                (this.Arguments.first().content()).equals(""))) {
            return;
        }
        o.println();
        o.print(this.Type);
        ListElement ap = this.Arguments.first();
        while (ap != null) {
            o.print("[");
            String s = (String) ap.content();
            final StringParser p = new StringParser(s);
            final Vector v = p.wrapwords(60);
            for (int i = 0; i < v.size(); i++) {
                s = (String) v.elementAt(i);
                if (i > 0) {
                    o.println();
                }
                int k = s.indexOf(']');
                while (k >= 0) {
                    if (k > 0) {
                        o.print(s.substring(0, k));
                    }
                    o.print("\\]");
                    s = s.substring(k + 1);
                    k = s.indexOf(']');
                }
                o.print(s);
            }
            o.print("]");
            ap = ap.next();
        }
    }

    /**
     * Print the node content in XML form.
     */
    public void print(XmlWriter xml, int size, int number) {
        if (this.Type.equals("C")) {
            xml.startTagNewLine("Comment");
            this.printTextArgument(xml);
            xml.endTagNewLine("Comment");
        } else if (this.Type.equals("GN") || this.Type.equals("AP")
                || this.Type.equals("FF") || this.Type.equals("GM")
                || this.Type.equals("N") || this.Type.equals("SZ")
                || this.Type.equals("PB") || this.Type.equals("BR")
                || this.Type.equals("PW") || this.Type.equals("WR")
                || this.Type.equals("HA") || this.Type.equals("KM")
                || this.Type.equals("RE") || this.Type.equals("DT")
                || this.Type.equals("TM") || this.Type.equals("US")
                || this.Type.equals("WL") || this.Type.equals("BL")
                || this.Type.equals("CP")) {
        } else if (this.Type.equals("B")) {
            xml.startTagStart("Black");
            xml.printArg("number", "" + number);
            xml.printArg("at", this.getXMLMove(size));
            xml.finishTagNewLine();
        } else if (this.Type.equals("W")) {
            xml.startTagStart("White");
            xml.printArg("number", "" + number);
            xml.printArg("at", this.getXMLMove(size));
            xml.finishTagNewLine();
        } else if (this.Type.equals("AB")) {
            this.printAllFields(xml, size, "AddBlack");
        } else if (this.Type.equals("AW")) {
            this.printAllFields(xml, size, "AddWhite");
        } else if (this.Type.equals("AE")) {
            this.printAllFields(xml, size, "Delete");
        } else if (this.Type.equals("MA")) {
            this.printAllFields(xml, size, "Mark");
        } else if (this.Type.equals("M")) {
            this.printAllFields(xml, size, "Mark");
        } else if (this.Type.equals("SQ")) {
            this.printAllFields(xml, size, "Mark", "type", "square");
        } else if (this.Type.equals("CR")) {
            this.printAllFields(xml, size, "Mark", "type", "circle");
        } else if (this.Type.equals("TR")) {
            this.printAllFields(xml, size, "Mark", "type", "triangle");
        } else if (this.Type.equals("TB")) {
            this.printAllFields(xml, size, "Mark", "territory", "black");
        } else if (this.Type.equals("TW")) {
            this.printAllFields(xml, size, "Mark", "territory", "white");
        } else if (this.Type.equals("LB")) {
            this.printAllSpecialFields(xml, size, "Mark", "label");
        } else {
            xml.startTag("SGF", "type", this.Type);
            ListElement ap = this.Arguments.first();
            while (ap != null) {
                xml.startTag("Arg");
                String s = (String) ap.content();
                final StringParser p = new StringParser(s);
                final Vector v = p.wrapwords(60);
                for (int i = 0; i < v.size(); i++) {
                    s = (String) v.elementAt(i);
                    if (i > 0) {
                        xml.println();
                    }
                    xml.print(s);
                }
                ap = ap.next();
                xml.endTag("Arg");
            }
            xml.endTagNewLine("SGF");
        }
    }

    /**
     * Print all arguments as field positions with the specified tag.
     */
    public void printAllFields(XmlWriter xml, int size, String tag) {
        ListElement ap = this.Arguments.first();
        while (ap != null) {
            ap.content();
            xml.startTagStart(tag);
            xml.printArg("at", this.getXMLMove(ap, size));
            xml.finishTagNewLine();
            ap = ap.next();
        }
    }

    public void printAllFields(XmlWriter xml, int size, String tag,
            String argument, String value) {
        ListElement ap = this.Arguments.first();
        while (ap != null) {
            ap.content();
            xml.startTagStart(tag);
            xml.printArg(argument, value);
            xml.printArg("at", this.getXMLMove(ap, size));
            xml.finishTagNewLine();
            ap = ap.next();
        }
    }

    public void printAllSpecialFields(XmlWriter xml, int size, String tag,
            String argument) {
        ListElement ap = this.Arguments.first();
        while (ap != null) {
            String s = (String) ap.content();
            final StringParser p = new StringParser(s);
            s = p.parseword(':');
            p.skip(":");
            final String value = p.parseword();
            xml.startTagStart(tag);
            xml.printArg(argument, value);
            xml.printArg("at", this.getXMLMove(ap, size));
            xml.finishTagNewLine();
            ap = ap.next();
        }
    }

    /**
     * Print the node content of a move in XML form and take care of times and
     * names.
     */
    public void printMove(XmlWriter xml, int size, int number, Node n) {
        String s = "";
        if (this.Type.equals("B")) {
            s = "Black";
        } else if (this.Type.equals("W")) {
            s = "White";
        } else {
            return;
        }
        xml.startTagStart(s);
        xml.printArg("number", "" + number);
        if (n.contains("N")) {
            xml.printArg("name", n.getaction("N"));
        }
        if (s.equals("Black") && n.contains("BL")) {
            xml.printArg("timeleft", n.getaction("BL"));
        }
        if (s.equals("White") && n.contains("WL")) {
            xml.printArg("timeleft", n.getaction("WL"));
        }
        xml.printArg("at", this.getXMLMove(size));
        xml.finishTagNewLine();
    }

    public void printTextArgument(XmlWriter xml) {
        final ListElement ap = this.Arguments.first();
        if (ap == null) {
            return;
        }
        xml.printParagraphs((String) ap.content(), 60);
    }

    public void toggleArgument(String s)
    // add an argument ot the list (at end)
    {
        ListElement ap = this.Arguments.first();
        while (ap != null) {
            final String t = (String) ap.content();
            if (t.equals(s)) {
                this.Arguments.remove(ap);
                return;
            }
            ap = ap.next();
        }
        this.Arguments.append(new ListElement(s));
    }

    // access methods:
    public String getType() {
        return this.Type;
    }

    // modifiers
    public void setType(String s) {
        this.Type = s;
    }
}
