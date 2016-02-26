package org.nutz.walnut.web.module;

import java.util.regex.Pattern;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
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
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.filter.WnCheckSession;
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

    @At("/signup")
    @Ok("jsp:jsp.signup")
    public void signup() {}

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
    public WnUsr do_signup(@Param("nm") String nm,
                           @Param("passwd") String passwd,
                           @Param("email") String email,
                           @Param("phone") String phone) {
        if (Strings.isBlank(nm)) {
            throw Er.create("e.usr.name.blank");
        }
        if (Strings.isBlank(passwd)) {
            throw Er.create("e.usr.pwd.blank");
        }
        if (!regexPasswd.matcher(passwd).find()) {
            throw Er.create("e.usr.pwd.invalid");
        }
        WnUsr u = usrs.create(nm, passwd);

        if (!Strings.isBlank(email)) {
            usrs.setEmail(nm, email);
        }

        if (!Strings.isBlank(phone)) {
            usrs.setPhone(nm, phone);
        }

        return u;
    }

    @At("/do/signup/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public WnUsr do_signup_ajax(@Param("nm") String nm,
                                @Param("passwd") String passwd,
                                @Param("email") String email,
                                @Param("phone") String phone) {
        return do_signup(nm, passwd, email, phone);
    }

    @Inject("java:$conf.get('page-login','login')")
    private String page_login;

    @At("/login")
    @Fail(">>:/")
    public View show_login() {
        String seid = Wn.WC().SEID();
        if (null != seid) {
            try {
                sess.check(seid);
                throw Lang.makeThrow("already login, go to /");
            }
            catch (WebException e) {}
        }
        return new ViewWrapper(new JspView("jsp." + page_login), null);
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
    public WnSession do_login(@Param("nm") String nm, @Param("passwd") String passwd) {
        WnSession se = sess.login(nm, passwd);
        Wn.WC().SE(se);
        return se;
    }

    @POST
    @At("/do/login/ajax")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public WnSession do_login_ajax(@Param("nm") String nm, @Param("passwd") String passwd) {
        WnSession se = sess.login(nm, passwd);
        Wn.WC().SE(se);
        return se;
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
     * @param pwd
     *            密码
     */
    @POST
    @At("/check/login")
    @Ok("ajax")
    @Fail("ajax")
    public boolean do_check_login(@Param("nm") String nm, @Param("passwd") String pwd) {
        if (Strings.isBlank(pwd))
            throw Er.create("e.usr.blank.pwd");

        if (!usrs.checkPassword(nm, pwd)) {
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
        String me = sess.check(seid).me();
        if (Strings.isBlank(passwd)) {
            throw Er.create("e.usr.pwd.blank");
        }
        if (!regexPasswd.matcher(passwd).find()) {
            throw Er.create("e.usr.pwd.invalid");
        }
        // 检查旧密码是否正确
        if (!usrs.checkPassword(me, oldpasswd)) {
            throw Er.create("e.usr.pwd.old.invalid");
        }
        // 设置新密码
        usrs.setPassword(me, passwd);
        return Ajax.ok();
    }

    @POST
    @At("/reset/password")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnCheckSession.class))
    public Object do_reset_password(@Param("nm") String nm) {
        String seid = Wn.WC().SEID();
        String me = sess.check(seid).me();
        if (!"root".equals(me)) // 只有root可以改其他人的密码
            throw Er.create("e.usr.not.root");
        usrs.setPassword(nm, "123456");
        return Ajax.ok();
    }

    /**
     * 销毁用户的当前会话
     * 
     * @return true 成功登出，false，没有可用会话不用登出
     */
    @At("/do/logout")
    @Filters(@By(type = WnCheckSession.class))
    @Ok("--cookie>>:/")
    @Fail("ajax")
    public boolean do_logout() {
        String seid = Wn.WC().SEID();
        if (null != seid) {
            sess.logout(seid);
            return true;
        }
        return false;
    }

    // --------- 用户头像

    // 用户头像默认存放在 $HOME/.avatar

    @At("/avatar/me")
    @Ok("raw")
    public Object usrAvatar() {
        WnSession se = sess.check(Wn.WC().SEID());
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

    // @At("/avatar/upload")
    // @AdaptBy(type = UploadAdaptor.class, args = {"${app.root}/WEB-INF/tmp"})
    // public Object usrAvatarUpload() {
    // // TODO 暂时直接使用objModule中的upload上传头像文件
    // return null;
    // }

    @GET
    @At("/do/login/auto")
    @Ok(">>:${obj}")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public Object do_login_auto(@Param("user") String nm, 
                                @Param("sign") String sign, 
                                @Param("time") long time,
                                @Param("once") String once,
                                @Param("target")String target) {
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
        if (System.currentTimeMillis() - time > 30*60*1000) {
            return new HttpStatusView(403);
        }
        String str = ackey + "," + nm + "," + time + "," + once;
        String _sign = Lang.sha1(str);
        if (!_sign.equals(sign)) {
            return new HttpStatusView(403);
        }
        sess.create(usr);
        if (Strings.isBlank(target))
            return "/";
        return target;
    }
}
