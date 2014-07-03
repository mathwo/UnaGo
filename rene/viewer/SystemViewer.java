package rene.viewer;

import java.awt.*;
import java.io.PrintWriter;

public class SystemViewer extends Viewer {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    TextArea T;

    public SystemViewer() {
        super("dummy");
        this.setLayout(new BorderLayout());
        this.add("Center", this.T = new TextArea());
    }

    @Override
    public void append(String s) {
        this.T.append(s);
    }

    @Override
    public void append(String s, Color c) {
        this.append(s);
    }

    @Override
    public void appendLine(String s) {
        this.T.append(s + "\n");
    }

    @Override
    public void appendLine(String s, Color c) {
        this.appendLine(s);
    }

    @Override
    public void doUpdate(boolean showlast) {
        this.T.repaint();
    }

    @Override
    public void save(PrintWriter fo) {
        fo.print(this.T.getText());
        fo.flush();
    }

    @Override
    public void setFont(Font s) {
        this.T.setFont(s);
    }

    @Override
    public void setText(String s) {
        this.T.setText(s);
    }
}
