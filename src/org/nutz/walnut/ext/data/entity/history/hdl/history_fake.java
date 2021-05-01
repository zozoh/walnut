package org.nutz.walnut.ext.data.entity.history.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.data.entity.history.HistoryApi;
import org.nutz.walnut.ext.data.entity.history.HistoryRecord;
import org.nutz.walnut.ext.data.entity.history.WnHistoryService;
import org.nutz.walnut.ext.data.entity.history.fake.HistoryFakeConfig;
import org.nutz.walnut.ext.data.entity.history.fake.HistoryFaker;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

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
        long spanInMs = Wn.msValueOf(span);
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
