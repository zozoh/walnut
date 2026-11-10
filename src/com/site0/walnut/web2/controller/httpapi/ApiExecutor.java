package com.site0.walnut.web2.controller.httpapi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Callback2;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.Wregex;
import com.site0.walnut.log.WnLog;

import com.site0.walnut.api.box.WnBox;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.util.tmpl.WnTmplX;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web2.controller.app.WnAppRespOpsWrapper;
import com.site0.walnut.web2.controller.app.WnHttpRespStatusSetter;

/**
 * RM-NUTZ-09：照抄旧 {@code HttpApiModule} 的执行方法组：
 * {@code _do_api}（463-508，含 http-dynamic-header / 301-302 / run_box 三分支）、
 * {@code _do_api_with_hook}（344-355）、{@code __setup_resp_header}（1023-1066）、
 * {@code __set_cross_origin_default_headers}（397-405）、
 * {@code __do_http_options}（364-391）、{@code _do_redirect}（1068-1084）、
 * {@code _do_run_box}（1086-1157）。
 *
 * <p>沙箱/命令执行能力来自引擎继承的 {@code WnRun}（{@code exec/boxes/newBoxContext/
 * runWithHook}）；{@code WnAppRespOpsWrapper/WnHttpRespStatusSetter} 复用
 * RM-NUTZ-07 返工产出。</p>
 */
public class ApiExecutor {

    private static final WnLog log = Wlog.getCMD();

    private static final Pattern P = Wregex.getPattern("^(attachment; *filename=\")(.+)(\")$");

    private final HttpApiEngine engine;

    public ApiExecutor(HttpApiEngine engine) {
        this.engine = engine;
    }

    public void doApi(WnHttpApiContext apc) throws IOException {

        // 记录是否客户端设定了响应的 ContentType
        apc.mimeType = apc.oReq.getString("http-qs-mime", apc.mimeType);

        // 解析命令
        String cmdPattern;
        // 如果 oApi 是个路径参数
        if (apc.oApi.isDIR() && apc.oApi.name().equals("_ANY")) {
            WnObj oAA = engine.io().check(apc.oApi, "_action");
            cmdPattern = engine.io().readText(oAA);
        }
        // 否则直接使用
        else {
            cmdPattern = engine.io().readText(apc.oApi);
        }
        apc.cmdText = WnTmplX.exec(cmdPattern, apc.oReq);

        // 如果是 API 的执行是自动决定的文本
        if (apc.oApi.getBoolean("http-dynamic-header")) {
            this.setupRespHeader(apc);

            WnHttpApiDynamicRender render = new WnHttpApiDynamicRender(apc.resp);
            engine.exec("box",
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
            this.doRedirect(apc);
        }
        // 肯定要写入返回流
        else {
            this.doRunBox(apc);
        }
    }

    public void doApiWithHook(final WnHttpApiContext apc) {
        engine.runWithHook(apc.se, new Callback<WnSession>() {
            public void invoke(WnSession se) {
                try {
                    doApi(apc);
                }
                catch (IOException e) {
                    throw Er.wrap(e);
                }
            }
        });
    }

    public void setupRespHeader(WnHttpApiContext apc) {
        // 设置响应头，并看看是否指定了 content-type
        for (String key : apc.oApi.keySet()) {
            if (key.startsWith("http-header-")) {
                String nm = key.substring("http-header-".length()).toUpperCase();
                String val = Ws.trim(apc.oApi.getString(key));
                val = WnTmpl.exec(val, apc.oReq);
                // 指定了响应内容
                if (nm.equals("CONTENT-TYPE")) {
                    apc.mimeType = Ws.sBlank(apc.mimeType, val);
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
                    // 标准接口：WnWeb.setHttpRespHeaderContentDisposition 吃的是
                    // javax 响应；等价物 = autoContentDisposition + header 设置
                    apc.resp.header("Content-Disposition",
                                    WnWeb.autoContentDisposition(fnm, ua));
                }
                // 其他头，添加
                else {
                    apc.resp.header(WnWeb.niceHeaderName(nm), val);
                }
            }
        }

        // 如果且当前请求是跨域的，则看看是否需要应用默认的跨域设定
        String origin = apc.reqMeta.getString("http-header-ORIGIN");
        if (!Ws.isBlank(origin)) {
            setCrossOriginDefaultHeaders(apc.oApi, origin, (name, value) -> {
                if (!apc.oApi.has("http-header-" + name)) {
                    apc.resp.header(WnWeb.niceHeaderName(name), value);
                }
            });
        }

        // 最后设定响应内容
        apc.mimeType = Ws.sBlank(apc.mimeType, "text/html");
        apc.resp.contentType(apc.mimeType);
    }

    public void doHttpOptions(WnHttpApiContext apc) {
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
        String origin = apc.req.getHeader("Origin");
        setCrossOriginDefaultHeaders(apc.oApi, origin, (name, value) -> {
            headers.putDefault(name, value);
        });

        // 拒绝跨域
        if (headers.has("ACCESS-CONTROL-ALLOW-ORIGIN")) {
            // 返回跨域设置
            for (String key : headers.keySet()) {
                String val = headers.getString(key);
                apc.resp.header(WnWeb.niceHeaderName(key), val);
            }
        }
    }

    static void setCrossOriginDefaultHeaders(WnObj oApi,
                                             String origin,
                                             Callback2<String, String> callback) {
        String allowOrigin = oApi.getString("http-cross-origin");
        if (!Ws.isBlank(allowOrigin)) {
            origin = Ws.sBlank(origin, allowOrigin);
            WnWeb.setCrossDomainHeaders(origin, callback);
        }
    }

    void doRedirect(WnHttpApiContext apc) throws IOException {
        StringBuilder sbOut = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        OutputStream out = Wlang.ops(sbOut);
        OutputStream err = Wlang.ops(sbErr);

        engine.exec("apiR", apc.se, apc.cmdText, out, err, null, null);

        // 处理出错了
        if (sbErr.length() > 0) {
            apc.resp.sendError(500, sbErr.toString());
        }
        // 正常的重定向
        else {
            apc.resp.sendRedirect(sbOut.toString());
        }
    }

    void doRunBox(WnHttpApiContext apc) throws UnsupportedEncodingException {
        // 执行命令
        WnBox box = engine.boxes().alloc(0);

        if (log.isDebugEnabled())
            log.debugf("box:alloc: %s", box.id());

        // 设置沙箱
        WnBoxContext bc = engine.newBoxContext(apc.se);

        if (log.isDebugEnabled())
            log.debugf("box:setup: %s", bc);
        box.setup(bc);

        // 根据请求，设置响应的头
        this.setupRespHeader(apc);

        // 准备回调
        if (log.isDebugEnabled())
            log.debug("box:set stdin/out/err");

        int dftBufSize = apc.oApi.getInt("http-resp-buff-size", -1);
        if (dftBufSize > 0) {
            apc.resp.setBufferSize(dftBufSize);
        }

        WnHttpRespStatusSetter _resp = new WnHttpRespStatusSetter(apc.resp);
        WnAppRespOpsWrapper out = new WnAppRespOpsWrapper(_resp, 200);
        WnAppRespOpsWrapper err = new WnAppRespOpsWrapper(_resp, 500);

        // 增加一个输出缓冲，这样就能知道最终输出的是什么内容，以便 History 机制记录
        String out_tp = apc.oApi.getString("http-output");
        StringBuilder out_sb = new StringBuilder();
        if (null != out_tp && out_tp.matches("^(text|json)$")) {
            out.addStringWatcher(out_sb);
        }

        // 如果有缓存对象，也计入观察者流
        if (null != apc.cacheObj || null != apc.cacheObjPath) {
            OutputStream cacheOps = new WnHttpApiCacheOutputStream(apc, engine.io());
            out.addWatcher(cacheOps);
        }

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
        engine.boxes().free(box);

        if (log.isDebugEnabled())
            log.debug("box:done");

        // 记入返回
        if (out_sb.length() > 0) {
            if ("text".equals(out_tp)) {
                apc.oReq.put("output", out_sb.toString());
            }
            // 解析一下
            else if ("json".equals(out_tp)) {
                Object json = Json.fromJson(out_sb);
                apc.oReq.put("output", json);
            }
        }
    }

}
