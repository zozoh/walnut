package org.nutz.walnut.core.bm.local;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;

public abstract class AbstractIoBM implements WnIoBM {

    protected WnIoHandleManager handles;

    public AbstractIoBM(WnIoHandleManager handles) {
        this.handles = handles;
    }

    @Override
    public WnIoHandle checkHandle(String hid) {
        WnIoHandle h = createHandle();
        handles.load(h);
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