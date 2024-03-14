package com.site0.walnut.ext.net.sshd.srv;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NoCloseOutputStream extends FilterOutputStream {

    public NoCloseOutputStream(OutputStream out) {
        super(out);
    }

    public void close() throws IOException {}

    boolean preR;

    public void write(int b) throws IOException {
        switch (b) {
        case '\r':
            preR = true;
            break;
        case '\n':
            if (!preR) {
                out.write('\r');
            }
            break;
        default:
            preR = false;
            break;
        }
        out.write(b);
    }
}