package com.site0.walnut.web2.jetty;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Response;

import com.site0.walnut.mvc.WnMvcCookie;
import com.site0.walnut.mvc.WnMvcResponse;

/**
 * {@link WnMvcResponse} 的 Jetty12（core，无 servlet）实现（RM-NUTZ-14）。
 * <p>
 * 直接包 {@code org.eclipse.jetty.server.Response}（core 类型）。status/headers 在未
 * committed 时写入原生响应；Cookie 用 <code>Set-Cookie</code> 文本写出（支持 SameSite）；
 * 输出走 {@link Content.Sink#asOutputStream(Content.Sink)}。
 */
public class Jetty12Response implements WnMvcResponse {

    private final Response resp;

    private final Map<String, String> headers = new LinkedHashMap<>();

    private int status = 200;

    private OutputStream out;

    public Jetty12Response(Response resp) {
        this.resp = resp;
        this.status = resp.getStatus();
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public void status(int code) {
        this.status = code;
        if (!committed())
            resp.setStatus(code);
    }

    @Override
    public String header(String name) {
        if (null == name)
            return null;
        String v = headers.get(name.toUpperCase());
        return null != v ? v : resp.getHeaders().get(name);
    }

    @Override
    public void header(String name, String value) {
        if (committed())
            return;
        headers.put(name.toUpperCase(), value);
        resp.getHeaders().put(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        if (committed())
            return;
        String key = name.toUpperCase();
        if (!headers.containsKey(key))
            headers.put(key, value);
        resp.getHeaders().add(name, value);
    }

    @Override
    public Map<String, String> headers() {
        return headers;
    }

    @Override
    public void contentType(String contentType) {
        if (committed())
            return;
        headers.put("CONTENT-TYPE", contentType);
        resp.getHeaders().put("Content-Type", contentType);
    }

    @Override
    public void cookie(WnMvcCookie cookie) {
        if (committed() || null == cookie)
            return;
        StringBuilder sb = new StringBuilder();
        sb.append(cookie.name()).append('=').append(null == cookie.value() ? "" : cookie.value());
        if (null != cookie.path())
            sb.append("; Path=").append(cookie.path());
        if (null != cookie.domain())
            sb.append("; Domain=").append(cookie.domain());
        if (cookie.maxAge() >= 0)
            sb.append("; Max-Age=").append(cookie.maxAge());
        if (cookie.secure())
            sb.append("; Secure");
        if (cookie.httpOnly())
            sb.append("; HttpOnly");
        if (null != cookie.sameSite())
            sb.append("; SameSite=").append(cookie.sameSite().name());
        addHeader("Set-Cookie", sb.toString());
    }

    @Override
    public void cookie(String name, String value) {
        cookie(WnMvcCookie.of(name, value));
    }

    @Override
    public void removeCookie(String name) {
        cookie(WnMvcCookie.remove(name));
    }

    @Override
    public OutputStream outputStream() {
        if (null == out) {
            out = Content.Sink.asOutputStream(resp);
        }
        return out;
    }

    @Override
    public Writer writer() {
        return new OutputStreamWriter(outputStream(), StandardCharsets.UTF_8);
    }

    @Override
    public boolean committed() {
        return resp.isCommitted();
    }

    @Override
    public void flush() {
        try {
            Content.Sink.write(resp, false, ByteBuffer.allocate(0));
        }
        catch (IOException e) {
            throw new IllegalStateException("Jetty12Response: fail to flush", e);
        }
    }

    @Override
    public void setBufferSize(int size) {
        // Jetty12 core 无响应缓冲概念，语义上为 no-op（RM-NUTZ-14 记录）
    }

    @Override
    public void reset() {
        if (committed())
            return;
        HttpFields.Mutable flds = resp.getHeaders();
        for (String name : new LinkedHashMap<>(headers).keySet()) {
            try {
                flds.remove(name);
            }
            catch (Exception e) {
                // 移除失败忽略
            }
        }
        headers.clear();
        this.status = 200;
        resp.setStatus(200);
    }

    @Override
    public void sendRedirect(String location) {
        status(302);
        header("Location", location);
    }

    @Override
    public void sendError(int code) {
        sendError(code, "HTTP " + code);
    }

    @Override
    public void sendError(int code, String message) {
        status(code);
        try {
            byte[] bs = (null == message ? "" : message).getBytes(StandardCharsets.UTF_8);
            outputStream().write(bs);
            flush();
        }
        catch (IOException e) {
            throw new IllegalStateException("Jetty12Response: fail to send error", e);
        }
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (null == type)
            return null;
        return type.isInstance(resp) ? type.cast(resp) : null;
    }

}
