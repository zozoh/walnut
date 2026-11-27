package com.site0.walnut.web2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;

import com.site0.walnut.mvc.MockWnMvcResponse;
import com.site0.walnut.util.WnHttpResponseWriter;
import com.site0.walnut.util.range.WnGetInputStream;

/**
 * #495：下载 / HTTPOUT 的 304 / 416 / 区间夹取。
 *
 * <p>两份 {@code WnHttpResponseWriter} 已合并成 {@code util} 一份：本测试分别走两个出口
 * （MVC 的「状态 + 头 + body」与裸 HTTP 的 {@code writeTo(OutputStream)}），守住三个 bug 的修复：</p>
 * <ol>
 * <li>304 必须带 ETag（以前回 304 却没有 validator）；</li>
 * <li>不可满足区间回 416 + <code>Content-Range: bytes * / len</code>（以前回 400）；</li>
 * <li>区间上界夹到内容长度（以前 <code>bytes=0-999</code> 会给出 1000 的假长度）。</li>
 * </ol>
 */
public class WnHttpRangeWritersTest {

    private static final String CONTENT = "hello";

    private static final String ETAG = "abc123-5-1764000000000";

    // ======================================================================
    // WnMvcResponse 版（web2/view）
    // ======================================================================

    /** 1) 无 Range：200 + ETag（带引号）+ Content-Length + Accept-Ranges。 */
    @Test
    public void t01_ok_headers() throws IOException {
        MockWnMvcResponse resp = write(null, null);
        assertEquals(200, resp.status());
        assertEquals("\"" + ETAG + "\"", resp.header("ETag"));
        assertEquals("bytes", resp.header("Accept-Ranges"));
        assertEquals("5", resp.header("Content-Length"));
        assertEquals(CONTENT, resp.bodyString());
    }

    /** 2) If-None-Match 命中：304 + ETag，无 Content-Length、无 body。 */
    @Test
    public void t02_not_modified_has_etag() throws IOException {
        MockWnMvcResponse resp = write("\"" + ETAG + "\"", null);
        assertEquals(304, resp.status());
        assertEquals("\"" + ETAG + "\"", resp.header("ETag"));
        assertNull("304 不应带 Content-Length", resp.header("Content-Length"));
        assertEquals("", resp.bodyString());
    }

    /** 2b) 弱校验前缀 W/ 也算命中。 */
    @Test
    public void t02b_not_modified_weak() throws IOException {
        MockWnMvcResponse resp = write("W/\"" + ETAG + "\"", null);
        assertEquals(304, resp.status());
    }

    /** 3) 区间越界：416 + Content-Range: bytes * / len，无 body。 */
    @Test
    public void t03_range_not_satisfiable() throws IOException {
        MockWnMvcResponse resp = write(null, "bytes=99-");
        assertEquals(416, resp.status());
        assertEquals("bytes */5", resp.header("Content-Range"));
        assertEquals("", resp.bodyString());
    }

    /** 4) 区间上界夹到文件长度：bytes=0-999 → 206 bytes 0-4/5。 */
    @Test
    public void t04_range_end_clamped() throws IOException {
        MockWnMvcResponse resp = write(null, "bytes=0-999");
        assertEquals(206, resp.status());
        assertEquals("bytes 0-4/5", resp.header("Content-Range"));
        assertEquals("5", resp.header("Content-Length"));
        assertEquals(CONTENT, resp.bodyString());
    }

    /** 5) 多区间忽略 Range，回 200 全量。 */
    @Test
    public void t05_multi_range_ignored() throws IOException {
        MockWnMvcResponse resp = write(null, "bytes=0-1,3-4");
        assertEquals(200, resp.status());
        assertEquals(CONTENT, resp.bodyString());
    }

    // ======================================================================
    // 裸 HTTP 版（util，cmd_httpout）
    // ======================================================================

    /** 6) 304：状态行正确，且绝不能带 body（以前会写一个空格）。 */
    @Test
    public void t06_raw_304_has_no_body() throws IOException {
        String text = raw("\"" + ETAG + "\"", null);
        assertTrue(text.startsWith("HTTP/1.1 304 "));
        assertTrue("应回 ETag 头", text.contains("ETag: \"" + ETAG + "\""));
        assertTrue("304 之后不应再有 body 字节", text.endsWith("\n\n"));
    }

    /** 7) 416：带 Content-Range: bytes * / len，且没有 body。 */
    @Test
    public void t07_raw_416() throws IOException {
        String text = raw(null, "bytes=99-");
        assertTrue(text.startsWith("HTTP/1.1 416 "));
        assertTrue(text.contains("Content-Range: bytes */5"));
        assertTrue(text.endsWith("\n\n"));
        assertFalse(text.contains("Walnut-Http-Range-WARN"));
    }

    // ======================================================================
    // 脚手架
    // ======================================================================

    /**
     * MVC 出口：仓库里只剩 util 一份 writer（#495 合并），渲染就是「状态 + 头 + body」
     * 三步——这里照 {@code WnDownloadView.render} 的写法走，保证测的是同一条路径。
     */
    private MockWnMvcResponse write(String ifNoneMatch, String range) throws IOException {
        WnHttpResponseWriter w = new WnHttpResponseWriter();
        w.setEtag(ifNoneMatch);
        w.prepare(range, src());
        MockWnMvcResponse resp = new MockWnMvcResponse();
        resp.status(w.getStatus());
        for (Map.Entry<String, String> en : w.toHeaders().entrySet()) {
            resp.header(en.getKey(), en.getValue());
        }
        w.writeBodyTo(resp.outputStream());
        return resp;
    }

    private String raw(String ifNoneMatch, String range) throws IOException {
        WnHttpResponseWriter w = new WnHttpResponseWriter();
        w.setEtag(ifNoneMatch);
        w.prepare(range, src());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        w.writeTo(out);
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private WnGetInputStream src() {
        final byte[] data = CONTENT.getBytes(StandardCharsets.UTF_8);
        return new WnGetInputStream() {

            @Override
            public InputStream getStream(long offset) {
                int s = (int) Math.max(0, Math.min(offset, data.length));
                return new ByteArrayInputStream(data, s, data.length - s);
            }

            @Override
            public long getContentLenth() {
                return data.length;
            }

            @Override
            public String getETag() {
                return ETAG;
            }
        };
    }
}
