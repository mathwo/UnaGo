package rene.util.xml;

public class XmlTag {
    protected String Tag = "";
    String Param[];
    String Value[];
    int N = 0;

    public XmlTag(String s) {
        int n = 0;
        int k = 0;
        n = this.skipBlanks(s, n);
        while (n < s.length()) {
            n = this.endItem(s, n);
            k++;
            n = this.skipBlanks(s, n);
        }
        if (k == 0) {
            return;
        }
        n = 0;
        n = this.skipBlanks(s, n);
        int m = this.endItem(s, n);
        this.Tag = s.substring(n, m);
        n = m;
        this.N = k - 1;
        this.Param = new String[this.N];
        this.Value = new String[this.N];
        for (int i = 0; i < this.N; i++) {
            n = this.skipBlanks(s, n);
            m = this.endItem(s, n);
            final String p = s.substring(n, m);
            n = m;
            final int kp = p.indexOf('=');
            if (kp >= 0) {
                this.Param[i] = p.substring(0, kp);
                this.Value[i] = XmlTranslator.toText(p.substring(kp + 1));
                if (this.Value[i].startsWith("\"")
                        && this.Value[i].endsWith("\"")) {
                    this.Value[i] = this.Value[i].substring(1,
                            this.Value[i].length() - 1);
                } else if (this.Value[i].startsWith("\'")
                        && this.Value[i].endsWith("\'")) {
                    this.Value[i] = this.Value[i].substring(1,
                            this.Value[i].length() - 1);
                }
            } else {
                this.Param[i] = p;
                this.Value[i] = "";
            }
        }
    }

    public int countParams() {
        return this.N;
    }

    int endItem(String s, int n) {
        while (n < s.length()) {
            final char c = s.charAt(n);
            if (c == ' ' || c == '\t' || c == '\n') {
                break;
            }
            if (c == '\"') {
                n++;
                while (true) {
                    if (n >= s.length()) {
                        return n;
                    }
                    if (s.charAt(n) == '\"') {
                        break;
                    }
                    n++;
                }
            } else if (c == '\'') {
                n++;
                while (true) {
                    if (n >= s.length()) {
                        return n;
                    }
                    if (s.charAt(n) == '\'') {
                        break;
                    }
                    n++;
                }
            }
            n++;
        }
        return n;
    }

    public String getParam(int i) {
        return this.Param[i];
    }

    public String getValue(int i) {
        return this.Value[i];
    }

    public String getValue(String param) {
        for (int i = 0; i < this.N; i++) {
            if (this.Param[i].equals(param)) {
                return this.Value[i];
            }
        }
        return null;
    }

    public boolean hasParam(String param) {
        for (int i = 0; i < this.N; i++) {
            if (this.Param[i].equals(param)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTrueParam(String param) {
        for (int i = 0; i < this.N; i++) {
            if (this.Param[i].equals(param)) {
                if (this.Value[i].equals("true")) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public String name() {
        return this.Tag;
    }

    int skipBlanks(String s, int n) {
        while (n < s.length()) {
            final char c = s.charAt(n);
            if (c == ' ' || c == '\t' || c == '\n') {
                n++;
            } else {
                break;
            }
        }
        return n;
    }
}
