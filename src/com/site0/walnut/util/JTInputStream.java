package com.site0.walnut.util;

import java.io.InputStream;

import com.site0.walnut.api.box.WnTunnel;

public class JTInputStream extends InputStream {

    private WnTunnel tnl;

    public JTInputStream(WnTunnel turnnel) {
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
