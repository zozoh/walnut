package org.nutz.walnut.ext.data.sqlx.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.sqlx.SqlxContext;
import org.nutz.walnut.ext.data.sqlx.SqlxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.ZParams;

public class sqlx_vars extends SqlxFilter {

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        // 从标准输入读取
        if (params.vals.length == 0) {
            String json = sys.in.readAll();
            NutMap map = Json.fromJson(NutMap.class, json);
            fc.vars.putAll(map);
        }
        // 逐个解析参数
        else {
            for (String val : params.vals) {
                if ("~STDIN~".equals(val)) {
                    String json = sys.in.readAll();
                    NutMap map = Json.fromJson(NutMap.class, json);
                    fc.vars.putAll(map);
                } else {
                    NutMap map = Wlang.map(val);
                    fc.vars.putAll(map);
                }
            }
        }
    }
}
