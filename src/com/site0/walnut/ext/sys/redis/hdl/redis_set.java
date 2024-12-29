package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.ext.sys.redis.support.RedisSupport;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

public class redis_set extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String key = params.val_check(0);
        String val = params.val_check(1);
        SetParams setParams = RedisSupport.genSetParams(params);
        if (null == setParams) {
            fc.addHandler(new RedisHanlder("SET", key) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.set(key, val);
                }
            });
        }
        // 指定参数
        else {
            fc.addHandler(new RedisHanlder("SET", key) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.set(key, val, setParams);
                }
            });
        }
    }
}