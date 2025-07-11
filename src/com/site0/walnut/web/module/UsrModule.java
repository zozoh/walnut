package com.site0.walnut.web.module;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.JspView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.WebException;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.web.filter.WnAsUsr;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnObjDownloadView;

/**
 * 处理用户的登入登出，以及用户资料信息等的接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@At("/u")
public class UsrModule extends AbstractWnModule {

    @Inject("java:$conf.get('page-login','login')")
    private String page_login;

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
                WnSession se = auth().checkSession(ticket);
                if (!se.isExpired()) {
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
                    WnSession se = auth().checkSession(ticket);
                    wc.setSession(se);
                    WnUser me = se.getUser();

                    // 创建转换上下文
                    context.put("grp", me.getMainGroup());
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
        WnSession se = auth().loginByPassword(nm, passwd);
        Wn.WC().setSession(se);

        // 执行登录后初始化脚本
        this.exec("do_login", se, "setup -quiet -u 'id:" + se.getUser().getId() + "' usr/login");

        return se.toBean();
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
            WnSession pse = auth().logout(ticket);
            if (null != pse)
                return pse.toBean();
        }
        throw Wlang.makeThrow("logout delete cookie");
    }

}
