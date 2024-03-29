package com.site0.walnut.core.hdl.redis;

import java.util.Map;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.core.HandleInfo;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoMappingFactory;
import com.site0.walnut.core.hdl.AbstractIoHandleManager;
import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;

/**
 * 这个是个 Redis 维护的句柄集合。
 * <p>
 * 它占有 Redis 的一个数据库，键为 "io:hdl:$ID"，值是HASH
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class RedisIoHandleManager extends AbstractIoHandleManager {

    private WedisConfig conf;

    public RedisIoHandleManager(WnIoMappingFactory mappings, int timeout, WedisConfig conf) {
        super(mappings, timeout);
        this.conf = conf;
    }

    @Override
    public HandleInfo load(String hid) {
        String key = _KEY(hid);
        return Wedis.runGet(conf, jed -> {
            Map<String, String> map = jed.hgetAll(key);
            HandleInfo info = null;
            if (null != map) {
                info = Wlang.map2Object(map, HandleInfo.class);
            }
            return info;
        });
    }

    @Override
    protected void doSave(WnIoHandle h) {
        if (!h.hasId()) {
            throw Er.create("e.io.hdl.doSave.withoutID");
        }
        String key = _KEY(h.getId());
        Wedis.run(conf, jed -> {
            Map<String, String> map = h.toStringMap();
            jed.hmset(key, map);
            if (h.hasTimeout()) {
                int du = h.getTimeoutInSecond();
                jed.expire(key, (long) du);
            }
        });
    }

    @Override
    protected void doTouch(WnIoHandle h) {
        if (h.hasTimeout()) {
            String key = _KEY(h.getId());
            Wedis.run(conf, jed -> {
                Map<String, String> map = h.toStringTouchMap();
                jed.hmset(key, map);
                int du = h.getTimeoutInSecond();
                jed.expire(key, (long) du);
            });
        }
    }

    @Override
    public void remove(String hid) {
        if (!Strings.isBlank(hid)) {
            String key = _KEY(hid);
            Wedis.run(conf, jed -> {
                jed.del(key);
            });
        }
    }

    private String _KEY(String hid) {
        String key = String.format("io:hdl:%s", hid);
        return key;
    }

}
