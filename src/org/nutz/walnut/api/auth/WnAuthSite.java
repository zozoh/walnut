package org.nutz.walnut.api.auth;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wn;

public class WnAuthSite {

    private String domainHomePath;

    private String siteId;

    private String sessions;

    private String captchas;

    private String accounts;

    private String roles;

    private String weixinConfPath;

    private long sessionDuration;

    /**
     * 从一个配置集合里设置各个字段信息
     * 
     * @param bean
     *            配置集合
     */
    public void valueOf(NutBean bean) {
        // 站点所在域的路径
        String d0 = bean.getString("d0");
        String d1 = bean.getString("d1");
        if ("home".equals(d0) && !Strings.isBlank(d1)) {
            domainHomePath = "/" + d0 + "/" + d1;
        }
        // 站点 ID
        siteId = bean.getString("id");
        sessions = fullPath("~/.domain/session/" + siteId);
        captchas = fullPath("~/.domain/captcha/" + siteId);
        // 账户/角色库路径
        accounts = fullPath(bean.getString("accounts"));
        roles = fullPath(bean.getString("roles"));
        // 微信配置文件路径
        String wxConfNm = bean.getString("weixin");
        if (Strings.isBlank(wxConfNm)) {
            if (bean.has("sellers")) {
                NutMap sellers = bean.getAs("sellers", NutMap.class);
                wxConfNm = sellers.getString("wx");
            }
        }
        if (!Strings.isBlank(wxConfNm)) {
            weixinConfPath = fullPath("~/.weixin/" + wxConfNm + "/wxconf");
        }
        // 默认会话时长
        sessionDuration = bean.getLong("se_du", 86400);
    }

    private String fullPath(String ph) {
        if (!Strings.isBlank(domainHomePath)) {
            NutMap vars = Lang.map("HOME", domainHomePath);
            return Wn.normalizeFullPath(ph, vars);
        }
        return ph;
    }

    public String getDomainHomePath() {
        return domainHomePath;
    }

    public void setDomainHomePath(String domainHomePath) {
        this.domainHomePath = domainHomePath;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSessions() {
        return sessions;
    }

    public void setSessions(String sessions) {
        this.sessions = sessions;
    }

    public String getCaptchas() {
        return captchas;
    }

    public void setCaptchas(String captchas) {
        this.captchas = captchas;
    }

    public String getAccounts() {
        return accounts;
    }

    public void setAccounts(String accounts) {
        this.accounts = accounts;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getWeixinConfPath() {
        return weixinConfPath;
    }

    public void setWeixinConfPath(String weixinConfPath) {
        this.weixinConfPath = weixinConfPath;
    }

    public long getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(long sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

}
