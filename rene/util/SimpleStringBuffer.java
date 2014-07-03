package rene.util;

public class SimpleStringBuffer {
    private int Size, N;
    private char Buf[];

    public SimpleStringBuffer(char b[]) {
        this.Size = b.length;
        this.Buf = b;
        this.N = 0;
    }

    public SimpleStringBuffer(int size) {
        this.Size = size;
        this.Buf = new char[size];
        this.N = 0;
    }

    public void append(char c) {
        if (this.N < this.Size) {
            this.Buf[this.N++] = c;
        } else {
            this.Size = 2 * this.Size;
            final char NewBuf[] = new char[this.Size];
            for (int i = 0; i < this.N; i++) {
                NewBuf[i] = this.Buf[i];
            }
            this.Buf = NewBuf;
            this.Buf[this.N++] = c;
        }
    }

    public void append(String s) {
        final int n = s.length();
        for (int i = 0; i < n; i++) {
            this.append(s.charAt(i));
        }
    }

    public void clear() {
        this.N = 0;
    }

    @Override
    public String toString() {
        if (this.N == 0) {
            return "";
        }
        return new String(this.Buf, 0, this.N);
    }
}
