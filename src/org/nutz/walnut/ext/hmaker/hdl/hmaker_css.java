package org.nutz.walnut.ext.hmaker.hdl;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class hmaker_css implements JvmHdl {

    private static final Pattern P_CMT = Pattern.compile("^/[*](.+)[*]/$");

    private static final Pattern P_NOT = Pattern.compile("[> \t+]");

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 准备返回值列表
        NutMap re = new NutMap();

        // 依次分析每个 CSS 文件
        for (String rph : hc.params.vals) {
            List<NutMap> list = __do_css_file(sys, hc, rph);
            // 按照名称排序
            if (hc.params.has("sort")) {
                __do_sort_list(list, hc.params.get("sort"));
            }
            re.put(rph, list);
        }

        // 输出
        sys.out.println(Json.toJson(re, hc.jfmt.setIgnoreNull(false)));
    }

    private void __do_sort_list(List<NutMap> list, String sort) {
        // 要排序的 key
        String key = "selector";
        int val = 1;
        int pos = sort.indexOf(':');
        if (pos >= 0) {
            key = Strings.sBlank(sort.substring(0, pos), "selector");
            val = Integer.parseInt(sort.substring(pos + 1));
        } else {
            val = Integer.parseInt(sort);
        }
        // 执行排序
        final String byKey = key;
        final int sortBy = val;
        Collections.sort(list, new Comparator<NutMap>() {
            public int compare(NutMap o1, NutMap o2) {
                String v1 = o1.getString(byKey);
                String v2 = o2.getString(byKey);
                int re = 0;
                // 有一个为空，空比较小
                if (v1 == null || v2 == null) {
                    // 都为空，相等
                    if (v1 == null && v2 == null) {
                        re = 0;
                    }
                    // 空的比较小
                    else if (v1 == null) {
                        re = -1;
                    }
                    // 那么就是 v2 大咯
                    else {
                        re = 1;
                    }
                }
                // 比较两值
                else {
                    re = v1.compareTo(v2);
                }
                // 返回结果
                return re * sortBy;
            }
        });
    }

    private List<NutMap> __do_css_file(WnSystem sys, JvmHdlContext hc, String rph) {
        // 读取文件
        WnObj oCss = sys.io.check(hc.oRefer, rph);

        // 准备返回值
        List<NutMap> list = new LinkedList<>();

        // 读取全部内容，并按行切割
        String cssText = sys.io.readText(oCss);
        String[] lines = Strings.splitIgnoreBlank(cssText, "(\r?\n)+");

        // 逐行分析
        String lastComment = null;
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            // 忽略空行
            if (Strings.isBlank(line))
                continue;

            // 遇到注释
            Matcher m = P_CMT.matcher(line);
            if (m.find()) {
                lastComment = Strings.trim(m.group(1));
                continue;
            }

            // 看看是不是遇到规则
            int pos = line.indexOf('{');

            // 遇到规则的开始
            if (pos >= 0) {
                sb.append(' ').append(line.substring(0, pos));
                String str = Strings.trim(sb);
                // 看看是不是有必要加入
                if (str.startsWith(".") && !P_NOT.matcher(str).find()) {
                    list.add(Lang.map("selector", str.substring(1)).setv("text", lastComment));
                }
            }
            // 否则记录到
            else {
                sb.append(' ').append(line);
            }

            // 看看是不是遇到规则结束，如果结束则清除
            pos = line.lastIndexOf('}');
            if (pos >= 0) {
                sb.setLength(0);
                lastComment = null;
            }

        }

        // 返回
        return list;
    }

}
