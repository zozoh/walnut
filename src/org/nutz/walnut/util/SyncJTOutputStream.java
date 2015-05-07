package org.nutz.walnut.util;

import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.box.WnTunnel;

public class SyncJTOutputStream extends OutputStream {

    private WnTunnel tnl;

    public SyncJTOutputStream(WnTunnel turnnel) {
        this.tnl = turnnel;
    }

    @Override
    public void write(int b) {
        tnl.write((byte) b);
        Lang.notifyAll(tnl);
    }

    @Override
    public void write(byte[] bs, int off, int len) {
        tnl.write(bs, off, len);
        Lang.notifyAll(tnl);
    }

    @Override
    public void close() {
        tnl.closeWrite();
        Lang.notifyAll(tnl);
    }

}
