package com.site0.walnut.ext.data.pebble.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.pebble.PebbleContext;
import com.site0.walnut.ext.data.pebble.PebbleFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

/**
 * `@vars` 设置上下文变量：`-f` 文件 / 参数 / 管道；多次调用与已有变量融合。
 */
public class pebble_vars extends PebbleFilter {

    @Override
    protected void process(WnSystem sys, PebbleContext fc, ZParams params) {
        // 从文件里获得
        if (params.hasString("f")) {
            String ph = params.getString("f");
            WnObj o = sys.io.fetch(null, Wn.normalizeFullPath(ph, sys.session));
            if (null == o) {
                throw Er.create("e.cmd.pebble.vars.noexists", ph);
            }
            fc.putVars(parse(sys.io.readText(o)));
        }
        // 从参数里获得（可以给多个，依次融合）
        else if (params.vals.length > 0) {
            for (String s : params.vals) {
                fc.putVars(parse(s));
            }
        }
        // 从管道获得
        else {
            fc.putVars(parse(sys.in.readAll()));
        }
    }

    private NutMap parse(String str) {
        if (Ws.isBlank(str)) {
            return null;
        }
        return Json.fromJson(NutMap.class, str);
    }
}
