package com.site0.walnut.core.bm.bml;

import java.io.File;
import java.io.IOException;
import com.site0.walnut.core.bm.WnIoWriteSwapHandle;

public class LocalSha1WriteHandle extends WnIoWriteSwapHandle {

    private LocalSha1BM bm;

    LocalSha1WriteHandle(LocalSha1BM bm) {
        this.bm = bm;
    }

    @Override
    protected File createSwapFile() {
        return bm.createSwapFile();
    }

    @Override
    protected void update_when_close(File swap) throws IOException {
        // 根据交换文件更新对象的索引
        bm.updateObjSha1AndSaveSwap(this.obj, swap, indexer);
    }

}
