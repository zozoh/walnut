package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.Map;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;

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
