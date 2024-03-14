package com.site0.walnut.ext.sys.cron.hdl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.lang.Times;
import com.site0.walnut.ext.sys.cron.WnSysCron;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.ext.sys.cron.WnSysCronQuery;
import com.site0.walnut.ext.sys.cron.cmd_cron;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.TextTable;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

@JvmHdlParamArgs("^(empty)$")
public class cron_preview implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析参数
        int slotN = hc.params.getInt("slot", 24);
        boolean showEmptySlot = hc.params.is("empty");
        WnSysCronQuery q = cmd_cron.prepareCronQuery(sys, hc);

        // 准备服务类
        WnSysCronApi cronApi = sys.services.getCronApi();

        // 执行查询
        List<WnSysCron> list = cronApi.listCron(q, true);

        // 得到今日
        String today = hc.params.getString("today", "now");
        long ams = Wtime.valueOf(today);
        Date d = new Date(ams);

        // 生成预览
        WnSysCron[][] matrix = cronApi.previewCron(list, d, slotN);

        // 得到时间槽数量，以及每个时间槽跨越的秒数
        int N = matrix.length;
        int slotSec = 86400 / N;

        // 找到最大的列
        int col = 0;
        for (int i = 0; i < matrix.length; i++) {
            WnSysCron[] crons = matrix[i];
            if (null != crons) {
                col = Math.max(col, crons.length);
            }
        }

        // 输出结果
        TextTable tt = new TextTable(col + 1);
        tt.setShowBorder(true);
        tt.setCellSpacing(2);

        // 输出头
        List<String> cells = new ArrayList<>(col + 1);
        cells.add("#");
        for (int i = 0; i < col; i++) {
            cells.add("[" + i + "]");
        }
        tt.addRow(cells);
        tt.addHr();

        // 输出体
        for (int i = 0; i < N; i++) {
            WnSysCron[] row = matrix[i];
            cells = new ArrayList<>(col);
            int sec = slotSec * i;
            Times.TmInfo ti = Times.Ti(sec);
            String I = Ws.padStart(i + "", 4, ' ');
            cells.add(I + ") " + ti.toString());
            for (int x = 0; x < col; x++) {
                String it;
                if (null == row || x >= row.length) {
                    it = "";
                } else {
                    WnSysCron cron = row[x];
                    it = cron.toBrief();
                }
                // 记入单元格
                cells.add(it);
            }
            // 记入显示的时间槽
            if (showEmptySlot || (null != row && row.length > 0)) {
                tt.addRow(cells);
            }
        }

        // 输出尾部
        tt.addHr();

        // 输出
        sys.out.print(tt.toString());
        sys.out.printlnf("Total %d slots, Max %d cols", matrix.length, col);

    }

}
