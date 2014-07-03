package rene.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Rene This is the most basic icon, handling mouse presses and display
 *         in activated, pressed, unset or disabled state.
 */
class BasicIcon extends Panel implements MouseListener, IconBarElement,
Runnable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    IconBar Bar;
    String Name;
    boolean Enabled; // Icon cannot be
                     // changed by user
                     // action.
    boolean On; // On or off are the
                // basic stated of
                // icons.
    boolean Focus = false;
    public static int Size = 22; // the size of icons
    boolean MouseOver, MouseDown; // for display states
                                  // during mouse
    // action
    boolean Unset; // Unknown State!

    Thread T;

    boolean Control;

    public BasicIcon(IconBar bar, String name) {
        this.Bar = bar;
        this.Name = name;
        this.Enabled = true;
        this.On = false;
        this.addMouseListener(this);
        this.enableEvents(AWTEvent.KEY_EVENT_MASK);
        this.setSize(BasicIcon.Size, BasicIcon.Size);
    }

    public void dopaint(Graphics g) {
    }

    @Override
    public String getName() {
        return this.Name;
    }

    @Override
    public Point getPosition() {
        return this.getLocationOnScreen();
    }

    @Override
    public boolean hasFocus() {
        return this.Focus;
    }

    public boolean isSet() {
        return !this.Unset;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Start a thread, that waits for one second, then tells the icon bar to
     * display the proper help text.
     */
    @Override
    public synchronized void mouseEntered(MouseEvent e) {
        if (this.T != null) {
            return;
        }
        if (this.Enabled) {
            this.MouseOver = true;
        }
        this.repaint();
        if (!Global.getParameter("iconbar.showtips", true)) {
            return;
        }
        this.Control = e.isControlDown();
        this.T = new Thread(this);
        this.T.start();
    }

    /**
     * Tell the run method, that display is no longer necessary, and remove the
     * help text.
     */
    @Override
    public synchronized void mouseExited(MouseEvent e) {
        if (this.T == null) {
            return;
        }
        this.T.interrupt();
        this.T = null;
        this.MouseOver = false;
        this.repaint();
        this.Bar.removeHelp();
    }

    /**
     * User pressed the mouse key over this button.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (!this.Enabled) {
            return;
        }
        this.MouseDown = true;
        this.repaint();
    }

    /**
     * User released the mouse key again.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (!this.Enabled) {
            return;
        }
        this.MouseDown = false;
        final Dimension d = this.getSize();
        if (e.getX() < 0 || e.getX() > d.width || e.getY() < 0
                || e.getY() > d.height) {
            this.repaint();
            return;
        }
        this.Unset = false;
        this.pressed(e); // call method for children to change states etc.
        this.repaint();
        this.T = null; // stop icon help thread
        // Notify Iconbar about activation:
        long time = System.currentTimeMillis();
        this.Bar.iconPressed(this.Name, e.isShiftDown(), e.isControlDown());
        // Necessary, since Java 1.4 does not report
        // MouseExited, if a modal dialog is active:
        time = System.currentTimeMillis() - time;
        if (this.MouseOver && time > 1000) {
            this.MouseOver = false;
            this.repaint();
        }
    }

    /**
     * Paint a button with an image
     */
    @Override
    public void paint(Graphics g) {
        if (this.MouseDown) {
            g.setColor(this.getBackground());
            g.fill3DRect(0, 0, BasicIcon.Size, BasicIcon.Size, false);
        } else {
            if (this.MouseOver) {
                if (this.On) {
                    final Color c = this.getBackground();
                    g.setColor(new SaveColor(c.getRed() - 30,
                            c.getGreen() - 30, c.getBlue()));
                } else {
                    g.setColor(this.getBackground());
                }
                g.fill3DRect(0, 0, BasicIcon.Size, BasicIcon.Size, true);
            } else {
                if (this.On) {
                    final Color c = this.getBackground();
                    g.setColor(c);
                    g.fillRect(0, 0, BasicIcon.Size, BasicIcon.Size);
                    g.setColor(new SaveColor(c.getRed() - 100,
                            c.getGreen() - 100, c.getBlue()));
                    g.fillRect(3, 3, BasicIcon.Size - 2, BasicIcon.Size - 2);
                    g.setColor(new SaveColor(c.getRed() - 50,
                            c.getGreen() - 50, c.getBlue()));
                    g.fillRect(1, 1, BasicIcon.Size - 2, BasicIcon.Size - 2);
                } else {
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, BasicIcon.Size, BasicIcon.Size);
                }
            }
        }
        this.dopaint(g);
        if (this.Unset) {
            final Color c = this.getBackground();
            g.setColor(new SaveColor(c.getRed() - 100, c.getGreen(), c
                    .getBlue()));
            g.drawLine(0, 0, BasicIcon.Size, BasicIcon.Size);
        }
        if (this.Focus) {
            this.showFocus(g);
        }
    }

    /**
     * Overwrite for children!
     *
     * @param e
     *            Mouse event for determining right button etc.
     */
    public void pressed(MouseEvent e) {
    }

    // for the IconBarElement interface

    @Override
    public void processKeyEvent(KeyEvent e) {
        this.Bar.getKey(e);
    }

    /**
     * A thread to display an icon help.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (final Exception e) {
        }
        if ((this.T != null) && (!this.T.isInterrupted())) {
            synchronized (this) {
                try {
                    this.getLocationOnScreen();
                    String help = Global.name("iconhelp." + this.Name, "");
                    if (help.equals("") && this.Name.length() > 1) {
                        help = Global.name(
                                "iconhelp."
                                        + this.Name.substring(0,
                                                this.Name.length() - 1) + "?",
                                "");
                    }
                    if (help.equals("")) {
                        help = this.Bar.getHelp(this.Name);
                    }
                    if (help.equals("")) {
                        help = Global.name("iconhelp.nohelp",
                                "No help available");
                    }
                    if (this.Control) {
                        final String hc = Global.name("iconhelp.control."
                                + this.Name, "");
                        if (!hc.equals("")) {
                            help = hc;
                        }
                    }
                    this.Bar.displayHelp(this, help);
                } catch (final Exception e) {
                }
            }
            try {
                Thread.sleep(5000);
            } catch (final Exception e) {
            }
            if (!this.T.isInterrupted()) {
                this.Bar.removeHelp();
            }
            this.T = null;
        }
    }

    @Override
    public void setEnabled(boolean flag) {
        if (this.Enabled == flag) {
            return;
        }
        this.Enabled = flag;
        this.repaint();
    }

    public void setFocus(boolean flag) {
        this.Focus = flag;
        this.repaint();
    }

    public void setOn(boolean flag) {
        this.On = flag;
        this.repaint();
    }

    @Override
    public void setPosition(int x, int y) {
        this.setLocation(x, y);
    }

    public void showFocus(Graphics g) {
        g.setColor(Color.white);
        g.drawRect(4, 4, 1, 1);
        g.drawRect(BasicIcon.Size - 5, 4, 1, 1);
        g.drawRect(4, BasicIcon.Size - 5, 1, 1);
        g.drawRect(BasicIcon.Size - 5, BasicIcon.Size - 5, 1, 1);
    }

    // needs to be removed:

    public void unset() {
        this.unset(true);
        this.repaint();
    }

    public void unset(boolean flag) {
        this.Unset = flag;
        this.repaint();
    }

    @Override
    public void update(Graphics g) {
        this.paint(g);
    }

    @Override
    public int width() {
        return BasicIcon.Size;
    }
}

/**
 * One icon, which can display one color
 *
 * @author Rene Grothmann
 */
class ColoredIcon extends BasicIcon {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Color C;

    public ColoredIcon(IconBar bar, String name, Color c) {
        super(bar, name);
        this.C = c;
    }

    @Override
    public void dopaint(Graphics g) {
        g.setColor(this.C);
        g.fill3DRect(5, 5, BasicIcon.Size - 9, BasicIcon.Size - 9, true);
    }

    public Color getColor() {
        return this.C;
    }

    public void setColor(Color c) {
        this.C = c;
    }
}

/**
 * @author Rene A toggle icon for several colors.
 */
class ColorIcon extends MultipleIcon {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Color Colors[];

    public ColorIcon(IconBar bar, String name, Color colors[]) {
        super(bar, name);
        this.N = colors.length;
        this.Colors = colors;
    }

    @Override
    public void dopaint(Graphics g) {
        g.setColor(this.Colors[this.Selected]);
        g.fill3DRect(5, 5, BasicIcon.Size - 9, BasicIcon.Size - 9, true);
    }
}

/**
 * This panel displays icons and reacts on mouse actions. It can also interpret
 * key strokes to traverse the icons.
 */

public class IconBar extends Panel implements KeyListener, FocusListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
        final CloseFrame f = new CloseFrame("Test");
        final IconBar IA = new IconBar(f);
        IA.Vertical = true;
        IA.setSize(30);
        IA.Resource = "/icons/";
        IA.addLeft("back");
        IA.addLeft("undo");
        IA.addSeparatorLeft();
        IA.addOnOffLeft("grid");
        IA.addSeparatorLeft();
        IA.addToggleLeft("delete");
        IA.addSeparatorLeft();
        final String tg[] = { "zoom", "draw", "", "rename", "edit" };
        IA.addToggleGroupLeft(tg);
        IA.addSeparatorLeft();
        IA.addMultipleToggleIconLeft("macro", 3);
        IA.addSeparatorLeft();
        final String tga[] = { "zoom", "draw", "rename", "edit" };
        IA.addLeft(new PopupIcon(IA, tga));
        IA.addSeparatorLeft();
        final String st[] = { "A", "B", "C", "D" };
        IA.addMultipleStringIconLeft("strings", st);
        final Color col[] = { Color.BLACK, Color.RED, Color.GREEN };
        IA.addStateLeft("needsave");
        IA.addColorIconLeft("color", col);
        f.add("Center", new IconBarPanel(new Panel3D(IA), new Panel3D(
                new Panel())));
        f.pack();
        f.center();
        f.setVisible(true);
    }

    Vector Left = new Vector(), Right = new Vector();
    int W;
    Window F;
    public final int Offset = 2;
    public String Resource = "/";
    int Focus = 0;
    public boolean TraverseFocus = true;
    public boolean UseSize = true;

    public boolean Vertical = false;

    boolean Overflow = false, Shifted = false;

    OverflowButton OB;

    int OverflowX;

    IconBarListener L = null;

    boolean Shift, Control;

    /*
     * public boolean isFocusTraversable () { return TraverseFocus; }
     */

    Window WHelp = null;

    public IconBar(Window f) {
        this(f, true);
    }

    public IconBar(Window f, boolean traversefocus) {
        this.F = f;
        this.TraverseFocus = traversefocus;
        if (Global.ControlBackground != null) {
            this.setBackground(Global.ControlBackground);
        } else {
            this.setBackground(SystemColor.menu);
        }
        this.Resource = Global.getParameter("iconpath", "");
        BasicIcon.Size = Global.getParameter("iconsize", 20);
        this.setLayout(null);
        this.W = this.Offset * 2;
        this.addKeyListener(this);
        if (this.TraverseFocus) {
            this.addFocusListener(this);
        }
    }

    /**
     * Add a colored icon
     */
    public void addColoredIconLeft(String name, Color c) {
        this.addLeft(new ColoredIcon(this, name, c));
    }

    public void addColoredIconRight(String name, Color c) {
        this.addRight(new ColoredIcon(this, name, c));
    }

    /**
     * Add a multiple icon (can toggle between the colors)
     */
    public void addColorIconLeft(String name, Color colors[]) {
        this.addLeft(new ColorIcon(this, name, colors));
    }

    public void addColorIconRight(String name, Color colors[]) {
        this.addRight(new ColorIcon(this, name, colors));
    }

    public void addLeft(BasicIcon i) {
        this.Left.addElement(i);
        this.add(i);
        this.W += i.width() + this.Offset;
    }

    /**
     * Add an icon
     */
    public void addLeft(String name) {
        this.addLeft(new IconWithGif(this, name));
    }

    /**
     * Add a multiple icon (can toggle between the icons)
     */
    public void addMultipleIconLeft(String name, int number) {
        this.addLeft(new MultipleIcon(this, name, number));
    }

    public void addMultipleIconRight(String name, int number) {
        this.addRight(new MultipleIcon(this, name, number));
    }

    /**
     * Add a multiple icon (can toggle between the icons)
     */
    public void addMultipleStringIconLeft(String name, String s[]) {
        this.addLeft(new MultipleStringIcon(this, name, s));
    }

    public void addMultipleStringIconRight(String name, String s[]) {
        this.addRight(new MultipleStringIcon(this, name, s));
    }

    /**
     * Add a multiple icon (can toggle between the icons)
     */
    public void addMultipleToggleIconLeft(String name, int number) {
        this.addLeft(new MultipleToggleIcon(this, name, number));
    }

    public void addMultipleToggleIconRight(String name, int number) {
        this.addRight(new MultipleToggleIcon(this, name, number));
    }

    /**
     * Add a toggle icon
     */
    public void addOnOffLeft(String name) {
        this.addLeft(new OnOffIcon(this, name));
    }

    public void addOnOffRight(String name) {
        this.addRight(new OnOffIcon(this, name));
    }

    public void addRight(BasicIcon i) {
        this.Right.addElement(i);
        this.add(i);
        this.W += i.width() + this.Offset;
    }

    /**
     * Add an icon at the right end
     */
    public void addRight(String name) {
        this.addRight(new IconWithGif(this, name));
    }

    /**
     * Add a separator
     */
    public void addSeparatorLeft() {
        if (this.Left.size() == 0) {
            return;
        }
        if (this.Left.lastElement() instanceof Separator) {
            return;
        }
        final Separator s = new Separator(this);
        this.Left.addElement(s);
        this.add(s);
        this.W += s.width() + this.Offset;
    }

    public void addSeparatorRight() {
        if (this.Right.size() == 0) {
            return;
        }
        if (this.Right.lastElement() instanceof Separator) {
            return;
        }
        final Separator s = new Separator(this);
        this.Right.addElement(s);
        this.add(s);
        this.W += s.width() + this.Offset;
    }

    /**
     * Add a state display at the left end.
     */
    public void addStateLeft(String name) {
        this.addLeft(new StateDisplay(this, name));
    }

    public void addStateRight(String name) {
        this.addRight(new StateDisplay(this, name));
    }

    public void addToggleGroupLeft(String names[]) {
        this.addToggleGroupLeft(names, names);
    }

    public void addToggleGroupLeft(String name, Color colors[]) {
        final IconGroup g = new IconGroup(this, name, colors);
        g.addLeft();
    }

    public void addToggleGroupLeft(String name, int n) {
        final IconGroup g = new IconGroup(this, name, n);
        g.addLeft();
    }

    /**
     * Add a complete groupe of toggle items.
     */
    public void addToggleGroupLeft(String names[], String breaks[]) {
        final IconGroup g = new IconGroup(this, names, breaks);
        g.addLeft();
    }

    public void addToggleGroupRight(String names[]) {
        this.addToggleGroupRight(names, names);
    }

    public void addToggleGroupRight(String name, Color colors[]) {
        final IconGroup g = new IconGroup(this, name, colors);
        g.addRight();
    }

    public void addToggleGroupRight(String name, int n) {
        final IconGroup g = new IconGroup(this, name, n);
        g.addRight();
    }

    public void addToggleGroupRight(String names[], String breaks[]) {
        final IconGroup g = new IconGroup(this, names, breaks);
        g.addRight();
    }

    /**
     * Add a toggle icon
     */
    public void addToggleLeft(String name) {
        this.addLeft(new ToggleIcon(this, name));
    }

    public void addToggleRight(String name) {
        this.addRight(new ToggleIcon(this, name));
    }

    public void clearShiftControl() {
        this.Shift = this.Control = false;
    }

    public synchronized void displayHelp(IconBarElement i, String text) {
        if (this.F == null || this.WHelp != null) {
            return;
        }
        final Point P = i.getPosition();
        this.WHelp = new Window(this.F);
        final Panel p = new Panel();
        final StringTokenizer t = new StringTokenizer(text, "+");
        p.setLayout(new GridLayout(0, 1));
        while (t.hasMoreTokens()) {
            p.add(new MyLabel(t.nextToken()));
        }
        this.WHelp.add("Center", p);
        this.WHelp.pack();
        final Dimension d = this.WHelp.getSize();
        final Dimension ds = this.getToolkit().getScreenSize();
        int x = P.x, y = P.y + i.width() + 10;
        if (x + d.width > ds.width) {
            x = ds.width - d.width;
        }
        if (y + d.height > ds.height) {
            y = P.y - i.width() - d.height;
        }
        this.WHelp.setLocation(x, y);
        this.WHelp.setBackground(new Color(255, 255, 220));
        this.WHelp.setForeground(Color.black);
        this.WHelp.setVisible(true);
    }

    /**
     * Override the layout and arrange the icons from the left and the right.
     */
    @Override
    public void doLayout() {
        if (this.OB != null) {
            this.remove(this.OB);
            this.OB = null;
        }
        if (this.Vertical) {
            int x;
            x = this.getSize().height;
            for (int k = 0; k < this.Right.size(); k++) {
                final IconBarElement i = (IconBarElement) this.Right
                        .elementAt(k);
                x -= i.width();
                i.setPosition(2, x);
                x -= this.Offset;
            }
            final int xmax = x;
            x = 0;
            for (int k = 0; k < this.Left.size(); k++) {
                final IconBarElement i = (IconBarElement) this.Left
                        .elementAt(k);
                i.setPosition(2, x);
                x += i.width();
                x += this.Offset;
                if (x + BasicIcon.Size > xmax) {
                    x = -1000;
                }
            }
        } else {
            int x;
            x = this.getSize().width;
            for (int k = 0; k < this.Right.size(); k++) {
                final IconBarElement i = (IconBarElement) this.Right
                        .elementAt(k);
                x -= i.width();
                i.setPosition(x, 2);
                x -= this.Offset;
            }
            final int xmax = x;
            x = 0;
            for (int k = 0; k < this.Left.size(); k++) {
                IconBarElement i = (IconBarElement) this.Left.elementAt(k);
                i.setPosition(x, 2);
                x += i.width();
                x += this.Offset;
                if (x + BasicIcon.Size > xmax - 10 && k < this.Left.size() - 1) {
                    this.Overflow = true;
                    this.OverflowX = x;
                    this.OB = new OverflowButton(this, this.Shifted);
                    this.add(this.OB);
                    this.OB.setSize(10, BasicIcon.Size);
                    this.OB.setLocation(xmax - 10 - this.Offset, 2);
                    if (!this.Shifted) {
                        x = -1000;
                    } else {
                        x = xmax - 10 - 2 * this.Offset;
                        for (int l = this.Left.size() - 1; l >= 0; l--) {
                            i = (IconBarElement) this.Left.elementAt(l);
                            x -= i.width();
                            i.setPosition(x, 2);
                            x -= this.Offset;
                            if (x - BasicIcon.Size < 0) {
                                x -= 1000;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private BasicIcon find(String name) {
        int k;
        for (k = 0; k < this.Left.size(); k++) {
            try {
                final BasicIcon i = (BasicIcon) this.Left.elementAt(k);
                if (i.getName().equals(name)) {
                    return i;
                }
            } catch (final Exception e) {
            }
        }
        for (k = 0; k < this.Right.size(); k++) {
            try {
                final BasicIcon i = (BasicIcon) this.Right.elementAt(k);
                if (i.getName().equals(name)) {
                    return i;
                }
            } catch (final Exception e) {
            }
        }
        return null;
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.TraverseFocus) {
            this.setFocus(this.Focus, true);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.TraverseFocus) {
            this.setFocus(this.Focus, false);
        }
    }

    /**
     * Do not know, if this is necessary. But sometimes the icons do not repaint
     * after an update.
     */
    public void forceRepaint() {
        super.repaint();
        Enumeration e = this.Left.elements();
        while (e.hasMoreElements()) {
            final BasicIcon i = (BasicIcon) e.nextElement();
            i.repaint();
        }
        e = this.Right.elements();
        while (e.hasMoreElements()) {
            final BasicIcon i = (BasicIcon) e.nextElement();
            i.repaint();
        }
    }

    public Color getColoredIcon(String name) {
        final BasicIcon icon = this.find(name);
        if (icon == null || !(icon instanceof ColoredIcon)) {
            return Color.black;
        }
        return ((ColoredIcon) icon).getColor();
    }

    // The IconBar can notify one IconBarListener on icon
    // clicks.

    /**
     * Overwrite in children!
     *
     * @param name
     * @return Help text
     */
    public String getHelp(String name) {
        return "";
    }

    public Object getIcon(int n) {
        if (n < this.Left.size()) {
            return this.Left.elementAt(n);
        } else {
            return this.Right.elementAt(this.Right.size() - 1
                    - (n - this.Left.size()));
        }
    }

    public void getKey(KeyEvent e) {
        this.processKeyEvent(e);
    }

    @Override
    public Dimension getMinimumSize() {
        return this.getPreferredSize();
    }

    /**
     * Get the state of the specified multiple icon
     */
    public int getMultipleState(String name) {
        int k;
        for (k = 0; k < this.Left.size(); k++) {
            final IconBarElement i = (IconBarElement) this.Left.elementAt(k);
            if (i.getName().equals(name) && i instanceof MultipleIcon) {
                return ((MultipleIcon) i).getSelected();
            }
        }
        for (k = 0; k < this.Right.size(); k++) {
            final IconBarElement i = (IconBarElement) this.Right.elementAt(k);
            if (i.getName().equals(name) && i instanceof MultipleIcon) {
                return ((MultipleIcon) i).getSelected();
            }
        }
        return -1;
    }

    /**
     * Override the preferred sizes.
     */
    @Override
    public Dimension getPreferredSize() {
        if (this.Vertical) {
            if (!this.UseSize) {
                return new Dimension(BasicIcon.Size + 4, 10);
            }
            return new Dimension(BasicIcon.Size + 4, this.W + 10);
        } else {
            if (!this.UseSize) {
                return new Dimension(10, BasicIcon.Size + 4);
            }
            return new Dimension(this.W + 10, BasicIcon.Size + 4);
        }
    }

    /**
     * Get the state of the specified toggle icon
     */
    public boolean getState(String name) {
        final BasicIcon icon = this.find(name);
        if (icon == null || !(icon instanceof ToggleIcon)) {
            return false;
        }
        return ((ToggleIcon) icon).getState();
    }

    /**
     * Return the state of a toggle icon.
     */
    public int getToggleState(String name) {
        final BasicIcon icon = this.find(name + 0);
        if (icon == null || !(icon instanceof ToggleIcon)) {
            return -1;
        }
        final int n = ((ToggleIcon) icon).countPeers();
        for (int i = 0; i < n; i++) {
            if (this.getState(name + i)) {
                return i;
            }
        }
        return -1;
    }

    // The tool tip help, initiated by the icons.

    /**
     * Have an Icon?
     */
    public boolean have(String name) {
        return this.find(name) != null;
    }

    public void iconPressed(String name, boolean shift, boolean control) {
        this.Shift = shift;
        this.Control = control;
        this.removeHelp();
        if (this.L != null) {
            this.L.iconPressed(name);
        }
    }

    public boolean isControlPressed() {
        return this.Control;
    }

    /**
     * See, if the specific icon has been set.
     */
    public boolean isSet(String name) {
        final BasicIcon icon = this.find(name);
        if (icon == null) {
            return false;
        }
        return icon.isSet();
    }

    public boolean isShiftPressed() {
        return this.Shift;
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                this.setFocus(this.Focus, false);
                this.Focus++;
                if (this.Focus >= this.Left.size() + this.Right.size()) {
                    this.Focus = 0;
                }
                while (!(this.getIcon(this.Focus) instanceof BasicIcon)) {
                    this.Focus++;
                    if (this.Focus >= this.Left.size() + this.Right.size()) {
                        this.Focus = 0;
                        break;
                    }
                }
                this.setFocus(this.Focus, true);
                break;
            case KeyEvent.VK_LEFT:
                this.setFocus(this.Focus, false);
                this.Focus--;
                if (this.Focus < 0) {
                    this.Focus = this.Left.size() + this.Right.size() - 1;
                }
                while (!(this.getIcon(this.Focus) instanceof BasicIcon)) {
                    this.Focus--;
                    if (this.Focus < 0) {
                        this.Focus = this.Left.size() + this.Right.size() - 1;
                        break;
                    }
                }
                this.setFocus(this.Focus, true);
                break;
            case KeyEvent.VK_SPACE:
                try {
                    final BasicIcon icon = (BasicIcon) this.getIcon(this.Focus);
                    icon.mouseReleased(new MouseEvent(this,
                            MouseEvent.MOUSE_RELEASED, 0, 0, 0, 0, 1, false));
                } catch (final Exception ex) {
                }
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void removeAll() {
        Enumeration e = this.Left.elements();
        while (e.hasMoreElements()) {
            this.remove((BasicIcon) e.nextElement());
        }
        e = this.Right.elements();
        while (e.hasMoreElements()) {
            this.remove((BasicIcon) e.nextElement());
        }
        this.Left.removeAllElements();
        this.Right.removeAllElements();
    }

    public synchronized void removeHelp() {
        if (this.WHelp == null) {
            return;
        }
        this.WHelp.setVisible(false);
        this.WHelp.dispose();
        this.WHelp = null;
    }

    public void removeIconBarListener(IconBarListener l) {
        this.L = null;
    }

    /**
     * Set the state of an icon
     */
    public void set(String name, boolean flag) {
        final BasicIcon icon = this.find(name);
        if (icon == null) {
            return;
        }
        icon.setOn(flag);
    }

    public void setColoredIcon(String name, Color c) {
        final BasicIcon icon = this.find(name);
        if (icon == null || !(icon instanceof ColoredIcon)) {
            return;
        }
        ((ColoredIcon) icon).setColor(c);
    }

    /**
     * Enable the tool with the specified name.
     */
    public void setEnabled(String name, boolean flag) {
        final BasicIcon icon = this.find(name);
        if (icon == null) {
            return;
        }
        icon.setEnabled(flag);
    }

    public void setFocus(int n, boolean flag) {
        if (!this.TraverseFocus) {
            return;
        }
        try {
            if (n < this.Left.size()) {
                final BasicIcon icon = (BasicIcon) this.Left.elementAt(n);
                icon.setFocus(flag);
            } else {
                final BasicIcon icon = (BasicIcon) this.Right
                        .elementAt(this.Right.size() - 1
                                - (n - this.Left.size()));
                icon.setFocus(flag);
            }
        } catch (final Exception e) {
        }
    }

    public void setIconBarListener(IconBarListener l) {
        this.L = l;
    }

    /**
     * Set the state of the specified multiple icon
     */
    public void setMultipleState(String name, int state) {
        int k;
        for (k = 0; k < this.Left.size(); k++) {
            final IconBarElement i = (IconBarElement) this.Left.elementAt(k);
            if (i.getName().equals(name) && i instanceof MultipleIcon) {
                ((MultipleIcon) i).setSelected(state);
            }
        }
        for (k = 0; k < this.Right.size(); k++) {
            final IconBarElement i = (IconBarElement) this.Right.elementAt(k);
            if (i.getName().equals(name) && i instanceof MultipleIcon) {
                ((MultipleIcon) i).setSelected(state);
            }
        }
    }

    public void setShifted(boolean flag) {
        this.Shifted = flag;
        this.doLayout();
    }

    public void setSize(int size) {
        BasicIcon.Size = size;
    }

    /**
     * Set the state of a single toggle icon.
     */
    public void setState(String name, boolean flag) {
        final BasicIcon icon = this.find(name);
        if (icon != null && icon instanceof ToggleIcon) {
            ((ToggleIcon) icon).setState(flag);
        }
        if (icon != null && icon instanceof MultipleToggleIcon) {
            ((MultipleToggleIcon) icon).setState(flag);
        }
    }

    /**
     * Select
     */
    public void toggle(String name) {
        final BasicIcon icon = this.find(name);
        if (icon == null) {
            return;
        }
        if (icon instanceof ToggleIcon) {
            ((ToggleIcon) icon).setState(true);
        }
    }

    /**
     * Toggle an item of an item group (known by name and number).
     */
    public void toggle(String name, int n) {
        this.toggle(name + n);
    }

    /**
     * Deselect all icons in the group of an icon
     */
    public void unselect(String name) {
        final BasicIcon icon = this.find(name);
        if (icon == null) {
            return;
        }
        if (icon instanceof ToggleIcon) {
            ((ToggleIcon) icon).unselect();
        }
    }

    /**
     * Set the specific icon to unset.
     */
    public void unset(String name) {
        final BasicIcon icon = this.find(name);
        if (icon != null) {
            icon.unset();
        }
    }

}

/**
 * These are the common things to Separators and Incons.
 */

interface IconBarElement {
    public String getName();

    public Point getPosition();

    public void setEnabled(boolean flag);

    public void setPosition(int x, int y);

    public int width();
}

/**
 * This class can add several ToggleItems and will enable only one of them.
 */

class IconGroup {
    String Files[], Breaks[];
    IconBar Bar;
    int N;
    ToggleIcon Icons[];

    public IconGroup(IconBar bar, String files[]) {
        this(bar, files, files);
    }

    public IconGroup(IconBar bar, String name, Color colors[]) {
        this.N = colors.length;
        this.Breaks = this.Files = new String[this.N];
        for (int i = 0; i < this.N; i++) {
            this.Files[i] = name + i;
        }
        this.Bar = bar;
        this.Icons = new ToggleIcon[this.N];
        for (int i = 0; i < this.N; i++) {
            this.Icons[i] = new ToggleIcon(this.Bar, this.Files[i], colors[i],
                    this);
        }
    }

    public IconGroup(IconBar bar, String name, int n) {
        this.Breaks = this.Files = new String[n];
        for (int i = 0; i < n; i++) {
            this.Files[i] = name + i;
        }
        this.Bar = bar;
        this.init();
    }

    public IconGroup(IconBar bar, String files[], String breaks[]) {
        this.Files = files;
        this.Breaks = breaks;
        this.Bar = bar;
        this.init();
    }

    public void addLeft() {
        int i = 0;
        for (int k = 0; k < this.Files.length; k++) {
            if (this.Files[k].equals("")) {
                this.Bar.addSeparatorLeft();
            } else {
                if (this.Breaks[k].startsWith("!")) {
                    this.Bar.addSeparatorLeft();
                }
                this.Bar.addLeft(this.Icons[i++]);
            }
        }
    }

    public void addRight() {
        int i = 0;
        for (int k = 0; k < this.Files.length; k++) {
            if (this.Files[k].equals("")) {
                this.Bar.addSeparatorRight();
            } else {
                if (this.Breaks[k].startsWith("!")) {
                    this.Bar.addSeparatorRight();
                }
                this.Bar.addRight(this.Icons[i++]);
            }
        }
    }

    public int getN() {
        return this.N;
    }

    public void init() {
        this.N = 0;
        for (int i = 0; i < this.Files.length; i++) {
            if (!this.Files[i].equals("")) {
                this.N++;
            }
        }
        this.Icons = new ToggleIcon[this.N];
        int k = 0;
        for (int i = 0; i < this.Files.length; i++) {
            if (!this.Files[i].equals("")) {
                this.Icons[k++] = new ToggleIcon(this.Bar, this.Files[i], this);
            }
        }
    }

    public void toggle(ToggleIcon icon) {
        for (int i = 0; i < this.N; i++) {
            if (this.Icons[i] == icon) {
                icon.setStateInGroup(true);
            } else {
                this.Icons[i].setStateInGroup(false);
            }
            this.Icons[i].unset(false);
        }
    }

    public void unselect() {
        for (int i = 0; i < this.N; i++) {
            this.Icons[i].setStateInGroup(false);
            this.Icons[i].unset(false);
        }
    }

    public void unset(boolean flag) {
        for (int i = 0; i < this.N; i++) {
            this.Icons[i].dounset(flag);
        }
    }
}

/**
 * @author Rene A primitive icon that displays a GIF image.
 */
class IconWithGif extends BasicIcon {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Image I;
    Color C;
    int W, H, X, Y;

    /**
     * Initialize the icon and load its image. By changing the global parameter
     * "icontype", png can be used too.
     */
    public IconWithGif(IconBar bar, String file) {
        super(bar, file);
        final String iconfile = this.getDisplay(file);
        if (!iconfile.equals("")) {
            file = iconfile;
        }
        try {
            final InputStream in = this.getClass().getResourceAsStream(
                    this.Bar.Resource + file + "."
                            + Global.getParameter("icontype", "gif"));
            int pos = 0;
            int n = in.available();
            final byte b[] = new byte[20000];
            while (n > 0) {
                final int k = in.read(b, pos, n);
                if (k < 0) {
                    break;
                }
                pos += k;
                n = in.available();
            }
            in.close();
            this.I = Toolkit.getDefaultToolkit().createImage(b, 0, pos);
            final MediaTracker T = new MediaTracker(bar);
            T.addImage(this.I, 0);
            T.waitForAll();
        } catch (final Exception e) {
            try {
                this.I = this.getToolkit().getImage(
                        file + "." + Global.getParameter("icontype", "gif"));
                final MediaTracker mt = new MediaTracker(this);
                mt.addImage(this.I, 0);
                mt.waitForID(0);
                if (!(mt.checkID(0) && !mt.isErrorAny())) {
                    throw new Exception("");
                }
            } catch (final Exception ex) {
                this.I = null;
                return;
            }
        }
        this.W = this.I.getWidth(this);
        this.H = this.I.getHeight(this);
        this.X = BasicIcon.Size / 2 - this.W / 2;
        this.Y = BasicIcon.Size / 2 - this.H / 2;
    }

    public IconWithGif(IconBar bar, String name, Color color) {
        super(bar, name);
        this.C = color;
    }

    @Override
    public void dopaint(Graphics g) {
        if (this.I != null) {
            if (this.W > this.getSize().width) {
                g.drawImage(this.I, 1, 1, BasicIcon.Size - 2,
                        BasicIcon.Size - 2, this);
            } else {
                g.drawImage(this.I, this.X, this.Y, this);
            }
        } else if (this.C != null) {
            g.setColor(this.C);
            g.fillRect(3, 3, BasicIcon.Size - 6, BasicIcon.Size - 6);
        } else {
            g.setFont(new Font("Courier", Font.BOLD, BasicIcon.Size / 3));
            final FontMetrics fm = this.getFontMetrics(this.getFont());
            String s = this.getDisplay(this.Name);
            if (s.length() > 3) {
                s = s.substring(0, 3);
            }
            final int w = fm.stringWidth(s);
            final int h = fm.getHeight();
            g.setColor(this.getForeground());
            final Graphics2D G = (Graphics2D) g;
            G.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            G.drawString(s, BasicIcon.Size / 2 - w / 2, BasicIcon.Size / 2 - h
                    / 2 + fm.getAscent());
        }
    }

    public String getDisplay(String name) {
        if (!name.endsWith(")")) {
            return "";
        }
        final int n = name.lastIndexOf('(');
        if (n < 0) {
            return "";
        }
        return name.substring(n + 1, name.length() - 1);
    }

}

/**
 * @author Rene A primitive icon that displays one of several GIF images.
 */
class MultipleIcon extends BasicIcon {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int N;
    Image I[];
    int Selected;
    int X[], Y[], W[], H[];

    public MultipleIcon(IconBar bar, String name) {
        super(bar, name);
        this.Selected = 0;
    }

    public MultipleIcon(IconBar bar, String name, int number) {
        super(bar, name);
        this.N = number;
        this.I = new Image[this.N];
        this.X = new int[this.N];
        this.Y = new int[this.N];
        this.W = new int[this.N];
        this.H = new int[this.N];
        final MediaTracker T = new MediaTracker(bar);
        try {
            for (int i = 0; i < this.N; i++) {
                try {
                    final InputStream in = this.getClass().getResourceAsStream(
                            this.Bar.Resource + name + i + "."
                                    + Global.getParameter("icontype", "gif"));
                    int pos = 0;
                    int n = in.available();
                    final byte b[] = new byte[20000];
                    while (n > 0) {
                        final int k = in.read(b, pos, n);
                        if (k < 0) {
                            break;
                        }
                        pos += k;
                        n = in.available();
                    }
                    in.close();
                    this.I[i] = Toolkit.getDefaultToolkit().createImage(b, 0,
                            pos);
                    T.addImage(this.I[i], i);
                } catch (final Exception e) {
                    this.I[i] = null;
                }
            }
            T.waitForAll();
            for (int i = 0; i < this.N; i++) {
                this.W[i] = this.I[i].getWidth(this);
                this.H[i] = this.I[i].getHeight(this);
                this.X[i] = BasicIcon.Size / 2 - this.W[i] / 2;
                this.Y[i] = BasicIcon.Size / 2 - this.H[i] / 2;
            }
        } catch (final Exception e) {
            for (int i = 0; i < this.N; i++) {
                this.I[i] = null;
            }
        }
    }

    /**
     * Paint a button with an image
     */
    @Override
    public void dopaint(Graphics g) {
        if (this.I[this.Selected] != null) {
            if (this.W[this.Selected] > this.getSize().width) {
                g.drawImage(this.I[this.Selected], 1, 1, BasicIcon.Size - 2,
                        BasicIcon.Size - 2, this);
            } else {
                g.drawImage(this.I[this.Selected], this.X[this.Selected],
                        this.Y[this.Selected], this);
            }
        }
    }

    public int getSelected() {
        return this.Selected;
    }

    /**
     * Go up and down the pictures.
     */
    @Override
    public void pressed(MouseEvent e) {
        if (e.isMetaDown()) {
            this.Selected--;
            if (this.Selected < 0) {
                this.Selected = this.N - 1;
            }
        } else {
            this.Selected++;
            if (this.Selected >= this.N) {
                this.Selected = 0;
            }
        }
    }

    public void setSelected(int s) {
        if (this.Selected == s) {
            return;
        }
        this.Selected = s;
        this.repaint();
    }
}

/**
 * @author Rene A toggle icon for several strings.
 */
class MultipleStringIcon extends MultipleIcon {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String S[];

    public MultipleStringIcon(IconBar bar, String name, String s[]) {
        super(bar, name);
        this.S = s;
        this.N = this.S.length;
    }

    @Override
    public void dopaint(Graphics g) {
        g.setColor(this.getForeground());
        final Font font = new Font("Dialog", Font.PLAIN, BasicIcon.Size * 2 / 3);
        g.setFont(font);
        final FontMetrics fm = this.getFontMetrics(font);
        final int w = fm.stringWidth(this.S[this.Selected]);
        g.drawString(this.S[this.Selected], (BasicIcon.Size - w) / 2,
                BasicIcon.Size - fm.getDescent());
    }

}

/**
 * @author Rene An MultipleIcon that can be enabled externally.
 */
class MultipleToggleIcon extends MultipleIcon {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MultipleToggleIcon(IconBar bar, String name, int number) {
        super(bar, name, number);
    }

    public void setState(boolean flag) {
        this.On = flag;
        this.repaint();
    }
}

/**
 * @author Rene An icon to display on/off state.
 */
class OnOffIcon extends ToggleIcon {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static int LampSize = 4;

    public OnOffIcon(IconBar bar, String file) {
        super(bar, file, null);
    }

    @Override
    public void pressed(MouseEvent e) {
        this.State = this.On = !this.On;
    }
}

/**
 * Button to get all icons, when there is not too much space.
 */
class OverflowButton extends Panel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    IconBar IB;
    boolean Left = true;

    public OverflowButton(IconBar ib, boolean left) {
        this.IB = ib;
        this.Left = left;
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                OverflowButton.this.IB.setShifted(!OverflowButton.this.Left);
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        final int size = BasicIcon.Size;
        g.setColor(this.getBackground());
        g.fill3DRect(0, 0, 10, size, true);
        g.setColor(this.getForeground());
        final int x[] = new int[3], y[] = new int[3];
        if (this.Left) {
            x[0] = 2;
            x[1] = x[2] = 8;
            y[0] = size / 2;
            y[1] = y[0] - 6;
            y[2] = y[0] + 6;
        } else {
            x[0] = 8;
            x[1] = x[2] = 2;
            y[0] = size / 2;
            y[1] = y[0] - 6;
            y[2] = y[0] + 6;
        }
        g.fillPolygon(x, y, 3);
    }
}

class PopupIcon extends BasicIcon {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public PopupIcon(IconBar bar, String name[]) {
        super(bar, name[0]);
    }
}

class SaveColor extends Color {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SaveColor(int red, int green, int blue) {
        super(red > 0 ? red : 0, green > 0 ? green : 0, blue > 0 ? blue : 0);
    }
}

/**
 * A simple separator between icons.
 */

class Separator extends Panel implements IconBarElement {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    final int Size = 6;

    public Separator(IconBar bar) {
        if (bar.Vertical) {
            this.setSize(BasicIcon.Size, this.Size);
        } else {
            this.setSize(this.Size, BasicIcon.Size);
        }
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Point getPosition() {
        return new Point(0, 0);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(this.getBackground());
        if (Global.getParameter("iconbar.showseparators", false)) {
            g.fill3DRect(1, 1, this.getSize().width - 1,
                    this.getSize().height - 1, false);
        } else {
            g.fillRect(1, 1, this.getSize().width - 1,
                    this.getSize().height - 1);
        }
    }

    @Override
    public void setEnabled(boolean flag) {
    }

    @Override
    public void setPosition(int x, int y) {
        this.setLocation(x, y);
    }

    @Override
    public int width() {
        return this.Size;
    }
}

/**
 * An state display. Loads two images from a resource and display either of
 * them, depending on the enabled state.
 */

class StateDisplay extends BasicIcon {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    Image IOn, IOff;
    int W, H, X, Y;

    /**
     * Initialize the icon and load its image.
     */
    public StateDisplay(IconBar bar, String file) {
        super(bar, file);
        try {
            InputStream in = this.getClass().getResourceAsStream(
                    this.Bar.Resource + file + "on" + "."
                            + Global.getParameter("icontype", "gif"));
            int pos = 0;
            int n = in.available();
            final byte b[] = new byte[20000];
            while (n > 0) {
                final int k = in.read(b, pos, n);
                if (k < 0) {
                    break;
                }
                pos += k;
                n = in.available();
            }
            in.close();
            this.IOn = Toolkit.getDefaultToolkit().createImage(b, 0, pos);
            final MediaTracker T = new MediaTracker(bar);
            T.addImage(this.IOn, 0);
            in = this.getClass().getResourceAsStream(
                    this.Bar.Resource + file + "off" + "."
                            + Global.getParameter("icontype", "gif"));
            pos = 0;
            n = in.available();
            final byte b1[] = new byte[20000];
            while (n > 0) {
                final int k = in.read(b1, pos, n);
                if (k < 0) {
                    break;
                }
                pos += k;
                n = in.available();
            }
            in.close();
            this.IOff = Toolkit.getDefaultToolkit().createImage(b1, 0, pos);
            T.addImage(this.IOff, 1);
            T.waitForAll();
            this.W = this.IOn.getWidth(this);
            this.H = this.IOn.getHeight(this);
            if (this.Bar.Vertical) {
                this.X = BasicIcon.Size / 2 - this.W / 2;
            } else {
                this.X = 0;
            }
            this.Y = BasicIcon.Size / 2 - this.H / 2;
        } catch (final Exception e) {
            this.IOn = this.IOff = null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.T = null;
    }

    /**
     * Paint a button with an image
     */
    @Override
    public void paint(Graphics g) {
        if (this.Enabled && this.IOn != null) {
            if (this.W > this.getSize().width) {
                g.drawImage(this.IOn, 1, 1, BasicIcon.Size - 2,
                        BasicIcon.Size - 2, this);
            } else {
                g.drawImage(this.IOn, this.X, this.Y, this);
            }
        } else if (!this.Enabled && this.IOff != null) {
            if (this.W > this.getSize().width) {
                g.drawImage(this.IOff, 1, 1, BasicIcon.Size - 2,
                        BasicIcon.Size - 2, this);
            } else {
                g.drawImage(this.IOff, this.X, this.Y, this);
            }
        }
    }
}

/**
 * @author Rene An action icon for one click.
 */
class ToggleIcon extends IconWithGif {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    boolean State;
    private final IconGroup G;

    public ToggleIcon(IconBar bar, String file) {
        this(bar, file, null);
    }

    public ToggleIcon(IconBar bar, String file, Color c, IconGroup g) {
        super(bar, file, c);
        this.State = false;
        this.G = g;
    }

    public ToggleIcon(IconBar bar, String file, IconGroup g) {
        super(bar, file);
        this.State = false;
        this.G = g;
    }

    public int countPeers() {
        if (this.G == null) {
            return 0;
        }
        return this.G.getN();
    }

    public void doset() {
        super.unset(false);
    }

    public void dounset(boolean flag) {
        super.unset(flag);
    }

    public boolean getState() {
        return this.State;
    }

    @Override
    public void pressed(MouseEvent e) {
        this.setState(!this.On);
    }

    public void set() {
        if (this.G != null) {
            this.G.unset(false);
        } else {
            super.unset(false);
        }
    }

    public void setState(boolean state) {
        if (this.G != null) {
            this.G.toggle(this);
        } else {
            if (this.On == state) {
                this.State = state;
                return;
            }
            this.On = this.State = state;
            this.repaint();
        }
    }

    public void setStateInGroup(boolean state) {
        if (this.On == state) {
            this.State = state;
            return;
        }
        this.On = this.State = state;
        this.repaint();
    }

    public void unselect() {
        if (this.G != null) {
            this.G.unselect();
        }
    }

    @Override
    public void unset() {
        if (this.G != null) {
            this.G.unset(true);
        } else {
            super.unset();
        }
    }
}
