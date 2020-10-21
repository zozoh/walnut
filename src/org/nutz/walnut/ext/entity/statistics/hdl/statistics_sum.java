package org.nutz.walnut.ext.entity.statistics.hdl;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.entity.statistics.agg.WnStatAgg;
import org.nutz.walnut.ext.entity.statistics.agg.WnStatAggConfig;
import org.nutz.walnut.ext.entity.statistics.bean.AmsRange;
import org.nutz.walnut.ext.entity.statistics.sum.WnStatSum;
import org.nutz.walnut.ext.entity.statistics.sum.WnStatSumConfig;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value = "cqnbish", regex = "^(json|quiet|test|force|agg-force)$")
public class statistics_sum implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 读取配置文件
        String phConf = hc.params.val_check(0);
        WnObj oConf = Wn.checkObj(sys, phConf);
        WnStatSumConfig config = sys.io.readJson(oConf, WnStatSumConfig.class);

        // 加载服务类
        WnStatSum sum = new WnStatSum(config, sys);

        // 分析参数
        sum.updateByParams(hc.params);

        // 分析时间范围
        AmsRange range = sum.getRangeFrom(hc.params);

        // 尝试首先聚合数据
        String phAggConf = hc.params.getString("agg");
        if (!Strings.isBlank(phAggConf)) {
            WnObj oAggConf = Wn.checkObj(sys, phAggConf);
            WnStatAggConfig aggConfig = sys.io.readJson(oAggConf, WnStatAggConfig.class);

            // 加载服务类
            WnStatAgg agg = new WnStatAgg(aggConfig, sys);
            agg.setForce(hc.params.is("agg-force"));
            agg.setTest(false);

            // 尝试聚合
            agg.invoke(range, null);
        }

        // 计算求和
        List<NutMap> list = sum.invoke(range);
        list = sum.normalizeList(list);

        // 输出结果(强制输出列表)
        hc.params.setv("l", true);
        Cmds.output_beans(sys, hc.params, null, list);
    }

}
