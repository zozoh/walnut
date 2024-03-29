package com.site0.walnut.ext.util.react.action;

import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.util.Wn;

public class ReactObjUpdateAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasMeta() || (!a.hasPath() && !a.hasTargetId())) {
            return;
        }

        // 准备目标
        String aph = Wn.normalizeFullPath(a.path, r.session);
        WnObj o = r.io.check(null, aph);

        // 批量
        if (a.hasQuery()) {
            WnQuery q = Wn.Q.pid(o);
            q.setAll(a.getQuery());
            List<WnObj> objs = r.io.query(q);
            for (WnObj obj : objs) {
                r.io.appendMeta(obj, a.meta);
            }
        }
        // 仅仅是单个对象
        else {
            r.io.appendMeta(o, a.meta);
        }
    }

}
