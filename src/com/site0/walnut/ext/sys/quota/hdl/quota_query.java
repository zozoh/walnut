package com.site0.walnut.ext.sys.quota.hdl;

import org.nutz.ioc.Ioc;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.ext.sys.quota.QuotaService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class quota_query implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Ioc ioc = Mvcs.ctx().getDefaultIoc();
        QuotaService quotaService = ioc.get(QuotaService.class);
        
        // 查询还是设置呢?
        WnAccount me = sys.getMe();
        String userName = hc.params.get("u", me.getName());
        if (!me.isSameName(userName)) {
            Wn.checkRootRole(sys);
        }
        
        String type = hc.params.check("t");
        boolean realtime = "true".equals(hc.params.get("r"));
        Long used = quotaService.getUsage(type, userName, realtime);
        Long quote = quotaService.getQuota(type, userName, realtime);
        
        NutMap meta = new NutMap("used", used);
        meta.setv("quota", quote);
        meta.setv("type", type);
        meta.setv("realtime", realtime);
        sys.out.writeJson(meta);
    }

}
