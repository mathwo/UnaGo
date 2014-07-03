package rene.util.xml;

import rene.util.list.ListElement;
import rene.util.list.Tree;
import rene.util.parser.StringParser;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class XmlTree extends Tree implements Enumeration<XmlTree>,
Iterator<XmlTree>, Iterable<XmlTree> {
    ListElement Current;

    public XmlTree(XmlTag t) {
        super(t);
    }

    public Enumeration<XmlTree> getContent() {
        this.Current = this.children().first();
        return this;
    }

    public XmlTag getTag() {
        return (XmlTag) this.content();
    }

    public String getText() {
        if (!this.haschildren()) {
            return "";
        }
        final XmlTree t = (XmlTree) this.firstchild();
        final XmlTag tag = t.getTag();
        return ((XmlTagText) tag).getContent();
    }

    @Override
    public boolean hasMoreElements() {
        return this.Current != null;
    }

    @Override
    public boolean hasNext() {
        return this.Current != null;
    }

    public boolean isTag(String s) {
        return this.getTag().name().equals(s);
    }

    public boolean isText() {
        if (!this.haschildren()) {
            return true;
        }
        if (this.firstchild() != this.lastchild()) {
            return false;
        }
        final XmlTree t = (XmlTree) this.firstchild();
        final XmlTag tag = t.getTag();
        if (!(tag instanceof XmlTagText)) {
            return false;
        }
        return true;
    }

    @Override
    public Iterator iterator() {
        this.Current = this.children().first();
        return this;
    }

    @Override
    public XmlTree next() {
        if (this.Current == null) {
            return null;
        }
        final XmlTree c = (XmlTree) (this.Current.content());
        this.Current = this.Current.next();
        return c;
    }

    @Override
    public XmlTree nextElement() {
        if (this.Current == null) {
            return null;
        }
        final XmlTree c = (XmlTree) (this.Current.content());
        this.Current = this.Current.next();
        return c;
    }

    public String parseComment() throws XmlReaderException {
        final StringBuffer s = new StringBuffer();
        final Enumeration e = this.getContent();
        while (e.hasMoreElements()) {
            final XmlTree tree = (XmlTree) e.nextElement();
            final XmlTag tag = tree.getTag();
            if (tag.name().equals("P")) {
                if (!tree.haschildren()) {
                    s.append("\n");
                } else {
                    final XmlTree h = tree.xmlFirstContent();
                    String k = ((XmlTagText) h.getTag()).getContent();
                    k = k.replace('\n', ' ');
                    final StringParser p = new StringParser(k);
                    final Vector v = p.wraplines(1000);
                    for (int i = 0; i < v.size(); i++) {
                        s.append((String) v.elementAt(i));
                        s.append("\n");
                    }
                }
            } else if (tag instanceof XmlTagText) {
                final String k = ((XmlTagText) tag).getContent();
                final StringParser p = new StringParser(k);
                final Vector v = p.wraplines(1000);
                for (int i = 0; i < v.size(); i++) {
                    s.append((String) v.elementAt(i));
                    s.append("\n");
                }
            } else {
                throw new XmlReaderException("<" + tag.name()
                        + "> not proper here.");
            }
        }
        return s.toString();
    }

    @Override
    public void remove() {
    }

    public XmlTree xmlFirstContent() {
        if (this.firstchild() != null) {
            return (XmlTree) this.firstchild();
        } else {
            return null;
        }
    }
}
