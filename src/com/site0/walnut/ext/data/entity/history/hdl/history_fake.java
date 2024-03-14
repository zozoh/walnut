package com.site0.walnut.ext.data.entity.history.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.ext.data.entity.history.HistoryApi;
import com.site0.walnut.ext.data.entity.history.HistoryRecord;
import com.site0.walnut.ext.data.entity.history.WnHistoryService;
import com.site0.walnut.ext.data.entity.history.fake.HistoryFakeConfig;
import com.site0.walnut.ext.data.entity.history.fake.HistoryFaker;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wtime;

@JvmHdlParamArgs(value = "cqnbish", regex = "^(json|quiet|test)$")
public class history_fake implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 参数
        int nb = hc.params.getInt("N", 10);
        int seq = hc.params.getInt("seq", 0);
        int pad = hc.params.getInt("pad", 6);
        boolean test = hc.params.is("test");

        // 参数:时间范围:结束日期
        String date = hc.params.getString("date", "now-1d");
        long endInMs = Wn.evalDateMs(date);

        // 参数:时间范围:开始日期
        String span = hc.params.getString("span", "7d");
        long spanInMs = Wtime.millisecond(span);
        long beginInMs = endInMs - spanInMs;

        // 确保时间区间是正向的
        if (beginInMs > endInMs) {
            long ms = beginInMs;
            beginInMs = endInMs;
            endInMs = ms;
        }

        // 准备配置文件
        HistoryFakeConfig config = Cmds.readConfig(sys, hc.params, HistoryFakeConfig.class);

        // 准备接口
        HistoryApi api = hc.getAs("api", WnHistoryService.class);
        HistoryFaker faker = new HistoryFaker(config, sys);
        faker.setBeginInMs(beginInMs);
        faker.setEndInMs(endInMs);
        faker.prepareSchema();

        // 准备历史记录
        List<HistoryRecord> list = faker.getRecords(seq, nb, pad);

        // 依次插入
        List<NutBean> beans = new ArrayList<>(list.size());
        for (HistoryRecord his : list) {
            if (test) {
                beans.add(his.toBean());
            } else {
                HistoryRecord his2 = api.add(his);
                beans.add(his2.toBean());
            }
        }

        // 输出
        if (!hc.params.is("quiet")) {
            Cmds.output_beans(sys, hc.params, null, beans);
        }
    }

}
