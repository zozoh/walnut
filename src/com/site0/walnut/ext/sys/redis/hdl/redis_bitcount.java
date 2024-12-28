package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_bitcount extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String key = params.val_check(0);
        if (params.vals.length == 1) {
            fc.addHandler(new RedisHanlder("BITCOUNT", key) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.bitcount(key);
                }
            });
        } else if (params.vals.length == 3) {
            long start = params.val_check_long(1);
            long end = params.val_check_long(2);
            fc.addHandler(new RedisHanlder("BITCOUNT", key) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.bitcount(key, start, end);
                }
            });
        } else {
            throw Er.create("e.cmd.redis.bitcount.LackParams", "Usage: BITCOUNT key [start end]");
        }
    }
}