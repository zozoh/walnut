package org.nutz.walnut.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.Ioc;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.quota.QuotaService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.WnConfig;

public class WalnutFilter implements Filter {

    private static final Log log = Logs.get();

    /**
     * 被这个正则表达式匹配的 host 不进行转义
     */
    private Pattern rMainHost;

    /**
     * 通过这个名字，从 ThreadLocal 里获取 Nutz 注册的 Ioc 容器等
     */
    private String nutzFilterName;

    /**
     * 显示错误信息的 JSP 页面
     */
    private String errorPage;

    private Ioc ioc;

    private WnIo io;

    private WnObj oDmnHome;

    private ArrayList<DmnMatcher> _dms;

    protected QuotaService quotaService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        // 用 Jettry 的 Request 对象接口
        // Request req = (Request) arg0;
        HttpServletRequest req = (HttpServletRequest) request;

        // 分析路径
        String path = Wn.appendPath(req.getServletPath(), req.getPathInfo());
        String usrip = Lang.getIP(req);
        String host = req.getHeader("Host");
        int port = req.getLocalPort();

        // 容忍 HEADER 里没有 Host 字段的情况
        if (null == host) {
            host = req.getLocalName();
        }
        // 如果有 Host 字段则提取
        else {
            int pos = host.lastIndexOf(':');
            if (pos > 0)
                host = host.substring(0, pos);
            // 把 www.nutz.cn 当 nutz.cn 看待
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
        }

        if (log.isInfoEnabled()) {
            log.infof("HTTP(%s)%s>%s:%d", path, usrip, host, port);
        }

        // 一定记录属性到请求对象
        req.setAttribute("wn_www_path_org", path);
        req.setAttribute("wn_www_host", host);
        req.setAttribute("wn_www_ip", usrip);
        req.setAttribute("wn_www_port", port);

        // 确保有 Io 接口等
        __setup();

        // TODO zozoh: 再检查一下下面的逻辑，登录状态也应该路由，不会有啥问题的，除非规则写错了
        // 检查一下会话状态，如果是登录状态，那么就不要路由了
        // 否则就根据配置，判断是否对域名路由
        // WnContext wc = Wn.WC();
        // CreateWnContext.setupWnContext(wc, req);
        // WnSession se = WnCheckSession.testSession(wc, ioc);

        // if (null == se && rMainHost != null &&
        // !rMainHost.matcher(host).find()) {

        // main-host 配置项目指定了哪些 host 是不要路由的
        if (null != rMainHost && !rMainHost.matcher(host).find()) {
            WnObj oDmn = null;

            // 首先试图找到对应的映射记录
            if (null != oDmnHome) {
                WnQuery q = Wn.Q.pid(oDmnHome);
                q.setv("dmn_host", host);
                // q.setv("dmn_expi", "[" + System.currentTimeMillis() + ",]");
                oDmn = io.getOne(q);
            }

            // 找不到记录，全当没有
            if (null == oDmn) {
                chain.doFilter(req, resp);
                return;
            }

            // 找到了记录，但是过期了，显示错误页
            if (oDmn.isExpired() || oDmn.getLong("dmn_expi", 0) < System.currentTimeMillis()) {
                Mvcs.updateRequestAttributes(req);
                req.setAttribute("obj", Lang.map("host", host).setv("path", path));
                req.setAttribute("err_message", "域名转发过期");
                req.getRequestDispatcher(errorPage).forward(req, resp);
                return;
            }

            String grp = oDmn.getString("dmn_grp");

            // 看看流量还够不够
            if (quotaService == null) {
                quotaService = Mvcs.ctx().getDefaultIoc().get(QuotaService.class, "quota");
            }
            if (!quotaService.checkQuota("network", grp, false)) {
                Mvcs.updateRequestAttributes(req);
                req.setAttribute("obj", Lang.map("host", host).setv("path", path));
                req.setAttribute("err_message", "流量已经超出限额");
                req.getRequestDispatcher(errorPage).forward(req, resp);
                return;
            }

            // 准备替换的上下文
            NutMap map = new NutMap().setv("grp", grp);
            String newPath = null;

            // 然后，根据映射规整得到新的改过的 URL
            // 这个规则通常定义在 hostmap 文件中
            // 当然在 web.xml 你可以指定其他的规则文件
            for (DmnMatcher dm : _dms) {
                Matcher m = dm.regex.matcher(path);
                if (m.find()) {
                    for (int i = 0; i <= m.groupCount(); i++) {
                        map.put("" + i, m.group(i));
                    }
                    newPath = dm.tmpl.render(map);
                    break;
                }
            }

            // 正常情况得到修订的 URL，那么 ...
            if (null != newPath) {
                // 将一些必要的信息都记录到 req 对象里，便于 WWW 模块处理
                req.setAttribute("wn_www_path_new", newPath);
                req.setAttribute("wn_www_grp", grp);

                // 这个通常还是要记录一下日志的
                if (log.isDebugEnabled()) {
                    log.debug(" - router to: " + newPath);
                }

                // 服务器端转发到对应的处理路径
                // req.setServletPath(newPath); // 擦,这是什么鬼 // zozoh: 额，换成下面的写法了
                req.getRequestDispatcher(newPath).forward(req, resp);
                return;

            }
            // 得不到修订的 URL，表示规则文件设定的不合理，没有考虑所有的情况
            // 那么我也做不了啥了，直接做个记录吧。
            // 考虑到或许可以依靠后续的 filter 来处理，那么 .. 还是继续其他的 Filter 吧
            else {
                if (log.isDebugEnabled()) {
                    log.debug(" - no rule -");
                }
            }
        }

        // 继续执行
        chain.doFilter(req, resp);
    }

    private void __setup() {
        // 得到 IO 接口
        if (null == io) {
            Mvcs.set(nutzFilterName, null, null);
            ioc = Mvcs.getIoc();
            io = ioc.get(WnIo.class, "io");

            // 处理域名的正则表达式，符合这个表达式的域名，统统不路由
            // 这个配置项，存放在 web.properties 里，如果没有，则为 null
            // 表示统统不路由
            WnConfig conf = ioc.get(WnConfig.class, "conf");
            String regex = Strings.trim(conf.get("main-host"));
            if (!Strings.isBlank(regex)) {
                rMainHost = Pattern.compile(regex);
            }
        }

        // 试图找到对应的 Host
        if (null == oDmnHome) {
            oDmnHome = io.createIfNoExists(null, "/domain", WnRace.DIR);
        }
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
        this.nutzFilterName = fc.getInitParameter("nutzFilterName");
        this.errorPage = fc.getInitParameter("errorPage");
        // 读取 hostmap 文件
        String hmPath = Strings.sBlank(fc.getInitParameter("hostMap"), "hostmap");
        BufferedReader br = Streams.buffr(Streams.fileInr(hmPath));
        try {
            _dms = new ArrayList<DmnMatcher>();
            String line;
            while (null != (line = br.readLine())) {
                // 忽略空行和注释
                if (Strings.isBlank(line) || line.startsWith("#")) {
                    continue;
                }
                String[] ss = Strings.splitIgnoreBlank(line, "=>");
                DmnMatcher dm = new DmnMatcher();
                dm.regex = Pattern.compile(ss[0]);
                dm.tmpl = Tmpl.parse(ss[1]);
                _dms.add(dm);
            }
            _dms.trimToSize();
        }
        catch (IOException e) {
            throw new ServletException(e);
        }
        finally {
            Streams.safeClose(br);
        }
    }

    @Override
    public void destroy() {}

}
