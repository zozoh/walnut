package com.site0.walnut.login;

import java.util.Date;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.net.xapi.bean.XApiRequest;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.login.session.WnSimpleSession;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.val.id.WnSnowQMaker;

public class WnLoginApi {

    private static final WnSnowQMaker TicketMaker = new WnSnowQMaker(null, 10);

    private WnUserStore users;

    private WnSessionStore sessions;

    private WnXApi xapi;

    private String domain;

    private long sessionDuration;

    private String wechatMpOpenIdKey;

    // TODO 这里是微信公众号页面的获取 openid 方式，暂时还未实现
    // private String wechatGhOpenIdKey;

    public WnLoginApi(WnLoginSetup options) {
        this.users = options.users;
        this.sessions = options.sessions;
        this.sessionDuration = options.sessionDuration;
        this.xapi = options.xapi;
        this.domain = options.domain;
        this.wechatMpOpenIdKey = options.wechatMpOpenIdKey;
    }

    public void changePassword(WnUser u, String rawPassword) {
        users.updateUserPassword(u, rawPassword);
    }

    public WnSession loginByPassword(String nameOrPhoneOrEmail, String rawPassword) {
        WnUser u = users.getUser(nameOrPhoneOrEmail);
        if (null == u) {
            throw Er.create("e.auth.login.Failed");
        }
        String saltedPassword = Wn.genSaltPassword(rawPassword, u.getSalt());
        if (saltedPassword.equals(u.getPasswd())) {
            WnSession se = createSession(u);
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
        xapi.prepare("wxmp", code, code, null, autoCreateUser);

        XApiRequest req = xapi.prepare(apiName, account, path, vars, false);
        req.setDisableCache(true);

        NutMap result = xapi.send(req, NutMap.class);
        String openid = result.getString("openid");

        // 看看是否成功的获取了用户的 openid
        // 没有 open id，那么必然是错误
        if (Ws.isBlank(openid)) {
            throw Er.create("e.auth.session.loginByWechatMPCode.Faild", Json.toJson(result));
        }

        // 根据用户的 openid 找回账号
        WnQuery q = new WnQuery();
        q.setv(this.wechatMpOpenIdKey, openid);
        return __create_session_by_openid(q, autoCreateUser);
    }

    private WnSession __create_session_by_openid(WnQuery q, boolean autoCreateUser) {
        WnUser u = users.getUser(q);

        // 是否自动创建账号
        if (autoCreateUser) {

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

    private WnSession createSession(WnUser u) {
        WnSimpleSession se = new WnSimpleSession();
        se.setExpiAt(System.currentTimeMillis() + sessionDuration);
        se.setUser(u);
        se.setTicket(TicketMaker.make(new Date(), null));
        return se;
    }

    public UserRace getUserRace() {
        return users.getUserRace();
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

    public WnSession getSession(String ticket) {
        return sessions.getSession(ticket, users);
    }

    public void saveSessionEnv(WnSession se) {
        sessions.saveSessionEnv(se);
    }

    public void touchSession(WnSession se, long sessionDuration) {
        sessions.touchSession(se, sessionDuration);
    }

}
