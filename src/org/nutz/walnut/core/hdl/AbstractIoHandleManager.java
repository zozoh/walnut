package org.nutz.walnut.core.hdl;

import org.nutz.lang.random.R;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.HandleInfo;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.core.WnIoMappingFactory;

public abstract class AbstractIoHandleManager implements WnIoHandleManager {

    private WnIoMappingFactory mappings;

    /**
     * 创建一个句柄的过期时间（秒）
     * <p>
     * 负数和0表示永不过期
     */
    private int timeout;

    public AbstractIoHandleManager(WnIoMappingFactory mappings, int timeout) {
        this.mappings = mappings;
        this.timeout = timeout;
    }

    @Override
    public WnIoHandle check(String hid) {
        HandleInfo info = this.load(hid);
        if (null == info) {
            throw Er.create("e.io.hdl.noexists", hid);
        }
        // 取得映射
        WnIoMapping mp = mappings.checkMapping(info.getHomeId(), info.getMount());

        // 创建真实句柄类
        WnIoHandle h = mp.getBucketManager().createHandle(info.getMode());
        this.setup(h, info);
        return h;
    }

    @Override
    public void save(WnIoHandle h) {
        long now = System.currentTimeMillis();
        h.setCreatTime(now);
        if (timeout > 0) {
            long du = timeout * 1000;
            long expi = now + du;
            h.setTimeout(du);
            h.setExpiTime(expi);
        }
        // 分配ID
        if (!h.hasId()) {
            h.setId(R.UU32());
        }
        doSave(h);
    }

    protected abstract void doSave(WnIoHandle h);

    @Override
    public void touch(WnIoHandle h) {
        long du = h.getTimeout();
        if (du > 0) {
            long now = System.currentTimeMillis();
            long expi = now + du;
            h.setExpiTime(expi);
        }
        doTouch(h);
    }

    protected abstract void doTouch(WnIoHandle h);

    @Override
    public void setup(WnIoHandle h, HandleInfo info) {
        // 取得映射
        WnIoMapping mp = mappings.checkMapping(info.getHomeId(), info.getMount());

        // 更新字段
        h.updateBy(info);

        // 设置必要属性
        WnObj o = mp.getIndexer().get(h.getTargetId());
        h.setManager(this);
        h.setObj(o);
        h.setIndexer(mp.getIndexer());
    }

}
