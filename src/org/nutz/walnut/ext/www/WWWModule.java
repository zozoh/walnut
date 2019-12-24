package org.nutz.walnut.ext.www;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
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
import org.nutz.mvc.view.HttpEnhanceResponse;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.Jvms;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnObjDownloadView;
import org.nutz.web.WebException;

@IocBean
@At("/www")
@Ok("void")
@Fail("void")
public class WWWModule extends TheMethodsShouldBeRemoved {

    private static final Log log = Logs.get();

    // private static final Pattern _P = Pattern.compile("^([^/]+)(/(.+))?$");

    private static final List<String> ENTRIES = Lang.list("index.wnml", "index.html");

    private Tmpl tmpl_400;
    private Tmpl tmpl_404;
    private Tmpl tmpl_500;

    public WWWModule() {
        tmpl_400 = Tmpl.parse(Files.read("html/400.wnml"));
        tmpl_404 = Tmpl.parse(Files.read("html/404.wnml"));
        tmpl_500 = Tmpl.parse(Files.read("html/500.wnml"));
    }

    @Inject("java:$conf.get('usr-name')")
    private Pattern regexName;

    @Inject("java:$conf.get('usr-phone')")
    private Pattern regexPhone;

    @Inject("java:$conf.get('usr-email')")
    private Pattern regexEmail;

    @At("/?/**")
    @Filters({@By(type = WWWSetSessionID.class)})
    public View show_page(String usr,
                          String a_path,
                          @Param("d") String download,
                          @ReqHeader("User-Agent") String ua,
                          @ReqHeader("If-None-Match") String etag,
                          @ReqHeader("Range") String range,
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
            log.infof("www(%s): /%s/%s", Lang.getIP(req), usr, a_path);

        // ..............................................
        // 找到用户
        WnAccount u = auth.checkAccount(usr);
        String homePath = u.getHomePath();
        WnObj oHome = io.fetch(null, homePath);

        if (log.isDebugEnabled())
            log.debugf(" - www:usrHome: %s : [%s]", homePath, oHome);

        if (null == oHome) {
            return gen_errpage(tmpl_404, a_path, "Home not exists!", 404);
        }

        // ..............................................
        // 准备起始查询条件: 要找 www 的目录，复制给 oROOT
        WnObj oWWW = null;
        WnQuery q = new WnQuery();
        q.setv("d0", oHome.d0());
        if (!"root".equals(usr))
            q.setv("d1", oHome.d1());

        if (log.isDebugEnabled())
            log.debugf(" - www:query: %s", q.toString());

        // 请求里带了 host 了吗
        Object host = req.getAttribute("wn_www_host");
        if (null != host && !"localhost".equals(host) && !"127.0.0.1".equals(host)) {
            q.setv("www", host.toString());
            oWWW = io.getOne(q);
        }
        if (log.isDebugEnabled())
            log.debugf(" - www:regHost: %s -> %s", host, oWWW);

        // 实在找不到用 www 目录
        if (null == oWWW) {
            oWWW = io.getOne(q.setv("www", "ROOT"));
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
            o = io.fetch(oWWW, oWWW.getString("hm_page_404", "404.html"));
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
                Lang.each(eno, new Each<String>() {
                    @Override
                    public void invoke(int index, String ele, int length) {
                        if (!Strings.isBlank(ele))
                            en_list.add(ele);
                    }
                });
                if (en_list.size() > 0)
                    entries = en_list;
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

            // 还是目录，那请求一定是错的
            if (o.isDIR()) {
                o = io.fetch(oWWW, oWWW.getString("hm_page_400", "400.html"));
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
                String input = io.readText(o);

                // 计算路径
                String rootPath = oWWW.path();
                String currentPath = o.path();
                String currentDir = o.parent().path();
                String pagePath = currentPath.substring(rootPath.length());

                // 从请求对象得到上下文
                NutMap context = __gen_www_context(req, oWWW, pagePath, a_path);
                context.put("CURRENT_PATH", currentPath);
                context.put("CURRENT_DIR", currentDir);
                String uriBase = context.getString("URI_BASE");
                String ctxName = o.getString("CONTEXT_NAME");
                String pageBase = Wn.appendPath(uriBase, ctxName) + "/";
                context.put("PAGE_BASE", pageBase);

                // 加入路径参数
                context.put("args", args);

                // 得到一些当前域账号的关键信息
                context.put("grp", se.getMyGroup());
                context.put("fnm", o.name());
                context.put("majorName", Files.getMajorName(o.name()));
                context.put("rs", "/gu/rs");

                // 如果上下文中有 "wn_www_path_new" 表示 WalnutFilter 已经修改了路径，那么
                if (req.getAttribute("wn_www_path_new") != null) {
                    context.put("REGAPI_BASE", "/api");
                }
                // 否则没有修改过路径，则应该本地环境，那么 api 要补全域
                else {
                    context.put("REGAPI_BASE", "/api/" + se.getMyGroup());
                }

                // 放置一些上下文的接口
                try {
                    WWWPageAPI api = new WWWPageAPI(io, oHome, sessionDu, oWWW, context);
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
                WnSystem sys = Jvms.createWnSystem(this, jef, bc, sbOut, sbErr, null);
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
            return new WnObjDownloadView(io, o, ua, etag, range);

        }
        catch (Exception e) {
            if (log.isWarnEnabled())
                log.warn("Server Error!", e);
            return gen_errpage(tmpl_500, a_path, e.toString(), 500);
        }
    }

    static class PageMatcher {

        int step; // 匹配时从哪个下标开始，1 表示从开始， -1 表示从路径结尾, 0 精确匹配
        String[] _phs;

        PageMatcher(String s) {
            String[] ss = Strings.splitIgnoreBlank(s, "/");
            // 后缀模式 "*/xx/xx"
            if (ss.length > 0 && "*".equals(ss[0])) {
                step = -1;
                _phs = Arrays.copyOfRange(ss, 1, ss.length);
            }
            // 前缀模式 "/xx/xx/*"
            else if (s.endsWith("*")) {
                step = 1;
                _phs = Arrays.copyOfRange(ss, 0, ss.length - 1);
            }
            // 精确匹配模式
            else {
                _phs = Strings.splitIgnoreBlank(s, "/");
            }
        }

        /**
         * 匹配给定路径数组
         * 
         * @param path
         *            路径（已经被拆分好的数组）
         * @return null 表示不匹配, [] 表示完全匹配, ["xx","xx"] 表示还剩下的路径部分
         */
        String[] match(String[] paths) {
            // 后缀模式 "*/xx/xx"
            if (-1 == step) {
                int len = paths.length - _phs.length;
                if (len < 0) {
                    return null;
                }
                int i = _phs.length - 1;
                for (; i >= 0; i--) {
                    if (!paths[i].equals(_phs[i]))
                        return null;
                }
                return Arrays.copyOfRange(paths, 0, len);
            }
            // 前缀模式 "/xx/xx/*"
            else if (1 == step) {
                int len = paths.length - _phs.length;
                if (len < 0) {
                    return null;
                }
                int i = 0;
                for (; i < _phs.length; i++) {
                    if (!paths[i].equals(_phs[i]))
                        return null;
                }
                return Arrays.copyOfRange(paths, _phs.length, paths.length);
            }
            // 精确匹配模式
            if (paths.length != _phs.length) {
                return null;
            }
            for (int i = 0; i < _phs.length; i++) {
                if (!paths[i].equals(_phs[i]))
                    return null;
            }
            return new String[0];
        }

    }

    // 格式化虚拟页面
    static class VirtualPage {
        // 用来渲染的页面，譬如 index.wnml
        // 也可以带路径，譬如 abc/index.wnml
        String entryPath;

        // 如果 entryPath 是 abc/index.wnml
        // 这个 abc 就作为 contextName
        String contextName;

        // 匹配列表
        PageMatcher[] matchers;

        VirtualPage(String s) {
            int pos = s.indexOf(':');
            if (pos >= 0) {
                entryPath = Strings.sBlank(s.substring(0, pos), null);
                s = s.substring(pos + 1);
                pos = entryPath.indexOf('/');
                if (pos > 0) {
                    contextName = entryPath.substring(0, pos);
                }
            }
            String[] vps = Strings.splitIgnoreBlank(s, ",");
            matchers = new PageMatcher[vps.length];
            for (int i = 0; i < vps.length; i++) {
                String vp = vps[i];
                matchers[i] = new PageMatcher(vp);
            }
        }

        // 匹配，如果 matchers 是空地，那么作废
        String[] match(String[] paths) {
            if (null != matchers && matchers.length > 0) {
                for (PageMatcher m : matchers) {
                    String[] re = m.match(paths);
                    if (null != re)
                        return re;
                }
            }
            return null;
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
                oDir = io.fetch(oWWW, p_ph);

            // 如果有所在目录，来一下
            if (null != oDir) {
                o = io.fetch(oDir, pgnm);
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
        List<VirtualPage> vpages = new ArrayList<>(5);
        Lang.each(wwwPages, false, new Each<String>() {
            public void invoke(int index, String str, int length) {
                vpages.add(new VirtualPage(str));
            }
        });
        VirtualPage theVirtualPage = null;
        String[] remains = null;
        if (vpages.size() > 0) {
            String[] paths = Strings.splitIgnoreBlank(a_path, "/");
            for (VirtualPage vpage : vpages) {
                remains = vpage.match(paths);
                if (null != remains) {
                    theVirtualPage = vpage;
                }
            }
        }
        // 存在 VirtualPage 的话，读取这个页
        WnObj obj = null;
        if (null != theVirtualPage) {
            String entryPath = theVirtualPage.entryPath;
            // 直接使用 oWWW
            if (Strings.isBlank(entryPath)) {
                obj = oWWW;
            }
            // 读一下这个页
            else {
                obj = io.fetch(oWWW, entryPath);
            }
        }
        // 设置一个 ContextName
        if (null != obj) {
            obj.setv("CONTEXT_NAME", theVirtualPage.contextName);
        }
        return obj;
    }

    // TODO 这是老的 hmaker 需要的特殊处理，重构完了 Ti 需要删掉
    private WnObj __for_hmaker_dynamic(NutMap args, WnObj o, WnObj oDir, String pgnm) {
        WnQuery q2 = Wn.Q.pid(oDir);
        q2.setv("hm_pg_args", true);
        List<WnObj> oCas = io.query(q2);
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
                io.appendMeta(oCa, meta);
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

    private View gen_errpage(Tmpl tmpl, String path) {
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

    private View gen_errpage(Tmpl tmpl, String path, String msg, int code) {
        path = Strings.escapeHtml(path);
        NutMap map = Lang.map("url", path);
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
