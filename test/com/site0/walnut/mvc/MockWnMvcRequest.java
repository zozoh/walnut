package com.site0.walnut.mvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutMap;

/**
 * JETTY-03 假请求：{@link WnMvcRequest} 的测试实现。
 *
 * <p>遵守接口约定：构造函数预解析 HTTP Head（headers/cookies/query string）与路径分段，
 * 创建 {@link #getPathParams()} / {@link #getPathArgs()} 活容器；请求体<b>惰性</b>，
 * {@link #getInputJson()} / {@link #getInputParams()} / {@link #getUploadParts()}
 * 互斥消费流、首读缓存幂等。</p>
 *
 * <p>用 {@link #builder()} 构造：</p>
 *
 * <pre>
 * WnMvcRequest req = MockWnMvcRequest.builder()
 *                                    .method("POST")
 *                                    .path("/a/open/xx")
 *                                    .query("a=1&amp;b=2")
 *                                    .header("X-Walnut-Ticket", "abc")
 *                                    .cookie("SEID", "s1")
 *                                    .bodyForm("nm=xx&amp;passwd=yy")
 *                                    .build();
 * </pre>
 */
public class MockWnMvcRequest implements WnMvcRequest {

    // ----------------------------------------------------------------------
    // Builder
    // ----------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String method = "GET";

        private String path = "/";

        private String queryString;

        private final NutMap headers = new NutMap();

        private final NutMap cookies = new NutMap();

        private final NutMap attrs = new NutMap();

        private byte[] body;

        private String contentType;

        private String remoteAddr = "127.0.0.1";

        private String remoteHost = "localhost";

        private int remotePort = 0;

        private String scheme = "http";

        private String host = "localhost";

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /** 原始 query string（不含 <code>?</code>）。 */
        public Builder query(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.put(name.toUpperCase(), value);
            return this;
        }

        public Builder cookie(String name, String value) {
            this.cookies.put(name, value);
            return this;
        }

        public Builder attr(String name, Object value) {
            this.attrs.put(name, value);
            return this;
        }

        /** 请求体为 form（application/x-www-form-urlencoded）。 */
        public Builder bodyForm(String form) {
            this.body = form.getBytes(StandardCharsets.UTF_8);
            this.contentType = "application/x-www-form-urlencoded";
            return this;
        }

        /** 请求体为 JSON 原文。 */
        public Builder bodyJson(String json) {
            this.body = json.getBytes(StandardCharsets.UTF_8);
            this.contentType = "application/json";
            return this;
        }

        /** 请求体为原始字节。 */
        public Builder body(byte[] body, String contentType) {
            this.body = body;
            this.contentType = contentType;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder remoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        public Builder remoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
            return this;
        }

        public Builder remotePort(int remotePort) {
            this.remotePort = remotePort;
            return this;
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public MockWnMvcRequest build() {
            return new MockWnMvcRequest(this);
        }
    }

    // ----------------------------------------------------------------------
    // 预解析状态（构造函数里完成）
    // ----------------------------------------------------------------------

    private final String method;

    private final String path;

    private final String queryString;

    private final NutMap queryStringMap;

    private final NutMap headers;

    private final NutMap cookies;

    private final NutMap attrs;

    private final NutMap pathParams = new NutMap();

    private final List<String> pathArgs = new ArrayList<>();

    private final String remoteAddr;

    private final String remoteHost;

    private final int remotePort;

    private final String scheme;

    private final String host;

    private String contentType;

    private final ByteArrayInputStream ins;

    // 惰性缓存（互斥消费流、首读幂等）
    private String inputJson;

    private NutMap inputParams;

    private List<NutMap> uploadParts;

    private MockWnMvcRequest(Builder b) {
        this.method = b.method;
        this.path = b.path;
        this.queryString = b.queryString;
        this.queryStringMap = __parse_form(b.queryString);
        this.headers = b.headers;
        this.cookies = b.cookies;
        this.attrs = b.attrs;
        this.remoteAddr = b.remoteAddr;
        this.remoteHost = b.remoteHost;
        this.remotePort = b.remotePort;
        this.scheme = b.scheme;
        this.host = b.host;
        this.contentType = b.contentType;
        this.ins = null == b.body ? null : new ByteArrayInputStream(b.body);
    }

    // ----------------------------------------------------------------------
    // Head 部分（预解析）
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
        return null == name ? null : headers.getString(name.toUpperCase());
    }

    @Override
    public NutMap getHeaders() {
        return headers;
    }

    @Override
    public String getCookie(String name) {
        return cookies.getString(name);
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
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return remoteHost;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getHost() {
        return host;
    }

    // ----------------------------------------------------------------------
    // 请求体（惰性；三解析方法互斥消费流、首读缓存幂等）
    // ----------------------------------------------------------------------

    @Override
    public InputStream getInputStream() {
        return ins;
    }

    @Override
    public Reader getReader() {
        return null == ins ? null : new InputStreamReader(ins, StandardCharsets.UTF_8);
    }

    @Override
    public String getInputJson() {
        if (null == inputJson)
            inputJson = __read_all();
        return inputJson;
    }

    @Override
    public NutMap getInputParams() {
        if (null == inputParams)
            inputParams = __parse_form(__read_all());
        return inputParams;
    }

    @Override
    public List<NutMap> getUploadParts() {
        if (null == uploadParts)
            uploadParts = __parse_multipart(__read_all_bytes());
        return uploadParts;
    }

    // ----------------------------------------------------------------------
    // 逃生口
    // ----------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> type) {
        return type.isInstance(this) ? (T) this : null;
    }

    // ----------------------------------------------------------------------
    // 内部实现
    // ----------------------------------------------------------------------

    /** 读出剩余请求体为字符串（消费流）。 */
    private String __read_all() {
        byte[] bs = __read_all_bytes();
        return new String(bs, StandardCharsets.UTF_8);
    }

    /** 读出剩余请求体为字节（消费流；无 body 或已消费时为空数组）。 */
    private byte[] __read_all_bytes() {
        if (null == ins)
            return new byte[0];
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        try {
            int n;
            while ((n = ins.read(buf)) >= 0)
                out.write(buf, 0, n);
        }
        catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    /** 解析 form（query string 或 urlencoded body）：单值 String、多值 String[]。 */
    private static NutMap __parse_form(String form) {
        NutMap re = new NutMap();
        if (null == form || form.isEmpty())
            return re;
        for (String pair : form.split("&")) {
            if (pair.isEmpty())
                continue;
            int i = pair.indexOf('=');
            String k = i < 0 ? pair : pair.substring(0, i);
            String v = i < 0 ? "" : pair.substring(i + 1);
            k = URLDecoder.decode(k, StandardCharsets.UTF_8);
            v = URLDecoder.decode(v, StandardCharsets.UTF_8);
            Object old = re.get(k);
            if (null == old) {
                re.put(k, v);
            } else if (old instanceof String[]) {
                String[] arr = (String[]) old;
                String[] arr2 = new String[arr.length + 1];
                System.arraycopy(arr, 0, arr2, 0, arr.length);
                arr2[arr.length] = v;
                re.put(k, arr2);
            } else {
                re.put(k, new String[]{(String) old, v});
            }
        }
        return re;
    }

    /**
     * 极简 multipart 解析（测试用）：按 boundary 拆分，
     * part 键 <code>name/fileName/contentType/size/inputStream</code>。
     */
    private List<NutMap> __parse_multipart(byte[] body) {
        List<NutMap> re = new ArrayList<>();
        String ct = this.contentType;
        if (null == ct || !ct.startsWith("multipart/form-data") || body.length == 0)
            return re;
        int bi = ct.indexOf("boundary=");
        if (bi < 0)
            return re;
        String boundary = ct.substring(bi + "boundary=".length());
        String delimiter = "--" + boundary;
        String text = new String(body, StandardCharsets.ISO_8859_1);
        // 按分隔符切段（最后一段是结束符 "--"）
        String[] segments = text.split(java.util.regex.Pattern.quote(delimiter));
        for (String seg : segments) {
            if (seg.isEmpty() || seg.equals("--") || seg.equals("--\r\n")
                || seg.equals("\r\n"))
                continue;
            String s = seg;
            if (s.startsWith("\r\n"))
                s = s.substring(2);
            if (s.endsWith("\r\n"))
                s = s.substring(0, s.length() - 2);
            if (s.endsWith("--"))
                s = s.substring(0, s.length() - 2);
            int hi = s.indexOf("\r\n\r\n");
            if (hi < 0)
                continue;
            String head = s.substring(0, hi);
            String content = s.substring(hi + 4);
            NutMap part = new NutMap();
            for (String line : head.split("\r\n")) {
                int ci = line.indexOf(':');
                if (ci < 0)
                    continue;
                String hn = line.substring(0, ci).trim();
                String hv = line.substring(ci + 1).trim();
                if ("Content-Disposition".equalsIgnoreCase(hn)) {
                    for (String item : hv.split(";")) {
                        item = item.trim();
                        if (item.startsWith("name=\""))
                            part.put("name", item.substring(6, item.length() - 1));
                        else if (item.startsWith("filename=\""))
                            part.put("fileName", item.substring(10, item.length() - 1));
                    }
                } else if ("Content-Type".equalsIgnoreCase(hn)) {
                    part.put("contentType", hv);
                }
            }
            byte[] cbs = content.getBytes(StandardCharsets.ISO_8859_1);
            part.put("size", cbs.length);
            part.put("inputStream", new ByteArrayInputStream(cbs));
            re.add(part);
        }
        return re;
    }

}
