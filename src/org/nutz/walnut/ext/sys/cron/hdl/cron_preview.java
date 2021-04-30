package org.nutz.walnut.ext.sys.cron.hdl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.walnut.ext.sys.cron.WnSysCron;
import org.nutz.walnut.ext.sys.cron.WnSysCronQuery;
import org.nutz.walnut.ext.sys.cron.WnSysCronService;
import org.nutz.walnut.ext.sys.cron.cmd_cron;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cron_preview implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析查询参数
        WnSysCronQuery q = cmd_cron.prepareCronQuery(sys, hc);

        // 准备服务类
        WnSysCronService cronApi = Wn.Service.crons();

        // 执行查询
        List<WnSysCron> list = cronApi.listCron(q, true);

        // 得到今日
        String today = hc.params.getString("today", "now");
        long ams = Wn.evalDatetimeStrToAMS(today);
        Date d = new Date(ams);

        // 生成预览
        WnSysCron[][] matrix = cronApi.previewCron(list, d);

        // 找到最大的列
        int col = 0;
        for (int i = 0; i < matrix.length; i++) {
            WnSysCron[] crons = matrix[i];
            if (null != crons) {
                col = Math.max(col, crons.length);
            }
        }

        // 输出结果
        TextTable tt = new TextTable(col);
        tt.setShowBorder(true);
        tt.setCellSpacing(2);

        // 输出头
        List<String> cells = new ArrayList<>(col + 1);
        cells.add("#");
        for (int i = 0; i < col; i++) {
            cells.add("C" + i);
        }
        tt.addHr();

        // 输出体
        for (int i = 0; i < matrix.length; i++) {
            WnSysCron[] crons = matrix[i];
            cells = new ArrayList<>(col);
            cells.add(i + "");
            for (int x = 0; x < crons.length; x++) {
                WnSysCron cron = crons[x];
                cells.add(cron.toBrief());
            }
            tt.addRow(cells);
        }

        // 输出尾部
        tt.addHr();

        // 输出
        sys.out.print(tt.toString());
        sys.out.printlnf("Total %d slots, Max %d cols", matrix.length, col);

    }

}
