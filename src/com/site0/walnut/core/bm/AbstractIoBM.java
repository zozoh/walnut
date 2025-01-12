package com.site0.walnut.core.bm;

import java.io.File;
import java.io.IOException;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.HandleInfo;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.WnIoHandleMutexException;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public abstract class AbstractIoBM implements WnIoBM {

    protected WnIoHandleManager handles;

    public AbstractIoBM(WnIoHandleManager handles) {
        this.handles = handles;
    }

    @Override
    public void updateObjSha1(WnObj o, File swap, WnIoIndexer indexer) throws IOException {
        String sha1 = null;
        long olen = 0;
        long lm = -1;
        // 某些时候，没有调用写接口的句柄实例，或者仅仅写了空字节的实例
        // 并不会生成 swap 文件
        if (null != swap && null != o) {
            sha1 = Wlang.sha1(swap);
            olen = swap.length();
            lm = swap.lastModified();
        }

        if (null != o) {
            if (lm <= 0) {
                lm = Wn.now();
            }
            // 如果和原来的一样,那就无语了，啥也不用做了
            if (o.isSameSha1(sha1)) {
                return;
            }

            o.sha1(sha1);
            o.lastModified(lm);
            o.len(olen);

            // 更新索引
            indexer.set(o, "^(sha1|len|lm)$");
        }
    }

    @Override
    public WnIoHandle open(WnObj o, int mode, WnIoIndexer indexer)
            throws WnIoHandleMutexException, IOException {
        // 先搞一个句柄
        WnIoHandle h = createHandle(mode);
        h.setManager(handles);
        h.setIndexer(indexer);
        h.setObj(o);
        h.setMode(mode);
        h.setOffset(0);

        // 声明准备完成，句柄实现类可能会调用管理器持久化自己
        // 之前，管理器实现类可能会依据持久化的数据进行写互斥
        // 如果当前这个句柄不能创建，这个方法会抛出异常
        h.ready();

        return h;
    }

    @Override
    public WnIoHandle checkHandle(String hid) {
        HandleInfo info = handles.load(hid);
        WnIoHandle h = createHandle(info.getMode());
        handles.setup(h, info);
        // 确保已经填充了索引
        if (h.getIndexer() == null) {
            throw Er.create("e.io.bm.local.checkHandle.NilIndexer");
        }
        // 确保已经填充了对象
        if (h.getObj() == null) {
            throw Er.create("e.io.bm.local.checkHandle.NilObj");
        }
        return h;
    }

}