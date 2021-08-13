package org.nutz.walnut.util.validate.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.mapl.Mapl;
import org.nutz.walnut.util.validate.WnMatch;

public class MapMatch implements WnMatch {

    private Map<String, WnMatch> matchs;

    public MapMatch(Map<String, Object> map) {
        this.matchs = new HashMap<>();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            boolean not = key.startsWith("!");
            if (not) {
                key = key.substring(1).trim();
            }
            WnMatch m = new AutoMatch(val);
            if (not) {
                m = new NotMatch(m);
            }
            matchs.put(key, m);
        }
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public boolean match(Object val) {
        if (null == val)
            return false;
        if (!(val instanceof Map<?, ?>)) {
            return false;
        }

        Map<String, Object> map = ((Map<String, Object>) val);
        for (Map.Entry<String, WnMatch> en : matchs.entrySet()) {
            String key = en.getKey();
            WnMatch m = en.getValue();
            Object v = Mapl.cell(map, key);
            if (!m.match(v)) {
                return false;
            }
        }

        return true;
    }

}
