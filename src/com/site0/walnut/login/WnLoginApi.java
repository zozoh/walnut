package com.site0.walnut.login;

import java.util.Date;
import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.net.xapi.bean.XApiRequest;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.login.role.WnRole;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.role.WnRoleStore;
import com.site0.walnut.login.role.WnRoleType;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.session.WnSessionStore;
import com.site0.walnut.login.session.WnSimpleSession;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.login.usr.WnUserStore;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wuu;
import com.site0.walnut.val.id.WnSnowQMaker;

public class WnLoginApi {

    private static final WnSnowQMaker TicketMaker = new WnSnowQMaker(null, 10);

    private static Log log = Wlog.getAUTH();

    private WnIo io;

    WnUserStore users;

    WnSessionStore sessions;

    WnRoleStore roles;

    WnXApi xapi;

    String domain;

    long sessionDuration;

    String wechatMpOpenIdKey;

    // TODO 这里是微信公众号页面的获取 openid 方式，暂时还未实现
    String wechatGhOpenIdKey;

    public WnLoginApi(WnIo io) {
        this.io = io;
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

    public WnRoleType getRoleTypeOfGroup(WnUser u, String group) {
        WnRoleList list = roles.getRoles(u);
        return list.getRoleTypeOfGroup(group);
    }

    public WnRole addRole(WnUser u, String grp, WnRoleType type) {
        WnRoleList roles = this.roles.getRoles(u);
        WnRole r = roles.getRole(grp);
        // 已经存在了
        if (null != r && r.getType() == type) {
            return r;
        }
        // 存在，但是类型不同，需要更新
        if (null != r) {
            return this.roles.setRole(u.getId(), grp, type, u.getName());
        }
        // 添加
        return this.roles.addRole(u.getId(), grp, type, u.getName());
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

    public WnSession createSession(WnUser u, long du) {
        WnSession se = __create_session_by_user(u, du);
        sessions.addSession(se);
        return se;
    }

    public WnSession createSession(WnSession parentSe, WnUser u, long du) {
        WnSession se = __create_session_by_user(u, du);
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
            WnSession se = __create_session_by_user(u, this.sessionDuration);
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

    private WnSession __create_session_by_user(WnUser u, long du) {
        WnSimpleSession se = new WnSimpleSession(u, du);
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

    public WnUser addRootUserIfNoExists(String dftPassword) {
        WnUser root = getUser("root");
        if (null == root) {
            root = new WnSimpleUser("root");
            root.genSaltAndRawPasswd(dftPassword);
            root = addUser(root);
            addRole(root, "root", WnRoleType.ADMIN);
            log.infof("init root usr: %s", root.getId());
        } else {
            log.infof("already exists root usr: %s", root.getId());
        }
        return root;
    }

    public WnUser addGuestUserIfNoExists() {
        WnUser guest = getUser("guest");
        if (null == guest) {
            guest = new WnSimpleUser("guest");
            guest = addUser(guest);
            addRole(guest, "guest", WnRoleType.MEMBER);
            log.infof("init guest usr: %s", guest.getId());
        } else {
            log.infof("already exists guest usr: %s", guest.getId());
        }
        return guest;
    }

    public WnUser addUser(WnUser u) {
        // 必须有登录名
        if (Ws.isBlank(u.getName())) {
            throw Er.create("e.auth.addUser.WithoutName");
        }
        // 默认主组为自己的名称
        if (Ws.isBlank(u.getMainGroup())) {
            u.setMainGroup(u.getName());
        }
        u = users.addUser(u);

        // 确保有主目录
        WnObj oUsrHome = io.createIfExists(null, u.getHomePath(), WnRace.DIR);
        NutMap delta = new NutMap();
        delta.put("c", u.getName());
        delta.put("m", u.getName());
        delta.put("g", u.getName());
        delta.put("md", 488); // oct: 750
        io.appendMeta(oUsrHome, delta);

        // 建立自己的额默认角色
        addRole(u, u.getMainGroup(), WnRoleType.ADMIN);

        // 搞定
        return u;
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

    public WnObj updateUserHomeName(WnUser u, String newName) {
        WnObj oHome = io.check(null, u.getHomePath());
        if (oHome.isSameName(newName)) {
            return oHome;
        }
        oHome = io.rename(oHome, newName);
        u.setHomePath(oHome.path());
        users.saveUserMeta(u);
        return oHome;
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
