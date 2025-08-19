package com.site0.walnut.ext.xo.io.bm;

import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.bm.AbstractIoBM;
import com.site0.walnut.core.bm.BMSwapFiles;
import com.site0.walnut.ext.xo.impl.XoService;

public class WnVofsBM extends AbstractIoBM {

    BMSwapFiles swaps;

    private XoService xos;

    public WnVofsBM(WnIoHandleManager handles, String phSwap, XoService xos) {
        super(handles);
        this.xos = xos;

        // 获取交换区目录
        this.swaps = BMSwapFiles.create(phSwap, true);
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public WnIoHandle createHandle(int mode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long copy(WnObj oSr, WnObj oTa) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long remove(WnObj o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        // TODO Auto-generated method stub
        return 0;
    }

}
