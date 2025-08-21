package com.site0.walnut.core.bm.bmv;

import java.io.File;
import java.io.IOException;

import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.WnIoHandleMutexException;
import com.site0.walnut.core.bm.AbstractIoBM;
import com.site0.walnut.ext.xo.impl.XoService;

public abstract class VirtualObjSha1BM extends AbstractIoBM {

    private XoService api;

    public VirtualObjSha1BM(WnIoHandleManager handles, XoService api) {
        super(handles);
        this.api = api;
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public WnIoHandle open(WnObj o, int mode, WnIoIndexer indexer)
            throws WnIoHandleMutexException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnIoHandle createHandle(int mode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WnIoHandle checkHandle(String hid) {
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

    @Override
    public void updateObjSha1(WnObj o, File swap, WnIoIndexer indexer) {
        // 对象存储，不需要这个方法
    }

    @Override
    public void updateObjSha1(WnObj o,
                              WnIoIndexer indexer,
                              String sha1,
                              long len,
                              long lm) {
        // 对象存储，不需要这个方法
    }
}
