package com.site0.walnut.ext.sys.quota.hdl;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.Mvcs;
import com.site0.walnut.ext.sys.quota.QuotaService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class quota_set implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Ioc ioc = Mvcs.ctx().getDefaultIoc();
        QuotaService quotaService = ioc.get(QuotaService.class);
        // 查询还是设置呢?
        String userName = hc.params.check("u");
        String type = hc.params.check("t");
        Wn.checkRootRole(sys);
        quotaService.setQuota(type, userName, Long.parseLong(hc.params.val_check(0)));
    }

}
