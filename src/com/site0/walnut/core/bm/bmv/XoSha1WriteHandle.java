package com.site0.walnut.core.bm.bmv;

import java.io.File;
import java.io.IOException;

import com.site0.walnut.core.bm.WnIoWriteSwapHandle;

public class XoSha1WriteHandle extends WnIoWriteSwapHandle {

    private VXDataSignBM bm;

    XoSha1WriteHandle(VXDataSignBM bm) {
        this.bm = bm;
    }

    @Override
    protected File createSwapFile() {
        return bm.swaps.createSwapFile();
    }

    @Override
    protected void update_when_close(File swap) throws IOException {
        // 根据交换文件更新对象的索引
        bm.updateObjSha1AndSaveSwap(this.obj, swap, indexer);
    }

}
