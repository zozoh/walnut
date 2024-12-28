package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_exists extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String[] keys = params.vals;
        if (keys.length > 0) {
            fc.addHandler(new RedisHanlder("EXISTS", Ws.join(keys, ",")) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.exists(keys);
                }
            });
        }
    }
}
