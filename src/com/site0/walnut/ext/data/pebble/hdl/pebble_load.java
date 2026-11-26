package com.site0.walnut.ext.data.pebble.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.pebble.PebbleContext;
import com.site0.walnut.ext.data.pebble.PebbleFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

/**
 * `@load` 读取模板内容：`-f` 文件 / 参数 / 管道。
 */
public class pebble_load extends PebbleFilter {

    @Override
    protected void process(WnSystem sys, PebbleContext fc, ZParams params) {
        // 从文件里获得
        if (params.hasString("f")) {
            String ph = params.getString("f");
            String path = Wn.normalizeFullPath(ph, sys.session);
            WnObj o = sys.io.fetch(null, path);
            if (null == o) {
                throw Er.create("e.cmd.pebble.tpl.noexists", ph);
            }
            fc.tplPath = path;
            fc.tmpl = null;
        }
        // 从参数里获得
        else if (params.vals.length > 0) {
            fc.tplPath = null;
            fc.tmpl = params.val(0);
        }
        // 从管道获得
        else {
            fc.tplPath = null;
            fc.tmpl = sys.in.readAll();
        }
    }
}
