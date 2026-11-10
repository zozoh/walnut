package com.site0.walnut.web2;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcDispatcher;
import com.site0.walnut.mvc.WnMvcFactory;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcHttpMethod;
import com.site0.walnut.mvc.WnMvcMapping;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcResponse;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.web2.filter.global.CorsFilter;

/**
 * RM-NUTZ-16 统一 CORS 过滤器用例：
 * <ul>
 * <li>t01 CorsFilter 直接加四个默认头</li>
 * <li>t02 OPTIONS 预检统一短路：204 + 四头 + 不进 controller（链级，生产与单测同一路径）</li>
 * <li>t03 /api/** 的 OPTIONS 不短路（只加头、状态不动，留给 HttpApi 引擎应答）</li>
 * <li>t04 普通 GET 链集成：响应带四头且业务照常执行</li>
 * </ul>
 */
public class Jetty16CorsTest {

    private static final AtomicInteger CONTROLLER_CALLS = new AtomicInteger(0);

    /**
     * 哑控制器：计数是否被调用。
     */
    public static class DummyController extends AbstractWnController {
        @Override
        public WnMvcView handle(WnMvcContext ctx) {
            CONTROLLER_CALLS.incrementAndGet();
            return WnMvcView.none();
        }
    }

    private void assertDefaultCorsHeaders(WnMvcResponse resp) {
        assertEquals("*", resp.header("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, PATCH",
                     resp.header("Access-Control-Allow-Methods"));
        assertEquals("Origin, Content-Type, Accept, X-Requested-With, X-Walnut-Ticket",
                     resp.header("Access-Control-Allow-Headers"));
        assertEquals("true", resp.header("Access-Control-Allow-Credentials"));
    }

    private WnMvcDispatcher newDispatcher() {
        WnMvcMapping mapping = new WnMvcMapping();
        mapping.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY},
                      "/a/run/?/**",
                      DummyController.class,
                      new WnMvcFilter[0],
                      new WnMvcFilter[0]);
        return new WnMvcDispatcher(mapping,
                                   new WnMvcFactory(),
                                   new WnMvcFilter[]{new CorsFilter()},
                                   new WnMvcFilter[0],
                                   null);
    }

    private WnMvcContext ctx(WnMvcFactory mvc, String method, String path) {
        WnMvcRequest req = com.site0.walnut.mvc.MockWnMvcRequest.builder()
                .method(method)
                .path(path)
                .build();
        return mvc.newContext(req, new com.site0.walnut.mvc.MockWnMvcResponse());
    }

    @Test
    public void t01_cors_filter_adds_default_headers() throws Exception {
        WnMvcFactory mvc = new WnMvcFactory();
        WnMvcContext c = ctx(mvc, "GET", "/a/run/demo");
        new CorsFilter().doFilter(c);
        assertDefaultCorsHeaders(c.response());
    }

    @Test
    public void t02_options_preflight_short_circuited() throws Exception {
        WnMvcFactory mvc = new WnMvcFactory();
        WnMvcContext c = ctx(mvc, "OPTIONS", "/a/run/demo");
        CONTROLLER_CALLS.set(0);
        newDispatcher().dispatch(c);
        assertEquals(204, c.response().status());
        assertDefaultCorsHeaders(c.response());
        assertEquals(0, CONTROLLER_CALLS.get());
    }

    @Test
    public void t03_api_options_not_short_circuited() throws Exception {
        WnMvcFactory mvc = new WnMvcFactory();
        WnMvcContext c = ctx(mvc, "OPTIONS", "/api/root/echo");
        new CorsFilter().doFilter(c);
        // 不抛短路异常即通过；只加了默认头，状态未动（保持 mock 默认 200）
        assertDefaultCorsHeaders(c.response());
        assertEquals(200, c.response().status());
    }

    @Test
    public void t04_chain_integration_normal_get() throws Exception {
        WnMvcFactory mvc = new WnMvcFactory();
        WnMvcContext c = ctx(mvc, "GET", "/a/run/demo");
        CONTROLLER_CALLS.set(0);
        newDispatcher().dispatch(c);
        assertEquals(1, CONTROLLER_CALLS.get());
        assertDefaultCorsHeaders(c.response());
    }

}
