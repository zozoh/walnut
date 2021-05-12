package org.nutz.walnut.ext.util.jsonx.hdl;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.ZParams;

public class jsonx_list2map extends JsonXFilter {

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        String key = params.val_check(0);
        String val = params.val(1);
        NutMap map = new NutMap();
        Wlang.each(fc.obj, (index, ele, src) -> {
            if (null == ele) {
                return;
            }
            if (ele instanceof Map<?, ?>) {
                NutMap o = NutMap.WRAP((Map<String, Object>) ele);
                String k = o.getString(key);
                if (null == k) {
                    return;
                }
                // 映射成名值对
                if (null != val) {
                    Object v = o.get(val);
                    map.put(k, v);
                }
                // 值用对象本身代替
                else {
                    map.put(k, o);
                }
            }
        });
        fc.obj = map;
    }

}
