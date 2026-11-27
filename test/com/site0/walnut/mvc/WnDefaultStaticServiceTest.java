package com.site0.walnut.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.range.WnGetInputStream;

/**
 * #494：C 类站静态服务的 ETag / 304 / Range / HEAD / 后缀白名单。
 *
 * <p><b>这一批用例按「期望行为」写，先红后绿</b>：它们只用当前的公开 API
 * （{@link WnDefaultStaticService#serve(WnMvcContext)} + {@link WnStaticResolver}），
 * 所以在实现改完之前应当失败，失败点就是缺口清单。</p>
 */
public class WnDefaultStaticServiceTest {

    private static final String GRP = "demo";

    private static final String SITE = "/home/demo/www";

    private FakeResolver resolver;

    private FakeMimes mimes;

    private Map<String, byte[]> contents;

    // ======================================================================
    // 用例
    // ======================================================================

    /** 1) 普通 GET：ETag（带引号）/ Accept-Ranges / Content-Length 齐全。 */
    @Test
    public void t01_ok_headers() {
        WnObj o = put("index.html", "<h1>hi</h1>");
        MockWnMvcResponse resp = get("/", null, null);

        assertEquals(200, resp.status());
        assertEquals("text/html", resp.header("Content-Type"));
        assertEquals("\"" + Wn.getEtag(o) + "\"", resp.header("ETag"));
        assertEquals("bytes", resp.header("Accept-Ranges"));
        assertEquals(String.valueOf(o.len()), resp.header("Content-Length"));
        assertEquals("<h1>hi</h1>", resp.bodyString());
    }

    /** 2) If-None-Match 命中 → 304，不写 body，也不带 Content-Length。 */
    @Test
    public void t02_not_modified() {
        WnObj o = put("index.html", "<h1>hi</h1>");
        String etag = "\"" + Wn.getEtag(o) + "\"";

        MockWnMvcResponse resp = get("/", etag, null);

        assertEquals(304, resp.status());
        assertEquals(etag, resp.header("ETag"));
        assertNull("304 不应带 Content-Length", resp.header("Content-Length"));
        assertEquals("", resp.bodyString());
    }

    /** 3) 前缀区间：bytes=0-2 → 206 + Content-Range。 */
    @Test
    public void t03_range_prefix() {
        put("index.html", "hello");
        MockWnMvcResponse resp = get("/", null, "bytes=0-2");

        assertEquals(206, resp.status());
        assertEquals("bytes 0-2/5", resp.header("Content-Range"));
        assertEquals("3", resp.header("Content-Length"));
        assertEquals("hel", resp.bodyString());
    }

    /** 4) 后缀区间：bytes=-2 → 206，取最后两个字节。 */
    @Test
    public void t04_range_suffix() {
        put("index.html", "hello");
        MockWnMvcResponse resp = get("/", null, "bytes=-2");

        assertEquals(206, resp.status());
        assertEquals("bytes 3-4/5", resp.header("Content-Range"));
        assertEquals("lo", resp.bodyString());
    }

    /** 5) 区间越界：bytes=99- → 416 + Content-Range: bytes * / len。 */
    @Test
    public void t05_range_unsatisfiable() {
        put("index.html", "hello");
        MockWnMvcResponse resp = get("/", null, "bytes=99-");

        assertEquals(416, resp.status());
        assertEquals("bytes */5", resp.header("Content-Range"));
        assertEquals("", resp.bodyString());
    }

    /** 6) HEAD：头与 GET 一致（含 Content-Length），但没有 body。 */
    @Test
    public void t06_head_no_body() {
        WnObj o = put("index.html", "hello");
        MockWnMvcResponse resp = serve("HEAD", "/", null, null);

        assertEquals(200, resp.status());
        assertEquals(String.valueOf(o.len()), resp.header("Content-Length"));
        assertEquals("\"" + Wn.getEtag(o) + "\"", resp.header("ETag"));
        assertEquals("", resp.bodyString());
    }

    /** 7) 白名单：/media/x.mp4 应该出文件，而不是回 index.html。 */
    @Test
    public void t07_mp4_served() {
        put("index.html", "INDEX");
        put("media/amb.mp4", "MP4-BYTES");
        MockWnMvcResponse resp = get("/media/amb.mp4", null, null);

        assertEquals(200, resp.status());
        assertEquals("video/mp4", resp.header("Content-Type"));
        assertEquals("MP4-BYTES", resp.bodyString());
    }

    /** 8) SPA 回退：非资源路径仍然回 index.html。 */
    @Test
    public void t08_spa_fallback() {
        put("index.html", "INDEX");
        MockWnMvcResponse resp = get("/order/12", null, null);

        assertEquals(200, resp.status());
        assertEquals("INDEX", resp.bodyString());
    }

    /** 9) 多区间不支持：忽略 Range，回 200 全量（RFC 允许）。 */
    @Test
    public void t09_multi_range_ignored() {
        put("index.html", "hello");
        MockWnMvcResponse resp = get("/", null, "bytes=0-0,2-2");

        assertEquals(200, resp.status());
        assertEquals("hello", resp.bodyString());
    }

    /** 10) 防回退：不许再整文件 readBytes（722MB 视频的坑）。 */
    @Test
    public void t10_no_full_read() {
        put("index.html", "hello");
        get("/", null, null);
        assertTrue("serve 应走流式 open", resolver.openCalls > 0);
        assertEquals("serve 不应调用 readBytes（应走流式 open）", 0, resolver.readBytesCalls);
    }

    // ======================================================================
    // 脚手架
    // ======================================================================

    @Before
    public void setUp() {
        resolver = new FakeResolver();
        mimes = new FakeMimes();
        contents = new HashMap<>();
        resolver.dir = new WnIoObj().name("www");
    }

    private MockWnMvcResponse get(String path, String ifNoneMatch, String range) {
        return serve("GET", path, ifNoneMatch, range);
    }

    private MockWnMvcResponse serve(String method, String path, String ifNoneMatch, String range) {
        MockWnMvcRequest.Builder b = MockWnMvcRequest.builder()
                                                    .method(method)
                                                    .path(path)
                                                    .attr("wn_www_grp", GRP)
                                                    .attr("wn_www_site", SITE);
        if (null != ifNoneMatch)
            b.header("If-None-Match", ifNoneMatch);
        if (null != range)
            b.header("Range", range);
        MockWnMvcRequest req = b.build();
        MockWnMvcResponse resp = new MockWnMvcResponse();
        WnMvcContext ctx = new WnMvcFactory().newContext(req, resp);
        boolean served = new WnDefaultStaticService(resolver, mimes).serve(ctx);
        assertTrue("静态服务应当命中 " + path, served);
        return resp;
    }

    private WnObj put(String path, String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        String nm = path.substring(path.lastIndexOf('/') + 1);
        int dot = nm.lastIndexOf('.');
        WnIoObj o = new WnIoObj();
        o.name(nm);
        if (dot > 0)
            o.type(nm.substring(dot + 1));
        o.mime(mimes.getMime(o.type()));
        o.len(data.length);
        o.sha1("0123456789abcdef0123456789abcdef01234567");
        o.lastModified(1764000000000L);
        resolver.files.put(path, o);
        contents.put(nm, data);
        return o;
    }

    /** 假解析器：记录 readBytes 调用次数，其余按名字取内容。 */
    class FakeResolver implements WnStaticResolver {

        WnObj dir;

        final Map<String, WnObj> files = new LinkedHashMap<>();

        int readBytesCalls;

        int openCalls;

        @Override
        public WnObj check(String path) {
            return dir;
        }

        @Override
        public WnObj fetch(WnObj parent, String path) {
            return files.get(path);
        }

        @Override
        public byte[] readBytes(WnObj obj) {
            readBytesCalls++;
            return null == obj ? null : contents.get(obj.name());
        }

        @Override
        public WnGetInputStream open(WnObj obj) {
            openCalls++;
            final byte[] data = null == obj ? null : contents.get(obj.name());
            final byte[] buf = null == data ? new byte[0] : data;
            final String etag = null == obj ? null : Wn.getEtag(obj);
            return new WnGetInputStream() {

                @Override
                public InputStream getStream(long offset) {
                    int off = (int) Math.max(0, Math.min(offset, buf.length));
                    return new ByteArrayInputStream(buf, off, buf.length - off);
                }

                @Override
                public long getContentLenth() {
                    return buf.length;
                }

                @Override
                public String getETag() {
                    return etag;
                }
            };
        }
    }

    /** 假 mime 表：keys() 覆盖真实 mime.properties 里会用到的后缀。 */
    static class FakeMimes implements MimeMap {

        final Map<String, String> map = new HashMap<>();

        FakeMimes() {
            map.put("html", "text/html");
            map.put("css", "text/css");
            map.put("js", "application/javascript");
            map.put("json", "application/json");
            map.put("txt", "text/plain");
            map.put("pdf", "application/pdf");
            map.put("jpg", "image/jpeg");
            map.put("jpeg", "image/jpeg");
            map.put("png", "image/png");
            map.put("gif", "image/gif");
            map.put("svg", "image/svg+xml");
            map.put("mp4", "video/mp4");
            map.put("webm", "video/webm");
            map.put("mp3", "audio/mpeg");
        }

        @Override
        public String getMime(String type) {
            return getMime(type, "application/octet-stream");
        }

        @Override
        public String getMime(String type, String dftMime) {
            String v = null == type ? null : map.get(type.toLowerCase());
            return null == v ? dftMime : v;
        }

        @Override
        public Set<String> keys() {
            return map.keySet();
        }
    }
}
