package org.nutz.walnut.core.bm.local;

import org.nutz.walnut.core.WnIoHandle;

public class LocalIoHandle extends WnIoHandle {

    @Override
    public int read(byte[] buf, int off, int len) {
        return 0;
    }

    @Override
    public int write(byte[] buf, int off, int len) {
        return 0;
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}

}
