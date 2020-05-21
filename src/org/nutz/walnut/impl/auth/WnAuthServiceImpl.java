package org.nutz.walnut.impl.auth;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAccountLoader;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.auth.WnAuthSetup;
import org.nutz.walnut.api.auth.WnAuths;
import org.nutz.walnut.api.auth.WnCaptchaService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.www.bean.WnWebSite;
import org.nutz.walnut.util.Wn;

public class WnAuthServiceImpl extends WnGroupRoleServiceImpl implements WnAuthService {

    private static final Log log = Logs.get();

    WnIo io;

    private WnAuthSetup setup;

    private WnAccountLoader accountLoader;

    public WnAuthServiceImpl(WnIo io, WnAuthSetup setup) {
        super(io);
        this.io = io;
        this.setup = setup;
        this.accountLoader = new WnAccountLoaderImpl(io, setup.getAccountDir(), true);
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
        String unm = u.getName("${id}");
        WnObj oU = io.create(oAccountDir, unm, WnRace.FILE);

        // 保存之
        u.updateBy(oU);

        // 如果有邮箱但是没昵称，用邮箱名代替一下
        if (Strings.isBlank(u.getNickname()) && u.hasEmail()) {
            String email = u.getEmail();
            int pos = email.indexOf('@');
            if (pos > 0) {
                u.setNickname(email.substring(0, pos));
            } else {
                u.setNickname(email);
            }
        }

        NutMap meta = u.toBean();
        io.appendMeta(oU, meta);

        // 初始化
        WnAccount newUser = new WnAccount(oU);
        setup.afterAccountCreated(this, newUser);
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
    public WnAccount saveAccount(WnAccount user, NutMap meta) {
        // 取得账户对象
        WnObj oU = io.checkById(user.getId());

        // 执行更新
        io.appendMeta(oU, meta);

        // 更新对象
        user.updateBy(oU);

        // 返回新对象
        return new WnAccount(oU);
    }

    @Override
    public void renameAccount(WnAccount user, String newName) {
        if (!user.isSameName(newName)) {
            // 看看是否有同名账户存在
            WnAccount ta = this.getAccount(newName);
            if (null != ta) {
                throw Er.create("e.auth.rename.exists", newName);
            }
            // 预处理确保可以重命名
            if (!setup.beforeAccountRenamed(this, user, newName)) {
                return;
            }

            // 为了最小化更新数据集，重新建立一个只有 id/name 的账户对象
            // 这样执行 save 的时候，仅仅会更新 name 字段
            String uid = user.getId();
            WnAccount u2 = new WnAccount();
            u2.setId(uid);
            u2.setName(newName);
            this.saveAccount(u2, WnAuths.ABMM.LOGIN);
        }
    }

    @Override
    public void deleteAccount(WnAccount user) {
        if (setup.beforeAccountDeleted(this, user)) {
            WnObj oU = io.checkById(user.getId());
            io.delete(oU);
        }
    }

    @Override
    public WnAuthSession getSession(String ticket) {
        WnObj oSessionDir = setup.getSessionDir();
        WnObj oSe = io.fetch(oSessionDir, ticket);

        if (null != oSe) {
            // 取得用户
            WnAccount me = __load_session_account(oSe);
            if (null != me) {
                // 会话文件的内容就是 JSON，记录这个会话的全部环境变量
                NutMap vars = io.readJson(oSe, NutMap.class);
                // 组装会话对象
                WnAuthSession se = new WnAuthSession(oSe, me);
                if (null != vars) {
                    se.getVars().putAll(vars);
                }
                // 并返回
                return se;
            }
        }

        return null;
    }

    private WnAccount __load_session_account(WnObj oSe) {
        String uid = oSe.getString("uid");
        // 域用户登录的会话，创立一个账户读取器
        if (oSe.is("by_tp", "auth_by_domain")) {
            String[] ss = Strings.splitIgnoreBlank(oSe.getString("by_val"), ":");
            String siteId = ss[0];
            WnObj oWWW = io.checkById(siteId);
            String siteHomePath = Wn.getObjHomePath(oWWW);
            WnWebSite site = new WnWebSite(io, siteHomePath, siteId, oWWW);
            WnAccountLoader accLoader = new WnAccountLoaderImpl(io, site.getAccountDir(), false);
            return accLoader.getAccountById(uid);
        }
        // 自身的账户体系直接获取
        return this.getAccountById(uid);
    }

    @Override
    public WnAuthSession getSession(String byType, String byValue) {
        WnObj oSessionDir = setup.getSessionDir();
        NutMap by = Lang.map("by_tp", byType);
        by.put("by_val", byValue);
        WnQuery q = Wn.Q.pid(oSessionDir);
        q.setAll(by);
        WnObj oSe = io.getOne(q);
        if (null != oSe) {
            WnAccount me = __load_session_account(oSe);
            if (null != me) {
                return new WnAuthSession(oSe, me);
            }
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
    public WnAuthSession checkSession(String byType, String byValue) {
        WnAuthSession se = this.getSession(byType, byValue);
        if (null == se) {
            throw Er.create("e.auth.session.noexist", byType + "=" + byValue);
        }
        return se;
    }

    @Override
    public WnAuthSession touchSession(WnAuthSession se) {
        if (null != se && !se.isDead()) {
            long nowInMs = System.currentTimeMillis();
            long se_du = setup.getSessionDefaultDuration();
            se.setExpi(nowInMs + (se_du * 1000L));
            io.appendMeta(se.getObj(), Lang.map("expi", se.getExpi()));
        }

        return se;
    }

    @Override
    public WnAuthSession createSession(WnAccount user, boolean longSession) {
        NutMap by = Lang.map("by_tp", "transient");
        by.put("by_val", null);
        long se_du = longSession ? setup.getSessionDefaultDuration()
                                 : setup.getSessionTransientDuration();
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
        io.appendMeta(se.getObj(), meta);
    }

    @Override
    public void saveSessionVars(WnAuthSession se) {
        NutMap vars = se.getVars();
        String json = Json.toJson(vars, JsonFormat.compact());
        WnObj oSe = se.getObj();
        io.writeText(oSe, json);
    }

    @Override
    public WnAuthSession removeSession(WnAuthSession se, long delay) {
        // 删除
        if (null != se) {
            WnObj oSe = io.get(se.getId());
            // 重新获取会话对象
            se = new WnAuthSession(oSe, se.getMe());
            // 删除数据
            if (null != oSe) {
                // 立即删除
                if (0 == delay) {
                    io.delete(oSe);
                }
                // 设置过期时间
                else {
                    long expi = System.currentTimeMillis() + delay;
                    oSe.expireTime(expi);
                    oSe.put("dead", true);
                    io.set(oSe, "^(expi|dead)$");
                }
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
    public WnAuthSession logout(String ticket, long delay) {
        // 重新取得
        WnAuthSession se = this.checkSession(ticket);

        // 删除
        return this.removeSession(se, delay);
    }

    @Override
    public WnAuthSession loginByWxCode(String code, String wxCodeType, boolean forbidUnsubscribe) {
        WnIoWeixinApi wxApi = setup.getWeixinApi();
        WnObj oSessionDir = setup.getSessionDir();

        // 得到用户的 OpenId
        String openid;
        String session_key = null;
        wxCodeType = Strings.sBlank(wxCodeType, "gh");
        if (!wxCodeType.matches("^(mp|gh)$")) {
            throw Er.create("e.auth.login.invalid.wxCodeType");
        }
        // 小程序的权限码
        if ("mp".equals(wxCodeType)) {
            NutMap resp = wxApi.user_info_by_mp_code(code);
            openid = resp.getString("openid");
            session_key = resp.getString("session_key");
            log.info("user_info_by_mp_code " + Json.toJson(resp, JsonFormat.compact()));
        }
        // 微信公号的登录码
        else {
            openid = wxApi.user_openid_by_gh_code(code);
        }
        if (Strings.isBlank(openid)) {
            throw Er.create("e.auth.login.invalid.wxCode");
        }
        // 得到公众号名称
        String ghOrMpName = wxApi.getHomeObj().name();
        String key = "wx_" + wxCodeType + "_" + ghOrMpName;

        // 如果已经有了这个用户的微信会话，重用之
        NutMap by = Lang.map("by_tp", key);
        by.put("by_val", openid);
        WnQuery q = Wn.Q.pid(oSessionDir);
        q.setAll(by);
        WnObj oSe = io.getOne(q);
        if (null != oSe) {
            String uid = oSe.getString("uid");
            WnAccount me = getAccountById(uid);
            if (null != me) {
                // 小程序的 session key 要保存一下，以便以后随时获取用户手机号等相关信息
                if (session_key != null) {
                    // 已经有存了（虽然不太可能）那就不用再存了
                    if (!Strings.isBlank(oSe.getString("mp_session_key"))
                        && session_key.equals(oSe.getString("mp_session_key"))) {
                        // nop
                    }
                    // 保存并持久化
                    else {
                        oSe.put("mp_session_key", session_key);
                        io.appendMeta(oSe, new NutMap("mp_session_key", session_key));
                    }
                }
                return new WnAuthSession(oSe, me);
            }
        }
        // 既然是微信小程序登录的，那么就固定记录一下 SessionKey，以便以后随时获取用户手机号等相关信息
        if (session_key != null) {
            by.put("mp_session_key", session_key);
        }

        // 看看这个用户是否存在
        WnAccount info = new WnAccount();
        info.setWxOpenId(wxCodeType, ghOrMpName, openid);
        WnAccount me = accountLoader.getAccount(info);

        // 如果已经存在了就直接创建 会话收工
        if (null != me) {
            long se_du = setup.getSessionDefaultDuration();
            return this.createSessionBy(se_du, me, by);
        }

        // 选择一个默认角色
        String role = setup.getDefaultRoleName();

        // 看来要创建一个用户，嗯嗯，如果是公号的话，先获取信息
        String headimgurl = null;
        NutMap meta = new NutMap();
        if ("gh".equals(wxCodeType)) {
            NutMap re = wxApi.user_info(openid, null);
            /**
             * 得到的返回信息格式为：
             * 
             * <pre>
             {
                subscribe: 1,
                openid: "xxx",
                nickname: "小白",
                sex: 1,
                language: "zh_CN",
                city: "海淀",
                province: "北京",
                country: "中国",
                headimgurl: "http://..",
                subscribe_time: 1474388443,
                remark: "",
                groupid: 0,
                tagid_list: [],
                subscribe_scene: "ADD_SCENE_OTHERS",
                qr_scene: 0,
                qr_scene_str: ""
             }
             * </pre>
             */
            if (forbidUnsubscribe && !re.is("subscribe", 1)) {
                throw Er.create("e.auth.login.WxGhNoSubscribed");
            }
            String nickname = re.getString("nickname", "anonymous");
            meta = re.pickBy("^(language|city|province|country|subscribe)$");
            info.setNickname(nickname);
            info.setSex(re.getInt("sex", 0));
            info.putAllMeta(meta);

            // 记录一下头像
            headimgurl = re.getString("headimgurl");
        }
        // 小程序的话，获得不了，那么就用默认的吧
        else {
            info.setNickname(openid);
        }
        info.setRoleName(role);

        // 创建账户
        me = this.createAccount(info);

        // 如果有头像的话，搞一下
        if (!Strings.isBlank(headimgurl)) {
            WnObj oThumb = this.setup.getAvatarObj(me, true);
            // 读取 Image
            try {
                URL thumb_url = new URL(headimgurl);
                BufferedImage im = Images.read(thumb_url);
                io.writeImage(oThumb, im);

                // 保存头像
                me.setThumb("id:" + oThumb.id());
                NutMap map = me.toBeanOf("thumb");
                this.saveAccount(me, map);
            }
            catch (MalformedURLException e) {
                throw Er.wrap(e);
            }
        }

        // 创建完毕，收工
        long se_du = setup.getSessionDefaultDuration();
        return createSessionBy(se_du, me, by);
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
        WnAccount me = this.getAccount(phoneOrEmail);

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
        se.setObj(oSe);

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

        // 保存会话的环境变量
        this.saveSessionVars(se);

        // 搞定
        return se;
    }

    @Override
    public List<WnAccount> queryAccount(WnQuery q) {
        return accountLoader.queryAccount(q);
    }

    @Override
    public WnAccount getAccount(String nameOrIdOrPhoneOrEmail) {
        return accountLoader.getAccount(nameOrIdOrPhoneOrEmail);
    }

    @Override
    public WnAccount checkAccount(String nameOrIdOrPhoneOrEmail) {
        return accountLoader.checkAccount(nameOrIdOrPhoneOrEmail);
    }

    @Override
    public WnAccount getAccount(WnAccount info) {
        return accountLoader.getAccount(info);
    }

    @Override
    public WnAccount checkAccount(WnAccount info) {
        return accountLoader.checkAccount(info);
    }

    @Override
    public WnAccount checkAccountById(String uid) {
        return accountLoader.checkAccountById(uid);
    }

    @Override
    public WnAccount getAccountById(String uid) {
        return accountLoader.getAccountById(uid);
    }

}
