package org.nutz.walnut.ext.www;

import java.util.Map;
import java.util.regex.Matcher;
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
import org.nutz.mvc.view.HttpServerResponse;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView;
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

    private static final Pattern _P = Pattern.compile("^([^/]+)(/(.+))?$");

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
        this._run_cmd("duc", grp, input, "dusr -login", sbOut, sbErr);

        // 读取配置信息
        NutMap conf = WWW.read_conf(io, grp);

        // 准备返回值
        NutMap re = new NutMap();

        // 如果失败
        if (sbErr.length() > 0) {
            re.put("url", conf.getString("create_fail", "/create_fail.wnml"));
        }

        // 成功的话
        re.put("url", conf.getString("login_ok", "/"));
        re.put("dseid", Strings.trim(sbOut));

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
            this._run_cmd("duc", grp, "dusr -logout " + dseid);
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
        this._run_cmd("duc", grp, input, "dusr -create", sbOut, sbErr);

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
    public View show_page(String usr, String a_path, HttpServletRequest req) {
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

        // 找到用户和对应的命令
        WnUsr u = usrs.check(usr);
        String homePath = Strings.sBlank(u.home(), "/home/" + u.name());
        WnObj oHome = io.fetch(null, homePath);

        if (null == oHome) {
            return gen_errpage(tmpl_404, a_path, "Home not exists!");
        }

        // 准备起始查询条件
        WnQuery q = new WnQuery();
        q.setv("d0", oHome.d0());
        if (!"root".equals(usr))
            q.setv("d1", oHome.d1());

        // 开始试图找到文件对象
        WnObj o = null;

        // 找到 ROOT
        String contextPath = "";
        WnObj oROOT = io.getOne(q.setv("www", "ROOT"));

        // 空路径的话，那么意味着对象是 ROOT
        if (Strings.isBlank(a_path)) {
            // 又没有 ROOT 又没有 a_path ，毛，肯定啥也没有
            if (null == oROOT) {
                return gen_errpage(tmpl_404, a_path);
            }
            // 对象本身就是 ROOT 需要拿它的默认值
            o = oROOT;
        }
        // 否则如果有 ROOT 再其内查找
        else if (null != oROOT) {
            o = io.fetch(oROOT, a_path);
        }

        // 这个是一个对象
        String[] entries = ENTRIES;
        if (null != oROOT) {
            entries = oROOT.getArray("www_entry", String.class, ENTRIES);
        }

        // 如果木有，那么看看是不是存在其他的 www 映射
        if (null == o) {
            // 分析路径
            Matcher m = _P.matcher(a_path);
            String cate = null;
            String path = null;
            // 至少是二级路径
            if (m.find()) {
                cate = m.group(1);
                path = m.group(3);

                oROOT = io.getOne(q.setv("www", cate));
                contextPath = cate;
                if (null != oROOT) {
                    o = io.fetch(oROOT, path);
                    entries = oROOT.getArray("www_entry", String.class, entries);
                }
            }
            // 否则肯定还是啥也木有
            else {
                return gen_errpage(tmpl_404, a_path);
            }

        }

        // 找不到文件对象，就是找不到咯
        if (null == o) {
            return gen_errpage(tmpl_404, a_path);
        }

        // 如果发现找到的是个路径对象，依次尝试入口
        if (o.isDIR()) {
            for (String entry : entries) {
                WnObj o2 = io.fetch(o, entry);
                if (null != o2 && o2.isFILE()) {
                    o = o2;
                    break;
                }
            }
            // 还是目录，那就抛错吧
            if (o.isDIR()) {
                return gen_errpage(tmpl_400, a_path);
            }
        }

        try {
            // 动态网页
            // 执行命令
            if (o.isType("wnml")) {
                // 从请求对象得到上下文
                NutMap context = _gen_context_by_req(req);
                context.put("SITE_HOME", oROOT.path());
                String base = Wn.WC().getString(WWW.AT_BASE, "/www/" + usr);
                base = Wn.appendPath(base, contextPath);
                if (base.endsWith("/")) {
                    base = base.substring(0, base.length() - 1);
                }
                context.put("base", base);

                // 执行命令
                String json = Json.toJson(context, JsonFormat.compact());
                String cmdText = "www -c -in id:" + o.id();
                String html = this._run_cmd("www", usr, json, cmdText);

                // 如果以 HTTP/1.x 开头，则认为是要输出 HTTP 头
                if (html.startsWith("HTTP/1.")) {
                    HttpServerResponse hsr = new HttpServerResponse();
                    hsr.updateBy(html);
                    return new HttpStatusView(hsr);
                }

                // 返回网页
                return new ViewWrapper(new RawView("text/html"), html);
            }
            // 其他的，就直接下载了
            if (log.isDebugEnabled())
                log.debugf(" - download (%s)@%s : %s", o.id(), usr, a_path);
            return new WnObjDownloadView(io, o);

        }
        catch (Exception e) {
            return gen_errpage(tmpl_500, a_path, e.toString());
        }

    }

    private NutMap _gen_context_by_req(HttpServletRequest req) {
        NutMap context = new NutMap();
        // 生成上下文
        NutMap params = new NutMap();
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
