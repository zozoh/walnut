package com.site0.walnut.ext.sys.crontab;

import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;

/**
 * @author zozoh(zozohtnt@gmail.com)
 * @deprecated 已经弃用了，定时任务功能被 <code>cmd_cron</code> 接手
 */
public class cmd_crontab extends JvmHdlExecutor {
    @Override
    protected void _before_invoke(WnSystem sys, JvmHdlContext hc)
            throws Exception {
        super._before_invoke(sys, hc);
    }
}
