package com.site0.walnut.jetty;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.log.Log;
import org.nutz.mvc.Mvcs;
import com.site0.walnut.jetty.log.AccessLog;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;

import com.alibaba.druid.pool.DruidDataSource;

public class AccessLogFilter implements Filter, Runnable {
    private static final Log log = Wlog.getAC();
    /**
     * 是否启用本filter
     */
    protected static Boolean enable;
    /**
     * 通往ORM的神器
     */
    public static Dao dao;
    /**
     * 执行插入的线程池
     */
    protected ExecutorService es;
    /**
     * 停止服务的标志位
     */
    protected boolean stoped;
    /**
     * 插入队列
     */
    protected ArrayBlockingQueue<AccessLog> queue;
    /**
     * 数据源
     */
    protected static DruidDataSource dataSource;
    /**
     * 跟踪id,跟随浏览器进程
     */
    protected static String AT_WN_TRACE_ID = "WNTID";
    /**
     * 用户跟踪id, 长期存活,标识唯一用户
     */
    protected static String AT_WN_TRACE_UID = "WNTUID";

    public void init(FilterConfig filterConfig) throws ServletException {
        queue = new ArrayBlockingQueue<>(8192);
        es = Executors.newSingleThreadExecutor();

        // 本实例就是worker,默认5线程工作
        for (int i = 0; i < 5; i++) {
            es.submit(this);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (enable == null) {
            // 检查是否启用,并创建连接池
            PropertiesProxy conf = Mvcs.ctx().getDefaultIoc().get(PropertiesProxy.class, "conf");
            setup(conf);
        }
        // 如果未启用或者已关机,直接下一个
        if (!enable || stoped) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        AccessLog alog = new AccessLog();
        // 检查跟踪信息, 包括 跟踪id,用户识别id,会话id
        String wntid = null;
        String wnuid = null;
        String seid = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : req.getCookies()) {
                if (AT_WN_TRACE_ID.equals(cookie.getName())) {
                    wntid = cookie.getValue();
                } else if (AT_WN_TRACE_UID.equals(cookie.getName())) {
                    wnuid = cookie.getValue();
                } else if (Wn.AT_SEID.equals(cookie.getName())) {
                    seid = cookie.getValue();
                }
            }
        }
        // 如果没有跟踪id,添加对应的cookie
        if (wntid == null) {
            wntid = R.UU32();
            Cookie cookie = new Cookie(AT_WN_TRACE_ID, wntid);
            cookie.setPath("/");
            resp.addCookie(cookie);
        }
        // 如果没有用户跟踪信息,添加对应的cookie
        if (wnuid == null) {
            wnuid = R.UU32();
            Cookie cookie = new Cookie(AT_WN_TRACE_UID, wnuid);
            cookie.setMaxAge(365 * 24 * 60 * 60);// 30天
            cookie.setPath("/");
            resp.addCookie(cookie);
        }
        alog.setTraceId(wntid);
        alog.setUserId(wnuid);
        alog.setSessionId(seid);
        // end 检查跟踪信息
        // 开始计时,执行后续操作
        Stopwatch sw = Stopwatch.begin();
        try {
            chain.doFilter(request, response);
        }
        finally {
            try {
                sw.stop();
                alog.setId(R.UU32());
                alog.setDuration(sw.du());
                alog.setCreateTime(new Date(sw.getStartTime()));
                alog.setHost(req.getHeader("Host"));
                alog.setMethod(req.getMethod().toUpperCase());
                // 最多存放512个字符的查询字符串,应该完全够了吧
                String queryString = req.getQueryString();
                if (!Strings.isBlank(queryString)) {
                    if (queryString.length() > 512) {
                        queryString = queryString.substring(0, 512);
                    }
                    alog.setQueryString(queryString);
                }
                // 请求的URI不会太长吧?
                String uri = req.getRequestURI();
                if (uri.length() > 512) {
                    uri = uri.substring(0, 512);
                }
                alog.setUri(uri);
                alog.setReferer(req.getHeader("Referer"));
                alog.setRemoteIp(Lang.getIP(req));
                alog.setRespCode(resp.getStatus());
                alog.setUserAgent(req.getHeader("User-Agent"));
                queue.offer(alog, 10, TimeUnit.MILLISECONDS);
            }
            catch (Throwable e) {
                log.debug("something happen", e);
            }
        }
    }

    public void destroy() {
        stoped = true;
        es.shutdown();
    }

    @Override
    public void run() {
        while (!stoped) {
            if (enable == null) {
                Lang.quiteSleep(1000);
                continue;
            }
            try {
                AccessLog alog = queue.poll(1, TimeUnit.SECONDS);
                if (alog == null)
                    continue;
                dao.fastInsert(alog);
            }
            catch (Throwable e) {
                log.debug("something happen", e);
                Lang.quiteSleep(1000);
            }
        }
    }
    
    public static final void setup(PropertiesProxy conf) {
        if (enable != null)
            return;
        enable = conf.getBoolean("access-log-enable", false);
        if (enable) {
            String jdbcUrl = conf.check("access-log-jdbc-url");
            String jdbcUserName = conf.check("access-log-jdbc-username");
            String jdbcPassword = conf.check("access-log-jdbc-password");
            dataSource = new DruidDataSource();
            dataSource.setUrl(jdbcUrl);
            dataSource.setUsername(jdbcUserName);
            dataSource.setPassword(jdbcPassword);
            dataSource.setMaxActive(10);
            dataSource.setMaxWait(1000);
            dao = new NutDao(dataSource);
            dao.create(AccessLog.class, false);
            Daos.migration(dao, AccessLog.class, true, false, false);
        }
        else {
            log.info("access log is disabled");
        }
    }
}
