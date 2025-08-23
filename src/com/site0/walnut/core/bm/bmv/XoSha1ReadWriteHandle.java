package com.site0.walnut.core.bm.bmv;

import java.io.File;
import java.io.IOException;
import com.site0.walnut.core.bm.WnIoReadWriteSwapHandle;

public class XoSha1ReadWriteHandle extends WnIoReadWriteSwapHandle {

    private XoSha1BM bm;

    XoSha1ReadWriteHandle(XoSha1BM bm) {
        this.bm = bm;
    }

    @Override
    protected File createSwapFile() {
        return bm.swaps.createSwapFile();
    }

    @Override
    protected void update_when_close(File swap) throws IOException {
        bm.updateObjSha1AndSaveSwap(this.obj, swap, indexer);
    }

    @Override
    protected void load_to_swap(File swap) {
        bm.load_to_swap(obj, swap);
    }
}
