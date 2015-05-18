package org.nutz.walnut.impl.box;

import java.io.IOException;
import java.io.OutputStream;

public class EscapeCloseOutputStream extends OutputStream {

    private OutputStream ops;

    public EscapeCloseOutputStream(OutputStream ops) {
        this.ops = ops;
    }

    public void write(int b) throws IOException {
        ops.write(b);
    }

    public int hashCode() {
        return ops.hashCode();
    }

    public void write(byte[] b) throws IOException {
        ops.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        ops.write(b, off, len);
    }

    public boolean equals(Object obj) {
        return ops.equals(obj);
    }

    public void flush() throws IOException {
        ops.flush();
    }

    public void close() throws IOException {
        // ops.close();
    }

    public String toString() {
        return ops.toString();
    }

}
