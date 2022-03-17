package org.nutz.walnut.ext.data.wf.hdl;

import java.util.Collection;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class wf_var extends WfFilter {

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 分析参数
        String varName = params.val(0);
        String fPath = params.getString("f");
        String oPath = params.getString("o");
        String pick = params.getString("pick");

        // 变量过滤器
        WnMatch mKey = AutoMatch.parse(pick);

        // 首先加载变量
        NutMap loadVars = new NutMap();

        // 加载参数变量
        for (int i = 1; i < params.vals.length; i++) {
            joinVars(loadVars, mKey, params.val(i));
        }

        // 其次加载对象内容
        if (!Ws.isBlank(fPath)) {
            WnObj o = Wn.checkObj(sys, fPath);
            String json = sys.io.readText(o);
            joinVars(loadVars, mKey, json);
        }

        // 再次，加载对象元数据
        if (!Ws.isBlank(oPath)) {
            WnObj o = Wn.checkObj(sys, fPath);
            joinVars(loadVars, mKey, o);
        }

        // 记入上下文
        if (!Ws.isBlank(varName)) {
            fc.vars.put(varName, loadVars);
        }
        // 否则全部融合进上下文
        else {
            fc.vars.putAll(loadVars);
        }
    }

    @SuppressWarnings("unchecked")
    private void joinVars(NutMap vars, WnMatch mKey, String json) {
        Object v = Json.fromJson(json);

        // 防守
        if (null == v) {
            return;
        }

        NutMap map;
        // 如果是集合
        if (v instanceof Collection<?>) {
            int n = ((Collection<?>) v).size();
            map = Wlang.map("list", v).setv("count", n);
        }
        // 其他的当作对象来处理
        else {
            map = NutMap.WRAP((Map<String, Object>) v);
        }

        joinVars(vars, mKey, map);
    }

    private void joinVars(NutMap vars, WnMatch mKey, NutBean bean) {
        if (null == bean || bean.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> en : bean.entrySet()) {
            String key = en.getKey();
            if (mKey.match(key)) {
                Object val = en.getValue();
                vars.put(key, val);
            }
        }
    }

}
