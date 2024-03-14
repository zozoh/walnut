package com.site0.walnut.ext.sys.path.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Lang;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.TextTable;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs("bish")
public class path_abs implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 相当于 pwd
        int N = hc.params.vals.length;
        if (N == 0) {
            sys.exec("pwd");
        }
        // 检查路径
        else if (N == 1) {
            String ph = hc.params.val(0);
            String aph = Wn.normalizeFullPath(ph, sys);
            sys.out.println(aph);
        }
        // 多个路径
        else if (N > 1) {
            // 准备参数
            boolean showBorder = hc.params.is("b");
            boolean showHeader = hc.params.is("h");
            boolean showSummary = hc.params.is("s");
            boolean showIndex = hc.params.is("i");
            int indexBase = hc.params.getInt("ibase", 0);

            String[] cols = Wlang.array("Path", "ABS");
            if (showIndex) {
                cols = Lang.arrayFirst("#", cols);
            }

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

            int i = indexBase;
            for (String ph : hc.params.vals) {
                String aph = Wn.normalizeFullPath(ph, sys);
                List<String> cells = new ArrayList<String>(cols.length);
                if (showIndex) {
                    cells.add("" + (i++));
                }
                cells.add(ph);
                cells.add(aph);
                tt.addRow(cells);
            }

            // 尾部
            if (showSummary) {
                tt.addHr();
            }
            // 输出
            sys.out.print(tt.toString());

            if (showSummary) {
                sys.out.printlnf("total %d paths", N);
            }
        }

    }

}
