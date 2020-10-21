package org.nutz.walnut.ext.entity.statistics.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.entity.statistics.agg.EachAggregation;
import org.nutz.walnut.ext.entity.statistics.agg.WnStatAggConfig;
import org.nutz.walnut.ext.entity.statistics.agg.WnStatAgg;
import org.nutz.walnut.ext.entity.statistics.bean.AmsRange;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value = "cqnbish", regex = "^(json|quiet|test|force)$")
public class statistics_agg implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 读取配置文件
        String phConf = hc.params.val_check(0);
        WnObj oConf = Wn.checkObj(sys, phConf);
        WnStatAggConfig config = sys.io.readJson(oConf, WnStatAggConfig.class);

        // 加载服务类
        WnStatAgg agg = new WnStatAgg(config, sys);

        // 分析参数
        agg.updateByParams(hc.params);

        // 分析时间范围
        AmsRange range = agg.getRangeFrom(hc.params);

        // 准备回调
        List<NutBean> list = new LinkedList<>();
        EachAggregation callback = null;
        if (!hc.params.is("quiet")) {
            callback = new EachAggregation() {
                public void invoke(String name, List<NutBean> records) {
                    list.addAll(records);
                }
            };
        }

        // 执行聚集
        agg.invoke(range, callback);

        // 输出结果
        if (null != callback) {
            Cmds.output_beans(sys, hc.params, null, list);
        }
    }

}
