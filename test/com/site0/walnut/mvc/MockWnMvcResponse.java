package com.site0.walnut.mvc;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RM-NUTZ-03 假响应：{@link WnMvcResponse} 的测试实现。
 *
 * <p>捕获 status / headers（含多值）/ cookies / 响应体字节，供断言；
 * 第一次写正文或 flush 即视为 <b>committed</b>（此后 status/header 修改被忽略）。</p>
 *
 * <p>测试辅助方法：{@link #bodyString()}、{@link #cookieValue(String)}、
 * {@link #redirectedTo()}、{@link #headerValues(String)}。</p>
 */
public class MockWnMvcResponse implements WnMvcResponse {

    private int status = 200;

    /** name → 全部值（保持写入顺序；name 大小写不敏感，统一大写存储）。 */
    private final Map<String, List<String>> headers = new LinkedHashMap<>();

    private final List<WnMvcCookie> cookies = new ArrayList<>();

    private final CommittingOutputStream out = new CommittingOutputStream();

    private Writer writer;

    private boolean committed;

    private int bufferSize = -1;

    /** 首次写正文即 committed。 */
    private class CommittingOutputStream extends ByteArrayOutputStream {

        @Override
        public synchronized void write(int b) {
            committed = true;
            super.write(b);
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            committed = true;
            super.write(b, off, len);
        }

        @Override
        public void flush() {
            committed = true;
        }
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public void status(int code) {
        if (committed)
            return;
        this.status = code;
    }

    @Override
    public String header(String name) {
        List<String> vs = headers.get(name.toUpperCase());
        return null == vs || vs.isEmpty() ? null : vs.get(0);
    }

    @Override
    public void header(String name, String value) {
        if (committed)
            return;
        List<String> vs = new ArrayList<>();
        vs.add(value);
        headers.put(name.toUpperCase(), vs);
    }

    @Override
    public void addHeader(String name, String value) {
        if (committed)
            return;
        headers.computeIfAbsent(name.toUpperCase(), k -> new ArrayList<>()).add(value);
    }

    @Override
    public Map<String, String> headers() {
        Map<String, String> re = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> en : headers.entrySet())
            re.put(en.getKey(), en.getValue().isEmpty() ? null : en.getValue().get(0));
        return re;
    }

    @Override
    public void contentType(String contentType) {
        header("Content-Type", contentType);
    }

    @Override
    public void cookie(WnMvcCookie cookie) {
        cookies.add(cookie);
    }

    @Override
    public void cookie(String name, String value) {
        cookies.add(WnMvcCookie.of(name, value));
    }

    @Override
    public void removeCookie(String name) {
        cookies.add(WnMvcCookie.remove(name));
    }

    @Override
    public OutputStream outputStream() {
        return out;
    }

    @Override
    public Writer writer() {
        if (null == writer)
            writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        return writer;
    }

    @Override
    public boolean committed() {
        return committed;
    }

    @Override
    public void flush() {
        committed = true;
    }

    @Override
    public void setBufferSize(int size) {
        this.bufferSize = size;
    }

    @Override
    public void reset() {
        if (committed)
            throw new IllegalStateException("MockWnMvcResponse: reset after committed");
        this.status = 200;
        this.headers.clear();
        this.cookies.clear();
        this.out.reset();
    }

    @Override
    public void sendRedirect(String location) {
        if (committed)
            return;
        this.status = 302;
        header("Location", location);
        committed = true;
    }

    @Override
    public void sendError(int code) {
        sendError(code, "HTTP " + code);
    }

    @Override
    public void sendError(int code, String message) {
        if (committed)
            return;
        this.status = code;
        try {
            contentType("text/plain;charset=utf-8");
            Writer w = writer();
            w.write(null == message ? "" : message);
            w.flush();
        }
        catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        committed = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> type) {
        return type.isInstance(this) ? (T) this : null;
    }

    // ----------------------------------------------------------------------
    // 测试辅助
    // ----------------------------------------------------------------------

    /** 响应体字节。 */
    public byte[] bodyBytes() {
        return out.toByteArray();
    }

    /** 最后设置的缓冲大小（未设置返回 -1）。 */
    public int bufferSize() {
        return bufferSize;
    }

    /** 响应体按 UTF-8 解码的字符串。 */
    public String bodyString() {
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    /** 一个响应头的全部值（多值头断言用）。 */
    public List<String> headerValues(String name) {
        List<String> vs = headers.get(name.toUpperCase());
        return null == vs ? new ArrayList<>() : vs;
    }

    /** 全部已添加的响应 Cookie。 */
    public List<WnMvcCookie> cookies() {
        return cookies;
    }

    /** 按名找响应 Cookie 的值，不存在返回 null。 */
    public String cookieValue(String name) {
        for (WnMvcCookie c : cookies)
            if (c.name().equals(name))
                return c.value();
        return null;
    }

    /** 按名找响应 Cookie（不存在返回 null）。 */
    public WnMvcCookie findCookie(String name) {
        for (WnMvcCookie c : cookies)
            if (c.name().equals(name))
                return c;
        return null;
    }

    /** 重定向目标（Location 头），未重定向返回 null。 */
    public String redirectedTo() {
        return 302 == status ? header("Location") : null;
    }

}
