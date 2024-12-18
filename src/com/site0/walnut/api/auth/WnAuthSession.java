package com.site0.walnut.api.auth;

import java.util.Collection;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

public class WnAuthSession {

    public static final String V_BT_AUTH_BY_DOMAIN = "auth_by_domain";
    public static final String V_BT_WEB_VCODE = "web_vcode";
    public static final String V_BT_WEB_PASSWD = "web_passwd";
    public static final String V_BT_MICROAPP = "microapp";
    public static final String V_BT_SESSION = "session";
    public static final String V_BT_TRANSIENT = "transient";

    public static final String V_WWW_SITE_ID = "WWW_SITE_ID";
    public static final String V_ROLE = "ROLE";
    public static final String V_DEPT = "DEPT";
    public static final String V_JOBS = "JOBS";
    public static final String K_PSE_ID = "pse_id";

    private String id;

    private WnAccount me;

    private String ticket;

    private long expi;

    private int durationInSec;

    private boolean dead;

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

    /**
     * 会话的数据对象
     */
    private WnObj obj;

    private WnAuthSession() {
        this.vars = new NutMap();
        this.currentPath = "~";
    }

    public WnAuthSession(String ticket, WnAccount me) {
        this();
        this.ticket = ticket;
        this.me = me;
        this.vars = new NutMap();

        // 记录 IP
        String ipv4 = Wlang.getIP(Mvcs.getReq(), false);
        this.vars.put("CLINET_IP", ipv4);
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
        this.durationInSec = oSe.getInt("duration", 0);
        this.dead = oSe.getBoolean("dead");
        this.currentPath = oSe.getString("pwd", "~");
        this.byType = oSe.getString("by_tp");
        this.byValue = oSe.getString("by_val");

        // 保存原始对象
        this.setObj(oSe);

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
        // 持续时间
        if (expi > 0) {
            map.put("duration", durationInSec);
        }
        // Dead
        if (this.dead) {
            map.put("dead", true);
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
            map.put("pse_id", this.getParentSessionId());
        }
        map.put("uid", this.getMyId());
        map.put("unm", this.getMyName());
        map.put("me", this.getMe().toBeanForClient());
        map.put("grp", this.getMyGroup());
        map.put("by_tp", this.getByType());
        map.put("by_val", this.getByValue());
        map.put("expi", expi);
        map.put("du", expi - Wn.now());
        map.put("pwd", this.currentPath);
        map.put("envs", vars);
        return map;
    }

    public String getParentSessionId() {
        // 2023-04-24: 采用 by_type/value
        // 方式来决定父会话ID 一定是我当时脑袋被门挤了
        // 这个必须是用一个专有的元数据据呀！
        // if ("session".equals(this.byType)) {
        // return this.byValue;
        // }
        return this.obj.getString(K_PSE_ID);
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
        se.dead = this.dead;

        se.byType = this.byType;
        se.byValue = this.byValue;

        if (null != this.me)
            se.me = this.me.clone();

        if (null != this.vars)
            se.vars = this.vars.duplicate();

        if (null != this.obj)
            se.obj = this.obj.clone();

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
            for (Map.Entry<String, Object> en : map.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                // 无视空值
                if (null == val) {
                    continue;
                }
                // 无视私有键
                if (key.startsWith("__")) {
                    continue;
                }
                // 数组或者集合，值需要拼合为字符串
                if (val.getClass().isArray()) {
                    val = Ws.join((Object[]) val, ",");
                } else if (val instanceof Collection<?>) {
                    val = Ws.join((Collection<?>) val, ",");
                }
                // 强制大写
                String k2 = key.toUpperCase();
                this.vars.put(k2, val);
            }
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

    public int getDurationInSec() {
        return durationInSec;
    }

    public void setDurationInSec(int durationInSec) {
        this.durationInSec = durationInSec;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
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

    public WnObj getObj() {
        return obj;
    }

    public void setObj(WnObj obj) {
        this.obj = obj;
    }

    public String formatJson(JsonFormat jfmt, boolean ajax) {
        NutMap bean = this.toMapForClient();
        if (ajax) {
            AjaxReturn re = Ajax.ok().setData(bean);
            return Json.toJson(re, jfmt);
        }
        return Json.toJson(bean, jfmt);
    }

}
