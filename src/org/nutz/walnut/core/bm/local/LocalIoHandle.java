package org.nutz.walnut.core.bm.local;

import java.io.File;

import org.nutz.walnut.core.WnIoHandle;

public class LocalIoHandle extends WnIoHandle {

    private LocalIoBM bm;
    
    private File swap;

    LocalIoHandle(LocalIoBM bm) {
        this.bm = bm;
    }
    
    private void prepareSwap() {
        
    }

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
