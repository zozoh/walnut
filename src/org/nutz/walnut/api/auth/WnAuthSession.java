package org.nutz.walnut.api.auth;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;

public class WnAuthSession {

    private String id;

    private WnAccount me;

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

    public WnAuthSession(String ticket) {
        this.ticket = ticket;
    }

    public WnAuthSession(WnObj oSe, WnObj oMe) {
        if (null != oMe) {
            String uid = oSe.getString("uid");
            if (!oMe.isSameId(uid)) {
                throw Er.create("e.auth.session.nomatched", uid);
            }
            this.me = new WnAccount(oMe);
        }
        this.id = oSe.id();
        this.ticket = oSe.name();
        this.expi = oSe.expireTime();
        this.byType = oSe.getString("by_tp");
        this.byValue = oSe.getString("by_val");
    }

    public NutMap toMeta() {
        NutMap map = new NutMap();
        if (null != me) {
            map.put("uid", me.getId());
            map.put("unm", me.getName());
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

    public WnAccount getMe() {
        return me;
    }

    public void setMe(WnAccount me) {
        this.me = me;
    }

    public void setMe(NutBean bean) {
        if (null == bean) {
            this.me = null;
        } else {
            this.me = new WnAccount(bean);
        }
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

    public void setByType(String byType) {
        this.byType = byType;
    }

    public String getByValue() {
        return byValue;
    }

    public void setByValue(String byValue) {
        this.byValue = byValue;
    }

}
