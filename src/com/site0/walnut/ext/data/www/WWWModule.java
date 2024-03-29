package com.site0.walnut.ext.data.www;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.view.HttpEnhanceResponse;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.www.impl.VirtualPage;
import com.site0.walnut.ext.data.www.impl.VirtualPageFactory;
import com.site0.walnut.impl.box.Jvms;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.module.AbstractWnModule;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnObjDownloadView;
import org.nutz.web.WebException;

@IocBean
@At("/www")
@Ok("void")
@Fail("void")
public class WWWModule extends AbstractWnModule {

    private static final Log log = Wlog.getCMD();

    // private static final Pattern _P = Pattern.compile("^([^/]+)(/(.+))?$");

    private static final List<String> ENTRIES = Wlang.list("index.wnml", "index.html");

    private WnTmpl tmpl_400;
    private WnTmpl tmpl_404;
    private WnTmpl tmpl_500;

    private VirtualPageFactory virtualPages;

    public WWWModule() {
        tmpl_400 = WnTmpl.parse(Files.read("html/400.wnml"));
        tmpl_404 = WnTmpl.parse(Files.read("html/404.wnml"));
        tmpl_500 = WnTmpl.parse(Files.read("html/500.wnml"));
        virtualPages = new VirtualPageFactory();
    }

    @Inject("java:$conf.get('usr-name')")
    private Pattern regexName;

    @Inject("java:$conf.get('usr-phone')")
    private Pattern regexPhone;

    @Inject("java:$conf.get('usr-email')")
    private Pattern regexEmail;

    /**
     * 会话有效期(秒）默认 86400 秒
     */
    @Inject("java:$conf.getLong('session-du',86400)")
    protected long sessionDu;

    @At("/?/**")
    @Filters({@By(type = WWWSetSessionID.class)})
    public View show_page(String usr,
                          String a_path,
                          @Param("d") String download,
                          @ReqHeader("User-Agent") String ua,
                          @ReqHeader("If-None-Match") String etag,
                          @ReqHeader("Range") String range,
                          @Attr("wn_www_host") String host,
                          @Attr("wn_www_site") String sitePath,
                          HttpServletRequest req,
                          HttpServletResponse resp) {
        // 如果有的话，去掉开头的绝对路径符
        if (null == a_path) {
            a_path = "";
        }
        // 得到相对路径
        else if (a_path.startsWith("/")) {
            a_path = a_path.substring(1);
        }

        if (log.isInfoEnabled())
            log.infof("www(%s): /%s/%s", Wlang.getIP(req), usr, a_path);

        // ..............................................
        // 找到用户
        WnAccount u = auth().checkAccount(usr);
        String homePath = u.getHomePath();
        WnObj oHome = io().fetch(null, homePath);

        if (log.isDebugEnabled())
            log.debugf(" - www:usrHome: %s : [%s]", homePath, oHome);

        if (null == oHome) {
            return gen_errpage(tmpl_404, a_path, "Home not exists!", 404);
        }

        // ..............................................
        // 准备起始查询条件: 要找 www 的目录，复制给 oROOT
        WnObj oWWW = null;

        // 如果指定了 site， 直接使用
        if (!Strings.isBlank(sitePath)) {
            NutBean vars = Wlang.map("HOME", homePath).setv("PWD", homePath);
            String aphSite = Wn.normalizeFullPath(sitePath, vars);
            oWWW = io().check(null, aphSite);
        }

        // 试图查找（这可能是一个稍微耗时的操作）
        if (null == oWWW) {
            WnQuery q = new WnQuery();
            q.setv("d0", oHome.d0());
            if (!"root".equals(usr))
                q.setv("d1", oHome.d1());

            if (log.isDebugEnabled())
                log.debugf(" - www:query: %s", q.toString());

            // 请求里带了 host 了吗
            if (null != host && !"localhost".equals(host) && !"127.0.0.1".equals(host)) {
                q.setv("www", host.toString());
                oWWW = io().getOne(q);
            }
            if (log.isDebugEnabled())
                log.debugf(" - www:regHost: %s -> %s", host, oWWW);

            // 实在找不到用 www 目录
            if (null == oWWW) {
                oWWW = io().getOne(q.setv("www", "ROOT"));
            }
        }

        if (log.isDebugEnabled())
            log.debugf(" - www:=ROOT: %s -> %s", host, oWWW);

        // 发布目录不存在
        if (null == oWWW) {
            return gen_errpage(tmpl_404, a_path);
        }

        // ..............................................
        // 通过 www 目录找到文件对象
        NutMap args = new NutMap();
        WnObj o = __find_opage_in_www(a_path, oWWW, args);

        if (log.isDebugEnabled())
            log.debugf(" - www:findObj: %s -> %s", a_path, o);

        // 文件对象不存在，直接 404 咯
        if (null == o) {
            o = io().fetch(oWWW, oWWW.getString("hm_page_404", "404.html"));
            resp.setStatus(404);
            if (o == null)
                return gen_errpage(tmpl_404, a_path);
        }

        // ..............................................
        // 根据目录找到对应的页面
        // ..............................................
        // 目录的话，依次上传入口
        if (o.isDIR()) {
            // 如果不是目录，那么应该返回一个重定向
            // 否则在访问 http://zozoh.com/abc 这样路径的时候，
            // 路径对应的网页里面如果有相对的图片链接，会有问题
            if (!req.getRequestURI().endsWith("/")) {
                String redirectPath;
                Object orgPath = req.getAttribute("wn_www_path_org");

                // 嗯，不是从 WalnutFilter 过来的
                if (null == orgPath) {
                    redirectPath = Wn.appendPath("/www", usr, a_path) + "/";
                }
                // 从 WalnutFilter 过来的，直接使用原始路径
                else {
                    redirectPath = orgPath.toString() + "/";
                }

                if (log.isDebugEnabled())
                    log.debugf(" - www:redirect-> %s", redirectPath);

                // 重定向吧
                return new ServerRedirectView(redirectPath);
            }
            // 获取入口网页的可能列表
            List<String> entries = ENTRIES;
            if (null != oWWW) {
                Object eno = oWWW.get("www_entry");
                List<String> en_list = new ArrayList<String>(6);
                Wlang.each(eno, (int index, String ele, Object src) -> {
                    if (!Strings.isBlank(ele))
                        en_list.add(ele);
                });
                if (en_list.size() > 0)
                    entries = en_list;
            }
            // 依次尝试入口对象
            for (String entry : entries) {
                WnObj o2 = io().fetch(o, entry);
                if (null != o2 && o2.isFILE()) {
                    o = o2;
                    break;
                }
            }

            if (log.isDebugEnabled())
                log.debugf(" - www:findEntry: %s", o);

            // 还是目录，那请求一定是错的
            if (o.isDIR()) {
                o = io().fetch(oWWW, oWWW.getString("hm_page_400", "400.html"));
                resp.setStatus(400);
                if (o == null)
                    return gen_errpage(tmpl_400, a_path);
            }
        }

        // 确保可读，同时处理链接文件
        o = Wn.WC().whenRead(o, false);

        // 渲染这个文件对象
        try {
            // 动态网页
            boolean isDynamic = o.is("as_wnml", true);
            if (!isDynamic)
                isDynamic = o.isType("wnml");
            // 执行命令
            if (isDynamic) {
                if (log.isDebugEnabled())
                    log.debugf(" - www.$ (%s)@%s : %s", o.id(), usr, a_path);

                // 首先创建一个会话
                WnAuthSession se = this.creatSession(usr, false);

                // 得到文件内容
                String input = io().readText(o);

                // 计算路径
                String rootPath = oWWW.getRegularPath();
                String currentPath = o.path();
                String currentDir = o.parent().getRegularPath();
                String pagePath = currentPath.substring(rootPath.length());

                // 从请求对象得到上下文
                NutMap context = __gen_www_context(req, oWWW, pagePath, a_path);
                context.put("CURRENT_PATH", currentPath);
                context.put("CURRENT_DIR", currentDir);
                String uriBase = context.getString("URI_BASE");
                String ctxName = o.getString("CONTEXT_NAME");
                String pageBase = Wn.appendPath(uriBase, ctxName);
                if (!pageBase.endsWith("/")) {
                    pageBase += "/";
                }
                context.put("PAGE_BASE", pageBase);

                // 加入路径参数
                context.put("args", args);

                // 得到一些当前域账号的关键信息
                context.put("grp", se.getMyGroup());
                context.put("fnm", o.name());
                context.put("majorName", Files.getMajorName(o.name()));
                context.put("rs", "/gu/rs/");
                context.put("siteRs", "/www/" + se.getMyGroup() + "/");

                // 如果上下文中有 "wn_www_path_new" 表示 WalnutFilter 已经修改了路径，那么
                if (req.getAttribute("wn_www_path_new") != null) {
                    context.put("REGAPI_BASE", "/api");
                }
                // 否则没有修改过路径，则应该本地环境，那么 api 要补全域
                else {
                    context.put("REGAPI_BASE", "/api/" + se.getMyGroup());
                }

                // 放置一些上下文的接口
                // 这段应该可以被废弃了
                try {
                    WWWPageAPI api = new WWWPageAPI(io(), oHome, sessionDu, oWWW, context);
                    context.put("API", api);
                }
                // 如果 oWWW 没有 hm_site_id, 会构建失败的那么上下文中就不会放置 API 这个对象
                // 但是后续逻辑还是可以继续执行的，页面中如果调用了 API 则通常会失败
                catch (WebException e) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Fail to set API in '%s'", oWWW.path()), e);
                    }
                }

                // 看看是否是已经登录的会话，如果已经登录了，那么要偷偷改一下会话的票据
                // String seph = api.updateSessionTicket();

                // 创建一下解析服务
                // WnmlModuleRuntime wrt = new WnmlModuleRuntime(this, se);
                WnBoxContext bc = createBoxContext(se);
                StringBuilder sbOut = new StringBuilder();
                StringBuilder sbErr = new StringBuilder();
                WnSystem sys = Jvms.createWnSystem(this, jef(), bc, sbOut, sbErr, null);
                WnmlRuntime wrt = new JvmWnmlRuntime(sys);
                WnmlService ws = new WnmlService();

                // 执行转换
                String html = ws.invoke(wrt, context, input);

                // 准备输出响应内容
                HttpEnhanceResponse hsr = new HttpEnhanceResponse();
                hsr.setUpperHeaderName(true);
                hsr.updateBy(html);

                // 默认的内容类型
                hsr.header().putDefault("CONTENT-TYPE", "text/html");
                hsr.setIfNoneMatch(etag);

                // 更新客户端的 cookie 以便匹配上新的 session
                // if (!Strings.isBlank(seph)) {
                // hsr.header().addv("SET-COOKIE", "www=" + seph + "; Path=/;");
                // }

                return new HttpStatusView(hsr);

                // // 如果以 HTTP/1.x 开头，则认为是要输出 HTTP 头
                // if (html.startsWith("HTTP/1.")) {
                //
                // }
                // if (log.isDebugEnabled())
                // log.debugf(" - www.$ (%s)@%s : %s", o.id(), usr, a_path);
                // // 返回网页
                // return new ViewWrapper(new RawView("text/html"), html);
            }
            // 其他的都是静态资源，就直接下载了
            if (log.isDebugEnabled())
                log.debugf(" - www.S (%s)@%s : %s", o.id(), usr, a_path);

            // 纠正一下下载模式
            ua = WnWeb.autoUserAgent(o, ua, download);

            // 返回下载视图
            return new WnObjDownloadView(io(), o, null, ua, etag, range);

        }
        catch (Exception e) {
            if (log.isWarnEnabled())
                log.warn("Server Error!", e);
            return gen_errpage(tmpl_500, a_path, e.toString(), 500);
        }
    }

    protected NutMap __gen_www_context(HttpServletRequest req,
                                       WnObj oWWW,
                                       String pagePath,
                                       String a_path) {
        try {
            NutMap context = _gen_context_by_req(req);
            WWW.joinWWWContext(context, oWWW);
            String url = (String) req.getAttribute("wn_www_url");
            if (url == null)
                url = req.getRequestURL().toString();
            URI uri = new URI(url);
            String uriPath = uri.getPath();
            String basePath;
            // 用 pagePath
            if (!Strings.isBlank(pagePath) && uriPath.endsWith(pagePath)) {
                basePath = uriPath.substring(0, uriPath.length() - pagePath.length());
            }
            // 用 a_path 咯
            else if (!Strings.isBlank(a_path) && uriPath.endsWith(a_path)) {
                basePath = uriPath.substring(0, uriPath.length() - a_path.length());
            }
            // 嗯，没招了
            else {
                basePath = uriPath;
            }

            context.put("PAGE_PATH", pagePath);
            context.put("URL", url);
            context.put("URI_PATH", uriPath);
            context.put("URI_BASE", basePath);

            return context;
        }
        catch (URISyntaxException e) {
            throw Er.wrap(e);
        }
    }

    private WnObj __find_opage_in_www(String a_path, WnObj oWWW, NutMap args) {
        WnObj o = null;

        // 空路径的话，那么意味着对象是 ROOT
        if (Strings.isBlank(a_path)) {
            o = oWWW;
        }
        // 否则如果有 ROOT 在其内查找
        else if (null != oWWW) {
            // 看看是否能直接读到，如果直接有，那么就证明虚页被服务器渲染了，直接读它
            if (!a_path.startsWith("~")
                && !a_path.startsWith("/")
                && !a_path.contains("id:")
                && a_path.endsWith(".html")) {
                o = io().fetch(oWWW, a_path);
                if (null != o) {
                    return o;
                }
            }

            // 如果路径没后缀，或者是 xxx.html（虚网页） 那么试图根据 www_pages，处理一下虚页
            // www_pages : ["index.wnml:abc/page/*,xyz/page/*"]
            if (a_path.endsWith(".html") || !a_path.matches("^.+[.][a-z]+$")) {
                o = __do_with_virtual_page(a_path, oWWW);
            }

            // 已经通过 VirtualPage 机制找到了页面，不再进行后续逻辑了
            if (null != o) {
                return o;
            }

            WnObj oDir;
            // 首先查到路径所在的目录
            String p_ph = Files.getParent(a_path);
            String pgnm = Files.getName(a_path);
            if ("/".equals(p_ph))
                oDir = oWWW;
            else
                oDir = io().fetch(oWWW, p_ph);

            // 如果有所在目录，来一下
            if (null != oDir) {
                o = io().fetch(oDir, pgnm);
                // TODO 这是老的 hmaker 需要的特殊处理，重构完了 Ti 需要删掉
                // 如果没有的话，那么依次找找 `hm_pg_args` 声明的网页能不能匹配上
                if (null == o) {
                    o = __for_hmaker_dynamic(args, o, oDir, pgnm);
                }
            }
        }
        return o;
    }

    protected WnObj __do_with_virtual_page(String a_path, WnObj oWWW) {
        Object wwwPages = oWWW.get("www_pages");

        // 找到相关的 pages
        List<VirtualPage> vpages = new ArrayList<>(5);
        Wlang.each(wwwPages, (int index, String str, Object src) -> {
            VirtualPage page = virtualPages.get(str);
            vpages.add(page);
        });

        WnObj obj = null;
        if (vpages.size() > 0) {
            for (VirtualPage vpage : vpages) {
                if (vpage.match(a_path)) {
                    // 存在 VirtualPage 的话，读取这个页
                    String entryPath = vpage.entryPath;
                    // 直接使用 oWWW
                    if (Strings.isBlank(entryPath)) {
                        obj = oWWW;
                    }
                    // 读一下这个页
                    else {
                        obj = io().fetch(oWWW, entryPath);
                    }
                    // 设置一个 ContextName
                    // 找到虚拟页对应的对象就退出
                    if (null != obj) {
                        obj.setv("CONTEXT_NAME", vpage.contextName);
                        break;
                    }
                }
            }
        }

        return obj;
    }

    // TODO 这是老的 hmaker 需要的特殊处理，重构完了 Ti 需要删掉
    private WnObj __for_hmaker_dynamic(NutMap args, WnObj o, WnObj oDir, String pgnm) {
        WnQuery q2 = Wn.Q.pid(oDir);
        q2.setv("hm_pg_args", true);
        List<WnObj> oCas = io().query(q2);
        for (WnObj oCa : oCas) {
            // 首先先获取一下页面的正则表达式和名称
            String regex = oCa.getString("hm_pg_args_regex");
            List<String> argNames = oCa.getList("hm_pg_args_names", String.class);

            Pattern p;
            Matcher m;

            // 看来没有缓存，或者缓存不正确，那么就分析一下咯
            if (Strings.isBlank(regex) || null == argNames || argNames.isEmpty()) {
                // 首先处理一下占位符，将其变成一个正则表达式
                String fnm = oCa.name();
                regex = "^";
                int pos = 0;
                p = Regex.getPattern("\\{\\{([^\\}]+)\\}\\}");
                m = p.matcher(fnm);
                argNames = new ArrayList<>(3);
                while (m.find()) {
                    argNames.add(m.group(1));
                    if (m.start() > pos) {
                        regex += fnm.substring(pos, m.start());
                    }
                    pos = m.end();
                    regex += "(.+)";
                }
                if (pos < fnm.length()) {
                    regex += fnm.substring(pos);
                }
                regex += "$";
                // 来吧，缓存一下
                NutMap meta = new NutMap();
                meta.put("hm_pg_args_regex", regex);
                meta.put("hm_pg_args_names", argNames);
                io().appendMeta(oCa, meta);
            }
            // 木有占位符，那么放弃吧
            if (argNames.isEmpty())
                continue;
            // 用正则表达式匹配一下
            p = Regex.getPattern(regex);
            m = p.matcher(pgnm);
            if (m.find()) {
                o = oCa;
                for (int i = 0; i < m.groupCount(); i++) {
                    String argName = argNames.get(i);
                    args.put(argName, m.group(i + 1));
                }
                break;
            }
        }
        return o;
    }

    private View gen_errpage(WnTmpl tmpl, String path) {
        String msg;
        int code;
        if (tmpl_400 == tmpl) {
            msg = "Invalid Request";
            code = 400;
        } else if (tmpl_404 == tmpl) {
            msg = "Page NoFound";
            code = 404;
        } else {
            msg = "Server Error";
            code = 500;
        }

        return gen_errpage(tmpl, path, msg, code);
    }

    private View gen_errpage(WnTmpl tmpl, String path, String msg, int code) {
        path = Strings.escapeHtml(path);
        NutMap map = Wlang.map("url", path);
        map.setv("msg", msg);
        String html = tmpl.render(map, false);
        return new ViewWrapper(new RawView("text/html") {
            @Override
            public void render(HttpServletRequest req, HttpServletResponse resp, Object obj)
                    throws Throwable {
                resp.setStatus(code);
                super.render(req, resp, obj);
            }
        }, html);
    }

}
