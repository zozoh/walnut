package com.site0.walnut.ext.data.o.hdl;

import java.util.Map;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class o_update extends OFilter {

    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(explain)$");
    }

    @SuppressWarnings("unchecked")
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
            // 错误字符串，打印到错误输出流
            if (json.startsWith("e.")) {
                sys.err.print(json);
                return;
            }
            meta = Wlang.map(json);
        }
        // 合并一下
        else {
            meta = new NutMap();
            for (String str : params.vals) {
                NutMap map = Wlang.map(str);
                meta.putAll(map);
            }
        }

        // 啥都不用干？
        if (meta.isEmpty())
            return;

        // 展开动态赋值
        if (params.is("explain")) {
            Object o = Wn.explainObj(fc.vars, meta);
            meta = NutMap.WRAP((Map<String, Object>) o);
        }

        // 元数据的宏搞一下
        Wn.explainMetaMacro(meta);

        // 执行更新
        for (WnObj o : fc.list) {
            sys.io.appendMeta(o, meta);
        }

    }

}
