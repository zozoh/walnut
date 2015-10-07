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
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.ZParams;

public class cmd_obj extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "iocnqhbslVNP");

        int skip = params.getInt("skip", -1);
        int limit = params.getInt("limit", -1);
        boolean countPage = params.is("pager");
        int pn = skip / limit + 1;
        int pgsz = limit;
        int sum_count = -1;
        int sum_page = -1;
        NutMap sort = null;
        if (params.has("sort")) {
            sort = Lang.map(params.check("sort"));
        }

        // 首先获取对象
        // 计算要列出的要处理的对象
        List<WnObj> list;

        // 指定查询
        if (params.vals.length == 0) {
            String json = params.get("match", "{}");
            WnQuery q = new WnQuery();
            q.setv("d1", sys.se.group());
            q.setAll(Lang.map(json));

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
        // 给定的路径
        else {
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

            // skip 大于 0
            if (skip > 0) {
                if (limit > 0)
                    list = list.subList(skip, skip + limit);
                else
                    list = list.subList(skip, list.size() - skip);
            }
            // limit 大于 0
            else if (limit > 0) {
                list = list.subList(0, limit);
            }
        }

        // 是否强制输出路径
        if (list.size() == 1 || params.is("P")) {
            for (WnObj o : list)
                o.path();
        }

        // 一次处理所有对象
        List<NutMap> outs = new ArrayList<NutMap>(list.size());
        for (WnObj o : list) {
            // 更新对象
            if (params.has("u")) {
                NutMap map = Lang.map(params.get("u"));
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
                sys.io.appendMeta(o, map);
            }
            // ................................................
            // 记录输出
            // 显示对象某几个值
            if (params.has("e")) {
                String regex = params.check("e");
                boolean not = false;
                if (regex.startsWith("!")) {
                    not = true;
                    regex = regex.substring(1);
                }
                Pattern p = Pattern.compile(regex);
                NutMap map = new NutMap();
                for (String key : o.keySet()) {
                    if (p.matcher(key).matches()) {
                        if (!not)
                            map.put(key, o.get(key));
                    } else if (not) {
                        map.put(key, o.get(key));
                    }
                }
                outs.add(map);
            }
            // 显示对象全部的值
            else {
                if (o instanceof WnBean)
                    outs.add((NutMap) o);
                else
                    outs.add(o.toMap(null));
            }
        }
        // 当没有更新，或者强制输出的时候，执行输出
        if (outs.size() > 0 && (params.is("o") || !params.has("u"))) {
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
