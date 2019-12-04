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

    private NutMap vars;

    /**
     * 创建的会话方式，可能的值为：
     * 
     * <ul>
     * <li><code>web_vcode</code>: 网页动态验证码登录， by_val 为手机号或邮箱</li>
     * <li><code>web_passwd</code>: 网页账号密码登录，by_val 为用户登录名</li>
     * <li><code>wx_gh_xxxx</code>: 某微信公众号code自动登录, by_val 为用户 OpenId</li>
     * <li><code>microapp</code>: 微信小程序，by_val 为用户 OpenId</li>
     * <li><code>session</code>: 创建的自会话，by_val 为父会话的 ID</li>
     * </ul>
     * 
     * 这个字段设计的动机是为了方便限制客户端重复登录的数量
     */
    private String byType;

    /**
     * 具体依靠什么值创建的会话
     */
    private String byValue;

    private WnAuthSession() {
        this.vars = new NutMap();
    }

    public WnAuthSession(String ticket) {
        this();
        this.ticket = ticket;
    }

    public WnAuthSession(WnObj oSe, WnObj oMe) {
        this();
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

    public NutMap toBean() {
        NutMap map = new NutMap();
        if (null != me) {
            map.put("uid", me.getId());
            map.put("unm", me.getName());
        }
        map.put("expi", expi);
        map.put("ticket", ticket);
        map.put("vars", vars);
        return map;
    }

    public NutMap toMapForClient() {
        NutMap map = new NutMap();
        map.put("id", id);
        if ("session".equals(this.byType)) {
            map.put("p_se_id", this.byValue);
        }
        map.put("me", this.getMyName());
        map.put("grp", this.getMyGroup());
        map.put("du", expi - System.currentTimeMillis());
        map.put("envs", vars);
        return map;
    }

    public WnAuthSession clone() {
        WnAuthSession se = new WnAuthSession();
        se.id = this.id;
        se.ticket = this.ticket;
        se.expi = this.expi;
        se.byType = this.byType;
        se.byValue = this.byValue;
        if (null != this.me)
            se.me = this.me.clone();
        if (null != this.vars)
            se.vars = this.vars.duplicate();
        return se;
    }

    public boolean isSame(WnAuthSession se) {
        if (null != se) {
            return this.id.equals(se.id);
        }
        return false;
    }

    public boolean isSameId(String sessionId) {
        if (null != sessionId) {
            return this.id.equals(sessionId);
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasMe() {
        return null != me;
    }

    public WnAccount getMe() {
        return me;
    }

    public void setMe(WnAccount me) {
        this.me = me;
        this.loadVars(me);
    }

    public void setMe(NutBean bean) {
        if (null == bean) {
            this.me = null;
            if (null != this.vars)
                this.vars.clear();
        } else {
            this.me = new WnAccount(bean);
            this.loadVars(me);
        }
    }

    public String getMyName() {
        return this.me.getName();
    }

    public String getMyGroup() {
        return this.me.getGroupName();
    }

    public void loadVars(WnAccount user) {
        NutMap map = user.getMetaMap();
        if (null == this.vars) {
            this.vars = new NutMap();
        }
        this.vars.putAll(map);
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

    public NutMap getVars() {
        if (null == vars) {
            vars = new NutMap();
        }
        return vars;
    }

    public void setVars(NutMap vars) {
        if (null != vars) {
            this.vars = vars;
        }
    }

}
