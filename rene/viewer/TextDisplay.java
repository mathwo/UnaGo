package rene.viewer;

import rene.gui.Global;
import rene.util.list.ListClass;
import rene.util.list.ListElement;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.PrintWriter;

class ClipboardCopy extends Thread {
    String S;
    ClipboardOwner C;
    Canvas Cv;

    public ClipboardCopy(ClipboardOwner c, Canvas cv, String s) {
        this.S = s;
        this.C = c;
        this.Cv = cv;
        this.start();
    }

    @Override
    public void run() {
        final Clipboard clip = this.Cv.getToolkit().getSystemClipboard();
        final StringSelection cont = new StringSelection(this.S);
        clip.setContents(cont, this.C);
    }
}

public class TextDisplay extends Canvas implements ClipboardOwner,
ComponentListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ListClass L;
    Font F = null;
    FontMetrics FM;
    Viewer V;
    int Leading, Height, Ascent, Descent;
    int LineCount, TopLineCount;
    int PageSize;
    ListElement TopLine;
    Image I;
    Graphics2D IG;
    int W, H;
    public int Tabsize = 4;
    public int Offset;
    boolean LineFinished = true;
    int Widths[], HW[];
    long LastScrollTime;
    Color Background;
    int TabWidth = 0;

    public TextDisplay(Viewer v) {
        this.L = new ListClass();
        this.F = null;
        this.V = v;
        this.LineCount = 0;
        this.TopLineCount = 0;
        this.TopLine = null;
        this.I = null;
        this.W = this.H = 0;
        this.PageSize = 10;
        this.HW = new int[1024];
        this.addKeyListener(v);
        this.addComponentListener(this);
    }

    /**
     * Set Anti-Aliasing on or off, if in Java 1.2 or better and the Parameter
     * "font.smooth" is switched on.
     *
     * @param flag
     */
    public void antialias(boolean flag) {
        if (Global.getParameter("font.smooth", true)) {
            this.IG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    flag ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                            : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
    }

    public void append(String S, Color c) {
        this.append(S, c, true);
    }

    public void append(String S, Color c, boolean suddenupdate) {
        while (true) {
            final int p = S.indexOf('\n');
            if (p < 0) {
                this.appendlast(S, c);
                this.LineFinished = false;
                break;
            }
            this.appendlast(S.substring(0, p), c);
            this.LineFinished = true;
            S = S.substring(p + 1);
            if (S.equals("")) {
                break;
            }
        }
        if (suddenupdate) {
            this.doUpdate(true);
        }
        this.repaint();
    }

    public synchronized void appendlast(String s, Color c) {
        if (this.LineFinished || this.L.last() == null) {
            Line l;
            this.L.append(new ListElement(l = new Line(s, this, c)));
            this.LineCount++;
            if (this.LineCount == 1) {
                this.TopLine = this.L.first();
            }
            if (this.TabWidth > 0) {
                l.expandTabs(this.TabWidth);
            }
        } else {
            ((Line) this.L.last().content()).append(s);
        }
    }

    public synchronized void appendLine(String s) {
        this.appendLine0(s);
        this.V.setVerticalScrollbar();
    }

    public synchronized void appendLine0(String S) {
        this.appendLine0(S, Color.black);
    }

    public synchronized void appendLine0(String S, Color c) {
        Line l;
        this.L.append(new ListElement(l = new Line(S, this, c)));
        this.LineCount++;
        if (this.LineCount == 1) {
            this.TopLine = this.L.first();
        }
        this.LineFinished = true;
        if (this.TabWidth > 0) {
            l.expandTabs(this.TabWidth);
        }
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        this.V.resized();
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    int computeVertical() {
        if (this.LineCount > 0) {
            return this.TopLineCount * 1000 / this.LineCount;
        } else {
            return 0;
        }
    }

    int computeVerticalSize() {
        if (this.LineCount == 0) {
            return 100;
        }
        int h = this.PageSize * 2000 / this.LineCount;
        if (h < 10) {
            h = 10;
        }
        return h;
    }

    void copy(TextPosition Start, TextPosition End) {
        if (Start == null || End == null) {
            return;
        }
        TextPosition P1, P2;
        if (Start.before(End)) {
            P1 = Start;
            P2 = End;
        } else if (End.before(Start)) {
            P1 = End;
            P2 = Start;
        } else {
            return;
        }
        String s = "";
        ListElement e = P1.L;
        while (e != null && e != P2.L) {
            s = s + ((Line) e.content()).getblock() + "\n";
            e = e.next();
        }
        if (e != null) {
            s = s + ((Line) e.content()).getblock();
        }
        new ClipboardCopy(this, this, s);
    }

    public void doUpdate(boolean showlast) {
        if (showlast) {
            final long m = System.currentTimeMillis();
            if (m - this.LastScrollTime > 10000) {
                this.showlast();
            }
        }
        this.repaint();
        this.V.setVerticalScrollbar();
    }

    @Override
    public Color getBackground() {
        if (Global.Background != null) {
            return Global.Background;
        } else {
            return SystemColor.window;
        }
    }

    public ListElement getline(int y) {
        if (this.TopLine == null) {
            return null;
        }
        ListElement e = this.TopLine;
        int h = this.Leading + this.Height;
        if (h == 0) {
            return null;
        }
        h = y / h;
        for (int i = 0; i < h; i++) {
            if (e.next() == null) {
                return e;
            }
            e = e.next();
        }
        return e;
    }

    public TextPosition getposition(int x, int y) {
        if (this.L.first() == null) {
            return null;
        }
        if (y < 0) {
            return new TextPosition(this.TopLine, this.TopLineCount, 0);
        }
        if (this.TopLine == null) {
            return null;
        }
        ListElement e = this.TopLine;
        int h = this.Leading + this.Height;
        if (h == 0) {
            return null;
        }
        h = y / h;
        int i;
        for (i = 0; i < h; i++) {
            if (e.next() == null || i == this.PageSize - 1) {
                return new TextPosition(e, this.TopLineCount + i,
                        ((Line) e.content()).length());
            }
            e = e.next();
        }
        return new TextPosition(e, this.TopLineCount + i,
                ((Line) e.content()).getpos(x, 2));
    }

    int[] getwidth(char a[]) {
        try {
            for (int i = 0; i < a.length; i++) {
                if (a[i] < 256) {
                    this.HW[i] = this.Widths[a[i]];
                } else {
                    this.HW[i] = this.FM.charWidth(a[i]);
                }
            }
        } catch (final Exception e) {
            return this.HW;
        }
        return this.HW;
    }

    @Override
    public boolean hasFocus() {
        return this.V.hasFocus();
    }

    void init(Font f) {
        this.F = f;
        this.FM = this.getFontMetrics(this.F);
        this.Leading = this.FM.getLeading()
                + Global.getParameter("fixedfont.spacing", -1);
        this.Height = this.FM.getHeight();
        this.Ascent = this.FM.getAscent();
        this.Descent = this.FM.getDescent();
        this.Widths = this.FM.getWidths();
        if (Global.Background != null) {
            this.Background = Global.Background;
        } else {
            this.Background = SystemColor.window;
        }
    }

    TextPosition lastpos() {
        final ListElement e = this.L.last();
        if (e == null) {
            return null;
        }
        final Line l = (Line) e.content();
        return new TextPosition(e, this.LineCount, l.length());
    }

    @Override
    public void lostOwnership(Clipboard clip, Transferable cont) {
    }

    public void makeimage() {
        final Dimension D = this.getSize();
        if (this.I == null || D.width != this.W || D.height != this.H) {
            this.I = this.createImage(this.W = D.width, this.H = D.height);
            this.IG = (Graphics2D) this.I.getGraphics();
            this.IG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);

        }
        this.IG.setColor(Color.black);
        this.IG.clearRect(0, 0, this.W, this.H);
        this.IG.setFont(this.F);
        try {
            this.PageSize = this.H / (this.Height + this.Leading);
        } catch (final Exception e) {
        }
    }

    public void mark(TextPosition Start, TextPosition End) {
        if (Start == null || End == null) {
            return;
        }
        TextPosition P1, P2;
        if (Start.before(End)) {
            P1 = Start;
            P2 = End;
        } else if (End.before(Start)) {
            P1 = End;
            P2 = Start;
        } else {
            return;
        }
        ListElement e = P1.L;
        ((Line) e.content()).block(P1.LPos, Line.START);
        if (e != P2.L) {
            e = e.next();
        }
        while (e != null && e != P2.L) {
            ((Line) e.content()).block(0, Line.FULL);
            e = e.next();
        }
        if (e != null) {
            ((Line) e.content()).block(P2.LPos, Line.END);
        }
        this.repaint();
        this.requestFocus();
    }

    @Override
    public synchronized void paint(Graphics g) {
        if (this.F == null) {
            this.init(this.getFont());
        }
        this.makeimage();
        ListElement e = this.TopLine;
        this.antialias(true);
        int h = this.Leading + this.Ascent;
        this.getSize();
        if (this.Background == null) {
            this.Background = this.getBackground();
        }
        this.IG.setColor(this.Background);
        this.IG.fillRect(0, 0, this.W, this.H);
        int lines = 0;
        while (lines < this.PageSize && e != null) {
            final Line l = (Line) e.content();
            l.draw(this.IG, 2, h);
            h += this.Leading + this.Height;
            e = e.next();
            lines++;
        }
        g.drawImage(this.I, 0, 0, this);
    }

    public void save(PrintWriter fo) {
        ListElement e = this.L.first();
        while (e != null) {
            fo.println(new String(((Line) e.content()).a));
            e = e.next();
        }
    }

    @Override
    public void setBackground(Color c) {
        this.Background = c;
        super.setBackground(c);
    }

    public int setHorizontal(int v) {
        this.Offset = v / 5;
        this.repaint();
        return v;
    }

    public void setTabWidth(int t) {
        this.TabWidth = t;
    }

    public void setText(String s) {
        this.TopLine = null;
        this.TopLineCount = 0;
        this.LineCount = 0;
        this.L = new ListClass();
        if (!s.equals("")) {
            this.append(s, Color.black);
        }
        this.repaint();
    }

    public int setVertical(int v) {
        if (this.TopLine == null) {
            return 0;
        }
        final int NewTop = this.LineCount * v / 1000;
        if (NewTop > this.TopLineCount) {
            for (int i = this.TopLineCount; i < NewTop; i++) {
                if (this.TopLine.next() == null) {
                    break;
                }
                this.TopLine = this.TopLine.next();
                this.TopLineCount++;
            }
            this.repaint();
        } else if (NewTop < this.TopLineCount) {
            for (int i = this.TopLineCount; i > NewTop; i--) {
                if (this.TopLine.previous() == null) {
                    break;
                }
                this.TopLine = this.TopLine.previous();
                this.TopLineCount--;
            }
            this.repaint();
        }
        this.LastScrollTime = System.currentTimeMillis();
        return v;
    }

    public void showFirst() {
        this.TopLine = this.L.first();
    }

    public void showlast() {
        ListElement e = this.L.last();
        if (e == null) {
            return;
        }
        this.TopLineCount = this.LineCount;
        for (int i = 0; i < this.PageSize - 1; i++) {
            if (e.previous() == null) {
                break;
            }
            e = e.previous();
            this.TopLineCount--;
        }
        this.TopLine = e;
        this.repaint();
    }

    public void showLine(ListElement line) {
        ListElement e = this.TopLine;
        this.getSize();
        if (this.Background == null) {
            this.Background = this.getBackground();
        }
        int lines = 0;
        while (lines < this.PageSize && e != null) {
            if (e == line) {
                return;
            }
            e = e.next();
            lines++;
        }
        if (e == line && this.TopLine.next() != null) {
            this.TopLine = this.TopLine.next();
        } else {
            this.TopLine = line;
        }
    }

    public void unmark() {
        ListElement e = this.L.first();
        while (e != null) {
            ((Line) e.content()).block(0, Line.NONE);
            e = e.next();
        }
        this.repaint();
    }

    public void unmark(TextPosition Start, TextPosition End) {
        if (Start == null || End == null) {
            return;
        }
        TextPosition P1, P2;
        if (Start.before(End)) {
            P1 = Start;
            P2 = End;
        } else if (End.before(Start)) {
            P1 = End;
            P2 = Start;
        } else {
            return;
        }
        ListElement e = P1.L;
        while (e != null && e != P2.L) {
            ((Line) e.content()).block(0, Line.NONE);
            e = e.next();
        }
        if (e != null) {
            ((Line) e.content()).block(0, Line.NONE);
        }
        this.repaint();
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }

    public void verticalDown() {
        if (this.TopLine == null) {
            return;
        }
        if (this.TopLine.previous() == null) {
            return;
        }
        this.TopLine = this.TopLine.previous();
        this.TopLineCount--;
        this.repaint();
        this.LastScrollTime = System.currentTimeMillis();
    }

    public void verticalPageDown() {
        if (this.TopLine == null) {
            return;
        }
        for (int i = 0; i < this.PageSize - 1; i++) {
            if (this.TopLine.previous() == null) {
                break;
            }
            this.TopLine = this.TopLine.previous();
            this.TopLineCount--;
        }
        this.repaint();
        this.LastScrollTime = System.currentTimeMillis();
    }

    public void verticalPageUp() {
        if (this.TopLine == null) {
            return;
        }
        for (int i = 0; i < this.PageSize - 1; i++) {
            if (this.TopLine.next() == null) {
                break;
            }
            this.TopLine = this.TopLine.next();
            this.TopLineCount++;
        }
        this.repaint();
        this.LastScrollTime = System.currentTimeMillis();
    }

    public void verticalUp() {
        if (this.TopLine == null) {
            return;
        }
        if (this.TopLine.next() == null) {
            return;
        }
        this.TopLine = this.TopLine.next();
        this.TopLineCount++;
        this.repaint();
        this.LastScrollTime = System.currentTimeMillis();
    }
}
