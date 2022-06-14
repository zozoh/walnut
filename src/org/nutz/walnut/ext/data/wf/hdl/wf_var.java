package org.nutz.walnut.ext.data.wf.hdl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.wf.WfContext;
import org.nutz.walnut.ext.data.wf.WfFilter;
import org.nutz.walnut.ext.data.wf.util.WfVarSelectItem;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class wf_var extends WfFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(select)$");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 分析参数
        String varName = params.val(0);
        String fPath = params.getString("f");
        String oPath = params.getString("o");
        String pick = params.get("pick");

        //
        // 选择模式，仅仅接受 -f 以及参数列表
        //
        if (params.is("select")) {
            // 路径参数
            List<WfVarSelectItem> list = new LinkedList<>();
            for (int i = 1; i < params.vals.length; i++) {
                String json = params.val(i);
                joinVarSelectItems(list, json);
            }

            // 文件内容
            if (!Ws.isBlank(fPath)) {
                String json = readFileJson(sys, fc, fPath);
                joinVarSelectItems(list, json);
            }

            // 循环判断，具体采用哪个值
            for (WfVarSelectItem li : list) {
                if (li.isMatch(fc.vars)) {
                    Object val = li.getValue();
                    // 没有声明变量名，那么必须是 map
                    if (Ws.isBlank(varName)) {
                        if (!(val instanceof Map<?, ?>)) {
                            throw Er.create("e.cmd.wf_var.NeedVarName", li.toString());
                        }
                        NutMap map = NutMap.WRAP((Map<String, Object>) val);
                        fc.vars.putAll(map);
                    }
                    // 否则设置
                    else {
                        fc.vars.put(varName, val);
                    }
                    // 退出循环
                    break;
                }
            }

            return;
        }

        //
        // 直接设置模式
        //

        // 变量过滤器
        WnMatch mKey = AutoMatch.parse(pick, true);

        // 首先加载变量
        NutMap loadVars = new NutMap();

        // 加载参数变量
        for (int i = 1; i < params.vals.length; i++) {
            joinVars(loadVars, mKey, params.val(i));
        }

        // 其次加载对象内容
        if (!Ws.isBlank(fPath)) {
            String json = readFileJson(sys, fc, fPath);
            joinVars(loadVars, mKey, json);
        }

        // 再次，加载对象元数据
        if (!Ws.isBlank(oPath)) {
            String aph = Wn.explainObj(fc.vars, oPath).toString();
            WnObj o = Wn.checkObj(sys, aph);
            joinVars(loadVars, mKey, o);
        }

        // 挑选模式

        // 记入上下文
        if (!Ws.isBlank(varName)) {
            fc.vars.put(varName, loadVars);
        }
        // 否则全部融合进上下文
        else {
            fc.vars.putAll(loadVars);
        }
    }

    private void joinVarSelectItems(List<WfVarSelectItem> list, String json) {
        if (Ws.isQuoteBy(json, '[', ']')) {
            List<WfVarSelectItem> items = Json.fromJsonAsList(WfVarSelectItem.class, json);
            list.addAll(items);
        }
        // 那么就是内容本身咯
        else {
            WfVarSelectItem item = Json.fromJson(WfVarSelectItem.class, json);
            list.add(item);
        }
    }

    private String readFileJson(WnSystem sys, WfContext fc, String fPath) {
        String aph = Wn.explainObj(fc.vars, fPath).toString();
        WnObj o = Wn.checkObj(sys, aph);
        String json = sys.io.readText(o);
        return json;
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
            // 收缩在一个集合里
            int n = ((Collection<?>) v).size();
            map = Wlang.map("list", v).setv("count", n);
            joinVars(vars, mKey, map);
            return;
        }
        //
        // 其他的当作对象来处理
        //
        map = NutMap.WRAP((Map<String, Object>) v);
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
