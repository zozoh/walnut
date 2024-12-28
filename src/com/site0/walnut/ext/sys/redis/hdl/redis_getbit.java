package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_getbit extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String key = params.val_check(0);
        long offset = params.val_check_long(1);
        fc.addHandler(new RedisHanlder("GETBIT", key) {
            public Object run(RedisContext fc, Jedis jed) {
                return jed.getbit(key, offset);
            }
        });
    }
}