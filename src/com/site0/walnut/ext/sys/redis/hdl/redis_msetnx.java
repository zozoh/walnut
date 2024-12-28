package com.site0.walnut.ext.sys.redis.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.sys.redis.RedisContext;
import com.site0.walnut.ext.sys.redis.RedisFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

import redis.clients.jedis.Jedis;

public class redis_msetnx extends RedisFilter {
    @Override
    protected void process(WnSystem sys, RedisContext fc, ZParams params) {
        NutMap map = params.getMap("map");
        if (null != map && !map.isEmpty()) {
            List<String> kvs = new ArrayList<>(map.size() * 2);
            for (Map.Entry<String, Object> en : map.entrySet()) {
                String k = en.getKey();
                Object v = en.getValue();
                kvs.add(k);
                if (null == v) {
                    kvs.add("<null>");
                } else {
                    kvs.add(v.toString());
                }
            }
            String[] kvArray = kvs.toArray(new String[kvs.size()]);
            String key = Ws.join(map.keySet(), ",");
            fc.addHandler(new RedisHanlder("MSET", key) {
                public Object run(RedisContext fc, Jedis jed) {
                    return jed.msetnx(kvArray);
                }
            });
        }
    }
}