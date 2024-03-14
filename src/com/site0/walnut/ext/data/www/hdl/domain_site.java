package com.site0.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.www.bean.WnWebSite;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.srv.WnDomainService;
import com.site0.walnut.impl.srv.WwwSiteInfo;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.validate.WnMatch;

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
        re.put("organization", site.getOrganization());
        re.put("pvgOwner", site.getPvgOwner());
        re.put("pvgMember", site.getPvgMember());

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
