package com.site0.walnut.ext.util.react.action;

import com.site0.walnut.api.WnExecutable;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.util.Wn;

public class ReactThingDeleteAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasPath() || !a.hasTargetId()) {
            return;
        }

        // 准备数据集对象
        WnObj oTs = Wn.checkObj(r.io, r.session, a.path);
        oTs = Things.checkThingSet(oTs);

        // 创建服务类
        String homePath = r.session.getMe().getHomePath();
        WnThingService wts = new WnThingService(r.io, oTs, homePath);

        // 准备必要的参数
        WnExecutable exec = r.runner;
        boolean hard = false;
        Object match = null;

        if (a.hasParams()) {
            hard = a.params.getBoolean("hard");
            match = a.params.get("match");
        }

        // 执行
        wts.deleteThing(exec, match, hard, a.targetId);
    }

}
