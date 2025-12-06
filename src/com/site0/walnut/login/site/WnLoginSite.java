package com.site0.walnut.login.site;

import org.nutz.lang.Files;
import org.nutz.lang.util.NutBean;
import org.nutz.trans.Proton;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.role.WnRoleLoader;
import com.site0.walnut.login.role.WnRoleStore;
import com.site0.walnut.login.role.WnRoleType;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.login.usr.WnUserStore;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.io.WnSecurityImpl;

public class WnLoginSite {

    public static WnLoginSite create(WnIo io, String siteIdOrPath, String hostName) {
        return Wn.WC().nosecurity(io, new Proton<WnLoginSite>() {
            protected WnLoginSite exec() {
                WnLoginSite site = null;
                if (!Ws.isBlank(siteIdOrPath)) {
                    site = WnLoginSite.createByPath(io, siteIdOrPath);
                }
                if (null == site && !Ws.isBlank(hostName)) {
                    site = WnLoginSite.createByHost(io, hostName);
                }
                return site;
            }
        });
    }

    public static WnLoginSite createByHost(WnIo io, String hostName) {
        // 获取域的映射信息对象
        WnObj oMapping = null;
        if (!Ws.isBlank(hostName)) {
            oMapping = io.fetch(null, "/domain/" + hostName);
        }
        // 防空
        if (null == oMapping) {
            throw Er.create("e.login.site.NoHostMapping", hostName);
        }
        String sitePath = oMapping.getString("site");
        WnLoginSite site = createByPath(io, sitePath);
        site.hostName = hostName;
        return site;
    }

    public static WnLoginSite createByPath(WnIo io, String siteIdOrPath) {
        WnLoginSite site = new WnLoginSite(io);
        // siteId 也可以是路径
        if (siteIdOrPath.startsWith("id:") || siteIdOrPath.startsWith("/")) {
            site.oSite = io.fetch(null, siteIdOrPath);
        } else {
            site.oSite = io.get(siteIdOrPath);
        }
        // 设置其他字段
        if (null != site.oSite) {
            site.domainHomePath = Wn.getObjHomePath(site.oSite);
            site.domain = Files.getName(site.domainHomePath);
        }
        // 搞定
        return site;
    }

    WnLoginSite(WnIo io) {
        this.io = io;
    }

    private WnIo io;

    /**
     * 记录一个站点的配置，可能是一个目录对象，那么它的元数据就是站点登录信息<br>
     * 或者就是一个 JSON 文件 通常为 `~/www` 目录
     */
    private WnObj oSite;

    /**
     * 站点所在域的名称
     */
    private String domain;

    /**
     * 站点所在主目录路径
     */
    private String domainHomePath;

    /**
     * 站点的域名
     */
    private String hostName;

    /**
     * 缓存创建的权鉴接口
     */
    private WnLoginApi _auth;

    /**
     * 缓存创建的权鉴选项
     */
    private WnLoginOptions _options;

    public WnLoginApi auth() {
        if (null == _auth) {
            WnLoginOptions options = getOptions();

            // 创建权鉴接口
            NutBean sessionVars = Wn.getVarsByObj(oSite);
            this._auth = WnLoginApiMaker.forDomain().make(io, sessionVars, options);

        }
        return _auth;
    }

    public WnLoginOptions getOptions() {
        if (null == _options) {
            // 文件的话，读取内容
            if (oSite.isFILE()) {
                _options = io.readJson(oSite, WnLoginOptions.class);
            }
            // 否则直接采用元数据
            else {
                _options = Wlang.map2Object(oSite, WnLoginOptions.class);
            }
        }
        return _options;
    }

    public NutBean getSessionVarsBySiteHome() {
        return Wn.getVarsByObj(this.getHomeObj());
    }

    public WnRoleLoader createRoleLoader() {
        WnLoginApi auth = auth();
        WnRoleStore roles = auth.getRoleStore();
        WnLoginApi auth2 = auth();
        WnUserStore users = auth2.getUserStore();
        return new WnRoleLoader(roles, users);
    }

    private WnObj _oHome;

    public WnObj getHomeObj() {
        if (null == _oHome) {
            this._oHome = io.check(null, this.domainHomePath);
        }
        return _oHome;
    }

    public boolean isHomeAccessable(WnSession se) {
        WnSecurity secu = new WnSecurityImpl(io, auth());
        WnObj oHome = this.getHomeObj();
        return secu.test(oHome, Wn.Io.R, se);
    }

    public void assertHomeAccessable(WnSession se) {
        if (!isHomeAccessable(se)) {
            throw Er.create("e.auth.home.forbidden");
        }
    }
    
    public boolean isRoleOfHome(WnSystem sys, WnRoleType expectRole) {
        WnUser me = sys.getMe();
        WnRoleLoader roles = sys.roles();
        return isRoleOfHome(me, roles, expectRole);
    }

    public boolean isRoleOfHome(WnUser u, WnRoleLoader loader, WnRoleType expectRole) {
        WnRoleList roles = loader.getRoles(u);
        return isRoleOfHome(roles, expectRole);
    }

    public boolean isRoleOfHome(WnRoleList roles, WnRoleType expectRole) {
        WnObj oHome = this.getHomeObj();
        WnRoleType myRole = roles.getRoleTypeOfGroup(oHome.group());

        if (myRole == expectRole) {
            return true;
        }

        if (WnRoleType.ADMIN == expectRole) {
            return roles.isAdminOfRole(oHome.group());
        }
        if (WnRoleType.MEMBER == expectRole) {
            return roles.isMemberOfRole(oHome.group());
        }
        if (WnRoleType.GUEST == expectRole) {
            return roles.isGuestOfRole(oHome.group());
        }
        if (WnRoleType.BLOCK == expectRole) {
            return roles.isBlockOfRole(oHome.group());
        }

        return false;
    }

    /**
     * 判断一个给定的用户名是否为域用户，实际上就是比对用户名与域名是否相等
     * 
     * @param userName
     *            用户名
     * @return 是否为域用户
     */
    public boolean isDomainUser(String userName) {
        return null != this.domain && this.domain.equals(userName);
    }

    public WnIo getIo() {
        return io;
    }

    public WnObj getoSite() {
        return oSite;
    }

    public String getDomain() {
        return domain;
    }

    public String getDomainHomePath() {
        return domainHomePath;
    }

    public String getHostName() {
        return hostName;
    }

}
