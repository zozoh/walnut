package org.nutz.walnut.ext.net.sendmail.hdl;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.ThQuery;
import org.nutz.walnut.ext.net.sendmail.SendmailContext;
import org.nutz.walnut.ext.net.sendmail.SendmailFilter;
import org.nutz.walnut.ext.net.sendmail.bean.WnMailReceiver;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public abstract class sendmail_to_by_file extends SendmailFilter {

    @Override
    protected void process(WnSystem sys, SendmailContext fc, ZParams params) {
        String rph = params.val_check(0);
        WnObj o = Wn.checkObj(sys, rph);

        // 准备结果列表
        List<WnObj> objs = null;

        // 如果是目录
        if (o.isDIR()) {
            // 读取过滤条件
            String match = params.getString("match");
            if (!Strings.isBlank(match)) {

                // 如果是 ThingSet
                if (o.isType("thing_set")) {
                    WnThingService wts = new WnThingService(sys, o);
                    ThQuery tq = new ThQuery();
                    tq.qStr = match;
                    tq.autoObj = false;
                    tq.needContent = false;
                    objs = wts.queryList(tq);
                }
                // 普通目录
                else {
                    WnQuery q = Wn.Q.pid(o);
                    NutMap map = Lang.map(match);
                    q.setAll(map);
                    objs = sys.io.query(q);
                }
            }
        }
        // 如果是文件
        else {
            objs = Lang.list(o);
        }

        // 空对象，无视
        if (null == objs || objs.isEmpty()) {
            return;
        }

        // 准备映射
        String mapping = params.getString("mapping");
        NutMap transMap;
        if (!Strings.isBlank(mapping)) {
            transMap = Lang.map(mapping);
        } else {
            transMap = Lang.map("name:'=name',account:'=email'");
        }

        // 根据结果，生成对象列表
        WnMailReceiver[] res = new WnMailReceiver[objs.size()];
        int i = 0;
        for (WnObj obj : objs) {
            NutBean bean = (NutBean) Wn.explainObj(obj, transMap);
            WnMailReceiver r = new WnMailReceiver(bean);
            res[i++] = r;
        }

        // 设置
        addReceivers(fc, res);
    }

    protected abstract void addReceivers(SendmailContext fc, WnMailReceiver[] res);

}
