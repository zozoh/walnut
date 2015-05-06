package org.nutz.walnut.web.module;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.web.WebException;

/**
 * 处理用户的登入登出，以及用户资料信息等的接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@At("/u")
public class UsrModule extends AbstractWnModule {

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
        if (Strings.isBlank(passwd) || passwd.length() < 8) {
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

    @At("/login")
    @Ok("jsp:jsp.login")
    @Fail(">>:/")
    public void show_login() {
        String seid = Wn.WC().SEID();
        if (null != seid) {
            try {
                sess.check(seid);
                throw Lang.makeThrow("already login, go to /");
            }
            catch (WebException e) {}
        }
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
    public WnSession do_login(@Param("nm") String nm, @Param("pwd") String passwd) {
        WnSession se = sess.login(nm, passwd);
        Wn.WC().SE(se);
        return se;
    }

    // /**
    // * 检查登陆信息, 看看是不是用户名密码都对了
    // *
    // * @param nm
    // * 用户名
    // * @param pwd
    // * 密码
    // */
    // @POST
    // @At("/check/login")
    // @Ok("ajax")
    // @Fail("ajax")
    // public boolean do_check_login(@Param("nm") String nm, @Param("pwd")
    // String pwd) {
    // if (Strings.isBlank(pwd))
    // throw Er.create("e.usr.blank.pwd");
    //
    // WnUsr u = usrs.check(nm);
    // if (!u.password().equals(pwd)) {
    // throw Er.create("e.usr.invalid.login");
    // }
    // return true;
    // }

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

}
