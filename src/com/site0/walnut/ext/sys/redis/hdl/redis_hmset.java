package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.ext.sys.redis.support.RedisSupport;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

import java.util.Map;

import org.nutz.lang.util.NutMap;

public class redis_hmset extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        NutMap map = params.getMap("map");
        if (null != map && !map.isEmpty()) {
            Map<String,String> hash = RedisSupport.toStrMap(map);
            String key = Ws.join(map.keySet(), ",");
            fc.addHandler(new RedisHanlder("HMSET", key) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.hmset(key, hash);
                }
            });
        }
    }
}