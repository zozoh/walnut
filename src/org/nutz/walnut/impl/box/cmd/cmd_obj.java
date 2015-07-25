package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
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
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.ZParams;

public class cmd_obj extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "iocnqhbslVN");

        // 首先获取对象
        // 计算要列出的要处理的对象
        List<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, params.vals, list, false);

        // 检查是否候选对象列表为空
        checkCandidateObjsNoEmpty(args, list);

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
            // 按表格输出
            else if (params.has("t")) {
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
                    sys.out.printlnf("total %d items", outs.size());
                }
            }
            // 用 Json 的方法输出
            else {
                JsonFormat fmt = params.is("c") ? JsonFormat.compact() : JsonFormat.forLook();
                fmt.setIgnoreNull(!params.is("n")).setQuoteName(params.is("q"));
                String json;
                if (params.is("l") || outs.size() > 1) {
                    json = Json.toJson(outs, fmt);
                } else {
                    json = Json.toJson(outs.get(0), fmt);
                }
                sys.out.println(json);
            }
        }
    }

}
