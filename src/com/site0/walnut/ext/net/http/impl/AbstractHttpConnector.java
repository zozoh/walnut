package com.site0.walnut.ext.net.http.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.net.http.HttpConnector;
import com.site0.walnut.ext.net.http.HttpContext;
import com.site0.walnut.ext.net.http.bean.HttpUrl;
import com.site0.walnut.ext.net.http.bean.WnHttpResponse;
import com.site0.walnut.ext.net.util.WnNet;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public abstract class AbstractHttpConnector implements HttpConnector {

    protected HttpContext hc;

    protected HttpURLConnection conn;

    public AbstractHttpConnector(HttpContext hc) {
        this.hc = hc;
    }

    @Override
    public void connect() throws IOException {
        HttpUrl url = hc.getUrl();
        URL jdkUrl = url.toURL();

        // 采用代理连接
        if (hc.hasProxy()) {
            this.conn = (HttpURLConnection) jdkUrl
                .openConnection(hc.getProxy());
        }
        // 直连
        else {
            this.conn = (HttpURLConnection) jdkUrl.openConnection();
        }

        // 对于 HTTPS 的支持
        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection sslc = (HttpsURLConnection) conn;
            if (hc.hasSslSocketFactory()) {
                sslc.setSSLSocketFactory(hc.getSslSocketFactory());
            }
        }

        // 对于 IPV6 的支持
        if (!WnNet.isIPv4Address(url.getHost())) {
            if (!url.isPort(80)) {
                String host = url.toConnectHost();
                conn.addRequestProperty("Host", host);
            }
        }

        //
        // 设置方法
        //
        // Patch
        if (hc.isPATCH()) {
            conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            conn.setRequestMethod("POST");
        }
        // 其他从心
        else {
            conn.setRequestMethod(hc.getMethod().name());
        }

        // 连接过期时间
        if (hc.hasConnectTimeout()) {
            conn.setConnectTimeout(hc.getConnectTimeout());
        }

        // 读取过期时间
        if (hc.hasReadTimeout()) {
            conn.setReadTimeout(hc.getReadTimeout());
        }

        // 是否重定向
        conn.setInstanceFollowRedirects(hc.isFollowRedirects());
    }

    @Override
    public void sendHeaders() {
        if (hc.hasHeaders()) {
            for (Map.Entry<String, Object> en : hc.getHeaders().entrySet()) {
                String key = en.getKey();
                String name = Ws.headerCase(key);
                // String name = Ws.kebabCase(key);
                Object val = en.getValue();
                // System.out.printf("%s : [%s]\n", name, val);
                if (null == val) {
                    continue;
                }
                Wlang.each(val, (index, v, src) -> {
                    if (null != v) {
                        conn.addRequestProperty(name, v.toString());
                    }
                });
            }
        }
    }

    @Override
    public WnHttpResponse getResponse() throws IOException {
        int code = conn.getResponseCode();
        if (code < 0) {
            throw Er.create("e.http.network", code);
        }
        NutMap headers = new NutMap();
        Map<String, List<String>> fields = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> en : fields.entrySet()) {
            String name = en.getKey();
            List<String> vals = en.getValue();
            if (null != vals && !vals.isEmpty()) {
                if (vals.size() == 1) {
                    headers.put(name, vals.get(0));
                } else {
                    headers.put(name, vals);
                }
            }
        }
        WnHttpResponse resp = new WnHttpResponse(code, headers);
        String enc = null;
        if (hc.isAutoDecode()) {
            enc = conn.getContentEncoding();
        }
        InputStream ins;
        if (code >= 400) {
            ins = conn.getErrorStream();
        } else {
            ins = conn.getInputStream();
        }
        resp.setBody(ins, enc);
        return resp;
    }

}
