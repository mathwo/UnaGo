package rene.util;

public class SimpleByteBuffer {
    private int Size, N;
    private byte Buf[];

    public SimpleByteBuffer(byte b[]) {
        this.Size = b.length;
        this.Buf = b;
        this.N = 0;
    }

    public SimpleByteBuffer(int size) {
        this.Size = size;
        this.Buf = new byte[size];
        this.N = 0;
    }

    public void append(byte c) {
        if (this.N < this.Size) {
            this.Buf[this.N++] = c;
        } else {
            this.Size = 2 * this.Size;
            final byte NewBuf[] = new byte[this.Size];
            for (int i = 0; i < this.N; i++) {
                NewBuf[i] = this.Buf[i];
            }
            this.Buf = NewBuf;
            this.Buf[this.N++] = c;
        }
    }

    public void clear() {
        this.N = 0;
    }

    public byte[] getBuffer() {
        return this.Buf;
    }

    public byte[] getByteArray() {
        final byte b[] = new byte[this.N];
        for (int i = 0; i < this.N; i++) {
            b[i] = this.Buf[i];
        }
        return b;
    }

    public int size() {
        return this.N;
    }
}
