package org.nutz.walnut.util;

import java.io.InputStream;

public class JTInputStream extends InputStream {

    private JvmTurnnel tnl;

    public JTInputStream(JvmTurnnel turnnel) {
        this.tnl = turnnel;
    }

    @Override
    public int read() {
        return tnl.read();
    }

    @Override
    public int read(byte[] bs, int off, int len) {
        return tnl.read(bs, off, len);
    }

    @Override
    public void close() {
        tnl.r4R = null;
    }

}
