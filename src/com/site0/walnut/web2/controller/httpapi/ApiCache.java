package com.site0.walnut.web2.controller.httpapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wsum;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;
import com.site0.walnut.web.util.WnWeb;

/**
 * RM-NUTZ-09：照抄旧 {@code HttpApiModule.__try_cache}（651-774）与
 * {@code __is_cache_match}（776-782）。命中后直写响应（标准接口）。
 */
public class ApiCache {

    private final WnIo io;

    private final ApiExecutor executor;

    public ApiCache(WnIo io, ApiExecutor executor) {
        this.io = io;
        this.executor = executor;
    }

    public boolean tryCache(WnHttpApiContext apc) throws IOException {
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
        WnObj oCache = io.fetch(null, aph);
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
                ApiExecutor.setCrossOriginDefaultHeaders(apc.oApi, origin, (name, value) -> {
                    if (!apc.oApi.has("http-header-" + name)) {
                        apc.resp.header(WnWeb.niceHeaderName(name), value);
                    }
                });
            }

            // 重定向
            apc.resp.sendRedirect(r_url);

        }
        // 直接输出缓存对象
        else {
            // 设置响应头
            executor.setupRespHeader(apc);

            // 写入响应流
            OutputStream ops = apc.resp.outputStream();
            InputStream ins = io.getInputStream(oCache, 0);
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

}
