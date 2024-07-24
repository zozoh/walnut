package com.site0.walnut.ext.data.val.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.ext.data.val.ValContext;
import com.site0.walnut.ext.data.val.ValFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;

public class val_vars extends ValFilter {

    @Override
    protected void process(WnSystem sys, ValContext fc, ZParams params) {
        // 从标准输入读取
        if (params.vals.length == 0) {
            NutMap map = _read_std_input_as_map(sys);
            fc.context.putAll(map);
        }
        // 逐个解析参数
        for (String val : params.vals) {
            if (val.startsWith("=") || val.startsWith(":")) {
                String key = val.substring(1).trim();
                if ("..".equals(key)) {
                    NutMap map = _read_std_input_as_map(sys);
                    fc.context.putAll(map);
                } else {
                    NutMap map = _read_std_input_as_map(sys);
                    fc.context = map;
                }
            } else {
                NutMap map = Wlang.map(val);
                fc.context.putAll(map);
            }
        }
    }

    private NutMap _read_std_input_as_map(WnSystem sys) {
        String json = sys.in.readAll();
        NutMap map = Json.fromJson(NutMap.class, json);
        return map;
    }

}
