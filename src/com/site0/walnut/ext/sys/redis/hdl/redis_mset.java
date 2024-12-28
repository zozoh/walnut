package com.site0.walnut.ext.sys.redis.hdl;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.ext.sys.redis.support.RedisSupport;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_mset extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        NutMap map = params.getMap("map");
        if (null != map && !map.isEmpty()) {
            String[] kvArray = RedisSupport.toKeyValue(map);
            String key = Ws.join(map.keySet(), ",");
            fc.addHandler(new RedisHanlder("MSET", key) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.mset(kvArray);
                }
            });
        }
    }
}