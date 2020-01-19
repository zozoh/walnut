package org.nutz.walnut.impl.srv;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.util.Wn;

public class WnDomainService {

    private WnIo io;

    public WnDomainService(WnIo io) {
        this.io = io;
    }

    /**
     * @param host
     *            域名，类似 <code>www.your_domain.com</code>
     * @return 域名映射对象（存放在 <code>/domain</code>目录下）
     */
    public WnObj getDomainMapping(String host) {
        // 首先从 domain 表里查询 hostName 对应的域
        if (!Strings.isBlank(host)) {
            WnObj oDomainHome = io.fetch(null, "/domain");
            if (null != oDomainHome) {
                WnQuery q = Wn.Q.pid(oDomainHome);
                q.setv("dmn_host", host);
                return io.getOne(q);
            }
        }
        return null;
    }

    /**
     * @param host
     *            域名，类似 <code>www.your_domain.com</code>
     * @return 域主目录对象（存放在 <code>/home</code>目录下）
     */
    public WnObj getDomainHome(String host) {
        WnObj oMapping = this.getDomainMapping(host);
        if (null != oMapping && oMapping.has("dmn_grp")) {
            return io.fetch(null, "/home/" + oMapping.getString("dmn_grp"));
        }
        return null;
    }

    public WnObj getDomainDefaultWebsite(String host) {
        WnObj oHome = this.getDomainHome(host);
        return getDomainDefaultWebsite(oHome);
    }

    public WnObj getDomainDefaultWebsite(WnObj oHome) {
        if (null != oHome && oHome.has("auth_site")) {
            String homePath = oHome.path();
            String sitePath = oHome.getString("auth_site");
            NutMap vars = Lang.map("HOME", homePath).setv("PWD", homePath);
            String aph = Wn.normalizeFullPath(sitePath, vars);
            return io.fetch(null, aph);
        }
        return null;
    }

    public WwwSiteInfo getWwwSiteInfo(String siteId, String hostName) {
        WwwSiteInfo si = new WwwSiteInfo();
        // 直接指明了站点 ID
        if (!Strings.isBlank(siteId)) {
            si.oWWW = io.get(siteId);
            if (null != si.oWWW) {
                si.webs = new WnWebService(io, si.oWWW);
                String domainHomePath = si.webs.getSite().getDomainHomePath();
                si.oHome = io.check(null, domainHomePath);
                si.siteId = si.oWWW.id();
            }
        }
        // 首先从 domain 表里查询 hostName 对应的域
        else {
            si.oHome = this.getDomainHome(hostName);
            si.oWWW = this.getDomainDefaultWebsite(si.oHome);
            if (null != si.oWWW) {
                si.webs = new WnWebService(io, si.oWWW);
                si.siteId = si.oWWW.id();
            }
        }
        return si;
    }

}
