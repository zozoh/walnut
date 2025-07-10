package com.site0.walnut.login.site;

import org.nutz.lang.Files;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.maker.WnLoginApiMaker;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class WnLoginSite {

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
        WnLoginSite site = createByHost(io, sitePath);
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
     * 记录一个站点的配置，可能是一个目录对象，那么它的元数据就是站点登录信息 或者就是一个 JSON 文件 通常为 `~/www` 目录
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

    public WnLoginApi auth() {
        if (null == _auth) {
            // 准备读取站点设置
            WnLoginOptions options;

            // 文件的话，读取内容
            if (oSite.isFILE()) {
                options = io.readJson(oSite, WnLoginOptions.class);
            }
            // 否则直接采用元数据
            else {
                options = Wlang.map2Object(oSite, WnLoginOptions.class);
            }

            // 创建权鉴接口
            this._auth = WnLoginApiMaker.forDomain().make(io, oSite, options);

        }
        return _auth;
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
