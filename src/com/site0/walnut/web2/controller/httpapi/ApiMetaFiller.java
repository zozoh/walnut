package com.site0.walnut.web2.controller.httpapi;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnStr;
import com.site0.walnut.util.Ws;

/**
 * RM-NUTZ-09：照抄旧 {@code 旧 HTTP API 模块} 的请求元数据方法组：
 * {@code __fill_req_meta}（784-877）、{@code __escape_req_meta}（632-649）、
 * {@code __copy_cookie_to_wc_context}（357-362）、
 * {@code __check_api_access_token}（255-284）。
 *
 * <p>javax→标准接口的差异（§3.2）：{@code getProtocol()} 用
 * {@code getScheme().toUpperCase()} 替代（{@code http-protocol} 元数据为
 * "HTTP" 而非旧 "HTTP/1.1"，差异已记录）；{@code getRequestURL()} 用
 * {@code getScheme()+"://"+getHost()+getPath()} 拼接；copy-cookie 改为
 * {@code getCookieMap()} 内联循环。</p>
 */
public class ApiMetaFiller {

    private final WnIo io;

    public ApiMetaFiller(WnIo io) {
        this.io = io;
    }

    public void fillReqMeta(WnHttpApiContext apc) throws UnsupportedEncodingException {
        // .........................................
        // 准备请求对象元数据（标准接口：头部在构造函数里已预解析好）
        NutMap headers = apc.req.getHeaders();

        // 保存 http 参数
        apc.reqMeta.put("http-usr", apc.u.getName());
        apc.reqMeta.put("http-grp", apc.u.getMainGroup());
        apc.reqMeta.put("http-home", apc.u.getHomePath());
        apc.reqMeta.put("http-api", apc.oApi.name());

        // .........................................
        // 记录老的Session对象信息
        if (null != apc.oldSe) {
            apc.reqMeta.put("http-se-ticket", apc.oldSe.getTicket());
            apc.reqMeta.put("http-se-me-name", apc.oldSe.getUser().getName());
            apc.reqMeta.put("http-se-me-group", apc.oldSe.getUser().getMainGroup());
            apc.reqMeta.put("http-se-vars", apc.oldSe.getEnv());
        }

        // .........................................
        // 记录请求信息的其他数据
        apc.uri = apc.req.getPath();
        apc.reqMeta.put("http-protocol", apc.req.getScheme().toUpperCase());
        apc.reqMeta.put("http-method", apc.req.getMethod().toUpperCase());
        apc.reqMeta.put("http-uri", apc.uri);
        apc.reqMeta.put("http-url",
                        apc.req.getScheme() + "://" + apc.req.getHost() + apc.req.getPath());
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
        for (String hnm : headers.keySet()) {
            String hval = apc.req.getHeader(hnm);
            apc.reqMeta.put("http-header-" + hnm.toUpperCase(), hval);
        }

        // .........................................
        // 计入请求 Cookie
        NutMap coos = apc.req.getCookieMap();
        if (null != coos && coos.size() > 0) {
            for (Map.Entry<String, Object> en : coos.entrySet()) {
                apc.reqMeta.put("http-cookie-" + en.getKey(), en.getValue());
            }
        }

        // .........................................
        // 请求的属性：只记录简单的可被序列化的属性
        NutMap attrs = apc.req.getAttrs();
        for (Map.Entry<String, Object> en : attrs.entrySet()) {
            String attrName = en.getKey();
            Object attrVal = en.getValue();
            if (null == attrVal) {
                continue;
            }
            Mirror<?> mi = Mirror.me(attrVal);
            if (mi.isSimple()) {
                apc.reqMeta.put(attrName, attrVal);
            }
        }
    }

    public void escapeReqMeta(WnHttpApiContext apc) {
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

    public void copyCookieToWcContext(WnHttpApiContext apc) {
        String[] copyCookieNames = apc.oApi.getAs("copy-cookie", String[].class);
        if (null != copyCookieNames && copyCookieNames.length > 0) {
            // 标准接口：WnContext.copyCookieItems 吃的是 javax 请求，这里按
            // §3.2 用 getCookieMap 内联同样的逻辑（SEID 进 ticket，其余 setv）
            for (String cknm : copyCookieNames) {
                String v = apc.req.getCookie(cknm);
                if (null != v) {
                    if (Wn.AT_SEID.equals(cknm)) {
                        apc.wc.setTicket(v);
                    } else {
                        apc.wc.setv(cknm, v);
                    }
                }
            }
        }
    }

    public void checkApiAccessToken(WnHttpApiContext apc) {
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
            WnObj oDmnKey = io.fetch(null, phDmnKey);
            if (null == oDmnKey) {
                throw Er.create("e.api.forbid");
            }
            String atDmnKey = io.readText(oDmnKey).trim();
            if (!atClientKey.equals(atDmnKey)) {
                throw Er.create("e.api.forbid", atClientKey);
            }
        }
    }

}
