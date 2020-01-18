package org.nutz.walnut.impl.srv;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
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
        if (null != oHome && oHome.has("auth_site")) {
            String sitePath = oHome.path();
            NutMap vars = Lang.map("HOME", sitePath).setv("PWD", sitePath);
            String aph = Wn.normalizeFullPath(sitePath, vars);
            return io.fetch(null, aph);
        }
        return null;
    }

}
