package org.nutz.walnut.ext.util.react.action;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.util.Wn;

public class ReactObjCreateAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasMeta() || !a.hasPath()) {
            return;
        }

        // 准备父目录
        WnObj oP = Wn.checkObj(r.io, a.path);

        // 准备对象
        WnObj o = new WnIoObj();
        o.putAll(a.meta);
        o.setParent(oP);

        // 执行创建
        r.io.create(oP, o);
    }

}
