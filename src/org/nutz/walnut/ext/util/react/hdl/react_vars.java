package org.nutz.walnut.ext.util.react.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.util.react.ReactContext;
import org.nutz.walnut.ext.util.react.ReactFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.ZParams;

public class react_vars extends ReactFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(clear)$");
    }

    @Override
    protected void process(WnSystem sys, ReactContext fc, ZParams params) {
        // 读取变量
        if (params.vals.length == 0) {
            String json = sys.in.readAll();
            NutMap vars = Wlang.map(json);
            fc.vars.putAll(vars);
        }
        // 逐个加入
        else {
            for (String json : params.vals) {
                NutMap vars = Wlang.map(json);
                fc.vars.putAll(vars);
            }
        }

        //
        // 清除变量
        //
        if (params.is("clear")) {
            fc.vars.clear();
        }
        // 移除变量
        else if (params.has("remove")) {
            String[] names = params.getAs("remove", String[].class);
            for (String name : names) {
                fc.vars.remove(name);
            }
        }
    }

}
