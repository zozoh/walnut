package org.nutz.walnut.util.validate.impl;

import java.util.Map;

import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;

public class ExistsMatch implements WnMatch {

    private boolean not;

    private String[] keyPath;

    private int lastI;

    public ExistsMatch(String key, boolean not) {
        this.keyPath = Ws.splitIgnoreBlank(key, "[.]");
        this.lastI = this.keyPath.length - 1;
        this.not = not;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean match(Object val) {
        boolean re = false;
        if ((val instanceof Map<?, ?>)) {
            Map<String, Object> map = ((Map<String, Object>) val);
            re = this.matchByKey(map, 0);
        }
        return re ^ not;
    }

    @SuppressWarnings("unchecked")
    private boolean matchByKey(Map<String, Object> map, int index) {
        if (this.keyPath.length <= 0 || null == map) {
            return false;
        }
        String key = this.keyPath[index];
        // 最后一个 key ，是判断是否存在
        if (index >= lastI) {
            return map.containsKey(key);
        }
        // 中间状态
        Object val = map.get(key);
        if ((val instanceof Map<?, ?>)) {
            Map<String, Object> map2 = ((Map<String, Object>) val);
            return matchByKey(map2, index + 1);
        }
        return false;
    }

}
