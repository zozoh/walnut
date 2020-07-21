package org.nutz.walnut.core.hdl.redis;

import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.walnut.core.HandleInfo;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoMappingFactory;
import org.nutz.walnut.core.hdl.AbstractIoHandleManager;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;

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
                info = Lang.map2Object(map, HandleInfo.class);
            }
            return info;
        });
    }

    @Override
    public void doSave(WnIoHandle h) {
        String key = _KEY(h.getId());
        Wedis.run(conf, jed -> {
            Map<String, String> map = h.toStringMap();
            jed.hmset(key, map);
            if (h.hasTimeout()) {
                int du = h.getTimeoutInSecond();
                jed.expire(key, du);
            }
        });
    }

    @Override
    public void doTouch(WnIoHandle h) {
        if (h.hasTimeout()) {
            String key = _KEY(h.getId());
            Wedis.run(conf, jed -> {
                Map<String, String> map = h.toStringTouchMap();
                jed.hmset(key, map);
                int du = h.getTimeoutInSecond();
                jed.expire(key, du);
            });
        }
    }

    @Override
    public void remove(String hid) {
        String key = _KEY(hid);
        Wedis.run(conf, jed -> {
            jed.del(key);
        });
    }

    private String _KEY(String hid) {
        String key = String.format("io:hdl:%s", hid);
        return key;
    }

}
