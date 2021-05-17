package org.nutz.walnut.ext.data.o.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.agg.WnAggFuncName;
import org.nutz.walnut.api.io.agg.WnAggItem;
import org.nutz.walnut.api.io.agg.WnAggMode;
import org.nutz.walnut.api.io.agg.WnAggOptions;
import org.nutz.walnut.api.io.agg.WnAggOrderBy;
import org.nutz.walnut.api.io.agg.WnAggResult;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_agg extends OFilter {

    private static final String S_REG = "^"
                                        + "((COUNT|MAX|MIN|SUM|AVG)"
                                        + "|(RAW|TIMESTAMP_TO_DATE)"
                                        + "|((NAME|VALUE)(:(ASC|DESC))?)"
                                        + "|(([^:]+):(.+))"
                                        + "|(\\d+)"
                                        + ")$";
    private static Pattern P_REG = Pattern.compile(S_REG);

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

        // 自动选择聚集分组模式
        if (!agg.hasAggregateMode()) {
            if (agg.getGroupBy().matches("^(ct|lm|expi)$")) {
                agg.setAggregateMode(WnAggMode.TIMESTAMP_TO_DATE);
            } else {
                agg.setAggregateMode(WnAggMode.RAW);
            }
        }

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

        //
        // 得到自定义键名
        //
        String as = params.getString("as");
        int pos = as.indexOf(':');
        String kName, kValue;
        // 'name' 只有名
        if (pos < 0) {
            kName = Ws.sBlank(as, "name");
            kValue = "value";
        }
        // ':value' 只有值
        else if (pos == 0) {
            kName = "name";
            kValue = as.substring(1).trim();
        }
        // 'name:value' 同时指定了名值
        else {
            kName = as.substring(0, pos).trim();
            kValue = as.substring(pos + 1).trim();
        }

        // 输出表格
        if (params.is("tab")) {
            TextTable tt = output_as_table(reList, params, kName, kValue);
            sys.out.print(tt.toString());
            sys.out.printlnf("total %d items", reList.size());
            return;
        }
        //
        // 得到 JSON 格式化方式
        //
        JsonFormat jfmt = Cmds.gen_json_format(params);

        // 转换一下 JSON 的输出键
        if (!Ws.isBlank(as)) {
            List<NutBean> list = gen_bean_list(reList, kName, kValue);
            String output = Json.toJson(list, jfmt);
            sys.out.println(output);
        }
        // 直接输出 JSON
        else {
            String output = Json.toJson(reList, jfmt);
            sys.out.println(output);
        }
    }

    private static List<NutBean> gen_bean_list(WnAggResult reList, String kName, String kValue) {
        // 循环处理
        List<NutBean> list = new ArrayList<>(reList.size());
        for (WnAggItem it : reList) {
            NutBean bean = new NutMap();
            bean.put(kName, it.getName());
            bean.put(kValue, it.getValue());
            list.add(bean);
        }
        return list;
    }

    private static TextTable output_as_table(WnAggResult reList,
                                             ZParams params,
                                             String kName,
                                             String kValue) {
        boolean showBorder = params.is("b");
        boolean showHeader = params.is("h");
        boolean showSummary = params.is("s");
        boolean showIndex = params.is("i");
        int indexBase = params.getInt("ibase", 0);

        // 准备表头
        String[] cols = showIndex ? Wlang.array("#", Ws.upperFirst(kName), Ws.upperFirst(kValue))
                                  : Wlang.array(Ws.upperFirst(kName), Ws.upperFirst(kValue));

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
        for (WnAggItem it : reList) {
            List<String> cells = new ArrayList<String>(cols.length);
            // 处理序号
            if (showIndex) {
                cells.add("" + (i++));
            }
            // 名称
            cells.add(it.getName());
            // 值
            cells.add(it.getValue() + "");
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

    private void updateAggOptions(WnAggOptions agg, ZParams params) {
        // 循环处理参数
        // 0/5 Regin:0/5
        // 0:[ 0, 5) `...`
        // 1:[ 0, 5) `...`
        // 2:[ 0, 5) `COUNT|MAX|MIN|...`
        // 3:[ 0, 17) `TIMESTAMP_TO_DATE|RAW`
        // 4:[ 0, 8) `[NAME|VALUE]:[ASC|DESC]`
        // 5:[ 0, 4) `NAME|VALUE`
        // 6:[ 4, 8) `:[ASC|DESC]`
        // 7:[ 5, 8) `ASC|DESC`
        // 8:[ 0, 5) `id:d0`
        // 9:[ 0, 2) `id`
        // 10:[ 3, 5) `d0`
        // 11:[ 0, 3) `100`
        for (String val : params.vals) {
            Matcher m = P_REG.matcher(val);
            if (!m.find()) {
                continue;
            }
            // 聚集方式
            String funcName = m.group(2);
            if (!Ws.isBlank(funcName)) {
                agg.setFuncName(WnAggFuncName.valueOf(funcName));
                continue;
            }
            // 聚集键的类型
            String aggMode = m.group(3);
            if (!Ws.isBlank(aggMode)) {
                agg.setAggregateMode(WnAggMode.valueOf(aggMode));
                continue;
            }
            // 聚集结果如何排序
            String order = m.group(4);
            if (!Ws.isBlank(order)) {
                agg.setOrderBy(WnAggOrderBy.valueOf(m.group(5)));
                String asc = m.group(7);
                if (!Ws.isBlank(asc)) {
                    agg.setASC("ASC".equals(asc));
                }
                continue;
            }
            // 聚集分组键名
            // 聚集计算键名
            String keys = m.group(8);
            if (!Ws.isBlank(keys)) {
                agg.setGroupBy(m.group(9));
                agg.setAggregateBy(m.group(10));
                continue;
            }
            // 查找记录的最多限制。小于等于零表示全部数据
            String limit = m.group(11);
            if (!Ws.isBlank(limit)) {
                agg.setLimit(Integer.parseInt(limit));
                continue;
            }
        }
    }

}
