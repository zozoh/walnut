package com.site0.walnut.util.validate.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.mapl.Mapl;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.validate.WnMatch;
import org.nutz.web.WebException;

public class MapMatch implements WnMatch {

    /**
     * 超出集合的字段都算错
     */
    private boolean onlyFields;

    /**
     * 空值不检查
     */
    private boolean ignoreNil;

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
            WnMatch m = null;
            // {key:"[EXISTS]"}
            if (null != val) {
                // 存在
                if (val.equals("[EXISTS]")) {
                    m = new ExistsMatch(key, not);
                }
                // 不存在
                else if (val.equals("![EXISTS]")) {
                    not = !not;
                    m = new ExistsMatch(key, not);
                }
            }
            // 自动匹配
            if (null == m) {
                m = new AutoMatch(val);
                if (not) {
                    m = new NotMatch(m);
                }
            }
            matchs.put(key, m);
        }
    }

    @Override
    public boolean match(Object val) {
        List<WebException> err = this.matchErr(val);
        return err.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<WebException> matchErr(Object val) {

        if (null == val)
            return Wlang.list(Er.create("e.v.isNil"));
        if (!(val instanceof Map<?, ?>)) {
            return Wlang.list(Er.create("e.v.shouldBeMap"));
        }
        Map<String, Object> map = ((Map<String, Object>) val);

        List<WebException> re = new ArrayList<>();

        // 看看有没有字段超范围
        if (this.onlyFields) {
            for (Map.Entry<String, Object> en : map.entrySet()) {
                String key = en.getKey();
                WnMatch m = matchs.get(key);
                if (null == m) {
                    re.add(Er.create("e.v.unkownKey", key));
                }
            }
        }
        // 检查值合法性，不在检查范围的字段放过
        for (Map.Entry<String, WnMatch> en : matchs.entrySet()) {
            WnMatch m = en.getValue();
            String key = en.getKey();
            Object v;
            if (m instanceof ExistsMatch) {
                v = map;
            } else {
                v = Mapl.cell(map, key);
            }
            if (null == v && this.ignoreNil) {
                continue;
            }
            if (!m.match(v)) {
                re.add(Er.create("e.v.invalid", key));
            }
        }
        return re;
    }

    public boolean isOnlyFields() {
        return onlyFields;
    }

    public void setOnlyFields(boolean onlyFields) {
        this.onlyFields = onlyFields;
    }

    public boolean isIgnoreNil() {
        return ignoreNil;
    }

    public void setIgnoreNil(boolean ignoreNil) {
        this.ignoreNil = ignoreNil;
    }

}
