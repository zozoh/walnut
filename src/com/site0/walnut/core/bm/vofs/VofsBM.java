package com.site0.walnut.core.bm.vofs;

import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.bm.AbstractIoBM;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.util.Wlang;

public class WnVofsBM extends AbstractIoBM {

    private XoService api;

    public WnVofsBM(WnIoHandleManager handles, XoService api) {
        super(handles);
        this.api = api;
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (this == bm)
            return true;
        if (null == bm)
            return false;
        if (bm instanceof WnVofsBM) {
            return Wlang.isEqual(this.api, ((WnVofsBM) bm).api);
        }
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
