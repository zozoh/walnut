package com.site0.walnut.ext.util.react.hdl;

import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.util.react.ReactContext;
import com.site0.walnut.ext.util.react.ReactFilter;
import com.site0.walnut.ext.util.react.action.ReactActionContext;
import com.site0.walnut.ext.util.react.action.ReactActionHandler;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.ext.util.react.bean.ReactItem;
import com.site0.walnut.ext.util.react.util.WnReacts;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class react_happen extends ReactFilter {

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, ReactContext fc, ZParams params) {
        List<ReactItem> items = fc.config.getItems();

        // 准备一下动作执行器上下文
        ReactActionContext r = new ReactActionContext(sys, fc.vars);

        // 依次处理项目
        for (ReactItem item : items) {
            // 检查
            if (!item.hasActions() || !item.isMatch(fc.vars)) {
                fc.result.put(item.getDisplayName(), 0);
                continue;
            }

            // 增加自定义变量
            if (item.hasVars()) {
                for (Map.Entry<String, Object> ven : item.getVars().entrySet()) {
                    String varName = ven.getKey();
                    Object varTmpl = ven.getValue();
                    Object varVal = Wn.explainObj(fc.vars, varTmpl);
                    if (varVal instanceof Map<?, ?>) {
                        NutMap varMap = NutMap.WRAP((Map<String, Object>) varVal);
                        String cmdText = varMap.getString("exec");
                        if (!Ws.isBlank(cmdText)) {
                            String re = sys.exec2(cmdText);
                            if (varMap.is("type", "json")) {
                                varVal = Json.fromJson(re);
                            } else {
                                varVal = re;
                            }
                        }
                    }
                    fc.vars.put(varName, varVal);
                }
            }

            // 执行动作
            int count = 0;
            for (ReactAction a : item.getActions()) {
                // 获取执行器
                ReactActionHandler hdl = WnReacts.getActionHandler(a);
                if (null == hdl) {
                    throw Er.create("e.cmd.react.happen.ActionNotFound", a.toString());
                }

                // 根据上下文，处理动作对象属性
                a.explain(fc.vars);

                // 执行
                hdl.run(r, a);
                count++;
            }

            // 记入结果
            fc.result.put(item.getDisplayName(), count);
        }
    }

}
