package com.site0.walnut.core.bm.localbm;

import java.io.File;
import java.io.IOException;
import org.nutz.lang.Files;
import com.site0.walnut.core.bm.WnIoReadWriteSwapHandle;

public class LocalIoReadWriteHandle extends WnIoReadWriteSwapHandle {

    private LocalIoBM bm;

    LocalIoReadWriteHandle(LocalIoBM bm) {
        this.bm = bm;
    }

    @Override
    protected File createSwapFile() {
        return bm.createSwapFile();
    }

    @Override
    protected void update_when_close(File swap) throws IOException {
        bm.updateObjSha1AndSaveSwap(this.obj, swap, indexer);
    }

    @Override
    protected void load_to_swap(File swap) {
        File buck = bm.checkBucketFile(obj.sha1());
        Files.copy(buck, swap);
    }

}
