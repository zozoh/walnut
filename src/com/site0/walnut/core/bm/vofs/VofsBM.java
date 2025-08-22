package com.site0.walnut.core.bm.vofs;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.bm.AbstractIoBM;
import com.site0.walnut.core.bm.BMSwapFiles;
import com.site0.walnut.core.indexer.vofs.WnVofsObj;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class VofsBM extends AbstractIoBM {

    BMSwapFiles swaps;

    XoService api;

    public VofsBM(WnIoHandleManager handles, String phSwap, XoService api) {
        super(handles);
        this.api = api;
        // 获取交换区目录
        this.swaps = BMSwapFiles.create(phSwap, true);
    }

    @Override
    public boolean isSame(WnIoBM bm) {
        if (this == bm)
            return true;
        if (null == bm)
            return false;
        if (bm instanceof VofsBM) {
            return Wlang.isEqual(this.api, ((VofsBM) bm).api);
        }
        return false;
    }

    @Override
    public WnIoHandle createHandle(int mode) {
        // 只读
        if (Wn.S.isRead(mode)) {
            return new VofsBMReadHandle(this);
        }
        // 只写
        if (Wn.S.isWrite(mode)) {
            return new VofsBMWriteHandle(this);
        }
        // 追加
        if (Wn.S.isAppend(mode)) {
            return new VofsBMReadWriteHandle(this);
        }
        // 修改
        if (Wn.S.canModify(mode) || Wn.S.isReadWrite(mode)) {
            return new VofsBMReadWriteHandle(this);
        }
        throw Er.create("e.io.bm.VofsBM.NonsupportMode", mode);
    }

    @Override
    public long copy(WnObj oSr, WnObj oTa) {
        String srcKey = ((WnVofsObj) oSr).getObjKey();
        String dstKey = ((WnVofsObj) oTa).getObjKey();
        api.copy(srcKey, dstKey);
        return 1;
    }

    @Override
    public long remove(WnObj o) {
        String objKey = ((WnVofsObj) o).getObjKey();
        api.deleteObj(objKey);
        return 0;
    }

    @Override
    public long truncate(WnObj o, long len, WnIoIndexer indexer) {
        String objKey = ((WnVofsObj) o).getObjKey();
        // 直接置空
        if (0 == len) {
            api.writeText(objKey, "", null);
        }
        // 读取后，再剪裁
        else if (len > 0) {
            byte[] content = api.readBytes(objKey);
            int sz = Math.min((int) len, content.length);
            byte[] bs = new byte[sz];
            System.arraycopy(content, 0, bs, 0, sz);
            api.writeBytes(objKey, bs, null);
        }
        return 0;
    }

}
