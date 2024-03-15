package com.site0.walnut.web.module;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonException;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.JspView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.trans.Atom;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;

import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.auth.WnAuths;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.captcha.Captchas;
import com.site0.walnut.ext.data.vcode.VCodes;
import com.site0.walnut.ext.data.vcode.WnVCodeService;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.web.filter.WnAsUsr;
import com.site0.walnut.web.filter.WnCheckSession;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnImageView;
import com.site0.walnut.web.view.WnObjDownloadView;

/**
 * 处理用户的登入登出，以及用户资料信息等的接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@At("/u")
public class UsrModule extends AbstractWnModule {

    @Inject
    private WnVCodeService vcodes;

    // 可打印字符：[\p{Graph}\x20]
    @Inject("java:$conf.get('usr-passwd','^\\p{Print}{6,}$')")
    private Pattern regexPasswd;

    @Inject("java:$conf.get('page-login','login')")
    private String page_login;

    /**
     * 图形验证码的有效期(分钟）默认 1 分钟
     */
    @Inject("java:$conf.getInt('vcode-du-phone',1)")
    private int vcodeDuCaptcha;

    /**
     * 手机验证码的有效期(分钟）默认 10 分钟
     */
    @Inject("java:$conf.getInt('vcode-du-phone',10)")
    private int vcodeDuPhone;

    /**
     * 邮件验证码的有效期(分钟）默认 2 天 60*24*2 = 2880
     */
    @Inject("java:$conf.getInt('vcode-du-email',2880)")
    private int vcodeDuEmail;

    @At("/signup")
    @Ok("jsp:jsp.signup")
    public void show_signup() {}

    @At("/login")
    @Fail(">>:/")
    public View show_login(String rph, @Attr("wn_www_host") String host) {
        // 确保没有登录过
        if (Wn.WC().hasTicket()) {
            try {
                String ticket = Wn.WC().getTicket();
                auth().checkSession(ticket);
                throw Wlang.makeThrow("already login, go to /");
            }
            catch (WebException e) {}
        }

        // 实在木有，用系统默认的吧
        return new ViewWrapper(new JspView("jsp." + page_login), null);
    }

    @At("/h/**")
    @Fail(">>:/")
    public View show_host(String rph,
                          @Attr("wn_www_host") String host,
                          @Param("d") String download,
                          @ReqHeader("User-Agent") String ua,
                          @ReqHeader("If-None-Match") String etag,
                          @ReqHeader("Range") String range,
                          HttpServletRequest req) {
        WnContext wc = Wn.WC();
        // 确保没有登录过
        if (wc.hasTicket() && "login.html".equals(rph)) {
            try {
                String ticket = wc.getTicket();
                WnAuthSession se = auth().checkSession(ticket);
                if (!se.isDead()) {
                    throw Wlang.makeThrow("already login, go to /");
                }
            }
            catch (WebException e) {}
        }

        // 嗯开始找一下登录界面
        WnObj oHostHome = null;

        // 看看有没有配置目录
        WnObj oHosts = io().fetch(null, "/etc/hosts.d");
        if (null != oHosts) {
            if (!Strings.isBlank(host)) {
                oHostHome = io().fetch(oHosts, host);
            }
            // 默认的域名为 default
            if (null == oHostHome) {
                oHostHome = io().fetch(oHosts, "default");
            }
        }

        // 获取页面主目录
        WnObj oPageHome = io().fetch(oHostHome, "pages");

        // 有配置目录，那么就要确保有内容哦
        if (null != oPageHome) {
            try {
                // 得到文件内容
                WnObj o = io().check(oPageHome, rph);

                // 确保可读，同时处理链接文件
                o = wc.whenRead(o, false);

                // 如果不是html，那么必然是资源
                if (!o.name().matches("^.*[.]html?")) {
                    // 纠正一下下载模式
                    ua = WnWeb.autoUserAgent(o, ua, download);

                    // 返回下载视图
                    return new WnObjDownloadView(io(), o, null, ua, etag, range);
                }

                String input = io().readText(o);

                // 准备转换上下文
                NutMap context = _gen_context_by_req(req);
                context.put("fnm", o.name());
                context.put("rs", "/gu/rs");

                // 试图获取代码模板
                WnObj oFragmentHome = io().fetch(oHostHome, "fragment");
                if (null != oFragmentHome) {
                    List<WnObj> oFrags = io().getChildren(oFragmentHome, null);
                    for (WnObj oFrag : oFrags) {
                        String key = Files.getMajorName(oFrag.name());
                        String str = io().readText(oFrag);
                        context.put(key, str);
                    }
                }

                // 如果是改名页面，得到会话信息，以便得到用户名
                if ("rename.html".equals(o.name())) {

                    // 得到会话信息
                    String ticket = wc.getTicket();
                    WnAuthSession se = auth().checkSession(ticket);
                    wc.setSession(se);
                    WnAccount me = se.getMe();

                    // 创建转换上下文
                    context.put("grp", me.getGroupName());
                    context.put("me", me.toBean());

                    // 创建一下解析服务
                    // WnBoxContext bc = createBoxContext(se);
                    // StringBuilder sbOut = new StringBuilder();
                    // StringBuilder sbErr = new StringBuilder();
                    // WnSystem sys = Jvms.createWnSystem(this, jef, bc, sbOut,
                    // sbErr, null);
                    // WnmlRuntime wrt = new JvmWnmlRuntime(sys);
                    // WnmlService ws = new WnmlService();

                    // 执行转换
                    // String html = ws.invoke(wrt, context, input);
                }

                // 执行转换
                String html = WnTmpl.exec(input, context);

                // 返回网页
                return new ViewWrapper(new RawView("text/html"), html);

            }
            catch (Exception e) {
                if (e instanceof WebException) {
                    if ("e.sess.noexists".equals(((WebException) e).getKey())) {
                        return new ServerRedirectView("/u/h/login.html");
                    }
                }
                if (!"login.html".equals(rph))
                    return new HttpStatusView(404);
            }
        }

        // 实在木有，用系统默认的吧
        String jsp_nm = Files.getMajorName(rph);
        if (jsp_nm.equals("login")) {
            jsp_nm = page_login;
        }
        return new ViewWrapper(new JspView("jsp." + jsp_nm), null);
    }

    @At("/vcode/captcha/get")
    @Ok("raw:image/png")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public byte[] vcode_captcha_get(@Param("d") String domain, @Param("a") String accountName) {
        String vcodePath = VCodes.getCaptchaPath(domain, accountName);
        String code = R.captchaNumber(4);

        // 保存:图形验证码只有一次机会
        vcodes.save(vcodePath, code, this.vcodeDuCaptcha, 1);

        // 返回成功
        return Captchas.genPng(code, 100, 50, Captchas.NOISE);
    }

    @At("/vcode/phone/get")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public boolean vcode_phone_get(@Param("d") String domain,
                                   @Param("a") String phone,
                                   @Param("s") String scene,
                                   @Param("t") String token) {
        // 首先验证一下图片验证码
        String vcodePath = VCodes.getCaptchaPath(domain, phone);
        if (!vcodes.checkAndRemove(vcodePath, token)) {
            return false;
        }

        // 生成手机验证码
        vcodePath = VCodes.getPathBy(domain, scene, phone);
        String code = R.captchaNumber(6);

        // 手机短信验证码最多重试 5 次
        vcodes.save(vcodePath, code, this.vcodeDuPhone, 5);

        // 发送短信
        String cmdText = String.format("sms -r '%s' -t 'i18n:%s' 'min:%d,code:\"%s\"'",
                                       phone,
                                       scene,
                                       this.vcodeDuPhone,
                                       code);
        String re = this.exec("vcode_phone_get", domain, cmdText);

        // 出现意外
        if (Strings.isBlank(re))
            throw Er.create("e.vcode.phone.get", re);

        // 解析返回结果
        try {
            NutMap reo = Json.fromJson(NutMap.class, re);
            NutMap map = reo.getAs(phone, NutMap.class);
            // TODO @wendal 稍后统一下 sms 返回的值的结构
            // https://github.com/zozoh/walnut/issues/456
            if (!map.is("msg", "OK")) {
                throw Er.create("e.vcode.phone.get", map);
            }
        }
        catch (JsonException e) {
            throw Er.create("e.vcode.phone.get", re);
        }

        // 成功
        return true;
    }

    @At("/vcode/email/get")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public boolean vcode_email_get(@Param("d") String domain,
                                   @Param("a") String email,
                                   @Param("s") String scene,
                                   @Param("t") String token) {

        // 首先验证一下图片验证码
        String vcodePath = VCodes.getCaptchaPath(domain, email);
        if (!vcodes.checkAndRemove(vcodePath, token)) {
            return false;
        }

        // 生成手机验证码
        vcodePath = VCodes.getPathBy(domain, scene, email);
        String code = R.captchaChar(8, false);

        // 邮件验证码最多重试 3 次
        vcodes.save(vcodePath, code, this.vcodeDuEmail, 3);

        // 发送邮件
        String cmdText = String.format("email -r '%s' -s 'i18n:%s' -tmpl 'i18n:%s' -vars 'day:%d,code:\"%s\"'",
                                       email,
                                       scene,
                                       scene,
                                       this.vcodeDuEmail / (60 * 24),
                                       code);
        String re = this.exec("vcode_email_get", domain, cmdText);

        // 出现意外
        if (!Strings.isBlank(re))
            throw Er.create("e.vcode.email.get", re);

        // 成功
        return true;
    }

    /**
     * 处理用户注册
     * 
     * @param nm
     *            用户名
     * @param passwd
     *            密码
     * @param mode
     *            指定创建后的逻辑
     * @return 新的用户对象
     */
    @At("/do/signup")
    @Ok(">>:/")
    @Fail("jsp:jsp.show_text")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public WnAccount do_signup(@Param("str") String str,
                               @Param("domain") String domain,
                               @Param("vcode") String vcode,
                               @Param("passwd") String passwd,
                               @Param("mode") String mode) {
        if (Strings.isBlank(str)) {
            throw Er.create("e.usr.signup.blank");
        }
        if (Strings.isBlank(passwd)) {
            throw Er.create("e.usr.pwd.blank");
        }
        if (!regexPasswd.matcher(passwd).find()) {
            throw Er.create("e.usr.pwd.invalid");
        }

        // 分析注册信息
        WnAccount info = new WnAccount(str);

        // 如果是手机，需要校验验证码
        if (info.hasPhone()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getSignupPath(domain, info.getPhone());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.vcode.invalid");
            }
        }

        // 如果是邮箱，则输入校验验证码
        if (info.hasEmail()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getSignupPath(domain, info.getEmail());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.vcode.invalid");
            }
        }

        // 创建账户
        info.setRawPasswd(passwd);
        WnAccount u = auth().createAccount(info);

        // 执行创建后初始化脚本
        String cmd = "setup -quiet -u '" + u.getName() + "' usr/create";
        if (!Strings.isBlank(mode) && mode.matches("[a-zA-Z0-9_]+")) {
            cmd += " -m " + mode;
        }
        this.exec("do_signup", u.getName(), cmd);

        // 返回
        return u;
    }

    @At("/do/signup/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public WnAccount do_signup_ajax(@Param("str") String str,
                                    @Param("domain") String domain,
                                    @Param("vcode") String vcode,
                                    @Param("passwd") String passwd,
                                    @Param("mode") String mode) {
        return do_signup(str, domain, vcode, passwd, mode);
    }

    /**
     * 处理用户登录，并返回客户端模块("zclient")需要的数据结构
     * 
     * @param nm
     *            用户名
     * @param passwd
     *            密码
     */
    @POST
    @At("/do/login")
    @Ok("++cookie>>:/")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public NutMap do_login(@Param("nm") String nm, @Param("passwd") String passwd) {
        WnAuthSession se = auth().loginByPasswd(nm, passwd);
        Wn.WC().setSession(se);

        // 执行登录后初始化脚本
        this.exec("do_login", se, "setup -quiet -u 'id:" + se.getMyId() + "' usr/login");

        return se.toMapForClient();
    }

    @POST
    @At("/do/login/ajax")
    @Ok("++cookie->ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public NutMap do_login_ajax(@Param("nm") String nm, @Param("passwd") String passwd) {
        return do_login(nm, passwd);
    }

    /**
     * 销毁用户的当前会话，如果有父会话，则退出到父会话。否则完全退出登录
     * 
     * @return 父会话
     */
    @At("/do/logout")
    @Ok("++cookie>>:/")
    @Fail("--cookie>>:/")
    public NutMap do_logout() {
        WnContext wc = Wn.WC();
        if (wc.hasTicket()) {
            String ticket = wc.getTicket();
            // 退出登录：延迟几秒以便给后续操作机会
            WnAuthSession pse = auth().logout(ticket, WnAuths.LOGOUT_DELAY);
            if (null != pse)
                return pse.toMapForClient();
        }
        throw Wlang.makeThrow("logout delete cookie");
    }

    @POST
    @At("/do/logout/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class))
    public boolean do_logout_ajax() {
        WnContext wc = Wn.WC();
        if (wc.hasTicket()) {
            String ticket = wc.getTicket();
            // 退出登录：延迟几秒以便给后续操作机会
            auth().logout(ticket, WnAuths.LOGOUT_DELAY);
            return true;
        }
        return false;
    }

    /**
     * 检查登陆信息, 看看是不是用户名密码都对了
     *
     * @param nm
     *            用户名
     * @param passwd
     *            密码
     */
    @POST
    @At("/check/login")
    @Ok("ajax")
    @Fail("ajax")
    public boolean do_check_login(@Param("nm") String nm, @Param("passwd") String passwd) {
        if (Strings.isBlank(passwd))
            throw Er.create("e.usr.blank.passwd");

        WnAccount u = auth().getAccount(nm);

        if (null == u || !u.isMatchedRawPasswd(passwd)) {
            throw Er.create("e.usr.invalid.login");
        }
        return true;
    }

    @POST
    @At("/change/password")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class))
    public Object do_change_password(@Param("oldpasswd") String oldpasswd,
                                     @Param("passwd") String passwd) {
        // 得到会话和用户
        String ticket = Wn.WC().getTicket();
        WnAuthSession se = auth().checkSession(ticket);
        WnAccount me = se.getMe();

        if (Strings.isBlank(passwd)) {
            throw Er.create("e.usr.pwd.blank");
        }
        if (!regexPasswd.matcher(passwd).find()) {
            throw Er.create("e.usr.pwd.invalid");
        }

        // 检查旧密码是否正确
        if (!me.isMatchedRawPasswd(oldpasswd)) {
            throw Er.create("e.usr.pwd.old.invalid");
        }

        // 设置新密码
        me.setRawPasswd(passwd);
        auth().saveAccount(me, WnAuths.ABMM.PASSWD);
        return Ajax.ok();
    }

    // zozoh@2017-04-14: 由于增加了找回密码功能，这个函数先去掉了吧
    // @POST
    // @At("/reset/password")
    // @Ok("ajax")
    // @Fail("ajax")
    // @Filters(@By(type = WnCheckSession.class))
    // public Object do_reset_password(@Param("nm") String nm) {
    // String seid = Wn.WC().SEID();
    // String me = sess.check(seid, true).me();
    // WnUsr uMe = usrs.check(me);
    //
    // // 只有root组管理员能修改别人密码
    // int role = usrs.getRoleInGroup(uMe, "root");
    // if (Wn.ROLE.ADMIN != role)
    // throw Er.create("e.usr.not.root");
    //
    // // 得到用户
    // WnUsr u = usrs.check(nm);
    //
    // // 修改密码
    // usrs.setPassword(u, "123456");
    //
    // // 返回
    // return Ajax.ok();
    // }

    @At("/do/passwd/reset/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public boolean do_passwd_reset_ajax(@Param("str") String str,
                                        @Param("domain") String domain,
                                        @Param("vcode") String vcode,
                                        @Param("passwd") String passwd,
                                        @Param("mode") String mode) {
        if (Strings.isBlank(str)) {
            throw Er.create("e.usr.passwd.reset.blank");
        }
        if (Strings.isBlank(passwd)) {
            throw Er.create("e.usr.passwd.reset.blank");
        }
        if (!regexPasswd.matcher(passwd).find()) {
            throw Er.create("e.usr.passwd.reset.invalid");
        }

        // 分析注册信息
        WnAccount info = new WnAccount(str);

        // 如果是手机，需要校验验证码
        if (info.hasPhone()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getPasswdBackPath(domain, info.getPhone());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.passwd.reset.vcode.invalid");
            }
        }

        // 如果是邮箱，则输入校验验证码
        if (info.hasEmail()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getPasswdBackPath(domain, info.getEmail());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.passwd.reset.vcode.invalid");
            }
        }

        // 得到用户
        WnAccount u = auth().checkAccount(info);

        // 修改密码
        u.setRawPasswd(passwd);
        auth().saveAccount(u, WnAuths.ABMM.PASSWD);

        // 返回
        return true;
    }

    @At("/do/rename/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class))
    public void do_rename(@Param("nm") String newName, @Param("passwd") String passwd) {
        WnAuthSession se = Wn.WC().checkSession();
        WnAccount me = se.getMe();

        // 先检查一下有没有必要改名
        if (me.isSameName(newName)) {
            throw Er.create("e.u.rename.same");
        }

        // 再检查一下名字是否合法
        WnAccount info = new WnAccount(newName);
        if (info.hasPhone()) {
            throw Er.create("e.u.rename.byphone");
        }
        if (info.hasEmail()) {
            throw Er.create("e.u.rename.byemail");
        }
        if (!info.hasName()) {
            throw Er.create("e.u.rename.invalid");
        }

        // 看看是否存在
        WnAccount u = auth().getAccount(info);

        // 如果用户存在，那么则必须要检查一下密码
        if (null != u) {
            if (null == passwd || !u.isMatchedRawPasswd(passwd)) {
                throw Er.create("e.usr.invalid.login");
            }

            // 更新一下用户的登录信息
            me.mergeTo(u);
            auth().saveAccount(u);

            // 切换当前会话到新用户
            se.setMe(u);
            auth().saveSession(se);

            // 原来那个用户就不要了
            auth().deleteAccount(me);
        }
        // 不存在，则搞一下
        else {
            // 正式执行改名
            auth().renameAccount(me, newName);

            // 更新 Session
            se.setMe(me);
            auth().saveSessionVars(se);
        }
    }

    // --------- 用户头像

    // 用户头像默认存放在 $HOME/.avatar

    @At("/avatar/me")
    @Ok("raw")
    public Object usrAvatar() {
        WnContext wc = Wn.WC();
        WnAuthSession se = wc.checkSession(auth());
        String avatarPath = Wn.normalizeFullPath("~/.avatar", se);
        WnObj oAvatar = io().fetch(null, avatarPath);
        // 有自定义的
        if (oAvatar != null) {
            InputStream ins = io().getInputStream(oAvatar, 0);
            String atype = oAvatar.type();
            String amime = oAvatar.mime();
            return new ViewWrapper(new WnImageView(atype, amime), ins);
        }
        // 否则用默认的
        InputStream ins = this.getClass().getResourceAsStream("/avatar.png");
        return new ViewWrapper(new WnImageView("png", "image/png"), ins);
    }

    @At("/avatar/usr")
    @Ok("raw")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public Object usrAvatar(@Param("nm") String nm) {
        String avatarPath = null;
        // 按照名称
        if ("root".equals(nm)) {
            avatarPath = "/root/.avatar";
        } else {
            avatarPath = "/home/" + nm + "/.avatar";
        }
        WnObj avatarObj = io().fetch(null, avatarPath);
        if (avatarObj != null) {
            return io().getInputStream(avatarObj, 0);
        } else {
            // 尝试查找用户
            WnAccount fUsr = auth().getAccount(nm);
            if (fUsr != null) {
                avatarPath = Wn.appendPath(fUsr.getHomePath(), ".avatar");
                avatarObj = io().fetch(null, avatarPath);
                if (avatarObj != null) {
                    return io().getInputStream(avatarObj, 0);
                }
            }
        }
        // 最后不管怎么样，返回一个默认的
        return this.getClass().getResourceAsStream("/avatar.png");
    }

    @At("/exists")
    @Ok("ajax")
    @Fail("ajax")
    public boolean usrExists(@Param("str") String str) {
        WnAccount u = auth().getAccount(str);
        return u == null ? false : true;
    }

    @At("/booking/exists")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public boolean bookExists(@Param("str") String str) {
        // 已经被预定了
        WnObj oBook = io().fetch(null, "/var/booking/" + str);

        // 已预定
        if (null != oBook)
            return true;

        // 已经存在这个用户
        WnAccount u = auth().getAccount(str);

        // 已存在
        if (null != u)
            return true;

        // 不存在
        return false;
    }

    /**
     * 根据给定的条件列出一定数量的用户信息
     * 
     * @param p
     *            用户名前缀，如果为空，则随便列出用户
     * @param nb
     *            最多列出多少用户
     * @param ignoreNames
     *            忽略的名字
     * @return 用户列表
     */
    @At("/ajax/list")
    @Ok("ajax")
    @Fail("ajax")
    public List<NutBean> ajax_list_users(@Param("p") String prefix,
                                         @Param("nb") int nb,
                                         @Param("ignore") String[] ignoreNames) {

        // 不能太多也不能太少
        if (nb <= 0)
            nb = 1;
        if (nb > 100)
            nb = 100;

        // 准备返回值列表
        List<NutBean> list = new ArrayList<>(nb);

        // 准备查询条件
        WnQuery q = new WnQuery();
        // 限制数量和排序
        q.limit(nb);
        q.sort(Wlang.map("nm:1"));

        // 限制条件
        if (!Strings.isBlank(prefix)) {
            Pattern p = Pattern.compile(prefix.startsWith("^") ? prefix : "^" + prefix);
            q.setv("$or", Wlang.list(Wlang.map("nm", p), Wlang.map("phone", p), Wlang.map("email", p)));
        }

        // 忽略的名字
        if (null != ignoreNames && ignoreNames.length > 0) {
            q.setv("nm", Wlang.map("$nin", ignoreNames));
        }

        // 执行查询
        Wn.WC().security(new WnEvalLink(io()), new Atom() {
            @Override
            public void run() {
                // 查询
                List<WnAccount> us = auth().queryAccount(q);
                // 提取内容
                for (WnAccount u : us) {
                    list.add(u.toBeanOf("nm", "nickname"));
                }
            }
        });

        // 返回结果
        return list;
    }

    /**
     * 执行改变当前页面的会话，会改变响应里面的 COOKIE
     * <p>
     * 会话的改变有两种方式，这个由参数 <code>isExit</code> 来界定
     * 
     * <ul>
     * <li><code>isExit == true</code> : 退出到父会话，那么给定的会话ID必须是当前会话的父会话
     * <li><code>isExit == false</code> : 进入子会话，那么给定的会话ID必须是当前会话的子会话
     * </ul>
     * 
     * @param ticket
     *            新的会话 ID
     * @param isExit
     *            切换会话是从当前会话退出到父会话。false 则表示创建当前会话的子会话
     * 
     * @return 新会话对象
     */
    @At("/ajax/chse")
    @Ok("++cookie->ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class, args = {"true", "true"}))
    public NutMap ajax_change_session(@Param("seid") String ticket, @Param("exit") boolean isExit) {
        // 得到当前会话的
        WnAuthSession se = Wn.WC().checkSession();

        // 不用切换
        if (se.isSameTicket(ticket)) {
            throw Er.create("e.web.u.chse.self");
        }
        
        // 会话是域的子账号

        // 得到新会话:系统会话
        WnAuthSession seNew = auth().checkSession(ticket);

        // 退出: 这个新会话必须是当前会话的父会话
        if (isExit) {
            if (!seNew.isParentOf(se)) {
                throw Er.create("e.web.u.chse.exit");
            }
        }
        // 进入: 这个新会话必须是当前会话的子会话
        else {
            if (!se.isParentOf(seNew)) {
                throw Er.create("e.web.u.chse.enter");
            }
        }

        // 执行切换
        return seNew.toMapForClient();
    }

    // @At("/avatar/upload")
    // @AdaptBy(type = UploadAdaptor.class, args = {"${app.root}/WEB-INF/tmp"})
    // public Object usrAvatarUpload() {
    // // TODO 暂时直接使用objModule中的upload上传头像文件
    // return null;
    // }

    @GET
    @At("/do/login/auto")
    @Ok("++cookie>>:${a.target}")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public Object do_login_auto(@Param("user") String nm,
                                @Param("sign") String sign,
                                @Param("time") long time,
                                @Param("once") String once,
                                @Param("target") String target,
                                HttpServletRequest req) {
        if (Strings.isBlank(nm)) {
            return new HttpStatusView(403);
        }
        WnAccount usr = auth().getAccount(nm);
        if (usr == null) {
            return new HttpStatusView(403);
        }
        String ackey = usr.getMetaString("ackey");
        if (ackey == null) {
            return new HttpStatusView(403);
        }
        int timeout = usr.getMetaInt("ackey_timeout", 1800) * 1000;
        if (timeout == 0) {
            return new HttpStatusView(403);
        }
        if (timeout > 0 && Wn.now() - time > timeout) {
            return new HttpStatusView(403);
        }
        String str = ackey + "," + nm + "," + time + "," + once;
        String _sign = Wlang.sha1(str);
        if (!_sign.equals(sign)) {
            return new HttpStatusView(403);
        }

        WnAuthSession se = auth().createSession(usr, true);
        Wn.WC().setSession(se);

        // 执行登录后初始化脚本
        this.exec("do_login", se, "setup -quiet -u 'id:" + se.getMyId() + "' usr/login");

        if (!Strings.isBlank(target))
            req.setAttribute("target", target);
        else
            req.setAttribute("target", "/");

        return se.toMapForClient();
    }

    @At("/check/mplogin")
    @Ok("++cookie>>:/")
    // zozoh: 应该不用切换到 root 目录吧，当前线程的权限就是免检的
    // @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public Object do_check_mplogin(@Param("uu32") String uu32) {
        // zozoh ?? 为啥要 contains("..") ?? 求解释
        if (Strings.isBlank(uu32) || uu32.contains(".."))
            return new HttpStatusView(403);

        // 嗯，那么用哪个账号的微信设置呢？
        // 需要读取 /etc/mplogin 文件，文件内容就是一个字符串，表示处理的路径
        // 如果没有这个文件，那么就采用 /root/.weixin/mplogin/tickets 目录
        WnObj oConf = io().fetch(null, "/etc/mplogin");
        String ticketsHomePath = "/root/.weixin/mplogin/tickets";
        if (null != oConf) {
            ticketsHomePath = Strings.trim(io().readText(oConf));
        }

        // 检查扫码结果
        WnObj obj = io().fetch(null, Wn.appendPath(ticketsHomePath, uu32));

        // 还木有生成，大约是没有被扫码吧
        if (obj == null)
            return new HttpStatusView(403);

        // 生成了文件，但是内容为空，也容忍一下吧
        String uid = Strings.trim(io().readText(obj));
        if (Strings.isBlank(uid))
            return new HttpStatusView(403);

        // 清除登陆信息
        io().delete(obj);

        // 扫码成功，看看给出 uid 是否正确
        WnAccount usr = auth().checkAccountById(uid);

        // 为这个用户创建一个会话
        WnAuthSession se = auth().createSession(usr, true);
        Wn.WC().setSession(se);

        // 执行登录后初始化脚本
        this.exec("do_login", se, "setup -quiet -u '" + se.getMyName() + "' usr/login");

        // 搞定，返回
        return se.toMapForClient();
    }
}
