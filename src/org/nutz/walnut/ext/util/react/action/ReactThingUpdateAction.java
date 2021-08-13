package org.nutz.walnut.ext.util.react.action;

import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.util.Wn;

public class ReactThingUpdateAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasMeta() || !a.hasPath() || !a.hasTargetId()) {
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
        Object match = null;

        if (a.hasParams()) {
            match = a.params.get("match");
        }

        // 执行
        wts.updateThing(a.targetId, a.meta, exec, match);
    }

}
