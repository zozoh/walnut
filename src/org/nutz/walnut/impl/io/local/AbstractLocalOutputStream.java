package org.nutz.walnut.impl.io.local;

import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractLocalOutputStream extends OutputStream {

    protected OutputStream ops;

    public AbstractLocalOutputStream(OutputStream ops) {
        this.ops = ops;
    }

    public void write(int b) throws IOException {
        ops.write(b);
    }

    public void write(byte[] b) throws IOException {
        ops.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        ops.write(b, off, len);
    }

    public void flush() throws IOException {
        ops.flush();
    }

}
