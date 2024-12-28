package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_hmget extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String key = params.val_check(0);
        String[] fields = new String[params.vals.length - 1];
        System.arraycopy(params.vals, 1, fields, 0, fields.length);
        fc.addHandler(new RedisHanlder("HMGET", key) {
            public Object run(RedisContext fc, Jedis jed) {
                return jed.hmget(key, fields);
            }
        });
    }
}