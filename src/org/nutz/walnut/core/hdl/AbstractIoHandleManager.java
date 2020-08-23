package org.nutz.walnut.core.hdl;

import org.nutz.lang.random.R;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.HandleInfo;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleManager;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.core.WnIoMappingFactory;
import org.nutz.walnut.util.Wn;

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
    public void alloc(WnIoHandle h) {
        if (!h.hasId()) {
            h.setId(R.UU32());
        }
        this.save(h);
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
        // 未分配的句柄，不予保存
        if (!h.hasId())
            return;
        long now = Wn.now();
        h.setCreatTime(now);
        if (timeout > 0) {
            long du = timeout * 1000;
            long expi = now + du;
            h.setTimeout(du);
            h.setExpiTime(expi);
        }
        // 执行保存
        doSave(h);
    }

    protected abstract void doSave(WnIoHandle h);

    @Override
    public void touch(WnIoHandle h) {
        // 未分配的句柄，不予持久化
        if (!h.hasId())
            return;
        long du = h.getTimeout();
        if (du > 0) {
            long now = Wn.now();
            long pas = h.getExpiTime() - now;
            // 如果已经经过了一半过期时间，为了防止删除，更新一下过期时间
            if (pas < (du * 500)) {
                long expi = now + du;
                h.setExpiTime(expi);
                doTouch(h);
            }
        }
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
