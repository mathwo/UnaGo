/*
 * Created on 20.07.2005
 *
 */
package rene.util.ftp;

import java.io.BufferedReader;
import java.io.IOException;

class Answer {
    int Code;
    String Text;

    int code() {
        return this.Code;
    }

    public void get(BufferedReader in) throws IOException {
        String s = in.readLine();
        final String CodeString = s.substring(0, 3);
        this.Code = Integer.parseInt(CodeString);
        this.Text = s.substring(4);
        if (s.charAt(3) == '-') {
            while (true) {
                s = in.readLine();
                if (s.startsWith(CodeString)) {
                    this.Text = this.Text + "\n" + s.substring(4);
                    break;
                }
                this.Text = this.Text + "\n" + s;
            }
        }
    }

    String text() {
        return this.Text;
    }
}
