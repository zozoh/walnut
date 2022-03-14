package org.nutz.walnut.ext.util.react.action;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.ThQuery;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;

public class ReactThingClearAction implements ReactActionHandler {

    @Override
    public void run(ReactActionContext r, ReactAction a) {
        // 防守
        if (!a.hasPath()) {
            return;
        }

        // 准备数据集对象
        WnObj oTs = Wn.checkObj(r.io, r.session, a.path);
        oTs = Things.checkThingSet(oTs);

        // 创建服务类
        String homePath = r.session.getMe().getHomePath();
        WnThingService wts = new WnThingService(r.io, oTs, homePath);

        // 准备查询条件
        ThQuery tq = new ThQuery();
        if (a.hasQuery()) {
            tq.qStr = Json.toJson(a.query);
        }
        if (a.hasSort()) {
            tq.sort = a.sort;
        }
        tq.wp = new WnPager(a.limit, a.skip);

        // 准备必要的参数
        WnExecutable exec = r.runner;
        boolean hard = false;
        Object match = null;

        if (a.hasParams()) {
            hard = a.params.getBoolean("hard");
            match = a.params.get("match");
        }

        // 首先执行查询
        List<WnObj> list = wts.queryList(tq);

        // 收集ID列表
        List<String> ids = new ArrayList<>(list.size());
        for (WnObj o : list) {
            ids.add(o.id());
        }

        // 执行删除
        wts.deleteThing(exec, match, hard, ids);
    }

}
