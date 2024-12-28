package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.ext.sys.redis.support.RedisSupport;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.GetExParams;

public class redis_getex extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String key = params.val_check(0);
        GetExParams gp = RedisSupport.genGetExParams(params);
        if (gp != null) {
            fc.addHandler(new RedisHanlder("GETDEL", key) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.getEx(key, gp);
                }
            });
        }
    }
}