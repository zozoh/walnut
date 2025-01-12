package com.site0.walnut.core.bm.localbm;

import java.io.File;
import java.io.IOException;
import com.site0.walnut.core.bm.WnIoWriteSwapHandle;

public class LocalIoWriteHandle extends WnIoWriteSwapHandle {

    private LocalIoBM bm;

    LocalIoWriteHandle(LocalIoBM bm) {
        this.bm = bm;
    }

    @Override
    protected File createSwapFile() {
        return bm.createSwapFile();
    }

    @Override
    protected void update_when_close(File swap) throws IOException {
        // 根据交换文件更新对象的索引
        bm.updateObjSha1(this.obj, swap, indexer);
    }

}
