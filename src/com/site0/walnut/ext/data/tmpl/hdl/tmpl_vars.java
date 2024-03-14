package com.site0.walnut.ext.data.tmpl.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.tmpl.TmplContext;
import com.site0.walnut.ext.data.tmpl.TmplFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class tmpl_vars extends TmplFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(reset|json)$");
    }

    @Override
    protected void process(WnSystem sys, TmplContext fc, ZParams params) {
        if (params.is("reset") || fc.vars == null) {
            fc.vars = new NutMap();
        }
        boolean asJson = params.is("json");
        String name = params.getString("name");
        String str;
        // 从文件里获得
        if (params.hasString("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            str = sys.io.readText(o);
            setVar(fc, name, str, asJson);
        }
        // 从参数里获得
        else if (params.vals.length > 0) {
            for (String s : params.vals) {
                setVar(fc, name, s, asJson);
                // 名称模式，只设置一次
                if (!Ws.isBlank(name)) {
                    break;
                }
            }
        }
        // 从管道获得
        else {
            str = sys.in.readAll();
            setVar(fc, name, str, asJson);
        }

    }

    void setVar(TmplContext fc, String name, String str, boolean asJson) {
        if (!Ws.isBlank(name)) {
            Object val = asJson ? Json.fromJson(str) : str;
            fc.vars.put(name, val);
        }
        // 看看能不能自动融合
        else if (!Ws.isBlank(str)) {
            NutMap vars = Json.fromJson(NutMap.class, str);
            fc.vars.putAll(vars);
        }
    }

}
