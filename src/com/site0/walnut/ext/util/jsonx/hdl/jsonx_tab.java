package com.site0.walnut.ext.util.jsonx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.ext.util.jsonx.JsonXContext;
import com.site0.walnut.ext.util.jsonx.JsonXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class jsonx_tab extends JsonXFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "bish");
    }

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 准备表格
        boolean showBorder = params.is("b");
        boolean showHeader = params.is("h");
        boolean showSummary = params.is("s");
        boolean showIndex = params.is("i");
        int indexBase = params.getInt("ibase", 0);

        // 收集列表
        List<NutBean> list = new LinkedList<>();
        Wlang.each(fc.obj, (index, ele, src) -> {
            if (null != ele && ele instanceof NutBean) {
                NutBean bean = (NutBean) ele;
                list.add(bean);
            }
        });

        // 准备列
        List<String> colList = new LinkedList<>();
        // 自动判断列
        if (0 == params.vals.length && !list.isEmpty()) {
            NutBean bean = list.get(0);
            for (String key : bean.keySet()) {
                colList.add(key);
            }
        }
        // 指定了列
        else {
            for (String val : params.vals) {
                String[] ss = Ws.splitIgnoreBlank(val);
                for (String s : ss) {
                    colList.add(s);
                }
            }
        }
        String[] cols = new String[colList.size()];
        colList.toArray(cols);

        // 输出
        Cmds.output_objs_as_table(sys,
                                  null,
                                  list,
                                  cols,
                                  showBorder,
                                  showHeader,
                                  showSummary,
                                  showIndex,
                                  indexBase);

        // 主程序就不要输出了
        fc.quite = true;
    }

}
