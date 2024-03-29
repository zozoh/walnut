package com.site0.walnut.ext.util.react.action;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.util.Wn;

public class ReactObjDeleteAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasMeta() || (!a.hasPath() && !a.hasTargetId())) {
            return;
        }

        // 准备目标
        String aph = Wn.normalizeFullPath(a.path, r.session);
        WnObj o = r.io.fetch(null, aph);

        // 如果指明了目标 ID
        if (null == o && a.hasTargetId()) {
            o = r.io.get(a.targetId);
        }

        // 执行更新
        if (null != o) {
            r.io.delete(o, true);
        }
    }

}
