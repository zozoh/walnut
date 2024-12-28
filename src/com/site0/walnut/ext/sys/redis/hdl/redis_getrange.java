package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_getrange extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String key = params.val_check(0);
        long start = params.val_check_long(1);
        long end = params.val_check_long(2);
        fc.addHandler(new RedisHanlder("GETRANGE", key) {
            public Object run(RedisContext fc, Jedis jed) {
                return jed.getrange(key, start, end);
            }
        });
    }
}