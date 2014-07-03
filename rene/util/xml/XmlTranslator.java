package rene.util.xml;

import rene.util.SimpleStringBuffer;

public class XmlTranslator {
    static boolean find(String s, int pos, String t) {
        try {
            for (int i = 0; i < t.length(); i++) {
                if (s.charAt(pos + i) != t.charAt(i)) {
                    return false;
                }
            }
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    static void toH(String s) {
        final int m = s.length();
        for (int i = 0; i < m; i++) {
            XmlTranslator.H.append(s.charAt(i));
        }
    }

    static String toText(String s) {
        final int m = s.length();
        XmlTranslator.H.clear();
        for (int i = 0; i < m; i++) {
            final char c = s.charAt(i);
            if (c == '&') {
                if (XmlTranslator.find(s, i, "&lt;")) {
                    XmlTranslator.H.append('<');
                    i += 3;
                } else if (XmlTranslator.find(s, i, "&gt;")) {
                    XmlTranslator.H.append('>');
                    i += 3;
                } else if (XmlTranslator.find(s, i, "&quot;")) {
                    XmlTranslator.H.append('\"');
                    i += 5;
                } else if (XmlTranslator.find(s, i, "&apos;")) {
                    XmlTranslator.H.append('\'');
                    i += 5;
                } else if (XmlTranslator.find(s, i, "&amp;")) {
                    XmlTranslator.H.append('&');
                    i += 4;
                } else {
                    XmlTranslator.H.append(c);
                }
            } else {
                XmlTranslator.H.append(c);
            }
        }
        return XmlTranslator.H.toString();
    }

    static String toXml(String s) {
        final int m = s.length();
        XmlTranslator.H.clear();
        for (int i = 0; i < m; i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '<':
                    XmlTranslator.toH("&lt;");
                    break;
                case '>':
                    XmlTranslator.toH("&gt;");
                    break;
                case '&':
                    XmlTranslator.toH("&amp;");
                    break;
                case '\'':
                    XmlTranslator.toH("&apos;");
                    break;
                case '\"':
                    XmlTranslator.toH("&quot;");
                    break;
                default:
                    XmlTranslator.H.append(c);
            }
        }
        return XmlTranslator.H.toString();
    }

    static SimpleStringBuffer H = new SimpleStringBuffer(10000);
}
