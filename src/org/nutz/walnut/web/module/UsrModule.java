package org.nutz.walnut.web.module;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.tmpl.Tmpl;
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
import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrInfo;
import org.nutz.walnut.ext.captcha.Captchas;
import org.nutz.walnut.ext.vcode.VCodes;
import org.nutz.walnut.ext.vcode.WnVCodeService;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnObjDownloadView;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;

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
        String seid = Wn.WC().SEID();
        if (null != seid) {
            try {
                sess.check(seid, true);
                throw Lang.makeThrow("already login, go to /");
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
                          @Param("down") boolean isDownload,
                          @ReqHeader("User-Agent") String ua,
                          @ReqHeader("If-None-Match") String etag,
                          @ReqHeader("Range") String range,
                          HttpServletRequest req) {
        // 确保没有登录过
        String seid = Wn.WC().SEID();
        if (null != seid && "login.html".equals(rph)) {
            try {
                sess.check(seid, true);
                throw Lang.makeThrow("already login, go to /");
            }
            catch (WebException e) {}
        }

        // 嗯开始找一下登录界面
        WnObj oHostHome = null;

        // 看看有没有配置目录
        WnObj oHosts = io.fetch(null, "/etc/hosts.d");
        if (null != oHosts) {
            if (!Strings.isBlank(host)) {
                oHostHome = io.fetch(oHosts, host);
            }
            // 默认的域名为 default
            if (null == oHostHome) {
                oHostHome = io.fetch(oHosts, "default");
            }
        }

        // 获取页面主目录
        WnObj oPageHome = io.fetch(oHostHome, "pages");

        // 有配置目录，那么就要确保有内容哦
        if (null != oPageHome) {
            try {
                // 得到文件内容
                WnObj o = io.check(oPageHome, rph);

                // 确保可读，同时处理链接文件
                o = Wn.WC().whenRead(o, false);

                // 如果不是html，那么必然是资源
                if (!o.name().matches("^.*[.]html?")) {
                    // 特殊的类型，将不生成下载目标
                    ua = WnWeb.autoUserAgent(o, ua, isDownload);

                    // 返回下载视图
                    return new WnObjDownloadView(io, o, ua, etag, range);
                }

                String input = io.readText(o);

                // 准备转换上下文
                NutMap context = _gen_context_by_req(req);
                context.put("fnm", o.name());
                context.put("rs", "/gu/rs");

                // 试图获取代码模板
                WnObj oFragmentHome = io.fetch(oHostHome, "fragment");
                if (null != oFragmentHome) {
                    List<WnObj> oFrags = io.getChildren(oFragmentHome, null);
                    for (WnObj oFrag : oFrags) {
                        String key = Files.getMajorName(oFrag.name());
                        String str = io.readText(oFrag);
                        context.put(key, str);
                    }
                }

                // 如果是改名页面，得到会话信息，以便得到用户名
                if ("rename.html".equals(o.name())) {

                    // 得到会话信息
                    WnSession se = sess.check(seid, false);
                    Wn.WC().SE(se);
                    Wn.WC().me(se.me(), se.group());
                    WnUsr me = Wn.WC().getMyUsr(usrs);

                    // 创建转换上下文
                    context.put("grp", se.group());
                    context.put("me", me);

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
                String html = Tmpl.exec(input, context);

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
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public byte[] vcode_captcha_get(@Param("d") String domain, @Param("a") String accountName) {
        String vcodePath = VCodes.getCaptchaPath(domain, accountName);
        String code = R.captchaNumber(4);

        // 保存:图形验证码只有一次机会
        vcodes.save(vcodePath, code, this.vcodeDuPhone, 1);

        // 返回成功
        return Captchas.genPng(code, 100, 50, Captchas.NOISE);
    }

    @At("/vcode/phone/get")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
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
        if (!Strings.isBlank(re))
            throw Er.create("e.vcode.phone.get", re);

        // 成功
        return true;
    }

    @At("/vcode/email/get")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
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
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public WnUsr do_signup(@Param("str") String str,
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
        WnUsrInfo info = new WnUsrInfo(str);

        // 如果是手机，需要校验验证码
        if (info.isByPhone()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getSignupPath(domain, info.getPhone());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.vcode.invalid");
            }
        }

        // 如果是邮箱，则输入校验验证码
        if (info.isByEmail()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getSignupPath(domain, info.getEmail());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.vcode.invalid");
            }
        }

        // 创建账户
        info.setLoginPassword(passwd);
        WnUsr u = usrs.create(info);

        // 执行创建后初始化脚本
        String cmd = "setup -quiet -u '" + u.name() + "' usr/create";
        if (!Strings.isBlank(mode) && mode.matches("[a-zA-Z0-9_]+")) {
            cmd += " -m " + mode;
        }
        this.exec("do_signup", u.name(), cmd);

        // 返回
        return u;
    }

    @At("/do/signup/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public WnUsr do_signup_ajax(@Param("str") String str,
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
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public NutMap do_login(@Param("nm") String nm, @Param("passwd") String passwd) {
        WnSession se = sess.login(nm, passwd);
        Wn.WC().SE(se);

        // 执行登录后初始化脚本
        this.exec("do_login", se, "setup -quiet -u '" + se.me() + "' usr/login");

        return se.toMapForClient();
    }

    @POST
    @At("/do/login/ajax")
    @Ok("++cookie->ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
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
        String seid = Wn.WC().SEID();
        if (null != seid) {
            WnSession pse = sess.logout(seid);
            if (null != pse)
                return pse.toMapForClient();
        }
        throw Lang.makeThrow("logout delete cookie");
    }

    @POST
    @At("/do/logout/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class))
    public boolean do_logout_ajax() {
        String seid = Wn.WC().SEID();
        if (null != seid) {
            sess.logout(seid);
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

        if (!usrs.checkPassword(nm, passwd)) {
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
        String seid = Wn.WC().SEID();
        String me = sess.check(seid, true).me();
        if (Strings.isBlank(passwd)) {
            throw Er.create("e.usr.pwd.blank");
        }
        if (!regexPasswd.matcher(passwd).find()) {
            throw Er.create("e.usr.pwd.invalid");
        }

        // 得到用户
        WnUsr uMe = usrs.check(me);

        // 检查旧密码是否正确
        if (!usrs.checkPassword(uMe, oldpasswd)) {
            throw Er.create("e.usr.pwd.old.invalid");
        }

        // 设置新密码
        usrs.setPassword(uMe, passwd);
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
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
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
        WnUsrInfo info = new WnUsrInfo(str);

        // 如果是手机，需要校验验证码
        if (info.isByPhone()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getPasswdBackPath(domain, info.getPhone());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.passwd.reset.vcode.invalid");
            }
        }

        // 如果是邮箱，则输入校验验证码
        if (info.isByEmail()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getPasswdBackPath(domain, info.getEmail());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.passwd.reset.vcode.invalid");
            }
        }

        // 得到用户
        WnUsr u = usrs.check(str);

        // 修改密码
        usrs.setPassword(u, passwd);

        // 返回
        return true;
    }

    @At("/do/rename/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class))
    public void do_rename(@Param("nm") String newName, @Param("passwd") String passwd) {
        WnSession se = Wn.WC().checkSE();
        WnUsr me = Wn.WC().getMyUsr(usrs);

        // 先检查一下有没有必要改名
        if (me.name().equals(newName)) {
            throw Er.create("e.u.rename.same");
        }
        // 再检查一下名字是否合法
        WnUsrInfo ui = new WnUsrInfo(newName);
        if (!ui.isByName()) {
            if (ui.isByPhone()) {
                throw Er.create("e.u.rename.byphone");
            }
            if (ui.isByEmail()) {
                throw Er.create("e.u.rename.byemail");
            }
            throw Er.create("e.u.rename.invalid");
        }

        // 看看是否存在
        WnUsr u = this.nosecurity(new Proton<WnUsr>() {
            @Override
            protected WnUsr exec() {
                return usrs.fetchBy(ui);
            }
        });
        // 如果用户存在，那么则必须要检查一下密码
        if (null != u) {
            if (null == passwd || !usrs.checkPassword(u, passwd)) {
                throw Er.create("e.usr.invalid.login");
            }

            // 进入内核态
            this.nosecurity(new Atom() {
                @Override
                public void run() {
                    // 更新一下用户的登录信息
                    NutMap meta = NutMap.WRAP(me.pickBy("^(email|phone|oauth_.+)$"));
                    usrs.set(u, meta);

                    // 切换当前会话到新用户
                    se.putUsrVars(u);
                    se.save();

                    // 原来那个用户就不要了
                    exec("Urnm", "root", "jsc /jsbin/delete_user.js " + me.id());
                }
            });
        }
        // 不存在，则搞一下
        else {
            // 正式执行改名
            usrs.rename(me, newName);

            // 更新 Session
            se.putUsrVars(me);
            se.save();
        }
    }

    // --------- 用户头像

    // 用户头像默认存放在 $HOME/.avatar

    @At("/avatar/me")
    @Ok("raw")
    public Object usrAvatar() {
        WnSession se = sess.check(Wn.WC().SEID(), true);
        String avatarPath = Wn.normalizeFullPath("~/.avatar", se);
        WnObj avatarObj = io.fetch(null, avatarPath);
        if (avatarObj != null) {
            return io.getInputStream(avatarObj, 0);
        } else {
            return this.getClass().getResourceAsStream("/avatar.png");
        }
    }

    @At("/avatar/usr")
    @Ok("raw")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public Object usrAvatar(@Param("nm") String nm) {
        String avatarPath = null;
        // 按照名称
        if ("root".equals(nm)) {
            avatarPath = "/root/.avatar";
        } else {
            avatarPath = "/home/" + nm + "/.avatar";
        }
        WnObj avatarObj = io.fetch(null, avatarPath);
        if (avatarObj != null) {
            return io.getInputStream(avatarObj, 0);
        } else {
            // 尝试查找用户
            WnUsr fUsr = usrs.fetch(nm);
            if (fUsr != null) {
                avatarPath = Wn.appendPath(fUsr.home(), ".avatar");
                avatarObj = io.fetch(null, avatarPath);
                if (avatarObj != null) {
                    return io.getInputStream(avatarObj, 0);
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
        WnUsr u = Wn.WC().security(new WnEvalLink(io), new Proton<WnUsr>() {
            @Override
            protected WnUsr exec() {
                return usrs.fetch(str);
            }
        });
        return u == null ? false : true;
    }

    @At("/booking/exists")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public boolean bookExists(@Param("str") String str) {
        // 已经被预定了
        WnObj oBook = io.fetch(null, "/var/booking/" + str);

        // 已预定
        if (null != oBook)
            return true;

        // 已经存在这个用户
        WnUsr u = usrs.fetch(str);

        // 已存在
        if (null != u)
            return true;

        // 不存在
        return false;
    }

    @At("/do/booking/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public WnObj do_booking_ajax(@Param("str") String str,
                                 @Param("domain") String domain,
                                 @Param("vcode") String vcode) {
        if (Strings.isBlank(str)) {
            throw Er.create("e.usr.signup.blank");
        }

        // 分析注册信息
        WnUsrInfo info = new WnUsrInfo(str);

        // 如果是手机，需要校验验证码
        if (info.isByPhone()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getBookingPath(domain, info.getPhone());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.vcode.invalid");
            }
        }

        // 如果是邮箱，则输入校验验证码
        if (info.isByEmail()) {
            domain = Strings.sBlank(domain, "walnut");
            String vcodePath = VCodes.getBookingPath(domain, info.getEmail());
            if (!vcodes.checkAndRemove(vcodePath, vcode)) {
                throw Er.create("e.usr.vcode.invalid");
            }
        }

        // 检查同名
        if (this.bookExists(str)) {
            throw Er.create("e.booking.exists", str);
        }

        // 创建登录记录
        WnObj oBook = io.create(null, "/var/booking/" + str, WnRace.FILE);

        // 返回
        return oBook;
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
        q.sort(Lang.map("nm:1"));

        // 限制条件
        if (!Strings.isBlank(prefix)) {
            Pattern p = Pattern.compile(prefix.startsWith("^") ? prefix : "^" + prefix);
            q.setv("$or", Lang.list(Lang.map("nm", p), Lang.map("phone", p), Lang.map("email", p)));
        }

        // 忽略的名字
        if (null != ignoreNames && ignoreNames.length > 0) {
            q.setv("nm", Lang.map("$nin", ignoreNames));
        }

        // 执行查询
        Wn.WC().security(new WnEvalLink(io), new Atom() {
            @Override
            public void run() {
                // 查询
                List<WnUsr> us = usrs.query(q);
                // 提取内容
                for (WnUsr u : us) {
                    list.add(u.pick("nm", "nickname"));
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
     * @param seid
     *            新的会话 ID
     * @param isExit
     *            切换会话是从当前会话退出到父会话。false 则表示创建当前会话的子会话
     * 
     * @return 新会话对象
     */
    @At("/ajax/chse")
    @Ok("++cookie->ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class))
    public NutMap ajax_change_session(@Param("seid") String seid, @Param("exit") boolean isExit) {
        // 得到当前会话的
        WnSession se = Wn.WC().checkSE();

        // 不用切换
        if (se.isSame(seid)) {
            throw Er.create("e.web.u.chse.self");
        }

        // 得到新会话
        WnSession seNew = this.sess.check(seid, false);

        // 退出: 这个新回话必须是当前会话的父会话
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
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public Object do_login_auto(@Param("user") String nm,
                                @Param("sign") String sign,
                                @Param("time") long time,
                                @Param("once") String once,
                                @Param("target") String target,
                                HttpServletRequest req) {
        if (Strings.isBlank(nm)) {
            return new HttpStatusView(403);
        }
        WnUsr usr = sess.usrs().fetch(nm);
        if (usr == null) {
            return new HttpStatusView(403);
        }
        String ackey = usr.getString("ackey");
        if (ackey == null) {
            return new HttpStatusView(403);
        }
        int timeout = usr.getInt("ackey_timeout", 1800) * 1000;
        if (timeout == 0) {
            return new HttpStatusView(403);
        }
        if (timeout > 0 && System.currentTimeMillis() - time > timeout) {
            return new HttpStatusView(403);
        }
        String str = ackey + "," + nm + "," + time + "," + once;
        String _sign = Lang.sha1(str);
        if (!_sign.equals(sign)) {
            return new HttpStatusView(403);
        }

        WnSession se = sess.create(sess.usrs().check(usr.name()));
        Wn.WC().SE(se);

        // 执行登录后初始化脚本
        this.exec("do_login", se, "setup -quiet -u '" + se.me() + "' usr/login");

        if (!Strings.isBlank(target))
            req.setAttribute("target", target);
        else
            req.setAttribute("target", "/");

        return se.toMapForClient();
    }

    @At("/check/mplogin")
    @Ok("++cookie>>:/")
    // zozoh: 应该不用切换到 root 目录吧，当前线程的权限就是免检的
    // @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public Object do_check_mplogin(@Param("uu32") String uu32) {
        // zozoh ?? 为啥要 contains("..") ?? 求解释
        if (Strings.isBlank(uu32) || uu32.contains(".."))
            return new HttpStatusView(403);

        // 嗯，那么用哪个账号的微信设置呢？
        // 需要读取 /etc/mplogin 文件，文件内容就是一个字符串，表示处理的路径
        // 如果没有这个文件，那么就采用 /root/.weixin/mplogin/tickets 目录
        WnObj oConf = io.fetch(null, "/etc/mplogin");
        String ticketsHomePath = "/root/.weixin/mplogin/tickets";
        if (null != oConf) {
            ticketsHomePath = Strings.trim(io.readText(oConf));
        }

        // 检查扫码结果
        WnObj obj = io.fetch(null, Wn.appendPath(ticketsHomePath, uu32));

        // 还木有生成，大约是没有被扫码吧
        if (obj == null)
            return new HttpStatusView(403);

        // 生成了文件，但是内容为空，也容忍一下吧
        String uid = Strings.trim(io.readText(obj));
        if (Strings.isBlank(uid))
            return new HttpStatusView(403);

        // 清除登陆信息
        io.delete(obj);

        // 扫码成功，看看给出 uid 是否正确
        WnUsr usr = usrs.check(uid);

        // 为这个用户创建一个会话
        WnSession se = sess.create(usr);
        Wn.WC().SE(se);

        // 执行登录后初始化脚本
        this.exec("do_login", se, "setup -quiet -u '" + se.me() + "' usr/login");

        // 搞定，返回
        return se.toMapForClient();
    }
}
