package org.nutz.walnut.ext.net.http;

import java.io.InputStream;
import java.net.Proxy;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.http.bean.HttpFormPart;
import org.nutz.walnut.ext.net.http.bean.HttpUrl;
import org.nutz.walnut.ext.net.http.impl.HttpGetConnector;
import org.nutz.walnut.util.Ws;

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

    public HttpContext() {
        headers = new NutMap();
        params = new NutMap();
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
        this.headers = headers;
    }

    public void addHeaders(NutBean headers) {
        if (null == this.headers) {
            this.headers = headers;
        } else {
            this.headers.putAll(params);
        }
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

    public boolean hasFormParts() {
        return null != this.formParts && !formParts.isEmpty();
    }

    public List<HttpFormPart> getFormParts() {
        return formParts;
    }

    public void setFormParts(List<HttpFormPart> formParts) {
        this.formParts = formParts;
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

}
