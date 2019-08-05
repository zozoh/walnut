package org.nutz.walnut.ext.www.impl;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import org.nutz.img.Images;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.www.bean.WnWebSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnLoginObj;
import org.nutz.walnut.util.WnPager;

public class WnWebAuthService {

    private WnIo io;

    private WnCaptchaService captcha;

    private WnIoWeixinApi wxApi;

    private WnObj oAccountHome;

    private WnObj oRoleHome;

    private WnObj oSessionHome;

    private long sessionDuration;

    public WnWebAuthService(WnIo io,
                            WnCaptchaService captcha,
                            WnIoWeixinApi wxApi,
                            WnObj oAccountHome,
                            WnObj oRoleHome,
                            WnObj oSessionHome,
                            long se_du) {
        this.io = io;
        this.captcha = captcha;
        this.wxApi = wxApi;
        this.oAccountHome = oAccountHome;
        this.oRoleHome = oRoleHome;
        this.oSessionHome = oSessionHome;
        this.sessionDuration = se_du;
    }

    /**
     * @return 角色库中的默认角色
     */
    public String getDefaultRoleName() {
        // 首先查询出对应的用户对象
        WnThingService things = new WnThingService(io, oRoleHome);
        ThQuery q = new ThQuery();
        q.qStr = "isdft:true";
        q.wp = new WnPager(1, 0);
        WnObj oRole = things.getOne(q);

        if (null != oRole)
            return oRole.name();

        return null;
    }

    public WnWebSession getSession(String ticket) {
        WnObj oSe = io.fetch(oSessionHome, ticket);
        if (null == oSe) {
            return null;
        }
        // 取得用户
        String uid = oSe.getString("uid");
        WnObj oU = io.get(uid);
        if (null == oU) {
            throw Er.create("e.www.account.noexist", oSe);
        }
        if (!this.oAccountHome.isSameId(oU.getString("th_set"))) {
            throw Er.create("e.www.account.invalid", oU);
        }
        // 返回对象
        return new WnWebSession(oSe, oU);
    }

    /**
     * 根据会话票据，找回自身。执行次操作将会自动更新票据
     * 
     * @param ticket
     *            票据
     * @return 更新后的会话对象
     * @throws "e.www.ticket.noexist"
     *             : 票据找不到对应会话
     * @throws "e.www.account.noexist"
     *             : 会话对应用户不存在
     * @throws "e.www.account.invalid"
     *             : 会话对应用户非法
     */
    public WnWebSession checkSession(String ticket) {
        WnWebSession se = this.getSession(ticket);
        if (null == se) {
            throw Er.create("e.www.ticked.noexist", ticket);
        }
        return se;
    }

    /**
     * 用微信的权限码自动登录
     * 
     * @param code
     *            微信的权限码
     * @return 登录成功后的会话
     */
    public WnWebSession loginByWxCode(String code) {
        // 得到用户的 OpenId
        String openid = wxApi.user_openid_by_code(code);
        if (Strings.isBlank(openid)) {
            throw Er.create("e.www.login.invalid.weixin_code");
        }
        // 得到公众号名称
        String ghName = wxApi.getHomeObj().name();
        String key = "wx_gh_" + ghName;

        // 准备帐号库的服务类
        WnThingService accounts = new WnThingService(io, oAccountHome);

        // 如果已经有了这个用户的微信会话，重用之
        NutMap by = Lang.map("by_type", key);
        by.put("by_val", openid);
        WnQuery q = Wn.Q.pid(oSessionHome);
        q.setAll(by);
        WnObj oSe = io.getOne(q);
        if (null != oSe) {
            String uid = oSe.getString("uid");
            WnObj oU = accounts.getThing(uid, false);
            if (null != oU) {
                return new WnWebSession(oSe, oU);
            }
        }

        // 看看这个用户是否存在，如果已经存在了就直接创建 会话收工
        NutMap meta = Lang.map(key, openid);
        WnObj oU = __get_user(accounts, meta);
        if (null != oU) {
            return this.__create_session(accounts, oU, by);
        }

        // 选择一个默认角色
        String role = this.getDefaultRoleName();

        // 看来要创建一个用户，嗯嗯，先获取信息
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
        String myName = re.getString("nickname", "anonymous");
        meta = re.pickBy("^(city|province|country|sex)$");
        meta.put(key, openid);
        meta.put("th_nm", myName);
        meta.put("role", role);
        oU = accounts.createThing(meta);

        // 如果有头像的话，搞一下
        String headimgurl = re.getString("headimgurl");
        if (!Strings.isBlank(headimgurl)) {
            WnObj oData = Things.dirTsData(io, oU);
            WnObj oThumb = io.createIfNoExists(oData, oU.id() + "/thumb.jpg", WnRace.FILE);
            // 读取 Image
            try {
                URL thumb_url = new URL(headimgurl);
                BufferedImage im = Images.read(thumb_url);
                io.writeImage(oThumb, im);
                oU.thumbnail("id:" + oThumb.id());
                io.set(oU, "^(thumb)$");
            }
            catch (MalformedURLException e) {
                throw Er.wrap(e);
            }
        }

        // 创建完毕，收工
        WnWebSession se = this.__create_session(accounts, oU, by);
        return se;
    }

    /**
     * 绑定手机/邮箱
     * 
     * @param account
     *            账号（手机号|邮箱）
     * @param scene
     *            验证码场景
     * @param vcode
     *            验证码
     * @param ticket
     *            用户登录的票据（必须是已经登录的用户才能绑定啊）
     * @return 绑定成功后的会话（可能会切换会话）
     * 
     * @throws "e.www.login.noexists"
     *             : 用户不存在
     * @throws "e.www.invalid.captcha"
     *             : 验证码错误
     */
    public WnWebSession bindAccount(String account, String scene, String vcode, String ticket) {
        WnLoginObj lo = new WnLoginObj(account);

        // 检查账号名是否合法，顺便准备查询条件
        NutMap meta = lo.toMap();

        // 首先验证一下验证码是否正确
        if (!captcha.removeCaptcha(scene, account, vcode)) {
            throw Er.create("e.www.invalid.captcha", vcode);
        }

        // 首先查询出对应的用户对象
        WnThingService accounts = new WnThingService(io, oAccountHome);
        WnObj oU = __check_user(accounts, meta);

        // 根据票据获取当前登录的会话
        WnWebSession se = this.checkSession(ticket);

        // 如果手机号已经有了一个账号对象
        // - 这种场景是用户预先用手机注册一个账号
        // - 然后在微信打开，微信会自动注册/登录，而这个账号是没有手机号的
        if (null != oU) {
            // 将当前的账号信息合并过去（用户信息作为默认值）
            NutBean info = se.getUserInfo();

            // 登录名就不同步了
            info.remove("nm");

            // 如果有头像的话，看看是否有必要搞一下
            String thumb = info.getString("thumb");
            if (!Strings.isBlank(thumb) && !oU.hasThumbnail()) {
                WnObj oSrcThumb = Wn.getObj(io, thumb);
                if (null != oSrcThumb) {
                    WnObj oData = Things.dirTsData(io, oU);
                    WnObj oThumb = io.createIfNoExists(oData, oU.id() + "/thumb.jpg", WnRace.FILE);
                    // 读取Copy缩略图
                    Wn.Io.copyFile(io, oSrcThumb, oThumb);
                    info.put("thumb", "id:" + oThumb.id());

                }
            }

            // 同时也更新用户最后登录时间咯
            info.setv("login", System.currentTimeMillis());
            io.appendMeta(oU, info);

            // 删除当前账号
            accounts.deleteThing(true, se.getUserId());

            // 修改当前会话的 uid/unm
            se.setMe(oU);
            NutMap seMeta = se.toMeta();

            // 更新会话
            WnObj oSe = io.fetch(oSessionHome, se.getTicket());
            io.appendMeta(oSe, seMeta);

        }
        // 否则直接修改当前账号的 phone 等字段
        else {
            io.appendMeta(se.getMe(), meta);
        }

        // 创建会话并返回
        return se;
    }

    /**
     * 验证码登录
     * 
     * @param account
     *            账号（手机号|邮箱）
     * @param scene
     *            验证码场景
     * @param vcode
     *            验证码
     * @return 登录成功后的会话
     * 
     * @throws "e.www.login.noexists"
     *             : 用户不存在
     * @throws "e.www.invalid.captcha"
     *             : 验证码错误
     */
    public WnWebSession loginByVcode(String account, String scene, String vcode) {
        WnLoginObj lo = new WnLoginObj(account);

        // 检查账号名是否合法，顺便准备查询条件
        NutMap meta = lo.toMap();

        // 首先验证一下验证码是否正确
        if (!captcha.removeCaptcha(scene, account, vcode)) {
            throw Er.create("e.www.invalid.captcha", vcode);
        }

        // 首先查询出对应的用户对象
        WnThingService accounts = new WnThingService(io, oAccountHome);
        WnObj oU = __get_user(accounts, meta);

        // 如果手机号未注册，创建一个新账号
        if (null == oU) {
            // 选择一个默认角色
            String role = this.getDefaultRoleName();
            meta.put("role", role);
            oU = accounts.createThing(meta);
        }

        // 创建会话并返回
        NutMap by = Lang.map("by_tp", "web_vcode");
        by.put("by_val", account);
        WnWebSession se = __create_session(accounts, oU, by);
        return se;
    }

    /**
     * 用户名（手机·邮箱）密码登录
     * 
     * @param account
     *            账号（手机号|邮箱|登录名）
     * @param passwd
     *            密码（明文）
     * @return 登录成功后的会话
     * 
     * @throws "e.www.login.noexists"
     *             : 用户不存在
     * @throws "e.www.login.invalid.passwd"
     *             : 用户名密码错误
     * @throws "e.www.login.forbid"
     *             : 没声明密码，因此禁止此种登录形式
     */
    public WnWebSession loginByPasswd(String account, String passwd) {
        WnLoginObj lo = new WnLoginObj(account);

        // 准备查询条件
        NutMap meta = lo.toMap();

        // 首先查询出对应的用户对象
        WnThingService accounts = new WnThingService(io, oAccountHome);
        WnObj oU = __check_user(accounts, meta);

        // 核对密码和盐
        String expect_pwd = oU.getString("passwd");
        String salt = oU.getString("salt");

        // 没有密码或盐
        if (Strings.isBlank(expect_pwd) || Strings.isBlank(salt)) {
            throw Er.create("e.www.login.forbid");
        }

        // 加盐验证
        String salted_pwd = Wn.genSaltPassword(passwd, salt);
        if (!salted_pwd.equals(expect_pwd)) {
            throw Er.create("e.www.login.invalid.passwd");
        }

        // 创建会话并返回
        NutMap by = Lang.map("by_tp", "web_passwd");
        by.put("by_val", account);
        WnWebSession se = __create_session(accounts, oU, by);
        return se;
    }

    /**
     * @param ticket
     *            用户登录的票据（必须是已经登录的用户才能绑定啊）
     * @return 被注销的会话对象
     */
    public WnWebSession logout(String ticket) {
        WnWebSession se = this.getSession(ticket);
        if (null != se) {
            WnObj oSe = io.get(se.getId());
            if (null != oSe) {
                io.delete(oSe);
            }
        }
        return se;
    }

    private WnWebSession __create_session(WnThingService accounts, WnObj oU, NutMap info) {
        // 默认一天过期
        long expi = System.currentTimeMillis() + (this.sessionDuration * 1000L);

        // 验证通过后，创建会话
        String ticket = R.UU64();
        WnObj oSe = io.create(oSessionHome, ticket, WnRace.FILE);
        WnWebSession se = new WnWebSession(ticket);
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

    private WnObj __get_user(WnThingService accounts, NutMap meta) {
        ThQuery q = new ThQuery(meta);
        return accounts.getOne(q);
    }

    private WnObj __check_user(WnThingService accounts, NutMap meta) {
        WnObj oU = __get_user(accounts, meta);

        // 没找到
        if (null == oU) {
            throw Er.create("e.www.login.noexists");
        }
        return oU;
    }
}
