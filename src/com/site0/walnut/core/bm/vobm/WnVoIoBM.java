package com.site0.walnut.core.bm.vobm;

import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.bm.AbstractIoBM;

public class WnVoIoBM extends AbstractIoBM{

    public WnVoIoBM(WnIoHandleManager handles) {
        super(handles);
        // TODO Auto-generated constructor stub
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
