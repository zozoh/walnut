package org.nutz.walnut.impl.auth;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.auth.WnCaptchaService;
import org.nutz.walnut.api.auth.WnGroupRole;
import org.nutz.walnut.api.auth.WnAuthSetup;
import org.nutz.walnut.api.auth.WnAuths;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class WnAuthServiceImpl extends WnGroupRoleServiceWrapper implements WnAuthService {

    private WnIo io;

    private WnAuthSetup setup;

    public WnAuthServiceImpl(WnIo io, WnAuthSetup setup) {
        super(io);
        this.io = io;
        this.setup = setup;
    }

    @Override
    public WnAccount getAccount(String nameOrIdOrPhoneOrEmail) {
        WnAccount info = new WnAccount(nameOrIdOrPhoneOrEmail);
        return this.getAccount(info);
    }

    @Override
    public WnAccount getAccount(WnAccount info) {
        // 用 ID 获取
        if (info.hasId()) {
            String uid = info.getId();
            return this.checkAccountById(uid);
        }
        // 将信息转换为查询条件
        // 通常这个信息是手机号/邮箱/登录名等
        NutMap qmap = info.toBean();
        WnObj oAccountDir = setup.getAccountDir();
        WnQuery q = Wn.Q.pid(oAccountDir);
        q.setAll(qmap);

        // 获取账户信息
        WnObj oU = io.getOne(q);
        if (null != oU) {
            return new WnAccount(oU);
        }
        return null;
    }

    @Override
    public WnAccount checkAccount(WnAccount info) {
        WnAccount u = this.getAccount(info);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", info);
        }
        return u;
    }

    @Override
    public WnAccount checkAccount(String name) {
        WnAccount u = this.getAccount(name);
        if (null == u) {
            throw Er.create("e.auth.account.noexists", name);
        }
        return u;
    }

    @Override
    public WnAccount createAccount(WnAccount u) {
        WnObj oAccountDir = setup.getAccountDir();
        // 裸密码自动加盐
        if (u.hasRawPasswd()) {
            u.setSalt(R.UU32());
            u.setRawPasswd(u.getPasswd());
        }

        // 自动设置业务角色名
        if (!u.hasRoleName()) {
            u.setRoleName(setup.getDefaultRoleName());
        }

        // 创建账户对象
        WnObj oU = io.create(oAccountDir, u.getName(), WnRace.FILE);

        // 保存之
        NutMap meta = u.toBean();
        io.appendMeta(oU, meta);

        // 初始化
        WnAccount newUser = new WnAccount(oU);
        setup.afterAccountCreated(newUser);
        return newUser;
    }

    @Override
    public void saveAccount(WnAccount user) {
        this.saveAccount(user, WnAuths.ABMM.ALL);
    }

    @Override
    public void saveAccount(WnAccount user, int mode) {
        // 取得账户对象
        WnObj oU = io.checkById(user.getId());

        // 准备元数据
        NutMap meta = user.toBean(mode);

        // 执行更新
        io.appendMeta(oU, meta);

        // 更新对象
        user.updateBy(oU);
    }

    @Override
    public void renameAccount(WnAccount user, String newName) {
        if (!user.isSameName(newName)) {
            // 为了最小化更新数据集，重新建立一个只有 id/name 的账户对象
            // 这样执行 save 的时候，仅仅会更新 name 字段
            String uid = user.getId();
            WnAccount u2 = new WnAccount();
            u2.setId(uid);
            u2.setName(newName);
            this.saveAccount(u2, WnAuths.ABMM.LOGIN);

            // 执行后续操作
            setup.afterAccountRenamed(u2);
        }
    }

    @Override
    public void deleteAccount(WnAccount user) {
        if (setup.beforeAccountDeleted(user)) {
            WnObj oU = io.checkById(user.getId());
            io.delete(oU);
        }
    }

    @Override
    public WnGroupRole getGroupRole(WnAccount user, String groupName) {
        WnContext wc = Wn.WC();
        // 进入内核态
        return wc.core(new WnEvalLink(io), true, null, new Proton<WnGroupRole>() {
            protected WnGroupRole exec() {
                // 本操作由 ROOT 账户完成
                WnAccount root = getAccount("root");
                // 切换账户并执行
                return wc.su(root, new Proton<WnGroupRole>() {
                    protected WnGroupRole exec() {
                        return __get_sys_role_without_security(user, groupName);
                    }
                });
            }
        });
    }

    private WnGroupRole __get_sys_role_without_security(WnAccount user, String groupName) {
        // 准备主组权限主目录
        String aph = "/sys/role/";
        final WnObj oSysRoleDir = io.createIfNoExists(null, aph, WnRace.DIR);
        // 默认组权限： 0 - GUEST
        int role = 0;
        // 尝试获取
        WnQuery q = Wn.Q.pid(oSysRoleDir);
        q.setv("uid", user.getId());
        q.setv("grp", groupName);
        WnObj oR = io.getOne(q);

        // 为了兼容老代码，如果试图用名字获取
        if (null == oR) {
            aph = Wn.appendPath("/sys/grp", groupName, user.getId());
            oR = io.fetch(null, aph);
            // 如果存在，则复制到新规则里
            if (null != oR) {
                role = oR.getInt("role", 0);
                WnObj newR = io.create(oSysRoleDir, "${id}", WnRace.FILE);
                NutMap meta = new NutMap();
                meta.put("uid", user.getId());
                meta.put("grp", groupName);
                meta.put("role", role);
                io.appendMeta(newR, meta);
            }
        }
        // 获取角色值
        else {
            role = oR.getInt("role", 0);
        }

        // 默认是GUEST
        return WnGroupRole.parseInt(role);
    }

    @Override
    public WnAuthSession getSession(String ticket) {
        WnObj oSessionDir = setup.getSessionDir();
        WnObj oSe = io.fetch(oSessionDir, ticket);

        if (null != oSe) {
            // 取得用户
            String uid = oSe.getString("uid");
            WnAccount me = this.checkAccountById(uid);
            // 返回对象
            return new WnAuthSession(oSe, me);
        }

        return null;
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
    public WnAuthSession touchSession(String ticket) {
        // 准备查询条件
        WnObj oSessionDir = setup.getSessionDir();
        WnQuery q = Wn.Q.pid(oSessionDir);
        q.setv("nm", ticket);

        // 更新会话时间并返回数据
        long nowInMs = System.currentTimeMillis();
        long se_du = setup.getSessionDefaultDuration();
        NutMap meta = Lang.map("expi", nowInMs + (se_du * 1000L));
        WnObj oSe = io.setBy(q, meta, true);

        if (null != oSe) {
            // 获取用户
            String uid = oSe.getString("uid");
            WnAccount me = this.checkAccountById(uid);

            // 返回会话
            return new WnAuthSession(oSe, me);
        }
        return null;
    }

    @Override
    public WnAuthSession createSession(WnAccount user) {
        NutMap by = Lang.map("by_tp", "transient");
        by.put("by_val", null);
        long se_du = setup.getSessionTransientDuration();
        return createSessionBy(se_du, user, by);
    }

    @Override
    public WnAuthSession createSession(WnAuthSession pse, WnAccount user) {
        NutMap by = Lang.map("by_tp", "session");
        by.put("by_val", pse.getId());
        long se_du = setup.getSessionDefaultDuration();
        return createSessionBy(se_du, user, by);
    }

    @Override
    public void saveSession(WnAuthSession se) {
        this.saveSessionInfo(se);
        this.saveSessionVars(se);
    }

    @Override
    public void saveSessionInfo(WnAuthSession se) {
        NutMap meta = se.toMeta();
        String seId = se.getId();
        io.setBy(seId, meta, false);
    }

    @Override
    public void saveSessionVars(WnAuthSession se) {
        NutMap vars = se.getVars();
        String json = Json.toJson(vars, JsonFormat.compact());
        String seId = se.getId();
        WnObj oSe = io.checkById(seId);
        io.writeText(oSe, json);
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
        WnIoWeixinApi wxApi = setup.getWeixinApi();
        WnObj oSessionDir = setup.getSessionDir();
        WnObj oAccountDir = setup.getAccountDir();

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
        WnQuery q = Wn.Q.pid(oSessionDir);
        q.setAll(by);
        WnObj oSe = io.getOne(q);
        if (null != oSe) {
            String uid = oSe.getString("uid");
            WnAccount me = checkAccountById(uid);
            if (null != me) {
                return new WnAuthSession(oSe, me);
            }
        }

        // 看看这个用户是否存在
        NutMap qmap = Lang.map(key, openid);

        q = Wn.Q.pid(oAccountDir);
        q.setAll(qmap);
        WnObj oU = io.getOne(q);
        WnAccount me = new WnAccount(oU);

        // 如果已经存在了就直接创建 会话收工
        if (null != oU) {
            long se_du = setup.getSessionDefaultDuration();
            return this.createSessionBy(se_du, me, by);
        }

        throw Lang.noImplement();
    }

    @Override
    public WnAuthSession bindAccount(String nameOrIdOrPhoneOrEmail,
                                     String scene,
                                     String vcode,
                                     String ticket) {
        WnCaptchaService captcha = setup.getCaptchaService();
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
            this.saveAccount(me, WnAuths.ABMM.LOGIN);
        }

        // 创建会话并返回
        return se;
    }

    @Override
    public WnAuthSession loginByVcode(String phoneOrEmail, String scene, String vcode) {
        WnCaptchaService captcha = setup.getCaptchaService();
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
            String role = setup.getDefaultRoleName();
            info.setRoleName(role);
            me = this.createAccount(info);
        }

        // 创建会话并返回
        NutMap by = Lang.map("by_tp", "web_vcode");
        by.put("by_val", account);
        long se_du = setup.getSessionDefaultDuration();
        WnAuthSession se = createSessionBy(se_du, me, by);
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
        long se_du = setup.getSessionDefaultDuration();
        WnAuthSession se = createSessionBy(se_du, me, by);
        return se;
    }

    private WnAuthSession createSessionBy(long duInSec, WnAccount me, NutMap meta) {
        WnObj oSessionDir = setup.getSessionDir();
        // 过期时间
        long expi = System.currentTimeMillis() + (duInSec * 1000L);

        // 验证通过后，创建会话
        String ticket = R.UU32();
        WnObj oSe = io.create(oSessionDir, ticket, WnRace.FILE);
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

    private WnAccount checkAccountById(String uid) {
        WnObj oAccountDir = setup.getAccountDir();
        WnObj oU = io.get(uid);
        if (null != oU) {
            if (!oAccountDir.isSameId(oU.parentId())) {
                throw Er.create("e.auth.acc_outof_home", uid);
            }
        }
        return new WnAccount(oU);
    }

}
