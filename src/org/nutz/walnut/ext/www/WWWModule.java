package org.nutz.walnut.ext.www;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.view.HttpServerResponse;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.module.AbstractWnModule;
import org.nutz.walnut.web.view.WnObjDownloadView;

@IocBean
@At("/www")
@Ok("void")
@Fail("void")
public class WWWModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    // private static final Pattern _P = Pattern.compile("^([^/]+)(/(.+))?$");

    private static final String[] ENTRIES = Lang.array("index.wnml", "index.html");

    private Tmpl tmpl_400;
    private Tmpl tmpl_404;
    private Tmpl tmpl_500;

    public WWWModule() {
        tmpl_400 = Tmpl.parse(Files.read("html/400.wnml"));
        tmpl_404 = Tmpl.parse(Files.read("html/404.wnml"));
        tmpl_500 = Tmpl.parse(Files.read("html/500.wnml"));
    }

    @At("/?/_usr/do/login")
    @Ok("++cookie>>:" + WWW.AT_SEID + "=${dseid},${obj.url}")
    @Fail(">>:${obj.url}")
    public NutMap u_do_login(String grp,
                             @Param("str") String str,
                             @Param("passwd") String passwd,
                             HttpServletResponse resp) {
        // 根据传入的用户名（邮箱，手机号）密码创建一个 Map 以便分析
        NutMap u = __gen_u_map(str, passwd);

        // 执行创建
        StringBuilder sbOut = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        String input = Json.toJson(u, JsonFormat.compact());
        this.exec("duc", grp, input, "dusr -login", sbOut, sbErr);

        // 读取配置信息
        NutMap conf = WWW.read_conf(io, grp);

        // 准备返回值
        NutMap re = new NutMap();

        // 如果失败
        if (sbErr.length() > 0) {
            re.put("url", conf.getString("login_fail", "/login_fail.wnml"));
        }
        // 成功的话
        else {
            re.put("url", conf.getString("login_ok", "/"));
            re.put("dseid", Strings.trim(sbOut));
        }

        // 返回
        return re;
    }

    @At("/?/_usr/do/logout")
    @Ok("--cookie>>:" + WWW.AT_SEID + ",${obj}")
    @Fail(">>:${obj}")
    @Filters({@By(type = WWWSetSessionID.class)})
    public String u_do_logout(String grp) {
        String dseid = Wn.WC().getString(WWW.AT_SEID);
        if (null != dseid) {
            this.exec("duc", grp, "dusr -logout " + dseid);
        }

        // 读取配置信息，决定重定向到什么 URL
        NutMap conf = WWW.read_conf(io, grp);
        return conf.getString("logout", "/");
    }

    @Inject("java:$conf.get('usr-name')")
    private Pattern regexName;

    @Inject("java:$conf.get('usr-phone')")
    private Pattern regexPhone;

    @Inject("java:$conf.get('usr-email')")
    private Pattern regexEmail;

    @At("/?/_usr/create")
    @Ok(">>:${obj}")
    @Fail(">>:${obj}")
    public String u_do_create(String grp,
                              @Param("str") String str,
                              @Param("passwd") String passwd) {

        // 根据传入的用户名（邮箱，手机号）密码创建一个 Map 以便分析
        NutMap u = __gen_u_map(str, passwd);

        // 执行创建
        StringBuilder sbOut = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        String input = Json.toJson(u, JsonFormat.compact());
        this.exec("duc", grp, input, "dusr -create", sbOut, sbErr);

        // 读取配置信息
        NutMap conf = WWW.read_conf(io, grp);

        // 如果失败
        if (sbErr.length() > 0) {
            return conf.getString("create_fail", "/create_fail.wnml");
        }

        // 成功的话
        return conf.getString("create_ok", "/");
    }

    private NutMap __gen_u_map(String str, String passwd) {
        NutMap u = new NutMap();

        // 根据传入的字符串自动判断是该设置电话 ...
        if (regexPhone.matcher(str).find()) {
            u.put("mobile", str);
        }
        // 还是邮箱
        else if (regexEmail.matcher(str).find()) {
            u.put("email", str);
        }
        // 还是登录名
        else if (regexName.matcher(str).find()) {
            u.put("name", str);
        }
        // 不合法
        else {
            throw Er.create("e.d.u.create.invalidStr", str);
        }

        // 记录密码
        u.put("passwd", passwd);
        return u;
    }

    @At("/?/**")
    @Filters({@By(type = WWWSetSessionID.class)})
    public View show_page(String usr,
                          String a_path,
                          HttpServletRequest req,
                          @ReqHeader("User-Agent") String ua) {
        // 如果有的话，去掉开头的绝对路径符
        if (null == a_path) {
            a_path = "";
        }
        // 得到相对路径
        else if (a_path.startsWith("/")) {
            a_path = a_path.substring(1);
        }

        if (log.isInfoEnabled())
            log.infof("www(%s): /%s/%s", req.getRemoteAddr(), usr, a_path);

        // ..............................................
        // 找到用户
        WnUsr u = usrs.check(usr);
        String homePath = Strings.sBlank(u.home(), "/home/" + u.name());
        WnObj oHome = io.fetch(null, homePath);

        if (log.isDebugEnabled())
            log.debugf(" - www:usrHome: %s : [%s]", homePath, oHome);

        if (null == oHome) {
            return gen_errpage(tmpl_404, a_path, "Home not exists!");
        }

        // ..............................................
        // 准备起始查询条件: 要找 www 的目录，复制给 oROOT
        WnObj oROOT = null;
        WnQuery q = new WnQuery();
        q.setv("d0", oHome.d0());
        if (!"root".equals(usr))
            q.setv("d1", oHome.d1());

        if (log.isDebugEnabled())
            log.debugf(" - www:query: %s", q.toString());

        // 请求里带了 host 了吗
        Object host = req.getAttribute("wn_www_host");
        if (null != host) {
            q.setv("www", host.toString());
            oROOT = io.getOne(q);
        }
        if (log.isDebugEnabled())
            log.debugf(" - www:regHost: %s -> %s", host, oROOT);

        // 实在找不到用 ROOT
        if (null == oROOT) {
            oROOT = io.getOne(q.setv("www", "ROOT"));
        }

        if (log.isDebugEnabled())
            log.debugf(" - www:=ROOT: %s -> %s", host, oROOT);

        // 发布目录不存在
        if (null == oROOT) {
            return gen_errpage(tmpl_404, a_path);
        }

        // ..............................................
        // 通过 ROOT 找到文件对象
        WnObj o = null;

        // 空路径的话，那么意味着对象是 ROOT
        if (Strings.isBlank(a_path)) {
            o = oROOT;
        }
        // 否则如果有 ROOT 再其内查找
        else if (null != oROOT) {
            o = io.fetch(oROOT, a_path);
        }

        if (log.isDebugEnabled())
            log.debugf(" - www:findObj: %s -> %s", a_path, o);

        // 文件对象不存在，直接 404 咯
        if (null == o) {
            return gen_errpage(tmpl_404, a_path);
        }

        // ..............................................
        // 根据目录找到对应的页面
        // ..............................................
        // 目录的话，依次上传入口
        if (o.isDIR()) {
            // 获取入口网页的可能列表
            String[] entries = ENTRIES;
            if (null != oROOT) {
                entries = oROOT.getArray("www_entry", String.class, ENTRIES);
            }
            // 依次尝试入口对象
            for (String entry : entries) {
                WnObj o2 = io.fetch(o, entry);
                if (null != o2 && o2.isFILE()) {
                    o = o2;
                    break;
                }
            }

            if (log.isDebugEnabled())
                log.debugf(" - www:findEntry: %s", o);

            // 还是目录，那就抛错吧
            if (o.isDIR()) {
                return gen_errpage(tmpl_400, a_path);
            }
            // 如果不是目录，那么应该返回一个重定向
            // 否则在访问 http://zozoh.com/abc 这样路径的时候，
            // 路径对应的网页里面如果有相对的图片链接，会有问题
            String redirectPath;
            Object orgPath = req.getAttribute("wn_www_path_org");

            // 嗯，不是从 WalnutFilter 过来的
            if (null == orgPath) {
                redirectPath = Wn.appendPath("/www", usr, a_path, o.name());
            }
            // 从 WalnutFilter 过来的，直接使用原始路径
            else {
                redirectPath = Wn.appendPath(orgPath.toString(), o.name());
            }

            if (log.isDebugEnabled())
                log.debugf(" - www:redirect-> %s", redirectPath);

            // 重定向吧
            return new ServerRedirectView(redirectPath);
        }

        // 渲染这个文件对象
        try {
            // 动态网页
            // 执行命令
            if (o.isType("wnml")) {
                // 从请求对象得到上下文
                NutMap context = _gen_context_by_req(req);
                context.put("SITE_HOME", oROOT.path());

                // 执行命令
                String json = Json.toJson(context, JsonFormat.compact());
                String cmdText = "www -c -in id:" + o.id();
                String html = this.exec("www", usr, json, cmdText);

                // 如果以 HTTP/1.x 开头，则认为是要输出 HTTP 头
                if (html.startsWith("HTTP/1.")) {
                    HttpServerResponse hsr = new HttpServerResponse();
                    hsr.updateBy(html);
                    return new HttpStatusView(hsr);
                }
                if (log.isDebugEnabled())
                    log.debugf(" - www.$ (%s)@%s : %s", o.id(), usr, a_path);
                // 返回网页
                return new ViewWrapper(new RawView("text/html"), html);
            }
            // 网页图片等，直接显示，清空 UA 后会去掉 CONTENT_DISPOSITION
            else if (o.isType("^(html|htm|css|js|txt|gif|png|jpe?g|webp)$")) {
                if (log.isDebugEnabled())
                    log.debugf(" - www.S (%s)@%s : %s", o.id(), usr, a_path);
                ua = null;
            }
            // 其他的都是静态资源，就直接下载了
            else {
                if (log.isDebugEnabled())
                    log.debugf(" - www.D (%s)@%s : %s", o.id(), usr, a_path);
            }
            // 输出吧
            WnObj o2 = Wn.WC().whenRead(o, false);
            return new WnObjDownloadView(io, o2, ua);

        }
        catch (Exception e) {
            return gen_errpage(tmpl_500, a_path, e.toString());
        }

    }

    private NutMap _gen_context_by_req(HttpServletRequest req) {
        NutMap context = new NutMap();
        // 生成上下文
        NutMap params = new NutMap();

        // 寻找一个请求里的所有参数
        Map<String, String[]> paramMap = req.getParameterMap();
        for (Map.Entry<String, String[]> en : paramMap.entrySet()) {
            String key = en.getKey();
            String[] val = en.getValue();

            if (null == val || val.length == 0)
                continue;

            if (val.length == 1)
                params.put(key, val[0]);
            else
                params.put(key, val);
        }
        context.put("params", params);

        // 得到会话 ID
        context.put("sessionId", Wn.WC().getString(WWW.AT_SEID));

        // 返回
        return context;
    }

    private View gen_errpage(Tmpl tmpl, String path) {
        String msg;
        if (tmpl_400 == tmpl) {
            msg = "Invalid Request";
        } else if (tmpl_404 == tmpl) {
            msg = "Page NoFound";
        } else {
            msg = "Server Error";
        }

        return gen_errpage(tmpl, path, msg);
    }

    private View gen_errpage(Tmpl tmpl, String path, String msg) {
        NutMap map = Lang.map("url", path);
        map.setv("msg", msg);
        String html = tmpl.render(map, false);
        return new ViewWrapper(new RawView("text/html"), html);
    }

}
