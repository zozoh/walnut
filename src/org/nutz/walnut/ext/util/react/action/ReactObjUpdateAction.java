package org.nutz.walnut.ext.util.react.action;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.util.Wn;

public class ReactObjUpdateAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasMeta() || (!a.hasPath() && !a.hasTargetId())) {
            return;
        }

        // 准备目标
        WnObj o;
        // 采用路径获取
        if (a.hasPath()) {
            o = Wn.checkObj(r.io, a.path);
        }
        // 采用 ID 获取
        else {
            o = r.io.checkById(a.targetId);
        }

        // 执行更新
        r.io.appendMeta(o, a.meta);
    }

}
