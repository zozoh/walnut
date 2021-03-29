package org.nutz.walnut.util.stream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NoFlushOutputStream extends FilterOutputStream {

    public NoFlushOutputStream(OutputStream out) {
        super(out);
    }

    public void flush() throws IOException {
        // nop
    }
    
}
