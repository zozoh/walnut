package org.nutz.walnut.ext.net.http.bean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.net.util.WnNet;
import org.nutz.walnut.util.Ws;

public class HttpUrl {

    private static Pattern P_URL = Pattern.compile("^((https?):)?((\\/\\/([^/:]+))(:(\\d+))?)?([^?#]*)(\\?([^#]*))?(#(.*))?");

    private String protocol;

    private String host;

    private int port;

    private String path;

    private NutMap query;;

    private String anchor;

    public HttpUrl(HttpUrl url) {
        this.protocol = url.protocol;
        this.host = url.host;
        this.port = url.port;
        this.path = url.path;
        this.query = null == url.query ? null : url.query.duplicate();
        this.anchor = url.anchor;
    }

    public HttpUrl(String url) {
        this.parse(url);
    }

    public HttpUrl() {}

    public void parse(String url) {
        // 解析 URL
        Matcher m = P_URL.matcher(url);
        if (!m.find()) {
            throw Er.create("e.http.url.invalid", url);
        }

        protocol = m.group(2);
        host = m.group(5);
        if (null != m.group(7)) {
            port = Integer.parseInt(m.group(7));
        } else {
            port = 80;
        }
        path = m.group(8);
        anchor = m.group(12);

        // 解析 Query String
        this.query = new NutMap();
        String qs = m.group(10);
        WnNet.parseQueryStringTo(this.query, qs, true);
    }

    public HttpUrl clone() {
        return new HttpUrl(this);
    }

    public URL toURL() {
        String str = this.toString(true, false);
        try {
            return new URL(str);
        }
        catch (MalformedURLException e) {
            throw Er.wrap(e);
        }
    }

    public String toString() {
        return this.toString(true, true);
    }

    public String toString(boolean encode, boolean withHash) {
        StringBuilder sb = new StringBuilder();
        joinUrlPath(sb);
        joinQuery(sb, encode);
        if (withHash) {
            joinHash(sb);
        }
        return sb.toString();
    }

    public void joinHash(StringBuilder sb) {
        if (this.hasAnchor()) {
            sb.append('#').append(anchor);
        }
    }

    public void joinQuery(StringBuilder sb, boolean encode) {
        if (this.hasQuery()) {
            sb.append('?');
            WnNet.joinQuery(sb, query, encode);
        }
    }

    public void joinUrlPath(StringBuilder sb) {
        sb.append(protocol).append("://");
        joinConnectHost(sb);
        if (this.hasPath()) {
            sb.append(path);
        }
    }

    public void joinConnectHost(StringBuilder sb) {
        sb.append(host);
        if (!this.isPort(80)) {
            sb.append(':').append(port);
        }
    }

    public String toUrlPath() {
        StringBuilder sb = new StringBuilder();
        joinUrlPath(sb);
        return sb.toString();
    }

    public String toConnectHost() {
        StringBuilder sb = new StringBuilder();
        this.joinConnectHost(sb);
        return sb.toString();
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isPort(int port) {
        return port == this.port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean hasPath() {
        return !Ws.isBlank(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean hasQuery() {
        return null != query && !query.isEmpty();
    }

    public NutMap getQuery() {
        return query;
    }

    public void setQuery(NutMap query) {
        this.query = query;
    }

    public void addQuery(String query) {
        NutMap map = WnNet.parseQuery(query, true);
        this.addQuery(map);
    }

    public void addQuery(NutBean query) {
        this.query.putAll(query);
        if (null == this.query) {
            this.query = NutMap.WRAP(query);
        } else {
            this.query.putAll(query);
        }
    }

    public void clearQuery() {
        this.query = new NutMap();
    }

    public boolean hasAnchor() {
        return !Ws.isBlank(anchor);
    }

    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

}
