package org.nutz.walnut.ext.util.react.action;

import org.nutz.lang.Each;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.util.Wn;

public class ReactObjClearAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasPath()) {
            return;
        }

        // 准备目标
        WnObj oP = Wn.checkObj(r.io, a.path);

        // 准备查询条件
        WnQuery q = new WnQuery();
        if (a.hasParams()) {
            q.setAllToList(a.params);
        }
        q.setv("pid", oP.id());

        if (a.hasSort()) {
            q.sort(a.sort);
        }
        q.limit(a.limit);
        q.skip(a.skip);

        // 执行更新
        r.io.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) {
                r.io.delete(ele, true);
            }
        });
    }

}
