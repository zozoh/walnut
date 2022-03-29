package org.nutz.walnut.ext.data.o.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class o_update extends OFilter {
    
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(explain)$");
    }

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

        // 啥都不用干？
        if (meta.isEmpty())
            return;

        // 元数据的宏搞一下
        Wn.explainMetaMacro(meta);

        // 执行更新
        for (WnObj o : fc.list) {
            sys.io.appendMeta(o, meta);
        }

    }

}
