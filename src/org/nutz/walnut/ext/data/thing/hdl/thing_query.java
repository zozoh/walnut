package org.nutz.walnut.ext.data.thing.hdl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.ThQr;
import org.nutz.walnut.ext.data.thing.util.ThQuery;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.bean.WnBeanMapping;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(maponly|dynamic_mapping|pager|content|obj)$")
public class thing_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 分析参数
        ThQuery tq = new ThQuery();

        // ..............................................
        // 准备分页信息
        tq.wp = new WnPager(hc.params);

        // ..............................................
        // 准备查询条件
        tq.qStr = Cmds.getParamOrPipe(sys, hc.params, 0);

        // ..............................................
        // 准备映射
        if (hc.params.has("mapping")) {
            String phMapping = hc.params.getString("mapping");
            // 动态映射路径
            if (hc.params.is("dynamic_mapping")) {
                tq.mappingPath = Tmpl.parse(phMapping);
            }
            // 直接指定了映射文件
            else {
                WnObj oMapping = Wn.checkObj(sys, phMapping);
                tq.mapping = sys.io.readJson(oMapping, WnBeanMapping.class);
                NutMap vars = sys.session.getVars();
                Map<String, NutMap[]> caches = new HashMap<>();
                tq.mapping.checkFields(sys.io, vars, caches);
            }
            tq.mappingOnly = hc.params.is("maponly");
        }

        // 如果还需要查询关联对象的内容指纹
        String sha1 = hc.params.getString("sha1");
        if (!Strings.isBlank(sha1)) {
            tq.sha1Fields = Strings.splitIgnoreBlank(sha1);
        }

        // 设置排序
        if (hc.params.hasString("sort")) {
            tq.sort = Lang.map(hc.params.check("sort"));
        }

        tq.needContent = hc.params.is("content");
        tq.autoObj = hc.params.is("obj");

        // 准备服务类
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 调用接口
        ThQr qr = wts.queryThing(tq);

        hc.pager = qr.pager;
        hc.output = qr.data;
    }

}
