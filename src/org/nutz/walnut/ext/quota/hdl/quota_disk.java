package org.nutz.walnut.ext.quota.hdl;

import org.nutz.ioc.Ioc;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.quota.QuotaService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class quota_disk implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Ioc ioc = Mvcs.ctx().getDefaultIoc();
        QuotaService quotaService = ioc.get(QuotaService.class);
        // 查询还是设置呢?
        String userName = hc.params.get("u", sys.me.name());
        if (!userName.equals(sys.me.name()) || hc.params.has("set")) {
            Wn.checkRootRole(sys);
        }
        if (hc.params.has("set")) {
            Wn.checkRootRole(sys);
            quotaService.setUserDiskQuota(userName, hc.params.getLong("set"));
        } else {
            long used = quotaService.getUserDiskUsage(userName);
            long quote = quotaService.getUserDiskQuota(userName);
            sys.out.writeJson(new NutMap("used", used).setv("quote", quote));
        }
    }

}
