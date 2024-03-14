package com.site0.walnut.util;

import java.io.InputStream;

import org.nutz.lang.Lang;
import org.nutz.log.Log;
import com.site0.walnut.api.box.WnTunnel;

public class SyncJTInputStream extends InputStream {

    private static final Log log = Wlog.getIO();

    private WnTunnel tnl;

    public SyncJTInputStream(WnTunnel turnnel) {
        this.tnl = turnnel;
    }

    @Override
    public int read() {
        byte re = tnl.read();
        while (re == -1) {
            if (tnl.isWritable())
                Lang.wait(tnl, 100);
            else
                break;
            re = tnl.read();
        }
        return re;
    }

    @Override
    public int read(byte[] bs, int off, int len) {
        int re = tnl.read(bs, off, len);
        while (re == -1) {
            if (tnl.isWritable())
                Lang.wait(tnl, 100);
            else
                break;
            if (log.isDebugEnabled())
                log.debugf("TURNNEL.retry: %d/%d", tnl.size(), tnl.getReadSum());
            re = tnl.read(bs, off, len);
        }
        return re;
    }

    @Override
    public void close() {
        tnl.closeRead();
    }

}
