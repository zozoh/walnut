package org.nutz.walnut.web.module;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
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
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.JspView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrInfo;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.filter.WnCheckSession;
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

    // 可打印字符：[\p{Graph}\x20]
    @Inject("java:$conf.get('usr-passwd','^\\p{Print}{6,}$')")
    private Pattern regexPasswd;

    @Inject("java:$conf.get('page-login','login')")
    private String page_login;

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
    public View show_host(String rph, @Attr("wn_www_host") String host) {
        // 确保没有登录过
        String seid = Wn.WC().SEID();
        if (null != seid) {
            try {
                sess.check(seid, true);
                throw Lang.makeThrow("already login, go to /");
            }
            catch (WebException e) {}
        }

        // 嗯开始找一下登录界面
        WnObj oPageHome = null;

        // 看看有没有配置目录
        WnObj oHosts = io.fetch(null, "/etc/hosts.d");
        if (null != oHosts) {
            if (!Strings.isBlank(host)) {
                oPageHome = io.fetch(oHosts, host + "/pages");
            }
            // 默认的域名为 default
            if (null == oPageHome)
                oPageHome = io.fetch(oHosts, "default/pages");
        }

        // 有配置目录，那么就要确保有内容哦
        if (null != oPageHome) {
            try {
                WnObj o = io.check(oPageHome, rph);
                return new WnObjDownloadView(io, o);
            }
            catch (Exception e) {
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

    /**
     * 处理用户注册
     * 
     * @param nm
     *            用户名
     * @param passwd
     *            密码
     * @param email
     *            邮箱
     * @return 新的用户对象
     */
    @At("/do/signup")
    @Ok(">>:/")
    @Fail("jsp:jsp.show_text")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public WnUsr do_signup(@Param("str") String str,
                           @Param("nm") String nm,
                           @Param("passwd") String passwd,
                           @Param("email") String email,
                           @Param("phone") String phone,
                           @Param("mode") String mode) {
        if (Strings.isBlank(str)
            && Strings.isBlank(nm)
            && Strings.isBlank(email)
            && Strings.isBlank(phone)) {
            throw Er.create("e.usr.signup.blank");
        }
        if (Strings.isBlank(passwd)) {
            throw Er.create("e.usr.pwd.blank");
        }
        if (!regexPasswd.matcher(passwd).find()) {
            throw Er.create("e.usr.pwd.invalid");
        }

        WnUsrInfo info = new WnUsrInfo();
        // 肯定是指定了特殊的字段
        // TODO zozoh 这段逻辑应该删掉，没用了，用自动判断就好
        if (Strings.isBlank(str)) {
            info.setName(nm);
            info.setEmail(email);
            info.setPhone(phone);
        }
        // 自动判断
        else {
            info.setLoginStr(str);
        }

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
                                @Param("nm") String nm,
                                @Param("passwd") String passwd,
                                @Param("email") String email,
                                @Param("phone") String phone,
                                @Param("mode") String mode) {
        return do_signup(str, nm, passwd, email, phone, mode);
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
    @Deprecated
    public NutMap do_login_ajax(@Param("nm") String nm, @Param("passwd") String passwd) {
        WnSession se = sess.login(nm, passwd);
        Wn.WC().SE(se);

        // 执行登录后初始化脚本
        this.exec("do_login", se, "setup -quiet -u '" + se.me() + "' usr/login");

        return se.toMapForClient();
    }

    @POST
    @At("/do/logout/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class))
    @Deprecated
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

    @POST
    @At("/reset/password")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class))
    public Object do_reset_password(@Param("nm") String nm) {
        String seid = Wn.WC().SEID();
        String me = sess.check(seid, true).me();
        WnUsr uMe = usrs.check(me);

        // 只有root组管理员能修改别人密码
        int role = usrs.getRoleInGroup(uMe, "root");
        if (Wn.ROLE.ADMIN != role)
            throw Er.create("e.usr.not.root");

        // 得到用户
        WnUsr u = usrs.check(nm);

        // 修改密码
        usrs.setPassword(u, "123456");

        // 返回
        return Ajax.ok();
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
            return this.getClass().getResourceAsStream("/avatar.png");
        }
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
