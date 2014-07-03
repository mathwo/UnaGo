/*
 * Created on 14.01.2006
 *
 */
package rene.lister;

import java.awt.*;

public class StringElement implements Element {
    public String S;
    public Color C;

    public StringElement(String s) {
        this(s, null);
    }

    public StringElement(String s, Color c) {
        this.S = s;
        this.C = c;
    }

    @Override
    public Color getElementColor() {
        return this.C;
    }

    @Override
    public String getElementString() {
        return this.S;
    }

    @Override
    public String getElementString(int state) {
        return this.S;
    }
}
