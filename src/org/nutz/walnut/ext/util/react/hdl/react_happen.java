package org.nutz.walnut.ext.util.react.hdl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.util.react.ReactContext;
import org.nutz.walnut.ext.util.react.ReactFilter;
import org.nutz.walnut.ext.util.react.action.ReactActionContext;
import org.nutz.walnut.ext.util.react.action.ReactActionHandler;
import org.nutz.walnut.ext.util.react.action.ReactCommandAction;
import org.nutz.walnut.ext.util.react.action.ReactJsAction;
import org.nutz.walnut.ext.util.react.action.ReactObjClearAction;
import org.nutz.walnut.ext.util.react.action.ReactObjCreateAction;
import org.nutz.walnut.ext.util.react.action.ReactObjDeleteAction;
import org.nutz.walnut.ext.util.react.action.ReactObjUpdateAction;
import org.nutz.walnut.ext.util.react.action.ReactThingClearAction;
import org.nutz.walnut.ext.util.react.action.ReactThingCreateAction;
import org.nutz.walnut.ext.util.react.action.ReactThingDeleteAction;
import org.nutz.walnut.ext.util.react.action.ReactThingUpdateAction;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.ext.util.react.bean.ReactItem;
import org.nutz.walnut.ext.util.react.bean.ReactType;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class react_happen extends ReactFilter {

    static Map<ReactType, ReactActionHandler> actions = new HashMap<>();

    static {
        actions.put(ReactType.thing_create, new ReactThingCreateAction());
        actions.put(ReactType.thing_update, new ReactThingUpdateAction());
        actions.put(ReactType.thing_delete, new ReactThingDeleteAction());
        actions.put(ReactType.thing_clear, new ReactThingClearAction());
        actions.put(ReactType.obj_create, new ReactObjCreateAction());
        actions.put(ReactType.obj_update, new ReactObjUpdateAction());
        actions.put(ReactType.obj_delete, new ReactObjDeleteAction());
        actions.put(ReactType.obj_clear, new ReactObjClearAction());
        actions.put(ReactType.exec, new ReactCommandAction());
        actions.put(ReactType.jsc, new ReactJsAction());
    }

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

            // 执行动作
            int count = 0;
            for (ReactAction a : item.getActions()) {
                // 获取执行器
                ReactActionHandler hdl = actions.get(a.type);
                if (null == hdl) {
                    throw Er.create("e.cmd.react.HdlNotFound", a.toString());
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
