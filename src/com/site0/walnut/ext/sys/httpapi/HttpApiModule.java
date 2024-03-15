package com.site0.walnut.ext.sys.httpapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.stream.ComboOutputStream;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Callback2;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.box.WnBox;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.entity.history.HistoryApi;
import com.site0.walnut.ext.data.entity.history.HistoryConfig;
import com.site0.walnut.ext.data.entity.history.HistoryRecord;
import com.site0.walnut.ext.data.entity.history.WnHistoryService;
import com.site0.walnut.ext.data.pvg.BizPvgService;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.ext.sys.sql.WnDaos;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnStr;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;
import com.site0.walnut.web.filter.WnAsUsr;
import com.site0.walnut.web.module.AbstractWnModule;
import com.site0.walnut.web.module.AppRespOpsWrapper;
import com.site0.walnut.web.module.HttpRespStatusSetter;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.util.Wsum;
import org.nutz.web.WebException;

@IocBean
@At("/api")
@Filters(@By(type = WnAsUsr.class, args = {"root"}))
public class HttpApiModule extends AbstractWnModule {

    private static final Log log = Wlog.getCMD();

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
        apc.reqQueryMap = new NutMap();
        // 找到用户和对应的命令
        try {
            if (log.isInfoEnabled())
                log.infof("httpAPI(%s): /%s/%s", Wlang.getIP(req), usr, api);

            // .........................................
            // 得到用户和主目录
            apc.u = auth().checkAccount(usr);
            String homePath = apc.u.getHomePath();
            apc.oHome = io().check(null, homePath);

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
            if (!Ws.isBlank(ticket)) {
                apc.oldSe = this.auth().getSession(ticket);
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
            // 验证 Access token
            __check_api_access_token(apc);

            // .........................................
            // 对所有的元数据进行逃逸处理
            __escape_req_meta(apc);

            // .........................................
            // 自动登陆站点用户
            __do_www_auth(apc);

            // .........................................
            // 预先加载数据
            __do_preload(apc);

            // .........................................
            // 这里处理缓存
            if (__try_cache(apc)) {
                return;
            }

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

                // 执行历史记录
                _record_history(apc);
            }
            // 确保退出登录
            finally {
                // 将请求的对象设置一下清除标志（默认缓存 1 分钟)
                if (null != apc.oReq) {
                    long dftDu = this.conf.getLong("http-api-tmp-duration", 60000L);
                    // 如果有站点的话，则看看站点的默认会话设定
                    if (null != apc.oWWW) {
                        int duInS = apc.oWWW.getInt("api_req_du", -1);
                        if (duInS >= 0) {
                            dftDu = duInS * 1000L;
                        }
                    }
                    long tmpDu = apc.oApi.getLong("http-tmp-duraion", dftDu);
                    // 如果时间短于 1 秒，就直接删除了
                    if (tmpDu < 1000) {
                        io().delete(apc.oReq);
                    }
                    // 否则，让清理进程删除
                    else {
                        long expi = Wn.now() + tmpDu;
                        apc.oReq.expireTime(expi);
                        io().set(apc.oReq, "^(expi)$");
                    }
                }

                // 注销会话
                auth().removeSession(apc.se, 0);
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
            if (apc.respCode < 400) {
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

    private void __check_api_access_token(final WnHttpApiContext apc) {
        String at = apc.oApi.getString("api-access-token");
        if (!Ws.isBlank(at)) {
            // 分析 Key 和 Path
            String atKey, atPath;
            int pos = at.indexOf('=');
            if (pos > 0) {
                atKey = at.substring(0, pos).trim();
                atPath = at.substring(pos + 1).trim();
            } else {
                atKey = at.trim();
                atPath = "~/.domain/api_access_token";
            }
            // 读取 Key
            String atClientKey = apc.reqMeta.getString(atKey);
            if (Ws.isBlank(atClientKey)) {
                throw Er.create("e.api.forbid");
            }
            // 读取验证 key
            String phDmnKey = Wn.normalizeFullPath(atPath, apc.se);
            WnObj oDmnKey = io().fetch(null, phDmnKey);
            if (null == oDmnKey) {
                throw Er.create("e.api.forbid");
            }
            String atDmnKey = io().readText(oDmnKey).trim();
            if (!atClientKey.equals(atDmnKey)) {
                throw Er.create("e.api.forbid");
            }
        }
    }

    private void __do_www_check_pvg(final WnHttpApiContext apc) {
        String phPvg = apc.oApi.getString("pvg-setup");
        // 如果木有设置，那么看看站点有没有设置
        if (Ws.isBlank(phPvg)) {
            phPvg = apc.oWWW.getString("pvg_setup");
        }
        String[] assActions = apc.oApi.getArray("pvg-assert", String.class);

        // 没设置不检
        if (Ws.isBlank(phPvg)) {
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
        apc.oPvgSetup = Wn.checkObj(io(), apc.se, phPvg);
        NutMap setup = io().readJson(apc.oPvgSetup, NutMap.class);
        apc.bizPvgs = new BizPvgService(setup);

        // 得到当前账户的角色
        String roleName = apc.wwwSe.getMe().getRoleName("user");

        // 开始检查咯
        for (String assA : assActions) {
            String[] actions = Ws.splitIgnoreBlank(assA, "[|]+");
            if (!apc.bizPvgs.canOne(roleName, actions)) {
                throw Er.create("e.api.forbid");
            }
        }

    }

    private WnAuthSession __switch_op_user(final WnHttpApiContext apc) {
        apc.wc = Wn.WC();
        WnAuthSession se = auth().createSession(apc.u, false);
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
        return io().createIfNoExists(apc.oHome, ".regapi/tmp", WnRace.DIR);
    }

    private void __set_cross_origin_default_headers(WnObj oApi,
                                                    String origin,
                                                    Callback2<String, String> callback) {
        String allowOrigin = oApi.getString("http-cross-origin");
        if (!Ws.isBlank(allowOrigin)) {
            origin = Ws.sBlank(origin, allowOrigin);
            WnWeb.setCrossDomainHeaders(origin, callback);
        }
    }

    private WnObj __find_api_obj(WnHttpApiContext apc) {
        // 准备返回值
        WnObj oApi = null;

        // 首先准备路径参数
        apc.args = new LinkedList<>();
        apc.params = new NutMap();

        // 得到 api 的主目录，分解要获取的路径
        apc.oApiHome = io().fetch(apc.oHome, ".regapi/api");
        String[] phs = Ws.splitIgnoreBlank(apc.api, "/");

        // 依次取得
        for (int i = 0; i < phs.length; i++) {
            String ph = phs[i];

            // 直接找一下看看有没有
            oApi = io().fetch(apc.oApiHome, ph);
            if (null != oApi) {
                apc.oApiHome = oApi;
                continue;
            }

            // 嗯，那就找 _ANY 咯
            oApi = io().fetch(apc.oApiHome, "_ANY");

            // 没有的话就是 null 咯
            if (null == oApi)
                return null;

            apc.oApiHome = oApi;

            // 目录的话，继续
            if (oApi.isDIR()) {
                apc.args.add(ph);
                String pnm = oApi.getString("api-param-name");
                if (!Ws.isBlank(pnm)) {
                    apc.params.put(pnm, ph);
                }
            }
            // 文件，表示的是 * 嘛，那就不要继续了
            else {
                String arg = Ws.join(phs, "/", i, phs.length - i);
                apc.args.add(arg);
                String pnm = oApi.getString("api-param-name");
                if (!Ws.isBlank(pnm)) {
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
        apc.mimeType = apc.oReq.getString("http-qs-mime", apc.mimeType);

        // 解析命令
        String cmdPattern;
        // 如果 oApi 是个路径参数
        if (apc.oApi.isDIR() && apc.oApi.name().equals("_ANY")) {
            WnObj oAA = io().check(apc.oApi, "_action");
            cmdPattern = io().readText(oAA);
        }
        // 否则直接使用
        else {
            cmdPattern = io().readText(apc.oApi);
        }
        apc.cmdText = WnTmpl.exec(cmdPattern, apc.oReq);

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

    @SuppressWarnings("unchecked")
    private WnObj __gen_req_obj(WnHttpApiContext apc)
            throws UnsupportedEncodingException, IOException {

        // .........................................
        // 创建临时文件以便保存请求的内容
        String uriName = apc.uri.replaceAll("[/\\\\]", "_").substring(1);
        WnObj oReq = io().create(apc.oTmp, uriName + "_${id}", WnRace.FILE);

        // .........................................
        // 更新头信息
        io().appendMeta(oReq, apc.reqMeta);

        // 增加一个输入缓冲
        String in_tp = apc.oApi.getString("http-body");
        StringBuilder in_sb = new StringBuilder();
        OutputStream in_ops = null;
        if (null != in_tp && in_tp.matches("^(text|form|json)$")) {
            in_ops = Wlang.ops(in_sb);
        }

        // .........................................
        // 看看是否声明了有效的 http-post-json-merge
        NutMap jsonUpdate = null;
        boolean needUpdateBody = false;
        if ("json".equals(in_tp)) {
            NutMap juMap = this.__find_post_json_merge_update(apc);
            if (null != juMap) {
                jsonUpdate = (NutMap) Wn.explainObj(apc.reqMeta, juMap);
                needUpdateBody = (null != jsonUpdate && !jsonUpdate.isEmpty());
            }
        }

        // .........................................
        // 保存请求体
        InputStream ins = apc.req.getInputStream();
        OutputStream ops;

        // 嗯？ 需要动态更新一下内容，那么就先读取吧
        if (needUpdateBody) {
            ops = in_ops;
        }
        // 需要同时将请求内容保存到元数据里，那么先复制一份把
        else if (null != in_ops) {
            OutputStream ioOps = io().getOutputStream(oReq, 0);
            ops = new ComboOutputStream(ioOps, in_ops);
        }
        // 直接写咯
        else {
            ops = io().getOutputStream(oReq, 0);
        }

        // 写入流
        try {
            Streams.write(ops, ins);
            Streams.safeFlush(ops);
        }
        finally {
            Streams.safeClose(ops);
        }

        // .........................................
        // 计入元数据！注意！这个 http-body 是不持久化的，否则有可能太大
        if (in_sb.length() > 0) {
            // 纯文本
            if ("text".equals(in_tp)) {
                oReq.put("body", in_sb.toString());
            }
            // 表单
            else if ("form".equals(in_tp)) {
                NutMap map = WnStr.parseFormData(in_sb.toString());
                oReq.put("body", map);
            }
            // JSON
            else if ("json".equals(in_tp)) {
                Object body = Json.fromJson(in_sb);
                // 更新一下 json，进入了这个分支，意味着请求对象的 body并未被写入
                // 因为内容需要被 update， 等合并完，需要补写一下。
                if (needUpdateBody) {
                    // 安全起见，只有 Map 才能融合 Map
                    if (body instanceof Map) {
                        NutMap map = NutMap.WRAP((Map<String, Object>) body);
                        map.mergeWith(jsonUpdate);
                        body = map;
                    }
                    // 写入
                    String json = Json.toJson(body);
                    io().writeText(oReq, json);
                }
                oReq.put("body", body);
            }
        }
        // .........................................
        // 搞定
        return oReq;
    }

    private NutMap __find_post_json_merge_update(WnHttpApiContext apc) {
        List<NutMap> list = apc.oApi.getAsList("http-post-json-merge", NutMap.class);
        if (null == list || list.isEmpty()) {
            return null;
        }
        // 逐个判断
        for (NutMap jm : list) {
            Object test = jm.get("test");
            // 需要判断
            if (null != test) {
                WnMatch m = new AutoMatch(test);
                if (!m.match(apc.reqMeta)) {
                    continue;
                }
            }
            // 嗯就是这个条件咯
            NutMap update = jm.getAs("update", NutMap.class);
            if (null == update || update.isEmpty()) {
                return null;
            }
            return update;
        }
        return null;
    }

    private void __escape_req_meta(WnHttpApiContext apc) {
        if (apc.oApi.getBoolean("http-safe-params", true)) {
            for (Map.Entry<String, Object> en : apc.reqMeta.entrySet()) {
                String key = en.getKey();
                // 只搞 http-qs-
                if (!key.startsWith("http-qs-")) {
                    continue;
                }
                // TODO 慎重起见，是不是也要同时防守一下 "http-cookie-" 和 "http-header-" 涅？
                Object val = en.getValue();
                if (val instanceof CharSequence) {
                    String str = val.toString();
                    String s2 = WnStr.safeTrim(str, "\r\n;", "`'");
                    en.setValue(s2);
                }
            }
        }
    }

    private boolean __try_cache(WnHttpApiContext apc) throws IOException {
        // 非GET请求无视
        if (!"GET".equalsIgnoreCase(apc.req.getMethod())) {
            return false;
        }
        // 需要校验auth的请求无视
        if (apc.isNeedWWWAuth) {
            return false;
        }
        // 动态请求头的 API无视
        if (apc.oApi.getBoolean("http-dynamic-header")) {
            return false;
        }

        // 是否启用了缓存
        List<NutMap> cacheTest = apc.oApi.getAsList("cache-test", NutMap.class);
        if (null == cacheTest || cacheTest.isEmpty())
            return false;

        // 是否命中缓存
        String cachePath = null;
        for (NutMap test : cacheTest) {
            String path = test.getString("path");

            // 靠，您忘记写 path了吧，无视你！
            if (Ws.isBlank(path))
                continue;

            Object cacheMatch = test.get("match");
            if (this.__is_cache_match(cacheMatch, apc.reqQueryMap)) {
                cachePath = path;
                apc.mimeType = test.getString("mime");
                break;
            }
        }
        // 找不到缓存路径，那么就意味着不命中缓存咯
        if (Ws.isBlank(cachePath)) {
            return false;
        }

        // 得到缓存对象
        boolean lazy = apc.oApi.getBoolean("cache-lazy", true);
        String ph = WnTmpl.exec(cachePath, apc.reqMeta);
        String aph = Wn.normalizeFullPath(ph, apc.se);
        WnObj oCache = this.io().fetch(null, aph);
        // 木有命中，看看是否需要懒加载
        if (null == oCache) {
            if (lazy) {
                apc.cacheObjPath = aph;
            }
            return false;
        }

        // 有缓存，但是过期了
        int cache_du_in_s = apc.oApi.getInt("cache-duration", 0);
        if (cache_du_in_s > 0) {
            long duInMs = Wn.now() - oCache.lastModified();
            long cache_du_in_ms = cache_du_in_s * 1000;
            if (duInMs > cache_du_in_ms) {
                if (lazy) {
                    apc.cacheObj = oCache;
                }
                return false;
            }
        }

        // 有缓存，但是匹配不上请求签名
        String fingerKey = apc.oApi.getString("cache-finger-key");
        if (!Ws.isBlank(fingerKey)) {
            String fingerAs = apc.oApi.getString("cache-finger-as", "MD5");
            apc.reqQuerySign = Wsum.digestAsString(fingerAs, apc.reqQuery);
            // 得到缓存的签名
            String sign = oCache.getString(fingerKey);
            if (Ws.isBlank(sign) || !sign.equals(apc.reqQuerySign)) {
                if (lazy) {
                    apc.cacheObj = oCache;
                }
                return false;
            }
        }
        // -----------------------------------------
        // 嗯，进行到了这里，那么就是命中缓存了
        // 下面看看如何输出
        // -----------------------------------------
        // 按照 HTTP 302 的方式输出
        String redirect = apc.oApi.getString("cache-redirect");
        if (!Ws.isBlank(redirect)) {
            NutMap vars = new NutMap();
            if (oCache.hasSha1()) {
                String sha1 = oCache.sha1();
                String sha1Path = sha1.substring(0, 4) + "/" + sha1.substring(4);
                vars.put("sha1Path", sha1Path);
            }
            vars.putAll(oCache);
            String r_url = WnTmpl.exec(redirect, vars);

            // 如果且当前请求是跨域的，则看看是否需要应用默认的跨域设定
            String origin = apc.reqMeta.getString("http-header-ORIGIN");
            if (!Ws.isBlank(origin)) {
                __set_cross_origin_default_headers(apc.oApi, origin, (name, value) -> {
                    if (!apc.oApi.has("http-header-" + name)) {
                        apc.resp.setHeader(WnWeb.niceHeaderName(name), value);
                    }
                });
            }

            // 重定向
            apc.resp.sendRedirect(r_url);

        }
        // 直接输出缓存对象
        else {
            // 设置响应头
            this.__setup_resp_header(apc);

            // 写入响应流
            OutputStream ops = apc.resp.getOutputStream();
            InputStream ins = this.io().getInputStream(oCache, 0);
            Streams.writeAndClose(ops, ins);
        }

        // 无论怎样，返回 true 表示不要继续后面的步骤了
        return true;
    }

    private boolean __is_cache_match(Object cacheMatch, NutMap reqQueryMap) {
        if (null != cacheMatch) {
            WnMatch am = new AutoMatch(cacheMatch);
            return am.match(reqQueryMap);
        }
        return true;
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
        // 保存 QueryString，同时，看看有没必要更改 mime-type
        apc.reqQuery = apc.req.getQueryString();
        apc.reqMeta.put("http-qs", apc.reqQuery);
        if (!Ws.isBlank(apc.reqQuery)) {
            // 解码
            apc.reqQuery = URLDecoder.decode(apc.reqQuery, "UTF-8");
            // 分析每个请求参数
            String[] ss = Ws.splitIgnoreBlank(apc.reqQuery, "[&]");
            for (String s : ss) {
                int pos = s.indexOf('=');
                // 有值
                if (pos > 0) {
                    String nm = s.substring(0, pos);
                    String val = s.substring(pos + 1);
                    apc.reqMeta.put("http-qs-" + nm, val);
                    apc.reqQueryMap.put(nm, val);
                }
                // 没值的用空串表示
                else {
                    apc.reqMeta.put("http-qs-" + s, "");
                    apc.reqQueryMap.put(s, true);
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

        // .........................................
        // 请求的属性：只记录简单的可被序列化的属性
        Enumeration<String> attrNames = apc.req.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = attrNames.nextElement();
            Object attrVal = apc.req.getAttribute(attrName);
            if (null == attrVal) {
                continue;
            }
            Mirror<?> mi = Mirror.me(attrVal);
            if (mi.isSimple()) {
                apc.reqMeta.put(attrName, attrVal);
            }
        }
    }

    private void __do_www_auth(WnHttpApiContext apc) {
        String phWWW = apc.oApi.getString("http-www-home");
        phWWW = (String) Wn.explainObj(apc.reqMeta, phWWW);
        boolean hasWWWHome = !Ws.isBlank(phWWW);
        apc.isNeedWWWAuth = apc.oApi.getBoolean("http-www-auth", hasWWWHome);
        apc.wwwSe = null;
        if (hasWWWHome) {
            String ticketBy = apc.oApi.getString("http-www-ticket", "http-qs-ticket");
            String ticket = apc.reqMeta.getString(ticketBy);
            if (!Ws.isBlank(ticket)) {
                apc.oWWW = Wn.checkObj(io(), apc.se, phWWW);
                String homePath = apc.se.getMe().getHomePath();
                apc.webs = new WnWebService(io(), homePath, apc.oWWW);
                apc.wwwSe = apc.webs.getAuthApi().getSession(ticket);
                if (null != apc.wwwSe) {
                    WnAccount wwwMe = apc.wwwSe.getMe();
                    // 会话信息
                    apc.reqMeta.put("http-www-se-id", apc.wwwSe.getId());
                    apc.reqMeta.put("http-www-se-ticket", apc.wwwSe.getTicket());
                    // 用户信息
                    apc.reqMeta.put("http-www-me-id", wwwMe.getId());
                    apc.reqMeta.put("http-www-me-id-home", wwwMe.OID().getHomeId());
                    apc.reqMeta.put("http-www-me-id-my", wwwMe.OID().getMyId());
                    apc.reqMeta.put("http-www-me-nm", wwwMe.getName());
                    apc.reqMeta.put("http-www-me-nickname", wwwMe.getNickname());
                    apc.reqMeta.put("http-www-me-thumb", wwwMe.getThumb());
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

    @SuppressWarnings("unchecked")
    private void __do_preload(WnHttpApiContext apc) {
        Object preloadObj = apc.oApi.get("preload");
        if (null == preloadObj)
            return;

        List<NutMap> preloads = new LinkedList<>();
        if (preloadObj instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) preloadObj;
            for (Object o : coll) {
                if (o instanceof Map<?, ?>) {
                    preloads.add(NutMap.WRAP((Map<String, Object>) o));
                }
            }
        }
        // 单个
        else if (preloadObj instanceof Map<?, ?>) {
            preloads.add(NutMap.WRAP((Map<String, Object>) preloadObj));
        }

        if (preloads.isEmpty())
            return;

        // 寻找到第一个可被处理的预加载命令
        boolean isPreloaded = false;
        for (NutMap plItem : preloads) {
            Object test = plItem.get("test");
            WnMatch am = AutoMatch.parse(test, true);

            // 判断是否符合条件
            if (!am.match(apc.reqMeta)) {
                continue;
            }

            // 置否需要预先转换
            NutMap appends = plItem.getAs("appends", NutMap.class);
            if (null != appends && !appends.isEmpty()) {
                NutMap more = (NutMap) Wn.explainObj(apc.reqMeta, appends);
                if (null != more && !more.isEmpty()) {
                    apc.reqMeta.putAll(more);
                }
            }

            // 执行
            NutMap run = plItem.getAs("run", NutMap.class);
            if (null != run && !run.isEmpty()) {
                for (Map.Entry<String, Object> en : run.entrySet()) {
                    String key = en.getKey();
                    Object cmd = en.getValue();
                    if (null == cmd || "!...".equals(key))
                        continue;

                    // 分析键
                    boolean isPureText = key.startsWith("!");
                    if (isPureText) {
                        key = key.substring(1).trim();
                    }

                    // 执行命令
                    WnTmpl tmpl = WnTmpl.parse(cmd.toString());
                    String cmdText = tmpl.render(apc.reqMeta);
                    try {
                        String re = this.exec("api-preload", apc.se, cmdText);

                        // 解析结果
                        Object reo = re;
                        if (!isPureText) {
                            reo = Json.fromJson(re);
                        }

                        // 计入结果：全部
                        if ("...".equals(key)) {
                            if (reo instanceof Map) {
                                apc.reqMeta.putAll((Map<String, Object>) reo);
                            }
                        }
                        // 计入结果：指定键
                        else {
                            apc.reqMeta.put(key, reo);
                        }
                    }
                    catch (Exception e) {
                        if (log.isWarnEnabled()) {
                            log.warn("Fail to run preload: " + key + " : " + cmd, e);
                        }
                    }
                }

                // 标记一下已经执行了预加载
                isPreloaded = true;
            }

            // 跳出
            break;
        }

        // 如果执行了 preload，则进行标志
        apc.reqMeta.put("api-preloaded", isPreloaded);
    }

    private static final Pattern P = Regex.getPattern("^(attachment; *filename=\")(.+)(\")$");

    private void __setup_resp_header(WnHttpApiContext apc) {
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
                    WnWeb.setHttpRespHeaderContentDisposition(apc.resp, fnm, ua);
                }
                // 其他头，添加
                else {
                    apc.resp.setHeader(WnWeb.niceHeaderName(nm), val);
                }
            }
        }

        // 如果且当前请求是跨域的，则看看是否需要应用默认的跨域设定
        String origin = apc.reqMeta.getString("http-header-ORIGIN");
        if (!Ws.isBlank(origin)) {
            __set_cross_origin_default_headers(apc.oApi, origin, (name, value) -> {
                if (!apc.oApi.has("http-header-" + name)) {
                    apc.resp.setHeader(WnWeb.niceHeaderName(name), value);
                }
            });
        }

        // 最后设定响应内容
        apc.mimeType = Ws.sBlank(apc.mimeType, "text/html");
        apc.resp.setContentType(apc.mimeType);
    }

    void _do_redirect(WnHttpApiContext apc) throws IOException {
        StringBuilder sbOut = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        OutputStream out = Wlang.ops(sbOut);
        OutputStream err = Wlang.ops(sbErr);

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

    void _do_run_box(WnHttpApiContext apc) throws UnsupportedEncodingException {
        // 执行命令
        WnBox box = boxes().alloc(0);

        if (log.isDebugEnabled())
            log.debugf("box:alloc: %s", box.id());

        // 设置沙箱
        WnBoxContext bc = this.createBoxContext(apc.se);

        if (log.isDebugEnabled())
            log.debugf("box:setup: %s", bc);
        box.setup(bc);

        // 根据请求，设置响应的头
        __setup_resp_header(apc);

        // 准备回调
        if (log.isDebugEnabled())
            log.debug("box:set stdin/out/err");

        int dftBufSize = apc.oApi.getInt("http-resp-buff-size", -1);
        if (dftBufSize > 0) {
            apc.resp.setBufferSize(dftBufSize);
        }

        HttpRespStatusSetter _resp = new HttpRespStatusSetter(apc.resp);
        AppRespOpsWrapper out = new AppRespOpsWrapper(_resp, 200);
        AppRespOpsWrapper err = new AppRespOpsWrapper(_resp, 500);

        // 增加一个输出缓冲，这样就能知道最终输出的是什么内容，以便 History 机制记录
        String out_tp = apc.oApi.getString("http-output");
        StringBuilder out_sb = new StringBuilder();
        if (null != out_tp && out_tp.matches("^(text|json)$")) {
            out.addStringWatcher(out_sb);
        }

        // 如果有缓存对象，也计入观察者流
        if (null != apc.cacheObj || null != apc.cacheObjPath) {
            OutputStream cacheOps = new WnHttpCacheOutputStream(apc, this.io());
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
        boxes().free(box);

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

    @SuppressWarnings("unchecked")
    void _record_history(WnHttpApiContext apc) {
        List<NutMap> historyList = null;
        Object history = apc.oApi.get("history");

        // 防守
        if (null == history)
            return;

        // 动态防守
        Object histest = apc.oApi.get("histest");
        if (null != histest) {
            WnMatch wm = new AutoMatch(histest);
            if (!wm.match(apc.oReq)) {
                return;
            }
        }

        // 多个历史记录
        if (history instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) history;
            historyList = new ArrayList<>(coll.size());

            for (Object it : coll) {
                if (null != it && (it instanceof Map)) {
                    NutMap itMap = NutMap.WRAP((Map<String, Object>) it);
                    historyList.add(itMap);
                }
            }
        }
        // 单条历史记录
        else if (history instanceof Map) {
            NutMap itMap = NutMap.WRAP((Map<String, Object>) history);
            historyList = Wlang.list(itMap);
        }
        // 神马也不是
        else {
            return;
        }

        // 没有历史记录模板，嗯，无视
        if (null == historyList || historyList.isEmpty()) {
            return;
        }

        // 首先，获取一下历史记录的配置文件
        String hisname = apc.oApi.getString("hisname", "_history");
        WnObj oHis = Wn.getObj(io(), apc.se, "~/.domain/history/" + hisname + ".json");

        // 木有的话，打印个警告
        if (null == oHis) {
            if (log.isWarnEnabled()) {
                log.warnf("!!! Fail to found history config(%s): %s.json",
                          apc.se.getMyName(),
                          hisname);
            }
            return;
        }

        // 生成服务类
        HistoryConfig conf = WnDaos.loadConfig(HistoryConfig.class, io(), oHis, apc.se);
        Dao dao = WnDaos.get(conf.getAuth());
        HistoryApi api = new WnHistoryService(conf, dao);

        // 看看有木有替换的模板
        List<NutMap> hismetas = apc.oApi.getAsList("hismetas", NutMap.class);
        NutMap hisUpdate = null;
        if (null != hismetas) {
            for (NutMap hismeta : hismetas) {
                Object test = hismeta.get("test");
                AutoMatch am = new AutoMatch(test);
                NutMap update = hismeta.getAs("update", NutMap.class);
                // 找到了即可
                if (am.match(apc.oReq)) {
                    hisUpdate = update;
                    break;
                }
            }
        }

        // 循环添加历史记录
        for (NutMap hisTmpl : historyList) {
            // 补充历史记录
            if (null != hisUpdate)
                hisTmpl.putAll(hisUpdate);

            // 解析历史记录
            NutMap hisre = (NutMap) Wn.explainObj(apc.oReq, hisTmpl);

            // 插入历史记录
            HistoryRecord his = Wlang.map2Object(hisre, HistoryRecord.class);
            api.add(his);
        }
    }

}
