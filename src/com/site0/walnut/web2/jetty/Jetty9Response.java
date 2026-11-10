package com.site0.walnut.web2.jetty;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.site0.walnut.mvc.WnMvcCookie;
import com.site0.walnut.mvc.WnMvcResponse;

/**
 * {@link WnMvcResponse} 的 Jetty9（javax.servlet）实现。
 * <p>
 * status/headers 在未 committed 时写入原生响应；Cookie 用 <code>Set-Cookie</code>
 * 文本写出（支持 SameSite）。
 */
public class Jetty9Response implements WnMvcResponse {

    private final HttpServletResponse resp;

    private final Map<String, String> headers = new LinkedHashMap<>();

    private int status = 200;

    public Jetty9Response(HttpServletResponse resp) {
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
        return null != v ? v : resp.getHeader(name);
    }

    @Override
    public void header(String name, String value) {
        if (committed())
            return;
        headers.put(name.toUpperCase(), value);
        resp.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        if (committed())
            return;
        String key = name.toUpperCase();
        if (!headers.containsKey(key))
            headers.put(key, value);
        resp.addHeader(name, value);
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
        resp.setContentType(contentType);
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
        try {
            return resp.getOutputStream();
        }
        catch (IOException e) {
            throw new IllegalStateException("Jetty9Response: fail to get output stream", e);
        }
    }

    @Override
    public Writer writer() {
        try {
            return resp.getWriter();
        }
        catch (IOException e) {
            throw new IllegalStateException("Jetty9Response: fail to get writer", e);
        }
    }

    @Override
    public boolean committed() {
        return resp.isCommitted();
    }

    @Override
    public void flush() {
        try {
            resp.flushBuffer();
        }
        catch (IOException e) {
            throw new IllegalStateException("Jetty9Response: fail to flush", e);
        }
    }

    @Override
    public void setBufferSize(int size) {
        resp.setBufferSize(size);
    }

    @Override
    public void reset() {
        resp.reset();
        this.status = resp.getStatus();
        this.headers.clear();
    }

    @Override
    public void sendRedirect(String location) {
        try {
            resp.sendRedirect(location);
        }
        catch (IOException e) {
            throw new IllegalStateException("Jetty9Response: fail to redirect", e);
        }
    }

    @Override
    public void sendError(int code) {
        try {
            resp.sendError(code);
        }
        catch (IOException e) {
            throw new IllegalStateException("Jetty9Response: fail to send error", e);
        }
    }

    @Override
    public void sendError(int code, String message) {
        try {
            resp.sendError(code, message);
        }
        catch (IOException e) {
            throw new IllegalStateException("Jetty9Response: fail to send error", e);
        }
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (null == type)
            return null;
        return type.isInstance(resp) ? type.cast(resp) : null;
    }

}
