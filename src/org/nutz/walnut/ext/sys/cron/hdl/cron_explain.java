package org.nutz.walnut.ext.sys.cron.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Times;
import org.nutz.walnut.cron.CronOverlapor;
import org.nutz.walnut.cron.WnCron;
import org.nutz.walnut.cron.WnCroni18n;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wtime;

public class cron_explain implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 预览要多少个时间槽？
        int slotN = hc.params.getInt("slot", 24);
        String today = hc.params.getString("today", "now");
        long ams = Wtime.valueOf(today);

        // 解析表达式
        WnCron[] crons = new WnCron[hc.params.vals.length];
        for (int i = 0; i < hc.params.vals.length; i++) {
            String v = hc.params.val(i);
            WnCron cron = new WnCron(v);
            crons[i] = cron;
        }

        // 准备语言
        String lang = sys.session.getVars().getString("LANG", "zh-cn");
        lang = hc.params.getString("lang", lang);

        // 得到多国语言字符串
        WnCroni18n i18n = WnCroni18n.getInstance(lang);

        // 逐个输出
        String HR = Ws.repeat('-', 60);
        CronOverlapor[] overs = new CronOverlapor[slotN];
        for (int i = 0; i < crons.length; i++) {
            WnCron cron = crons[i];
            String it = String.format("C%d", i);
            cron.overlapBy(overs, it, ams);
            String str = cron.toText(i18n);
            sys.out.println(cron.toString());
            sys.out.println(str);
            sys.out.println(HR);
        }

        // 变成二维数组
        Object[][] matrix = WnCron.toMatrix(overs);

        // 输出表格
        output_cron_matrix(sys, matrix);

    }

    private void output_cron_matrix(WnSystem sys, Object[][] matrix) {
        // 得到时间槽数量，以及每个时间槽跨越的秒数
        int N = matrix.length;
        int slotSec = 86400 / N;

        // 分析并准备输出表格
        int col = WnCron.getMaxColCount(matrix);
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
            Object[] row = matrix[i];
            cells = new ArrayList<>(col);
            int sec = slotSec * i;
            Times.TmInfo ti = Times.Ti(sec);
            cells.add(i + ") " + ti.toString());
            for (int x = 0; x < col; x++) {
                Object it;
                if (null == row || x >= row.length) {
                    it = "";
                } else {
                    it = row[x];
                }
                cells.add(it.toString());
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
