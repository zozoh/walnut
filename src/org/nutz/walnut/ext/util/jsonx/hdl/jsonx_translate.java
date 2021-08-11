package org.nutz.walnut.ext.util.jsonx.hdl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.bean.WnBeanMapping;

public class jsonx_translate extends JsonXFilter {

    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(mapping|only)$");
    }

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        if (null == fc.obj)
            return;

        String json = null;
        //
        // 得到转换的 Map
        //
        if (params.has("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            json = sys.io.readText(o);
        }
        // 从标准输入获取
        else {
            json = Cmds.getParamOrPipe(sys, params, 0);
        }
        if (Ws.isBlank(json)) {
            return;
        }
        //
        // 高级转换
        //
        if (params.is("mapping")) {
            boolean isOnly = params.is("only");
            NutMap map = Json.fromJson(NutMap.class, json);
            String by = params.getString("by");
            if (!Ws.isBlank(by)) {
                map = map.getAs(by, NutMap.class);
            }
            WnBeanMapping bm = new WnBeanMapping();
            Map<String, NutMap[]> caches = new HashMap<>();
            NutMap vars = sys.session.getVars();
            bm.setFields(map, sys.io, vars, caches);
            fc.obj = bm.translateAny(fc.obj, isOnly);
        }
        //
        // 普通转换
        //
        else {
            NutMap mapping = Lang.map(json);
            Object v2 = Wn.translate(fc.obj, mapping);
            fc.obj = v2;
        }

    }

}
