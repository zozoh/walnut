package com.site0.walnut.util.validate.impl;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.each.WnBreakException;
import com.site0.walnut.util.each.WnEachIteratee;
import com.site0.walnut.util.validate.WnMatch;

public class MapFindInArrayMatch implements WnMatch {

    private WnMatch m;

    private boolean not;

    public MapFindInArrayMatch(Map<String, Object> map) {
        NutMap m2 = NutMap.WRAP(map);
        m = AutoMatch.parse(m2.get("matchBy"));
        not = m2.getBoolean("not");
    }

    @Override
    public boolean match(Object val) {
        boolean[] re = new boolean[1];
        re[0] = false;
        Wlang.each(val, new WnEachIteratee<Object>() {
            public void invoke(int index, Object ele, Object src) throws WnBreakException {
                if (m.match(ele)) {
                    re[0] = true;
                    throw new WnBreakException();
                }
            }
        });
        return re[0] ^ not;
    }

}
