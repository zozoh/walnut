package com.site0.walnut.ext.util.react.action;

import org.nutz.lang.Each;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.util.Wn;

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
        if (a.hasQuery()) {
            q.setAllToList(a.query);
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
