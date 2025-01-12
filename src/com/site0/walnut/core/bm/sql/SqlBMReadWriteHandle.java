package com.site0.walnut.core.bm.sql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.nutz.lang.Streams;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.bm.WnIoReadWriteSwapHandle;

public class SqlBMReadWriteHandle extends WnIoReadWriteSwapHandle {

    private SqlBM bm;

    public SqlBMReadWriteHandle(SqlBM bm) {
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

    @Override
    protected void load_to_swap(File swap) {
        try {
            Blob blob = bm.getBlob(obj);
            InputStream ins = blob.getBinaryStream();
            OutputStream ops = Streams.fileOut(swap);
            Streams.writeAndClose(ops, ins);
        }
        catch (SQLException e) {
            throw Er.wrap(e);
        }
    }

}
