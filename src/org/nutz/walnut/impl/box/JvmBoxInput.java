package org.nutz.walnut.impl.box;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;

public class JvmBoxInput implements Closeable {

    private InputStream ins;

    private BufferedReader __r;

    public JvmBoxInput(InputStream ins) {
        this.ins = ins;
        this.__r = Streams.buffr(Streams.utf8r(ins));
    }

    public String readLine() {
        if (null == __r)
            return null;
        try {
            return __r.readLine();
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }
    }

    public void close() throws IOException {
        Streams.safeClose(__r);
    }

}
