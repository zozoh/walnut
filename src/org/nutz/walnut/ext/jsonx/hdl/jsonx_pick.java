package org.nutz.walnut.ext.jsonx.hdl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.jsonx.JsonXContext;
import org.nutz.walnut.ext.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.validate.WnMatch;
import org.nutz.walnut.validate.match.AutoEnumStrMatch;

public class jsonx_pick extends JsonXFilter {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 防守
        if (null == fc.obj)
            return;

        // 准备条件
        WnMatch wm = new AutoEnumStrMatch(params.vals);

        // 对于 Map
        if (fc.obj instanceof Map) {
            fc.obj = filterMap((Map<String, Object>) fc.obj, wm);
        }
        // 对于 容器
        else if (fc.obj instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) fc.obj;
            List list = new ArrayList(coll.size());
            for (Object o : coll) {
                Object o2 = filterObj(o, wm);
                list.add(o2);
            }
            fc.obj = list;
        }
        // 对于数组
        else if (fc.obj.getClass().isArray()) {
            int len = Lang.eleSize(fc.obj);
            List list = new ArrayList(len);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(fc.obj, i);
                Object o2 = filterObj(o, wm);
                list.add(o2);
            }
            fc.obj = list;
        }
    }

    @SuppressWarnings({"unchecked"})
    private Object filterObj(Object obj, WnMatch wm) {
        if (obj instanceof Map) {
            return filterMap((Map<String, Object>) obj, wm);
        }
        return obj;
    }

    private NutMap filterMap(Map<String, Object> map, WnMatch wm) {
        NutMap reMap = new NutMap();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            if (wm.match(key)) {
                Object val = en.getValue();
                reMap.put(key, val);
            }
        }
        return reMap;
    }
}
