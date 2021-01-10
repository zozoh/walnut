package org.nutz.walnut.impl.srv;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.util.Wn;

/**
 * 封装了域名与站点关系的服务类。
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
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
            return io.fetch(null, "/domain/" + host);
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
        if (null != oMapping && oMapping.has("domain")) {
            return io.fetch(null, "/home/" + oMapping.getString("domain"));
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
            WnObj oMapping = this.getDomainMapping(hostName);

            // 必须要有映射
            if (null == oMapping)
                return si;

            // 映射了 domain Home
            if (oMapping.has("domain")) {
                si.oHome = io.fetch(null, "/home/" + oMapping.getString("domain"));
            }
            if (null == si.oHome)
                return si;

            // 映射里直接指定了站点名称
            if (oMapping.has("site")) {
                NutMap vars = Lang.map("HOME", si.oHome.path());
                String sitePath = oMapping.getString("site");
                sitePath = Wn.normalizeFullPath(sitePath, vars);
                si.oWWW = io.fetch(null, sitePath);
            }
            // 否则，尝试查看域目录的设置
            else {
                si.oWWW = this.getDomainDefaultWebsite(si.oHome);
            }

            // 加载更多配置
            if (null != si.oWWW) {
                si.webs = new WnWebService(io, si.oWWW);
                si.siteId = si.oWWW.id();
            }
        }
        return si;
    }

}
