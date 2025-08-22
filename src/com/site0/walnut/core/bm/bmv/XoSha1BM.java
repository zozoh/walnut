package com.site0.walnut.core.bm.bmv;

import java.io.File;
import java.io.IOException;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.WnIoHandleMutexException;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.core.bm.AbstractIoBM;
import com.site0.walnut.core.bm.BMSwapFiles;
import com.site0.walnut.core.bm.Sha1Parts;
import com.site0.walnut.core.bm.vofs.VofsBM;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public abstract class XoSha1BM extends AbstractIoBM {

    XoService api;

    private BMSwapFiles swaps;

    private WnReferApi refers;

    Sha1Parts parts;

    public XoSha1BM(WnIoHandleManager handles,
                    String phSwap,
                    boolean autoCreate,
                    String signAlg,
                    String parts,
                    XoService api,
                    WnReferApi refers) {
        super(handles);
        this.api = api;
        // 仅仅支持 SHA1
        if ("sha1".equals(signAlg)) {
            throw Er.create("e.io.bm.XoSha1BM.invalidSignAlg", signAlg);
        }

        // 如何分段, parts=22 表示 [2,2]
        // 譬如签名: b10d47941e27dad21b63fb76443e1669195328f2
        // 对应路径: b1/0d/47941e27dad21b63fb76443e1669195328f2
        this.parts = new Sha1Parts(parts);

        // 获取交换区目录
        this.swaps = BMSwapFiles.create(phSwap, autoCreate);

        // 引用计数管理器
        this.refers = refers;
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (this == bm)
            return true;
        if (null == bm)
            return false;
        if (bm instanceof XoSha1BM) {
            if (!Wlang.isEqual(this.api, ((XoSha1BM) bm).api)) {
                return false;
            }
            if (!Wlang.isEqual(this.parts, ((XoSha1BM) bm).parts)) {
                return false;
            }
            return true;
        }
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
