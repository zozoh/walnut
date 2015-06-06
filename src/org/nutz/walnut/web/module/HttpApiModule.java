package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.meta.Pair;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.web.filter.WnAsUsr;

@IocBean
@At("/api")
@Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
public class HttpApiModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    @At("/?/**")
    @Ok("void")
    @Fail("void")
    public void handle(String usr,
                       String api,
                       final HttpServletRequest req,
                       final HttpServletResponse resp) throws IOException {
        final WnUsr u;
        final WnObj oHome;
        final WnObj oApi;
        int respCode = 500;
        // 找到用户和对应的命令
        try {
            if (log.isInfoEnabled())
                log.infof("httpAPI(%s): /%s/%s", req.getRemoteAddr(), usr, api);

            u = usrs.check(usr);
            String homePath = Strings.sBlank(u.home(), "/home/" + u.name());
            oHome = io.check(null, homePath);
            WnObj oApiHome = Wn.WC().su(u, new Proton<WnObj>() {
                protected WnObj exec() {
                    return io.createIfNoExists(oHome, ".regapi/api", WnRace.DIR);
                }
            });
            WnObj o = io.fetch(oApiHome, api);
            // 逐级寻找 default
            if (null == o) {
                WnObj p = oApiHome;
                String[] ss = Strings.splitIgnoreBlank(api, "/");
                int i = 0;
                while (p != null) {
                    WnObj oDft = io.fetch(p, "_default");
                    if (null != oDft)
                        o = oDft;
                    p = io.fetch(p, ss, i, i + 1);
                }
            }
            if (null == o) {
                respCode = 404;
                throw Er.create("e.api.nofound", api);
            }
            oApi = o;

            // 确保是文本文件
            String mime = oApi.mime();
            if (!mime.startsWith("text")) {
                throw Er.create("e.api.notext", oApi);
            }

            // 将当前线程切换到指定的用户
            WnContext wc = Wn.WC();
            wc.me("root", "root");
            WnSession se = sess.create(u);
            wc.SE(se);
            try {
                String mimeType = Strings.trim(req.getQueryString());
                _do_api(req, resp, mimeType, oHome, u, oApi);
            }
            // 确保退出登录
            finally {
                sess.logout(se.id());
                wc.SE(null);
            }
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail to handle API", e);
            }
            resp.setStatus(respCode);
            e.printStackTrace(resp.getWriter());
            resp.flushBuffer();
            return;
        }

    }

    private void _do_api(HttpServletRequest req,
                         HttpServletResponse resp,
                         String mimeType,
                         final WnObj oHome,
                         WnUsr u,
                         WnObj oApi) throws UnsupportedEncodingException, IOException {

        // 默认返回的 mime-type 是文本
        if (Strings.isBlank(mimeType))
            mimeType = "text/plain";
        resp.setContentType(mimeType);

        // 读取输入
        final WnObj oTmp = Wn.WC().su(u, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.createIfNoExists(oHome, ".regapi/tmp", WnRace.DIR);
            }
        });

        // 创建临时文件以便保存请求的内容
        WnObj oReq = Wn.WC().su(u, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.create(oTmp, "${id}", WnRace.FILE);
            }
        });
        Enumeration<String> hnms = req.getHeaderNames();
        NutMap map = new NutMap();

        // 保存 http 参数
        map.setv("http-usr", u.name()).setv("http-api", oApi.name());

        map.setv("http-protocol", req.getProtocol().toLowerCase());
        map.setv("http-method", req.getMethod().toUpperCase());
        map.setv("http-uri", req.getRequestURI());
        map.setv("http-url", req.getRequestURL());

        // 保存 QueryString
        String qs = req.getQueryString();
        map.setv("http-qs", qs);
        if (!Strings.isBlank(qs)) {
            qs = URLDecoder.decode(qs, "UTF-8");
            String[] ss = Strings.splitIgnoreBlank(qs, "[&]");
            for (String s : ss) {
                Pair<String> pair = Pair.create(s);
                map.setv("http-qs-" + pair.getName(),
                         pair.getValue() == null ? true : pair.getValue());
            }
        }

        // 保存请求头
        while (hnms.hasMoreElements()) {
            String hnm = hnms.nextElement();
            String hval = req.getHeader(hnm);
            map.setv("http-header-" + hnm.toUpperCase(), hval);
        }

        // 更新头信息
        io.appendMeta(oReq, map);

        // 保存请求体
        InputStream ins = req.getInputStream();
        OutputStream ops = io.getOutputStream(oReq, 0);
        Streams.writeAndClose(ops, ins);

        // 解析命令
        String cmdPattern = io.readText(oApi);
        Context c = Lang.context(oReq.toMap(null));
        String cmdText = Segments.replace(cmdPattern, c);

        // 执行命令
        WnBox box = boxes.alloc(0);

        if (log.isDebugEnabled())
            log.debugf("box:alloc: %s", box.id());

        // 设置沙箱
        WnContext wc = Wn.WC();
        WnBoxContext bc = new WnBoxContext();
        bc.io = io;
        bc.me = usrs.check(wc.checkMe());
        bc.session = wc.checkSE();
        bc.usrService = usrs;
        bc.sessionService = sess;

        if (log.isDebugEnabled())
            log.debugf("box:setup: %s", bc);
        box.setup(bc);

        // 准备回调
        if (log.isDebugEnabled())
            log.debug("box:set stdin/out/err");

        OutputStream out = new AppRespOutputStreamWrapper(resp, 200);
        OutputStream err = new AppRespOutputStreamWrapper(resp, 500);

        box.setStdin(null); // HTTP GET 方式，不支持沙箱的 stdin
        box.setStdout(out);
        box.setStderr(err);

        // 运行
        if (log.isDebugEnabled())
            log.debugf("box:run: %s", cmdText);
        box.run(cmdText);

        // 释放沙箱
        if (log.isDebugEnabled())
            log.debugf("box:free: %s", box.id());
        boxes.free(box);

        if (log.isDebugEnabled())
            log.debug("box:done");

        // 最后将请求的对象设置一下清除标志
        oReq.expireTime(System.currentTimeMillis() + 100L * 1000);
        io.appendMeta(oReq, "^expi$");

    }

}
