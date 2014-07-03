package rene.util.parser;

import java.util.Vector;

/**
 * This is a string simple parser.
 */

public class StringParser {
    char C[];
    int N, L;
    boolean Error;

    /**
     * @param S
     *            the string to be parsed.
     */
    public StringParser(String S) {
        this.C = S.toCharArray();
        this.N = 0;
        this.L = this.C.length;
        this.Error = (this.N >= this.L);
    }

    /**
     * Advance one character.
     *
     * @return String is not empty.
     */
    public boolean advance() {
        if (this.N < this.L) {
            this.N++;
        }
        if (this.N >= this.L) {
            this.Error = true;
        }
        return !this.Error;
    }

    /**
     * @return next character is white?
     */
    public boolean blank() {
        return (this.C[this.N] == ' ' || this.C[this.N] == '\t'
                || this.C[this.N] == '\n' || this.C[this.N] == '\r');
    }

    /**
     * @return next character is white or c?
     */
    public boolean blank(char c) {
        return (this.C[this.N] == ' ' || this.C[this.N] == '\t'
                || this.C[this.N] == '\n' || this.C[this.N] == '\r' || this.C[this.N] == c);
    }

    /**
     * @return if an error has occured during the parsing.
     */
    public boolean error() {
        return this.Error;
    }

    /**
     * @return next character is a digit?
     */
    public boolean isint() {
        if (this.Error) {
            return false;
        }
        return (this.C[this.N] >= '0' && this.C[this.N] <= '9');
    }

    /**
     * Get the next character.
     */
    public char next() {
        if (this.Error) {
            return ' ';
        } else {
            this.N++;
            if (this.N >= this.L) {
                this.Error = true;
            }
            return this.C[this.N - 1];
        }
    }

    /**
     * Parse digits upto a blank.
     */
    public String parsedigits() {
        if (this.Error) {
            return "";
        }
        while (this.blank()) {
            if (!this.advance()) {
                return "";
            }
        }
        final int n = this.N;
        while (!this.Error && !this.blank()) {
            if (this.N > this.L || this.C[this.N] < '0' || this.C[this.N] > '9') {
                break;
            }
            this.advance();
        }
        return new String(this.C, n, this.N - n);
    }

    /**
     * Parse digits upto the character c or blank.
     */
    public String parsedigits(char c) {
        if (this.Error) {
            return "";
        }
        while (this.blank()) {
            if (!this.advance()) {
                return "";
            }
        }
        final int n = this.N;
        while (!this.Error && !this.blank()) {
            if (this.N > this.L || this.C[this.N] < '0' || this.C[this.N] > '9'
            || this.C[this.N] == c) {
                break;
            }
            this.advance();
        }
        return new String(this.C, n, this.N - n);
    }

    /**
     * Parse an int upto a blank. The int may be negative.
     *
     * @return the int
     */
    public int parseint() {
        int sig = 1;
        try {
            this.skipblanks();
            if (this.Error) {
                return 0;
            }
            if (this.C[this.N] == '-') {
                sig = -1;
                this.N++;
                if (this.N > this.L) {
                    this.Error = true;
                    return 0;
                }
            }
            return sig * Integer.parseInt(this.parsedigits(), 10);
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parse an int upto a blank or c. The int may be negative.
     *
     * @return the int
     */
    public int parseint(char c) {
        int sig = 1;
        try {
            this.skipblanks();
            if (this.Error) {
                return 0;
            }
            if (this.C[this.N] == '-') {
                sig = -1;
                this.N++;
                if (this.N > this.L) {
                    this.Error = true;
                    return 0;
                }
            }
            return sig * Integer.parseInt(this.parsedigits(c), 10);
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parse a word up to a blank.
     */
    public String parseword() {
        if (this.Error) {
            return "";
        }
        while (this.blank()) {
            if (!this.advance()) {
                return "";
            }
        }
        final int n = this.N;
        while (!this.Error && !this.blank()) {
            this.advance();
        }
        return new String(this.C, n, this.N - n);
    }

    /**
     * Parse a word upto the character c or blank.
     */
    public String parseword(char c) {
        if (this.Error) {
            return "";
        }
        while (this.blank()) {
            if (!this.advance()) {
                return "";
            }
        }
        final int n = this.N;
        while (!this.Error && !this.blank(c)) {
            this.advance();
        }
        return new String(this.C, n, this.N - n);
    }

    /**
     * Replace c1 by c2
     */
    public void replace(char c1, char c2) {
        for (int i = 0; i < this.L; i++) {
            if (this.C[i] == c1) {
                this.C[i] = c2;
            }
        }
    }

    /**
     * Skip everything to the string s.
     *
     * @return String was found
     */
    public boolean skip(String s) {
        if (this.Error) {
            return false;
        }
        final int l = s.length();
        if (this.N + l > this.L) {
            return false;
        }
        if (!new String(this.C, this.N, l).equals(s)) {
            return false;
        }
        this.N += l;
        if (this.N >= this.L) {
            this.Error = true;
        }
        return true;
    }

    /**
     * Skip all white characters.
     */
    public void skipblanks() {
        if (this.Error) {
            return;
        }
        while (this.blank()) {
            if (!this.advance()) {
                break;
            }
        }
    }

    /**
     * Cut off the String upto a character c.
     */
    public String upto(char c) {
        if (this.Error) {
            return "";
        }
        int n = this.N;
        while (n < this.L && this.C[n] != c) {
            n++;
        }
        if (n >= this.L) {
            this.Error = true;
        }
        final String s = new String(this.C, this.N, n - this.N);
        this.N = n;
        return s;
    }

    /**
     * Return a String, which is parsed from words with limited length.
     *
     * @param columns
     *            the maximal length of the string
     */
    public String wrapline(int columns) {
        int n = this.N, good = this.N;
        String s = "";
        while (n < this.L) {
            if (this.C[n] == '\n') {
                if (n > this.N) {
                    s = new String(this.C, this.N, n - this.N);
                }
                this.N = n + 1;
                break;
            }
            if (this.C[n] == ' ' || this.C[n] == '\t' || this.C[n] == '\n') {
                good = n;
            }
            n++;
            if (n - this.N >= columns && good > this.N) {
                s = new String(this.C, this.N, good - this.N);
                this.N = good + 1;
                break;
            }
            if (n >= this.L) {
                if (n > this.N) {
                    s = new String(this.C, this.N, n - this.N);
                }
                this.N = n;
                break;
            }
        }
        if (this.N >= this.L) {
            this.Error = true;
        }
        return s;
    }

    /**
     * Parse the string into lines.
     *
     * @param columns
     *            the maximal length of each line
     * @return a Vector with lines
     */
    public Vector<String> wraplines(int columns) {
        final Vector v = new Vector(10, 10);
        String s;
        while (!this.Error) {
            s = this.wrapline(columns);
            v.addElement(s);
        }
        return v;
    }

    public String wraplineword(int columns) {
        int n = this.N;
        final int good = this.N;
        String s = "";
        while (n < this.L) {
            if (this.C[n] == '\n') {
                s = new String(this.C, this.N, n - this.N);
                this.N = n + 1;
                break;
            }
            n++;
            if (n >= this.L) {
                if (n > this.N) {
                    s = new String(this.C, this.N, n - this.N);
                }
                this.N = n;
                break;
            }
            if (n - this.N >= columns && good > this.N) {
                s = new String(this.C, this.N, good - this.N);
                this.N = good + 1;
                if (this.N < this.L && this.C[this.N] != '\n') {
                    s = s + "\\";
                }
                break;
            }
        }
        if (this.N >= this.L) {
            this.Error = true;
        }
        return s;
    }

    public Vector wrapwords(int columns) {
        final Vector v = new Vector(10, 10);
        String s;
        while (!this.Error) {
            s = this.wraplineword(columns);
            v.addElement(s);
        }
        return v;
    }
}
