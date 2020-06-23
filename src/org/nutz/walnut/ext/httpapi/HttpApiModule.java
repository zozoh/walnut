package org.nutz.walnut.ext.httpapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Callback2;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.pvg.BizPvgService;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnStr;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.module.AbstractWnModule;
import org.nutz.walnut.web.module.AppRespOutputStreamWrapper;
import org.nutz.walnut.web.module.HttpRespStatusSetter;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.web.WebException;

@IocBean
@At("/api")
@Filters(@By(type = WnAsUsr.class, args = {"root"}))
public class HttpApiModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    @At("/?/**")
    @Ok("void")
    @Fail("void")
    public void do_api(String usr,
                       String api,
                       final HttpServletRequest req,
                       final HttpServletResponse resp)
            throws IOException {
        final WnHttpApiContext apc = new WnHttpApiContext();
        apc.usr = usr;
        apc.api = api;
        apc.req = req;
        apc.resp = resp;
        apc.reqMeta = new NutMap();
        // 找到用户和对应的命令
        try {
            if (log.isInfoEnabled())
                log.infof("httpAPI(%s): /%s/%s", Lang.getIP(req), usr, api);

            // .........................................
            // 得到用户和主目录
            apc.u = auth.checkAccount(usr);
            String homePath = apc.u.getHomePath();
            apc.oHome = io.check(null, homePath);

            // .........................................
            // 找到 API 对象
            apc.oApi = __find_api_obj(apc);

            // .........................................
            // 如果没有这个 API 文件，就 404 吧
            if (null == apc.oApi) {
                apc.respCode = 404;
                throw Er.create("e.api.nofound", api);
            }

            // .........................................
            // 如果有原来的老会话，则记录一下操作会话
            apc.oldSe = null;
            String ticket = Wn.WC().getTicket();
            if (!Strings.isBlank(ticket)) {
                apc.oldSe = this.auth.getSession(ticket);
            }

            // .........................................
            // 将当前线程切换到指定的用户
            apc.se = __switch_op_user(apc);

            // .........................................
            // 如果 API 文件声明了需要 copy 的 cookie 到线程上下文 ...
            __copy_cookie_to_wc_context(apc);

            // .........................................
            // 如果是跨域的 Preflighted 请求，直接根据设定返回就是
            if ("OPTIONS".equals(req.getMethod())) {
                __do_http_options(req, resp, apc);

                // 无论如何，都不需要继续创建请求对象之类的了
                return;
            }

            // .........................................
            // 填充请求元数据
            __fill_req_meta(apc);

            // .........................................
            // 对所有的元数据进行逃逸处理
            __escape_req_meta(apc);

            // .........................................
            // 自动登陆站点用户
            __do_www_auth(apc);

            // .........................................
            // 检查权限
            if (null != apc.wwwSe) {
                __do_www_check_pvg(apc);
            }

            // .........................................
            // 准备临时目录
            apc.oTmp = __gen_tmp_obj(apc);

            // .........................................
            // 生成请求对象
            apc.oReq = __gen_req_obj(apc);

            // .........................................
            // 执行 API 文件
            try {
                // 带钩子方式的运行
                if (apc.oApi.getBoolean("run-with-hook")) {
                    _do_api_with_hook(apc);
                }
                // 不带钩子
                else {
                    _do_api(apc);
                }
            }
            // 确保退出登录
            finally {
                auth.removeSession(apc.se, 0);
                apc.wc.setSession(null);
            }
        }
        catch (Exception e) {
            // .........................................
            // 根据类型，设置 HTTP 错误码
            if (e instanceof WebException) {
                String ek = ((WebException) e).getKey();
                // 有东西木有找到
                if ("e.io.obj.noexists".equals(ek)) {
                    apc.respCode = 404;
                }
                // 没有权限
                else if ("e.api.forbid".equals(ek)) {
                    apc.respCode = 403;
                }
            }
            // .........................................
            // 不是 4xx 的话，默认设置 500，并且打印的
            if (apc.respCode < 500) {
                apc.respCode = 500;
            }
            if (apc.respCode >= 500) {
                if (log.isWarnEnabled()) {
                    log.warn("Fail to handle API", e);
                }
                e.printStackTrace(resp.getWriter());
            }
            // .........................................
            resp.sendError(apc.respCode);
            resp.flushBuffer();
            return;
        }

    }

    private void __do_www_check_pvg(final WnHttpApiContext apc) {
        String phPvg = apc.oApi.getString("pvg-setup");
        String[] assActions = apc.oApi.getArray("pvg-assert", String.class);

        // 没设置不检
        if (Strings.isBlank(phPvg)) {
            return;
        }

        // 没有声明检查项，不捡
        if (null == assActions) {
            return;
        }

        // 木有检查项，那就是全都不通过
        if (assActions.length == 0) {
            throw Er.create("e.api.forbid");
        }

        // 读一下设置
        apc.oPvgSetup = Wn.checkObj(io, apc.se, phPvg);
        NutMap setup = io.readJson(apc.oPvgSetup, NutMap.class);
        apc.bizPvgs = new BizPvgService(setup);

        // 得到当前账户的角色
        String roleName = apc.wwwSe.getMe().getRoleName("user");

        // 开始检查咯
        for (String assA : assActions) {
            String[] actions = Strings.splitIgnoreBlank(assA, "[|]+");
            if (!apc.bizPvgs.canOne(roleName, actions)) {
                throw Er.create("e.api.forbid");
            }
        }

    }

    private WnAuthSession __switch_op_user(final WnHttpApiContext apc) {
        apc.wc = Wn.WC();
        WnAuthSession se = auth.createSession(apc.u, false);
        apc.wc.setSession(se);
        return se;
    }

    private void _do_api_with_hook(final WnHttpApiContext apc) {
        this.runWithHook(apc.se, new Callback<WnAuthSession>() {
            public void invoke(WnAuthSession se) {
                try {
                    _do_api(apc);
                }
                catch (IOException e) {
                    throw Er.wrap(e);
                }
            }
        });
    }

    private void __copy_cookie_to_wc_context(WnHttpApiContext apc) {
        String[] copyCookieNames = apc.oApi.getAs("copy-cookie", String[].class);
        if (null != copyCookieNames && copyCookieNames.length > 0) {
            apc.wc.copyCookieItems(apc.req, copyCookieNames);
        }
    }

    private void __do_http_options(final HttpServletRequest req,
                                   final HttpServletResponse resp,
                                   final WnHttpApiContext apc) {
        NutMap headers = new NutMap();
        headers.put("ACCESS-CONTROL-ALLOW-ORIGIN",
                    apc.oApi.get("http-header-ACCESS-CONTROL-ALLOW-ORIGIN"));
        headers.put("ACCESS-CONTROL-ALLOW-METHODS",
                    apc.oApi.get("http-header-ACCESS-CONTROL-ALLOW-METHODS"));
        headers.put("ACCESS-CONTROL-ALLOW-HEADERS",
                    apc.oApi.get("http-header-ACCESS-CONTROL-ALLOW-HEADERS"));
        headers.put("ACCESS-CONTROL-ALLOW-CREDENTIALS",
                    apc.oApi.get("http-header-ACCESS-CONTROL-ALLOW-CREDENTIALS"));

        // 设置默认值
        String origin = req.getHeader("Origin");
        __set_cross_origin_default_headers(apc.oApi, origin, (name, value) -> {
            headers.putDefault(name, value);
        });

        // 拒绝跨域
        if (headers.has("ACCESS-CONTROL-ALLOW-ORIGIN")) {
            // 返回跨域设置
            for (String key : headers.keySet()) {
                String val = headers.getString(key);
                resp.setHeader(WnWeb.niceHeaderName(key), val);
            }
        }
    }

    private WnObj __gen_tmp_obj(final WnHttpApiContext apc) {
        return Wn.WC().su(apc.u, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.createIfNoExists(apc.oHome, ".regapi/tmp", WnRace.DIR);
            }
        });
    }

    private void __set_cross_origin_default_headers(WnObj oApi,
                                                    String origin,
                                                    Callback2<String, String> callback) {
        String allowOrigin = oApi.getString("http-cross-origin");
        if (!Strings.isBlank(allowOrigin)) {
            callback.invoke("ACCESS-CONTROL-ALLOW-ORIGIN", Strings.sBlank(origin, allowOrigin));
            callback.invoke("ACCESS-CONTROL-ALLOW-METHODS",
                            "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            callback.invoke("ACCESS-CONTROL-ALLOW-HEADERS",
                            "Origin, Content-Type, Accept, X-Requested-With");
            callback.invoke("ACCESS-CONTROL-ALLOW-CREDENTIALS", "true");
        }
    }

    private WnObj __find_api_obj(WnHttpApiContext apc) {
        // 准备返回值
        WnObj oApi = null;

        // 首先准备路径参数
        apc.args = new LinkedList<>();
        apc.params = new NutMap();

        // 得到 api 的主目录，分解要获取的路径
        apc.oApiHome = io.fetch(apc.oHome, ".regapi/api");
        String[] phs = Strings.splitIgnoreBlank(apc.api, "/");

        // 依次取得
        for (int i = 0; i < phs.length; i++) {
            String ph = phs[i];

            // 直接找一下看看有没有
            oApi = io.fetch(apc.oApiHome, ph);
            if (null != oApi) {
                apc.oApiHome = oApi;
                continue;
            }

            // 嗯，那就找 _ANY 咯
            oApi = io.fetch(apc.oApiHome, "_ANY");

            // 没有的话就是 null 咯
            if (null == oApi)
                return null;

            apc.oApiHome = oApi;

            // 目录的话，继续
            if (oApi.isDIR()) {
                apc.args.add(ph);
                String pnm = oApi.getString("api-param-name");
                if (!Strings.isBlank(pnm)) {
                    apc.params.put(pnm, ph);
                }
            }
            // 文件，表示的是 * 嘛，那就不要继续了
            else {
                String arg = Strings.join(i, phs.length - i, "/", phs);
                apc.args.add(arg);
                String pnm = oApi.getString("api-param-name");
                if (!Strings.isBlank(pnm)) {
                    apc.params.put(pnm, arg);
                }
                break;
            }
        }

        // 搞定，收工
        return oApi;
    }

    private void _do_api(WnHttpApiContext apc) throws IOException {

        // 记录是否客户端设定了响应的 ContentType
        apc.mimeType = apc.oReq.getString("http-qs-mime");

        // 解析命令
        String cmdPattern;
        // 如果 oApi 是个路径参数
        if (apc.oApi.isDIR() && apc.oApi.name().equals("_ANY")) {
            WnObj oAA = io.check(apc.oApi, "_action");
            cmdPattern = io.readText(oAA);
        }
        // 否则直接使用
        else {
            cmdPattern = io.readText(apc.oApi);
        }
        apc.cmdText = Tmpl.exec(cmdPattern, apc.oReq);

        // 如果是 API 的执行是自动决定的文本
        if (apc.oApi.getBoolean("http-dynamic-header")) {
            this.__setup_resp_header(apc);

            HttpApiDynamicRender render = new HttpApiDynamicRender(apc.resp);
            this.exec("box",
                      apc.se,
                      apc.cmdText,
                      render.getStdout(),
                      render.getStderr(),
                      null,
                      null);
            render.close();
            return;
        }

        // 根据返回码决定怎么处理
        apc.respCode = apc.oApi.getInt("http-resp-code", 200);

        // 重定向
        if (apc.respCode == 301 || apc.respCode == 302) {
            _do_redirect(apc);
        }
        // 肯定要写入返回流
        else {
            _do_run_box(apc);
        }
    }

    private WnObj __gen_req_obj(WnHttpApiContext apc)
            throws UnsupportedEncodingException, IOException {

        // .........................................
        // 创建临时文件以便保存请求的内容
        String uriName = apc.uri.replaceAll("[/\\\\]", "_").substring(1);
        WnObj oReq = Wn.WC().su(apc.u, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.create(apc.oTmp, uriName + "_${id}", WnRace.FILE);
            }
        });

        // .........................................
        // 更新头信息
        io.appendMeta(oReq, apc.reqMeta);

        // .........................................
        // 保存请求体
        InputStream ins = apc.req.getInputStream();
        try (OutputStream ops = io.getOutputStream(oReq, 0)) {
            Streams.write(ops, ins);
        }

        // .........................................
        // 搞定
        return oReq;
    }

    private void __escape_req_meta(WnHttpApiContext apc) {
        if (apc.oApi.getBoolean("http-safe-params", true)) {
            for (Map.Entry<String, Object> en : apc.oApi.entrySet()) {
                Object val = en.getValue();
                if (val instanceof CharSequence) {
                    String str = val.toString();
                    String s2 = WnStr.safeTrim(str, "\r\n;", "`'");
                    en.setValue(s2);
                }
            }
        }
    }

    private void __fill_req_meta(WnHttpApiContext apc) throws UnsupportedEncodingException {
        // .........................................
        // 准备请求对象元数据
        Enumeration<String> hnms = apc.req.getHeaderNames();

        // 保存 http 参数
        apc.reqMeta.put("http-usr", apc.u.getName());
        apc.reqMeta.put("http-grp", apc.u.getGroupName());
        apc.reqMeta.put("http-home", apc.u.getHomePath());
        apc.reqMeta.put("http-api", apc.oApi.name());

        // .........................................
        // 记录老的Session对象信息
        if (null != apc.oldSe) {
            apc.reqMeta.put("http-se-id", apc.oldSe.getId());
            apc.reqMeta.put("http-se-ticket", apc.oldSe.getTicket());
            apc.reqMeta.put("http-se-me-name", apc.oldSe.getMe().getName());
            apc.reqMeta.put("http-se-me-group", apc.oldSe.getMe().getGroupName());
            apc.reqMeta.put("http-se-vars", apc.oldSe.getVars());
        }

        // .........................................
        // 记录请求信息的其他数据
        apc.uri = apc.req.getRequestURI();
        apc.reqMeta.put("http-protocol", apc.req.getProtocol().toUpperCase());
        apc.reqMeta.put("http-method", apc.req.getMethod().toUpperCase());
        apc.reqMeta.put("http-uri", apc.uri);
        apc.reqMeta.put("http-url", apc.req.getRequestURL());
        apc.reqMeta.put("http-remote-addr", apc.req.getRemoteAddr());
        apc.reqMeta.put("http-remote-host", apc.req.getRemoteHost());
        apc.reqMeta.put("http-remote-port", apc.req.getRemotePort());

        // .........................................
        // 更新路径参数
        apc.reqMeta.put("args", apc.args);
        apc.reqMeta.put("params", apc.params);

        // .........................................
        // 将请求的对象设置一下清除标志（默认缓存 1 分钟)
        long dftDu = this.conf.getLong("http-api-tmp-duration", 60000L);
        long tmpDu = apc.oApi.getLong("http-tmp-duraion", dftDu);
        apc.reqMeta.put("expi", System.currentTimeMillis() + tmpDu);

        // .........................................
        // 保存 QueryString，同时，看看有没必要更改 mime-type
        String qs = apc.req.getQueryString();
        apc.reqMeta.put("http-qs", qs);
        if (!Strings.isBlank(qs)) {
            // 解码
            qs = URLDecoder.decode(qs, "UTF-8");
            // 分析每个请求参数
            String[] ss = Strings.splitIgnoreBlank(qs, "[&]");
            for (String s : ss) {
                int pos = s.indexOf('=');
                // 有值
                if (pos > 0) {
                    String nm = s.substring(0, pos);
                    String val = s.substring(pos + 1);
                    apc.reqMeta.put("http-qs-" + nm, val);
                }
                // 没值的用空串表示
                else {
                    apc.reqMeta.put("http-qs-" + s, "");
                }
            }
        }

        // .........................................
        // 保存请求头
        while (hnms.hasMoreElements()) {
            String hnm = hnms.nextElement();
            String hval = apc.req.getHeader(hnm);
            apc.reqMeta.put("http-header-" + hnm.toUpperCase(), hval);
        }

        // .........................................
        // 计入请求 Cookie
        Cookie[] coos = apc.req.getCookies();
        if (null != coos && coos.length > 0) {
            for (Cookie coo : coos) {
                apc.reqMeta.put("http-cookie-" + coo.getName(), coo.getValue());
            }
        }
    }

    private void __do_www_auth(WnHttpApiContext apc) {
        String phWWW = apc.oApi.getString("http-www-home");
        boolean hasWWWHome = !Strings.isBlank(phWWW);
        apc.isNeedWWWAuth = apc.oApi.getBoolean("http-www-auth", hasWWWHome);
        apc.wwwSe = null;
        if (hasWWWHome) {
            String ticketBy = apc.oApi.getString("http-www-ticket", "http-qs-ticket");
            String ticket = apc.reqMeta.getString(ticketBy);
            if (!Strings.isBlank(ticket)) {
                WnObj oWWW = Wn.checkObj(io, apc.se, phWWW);
                String homePath = apc.se.getMe().getHomePath();
                WnWebService webs = new WnWebService(io, homePath, oWWW);
                apc.wwwSe = webs.getAuthApi().getSession(ticket);
                if (null != apc.wwwSe) {
                    WnAccount wwwMe = apc.wwwSe.getMe();
                    // 会话信息
                    apc.reqMeta.put("http-www-se-id", apc.wwwSe.getId());
                    apc.reqMeta.put("http-www-se-ticket", apc.wwwSe.getTicket());
                    // 用户信息
                    apc.reqMeta.put("http-www-me-id", wwwMe.getId());
                    apc.reqMeta.put("http-www-me-nm", wwwMe.getName());
                    apc.reqMeta.put("http-www-me-phone", wwwMe.getPhone());
                    apc.reqMeta.put("http-www-me-email", wwwMe.getEmail());
                    apc.reqMeta.put("http-www-me-role", wwwMe.getRoleName());
                    apc.reqMeta.put("http-www-me-nickname", wwwMe.getNickname());
                }
            }
        }
        // 检查一下权限
        if (null == apc.wwwSe && apc.isNeedWWWAuth) {
            throw Er.create("e.api.forbid");
        }
    }

    private void _do_redirect(WnHttpApiContext apc) throws IOException {
        StringBuilder sbOut = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        OutputStream out = Lang.ops(sbOut);
        OutputStream err = Lang.ops(sbErr);

        this.exec("apiR", apc.se, apc.cmdText, out, err, null, null);

        // 处理出错了
        if (sbErr.length() > 0) {
            apc.resp.sendError(500, sbErr.toString());
        }
        // 正常的重定向
        else {
            apc.resp.sendRedirect(sbOut.toString());
        }
    }

    private static final Pattern P = Pattern.compile("^(attachment; *filename=\")(.+)(\")$");

    private void _do_run_box(WnHttpApiContext apc) throws UnsupportedEncodingException {
        // 执行命令
        WnBox box = boxes.alloc(0);

        if (log.isDebugEnabled())
            log.debugf("box:alloc: %s", box.id());

        // 设置沙箱
        WnBoxContext bc = new WnBoxContext(new NutMap());
        bc.io = io;
        bc.session = apc.se;
        bc.auth = auth;

        if (log.isDebugEnabled())
            log.debugf("box:setup: %s", bc);
        box.setup(bc);

        // 根据请求，设置响应的头
        __setup_resp_header(apc);

        // 准备回调
        if (log.isDebugEnabled())
            log.debug("box:set stdin/out/err");

        HttpRespStatusSetter _resp = new HttpRespStatusSetter(apc.resp);
        OutputStream out = new AppRespOutputStreamWrapper(_resp, 200);
        OutputStream err = new AppRespOutputStreamWrapper(_resp, 500);

        box.setStdin(null); // HTTP GET 方式，不支持沙箱的 stdin
        box.setStdout(out);
        box.setStderr(err);

        // 运行
        if (log.isInfoEnabled())
            log.infof("box:run: %s", apc.cmdText);
        box.run(apc.cmdText);

        // 释放沙箱
        if (log.isDebugEnabled())
            log.debugf("box:free: %s", box.id());
        boxes.free(box);

        if (log.isDebugEnabled())
            log.debug("box:done");
    }

    private void __setup_resp_header(WnHttpApiContext apc) {
        // 设置响应头，并看看是否指定了 content-type
        for (String key : apc.oApi.keySet()) {
            if (key.startsWith("http-header-")) {
                String nm = key.substring("http-header-".length()).toUpperCase();
                String val = Strings.trim(apc.oApi.getString(key));
                val = Tmpl.exec(val, apc.oReq);
                // 指定了响应内容
                if (nm.equals("CONTENT-TYPE")) {
                    apc.mimeType = val;
                }
                // 指定了下载目标
                else if (nm.equals("CONTENT-DISPOSITION")) {
                    Matcher m = P.matcher(val);
                    String fnm;
                    if (m.find()) {
                        fnm = m.group(2);
                    } else {
                        fnm = val;
                    }
                    String ua = apc.oReq.getString("http-header-USER-AGENT", "");
                    WnWeb.setHttpRespHeaderContentDisposition(apc.resp, fnm, ua);
                }
                // 其他头，添加
                else {
                    apc.resp.setHeader(WnWeb.niceHeaderName(nm), val);
                }
            }
        }

        // 如果且当前请求是跨域的，则看看是否需要应用默认的跨域设定
        String origin = apc.oReq.getString("http-header-ORIGIN");
        if (!Strings.isBlank(origin)) {
            __set_cross_origin_default_headers(apc.oApi, origin, (name, value) -> {
                if (!apc.oApi.has("http-header-" + name)) {
                    apc.resp.setHeader(WnWeb.niceHeaderName(name), value);
                }
            });
        }

        // 最后设定响应内容
        apc.mimeType = Strings.sBlank(apc.mimeType, "text/html");
        apc.resp.setContentType(apc.mimeType);
    }

}
