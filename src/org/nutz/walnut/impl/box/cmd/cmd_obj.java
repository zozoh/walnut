package org.nutz.walnut.impl.box.cmd;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

public class cmd_obj extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "iocnqhbslVNPH");

        WnPager wp = new WnPager(params);

        NutMap sort = null;
        if (params.has("sort")) {
            sort = Lang.map(params.check("sort"));
        }

        // 如果是要更新，首先分析一下更新参数
        NutMap u_map = null;
        if (params.has("u")) {
            String mapstr;
            // 从管道里读取
            if (sys.pipeId > 0 && "true".equals(params.get("u"))) {
                mapstr = sys.in.readAll();
            }
            // 从内容里读取
            else {
                mapstr = params.get("u");
            }
            u_map = Lang.map(mapstr);
        }

        // 计算要列出的要处理的对象
        List<WnObj> list = new LinkedList<WnObj>();

        // 创建新对象
        if (params.has("new")) {
            String json = params.get("new", "{}");
            NutMap meta;
            // 类似 cat xxx | obj -new -u 的方式创建对象
            if (json.equals("true")) {
                meta = new NutMap();
            }
            // 解析 JSON
            else {
                meta = Lang.map(json);
            }

            // 执行创建
            WnObj o = __do_new(sys, params, meta);

            if (null == u_map) {
                u_map = meta;
            } else {
                u_map.putAll(meta);
            }

            // 记录到列表以备后续操作
            list.add(o);

        }
        // 确保某一路径存在
        else if (params.has("check")) {
            String path = Wn.normalizeFullPath(params.check("check"), sys);

            WnRace race = params.getEnum("race", WnRace.class);
            if (null == race)
                race = WnRace.FILE;

            WnObj o = sys.io.createIfNoExists(null, path, race);
            list.add(o);

        }
        // 给定的路径
        else if (params.vals.length > 0) {
            list = __query_by_path(sys, args, params, wp, sort);
        }
        // 指定查询
        else if (params.has("match")) {
            list = __query_by_match(sys, params, wp, sort);
        }
        // 全都没有，那么看看 u_map 里是不是有 id
        else if (null != u_map && u_map.has("id")) {
            String id = u_map.getString("id");
            list.add(sys.io.checkById(id));
        }
        // 啥都木有，那就用当前路径吧
        else {
            list.add(this.getCurrentObj(sys));
        }

        // 执行更新
        __do_update(sys, u_map, list);

        // 最后执行输出
        if (null == u_map || params.is("o")) {
            output_objs(sys, params, wp, list);
        }

    }

    private WnObj __do_new(WnSystem sys, ZParams params, NutMap meta) {
        String pid = meta.getString("pid");
        WnRace race = meta.getAs("race", WnRace.class, WnRace.FILE);

        // 得到父对象
        WnObj oP;
        // 根据路径
        if (params.vals.length > 0) {
            WnObj oCurrent = this.getCurrentObj(sys);
            String path = Wn.normalizePath(params.vals[0], sys);
            oP = sys.io.check(oCurrent, path);
        }
        // 指明了 pid
        else if (null != pid) {
            oP = sys.io.checkById(pid);
        }
        // 采用当前路径
        else {
            oP = this.getCurrentObj(sys);
        }

        // 创建对象
        WnObj o = sys.io.create(oP, meta.getString("nm", "${id}"), race);

        // 准备更新的元数据
        meta.remove("id");
        meta.remove("pid");
        meta.remove("race");
        meta.remove("nm");
        return o;
    }

    private List<WnObj> __query_by_path(WnSystem sys,
                                        String[] args,
                                        ZParams params,
                                        WnPager wp,
                                        NutMap sort) {
        List<WnObj> list;
        list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, params.vals, list, false);

        // 不是强制列表模式的时候，检查是否候选对象列表为空
        if (!params.is("l")) {
            checkCandidateObjsNoEmpty(args, list);
        }

        // 排序
        if (null != sort) {
            __do_sort(sort, list);
        }

        // 看看是否需要查询分页信息
        if (wp.countPage && wp.limit > 0) {
            wp.sum_count = list.size();
            wp.sum_page = (int) Math.ceil(((double) wp.sum_count) / ((double) wp.limit));
        }

        // skip 大于 0
        if (wp.skip > 0) {
            // 截取一部分
            int toIndex = wp.skip + wp.limit;
            if (toIndex > wp.skip && toIndex < list.size()) {
                list = list.subList(wp.skip, wp.skip + wp.limit);
            }
            // 从 skip 开始全部的
            else {
                list = list.subList(wp.skip, list.size() - wp.skip);
            }
        }
        // limit 大于 0
        else if (wp.limit > 0 && wp.limit < list.size()) {
            list = list.subList(0, wp.limit);
        }
        return list;
    }

    private List<WnObj> __query_by_match(WnSystem sys, ZParams params, WnPager wp, NutMap sort) {
        List<WnObj> list;
        String json = params.get("match", "{}");
        WnQuery q = new WnQuery();
        // 条件是"或"
        if (Strings.isQuoteBy(json, '[', ']')) {
            List<NutMap> ors = Json.fromJsonAsList(NutMap.class, json);
            q.addAll(ors);
        }
        // 条件是"与"
        else {
            q.add(Lang.map(json));
        }

        // 添加更多条件
        q.setv("d1", sys.se.group());

        // 看看是否需要查询分页信息
        if (wp.countPage && wp.limit > 0) {
            wp.sum_count = (int) sys.io.count(q);
            wp.sum_page = (int) Math.ceil(((double) wp.sum_count) / ((double) wp.limit));
        }

        if (wp.skip > 0)
            q.skip(wp.skip);

        if (wp.limit > 0)
            q.limit(wp.limit);

        if (null != sort)
            q.sort(sort);

        list = sys.io.query(q);
        return list;
    }

    private void __do_update(WnSystem sys, NutMap u_map, List<WnObj> list) {
        if (null != u_map) {
            u_map.remove("id");
            // 将日期的字符串，搞一下
            for (Map.Entry<String, Object> en : u_map.entrySet()) {
                Object v = en.getValue();
                if (null != v && v instanceof String) {
                    String s = v.toString();
                    // 日期对象
                    if (s.startsWith("$date:")) {
                        String str = s.substring("$date:".length());
                        if ("now".equals(str)) {
                            en.setValue(Times.now());
                        } else {
                            en.setValue(Times.D(str));
                        }
                    }
                    // 毫秒数
                    else if (s.startsWith("$ms:")) {
                        String str = s.substring("$ms:".length());
                        if ("now".equals(str)) {
                            en.setValue(System.currentTimeMillis());
                        } else {
                            en.setValue(Times.D(str).getTime());
                        }
                    }
                }
            }
            // 对每个对象执行更新
            for (WnObj o : list) {
                sys.io.appendMeta(o, u_map);
            }
        }
    }

    private void __do_sort(NutMap sort, List<WnObj> list) {
        final NutMap smap = sort;
        Collections.sort(list, new Comparator<WnObj>() {
            public int compare(WnObj o1, WnObj o2) {
                for (String key : smap.keySet()) {
                    Object v1 = o1.get(key);
                    Object v2 = o2.get(key);

                    if (null == v1 && null == v2)
                        continue;

                    // 排序因子
                    int E = smap.getInt(key) > 0 ? 1 : -1;

                    // 有一个为 null
                    if (null == v1)
                        return E;

                    if (null == v2)
                        return E * -1;

                    // 数字字段
                    if (v1 instanceof Number && v2 instanceof Number) {
                        double re = ((Number) v1).doubleValue() - ((Number) v2).doubleValue();

                        if (re == 0.0) {
                            continue;
                        }

                        if (re > 0.0)
                            return E;
                        else
                            return E * -1;
                    }
                    // 其他的当做字符串
                    String s1 = v1.toString();
                    String s2 = v2.toString();
                    return s1.compareTo(s2) * E;

                }
                return 0;
            }
        });
    }

}
