package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.util.WnObjTrans;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.bean.WnBeanMapping;
import com.site0.walnut.util.explain.WnExplains;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

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

            // 准备字段过滤器
            Object keysBy = params.getString("keys", null);
            Object namesBy = params.getString("names", null);
            WnMatch keys = null;
            WnMatch names = null;
            if (null != keysBy) {
                keys = AutoMatch.parse(keysBy);
            }
            if (null != namesBy) {
                names = AutoMatch.parse(namesBy);
            }

            // 准备映射规则
            WnBeanMapping bm = new WnBeanMapping();
            bm.setPickKeys(keys);
            bm.setPickNames(names);

            // 编译规则
            Map<String, NutMap[]> caches = new HashMap<>();
            NutMap vars = sys.session.getVars();
            bm.setFields(map, sys.io, vars, caches);

            // 执行映射
            fc.obj = bm.translateAny(fc.obj, isOnly);

            WnObjTrans trans = new WnObjTrans();
            trans.loadExplainFrom(sys, params);
            if (trans.hasExplain()) {
                fc.obj = WnExplains.explainAny(trans.getExplain(), fc.obj);
            }
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
