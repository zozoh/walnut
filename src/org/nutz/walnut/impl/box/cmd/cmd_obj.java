package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_obj extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "iocnqhbslVNPH");

        int skip = params.getInt("skip", -1);
        int limit = params.getInt("limit", -1);
        boolean countPage = params.is("pager");
        int pgsz = limit > 0 ? limit : 50;
        int pn = skip > 0 ? skip / pgsz + 1 : 1;
        int sum_count = -1;
        int sum_page = -1;
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
            if (countPage && limit > 0) {
                sum_count = list.size();
                sum_page = (int) Math.ceil(((double) sum_count) / ((double) limit));
            }

            // skip 大于 0
            if (skip > 0) {
                // 截取一部分
                int toIndex = skip + limit;
                if (toIndex > skip && toIndex < list.size()) {
                    list = list.subList(skip, skip + limit);
                }
                // 从 skip 开始全部的
                else {
                    list = list.subList(skip, list.size() - skip);
                }
            }
            // limit 大于 0
            else if (limit > 0 && limit < list.size()) {
                list = list.subList(0, limit);
            }
        }
        // 指定查询
        else if (params.has("match")) {
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
            if (countPage && limit > 0) {
                sum_count = (int) sys.io.count(q);
                sum_page = (int) Math.ceil(((double) sum_count) / ((double) limit));
            }

            if (skip > 0)
                q.skip(skip);

            if (limit > 0)
                q.limit(limit);

            if (null != sort)
                q.sort(sort);

            list = sys.io.query(q);
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

        // 一次处理所有对象
        List<NutMap> outs = new ArrayList<NutMap>(list.size());
        for (WnObj o : list) {
            // 更新对象
            if (null != u_map) {
                NutMap map = u_map;
                map.remove("id");
                // 将日期的字符串，搞一下
                for (Map.Entry<String, Object> en : map.entrySet()) {
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
                // 执行更新
                sys.io.appendMeta(o, map);

                // 是否要输出
                if (params.is("o")) {
                    // 是否强制输出路径
                    if (list.size() == 1 || params.is("P")) {
                        o.path();
                    }
                    outs.add(__obj_to_outmap(o, params));
                }

            }
            // ................................................
            // 添加到输出结果里
            else {
                // 是否强制输出路径
                if (list.size() == 1 || params.is("P")) {
                    o.path();
                }
                outs.add(__obj_to_outmap(o, params));
            }
        }
        // 当没有更新，或者强制输出的时候，执行输出
        if (outs.size() > 0) {
            // 仅输出值
            if (params.is("V")) {
                __output_as_value(sys, params, outs);
            }
            // 按表格输出
            else if (params.has("t")) {
                __output_as_table(sys,
                                  params,
                                  countPage,
                                  pn,
                                  pgsz,
                                  sum_count,
                                  sum_page,
                                  skip,
                                  outs);
            }
            // 用 Json 的方法输出
            else {
                __output_as_json(sys, params, countPage, pn, pgsz, sum_count, sum_page, skip, outs);
            }
        }
    }

    private NutMap __obj_to_outmap(WnObj o, ZParams params) {
        // true 表示输出的时候，也显示双下划线开头的隐藏字段
        boolean isShowAutoHide = params.is("H");

        // 字段过滤正则表达式
        Pattern p = null;
        boolean not = false;
        String regex = params.get("e");
        if (!Strings.isBlank(regex)) {
            if (regex.startsWith("!")) {
                not = true;
                regex = regex.substring(1);
            }
            p = Pattern.compile(regex);
        }

        // 依次判断字段
        NutMap map = new NutMap();
        for (String key : o.keySet()) {
            // 忽略自动隐藏字段
            if (!isShowAutoHide && key.startsWith("__"))
                continue;

            // 用正则表达式判断
            if (null != p) {
                if (p.matcher(key).matches()) {
                    if (!not)
                        map.put(key, o.get(key));
                } else if (not) {
                    map.put(key, o.get(key));
                }
            }
            // 那么一定要添加的
            else {
                map.put(key, o.get(key));
            }
        }
        return map;
    }

    private void __output_as_value(WnSystem sys, ZParams params, List<NutMap> outs) {
        String sep = params.get("sep", "");
        for (NutMap map : outs) {
            sys.out.print(Lang.concat(sep, map.values()));
            if (params.is("N")) {
                sys.out.println();
            }
        }
        if (!params.is("N"))
            sys.out.println();
    }

    private void __output_as_table(WnSystem sys,
                                   ZParams params,
                                   boolean countPage,
                                   int pn,
                                   int pgsz,
                                   int sum_count,
                                   int sum_page,
                                   int skip,
                                   List<NutMap> outs) {
        String sCols = params.get("t");
        String[] aCols = Strings.splitIgnoreBlank(sCols);
        if (params.is("i")) {
            aCols = Lang.arrayFirst("#", aCols);
        }

        // 准备输出表
        TextTable tt = new TextTable(aCols.length);
        if (params.is("b")) {
            tt.setShowBorder(true);
        } else {
            tt.setCellSpacing(2);
        }
        // 加标题
        if (params.is("h")) {
            tt.addRow(aCols);
            tt.addHr();
        }
        // 主体
        int i = params.getInt("ibase", 0);
        for (NutMap map : outs) {
            List<String> cells = new ArrayList<String>(aCols.length);
            for (String key : aCols) {
                if ("#".equals(key)) {
                    cells.add("" + (i++));
                    continue;
                }
                Object v = map.get(key);
                cells.add(v == null ? null : v.toString());
            }
            tt.addRow(cells);
        }
        // 尾部
        if (params.is("s")) {
            tt.addHr();
        }
        // 输出
        sys.out.print(tt.toString());
        if (params.is("s")) {
            // 是计算分页的
            if (countPage) {
                sys.out.printlnf("total %d/%d items, skip %d page %d/%d, %d per page",
                                 outs.size(),
                                 sum_count,
                                 skip,
                                 pn,
                                 sum_page,
                                 pgsz);
            }
            // 就是显示列表
            else {
                sys.out.printlnf("total %d items", outs.size());
            }
        }
    }

    private void __output_as_json(WnSystem sys,
                                  ZParams params,
                                  boolean countPage,
                                  int pn,
                                  int pgsz,
                                  int sum_count,
                                  int sum_page,
                                  int skip,
                                  List<NutMap> outs) {
        JsonFormat fmt = params.is("c") ? JsonFormat.compact() : JsonFormat.forLook();
        fmt.setIgnoreNull(!params.is("n")).setQuoteName(params.is("q"));
        String json;
        // 强制输出列表
        if (params.is("l") || outs.size() > 1 || countPage) {
            // 输出分页信息
            if (countPage) {
                NutMap re = new NutMap();
                re.setv("list", outs);
                re.setv("pager",
                        Lang.mapf("pn:%d,pgsz:%d,pgnb:%d,sum:%d,skip:%d,nb:%d",
                                  pn,
                                  pgsz,
                                  sum_page,
                                  sum_count,
                                  skip,
                                  outs.size()));
                json = Json.toJson(re, fmt);
            }
            // 直接打印列表
            else {
                json = Json.toJson(outs, fmt);
            }
        }
        // 显示一个单独对象
        else {
            json = Json.toJson(outs.get(0), fmt);
        }
        sys.out.println(json);
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
