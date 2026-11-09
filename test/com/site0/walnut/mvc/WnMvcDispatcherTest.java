package com.site0.walnut.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.site0.walnut.mvc.fake.FakeBornController;
import com.site0.walnut.mvc.fake.FakeController;

/**
 * 调度与未命中回退分流测试（T2/T5 集成 + T6 分流规则）。
 *
 * <p>分流规则（JETTY-03 设计，用户 2026-11-09T04:40 已确认）：API 前缀（/o /u /api）未命中 →
 * 404 JSON（绝不返回 HTML）；静态后缀路径 → 委托 {@link WnMvcStaticService}，
 * 文件不存在 → 404 HTML；其余页面路径 → 404 HTML。</p>
 */
public class WnMvcDispatcherTest extends WnMvcTestBase {

    private static MockWnMvcRequest req(String method, String path) {
        return MockWnMvcRequest.builder().method(method).path(path).build();
    }

    /**
     * T-70 调度全流程：match → born 控制器 → 动作链 → 视图渲染。
     */
    @Test
    public void t70_dispatch_full_pipeline() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/u/version", FakeController.class);
        FakeController.HANDLER = ctx -> WnMvcView.raw("wn-12".getBytes(), "text/plain");
        MockWnMvcResponse resp = dispatch(m, req("GET", "/u/version"));
        assertEquals(200, resp.status());
        assertEquals("wn-12", resp.bodyString());
    }

    /**
     * T-71 控制器每请求 born 新实例：两次请求 born 计数 +2。
     */
    @Test
    public void t71_controller_born_per_request() {
        FakeBornController.BORN_COUNT.set(0);
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/a/me", FakeBornController.class);
        dispatch(m, req("GET", "/a/me"));
        dispatch(m, req("POST", "/a/me"));
        assertEquals(2, FakeBornController.BORN_COUNT.get());
    }

    /**
     * T-72 动作链顺序（调度器组装）：全局 head → 路由 head → controller →
     * 路由 tail → 全局 tail。
     */
    @Test
    public void t72_global_route_filter_order() {
        List<String> steps = new ArrayList<>();
        WnMvcFilter gHead = ctx -> steps.add("gHead");
        WnMvcFilter rHead = ctx -> steps.add("rHead");
        WnMvcFilter rTail = ctx -> steps.add("rTail");
        WnMvcFilter gTail = ctx -> steps.add("gTail");
        WnMvcMapping m = new WnMvcMapping();
        m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.GET},
                "/x",
                FakeController.class,
                new WnMvcFilter[]{rHead},
                new WnMvcFilter[]{rTail});
        FakeController.HANDLER = ctx -> {
            steps.add("controller");
            return WnMvcView.none();
        };
        WnMvcFactory factory = newFactory();
        WnMvcDispatcher d = new WnMvcDispatcher(m,
                                                factory,
                                                new WnMvcFilter[]{gHead},
                                                new WnMvcFilter[]{gTail});
        dispatch(d, factory, req("GET", "/x"));
        assertEquals("[gHead, rHead, controller, rTail, gTail]", steps.toString());
    }

    /**
     * T-73 方法不匹配 = 未命中（对齐 Nutz，无 405）：POST 路由被 GET 请求时
     * 走回退；/u 前缀 → 404 JSON。
     */
    @Test
    public void t73_method_mismatch_is_miss() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.POST, "/u/do/login", FakeController.class);
        MockWnMvcResponse resp = dispatch(m, req("GET", "/u/do/login"));
        assertEquals(404, resp.status());
        assertEquals("application/json;charset=utf-8", resp.header("Content-Type"));
        assertTrue(resp.bodyString().contains("e.mvc.nofound"));
    }

    /**
     * T-74 未命中 API 前缀（/o/**）→ 404 + JSON 错误体，绝不返回 HTML。
     */
    @Test
    public void t74_miss_api_prefix_o_returns_json() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/o/fetch", FakeController.class);
        MockWnMvcResponse resp = dispatch(m, req("GET", "/o/no/such/entry"));
        assertEquals(404, resp.status());
        assertEquals("application/json;charset=utf-8", resp.header("Content-Type"));
        assertTrue(resp.bodyString().contains("\"ok\":false"));
        assertTrue(resp.bodyString().contains("e.mvc.nofound"));
        assertFalse(resp.bodyString().contains("<html"));
    }

    /**
     * T-75 未命中 /u 与 /api 前缀 → 同为 404 JSON。
     */
    @Test
    public void t75_miss_prefix_u_and_api() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/u/version", FakeController.class);
        assertEquals("application/json;charset=utf-8",
                     dispatch(m, req("GET", "/u/nothing")).header("Content-Type"));
        assertEquals("application/json;charset=utf-8",
                     dispatch(m, req("GET", "/api/root/gone")).header("Content-Type"));
    }

    /**
     * T-76 未命中页面路径 → 404 + HTML 错误页。
     */
    @Test
    public void t76_miss_page_returns_html() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/a/login", FakeController.class);
        MockWnMvcResponse resp = dispatch(m, req("GET", "/a/no/such/page"));
        assertEquals(404, resp.status());
        assertEquals("text/html;charset=utf-8", resp.header("Content-Type"));
        assertTrue(resp.bodyString().contains("404"));
        assertTrue(resp.bodyString().contains("<html"));
    }

    /**
     * T-77 未命中静态后缀路径：委托静态文件服务；服务成功（true）则不再走 404。
     */
    @Test
    public void t77_miss_static_delegated() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/a/login", FakeController.class);
        WnMvcFactory factory = newFactory();
        boolean[] served = {false};
        WnMvcStaticService staticService = ctx -> {
            served[0] = true;
            ctx.response().contentType("text/css");
            ctx.response().status(200);
            return true;
        };
        WnMvcDispatcher d = new WnMvcDispatcher(m,
                                                factory,
                                                NO_FILTERS,
                                                NO_FILTERS,
                                                staticService);
        MockWnMvcResponse resp = dispatch(d, factory, req("GET", "/rs/core/site.css"));
        assertTrue(served[0]);
        assertEquals(200, resp.status());
    }

    /**
     * T-78 静态文件服务返回 false（文件不存在）→ 404 HTML。
     */
    @Test
    public void t78_miss_static_not_found() {
        WnMvcMapping m = new WnMvcMapping();
        WnMvcFactory factory = newFactory();
        WnMvcDispatcher d = new WnMvcDispatcher(m,
                                                factory,
                                                NO_FILTERS,
                                                NO_FILTERS,
                                                ctx -> false);
        MockWnMvcResponse resp = dispatch(d, factory, req("GET", "/rs/gone.js"));
        assertEquals(404, resp.status());
        assertEquals("text/html;charset=utf-8", resp.header("Content-Type"));
    }

    /**
     * T-79 未注册静态文件服务时，静态后缀路径 → 404 HTML（不抛异常）。
     */
    @Test
    public void t79_miss_static_without_service() {
        WnMvcMapping m = new WnMvcMapping();
        MockWnMvcResponse resp = dispatch(m, req("GET", "/favicon.ico"));
        assertEquals(404, resp.status());
        assertEquals("text/html;charset=utf-8", resp.header("Content-Type"));
    }

    /**
     * T-80 未知 HTTP 方法：按未命中回退（不抛异常）；/o 前缀 → 404 JSON。
     */
    @Test
    public void t80_unknown_method_fallback() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/o/fetch", FakeController.class);
        MockWnMvcResponse resp = dispatch(m, req("WEIRD", "/o/fetch"));
        assertEquals(404, resp.status());
        assertEquals("application/json;charset=utf-8", resp.header("Content-Type"));
    }

    /**
     * T-81 API 前缀分流优先于静态后缀：/o/x.js 未命中仍返回 JSON 404。
     */
    @Test
    public void t81_api_prefix_wins_over_suffix() {
        WnMvcMapping m = new WnMvcMapping();
        WnMvcFactory factory = newFactory();
        boolean[] served = {false};
        WnMvcDispatcher d = new WnMvcDispatcher(m,
                                                factory,
                                                NO_FILTERS,
                                                NO_FILTERS,
                                                ctx -> {
                                                    served[0] = true;
                                                    return true;
                                                });
        MockWnMvcResponse resp = dispatch(d, factory, req("GET", "/o/x.js"));
        assertFalse(served[0]);
        assertEquals(404, resp.status());
        assertEquals("application/json;charset=utf-8", resp.header("Content-Type"));
    }

}
