package com.site0.walnut.ext.net.http;

import java.io.InputStream;
import java.net.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.nutz.lang.Lang;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.http.bean.HttpFormPart;
import com.site0.walnut.ext.net.http.bean.HttpUrl;
import com.site0.walnut.ext.net.http.impl.HttpGetConnector;
import com.site0.walnut.ext.net.http.impl.HttpMultipartPostConnector;
import com.site0.walnut.ext.net.http.impl.HttpPostConnector;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.stream.WnInputStreamFactory;
import com.site0.walnut.util.stream.WnInputStreamInfo;

public class HttpContext {

    private HttpMethod method;

    private HttpUrl url;

    private NutBean headers;

    private NutBean params;

    private InputStream body;

    private List<HttpFormPart> formParts;

    private SSLSocketFactory sslSocketFactory;

    private HostnameVerifier hostnameVerifier;

    private int connectTimeout;

    private int readTimeout;

    private boolean followRedirects;

    private Proxy proxy;

    /**
     * 文件上传的时候要用到，根据这个接口，可以从一个路径获取输入流
     */
    private WnInputStreamFactory inputStreamFactory;

    public HttpContext() {
        connectTimeout = 3000;
        readTimeout = 15000;
        followRedirects = true;
    }

    public HttpConnector open() {
        HttpMethod m = this.method;
        if (null == method) {
            if (this.hasBody() || this.hasFormParts()) {
                m = HttpMethod.POST;
            } else {
                m = HttpMethod.GET;
            }
        }
        if (HttpMethod.GET == m) {
            return new HttpGetConnector(this);
        }
        if (HttpMethod.POST == m) {
            if (this.hasFormParts()) {
                return new HttpMultipartPostConnector(this);
            }
            return new HttpPostConnector(this);
        }

        throw Lang.impossible();
    }

    public boolean isAUTO() {
        return null == this.method;
    }

    public boolean isGET() {
        return HttpMethod.GET == this.method;
    }

    public boolean isPOST() {
        return HttpMethod.POST == this.method;
    }

    public boolean isHEAD() {
        return HttpMethod.HEAD == this.method;
    }

    public boolean isPUT() {
        return HttpMethod.PUT == this.method;
    }

    public boolean isDELETE() {
        return HttpMethod.DELETE == this.method;
    }

    public boolean isCONNECT() {
        return HttpMethod.CONNECT == this.method;
    }

    public boolean isOPTIONS() {
        return HttpMethod.OPTIONS == this.method;
    }

    public boolean isTRACE() {
        return HttpMethod.TRACE == this.method;
    }

    public boolean isPATCH() {
        return HttpMethod.PATCH == this.method;
    }

    public HttpMethod getMethod() {
        // 自动判断
        if (null == method) {
            if (this.hasBody() || this.hasFormParts()) {
                return HttpMethod.POST;
            }
            return HttpMethod.GET;
        }
        // 指定了方法
        return this.method;
    }

    public void setMethod(String method) {
        String m = Ws.sBlank(method, "GET");
        m = m.toUpperCase();
        this.method = HttpMethod.valueOf(m);
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public HttpUrl getUrl() {
        HttpUrl url = this.url.clone();
        // 指定了 body，那么参数就必须变成 QueryString
        if (this.hasParams()) {
            if (this.hasBody() || this.hasFormParts()) {
                url.addQuery(this.getParams());
            }
        }
        return url;
    }

    public void setUrl(HttpUrl url) {
        this.url = url;

    }

    public void setUrl(String url) {
        this.url = new HttpUrl(url);

    }

    public boolean hasQuery() {
        return this.url.hasQuery();
    }

    public NutMap getQuery() {
        return this.url.getQuery();
    }

    public void setQuery(NutMap query) {
        this.url.setQuery(query);
    }

    public void addQuery(String query) {
        this.url.addQuery(query);
    }

    public void addQuery(NutBean query) {
        this.url.addQuery(query);
    }

    public void clearQuery() {
        this.url.clearQuery();
    }

    public boolean hasHeaders() {
        return null != headers && !headers.isEmpty();
    }

    public NutBean getHeaders() {
        return headers;
    }

    public void setHeaders(NutBean headers) {
        this.headers = null;
        this.addHeaders(headers);
    }

    public void addHeaders(NutBean headers) {
        for (Map.Entry<String, Object> en : headers.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null == val) {
                continue;
            }
            this.addHeader(key, val);
        }
    }

    public void addHeader(String name, Object value) {
        String key = Ws.headerCase(name);
        if (null == this.headers) {
            headers = new NutMap();
        }
        headers.addv3(key, value);
    }

    public boolean hasParams() {
        return null != params && !params.isEmpty();
    }

    public NutBean getParams() {
        return params;
    }

    public void setParams(NutBean params) {
        this.params = params;
    }

    public void addParams(NutBean params) {
        if (null == this.params) {
            this.params = params;
        } else {
            this.params.putAll(params);
        }
    }

    public boolean hasBody() {
        return null != body;
    }

    public InputStream getBody() {
        return body;
    }

    public void setBody(InputStream body) {
        this.body = body;
    }

    public void setBody(String input) {
        this.body = new StringInputStream(input);
    }

    public boolean hasFormParts() {
        return null != this.formParts && !formParts.isEmpty();
    }

    public List<HttpFormPart> getFormParts() {
        return formParts;
    }

    public void setFormParts(List<HttpFormPart> formParts) {
        this.formParts = formParts;
    }

    public void addFormParts(List<HttpFormPart> formParts) {
        if (null == this.formParts) {
            this.formParts = new LinkedList<>();
        }
        this.formParts.addAll(formParts);
    }

    public void addFormPart(HttpFormPart... formParts) {
        if (null == this.formParts) {
            this.formParts = new LinkedList<>();
        }
        for (HttpFormPart part : formParts) {
            this.formParts.add(part);
        }
    }

    public boolean hasSslSocketFactory() {
        return null != sslSocketFactory;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public boolean hasHostnameVerifier() {
        return null != hostnameVerifier;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public boolean hasConnectTimeout() {
        return this.connectTimeout > 0;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public boolean hasReadTimeout() {
        return this.readTimeout > 0;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public boolean hasProxy() {
        return null != proxy;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public WnInputStreamInfo getStreamInfo(String path) {
        return inputStreamFactory.getStreamInfo(path);
    }

    public WnInputStreamFactory getInputStreamFactory() {
        return inputStreamFactory;
    }

    public void setInputStreamFactory(WnInputStreamFactory inputStreamFactory) {
        this.inputStreamFactory = inputStreamFactory;
    }

}
