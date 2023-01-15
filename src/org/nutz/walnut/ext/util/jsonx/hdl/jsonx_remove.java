package org.nutz.walnut.ext.util.jsonx.hdl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMultiStrMatch;

public class jsonx_remove extends JsonXFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(value)$");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 防守
        if (null == fc.obj)
            return;

        boolean isForValue = params.is("value");

        // 准备条件
        WnMatch wm = new AutoMultiStrMatch(params.vals);

        // 对于 Map
        if (fc.obj instanceof Map) {
            fc.obj = filterMap((Map<String, Object>) fc.obj, wm, isForValue);
        }
        // 对于 容器
        else if (fc.obj instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) fc.obj;
            List list = new ArrayList(coll.size());
            for (Object o : coll) {
                Object o2 = filterObj(o, wm, isForValue);
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
                Object o2 = filterObj(o, wm, isForValue);
                list.add(o2);
            }
            fc.obj = list;
        }
    }

    @SuppressWarnings({"unchecked"})
    private Object filterObj(Object obj, WnMatch wm, boolean isForValue) {
        if (obj instanceof Map) {
            return filterMap((Map<String, Object>) obj, wm, isForValue);
        }
        return obj;
    }

    private NutMap filterMap(Map<String, Object> map, WnMatch wm, boolean isForValue) {
        NutMap reMap = new NutMap();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (isForValue) {
                if (!wm.match(val)) {
                    reMap.put(key, val);
                }
            } else if (!wm.match(key)) {
                reMap.put(key, val);
            }
        }
        return reMap;
    }

}
