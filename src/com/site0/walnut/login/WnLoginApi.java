package com.site0.walnut.login;

import java.util.Date;
import java.util.List;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.net.xapi.bean.XApiRequest;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.login.session.WnSimpleSession;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wuu;
import com.site0.walnut.val.id.WnSnowQMaker;

public class WnLoginApi {

    private static final WnSnowQMaker TicketMaker = new WnSnowQMaker(null, 10);

    private WnUserStore users;

    private WnSessionStore sessions;

    private WnRoleStore roles;

    private WnXApi xapi;

    private String domain;

    private long sessionDuration;

    private String wechatMpOpenIdKey;

    // TODO 这里是微信公众号页面的获取 openid 方式，暂时还未实现
    // private String wechatGhOpenIdKey;

    public WnLoginApi(WnLoginSetup setup) {
        this.users = setup.users;
        this.sessions = setup.sessions;
        this.roles = setup.roles;
        this.sessionDuration = setup.sessionDuration;
        this.xapi = setup.xapi;
        this.domain = setup.domain;
        this.wechatMpOpenIdKey = setup.wechatMpOpenIdKey;
    }

    public WnRoleList getRoles(WnUser u) {
        WnRoleList list = roles.getRoles(u);
        for (WnRole r : list) {
            if (!r.hasUserName()) {
                r.setUserName(u.getName());
            }
        }
        return list;
    }

    public WnRoleList queryRolesOf(String name) {
        WnRoleList list = roles.queryRolesOf(name);
        for (WnRole r : list) {
            if (!r.hasUserName()) {
                WnUser u = users.getUserById(r.getUserId());
                if (null != u) {
                    r.setUserName(u.getName());
                }
            }
        }
        return list;
    }

    public WnRole addRole(WnUser u, String name, WnRoleType type) {
        WnRole role = this.roles.addRole(u.getId(), name, type);
        if (!role.hasUserName()) {
            role.setUserName(u.getName());
        }
        return role;
    }

    public void changePassword(WnUser u, String rawPassword) {
        users.updateUserPassword(u, rawPassword);
    }

    public void removeRole(WnRole role) {
        roles.removeRole(role);
    }

    public void removeRole(String uid, String name) {
        roles.removeRole(uid, name);
    }

    public WnSession createSession(WnUser u) {
        return createSession(u, this.getSessionDuration());
    }

    public WnSession createSession(WnUser u, long duration) {
        WnSession se = __create_session_by_user(u);
        sessions.addSession(se);
        return se;
    }

    public WnSession createSession(WnSession parentSe, WnUser u, long duration) {
        WnSession se = __create_session_by_user(u);
        se.setParentTicket(parentSe.getTicket());
        sessions.addSession(se);
        return se;
    }

    public WnSession removeSession(WnSession se) {
        if (null == se) {
            return se;
        }
        return sessions.reomveSession(se, users);
    }

    public WnSession loginByPassword(String nameOrPhoneOrEmail, String rawPassword) {
        WnUser u = users.getUser(nameOrPhoneOrEmail);
        if (null == u) {
            throw Er.create("e.auth.login.Failed");
        }
        String saltedPassword = Wn.genSaltPassword(rawPassword, u.getSalt());
        if (saltedPassword.equals(u.getPasswd())) {
            WnSession se = __create_session_by_user(u);
            sessions.addSession(se);
            return se;
        }
        throw Er.create("e.auth.login.Failed");
    }

    public WnSession loginByWechatMPCode(String code, boolean autoCreateUser) {
        String apiName = "wxmp";
        String account = this.domain;
        String path = "jscode2session";
        // boolean force = false;

        NutMap vars = Wlang.map("code", code);
        XApiRequest req = xapi.prepare(apiName, account, path, vars, false);
        req.setDisableCache(true);

        NutMap result = xapi.send(req, NutMap.class);
        String openid = result.getString("openid");

        // 看看是否成功的获取了用户的 openid
        // 没有 open id，那么必然是错误
        if (Ws.isBlank(openid)) {
            throw Er.create("e.auth.session.loginByWechatMPCode.Faild", result);
        }

        // 根据用户的 openid 找回账号
        WnQuery q = new WnQuery();
        q.setv(this.wechatMpOpenIdKey, openid);

        // 自动创建用户
        WnUser autoUser = null;
        if (autoCreateUser) {
            autoUser = new WnSimpleUser();
            autoUser.setName("bywxmp_" + Wuu.UU32());
            users.patchDefaultEnv(autoUser);
            autoUser.putMetas(Wlang.map(this.wechatMpOpenIdKey, openid));
        }

        return add_session_by_openid(q, autoUser);
    }

    private WnSession add_session_by_openid(WnQuery q, WnUser autoUser) {
        WnUser u = users.getUser(q);

        // 是否自动创建账号
        if (null == u && null != autoUser) {
            u = users.addUser(autoUser);
        }

        // 账号不存在
        if (null == u) {
            throw Er.create("e.auth.account.noexists", q.toString());
        }

        // 创建会话
        WnSession se = new WnSimpleSession(u, this.sessionDuration);
        sessions.addSession(se);

        // 搞定
        return se;
    }

    private WnSession __create_session_by_user(WnUser u) {
        WnSimpleSession se = new WnSimpleSession();
        se.setExpiAt(System.currentTimeMillis() + sessionDuration);
        se.setUser(u);
        se.setTicket(TicketMaker.make(new Date(), null));
        return se;
    }

    public WnSession logout(String ticket) {
        WnSession se = sessions.getSession(ticket, users);
        return removeSession(se);
    }

    public UserRace getUserRace() {
        return users.getUserRace();
    }

    public WnUser addUser(WnUser u) {
        return users.addUser(u);
    }

    public List<WnUser> queryUser(WnQuery q) {
        return users.queryUser(q);
    }

    public WnUser getUser(String nameOrPhoneOrEmail) {
        return users.getUser(nameOrPhoneOrEmail);
    }

    public WnUser checkUser(String nameOrPhoneOrEmail) {
        return users.checkUser(nameOrPhoneOrEmail);
    }

    public WnUser getUser(WnUser info) {
        return users.getUser(info);
    }

    public WnUser checkUser(WnUser info) {
        return users.checkUser(info);
    }

    public WnUser getUserById(String uid) {
        return users.getUserById(uid);
    }

    public WnUser checkUserById(String uid) {
        return users.checkUserById(uid);
    }

    public void saveUserMeta(WnUser u) {
        users.saveUserMeta(u);
    }

    public void updateUserName(WnUser u) {
        users.updateUserName(u);
    }

    public void updateUserPhone(WnUser u) {
        users.updateUserPhone(u);
    }

    public void updateUserEmail(WnUser u) {
        users.updateUserEmail(u);
    }

    public void updateUserLastLoginAt(WnUser u) {
        users.updateUserLastLoginAt(u);
    }

    public void updateUserPassword(WnUser u, String rawPassword) {
        users.updateUserPassword(u, rawPassword);
    }

    public WnSession getSession(String ticket) {
        return sessions.getSession(ticket, users);
    }

    public WnSession checkSession(String ticket) {
        WnSession se = sessions.getSession(ticket, users);
        if (null == se) {
            throw Er.create("e.auth.session.NoExists", ticket);
        }
        return se;
    }

    public void saveSessionEnv(WnSession se) {
        sessions.saveSessionEnv(se);
    }

    public void touchSession(WnSession se, long sessionDuration) {
        sessions.touchSession(se, sessionDuration);
    }

    public long getSessionDuration() {
        return sessionDuration;
    }

    public long getSessionDuration(boolean longSession) {
        if (longSession) {
            return sessionDuration;
        }
        return 3000L;
    }

}
