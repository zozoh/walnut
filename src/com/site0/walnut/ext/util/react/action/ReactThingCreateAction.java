package com.site0.walnut.ext.util.react.action;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.WnExecutable;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.ext.util.react.bean.ReactAction;
import com.site0.walnut.util.Wn;

public class ReactThingCreateAction implements ReactActionHandler {

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
        String homePath = r.session.getUser().getHomePath();
        WnThingService wts = new WnThingService(r.io, oTs, homePath);

        // 准备必要的参数
        WnExecutable exec = r.runner;
        String ukey = null;
        String afterCmd = null;
        NutMap fixedMeta = null;

        if (a.hasParams()) {
            ukey = a.params.getString("unique", null);
            afterCmd = a.params.getString("after", null);
            fixedMeta = a.params.getAs("fixed", NutMap.class);
        }

        // 执行
        wts.createThing(a.meta, ukey, fixedMeta, exec, afterCmd);
    }

}
