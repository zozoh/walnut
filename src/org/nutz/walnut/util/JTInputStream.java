package org.nutz.walnut.util;

import java.io.InputStream;

import org.nutz.walnut.api.box.WnTurnnel;

public class JTInputStream extends InputStream {

    private WnTurnnel tnl;

    public JTInputStream(WnTurnnel turnnel) {
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
        tnl.closeRead();
    }

}
