package com.site0.walnut.core.bm.sql;

import java.io.File;
import java.io.IOException;
import com.site0.walnut.core.bm.WnIoWriteSwapHandle;

public class SqlBMWriteHandle extends WnIoWriteSwapHandle {

    private SqlBM bm;

    public SqlBMWriteHandle(SqlBM bm) {
        this.bm = bm;
    }

    @Override
    protected File createSwapFile() {
        return bm.swaps.createSwapFile();
    }

    @Override
    protected void update_when_close(File swap) throws IOException {
        bm.writeBlob(obj, swap);
        bm.updateObjSha1(obj, swap, indexer);
    }

}
