package com.site0.walnut.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.mvc.fake.FakeController;

/**
 * T4 视图与错误路径测试（特性矩阵 §3.8 视图、§3.4 Cookie、§3.5 重定向）。
 */
public class WnMvcViewTest extends WnMvcTestBase {

    /** 渲染一个视图并返回捕获响应。 */
    private MockWnMvcResponse render(WnMvcView view) {
        WnMvcFactory factory = newFactory();
        MockWnMvcResponse resp = new MockWnMvcResponse();
        WnMvcContext ctx = factory.newContext(MockWnMvcRequest.builder().build(), resp);
        view.render(ctx);
        return resp;
    }

    /**
     * T-40 json 视图（对应 @Ok("json")）：JSON 序列化 + Content-Type。
     */
    @Test
    public void t40_json_view() {
        MockWnMvcResponse resp = render(WnMvcView.json(new NutMap().setv("a", 1)));
        assertEquals(200, resp.status());
        assertEquals("application/json;charset=utf-8", resp.header("Content-Type"));
        assertEquals("{\"a\":1}", resp.bodyString());
    }

    /**
     * T-41 ajax 成功信封（对应 @Ok("ajax")）：{"ok":true,"data":...}。
     */
    @Test
    public void t41_ajax_ok() {
        MockWnMvcResponse resp = render(WnMvcView.ajax(new NutMap().setv("nm", "x")));
        assertEquals("{\"ok\":true,\"data\":{\"nm\":\"x\"}}", resp.bodyString());
    }

    /**
     * T-42 ajax 失败信封：{"ok":false,"errCode":...,"msg":...}。
     */
    @Test
    public void t42_ajax_fail() {
        MockWnMvcResponse resp = render(WnMvcView.ajax("e.se.noexists", "no session"));
        assertEquals("{\"ok\":false,\"errCode\":\"e.se.noexists\",\"msg\":\"no session\"}",
                     resp.bodyString());
    }

    /**
     * T-43 raw 字节视图（对应 @Ok("raw")）。
     */
    @Test
    public void t43_raw_bytes() {
        MockWnMvcResponse resp = render(WnMvcView.raw("hi".getBytes(StandardCharsets.UTF_8),
                                                      "text/plain"));
        assertEquals("text/plain", resp.header("Content-Type"));
        assertEquals("hi", resp.bodyString());
    }

    /**
     * T-44 raw 流视图：写出输入流内容。
     */
    @Test
    public void t44_raw_stream() {
        MockWnMvcResponse resp = render(WnMvcView.raw(new ByteArrayInputStream("s1".getBytes(StandardCharsets.UTF_8)),
                                                      "text/plain"));
        assertEquals("s1", resp.bodyString());
    }

    /**
     * T-45 download 视图（对应 WnObjDownloadView）：Content-Disposition attachment。
     */
    @Test
    public void t45_download() {
        MockWnMvcResponse resp = render(WnMvcView.download("data".getBytes(StandardCharsets.UTF_8),
                                                           "a.txt",
                                                           "text/plain"));
        String cd = resp.header("Content-Disposition");
        assertNotNull(cd);
        assertTrue(cd.startsWith("attachment"));
        assertTrue(cd.contains("filename=\"a.txt\""));
        assertEquals("data", resp.bodyString());
    }

    /**
     * T-46 redirect 视图（对应 ServerRedirectView / `&gt;&gt;:/`）：302 + Location。
     */
    @Test
    public void t46_redirect() {
        MockWnMvcResponse resp = render(WnMvcView.redirect("/a/login/"));
        assertEquals(302, resp.status());
        assertEquals("/a/login/", resp.redirectedTo());
    }

    /**
     * T-47 html 视图：text/html 输出。
     */
    @Test
    public void t47_html() {
        MockWnMvcResponse resp = render(WnMvcView.html("<html>ok</html>"));
        assertEquals("text/html;charset=utf-8", resp.header("Content-Type"));
        assertEquals("<html>ok</html>", resp.bodyString());
    }

    /**
     * T-48 void 视图（对应 @Ok("void")）：controller 直写 response，视图不写任何内容。
     */
    @Test
    public void t48_none_void() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/gu/qrcode", FakeController.class);
        FakeController.HANDLER = ctx -> {
            try {
                ctx.response().contentType("image/png");
                ctx.response().outputStream().write(new byte[]{1, 2, 3});
                ctx.response().flush();
            }
            catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            return null; // void：响应已直接写出
        };
        MockWnMvcResponse resp = dispatch(m, MockWnMvcRequest.builder().path("/gu/qrcode").build());
        assertEquals("image/png", resp.header("Content-Type"));
        assertEquals(3, resp.bodyBytes().length);
    }

    /**
     * T-49 状态视图（对应 http:NNN）：只设置状态码。
     */
    @Test
    public void t49_status_view() {
        MockWnMvcResponse resp = render(WnMvcView.status(204));
        assertEquals(204, resp.status());
        assertEquals("", resp.bodyString());
    }

    /**
     * T-50 错误视图（空体）：sendError(code)。
     */
    @Test
    public void t50_error_no_body() {
        MockWnMvcResponse resp = render(WnMvcView.error(403));
        assertEquals(403, resp.status());
        assertTrue(resp.committed());
    }

    /**
     * T-51 错误视图（JSON 体）：状态码 + JSON 错误体。
     */
    @Test
    public void t51_error_json_body() {
        NutMap body = new NutMap().setv("ok", false).setv("errCode", "e.x");
        MockWnMvcResponse resp = render(WnMvcView.error(500, body));
        assertEquals(500, resp.status());
        assertEquals("{\"ok\":false,\"errCode\":\"e.x\"}", resp.bodyString());
    }

    /**
     * T-52 Cookie 增（对应 `++cookie&gt;&gt;:` / WnAddCookieViewWrapper）：
     * 响应添加会话 Cookie（Path=/）。
     */
    @Test
    public void t52_cookie_add() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/u/do/login", FakeController.class);
        FakeController.HANDLER = ctx -> {
            ctx.response().cookie("SEID", "ticket-1");
            return WnMvcView.ajax("ok");
        };
        MockWnMvcResponse resp = dispatch(m,
                                          MockWnMvcRequest.builder()
                                                          .method("POST")
                                                          .path("/u/do/login")
                                                          .build());
        WnMvcCookie c = resp.findCookie("SEID");
        assertNotNull(c);
        assertEquals("ticket-1", c.value());
        assertEquals("/", c.path());
    }

    /**
     * T-53 Cookie 删（对应 `--cookie&gt;&gt;:` / WnDelCookieViewWrapper）：
     * removeCookie 等价同名过期 Cookie（maxAge=0）。
     */
    @Test
    public void t53_cookie_remove() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/u/do/logout", FakeController.class);
        FakeController.HANDLER = ctx -> {
            ctx.response().removeCookie("SEID");
            return WnMvcView.redirect("/");
        };
        MockWnMvcResponse resp = dispatch(m,
                                          MockWnMvcRequest.builder()
                                                          .path("/u/do/logout")
                                                          .build());
        WnMvcCookie c = resp.findCookie("SEID");
        assertNotNull(c);
        assertEquals(0, c.maxAge());
        assertEquals(302, resp.status());
    }

    /**
     * T-54 响应一旦 committed，不允许再改 status/header（状态模型约定）。
     */
    @Test
    public void t54_committed_locks_status() {
        MockWnMvcResponse resp = render(WnMvcView.html("x"));
        assertTrue(resp.committed());
        resp.status(500);
        resp.header("X-Late", "y");
        assertEquals(200, resp.status());
        assertEquals(null, resp.header("X-Late"));
    }

}
