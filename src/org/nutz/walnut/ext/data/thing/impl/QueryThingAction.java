package org.nutz.walnut.ext.data.thing.impl;

import java.util.LinkedList;
import java.util.List;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.data.thing.ThingAction;
import org.nutz.walnut.ext.data.thing.util.ThQr;
import org.nutz.walnut.ext.data.thing.util.ThQuery;
import org.nutz.walnut.ext.data.thing.util.Things;

public class QueryThingAction extends ThingAction<ThQr> {

    protected ThQuery tq;

    public QueryThingAction setQuery(ThQuery tq) {
        this.tq = tq;
        return this;
    }

    @Override
    public ThQr invoke() {
        WnQuery q = new WnQuery();
        ThQr output = new ThQr();

        // ..............................................
        // 准备查询条件
        // 指定了条件
        if (!Strings.isBlank(tq.qStr)) {
            // 条件是"或"
            if (Strings.isQuoteBy(tq.qStr, '[', ']')) {
                List<NutMap> ors = Json.fromJsonAsList(NutMap.class, tq.qStr);
                q.addAll(ors);
            }
            // 条件是"与"
            else {
                q.add(Lang.map(tq.qStr));
            }
        }
        // 未指定条件
        else {
            q.first();
        }

        // ..............................................
        // 找到数据目录
        WnObj oIndex = Things.dirTsIndex(io, oTs);

        // 没有数据
        if (null == oIndex) {
            output.pager = tq.wp;
            output.data = new LinkedList<WnObj>();
            return output;
        }

        // 限定数据集
        q.setAllToList(Lang.map("pid", oIndex.id()));

        // 检查 th_live
        List<NutMap> qList = q.getList();
        for (NutMap qe : qList) {
            if (!qe.has("th_live")) {
                qe.put("th_live", Things.TH_LIVE);
            }
        }

        // ..............................................
        // 设置分页信息
        if (null != tq.wp) {
            tq.wp.setupQuery(io, q);
        }

        // 设置排序
        if (null != tq.sort && tq.sort.size() > 0) {
            q.sort(tq.sort);
        }

        // ..............................................
        // 执行查询并返回结果
        List<WnObj> list = io.query(q);

        // ..............................................
        // 循环补充上 ThingSet 的集合名称
        for (WnObj oT : list) {
            oT.putDefault("th_live", 1);
            oT.put("th_set", oTs.id());
            oT.put("th_set_nm", oTs.name());
        }

        // 循环读取内容
        if (tq.needContent) {
            for (WnObj oT : list) {
                oT.put("content", io.readText(oT));
            }
        }

        // ..............................................
        // 读取指定字段的 SHA1 指纹
        if (null != tq.sha1Fields && tq.sha1Fields.length > 0) {
            for (WnObj oT : list) {
                for (String key : tq.sha1Fields) {
                    String val = oT.getString(key);
                    if (!Strings.isBlank(val)) {
                        WnObj o = io.fetch(oT, val);
                        if (null != o) {
                            oT.put(key + "_obj",
                                   o.pickBy("^(id|nm|title|sha1|len|mime|tp|width|height)$"));
                        }
                    }
                }
            }
        }

        // ..............................................
        // 如果只输出一个对象
        if (tq.autoObj) {
            // 空
            if (null == list || list.isEmpty()) {
                output.data = null;
            }
            // 选择一个元素
            else {
                output.data = list.get(0);
            }
        }
        // 输出列表
        else {
            output.pager = tq.wp;
            output.data = list;
        }
        return output;
    }

}
