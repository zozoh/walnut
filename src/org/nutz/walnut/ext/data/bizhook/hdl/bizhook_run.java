package org.nutz.walnut.ext.data.bizhook.hdl;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.data.bizhook.BizHook;
import org.nutz.walnut.ext.data.bizhook.BizHookGroup;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class bizhook_run implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 获取配置
        boolean isBatch = hc.params.is("batch");
        boolean fromTail = hc.params.is("tail");
        int limit = hc.params.getInt("limit", 0);

        // 如果不是批量，那么默认 limit 1
        if (!isBatch) {
            limit = 1;
        }

        // 获取数据
        BizHookGroup hgroups = hc.getAs("hooks", BizHookGroup.class);
        List<NutBean> beans = hc.getList("beans", NutBean.class);

        // 针对每个对象获取钩子
        for (NutBean bean : beans) {
            // 获取钩子
            List<BizHook> hooks = fromTail ? hgroups.getHooksFromTail(bean, limit)
                                           : hgroups.getHooks(bean, limit);

            // 详情打印
            for (BizHook bh : hooks) {
                bh.runCommands(sys, bean);
            }
        }
    }

}
