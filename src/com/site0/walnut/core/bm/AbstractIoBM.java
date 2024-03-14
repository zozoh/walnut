package com.site0.walnut.core.bm;

import java.io.IOException;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.HandleInfo;
import com.site0.walnut.core.WnIoBM;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleManager;
import com.site0.walnut.core.WnIoHandleMutexException;

public abstract class AbstractIoBM implements WnIoBM {

    protected WnIoHandleManager handles;

    public AbstractIoBM(WnIoHandleManager handles) {
        this.handles = handles;
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