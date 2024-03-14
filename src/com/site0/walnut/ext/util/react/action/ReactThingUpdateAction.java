package com.site0.walnut.ext.util.react.action;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.api.WnExecutable;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.util.ThQuery;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPager;

public class ReactThingUpdateAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasMeta() || !a.hasPath()) {
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
        if (a.hasTargetId()) {
            wts.updateThing(a.targetId, a.meta, exec, match);
        }
        // 一个至多个
        else if (a.hasQuery()) {
            ThQuery tq = new ThQuery();
            tq.qStr = Json.toJson(a.query);
            if (a.hasParams()) {
                int limit = a.params.getInt("limit", 0);
                int skip = a.params.getInt("skip", 0);
                if (limit > 0) {
                    tq.wp = new WnPager(limit, skip);
                }
            }

            // 首先执行查询
            List<WnObj> list = wts.queryList(tq);

            // 收集ID列表
            List<String> ids = new ArrayList<>(list.size());
            for (WnObj o : list) {
                ids.add(o.id());
            }

            // 执行批量更新
            for (String id : ids) {
                wts.updateThing(id, a.meta, exec, match);
            }
        }
    }

}
