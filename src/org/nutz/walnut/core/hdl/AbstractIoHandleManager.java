package org.nutz.walnut.core.hdl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.HandleInfo;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.core.WnIoMappingFactory;

public abstract class AbstractIoHandleManager implements WnIoHandleManager {

    protected WnIoMappingFactory mappings;

    public AbstractIoHandleManager(WnIoMappingFactory mappings) {
        this.mappings = mappings;
    }

    @Override
    public WnIoHandle check(String hid) {
        HandleInfo info = this.load(hid);
        if (null == info) {
            throw Er.create("e.io.hdl.noexists", hid);
        }
        // 取得映射
        WnIoMapping mp = mappings.check(info.getMount());

        // 创建真实句柄类
        WnIoHandle h = mp.getBucketManager().createHandle(info.getMode());
        this.setup(h, info);
        return h;
    }

    @Override
    public void setup(WnIoHandle h, HandleInfo info) {
        // 取得映射
        WnIoMapping mp = mappings.check(info.getMount());

        // 更新字段
        h.updateBy(info);

        // 设置必要属性
        WnObj o = mp.get(h.getTargetId());
        h.setManager(this);
        h.setObj(o);
        h.setIndexer(mp.getIndexer());
    }

}
