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

import org.eclipse.jetty.server.Request;
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
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.web.WnConfig;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.processor.CreateWnContext;

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

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        Request req = (Request) arg0;

        String path = Wn.appendPath(req.getServletPath(), req.getPathInfo());
        String usrip = req.getRemoteAddr();
        String host = req.getHeader("Host");
        int port = req.getLocalPort();
        int pos = host.lastIndexOf(':');
        if (pos > 0)
            host = host.substring(0, pos);

        if (log.isInfoEnabled()) {
            log.infof("HTTP(%s)%s>%s:%d", path, usrip, host, port);
        }

        // 确保有 Io 接口等
        __setup();

        // 检查一下会话状态，如果是登录状态，那么就不要路由了
        // 否则就根据配置，判断是否对域名路由
        WnContext wc = Wn.WC();
        CreateWnContext.setupWnContext(wc, req);
        WnSession se = WnCheckSession.testSession(wc, ioc);

        if (null == se && rMainHost != null && !rMainHost.matcher(host).find()) {
            WnObj oDmn = null;

            // 首先试图找到对应的组
            if (null != oDmnHome) {
                WnQuery q = Wn.Q.pid(oDmnHome);
                q.setv("dmn_host", host);
                q.setv("dmn_expi", "[" + System.currentTimeMillis() + ",]");
                oDmn = io.getOne(q);
            }

            // 找不到记录
            if (null == oDmn) {
                Mvcs.updateRequestAttributes(req);
                req.setAttribute("obj", Lang.map("host", host));
                req.getRequestDispatcher(errorPage).forward(req, resp);
                return;
            }

            String grp = oDmn.getString("dmn_grp");

            // 准备替换的上下文
            NutMap map = new NutMap().setv("grp", grp);
            String newPath = null;

            // 然后，根据映射规整改变 URL
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

            // 如果改变了 URL
            if (null != newPath) {
                req.setServletPath(newPath);
                if (log.isDebugEnabled()) {
                    log.debug(" - router to: " + newPath);
                }
            }
            // 否则做个记录
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
