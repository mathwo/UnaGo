package rene.util.regexp;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

class AlphaNumericRange extends RangeClass {
    public AlphaNumericRange(boolean exclude) {
        super(exclude);
    }

    @Override
    public boolean inRange(char c) {
        return Character.isLetterOrDigit(c);
    }
}

class AlphaRange extends RangeClass {
    public AlphaRange(boolean exclude) {
        super(exclude);
    }

    @Override
    public boolean inRange(char c) {
        return Character.isLetter(c);
    }
}

/**
 * An atom is a single letter a dot, or a range. It has a multiplication state
 * *, + or ?. The atom can scan itself from the regular expression, advancing
 * the scan position, and it can match itself against a sting, advancing the
 * search position. If asked, it can find a second match or say that there is
 * none. In the first case the position needs to be restored to the end of the
 * matched string.
 */

class Atom {
    /**
     * The regular expression this atom belopngs to
     */
    RegExp R;
    /**
     * The multiplicator states
     */
    final static int mult1 = 0, mult01 = 1, mult12 = 2, mult012 = 3;
    /**
     * The state of this atom
     */
    int Mult;
    /**
     * Match position and end of the matching string.
     */
    int LastMatch, MatchEnd;
    /**
     * Place to store the position, which must be restored for nextMatch.
     */
    Position P;
    /**
     * There might be a nextMatch()
     */
    boolean Match;

    public Atom(RegExp r) {
        this.R = r;
        this.Mult = Atom.mult1;
    }

    /**
     * Search for one or more repetitions.
     */
    public boolean canMultiple() {
        return this.Mult == Atom.mult012 || this.Mult == Atom.mult12;
    }

    /**
     * Satisfied with zero strings or not.
     */
    public boolean canVoid() {
        return this.Mult == Atom.mult012 || this.Mult == Atom.mult01;
    }

    /**
     * Does the position match? Find the longest match first.
     *
     * @return Success or failure.
     */
    public boolean match(Position p) {
        return false;
    }

    /**
     * Is there another match? Restore the position, if there is.
     */
    public boolean nextMatch() {
        return false;
    }

    /**
     * Note the position structure and the current position. Set Match initality
     * to false.
     */
    public void notePosition(Position p) {
        this.P = p;
        this.LastMatch = this.P.pos();
        this.Match = false;
    }

    /**
     * Scan yourself from the regular expression and advance the position.
     *
     * @return Success or failure.
     */
    public boolean scan(Position p) throws RegExpException {
        return false;
    }

    /**
     * Scan the multiplicator item behind the atom.
     */
    public void scanMult(Position p) {
        if (!p.end()) {
            switch (p.get()) {
                case '*':
                    this.Mult = Atom.mult012;
                    break;
                case '+':
                    this.Mult = Atom.mult12;
                    break;
                case '?':
                    this.Mult = Atom.mult01;
                    break;
                default:
                    return;
            }
            p.advance();
        }
    }
}

/**
 * This scans and matches (expression).
 */

class Bracket extends Atom {
    Part P;
    boolean Top;
    int EN;
    Position Pos;
    int K;

    public Bracket(RegExp r, boolean top) {
        super(r);
        this.Top = top;
        this.EN = r.EN;
        r.EN++;
    }

    @Override
    public boolean match(Position p) {
        this.Pos = p;
        this.K = p.pos();
        final boolean result = this.P.match(p);
        if (result && this.Top) {
            this.R.E.insertElementAt(new PositionRange(this.K, p.pos()),
                    this.EN);
        }
        return result;
    }

    @Override
    public boolean nextMatch() {
        final boolean result = this.P.nextMatch();
        if (result && this.Top) {
            this.R.E.insertElementAt(new PositionRange(this.K, this.Pos.pos()),
                    this.EN);
        }
        return result;
    }

    @Override
    public boolean scan(Position p) throws RegExpException {
        p.advance();
        this.P = new Part(this.R, false);
        this.P.scan(p);
        if (p.end() || p.get() != ')') {
            throw new RegExpException("round.bracket", p.pos());
        }
        p.advance();
        return true;
    }
}

/**
 * Branches are |ed to get a regular expression. Each branch consists of a
 * sequence of atoms.
 */

class Branch {
    RegExp R;
    Vector V;
    boolean Top;

    public Branch(RegExp r, boolean top) {
        this.R = r;
        this.Top = top;
        this.V = new Vector();
    }

    public boolean match(Position p) {
        return this.match(p, 0);
    }

    /**
     * The match is done by crawling through the atoms recursively. The atom i
     * is asked for another match, until everything fails.
     */
    public boolean match(Position p, int i) {
        if (i >= this.V.size()) {
            return false;
        }
        if (i + 1 >= this.V.size()) {
            final Atom a = (Atom) this.V.elementAt(i);
            return a.match(p);
        } else {
            final Atom a = (Atom) this.V.elementAt(i);
            if (a.match(p)) {
                if (this.match(p, i + 1)) {
                    return true;
                } else {
                    while (a.nextMatch()) {
                        if (this.match(p, i + 1)) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Search for another match.
     */
    public boolean nextMatch() {
        return this.nextMatch(0);
    }

    public boolean nextMatch(int i) {
        if (i >= this.V.size()) {
            return false;
        }
        if (i + 1 >= this.V.size()) {
            final Atom a = (Atom) this.V.elementAt(i);
            return a.nextMatch();
        } else {
            final Atom a = (Atom) this.V.elementAt(i);
            if (a.nextMatch()) {
                if (this.nextMatch(i + 1)) {
                    return true;
                } else {
                    while (a.nextMatch()) {
                        if (this.nextMatch(i + 1)) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Scan for atoms. The atoms are recognized by their first letter.
     */
    public boolean scan(Position p) throws RegExpException {
        while (!p.end()) {
            final char c = p.get();
            Atom a;
            switch (c) {
                case '.':
                    a = new Dot(this.R);
                    break;
                case '\\':
                    p.advance();
                    if (!p.end()) {
                        switch (p.get()) {
                            case 't':
                                a = new SpecialChar(this.R, (char) 9);
                                break;
                            default:
                                if (p.get() >= '0' && p.get() <= '9') {
                                    a = new Previous(this.R, p.get() - '0');
                                } else {
                                    a = new Char(this.R);
                                }
                                break;
                        }
                    } else {
                        throw new RegExpException("illegal.escape", p.pos());
                    }
                    break;
                case '[':
                    a = new Range(this.R);
                    break;
                case '(':
                    a = new Bracket(this.R, this.Top);
                    break;
                case '|':
                    return true;
                case ')':
                    return true;
                case '^':
                    a = new Pos(this.R, 0);
                    break;
                case '$':
                    a = new Pos(this.R, -1);
                    break;
                default:
                    a = new Char(this.R);
                    break;
            }
            a.scan(p);
            this.V.addElement(a);
        }
        return this.V.size() > 0;
    }
}

/**
 * A single character match.
 */

class Char extends Simple {
    char C;

    public Char(RegExp r) {
        super(r);
    }

    @Override
    public boolean matchSimple(Position p) {
        return (this.R.uppercase(p.get()) == this.C);
    }

    @Override
    public boolean scan(Position p) throws RegExpException {
        this.C = p.get();
        p.advance();
        this.scanMult(p);
        return true;
    }
}

class CharRange extends RangeClass {
    int Min, Max;

    public CharRange(int min, int max, boolean exclude) {
        super(exclude);
        this.Min = min;
        this.Max = max;
    }

    @Override
    public boolean inRange(char c) {
        return c >= this.Min && c <= this.Max;
    }
}

class ControlRange extends RangeClass {
    public ControlRange(boolean exclude) {
        super(exclude);
    }

    @Override
    public boolean inRange(char c) {
        return Character.isISOControl(c);
    }
}

/**
 * Matches any character.
 */

class Dot extends Simple {
    public Dot(RegExp r) {
        super(r);
    }

    @Override
    public boolean matchSimple(Position p) {
        return true;
    }

    @Override
    public boolean scan(Position p) throws RegExpException {
        p.advance();
        this.scanMult(p);
        return true;
    }
}

class LowerRange extends RangeClass {
    public LowerRange(boolean exclude) {
        super(exclude);
    }

    @Override
    public boolean inRange(char c) {
        return Character.isLowerCase(c);
    }
}

class NumericRange extends RangeClass {
    public NumericRange(boolean exclude) {
        super(exclude);
    }

    @Override
    public boolean inRange(char c) {
        return Character.isDigit(c);
    }
}

/**
 * A part is expression|part or a single expression.
 */

class Part {
    RegExp R;
    Branch Left;
    Part Right;
    boolean Top;
    int EN;

    public Part(RegExp r, boolean top) {
        this.R = r;
        this.Top = top;
    }

    /**
     * The match is true if the first part is true. or the remaining parts are
     * true.
     */
    public boolean match(Position p) {
        final int k = p.pos();
        if (this.Top) {
            this.R.E.removeAllElements();
            this.R.EN = 0;
        }
        if (this.Left.match(p)) {
            if (this.Top) {
                this.R.EN = this.EN;
            }
            return true;
        } else {
            p.pos(k);
            if (this.Right != null) {
                return this.Right.match(p);
            } else {
                return false;
            }
        }
    }

    /**
     * This tests for another match of any sub-branch.
     */
    public boolean nextMatch() {
        if (this.Left.nextMatch()) {
            return true;
        } else {
            if (this.Right != null) {
                return this.Right.nextMatch();
            }
        }
        return false;
    }

    public boolean scan(Position p) throws RegExpException {
        if (this.Top) {
            this.R.EN = 0;
        }
        this.Left = new Branch(this.R, this.Top);
        this.Left.scan(p);
        if (this.Top) {
            this.EN = this.R.EN;
        }
        if (!p.end() && p.get() == '|') {
            if (this.Top) {
                this.R.EN = 0;
            }
            this.Right = new Part(this.R, this.Top);
            p.advance();
            return this.Right.scan(p);
        }
        return true;
    }
}

/**
 * Pos matches the nullstring at a specified position.
 */

class Pos extends Atom {
    int P;

    public Pos(RegExp r, int pos) {
        super(r);
        this.P = pos;
    }

    @Override
    public boolean match(Position p) {
        if (this.P >= 0) {
            return (p.pos() == this.P);
        } else {
            return (p.pos() == p.length() + this.P + 1);
        }
    }

    @Override
    public boolean scan(Position p) {
        p.advance();
        return true;
    }
}

/**
 * Holds a position in a line of characters. This is used to store the new
 * postion too, so that matches can advance the position and return the match
 * state.
 */

class Position {
    public char A[];
    public int K, N;

    public Position(char a[]) {
        this.A = a;
        this.K = 0;
        this.N = this.A.length;
    }

    public void advance() {
        this.K++;
    }

    public void advance(int i) {
        this.K += i;
    }

    public boolean end() {
        return this.K >= this.N;
    }

    public char get() {
        return this.A[this.K];
    }

    public int length() {
        return this.N;
    }

    public int pos() {
        return this.K;
    }

    public void pos(int k) {
        this.K = k;
    }
}

class Previous extends Atom {
    int P;

    public Previous(RegExp r, int p) {
        super(r);
        this.P = p;
    }

    @Override
    public boolean match(Position p) {
        try {
            final String s = this.R.getBracket(this.P);
            if (s == null) {
                return false;
            }
            final char a[] = s.toCharArray();
            for (int i = 0; i < a.length; i++) {
                if (p.end() || a[i] != p.get()) {
                    return false;
                }
                p.advance();
            }
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public boolean scan(Position p) {
        p.advance();
        return true;
    }
}

/**
 * The Range class holds a vector of ranges, single characters, or named ranges.
 * All are subclasses of RangeClass.
 */

class Range extends Simple {
    Vector V;
    boolean Any;

    public Range(RegExp r) {
        super(r);
        this.Any = true;
    }

    /**
     * Get the next position and scan \] etc. correctly.
     *
     * @return 0 on failure.
     */
    public char getNext(Position p) throws RegExpException {
        if (p.end()) {
            throw new RegExpException("bracket.range");
        }
        char c = p.get();
        if (c == '\\') {
            p.advance();
            if (p.end()) {
                throw new RegExpException("illegal.backslash", p.pos());
            }
            c = p.get();
            if (c == 't') {
                c = (char) 9;
            }
        }
        p.advance();
        if (p.end()) {
            throw new RegExpException("bracket.range", p.pos());
        }
        return c;
    }

    /**
     * Walk through the vector of ranges and set the range.
     */
    @Override
    public boolean matchSimple(Position p) {
        boolean match = this.Any;
        for (int i = 0; i < this.V.size(); i++) {
            final RangeClass r = (RangeClass) this.V.elementAt(i);
            if (r.isExclude()) {
                if (r.inRange(this.R.uppercase(p.get()))) {
                    return false;
                }
            } else {
                if (r.inRange(this.R.uppercase(p.get()))) {
                    match = true;
                }
            }
        }
        return match;
    }

    @Override
    public boolean scan(Position p) throws RegExpException {
        this.V = new Vector();
        boolean exclude = false;
        p.advance();
        while (!p.end() && p.get() != ']') {
            if (p.get() == '^') {
                exclude = true;
                p.advance();
            }
            if (!exclude) {
                this.Any = false;
            }
            final char a = this.getNext(p);
            if (a == '[') {
                this.scanNamedRange(p, exclude);
            } else {
                char b = a;
                if (p.get() == '-') {
                    p.advance();
                    b = this.getNext(p);
                }
                this.V.addElement(new CharRange(a, b, exclude));
            }
        }
        if (p.end() || p.get() != ']') {
            throw new RegExpException("bracket.range", p.pos());
        }
        p.advance();
        this.scanMult(p);
        return true;
    }

    public void scanNamedRange(Position p, boolean exclude)
            throws RegExpException {
        if (this.getNext(p) != ':') {
            throw new RegExpException("bracket.namedrange", p.pos());
        }
        final StringBuffer b = new StringBuffer();
        while (true) {
            final char a = this.getNext(p);
            if (a == ':') {
                break;
            }
            b.append(a);
        }
        if (this.getNext(p) != ']') {
            throw new RegExpException("bracket.namedrange", p.pos());
        }
        final String s = b.toString();
        if (s.equals("alpha")) {
            this.V.addElement(new AlphaRange(exclude));
        } else if (s.equals("digit")) {
            this.V.addElement(new NumericRange(exclude));
        } else if (s.equals("alnum")) {
            this.V.addElement(new AlphaNumericRange(exclude));
        } else if (s.equals("cntrl")) {
            this.V.addElement(new ControlRange(exclude));
        } else if (s.equals("lower")) {
            this.V.addElement(new LowerRange(exclude));
        } else if (s.equals("upper")) {
            this.V.addElement(new UpperRange(exclude));
        } else if (s.equals("space")) {
            this.V.addElement(new SpaceRange(exclude));
        } else if (s.equals("white")) {
            this.V.addElement(new WhiteSpaceRange(exclude));
        } else {
            throw new RegExpException("bracket.namedrange", p.pos());
        }
    }
}

/**
 * Holds one of the ranges in a character range, or a single character. The
 * range may include or exclude.
 */

class RangeClass {
    boolean Exclude;

    public RangeClass(boolean exclude) {
        this.Exclude = exclude;
    }

    public boolean inRange(char c) {
        return false;
    }

    public boolean isExclude() {
        return this.Exclude;
    }
}

/**
 * This is a class to scan a string with a regular expression. It follows the
 * normal rules for regular expressions with some exceptions and extensions. Any
 * instance of this class can perform as a match tool for input strings, using
 * the match method.
 * <p>
 * Here is a formal description of a regular expression. It is has one or more
 * branches, separated by |. It matches anything, that matches one of the
 * branches.
 * <p>
 * A branch has one or more atoms concatenated. It matches the string, if the
 * atoms match strings, which concatenate to the given string.
 * <p>
 * An atom is either a string of non-special letters. A special letter (such as
 * |) becomes a non-special letter, if it is preceded by \. Or an atom is a
 * regular expression in (). Or it is a sequence of letters in [] (a range). Or
 * it is a . indicating any character.
 * <p>
 * An atom followed by * may repeat zero or more times, followed by + one or
 * more times, followed by ? zero or one times.
 * <p>
 * A range consists of letters, ranges of letters as in A-Z, or a ^ character,
 * indicating that the letters or letter ranges are excluded. The special
 * letters must be preceded by \.
 * <p>
 * There are the predefined ranges [:alpha:], [:digit:], [:alnum:], [:space:],
 * [:white:], [:cntrl:], [:lower:] and [:upper:]. Note that the brackes are part
 * of the range definition.
 * <p>
 * Contrary to the normal implementation, ] and - must be escaped, when they are
 * to appear in ranges. Also, ranges may contain include and exclude character
 * ranges at the same time, as in [a-z^x].
 * <p>
 * The atom ^ matches only the beginning of the line, while $ matches the line
 * end.
 */

public class RegExp {
    /**
     * A main() to test the scanner.
     */
    public static void main(String args[]) {
        final RegExp R = new RegExp(args[0], false);
        if (R.Valid) {
            if (R.match(args[1])) {
                System.out.println("Matched from " + R.StartMatch + " to "
                        + R.EndMatch);
                System.out.println(R.EN + " brackets assigned");
                for (int i = 0; i < R.EN; i++) {
                    System.out.println(i + ": " + R.expand("(" + i + ")"));
                }
            }
        } else {
            System.out.println(R.ErrorString + " at " + R.Pos);
        }
    }

    /**
     * store the regular expression string here
     */
    String S;
    /**
     * the regular expression scanned tree
     */
    Part Left;
    /**
     * the Valid flag
     */
    boolean Valid = false;
    /**
     * the error string, if Valid is false
     */
    String ErrorString;
    /**
     * the error position, if Valid is false
     */
    int Pos;
    /**
     * the minimal length a string must have to match
     */
    int minLength = 0;
    /**
     * the found match
     */
    int StartMatch, EndMatch;
    /**
     * a vector for the found expressions in brackets
     */
    Vector E;
    /**
     * A counter to use for the brackets
     */
    int EN;
    /**
     * Note the searched string here
     */
    char A[];

    /**
     * Ignore case
     */
    boolean IgnoreCase = false;

    /**
     * This scans a regular expression for further usage. The error flag may be
     * checked with the valid() function.
     *
     * @param s
     *            The regular expression.
     */
    public RegExp(String s, boolean ignorecase) {
        if (ignorecase) {
            s = s.toUpperCase();
        }
        this.S = s;
        this.E = new Vector();
        this.IgnoreCase = ignorecase;
        final char A[] = this.S.toCharArray();
        final Position p = new Position(A);
        this.Left = new Part(this, true);
        this.ErrorString = "";
        try {
            this.Left.scan(p);
            this.Valid = true;
        } catch (final RegExpException e) {
            this.Valid = false;
            this.ErrorString = e.string();
            this.Pos = e.pos();
        } catch (final Exception e) {
            this.Valid = false;
            this.ErrorString = "internal.error";
            this.Pos = 0;
        }
    }

    /**
     * @return end of the matching string.
     */
    public int endMatch() {
        return this.EndMatch;
    }

    /**
     * The position, where the scan error occured.
     *
     * @return the error position.
     */
    public int errorPos() {
        return this.Pos;
    }

    /**
     * The error string tries to explain the error, when valid() return false.
     *
     * @return the error string
     */
    public String errorString() {
        return this.ErrorString;
    }

    /**
     * Expand the replacement string and change (1), (2) etc. to the found
     * bracket expansions.
     *
     * @return expanded string or null on error.
     */
    public String expand(String s) {
        try {
            final StringBuffer B = new StringBuffer();
            s = s.replace("\\t", "\t");
            final StringTokenizer T = new StringTokenizer(s, "\\()", true);
            while (T.hasMoreTokens()) {
                String a = T.nextToken();
                if (a.equals("(")) {
                    final String b = T.nextToken();
                    final String c = T.nextToken();
                    if (!c.equals(")")) {
                        return null;
                    }
                    final PositionRange p = (PositionRange) this.E
                            .elementAt(Integer.parseInt(b));
                    B.append(new String(this.A, p.start(), p.end() - p.start()));
                } else if (a.equals("\\")) {
                    a = T.nextToken();
                    B.append(a);
                } else {
                    B.append(a);
                }
            }
            return B.toString();
        } catch (final Exception e) {
            return null;
        }
    }

    public String getBracket(int i) {
        try {
            final PositionRange r = (PositionRange) this.E.elementAt(i);
            return new String(this.A, r.start(), r.end() - r.start());
        } catch (final Exception e) {
            return null;
        }
    }

    public int getBracketNumber() {
        return this.E.size();
    }

    /**
     * Return an enumeration with the found brackets. The objects are instances
     * of the PositionRange class.
     *
     * @see rene.regexp.PositionRange
     */
    public Enumeration getBrackets() {
        return this.E.elements();
    }

    public boolean match(char a[], int pos) {
        this.A = a;
        final Position p = new Position(this.A);
        int i;
        final int n = this.A.length - this.minLength;
        for (i = pos; i <= n; i++) {
            p.pos(i);
            if (this.Left.match(p)) {
                this.StartMatch = i;
                this.EndMatch = p.pos();
                return true;
            }
        }
        return false;
    }

    /**
     * Match the regular expression against a string.
     *
     * @return true, if a match was found.
     */
    public boolean match(String s) {
        final char A[] = s.toCharArray();
        return this.match(A, 0);
    }

    /**
     * @return start position of matching string.
     */
    public int startMatch() {
        return this.StartMatch;
    }

    char uppercase(char c) {
        if (this.IgnoreCase) {
            return Character.toUpperCase(c);
        } else {
            return c;
        }
    }

    /**
     * Checks the error state for the regular expression.
     *
     * @return true, if there is no error.
     */
    public boolean valid() {
        return this.Valid;
    }
}

/**
 * An exception for the scanning of the regular expression.
 */

class RegExpException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int pos;
    String S;

    public RegExpException(String s) {
        super(s);
        this.pos = 0;
    }

    public RegExpException(String s, int p) {
        super(s);
        this.S = s;
        this.pos = p;
    }

    public int pos() {
        return this.pos;
    }

    public String string() {
        return this.S;
    }
}

/**
 * This is an atom, which is capable of finding the longest match and upon
 * request by nextMatch() shorter matches.
 */

class Simple extends Atom {
    public Simple(RegExp r) {
        super(r);
    }

    @Override
    public boolean match(Position p) {
        this.notePosition(p);
        if (!p.end() && this.matchSimple(p)) {
            p.advance();
            this.Match = true;
            if (this.canMultiple()) {
                while (!p.end() && this.matchSimple(p)) {
                    p.advance();
                }
            }
            this.MatchEnd = p.pos();
            return true;
        } else {
            if (this.canVoid()) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Override this to get useful matches.
     *
     * @return The singe character in the position matches this atom.
     */
    public boolean matchSimple(Position p) {
        return false;
    }

    @Override
    public boolean nextMatch() {
        if (!this.Match) {
            return false;
        }
        this.MatchEnd--;
        if (this.MatchEnd < this.LastMatch
                || (this.MatchEnd == this.LastMatch && !this.canVoid())) {
            this.Match = false;
            return false;
        }
        this.P.pos(this.MatchEnd);
        this.Match = true;
        return true;
    }
}

class SpaceRange extends RangeClass {
    public SpaceRange(boolean exclude) {
        super(exclude);
    }

    @Override
    public boolean inRange(char c) {
        return Character.isSpaceChar(c);
    }
}

class SpecialChar extends Char {
    public SpecialChar(RegExp r, char c) {
        super(r);
        this.C = c;
    }

    @Override
    public boolean scan(Position p) throws RegExpException {
        p.advance();
        this.scanMult(p);
        return true;
    }
}

class UpperRange extends RangeClass {
    public UpperRange(boolean exclude) {
        super(exclude);
    }

    @Override
    public boolean inRange(char c) {
        return Character.isUpperCase(c);
    }
}

class WhiteSpaceRange extends RangeClass {
    public WhiteSpaceRange(boolean exclude) {
        super(exclude);
    }

    @Override
    public boolean inRange(char c) {
        return Character.isWhitespace(c);
    }
}
