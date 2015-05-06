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
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.Wn;

@IocBean
@At("/api")
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
        // 找到用户和对应的命令
        try {
            if (log.isInfoEnabled())
                log.infof("httpAPI(%s): /%s/%s", req.getRemoteAddr(), usr, api);

            u = usrs.check(usr);
            String homePath = Strings.sBlank(u.home(), "/home/" + u.name());
            oHome = io.check(null, homePath);
            oApi = io.check(oHome, ".regapi/api/" + api);

            // 确保是文本文件
            String mime = oApi.mime();
            if (!mime.startsWith("text")) {
                throw Er.create("e.api.notext", oApi);
            }

            // 将当前线程切换到指定的用户
            Wn.WC().me(u.name(), u.group());
            try {
                _do_api(req, resp, oHome, u, oApi);
            }
            catch (Exception e) {
                resp.setStatus(500);
                try {
                    e.printStackTrace(resp.getWriter());
                    resp.flushBuffer();
                }
                catch (IOException e1) {
                    throw Lang.wrapThrow(e1);
                }
            }
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail to handle API", e);
            }
            resp.setStatus(404);
            e.printStackTrace(resp.getWriter());
            resp.flushBuffer();
            return;
        }

    }

    @SuppressWarnings("unchecked")
    private void _do_api(HttpServletRequest req,
                         HttpServletResponse resp,
                         WnObj oHome,
                         WnUsr u,
                         WnObj oApi) throws UnsupportedEncodingException, IOException {

        WnObj oTmp = io.createIfNoExists(oHome, ".regapi/tmp", WnRace.DIR);

        // 创建临时文件以便保存请求的内容
        WnObj oReq = io.create(oTmp, "${id}", WnRace.FILE);
        Enumeration<String> hnms = req.getHeaderNames();
        NutMap map = new NutMap();

        // 保存 http 参数
        map.setv("http-usr", u.name()).setv("http-api", oApi.name());

        map.setv("http-protocol", req.getProtocol().toLowerCase());
        map.setv("http-method", req.getMethod().toUpperCase());

        // 保存 QueryString
        String qs = req.getQueryString();
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
        String cmd = Segments.replace(cmdPattern, c);

        // 执行命令
        WnSession se = sess.create(u);
        WnBox box = null;
        int httpStatus = 200;
        try {
            box = boxes.alloc(0);
            // 暂时粗暴的搞一下多行命令吧
            // TODO 需要考虑 \ 的连接行，以及分号
            String[] ss = Strings.splitIgnoreBlank(cmd, "\n");
            for (String s : ss) {
                box.submit(s);
            }
            
            // TODO 这里设定沙箱的运行参数
            

            // 最后将请求的对象设置一下清除标志
            oReq.expireTime(System.currentTimeMillis() + 100L * 1000);
            io.appendMeta(oReq, "^expi$");
        }
        // 出现错误
        catch (Exception e) {
            httpStatus = 500;
            oReq.setv("http_error", e.toString());
            io.appendMeta(oReq, "^http_error$");
        }
        // 清除会话的进程，并写入返回
        finally {
            boxes.free(box);
        }

        // TODO 这里不要了 !!! 写回返回
        resp.setStatus(httpStatus);
        resp.setContentType(oApi.getString("http-header-CONTENT-TYPE", "text/plain"));
        resp.setCharacterEncoding("UTF-8");
        resp.flushBuffer();
    }

}
