package org.nutz.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.www.bean.WnWebSite;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.srv.WnDomainService;
import org.nutz.walnut.impl.srv.WwwSiteInfo;
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.util.validate.WnMatch;

@JvmHdlParamArgs("cqn")
public class domain_site implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String sitePath = hc.params.val(0);
        WnDomainService domains = new WnDomainService(sys.io);
        WnObj oHome = sys.getHome();
        WwwSiteInfo si = domains.getWwwSiteInfoByHome(oHome, sitePath);

        WnWebSite site = si.webs.getSite();

        // 过滤字段
        String mk = hc.params.getString("keys", "%PID");
        WnMatch wm = Wobj.explainObjKeyMatcher(mk);

        NutMap re = new NutMap();
        addPart(re, wm, "accountHome", site.getAccountHome());
        addPart(re, wm, "roleHome", site.getRoleHome());

        if (site.hasCompanyBy()) {
            re.put("companyBy", site.getCompanyBy());
        }

        if (site.hasDeptBy()) {
            re.put("deptBy", site.getDeptBy());
        }

        if (site.hasProjectBy()) {
            re.put("projectBy", site.getProjectBy());
        }

        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

    private void addPart(NutMap re, WnMatch wm, String key, WnObj obj) {
        NutBean bean = Wobj.filterObjKeys(obj, wm);
        re.put(key, bean);
    }

}