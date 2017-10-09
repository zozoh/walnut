package org.nutz.walnut.ext.quota.hdl;

import org.nutz.ioc.Ioc;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.quota.QuotaService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class quota_network implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Ioc ioc = Mvcs.ctx().getDefaultIoc();
        QuotaService quotaService = ioc.get(QuotaService.class);
        // 查询还是设置呢?
        String hostname = hc.params.check("hostname");
        if (hc.params.has("set")) {
            Wn.checkRootRole(sys);
            quotaService.setHostnameNetworkQuota(hostname, hc.params.getLong("set"));
        } else {
            long used = quotaService.getHostnameNetworkUsage(hostname);
            long quote = quotaService.getHostnameNetworkQuota(hostname);
            sys.out.writeJson(new NutMap("used", used).setv("quote", quote));
        }
    }

}
