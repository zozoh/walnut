package com.site0.walnut.ext.data.www.bean;

import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

public class WnWebSession {

    private String id;

    private WnObj me;

    private String ticket;

    private long expi;

    /**
     * 创建的会话方式，可能的值为：
     * 
     * <ul>
     * <li><code>web_vcode</code>: 验证码登录</li>
     * <li><code>web_passwd</code>: 密码登录</li>
     * <li><code>wx_gh_xxxx</code>: 某个微信公众号登录</li>
     * </ul>
     * 
     * 这个字段设计的动机是为了方便限制客户端重复登录的数量
     */
    private String byType;

    /**
     * 具体依靠什么值创建的会话
     */
    private String byValue;

    public WnWebSession(String ticket) {
        this.ticket = ticket;
    }

    public WnWebSession(WnObj oSe, WnObj oMe) {
        String uid = oSe.getString("uid");
        if (!oMe.isSameId(uid)) {
            throw Er.create("e.www.session.nomatched", uid);
        }
        this.id = oSe.id();
        this.me = oMe;
        this.ticket = oSe.name();
        this.expi = oSe.expireTime();
        this.byType = oSe.getString("by_tp");
        this.byValue = oSe.getString("by_val");
    }

    public NutMap toMeta() {
        NutMap map = new NutMap();
        if (null != me) {
            map.put("uid", me.id());
            map.put("unm", me.name());
        }
        map.put("expi", expi);
        return map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WnObj getMe() {
        return me;
    }

    public void setMe(WnObj me) {
        this.me = me;
    }

    public String getUserId() {
        return me.id();
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public long getExpi() {
        return expi;
    }

    public void setExpi(long expi) {
        this.expi = expi;
    }

    public String getByType() {
        return byType;
    }

    public String getByValue() {
        return byValue;
    }

    public NutBean getUserInfo() {
        if (null == this.me) {
            return new NutMap();
        }
        String locked = "^(nm|ph|race|tp|mime|pid|len|sha1|ct|lm|"
                        + "c|m|g|md|ph|th_set|th_live|d0|d1||passwd|salt)$";
        return this.me.pickBy(Pattern.compile(locked), true);
    }

    public String formatJson(JsonFormat jfmt, boolean ajax) {
        jfmt.setLocked("^(ph|race|tp|mime|pid|len|sha1|ct|lm|c|m|g|md|ph|th_set|th_live|"
                       + "d0|d1||passwd|salt|oauth_.+|wx_.+)$");
        jfmt.setIgnoreNull(true);
        if (ajax) {
            AjaxReturn re = Ajax.ok().setData(this);
            return Json.toJson(re, jfmt);
        }
        return Json.toJson(this, jfmt);
    }

}
