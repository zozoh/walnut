package com.site0.walnut.ext.sys.redis.hdl;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;

public class redis_scan extends RedisFilter {

    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        long _cu = params.val_long(0, 0);
        String cursor = Long.toString(_cu);
        String match = params.get("match");
        int count = params.getInt("count", 0);
        fc.addHandler(new RedisHanlder("SCAN", cursor) {
            public Object run(RedisContext fc, Jedis jed) {
                // 指定了 count
                if (count > 0 || !Ws.isBlank(match)) {
                    ScanParams sp = new ScanParams();
                    if (count > 0) {
                        sp.count(count);
                    }
                    if (!Ws.isBlank(match)) {
                        sp.match(match);
                    }
                    return jed.scan(cursor, sp);
                }
                // 无参数
                return jed.scan(cursor);
            }
        });
    }

}
