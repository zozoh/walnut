package org.nutz.walnut.impl.auth;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.auth.WnCaptchaService;
import org.nutz.walnut.api.auth.WnRoleService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.util.Wn;

public class WnAuthServiceImpl implements WnAuthService {

    private WnIo io;

    private WnCaptchaService captcha;

    private WnRoleService roles;

    private WnIoWeixinApi wxApi;

    private WnObj oAccountHome;

    private WnObj oSessionHome;

    private long sessionDuration;

    public WnAuthServiceImpl(WnIo io,
                             WnCaptchaService captcha,
                             WnRoleService roles,
                             WnIoWeixinApi wxApi,
                             WnObj oAccountHome,
                             WnObj oSessionHome,
                             long se_du) {
        this.io = io;
        this.captcha = captcha;
        this.roles = roles;
        this.wxApi = wxApi;
        this.oAccountHome = oAccountHome;
        this.oSessionHome = oSessionHome;
        this.sessionDuration = se_du;
    }

    @Override
    public WnAccount getAccount(String name) {
        if (Strings.isBlank(name))
            return null;
        // ID
        if (Wn.isFullObjId(name)) {
            WnObj oU = this.__check_user_byId(name);
            return new WnAccount(oU);
        }
        // 查询
        NutMap meta = new NutMap();
        // 手机
        if (Strings.isMobile(name)) {
            meta.put("phone", name);
        }
        // 邮箱
        else if (Strings.isEmail(name)) {
            meta.put("email", name);
        }
        // 登录名
        else {
            meta.put("nm", name);
        }

        WnObj oU = this.__get_user(meta);
        if (null == oU) {
            return null;
        }
        return new WnAccount(oU);
    }

    @Override
    public WnAccount checkAccount(String name) {
        WnAccount acc = this.getAccount(name);
        if (null == acc) {
            throw Er.create("e.auth.account.noexists", name);
        }
        return acc;
    }

    @Override
    public WnAuthSession getSession(String ticket) {
        WnObj oSe = io.fetch(oSessionHome, ticket);
        if (null == oSe) {
            return null;
        }
        // 取得用户
        String uid = oSe.getString("uid");
        WnObj oU = this.__check_user_byId(uid);
        // 返回对象
        return new WnAuthSession(oSe, oU);
    }

    @Override
    public WnAuthSession checkSession(String ticket) {
        WnAuthSession se = this.getSession(ticket);
        if (null == se) {
            throw Er.create("e.auth.ticked.noexist", ticket);
        }
        return se;
    }

    @Override
    public WnAuthSession removeSession(String ticket) {
        WnObj oSe = io.fetch(oSessionHome, ticket);
        if (null == oSe) {
            throw Er.create("e.auth.ticked.noexist", ticket);
        }
        // 返回对象
        return new WnAuthSession(oSe, null);
    }

    @Override
    public WnAuthSession loginByWxCode(String code) {
        // 得到用户的 OpenId
        String openid = wxApi.user_openid_by_code(code);
        if (Strings.isBlank(openid)) {
            throw Er.create("e.auth.login.invalid.weixin_code");
        }
        // 得到公众号名称
        String ghName = wxApi.getHomeObj().name();
        String key = "wx_gh_" + ghName;

        // 如果已经有了这个用户的微信会话，重用之
        NutMap by = Lang.map("by_tp", key);
        by.put("by_val", openid);
        WnQuery q = Wn.Q.pid(oSessionHome);
        q.setAll(by);
        WnObj oSe = io.getOne(q);
        if (null != oSe) {
            String uid = oSe.getString("uid");
            WnObj oU = __get_account_by_id(uid);
            if (null != oU) {
                return new WnAuthSession(oSe, oU);
            }
        }

        // 看看这个用户是否存在，如果已经存在了就直接创建 会话收工
        NutMap meta = Lang.map(key, openid);
        WnObj oU = __get_user(meta);
        if (null != oU) {
            return this.__create_session(oU, by);
        }

        throw Lang.noImplement();
    }

    @Override
    public WnAuthSession bindAccount(String account, String scene, String vcode, String ticket) {
        return null;
    }

    @Override
    public WnAuthSession loginByVcode(String account, String scene, String vcode) {
        return null;
    }

    @Override
    public WnAuthSession loginByPasswd(String account, String passwd) {
        return null;
    }

    @Override
    public WnAuthSession logout(String ticket) {
        WnAuthSession se = this.getSession(ticket);
        if (null != se) {
            WnObj oSe = io.get(se.getId());
            if (null != oSe) {
                io.delete(oSe);
            }
        }
        return se;
    }

    private WnAuthSession __create_session(WnObj oU, NutMap info) {
        // 过期时间
        long expi = System.currentTimeMillis() + (this.sessionDuration * 1000L);

        // 验证通过后，创建会话
        String ticket = R.UU64();
        WnObj oSe = io.create(oSessionHome, ticket, WnRace.FILE);
        WnAuthSession se = new WnAuthSession(ticket);
        se.setId(oSe.id());
        se.setMe(oU);
        se.setExpi(expi);

        // 更新会话
        NutMap meta = se.toMeta();
        if (null != info) {
            meta.putAll(info);
        }
        io.appendMeta(oSe, meta);

        // 更新用户最后登录时间
        oU.put("login", oSe.lastModified());
        io.set(oU, "^(login)$");

        // 搞定
        return se;
    }

    private WnObj __get_account_by_id(String uid) {
        WnObj oU = io.get(uid);
        if (null != oU) {
            if (!this.oAccountHome.isSameId(oU.parentId())) {
                throw Er.create("e.auth.acc_outof_home", uid);
            }
        }
        return oU;
    }

    private WnObj __check_user_byId(String uid) {
        WnObj oU = io.get(uid);
        if (null == oU) {
            throw Er.create("e.auth.account.noexist", uid);
        }
        if (!this.oAccountHome.isSameId(oU.parentId())) {
            throw Er.create("e.auth.acc_outof_home", uid);
        }
        return oU;
    }

    private WnObj __get_user(NutMap meta) {
        WnQuery q = Wn.Q.pid(oAccountHome);
        q.setAll(meta);
        return io.getOne(q);
    }

    private WnObj __check_user(NutMap meta) {
        WnObj oU = __get_user(meta);

        // 没找到
        if (null == oU) {
            throw Er.create("e.auth.login.noexists");
        }
        return oU;
    }

}
