package org.nutz.walnut.ext.thing.impl;

import java.util.List;

import org.nutz.lang.util.Region;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.Wn;

public class CleanTmpFileAction extends ThingAction<List<WnObj>> {

    public int limit;

    @Override
    public List<WnObj> invoke() {
        // 准备查询条件
        WnObj oTmpd = Things.dirTsTmpFile(io, oTs);
        long now = Wn.now();
        WnQuery q = Wn.Q.pid(oTmpd);
        q.setv("expi", Region.Longf("(,%d]", now));
        q.asc("lm"); // 最后活跃时间升序清理
        if (limit > 0)
            q.limit(limit);

        // 来吧
        List<WnObj> list = io.query(q);
        for (WnObj o : list) {
            io.delete(o);
        }

        // 返回
        return list;
    }

}
