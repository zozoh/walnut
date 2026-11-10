package com.site0.walnut.web2.jetty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.web2.support.WnPathRewriter;

/**
 * {@link WnMvcRequest} 的 Jetty9（javax.servlet）实现。
 * <p>
 * 构造器预解析 Head（headers/cookies/query string/path 分段）并创建
 * pathParams/pathArgs 活容器；请求体全部惰性、首读缓存、三种解析互斥。
 * 同时实现 {@link WnPathRewriter}，供 Host→域 A 记录在匹配前重写路径。
 */
public class Jetty9Request implements WnMvcRequest, WnPathRewriter {

    private final HttpServletRequest req;

    private final String method;

    private String path;

    private final String queryString;

    private final NutMap queryStringMap;

    private final NutMap headers;

    private final NutMap cookies;

    private final NutMap attrs;

    private final NutMap pathParams;

    private final List<String> pathArgs;

    private final String contentType;

    private final String remoteAddr;

    private final String scheme;

    private final String host;

    private final Charset charset;

    private byte[] body;

    private boolean bodyLoaded;

    private String inputJson;

    private NutMap inputParams;

    private List<NutMap> uploadParts;

    public Jetty9Request(HttpServletRequest req) {
        this.req = req;
        this.method = req.getMethod();
        this.path = __normalize_path(req.getRequestURI(), req.getContextPath());
        this.queryString = req.getQueryString();
        this.queryStringMap = __parse_query(queryString);
        this.headers = __parse_headers(req);
        this.cookies = __parse_cookies(req);
        this.attrs = new NutMap();
        this.pathParams = new NutMap();
        this.pathArgs = new ArrayList<>();
        this.contentType = req.getContentType();
        this.remoteAddr = __remote_addr(req);
        this.scheme = req.getScheme();
        this.host = __host(req);
        String enc = req.getCharacterEncoding();
        this.charset = (null == enc || enc.isEmpty()) ? StandardCharsets.UTF_8 : Charset.forName(enc);
    }

    // ----------------------------------------------------------------------
    // WnMvcRequest
    // ----------------------------------------------------------------------

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public NutMap queryStringMap() {
        return queryStringMap;
    }

    @Override
    public NutMap getPathParams() {
        return pathParams;
    }

    @Override
    public List<String> getPathArgs() {
        return pathArgs;
    }

    @Override
    public String getHeader(String name) {
        if (null == name)
            return null;
        return headers.getString(name.toUpperCase());
    }

    @Override
    public NutMap getHeaders() {
        return headers;
    }

    @Override
    public String getCookie(String name) {
        return null == name ? null : cookies.getString(name);
    }

    @Override
    public NutMap getCookieMap() {
        return cookies;
    }

    @Override
    public Object getAttr(String name) {
        return attrs.get(name);
    }

    @Override
    public NutMap getAttrs() {
        return attrs;
    }

    @Override
    public InputStream getInputStream() {
        byte[] bs = __body();
        return null == bs ? null : new ByteArrayInputStream(bs);
    }

    @Override
    public Reader getReader() {
        byte[] bs = __body();
        return null == bs ? null : new InputStreamReader(new ByteArrayInputStream(bs), charset);
    }

    @Override
    public String getInputJson() {
        if (null == inputJson) {
            byte[] bs = __body();
            inputJson = null == bs ? "" : new String(bs, charset);
        }
        return inputJson;
    }

    @Override
    public NutMap getInputParams() {
        if (null == inputParams) {
            inputParams = new NutMap();
            if (null != contentType
                && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
                byte[] bs = __body();
                if (null != bs) {
                    inputParams.putAll(__parse_query(new String(bs, charset)));
                }
            }
        }
        return inputParams;
    }

    @Override
    public List<NutMap> getUploadParts() {
        if (null == uploadParts) {
            uploadParts = new ArrayList<>();
            if (null != contentType && contentType.toLowerCase().startsWith("multipart/form-data")) {
                byte[] bs = __body();
                String boundary = __boundary(contentType);
                if (null != bs && null != boundary) {
                    uploadParts.addAll(__parse_multipart(bs, boundary));
                }
            }
        }
        return uploadParts;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return req.getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return req.getRemotePort();
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (null == type)
            return null;
        return type.isInstance(req) ? type.cast(req) : null;
    }

    // ----------------------------------------------------------------------
    // WnPathRewriter
    // ----------------------------------------------------------------------

    @Override
    public void setPath(String path) {
        this.path = __normalize_path(path, null);
        this.pathParams.clear();
        this.pathArgs.clear();
    }

    // ----------------------------------------------------------------------
    // 内部解析
    // ----------------------------------------------------------------------

    private byte[] __body() {
        if (!bodyLoaded) {
            bodyLoaded = true;
            try (InputStream in = req.getInputStream()) {
                body = null == in ? null : in.readAllBytes();
            }
            catch (IOException e) {
                body = null;
            }
        }
        return body;
    }

    private static String __normalize_path(String path, String contextPath) {
        String p = path;
        if (null != contextPath && !contextPath.isEmpty() && null != p
            && p.startsWith(contextPath)) {
            p = p.substring(contextPath.length());
        }
        if (null == p || p.isEmpty())
            return "/";
        return p.startsWith("/") ? p : "/" + p;
    }

    private static NutMap __parse_headers(HttpServletRequest req) {
        NutMap map = new NutMap();
        Enumeration<String> names = req.getHeaderNames();
        if (null != names) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if (null == name)
                    continue;
                String value = req.getHeader(name);
                map.put(name.toUpperCase(), value);
            }
        }
        return map;
    }

    private static NutMap __parse_cookies(HttpServletRequest req) {
        NutMap map = new NutMap();
        Cookie[] cs = req.getCookies();
        if (null != cs) {
            for (Cookie c : cs) {
                if (null != c.getName())
                    map.put(c.getName(), c.getValue());
            }
        }
        return map;
    }

    static NutMap __parse_query(String qs) {
        NutMap map = new NutMap();
        if (null == qs || qs.isEmpty())
            return map;
        for (String pair : qs.split("&")) {
            if (pair.isEmpty())
                continue;
            int i = pair.indexOf('=');
            String name = i < 0 ? pair : pair.substring(0, i);
            String value = i < 0 ? "" : pair.substring(i + 1);
            try {
                name = URLDecoder.decode(name, StandardCharsets.UTF_8);
                value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            }
            catch (Exception e) {
                // 解码失败保留原文
            }
            Object old = map.get(name);
            if (null == old) {
                map.put(name, value);
            }
            else if (old instanceof String[]) {
                String[] olds = (String[]) old;
                String[] news = new String[olds.length + 1];
                System.arraycopy(olds, 0, news, 0, olds.length);
                news[olds.length] = value;
                map.put(name, news);
            }
            else {
                map.put(name, new String[]{old.toString(), value});
            }
        }
        return map;
    }

    private static String __remote_addr(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (null != xff && !xff.isEmpty()) {
            int i = xff.indexOf(',');
            return (i < 0 ? xff : xff.substring(0, i)).trim();
        }
        String real = req.getHeader("X-Real-IP");
        if (null != real && !real.isEmpty())
            return real.trim();
        return req.getRemoteAddr();
    }

    private static String __host(HttpServletRequest req) {
        String host = req.getHeader("Host");
        if (null == host || host.isEmpty())
            host = req.getServerName();
        if (null == host)
            return "";
        int i = host.lastIndexOf(':');
        if (i > 0)
            host = host.substring(0, i);
        if (host.startsWith("www."))
            host = host.substring(4);
        return host.toLowerCase();
    }

    private static String __boundary(String contentType) {
        for (String part : contentType.split(";")) {
            String p = part.trim();
            if (p.toLowerCase().startsWith("boundary=")) {
                String b = p.substring("boundary=".length()).trim();
                if (b.startsWith("\"") && b.endsWith("\"") && b.length() >= 2)
                    b = b.substring(1, b.length() - 1);
                return b;
            }
        }
        return null;
    }

    private List<NutMap> __parse_multipart(byte[] bs, String boundary) {
        List<NutMap> list = new ArrayList<>();
        String marker = "--" + boundary;
        String text = new String(bs, StandardCharsets.ISO_8859_1);
        int off = 0;
        while (true) {
            int start = text.indexOf(marker, off);
            if (start < 0)
                break;
            int headStart = start + marker.length();
            if (text.startsWith("--", headStart))
                break;
            if (text.startsWith("\r\n", headStart))
                headStart += 2;
            int headerEnd = text.indexOf("\r\n\r\n", headStart);
            if (headerEnd < 0)
                break;
            int bodyStart = headerEnd + 4;
            int next = text.indexOf(marker, bodyStart);
            if (next < 0)
                break;
            int bodyEnd = next;
            if (bodyEnd >= 2 && text.charAt(bodyEnd - 2) == '\r' && text.charAt(bodyEnd - 1) == '\n')
                bodyEnd -= 2;

            String head = text.substring(headStart, headerEnd);
            NutMap part = new NutMap();
            String name = null;
            String fileName = null;
            String partType = null;
            for (String line : head.split("\r\n")) {
                String lower = line.toLowerCase();
                if (lower.startsWith("content-disposition:")) {
                    name = __attr(line, "name");
                    fileName = __attr(line, "filename");
                }
                else if (lower.startsWith("content-type:")) {
                    partType = line.substring("content-type:".length()).trim();
                }
            }
            byte[] data = new byte[bodyEnd - bodyStart];
            System.arraycopy(bs, bodyStart, data, 0, data.length);

            part.put("name", name);
            part.put("fileName", fileName);
            part.put("contentType", partType);
            part.put("size", data.length);
            part.put("inputStream", new ByteArrayInputStream(data));
            list.add(part);

            off = next;
        }
        return list;
    }

    private static String __attr(String line, String key) {
        int i = line.toLowerCase().indexOf(key + "=\"");
        if (i < 0)
            return null;
        int start = i + key.length() + 2;
        int end = line.indexOf('"', start);
        return end < 0 ? null : line.substring(start, end);
    }

}
