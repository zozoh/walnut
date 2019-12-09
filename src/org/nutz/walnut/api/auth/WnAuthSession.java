package org.nutz.walnut.api.auth;

import org.nutz.lang.Strings;
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

    private String currentPath;

    /**
     * 创建的会话方式，可能的值为：
     * 
     * <ul>
     * <li><code>web_vcode</code>: 网页动态验证码登录， by_val 为手机号或邮箱</li>
     * <li><code>web_passwd</code>: 网页账号密码登录，by_val 为用户登录名</li>
     * <li><code>wx_gh_xxxx</code>: 某微信公众号code自动登录, by_val 为用户 OpenId</li>
     * <li><code>microapp</code>: 微信小程序，by_val 为用户 OpenId</li>
     * <li><code>session</code>: 创建的子会话，by_val 为父会话的 ID</li>
     * <li><code>transient</code>: 短暂的会话，by_val 为 null</li>
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
        this.currentPath = "~";
    }

    public WnAuthSession(String ticket, WnAccount me) {
        this();
        this.ticket = ticket;
        this.me = me;
    }

    public WnAuthSession(WnObj oSe, WnAccount me) {
        this();
        // 木有用户还有会话？笑话！
        if (null == me) {
            throw Er.create("e.auth.session.nome");
        }
        // 检查给定用户是否匹配
        String uid = oSe.getString("uid");
        if (!me.isSameId(uid)) {
            throw Er.create("e.auth.session.nomatched", uid);
        }

        // 赋值
        this.id = oSe.id();
        this.ticket = oSe.name();
        this.expi = oSe.expireTime();
        this.currentPath = oSe.getString("pwd", "~");
        this.byType = oSe.getString("by_tp");
        this.byValue = oSe.getString("by_val");

        // 设置会话所属用户，同时初始化 vars
        // 这里会根据用户的主目录加载 HOME 以及 根据 currentPath 加载环境变量 PWD
        // 因为 PWD 被假设为总是变化的，所以放到元数据里比较好，又为了兼容老的调用者预期
        // 所以 VARS 里面也放上一份
        this.setMe(me);
    }

    public NutMap toMeta() {
        NutMap map = new NutMap();
        if (null != me) {
            map.put("uid", me.getId());
            map.put("unm", me.getName());
        }
        // 当前路径
        map.put("pwd", Strings.sBlank(currentPath, "~"));
        // 过期时间
        if (expi > 0) {
            map.put("expi", expi);
        }
        // by_type
        if (!Strings.isBlank(byType)) {
            map.put("by_tp", byType);
        }
        // by_value
        if (!Strings.isBlank(byValue)) {
            map.put("by_val", byValue);
        }
        return map;
    }

    public NutMap toMapForClient() {
        NutMap map = new NutMap();
        map.put("id", id);
        map.put("ticket", ticket);
        if (this.hasParentSession()) {
            map.put("p_se_id", this.getParentSessionId());
        }
        map.put("uid", this.getMyId());
        map.put("me", this.getMyName());
        map.put("grp", this.getMyGroup());
        map.put("expi", expi);
        map.put("du", expi - System.currentTimeMillis());
        map.put("pwd", this.currentPath);
        map.put("envs", vars);
        return map;
    }

    public String getParentSessionId() {
        if ("session".equals(this.byType)) {
            return this.byValue;
        }
        return null;
    }

    public boolean hasParentSession() {
        String pseid = this.getParentSessionId();
        return !Strings.isBlank(pseid);
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

    public boolean isSameTicket(String ticket) {
        if (null != ticket) {
            return this.ticket.equals(ticket);
        }
        return false;
    }

    public boolean isParentOf(WnAuthSession se) {
        String pSeId = se.getParentSessionId();
        return this.isSameId(pSeId);
    }

    public boolean isChildOf(WnAuthSession se) {
        String pSeId = this.getParentSessionId();
        return se.isSameId(pSeId);
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
        if (null == me) {
            throw Er.create("e.auth.session.meToNull");
        }

        this.me = me;
        this.loadVars(me);
    }

    public void setMe(NutBean bean) {
        if (null == bean) {
            throw Er.create("e.auth.session.meToNull");
        }
        if (bean.isEmpty()) {
            throw Er.create("e.auth.session.meToEmpty");
        }

        this.me = new WnAccount(bean);
        this.loadVars(me);
    }

    public String getMyId() {
        if (null == me)
            return null;
        return this.me.getId();
    }

    public String getMyName() {
        if (null == me)
            return null;
        return this.me.getName();
    }

    public String getMyGroup() {
        if (null == me)
            return null;
        return this.me.getGroupName();
    }

    public void loadVars(WnAccount user) {
        NutMap map = user.getMetaMap();
        if (null == this.vars) {
            this.vars = new NutMap();
        }
        // 加入用户的环境变量
        if (null != map) {
            this.vars.putAll(map);
        }
        // 加入 PWD
        this.vars.put("PWD", this.currentPath);
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

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = Strings.sBlank(currentPath, "~");
        this.getVars().put("PWD", this.currentPath);
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
