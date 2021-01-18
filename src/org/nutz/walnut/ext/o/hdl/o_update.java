package org.nutz.walnut.ext.o.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class o_update extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 防守
        if (fc.list.isEmpty())
            return;

        // 合并一下
        NutMap meta;

        // 从标准输入读取
        if (params.vals.length == 0) {
            String json = sys.in.readAll();
            meta = Lang.map(json);
        }
        // 合并一下
        else {
            meta = new NutMap();
            for (String str : params.vals) {
                NutMap map = Lang.map(str);
                meta.putAll(map);
            }
        }

        // 执行更新
        if (meta.isEmpty())
            return;

        for (WnObj o : fc.list) {
            sys.io.appendMeta(o, meta);
        }

    }

}
