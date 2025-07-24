package com.site0.walnut.login;

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
import com.site0.walnut.login.role.WnRoleLoader;
import com.site0.walnut.login.role.WnRoleStore;
import com.site0.walnut.login.role.WnRoleType;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.session.WnSessionStore;
import com.site0.walnut.login.session.WnSimpleSession;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.login.usr.WnUserStore;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wuu;

/**
 * 提供一个基本权鉴的接口的逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSimpleLoginApi implements WnLoginApi {

    private static Log log = Wlog.getAUTH();

    private WnIo io;

    WnUserStore users;

    WnSessionStore sessions;

    WnRoleStore roles;

    WnXApi xapi;

    String site;

    String domain;

    /**
     * 标准会话的时长（秒）
     */
    int sessionDuration;

    /**
     * 临时会话的时长（秒）
     */
    int sessionShortDu;

    String wechatMpOpenIdKey;

    // TODO 这里是微信公众号页面的获取 openid 方式，暂时还未实现
    String wechatGhOpenIdKey;

    public WnSimpleLoginApi(WnIo io) {
        this.io = io;
    }

    @Override
    public WnUserStore getUserStore() {
        return users;
    }

    @Override
    public WnSessionStore getSessionStore() {
        return sessions;
    }

    @Override
    public WnRoleStore getRoleStore() {
        return roles;
    }

    @Override
    public WnXApi getXapi() {
        return xapi;
    }

    @Override
    public String getWechatMpOpenIdKey() {
        return wechatMpOpenIdKey;
    }

    @Override
    public String getWechatGhOpenIdKey() {
        return wechatGhOpenIdKey;
    }

    @Override
    public int getSessionDuration() {
        return sessionDuration;
    }

    @Override
    public int getSessionDuration(boolean longSession) {
        if (longSession) {
            return sessionDuration;
        }
        return this.sessionShortDu;
    }

    @Override
    public WnRoleLoader roleLoader(WnSession se) {
        String mySite = this.site;
        if (null != se && se.hasSite()) {
            mySite = se.getSite();
        }
        if (Ws.isBlank(mySite)) {
            return new WnRoleLoader(roles, users);
        }
        // 根据站点建立
        WnLoginSite site = WnLoginSite.create(io, mySite, null);
        return site.createRoleLoader();
    }

    // @Override
    // public WnRoleList getRoles(WnUser u) {
    // WnRoleList list = roles.getRoles(u);
    // for (WnRole r : list) {
    // if (!r.hasUserName()) {
    // r.setUserName(u.getName());
    // }
    // }
    // return list;
    // }
    //
    // @Override
    // public WnRoleList queryRolesOf(String name) {
    // WnRoleList list = roles.queryRolesOf(name);
    // for (WnRole r : list) {
    // if (!r.hasUserName()) {
    // WnUser u = users.getUserById(r.getUserId());
    // if (null != u) {
    // r.setUserName(u.getName());
    // }
    // }
    // }
    // return list;
    // }
    //
    // @Override
    // public WnRoleType getRoleTypeOfGroup(WnUser u, String group) {
    // WnRoleList list = roles.getRoles(u);
    // return list.getRoleTypeOfGroup(group);
    // }

    @Override
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

    @Override
    public void changePassword(WnUser u, String rawPassword) {
        users.updateUserPassword(u, rawPassword);
    }

    @Override
    public void removeRole(WnRole role) {
        roles.removeRole(role);
    }

    @Override
    public void removeRole(String uid, String name) {
        roles.removeRole(uid, name);
    }

    @Override
    public WnSession createSession(WnUser u, String type) {
        return createSession(u, type, this.getSessionDuration());
    }

    @Override
    public WnSession createSession(WnUser u, String type, int duInSec) {
        WnSession se = __create_session_by_user(u, type, duInSec);
        sessions.addSession(se);
        return se;
    }

    @Override
    public WnSession createSession(WnSession parentSe, WnUser u, String type, int duInSec) {
        WnSession se = __create_session_by_user(u, type, duInSec);
        se.setParentTicket(parentSe.getTicket());
        sessions.addSession(se);
        return se;
    }

    @Override
    public WnSession removeSession(WnSession se) {
        if (null == se) {
            return se;
        }
        return sessions.reomveSession(se, users);
    }

    @Override
    public WnSession loginByPassword(String nameOrPhoneOrEmail, String rawPassword) {
        WnUser u = users.getUser(nameOrPhoneOrEmail);
        if (null == u) {
            throw Er.create("e.auth.login.Failed");
        }
        String saltedPassword = Wn.genSaltPassword(rawPassword, u.getSalt());
        if (saltedPassword.equals(u.getPasswd())) {
            WnSession se = __create_session_by_user(u, Wn.SET_AUTH_PASS, this.sessionDuration);
            sessions.addSession(se);
            return se;
        }
        throw Er.create("e.auth.login.Failed");
    }

    @Override
    public WnSession loginByWechatMPCode(String code, boolean autoCreateUser) {
        if (Ws.isBlank(this.wechatMpOpenIdKey)) {
            throw Er.create("e.auth.wechatMpOpenIdKey.NotDefined");
        }
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

        return add_session_by_openid(q, Wn.SET_AUTH_WXMP, autoUser);
    }

    private WnSession add_session_by_openid(WnQuery q, String type, WnUser autoUser) {
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
        WnSession se = __create_session_by_user(u, type, this.sessionDuration);
        sessions.addSession(se);

        // 搞定
        return se;
    }

    private WnSession __create_session_by_user(WnUser u, String type, int duInSec) {
        WnSession se = new WnSimpleSession(u, duInSec);
        se.setSite(this.site);
        se.setType(type);
        sessions.patchDefaultEnv(se);
        return se;
    }

    @Override
    public WnSession logout(String ticket) {
        WnSession se = sessions.getSession(ticket, users);
        return removeSession(se);
    }

    @Override
    public UserRace getUserRace() {
        return users.getUserRace();
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public List<WnUser> queryUser(WnQuery q) {
        return users.queryUser(q);
    }

    @Override
    public WnUser getUser(String nameOrPhoneOrEmail) {
        return users.getUser(nameOrPhoneOrEmail);
    }

    @Override
    public WnUser checkUser(String nameOrPhoneOrEmail) {
        return users.checkUser(nameOrPhoneOrEmail);
    }

    @Override
    public WnUser getUser(WnUser info) {
        return users.getUser(info);
    }

    @Override
    public WnUser checkUser(WnUser info) {
        return users.checkUser(info);
    }

    @Override
    public WnUser getUserById(String uid) {
        return users.getUserById(uid);
    }

    @Override
    public WnUser checkUserById(String uid) {
        return users.checkUserById(uid);
    }

    @Override
    public void saveUserMeta(WnUser u) {
        users.saveUserMeta(u);
    }

    @Override
    public void updateUserName(WnUser u) {
        users.updateUserName(u);
    }

    @Override
    public void updateUserPhone(WnUser u) {
        users.updateUserPhone(u);
    }

    @Override
    public void updateUserEmail(WnUser u) {
        users.updateUserEmail(u);
    }

    @Override
    public void updateUserLastLoginAt(WnUser u) {
        users.updateUserLastLoginAt(u);
    }

    @Override
    public void updateUserPassword(WnUser u, String rawPassword) {
        users.updateUserPassword(u, rawPassword);
    }

    @Override
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

    @Override
    public WnSession getSession(String ticket) {
        return sessions.getSession(ticket, users);
    }

    @Override
    public WnSession getSessionByUserIdAndType(String uid, String type) {
        return sessions.getSessionByUserIdAndType(uid, type, users);
    }

    @Override
    public WnSession getSessionByUserNameAndType(String unm, String type) {
        return sessions.getSessionByUserNameAndType(unm, type, users);
    }

    @Override
    public List<WnSession> querySession(int limit) {
        return sessions.querySession(limit, users);
    }

    @Override
    public WnSession checkSession(String ticket) {
        WnSession se = sessions.getSession(ticket, users);
        if (null == se) {
            throw Er.create("e.auth.session.NoExists", ticket);
        }
        return se;
    }

    @Override
    public void saveSessionEnv(WnSession se) {
        sessions.saveSessionEnv(se);
    }

    @Override
    public void touchSession(WnSession se) {
        int du = se.getDuration();
        if (du <= 0) {
            du = this.sessionDuration;
        }
        sessions.touchSession(se, du);
    }

}
