package org.nutz.walnut.ext.thing.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.WnPager;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(pager|content|obj)$")
public class thing_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 找到数据目录
        WnObj oIndex = Things.dirTsIndex(sys, hc);

        // ..............................................
        // 准备分页信息
        WnPager wp = new WnPager(hc.params);

        // 没有数据
        if (null == oIndex) {
            hc.pager = wp;
            hc.output = new LinkedList<WnObj>();
            return;
        }

        // ..............................................
        // 准备查询条件
        String qStr = Cmds.getParamOrPipe(sys, hc.params, 0);
        WnQuery q = new WnQuery();
        // 指定了条件
        if (!Strings.isBlank(qStr)) {
            // 条件是"或"
            if (Strings.isQuoteBy(qStr, '[', ']')) {
                List<NutMap> ors = Json.fromJsonAsList(NutMap.class, qStr);
                q.addAll(ors);
            }
            // 条件是"与"
            else {
                q.add(Lang.map(qStr));
            }
        }
        // 未指定条件
        else {
            q.first();
        }
        // 确保限定了集合
        NutMap map = new NutMap();
        map.put("pid", oIndex.id());
        q.setAllToList(map);

        // 检查 th_live
        List<NutMap> qList = q.getList();
        for (NutMap qe : qList) {
            if (!qe.has("th_live")) {
                qe.put("th_live", Things.TH_LIVE);
            }
        }

        // ..............................................
        // 设置分页信息
        if (null != wp) {
            wp.setupQuery(sys, q);
        }

        // 设置排序
        if (hc.params.hasString("sort")) {
            NutMap sort = Lang.map(hc.params.check("sort"));
            q.sort(sort);
        }

        // ..............................................
        // 执行查询并返回结果
        List<WnObj> list = sys.io.query(q);

        // 循环读取内容
        if (hc.params.is("content")) {
            for (WnObj oT : list) {
                oT.put("content", sys.io.readText(oT));
            }
        }

        // 如果只输出一个对象
        if (hc.params.is("obj")) {
            // 空
            if (null == list || list.isEmpty()) {
                hc.output = null;
            }
            // 选择一个元素
            else {
                hc.output = list.get(0);
            }
        }
        // 输出列表
        else {
            hc.pager = wp;
            hc.output = list;
        }
    }

}
