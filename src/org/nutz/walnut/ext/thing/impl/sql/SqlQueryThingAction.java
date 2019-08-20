package org.nutz.walnut.ext.thing.impl.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.dao.Cnd;
import org.nutz.dao.entity.Record;
import org.nutz.dao.util.cri.Exps;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.thing.impl.QueryThingAction;
import org.nutz.walnut.ext.thing.util.ThQr;
import org.nutz.walnut.ext.thing.util.Things;

public class SqlQueryThingAction extends QueryThingAction {

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
        // 确保限定了集合
        Map<String, WnObj> oTsCache = new HashMap<String, WnObj>();
        if (null != tq.tss && tq.tss.length > 0) {

            // 寻找对应的 ThingSet.index 对象，并计入 pid
            String[] pids = new String[tq.tss.length];
            for (int i = 0; i < tq.tss.length; i++) {
                String tsId = tq.tss[i];
                WnObj oRefer = io.checkById(tsId);
                WnObj oTs = Things.checkThingSet(oRefer);
                WnObj oIndex = Things.dirTsIndex(io, oTs);
                oTsCache.put(oTs.id(), oTs);
                pids[i] = oIndex.id();
            }

            // 没有数据
            if (0 == pids.length) {
                output.pager = tq.wp;
                output.data = new LinkedList<WnObj>();
                return output;
            }

            // 只有一个数据
            if (1 == pids.length) {
                q.setAllToList(Lang.map("pid", pids[0]));
            }
            // 多个数据
            else {
                q.setAllToList(Lang.map("pid", pids));
            }
        }
        // 找到数据目录
        else {
            WnObj oIndex = Things.dirTsIndex(io, oTs);
            oTsCache.put(oTs.id(), oTs);

            // 没有数据
            if (null == oIndex) {
                output.pager = tq.wp;
                output.data = new LinkedList<WnObj>();
                return output;
            }

            // 限定数据集
            q.setAllToList(Lang.map("pid", oIndex.id()));
        }

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
        // List<WnObj> list = io.query(q);
        SqlThingContext ctx = SqlThingMaster.me().getSqlThingContext(oTs);
        Cnd cnd = Cnd.NEW();
        boolean isAnd = qList.size() < 2;
        for (NutMap qe : qList) {
            SqlExpressionGroup g = Exps.begin();
            for (Map.Entry<String, Object> en : qe.entrySet()) {
                String key = en.getKey();
                if ("pid".equals(key))
                    continue;
                Object value = en.getValue();
                if (value == null) {
                    g.and(key, "is", null);
                } else if (value instanceof String) {
                    if (((String) value).contains("%")) {
                        g.and(key, "like", value);
                    } else {
                        g.and(key, "=", value);
                    }
                } else if (value instanceof Map) {
                    NutMap tmp = (NutMap) value;
                    for (Map.Entry<String, Object> en2 : tmp.entrySet()) {
                        String _key = en2.getKey();
                        switch (_key) {
                        case "$eq":
                        case "@eq":
                            g.and(key, "=", en2.getValue());
                            break;
                        case "$gt":
                        case "@gt":
                            g.and(key, ">", en2.getValue());
                            break;
                        case "$gte":
                        case "@gte":
                            g.and(key, ">=", en2.getValue());
                            break;
                        case "$lt":
                        case "@lt":
                            g.and(key, "<", en2.getValue());
                            break;
                        case "$lte":
                        case "@lte":
                            g.and(key, "<=", en2.getValue());
                            break;
                        case "$ne":
                        case "@ne":
                            g.and(key, "!=", en2.getValue());
                            break;
                        default:
                            // no support yet
                            break;
                        }
                    }
                } else {
                    g.and(key, "=", value);
                }
            }
            if (isAnd) {
                cnd.and(g);
            } else {
                cnd.or(g);
            }
        }
        // 排序
        if (null != tq.sort && tq.sort.size() > 0) {
            for (Map.Entry<String, Object> en : tq.sort.entrySet()) {
                cnd.orderBy(en.getKey(), String.valueOf(en.getValue()).equals("1") ? "asc" : "desc");
            }
        }
        // 执行查询
        List<Record> _list = ctx.dao.query(ctx.table, cnd, tq.wp);
        // 转换结果集合
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Record re : _list) {
            maps.add(re.sensitive());
        }
        List<WnObj> list = SqlThingMaster.asWnObj(oTs, checkDirTsIndex(), maps);
        // 补齐pager数据
        if (null != tq.wp && tq.wp.countPage) {
            tq.wp.setRecordCount(ctx.dao.count(ctx.table, cnd));
        }

        // 循环补充上 ThingSet 的集合名称
        for (WnObj oT : list) {
            WnObj oTs = oTsCache.get(oT.getString("th_set"));
            if (null != oTs) {
                oT.put("th_set_nm", oTs.name());
            }
        }

        // 循环读取内容
        if (tq.needContent) {
            for (WnObj oT : list) {
                oT.put("content", io.readText(oT));
            }
        }

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
