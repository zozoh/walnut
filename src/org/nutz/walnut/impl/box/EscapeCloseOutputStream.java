package org.nutz.walnut.impl.box;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EscapeCloseOutputStream extends FilterOutputStream {

    public EscapeCloseOutputStream(OutputStream ops) {
        super(ops);
    }

    public void close() throws IOException {
    }
}
