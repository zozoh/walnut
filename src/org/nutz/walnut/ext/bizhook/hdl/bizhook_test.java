package org.nutz.walnut.ext.bizhook.hdl;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.bizhook.BizHook;
import org.nutz.walnut.ext.bizhook.BizHookGroup;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("^(batch|tail)$")
public class bizhook_test implements JvmHdl {

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
        int i = 0;
        for (NutBean bean : beans) {
            // 多个 Bean 则显示列表
            if (beans.size() > 1)
                sys.out.printlnf("Bean[%d]", i++);

            // 获取钩子
            List<BizHook> hooks = fromTail ? hgroups.getHooksFromTail(bean, limit)
                                           : hgroups.getHooks(bean, limit);

            // 打印
            int x = 0;
            for (BizHook bh : hooks) {
                sys.out.printlnf(" - %d: %s", x++, bh.toString());
            }
        }
    }

}
