package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_info extends RedisFilter {

    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        String section = params.val(0);
        fc.addHandler(new RedisHanlder("INFO", null) {
            public Object run(RedisContext fc, Jedis jed) {
                if (null == section) {
                    return jed.info();
                }
                return jed.info(section);
            }
        });

    }

}