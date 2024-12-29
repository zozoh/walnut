package com.site0.walnut.ext.sys.redis.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.ZParams;

import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SetParams;

public class RedisSupport {

    public static GetExParams genGetExParams(ZParams params) {
        if (params.has("ex")) {
            return new GetExParams().ex(params.getLong("ex"));
        }
        if (params.has("px")) {
            return new GetExParams().px(params.getLong("px"));
        }
        if (params.has("exAt")) {
            return new GetExParams().exAt(params.getLong("exAt"));
        }
        if (params.has("pxAt")) {
            return new GetExParams().pxAt(params.getLong("pxAt"));
        }
        return null;
    }
    
    public static SetParams genSetParams(ZParams params) {
        if (params.has("ex")) {
            return new SetParams().ex(params.getLong("ex"));
        }
        if (params.has("px")) {
            return new SetParams().px(params.getLong("px"));
        }
        if (params.has("exAt")) {
            return new SetParams().exAt(params.getLong("exAt"));
        }
        if (params.has("pxAt")) {
            return new SetParams().pxAt(params.getLong("pxAt"));
        }
        return null;
    }

    public static String[] toKeyValue(NutMap map) {
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
        return kvs.toArray(new String[kvs.size()]);
    }

    public static Map<String, String> toStrMap(NutMap map) {
        Map<String, String> re = new TreeMap<>();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String k = en.getKey();
            Object v = en.getValue();
            if (null != v) {
                re.put(k, v.toString());
            }
        }
        return re;
    }
}
