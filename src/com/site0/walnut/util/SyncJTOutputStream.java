package com.site0.walnut.util;

import java.io.OutputStream;

import com.site0.walnut.api.box.WnTunnel;

public class SyncJTOutputStream extends OutputStream {

    private WnTunnel tnl;

    public SyncJTOutputStream(WnTunnel turnnel) {
        this.tnl = turnnel;
    }

    @Override
    public void write(int b) {
        tnl.write((byte) b);
        Wlang.notifyAll(tnl);
    }

    @Override
    public void write(byte[] bs, int off, int len) {
        tnl.write(bs, off, len);
        Wlang.notifyAll(tnl);
    }

    @Override
    public void close() {
        tnl.closeWrite();
        Wlang.notifyAll(tnl);
    }

}
