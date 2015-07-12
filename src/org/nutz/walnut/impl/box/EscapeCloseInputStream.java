package org.nutz.walnut.impl.box;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EscapeCloseInputStream extends FilterInputStream {

    public EscapeCloseInputStream(InputStream ins) {
        super(ins);
    }
    
    public void close() throws IOException {
    }
}
