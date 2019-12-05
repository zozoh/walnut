package org.nutz.walnut.impl.auth;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
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
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.www.bean.WnWebSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnLoginObj;

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
    public WnAccount getAccount(String nameOrIdOrPhoneOrEmail) {
        // 分析信息
        WnAccount info = new WnAccount(nameOrIdOrPhoneOrEmail);

        // 查询
        NutMap qmap = new NutMap();
        info.mergeToBean(qmap);

        WnQuery q = Wn.Q.pid(oAccountHome);
        q.setAll(qmap);

        WnObj oU = io.getOne(q);
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
        WnAccount me = this.getAccountById(uid);
        // 返回对象
        return new WnAuthSession(oSe, me);
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
    public WnAuthSession removeSession(WnAuthSession se) {
        // 删除
        if (null != se) {
            WnObj oSe = io.get(se.getId());
            // 重新获取会话对象
            se = new WnAuthSession(oSe, se.getMe());
            // 删除数据
            if (null != oSe) {
                io.delete(oSe);
            }
            // 子会话的话，获取其父会话
            if (se.hasParentSession()) {
                WnObj oPse = io.get(se.getParentSessionId());
                if (null != oPse) {
                    String uid = oPse.getString("uid");
                    WnObj oU = io.get(uid);
                    WnAccount me = new WnAccount(oU);
                    WnAuthSession pse = new WnAuthSession(oPse, me);
                    return pse;
                }
            }
        }

        // 搞定（顶级会话）
        return null;
    }

    @Override
    public WnAuthSession logout(String ticket) {
        // 重新取得
        WnAuthSession se = this.checkSession(ticket);

        // 删除
        return this.removeSession(se);
    }
    
    

    @Override
    public WnAuthSession loginByWxCode(String code) {
        // 得到用户的 OpenId
        String openid = wxApi.user_openid_by_gh_code(code);
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
            WnAccount me = getAccountById(uid);
            if (null != me) {
                return new WnAuthSession(oSe, me);
            }
        }

        // 看看这个用户是否存在
        NutMap qmap = Lang.map(key, openid);

        q = Wn.Q.pid(oAccountHome);
        q.setAll(qmap);
        WnObj oU = io.getOne(q);
        WnAccount me = new WnAccount(oU);

        // 如果已经存在了就直接创建 会话收工
        if (null != oU) {
            return this.createSessionBy(me, by);
        }

        throw Lang.noImplement();
    }

    @Override
    public WnAuthSession bindAccount(String nameOrIdOrPhoneOrEmail,
                                     String scene,
                                     String vcode,
                                     String ticket) {
        // 首先验证一下验证码是否正确
        if (!captcha.removeCaptcha(scene, nameOrIdOrPhoneOrEmail, vcode)) {
            throw Er.create("e.auth.captcha.invalid", vcode);
        }

        // 其次查询出对应的用户对象
        WnAccount ta = this.getAccount(nameOrIdOrPhoneOrEmail);

        // 根据票据获取当前登录的会话
        WnAuthSession se = this.checkSession(ticket);
        WnAccount me = se.getMe();

        // 如果手机号已经有了一个账号对象
        // - 这种场景是用户预先用手机注册一个账号
        // - 然后在微信打开，微信会自动注册/登录，而这个账号是没有手机号的（比较新）
        // - 但是有可能预先注册的账户已经有了设置
        // 这种时候应该:
        // - 将当前账户(me)信息Copy到目标账户(ta)，并删掉me
        // - 把会话指向目标账户(ta)
        if (null != ta) {
            // 如果有头像的话，看看是否有必要搞一下
            if (me.hasThumb() && !ta.hasThumb()) {
                String thumb = me.getThumb();
                WnObj oSrcThumb = Wn.getObj(io, thumb);
                if (null != oSrcThumb) {
                    ta.setThumb(thumb);
                }
            }

            // Copy 过去
            me.mergeTo(ta);

            // 同时也更新用户最后登录时间咯
            ta.setLoginAt(System.currentTimeMillis());
            this.saveAccount(ta);

            // 删除当前账号
            this.deleteAccount(me);

            // 修改当前会话的 uid/unm
            se.setMe(ta);

            // 更新会话
            this.saveSession(se);

        }
        // 否则直接修改当前账号的 phone 等字段
        else {
            me.setLoginStr(nameOrIdOrPhoneOrEmail);
            this.saveAccountInfo(me);
        }

        // 创建会话并返回
        return se;
    }

    @Override
    public WnAuthSession loginByVcode(String phoneOrEmail, String scene, String vcode) {
        WnAccount info = new WnAccount(phoneOrEmail);

        // 只允许手机或者邮箱
        String account = null;
        if (info.hasPhone()) {
            account = info.getPhone();
        }
        // 邮箱
        else if (info.hasEmail()) {
            account = info.getEmail();
        }
        // 错误的名称
        else {
            throw Er.create("e.auth.login.NoPhoneOrEmail", phoneOrEmail);
        }

        // 首先验证一下验证码是否正确
        if (!captcha.removeCaptcha(scene, account, vcode)) {
            throw Er.create("e.auth.captcha.invalid", vcode);
        }

        // 首先查询出对应的用户对象
        WnAccount me = this.checkAccount(phoneOrEmail);

        // 如果手机号未注册，创建一个新账号
        if (null == me) {
            // 选择一个默认角色
            String role = roles.getDefaultRoleName();
            info.setRoleName(role);
            me = this.createAccount(info);
        }

        // 创建会话并返回
        NutMap by = Lang.map("by_tp", "web_vcode");
        by.put("by_val", account);
        WnAuthSession se = createSessionBy(me, by);
        return se;
    }

    @Override
    public WnAuthSession loginByPasswd(String nameOrIdOrPhoneOrEmail, String passwd) {
        WnAccount me = this.checkAccount(nameOrIdOrPhoneOrEmail);

        // 没有密码或盐
        if (!me.hasSaltedPasswd()) {
            throw Er.create("e.auth.login.NoSaltedPasswd");
        }

        // 加盐验证
        if (!me.isMatchedRawPasswd(passwd)) {
            throw Er.create("e.auth.login.invalid.passwd");
        }

        // 创建会话并返回
        NutMap by = Lang.map("by_tp", "web_passwd");
        by.put("by_val", nameOrIdOrPhoneOrEmail);
        WnAuthSession se = createSessionBy(me, by);
        return se;
    }

    private WnAuthSession createSessionBy(WnAccount me, NutMap meta) {
        // 过期时间
        long expi = System.currentTimeMillis() + (this.sessionDuration * 1000L);

        // 验证通过后，创建会话
        String ticket = R.UU32();
        WnObj oSe = io.create(oSessionHome, ticket, WnRace.FILE);
        WnAuthSession se = new WnAuthSession(ticket, me);
        se.setId(oSe.id());
        se.setExpi(expi);

        // 更新会话
        NutMap seMeta = se.toMeta();
        if (null != meta) {
            seMeta.putAll(meta);
        }
        io.appendMeta(oSe, seMeta);

        // 更新用户最后登录时间
        WnObj oU = io.setBy(me.getId(), "login", oSe.lastModified(), true);
        me = new WnAccount(oU);
        se.setMe(me);

        // 搞定
        return se;
    }

    private WnAccount getAccountById(String uid) {
        WnObj oU = io.get(uid);
        if (null != oU) {
            if (!this.oAccountHome.isSameId(oU.parentId())) {
                throw Er.create("e.auth.acc_outof_home", uid);
            }
        }
        return new WnAccount(oU);
    }

}
