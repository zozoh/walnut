package com.site0.walnut.ext.data.o.hdl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.agg.WnAggGroupKey;
import com.site0.walnut.api.io.agg.WnAggKey;
import com.site0.walnut.api.io.agg.WnAggTransMode;
import com.site0.walnut.api.io.agg.WnAggregateKey;
import com.site0.walnut.api.io.agg.WnAggOptions;
import com.site0.walnut.api.io.agg.WnAggOrderBy;
import com.site0.walnut.api.io.agg.WnAggResult;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.TextTable;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class o_agg extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnbish", "^(tab)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 获取过滤参数
        String json = Cmds.getParamOrPipe(sys, params, "match", true);
        NutMap match = Wlang.map(json);

        // 准备默认聚集设置
        WnAggOptions agg = new WnAggOptions();

        // 更新聚集设置
        updateAggOptions(agg, params);

        // 检查聚集设置
        agg.assertValid();

        // 准备聚集结果
        WnAggResult reList = null;

        // 处理上下文对象
        for (WnObj o : fc.list) {
            // 仅仅处理目录
            if (!o.isDIR()) {
                continue;
            }
            // 计算聚集结果
            WnQuery q = Wn.Q.pid(o);
            if (null != match && !match.isEmpty()) {
                q.setAll(match);
            }
            WnAggResult re = sys.io.aggregate(q, agg);
            // 第一个聚集结果
            if (null == reList || reList.isEmpty()) {
                reList = re;
            }
            // 融合聚集函数
            else {
                reList.addAll(re);
            }
        }

        // 禁止主函数输出
        fc.quiet = true;

        // 输出表格
        if (params.is("tab")) {
            TextTable tt = output_as_table(reList, agg, params);
            sys.out.print(tt.toString());
            sys.out.printlnf("total %d items", reList.size());
            return;
        }
        //
        // 得到 JSON 格式化方式
        //
        JsonFormat jfmt = Cmds.gen_json_format(params);

        // 直接输出 JSON
        String output = Json.toJson(reList, jfmt);
        sys.out.println(output);
    }

    private static TextTable output_as_table(WnAggResult reList, WnAggOptions agg, ZParams params) {
        boolean showBorder = params.is("b");
        boolean showHeader = params.is("h");
        boolean showSummary = params.is("s");
        boolean showIndex = params.is("i");
        int indexBase = params.getInt("ibase", 0);

        // 准备表头
        List<String> chList = new LinkedList<>();
        if (showIndex) {
            chList.add("#");
        }
        for (WnAggGroupKey gk : agg.getGroupBy()) {
            chList.add(gk.getToName());
        }
        chList.add(agg.getAggregateBy().getToName());
        String[] cols = new String[chList.size()];
        chList.toArray(cols);

        // 准备输出表
        TextTable tt = new TextTable(cols.length);
        if (showBorder) {
            tt.setShowBorder(true);
        } else {
            tt.setCellSpacing(2);
        }
        // 加标题
        if (showHeader) {
            tt.addRow(cols);
            tt.addHr();
        }
        // 主体
        int i = indexBase;
        for (NutBean it : reList) {
            List<String> cells = new ArrayList<String>(cols.length);
            // 处理序号
            if (showIndex) {
                cells.add("" + (i++));
            }
            // 分组
            String v;
            for (WnAggGroupKey gk : agg.getGroupBy()) {
                v = it.getString(gk.getToName());
                cells.add(v);
            }
            // 计算值
            v = it.getString(agg.getAggregateBy().getToName());
            cells.add(v);
            // 计入表格行
            tt.addRow(cells);
        }
        // 尾部
        if (showSummary) {
            tt.addHr();
        }
        // 输出
        return tt;
    }

    private static final String S_REG = "^(DATA|TOP)(\\d+)$";
    private static Pattern P_REG = Pattern.compile(S_REG);

    private void updateAggOptions(WnAggOptions agg, ZParams params) {
        for (String val : params.vals) {
            String upper = val.toUpperCase();

            // 排序: xxx:ASC
            if (upper.endsWith(":ASC")) {
                String key = val.substring(0, val.length() - 4).trim();
                WnAggOrderBy ob = new WnAggOrderBy(key, true);
                agg.addOrderBy(ob);
                continue;
            }
            // 排序: xxx:DESC
            if (upper.endsWith(":DESC")) {
                String key = val.substring(0, val.length() - 5).trim();
                WnAggOrderBy ob = new WnAggOrderBy(key, false);
                agg.addOrderBy(ob);
                continue;
            }
            // 限制数量: (TOP|DATA)10
            Matcher m = P_REG.matcher(upper);
            if (m.find()) {
                String lt = m.group(1);
                int lv = Integer.parseInt(m.group(2));
                if ("TOP".equals(lt)) {
                    agg.setOutputLimit(lv);
                } else {
                    agg.setDataLimit(lv);
                }
                continue;
            }
            // 聚集键的类型
            WnAggKey ak = WnAggKey.parse(val);
            if (null != ak) {
                if (ak instanceof WnAggGroupKey) {
                    WnAggGroupKey gk = (WnAggGroupKey) ak;
                    if (!gk.hasFunc()) {
                        if (gk.getFromName().matches("^(ct|lm|expi)$")) {
                            gk.setFunc(WnAggTransMode.TIMESTAMP_TO_DATE);
                        } else {
                            gk.setFunc(WnAggTransMode.RAW);
                        }
                    }
                    agg.addGroupBy(gk);
                } else {
                    agg.setAggregateBy((WnAggregateKey) ak);
                }
            }
        }
    }

}
