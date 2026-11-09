package com.site0.walnut.mvc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.mvc.fake.FakeController;

/**
 * T3 参数绑定测试（特性矩阵 §3.2）：查询串/表单/JSON、默认值、类型转换、
 * <code>@Attr</code>/<code>@ReqHeader</code> 等价能力、请求体流、multipart。
 *
 * <p>WnMvc 不复刻 Nutz 反射注入：控制器经 {@link WnMvcContext#request()}
 * 的中立读取面取值，类型转换/默认值用 {@link NutMap} 的类型 getter。</p>
 */
public class WnMvcParamBindTest extends WnMvcTestBase {

    /**
     * T-20 查询串解析：单值 String、多值 String[]、UTF-8 URL 解码。
     */
    @Test
    public void t20_query_string_map() {
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .path("/o/query")
                                               .query("a=1&b=x&b=y&nm=%E4%BD%A0%E5%A5%BD")
                                               .build();
        NutMap qs = req.queryStringMap();
        assertEquals("1", qs.getString("a"));
        assertArrayEquals(new String[]{"x", "y"}, (String[]) qs.get("b"));
        assertEquals("你好", qs.getString("nm"));
    }

    /**
     * T-21 表单 body：getInputParams 按 urlencoded 解析，值形态同 queryStringMap。
     */
    @Test
    public void t21_form_body_params() {
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .method("POST")
                                               .path("/u/do/login")
                                               .bodyForm("nm=demo&passwd=%40b%26c&tag=x&tag=y")
                                               .build();
        NutMap ps = req.getInputParams();
        assertEquals("demo", ps.getString("nm"));
        assertEquals("@b&c", ps.getString("passwd"));
        assertArrayEquals(new String[]{"x", "y"}, (String[]) ps.get("tag"));
    }

    /**
     * T-22 JSON body：getInputJson 返回原文（不解析），二次调用幂等。
     */
    @Test
    public void t22_json_body_raw_text() {
        String json = "{\"a\":1,\"b\":\"x\"}";
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .method("POST")
                                               .path("/o/update")
                                               .bodyJson(json)
                                               .build();
        assertEquals(json, req.getInputJson());
        // 幂等
        assertEquals(json, req.getInputJson());
    }

    /**
     * T-23 默认值与类型转换：NutMap 类型 getter（对应 @Param(df=...) 与 Castors）。
     */
    @Test
    public void t23_default_and_type_cast() {
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .path("/a/lookup/xx")
                                               .query("limit=50&reset=true")
                                               .build();
        NutMap qs = req.queryStringMap();
        // 命中转换
        assertEquals(50, qs.getInt("limit", 30));
        assertTrue(qs.getBoolean("reset", false));
        // 缺省走默认值（对应 @Param(value="limit", df="30")）
        assertEquals(30, qs.getInt("limit_x", 30));
        assertEquals("dft", qs.getString("no_such", "dft"));
    }

    /**
     * T-24 `@Attr` 等价能力：请求属性由动作链写入、controller 读取。
     * <p>对应旧 <code>@Attr("wn_www_grp")</code>：WalnutFilter 写请求属性，
     * 新体系由 HeadFilter 写 {@link WnMvcRequest#getAttrs()}。</p>
     */
    @Test
    public void t24_attr_equivalent() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/a/login/**", FakeController.class);
        final Object[] seen = new Object[1];
        FakeController.HANDLER = ctx -> {
            seen[0] = ctx.request().getAttr("wn_www_grp");
            return WnMvcView.none();
        };
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .path("/a/login/")
                                               .attr("wn_www_grp", "demo.com")
                                               .build();
        dispatch(m, req);
        assertEquals("demo.com", seen[0]);
    }

    /**
     * T-25 `@ReqHeader` 等价能力：请求头读取（大小写不敏感）。
     */
    @Test
    public void t25_req_header_equivalent() {
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .path("/o/read/xx")
                                               .header("If-None-Match", "abc123")
                                               .header("range", "bytes=0-99")
                                               .build();
        assertEquals("abc123", req.getHeader("if-none-match"));
        assertEquals("bytes=0-99", req.getHeader("Range"));
        assertNull(req.getHeader("x-no-such"));
        assertEquals("abc123", req.getHeaders().getString("IF-NONE-MATCH"));
    }

    /**
     * T-26 请求 Cookie：按名读取与全量表（对应 SEID 会话恢复）。
     */
    @Test
    public void t26_request_cookie() {
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .path("/a/me")
                                               .cookie("SEID", "s-1")
                                               .build();
        assertEquals("s-1", req.getCookie("SEID"));
        assertEquals("s-1", req.getCookieMap().getString("SEID"));
        assertNull(req.getCookie("NO_SUCH"));
    }

    /**
     * T-27 请求体原始流（对应无注解 InputStream 注入 / 原始流上传）：
     * getInputStream 多次调用返回同一流，被消费后为空。
     */
    @Test
    public void t27_input_stream_once() throws Exception {
        byte[] body = "hello-stream".getBytes(StandardCharsets.UTF_8);
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .method("POST")
                                               .path("/o/save/stream")
                                               .body(body, "application/octet-stream")
                                               .build();
        InputStream ins = req.getInputStream();
        assertNotNull(ins);
        assertTrue(ins == req.getInputStream());
        byte[] buf = new byte[body.length];
        int n = ins.read(buf);
        assertEquals(body.length, n);
        assertEquals("hello-stream", new String(buf, StandardCharsets.UTF_8));
        // 已消费：getInputJson 读到空
        assertEquals("", req.getInputJson());
    }

    /**
     * T-28 multipart 上传表单：getUploadParts 解析 name/fileName/contentType/size/inputStream。
     */
    @Test
    public void t28_multipart_upload_parts() throws Exception {
        String boundary = "----wnBoundary7";
        String body = "--" + boundary + "\r\n"
                      + "Content-Disposition: form-data; name=\"str\"\r\n\r\n"
                      + "~/a.txt\r\n"
                      + "--" + boundary + "\r\n"
                      + "Content-Disposition: form-data; name=\"file\"; filename=\"a.txt\"\r\n"
                      + "Content-Type: text/plain\r\n\r\n"
                      + "FILE-CONTENT\r\n"
                      + "--" + boundary + "--\r\n";
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .method("POST")
                                               .path("/o/upload")
                                               .body(body.getBytes(StandardCharsets.UTF_8),
                                                     "multipart/form-data; boundary=" + boundary)
                                               .build();
        List<NutMap> parts = req.getUploadParts();
        assertEquals(2, parts.size());
        // 字段 part
        assertEquals("str", parts.get(0).getString("name"));
        // 文件 part
        NutMap f = parts.get(1);
        assertEquals("file", f.getString("name"));
        assertEquals("a.txt", f.getString("fileName"));
        assertEquals("text/plain", f.getString("contentType"));
        assertEquals("FILE-CONTENT".length(), f.getInt("size"));
        InputStream ins = (InputStream) f.get("inputStream");
        byte[] buf = new byte[f.getInt("size")];
        assertEquals(buf.length, ins.read(buf));
        assertEquals("FILE-CONTENT", new String(buf, StandardCharsets.UTF_8));
    }

    /**
     * T-29 三解析方法互斥消费流：先 getInputJson 后 getInputParams 得到空表
     * （流已被消费）；同类方法重复调用幂等（首读缓存）。
     */
    @Test
    public void t29_body_parse_mutex_and_idempotent() {
        MockWnMvcRequest req = MockWnMvcRequest.builder()
                                               .method("POST")
                                               .path("/o/set")
                                               .bodyForm("a=1")
                                               .build();
        // form 解析幂等
        assertEquals("1", req.getInputParams().getString("a"));
        assertEquals("1", req.getInputParams().getString("a"));
        // 互斥：JSON 再读为空（流已消费）
        assertEquals("", req.getInputJson());
    }

    /**
     * T-30 路径绑定经 context 透传：pathParams/pathArgs 由 match 写入后，
     * controller 经 ctx.pathParam/pathArg 读取。
     */
    @Test
    public void t30_path_bind_via_context() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/api/{usr}/**", FakeController.class);
        final String[] seen = new String[2];
        FakeController.HANDLER = ctx -> {
            seen[0] = (String) ctx.pathParam("usr");
            seen[1] = ctx.pathArg(0);
            return WnMvcView.none();
        };
        dispatch(m, MockWnMvcRequest.builder().path("/api/root/x/y").build());
        assertEquals("root", seen[0]);
        assertEquals("x/y", seen[1]);
    }

}
