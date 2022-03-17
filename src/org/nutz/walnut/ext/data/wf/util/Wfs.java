package org.nutz.walnut.ext.data.wf.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutMap;

public abstract class Wfs {

    public static final String K_CURRENT_NAME = "CURRENT_NAME";
    public static final String K_NEXT_NAME = "NEXT_NAME";

    public static class AtmlResult {
        public List<NutMap> list;
        public boolean asList;
    }

    public static AtmlResult anyToMetaList(Object input) {
        AtmlResult re = new AtmlResult();
        if (input instanceof Collection<?>) {
            re.asList = true;
            Collection<?> col = (Collection<?>) input;
            re.list = new ArrayList<>(col.size());
            for (Object it : col) {
                NutMap map = anyToMeta(it);
                re.list.add(map);
            }
        }
        // 否则直接转换
        else {
            re.asList = false;
            re.list = new ArrayList<>(1);
            NutMap map = anyToMeta(input);
            re.list.add(map);
        }
        return re;
    }

    @SuppressWarnings("unchecked")
    public static NutMap anyToMeta(Object any) {
        return NutMap.WRAP((Map<String, Object>) any);
    }
}
