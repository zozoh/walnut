package com.site0.walnut.core.bm.vofs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.core.bm.WnIoWriteSwapHandle;
import com.site0.walnut.core.indexer.vofs.WnVofsObj;
import com.site0.walnut.util.Wlang;

public class VofsBMWriteHandle extends WnIoWriteSwapHandle {

    private VofsBM bm;

    public VofsBMWriteHandle(VofsBM bm) {
        this.bm = bm;
    }

    @Override
    protected File createSwapFile() {
        return bm.swaps.createSwapFile();
    }

    @Override
    protected void update_when_close(File swap) throws IOException {
        // 写入时直接记录 sha1
        String sha1 = Wlang.sha1(swap);

        String key = ((WnVofsObj) obj).getObjKey();
        InputStream ins = Streams.fileIn(swap);
        NutMap meta = Wlang.map("sha1", sha1);
        try {
            bm.api.write(key, ins, meta);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

}
