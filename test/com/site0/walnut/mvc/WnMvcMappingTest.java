package com.site0.walnut.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.site0.walnut.mvc.WnMvcMapping.Route;
import com.site0.walnut.mvc.fake.FakeController;

/**
 * T2 路由/通配/多路径/方法约束测试（特性矩阵 §3.1 路径通配、§2 方法约束）。
 *
 * <p>每个用到的路径/方法特性一个用例，用例 ID 见函数注释。</p>
 */
public class WnMvcMappingTest extends WnMvcTestBase {

    private static WnMvcRequest req(String method, String path) {
        return MockWnMvcRequest.builder().method(method).path(path).build();
    }

    private static WnMvcHttpMethod m(String method) {
        return WnMvcHttpMethod.valueOf(method);
    }

    /**
     * T-01 精确路径：命中返回路由；路径不同不命中。
     */
    @Test
    public void t01_exact_path() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/u/version", FakeController.class);
        assertNotNull(m.match(m("GET"), req("GET", "/u/version")));
        assertNull(m.match(m("GET"), req("GET", "/u/versionx")));
        assertNull(m.match(m("GET"), req("GET", "/u")));
    }

    /**
     * T-02 `?` 单段捕获：按出现顺序进 pathArgs。
     */
    @Test
    public void t02_qmark_capture_to_args() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/a/captcha/get/?", FakeController.class);
        WnMvcRequest req = req("GET", "/a/captcha/get/demo.com");
        Route r = m.match(m("GET"), req);
        assertNotNull(r);
        assertEquals(1, req.getPathArgs().size());
        assertEquals("demo.com", req.getPathArgs().get(0));
        assertTrue(req.getPathParams().isEmpty());
    }

    /**
     * T-03 `{name}` 命名捕获：进 pathParams，不进 pathArgs。
     */
    @Test
    public void t03_named_capture_to_params() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/a/open/{appName}", FakeController.class);
        WnMvcRequest req = req("GET", "/a/open/myapp");
        Route r = m.match(m("GET"), req);
        assertNotNull(r);
        assertEquals("myapp", req.getPathParams().getString("appName"));
        assertTrue(req.getPathArgs().isEmpty());
    }

    /**
     * T-04 `*` 单段通配：匹配一段但不捕获。
     */
    @Test
    public void t04_star_no_capture() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/o/content/*", FakeController.class);
        WnMvcRequest req = req("GET", "/o/content/a.txt");
        assertNotNull(m.match(m("GET"), req));
        assertTrue(req.getPathArgs().isEmpty());
        assertTrue(req.getPathParams().isEmpty());
        // 只匹配一段
        assertNull(m.match(m("GET"), req("GET", "/o/content/a/b.txt")));
    }

    /**
     * T-05 `**` 余段捕获：多段按 `/` 拼接进 pathArgs。
     */
    @Test
    public void t05_dstar_join_capture() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/a/login/**", FakeController.class);
        WnMvcRequest req = req("GET", "/a/login/x/y/z.html");
        assertNotNull(m.match(m("GET"), req));
        assertEquals(1, req.getPathArgs().size());
        assertEquals("x/y/z.html", req.getPathArgs().get(0));
    }

    /**
     * T-06 `**` 零段匹配：模式 `/a/login/**` 也命中 `/a/login`，捕获为空串。
     */
    @Test
    public void t06_dstar_zero_segment() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/a/login/**", FakeController.class);
        WnMvcRequest req = req("GET", "/a/login");
        assertNotNull(m.match(m("GET"), req));
        assertEquals(1, req.getPathArgs().size());
        assertEquals("", req.getPathArgs().get(0));
    }

    /**
     * T-07 `?` + `**` 组合（HttpApiModule `/api/?/**` 形态）：`?` 先进 args，`**` 余段随后。
     */
    @Test
    public void t07_qmark_then_dstar() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/api/?/**", FakeController.class);
        WnMvcRequest req = req("GET", "/api/root/path/to/api");
        assertNotNull(m.match(m("GET"), req));
        assertEquals(2, req.getPathArgs().size());
        assertEquals("root", req.getPathArgs().get(0));
        assertEquals("path/to/api", req.getPathArgs().get(1));
    }

    /**
     * T-08 多路径数组等价：同一控制器注册多条模式，均可命中。
     * <p>Nutz 的 <code>@At({"/a","/b"})</code> 多路径数组在 WnMvc 用多次
     * {@link WnMvcMapping#route} 显式注册表达。</p>
     */
    @Test
    public void t08_multi_paths_same_controller() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/a/captcha/get", FakeController.class);
        route(m, WnMvcHttpMethod.GET, "/a/captcha/get/?", FakeController.class);
        assertNotNull(m.match(m("GET"), req("GET", "/a/captcha/get")));
        assertNotNull(m.match(m("GET"), req("GET", "/a/captcha/get/site")));
        assertEquals(2, m.size());
    }

    /**
     * T-09 类级前缀等价：同前缀逐条拼接注册。
     * <p>Nutz 的类级 <code>@At("/a")</code> 前缀在 WnMvc 无对应概念，
     * 装配方把前缀拼进每条模式（如 <code>/a/login</code>、<code>/a/me</code>）。</p>
     */
    @Test
    public void t09_class_prefix_by_explicit_join() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/a/login", FakeController.class);
        route(m, WnMvcHttpMethod.GET, "/a/me", FakeController.class);
        assertNotNull(m.match(m("GET"), req("GET", "/a/login")));
        assertNotNull(m.match(m("GET"), req("GET", "/a/me")));
        assertNull(m.match(m("GET"), req("GET", "/b/me")));
    }

    /**
     * T-10 方法约束 GET/POST/PUT：声明 POST 的模式，GET/PUT 不命中。
     */
    @Test
    public void t10_method_constraint() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.POST, "/u/do/login", FakeController.class);
        route(m, WnMvcHttpMethod.PUT, "/gu/tf", FakeController.class);
        assertNotNull(m.match(m("POST"), req("POST", "/u/do/login")));
        assertNull(m.match(m("GET"), req("GET", "/u/do/login")));
        assertNotNull(m.match(m("PUT"), req("PUT", "/gu/tf")));
        assertNull(m.match(m("POST"), req("POST", "/gu/tf")));
    }

    /**
     * T-11 多方法约束：一条路由声明 GET+POST 两个方法。
     */
    @Test
    public void t11_multi_methods() {
        WnMvcMapping m = new WnMvcMapping();
        m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.GET, WnMvcHttpMethod.POST},
                "/gu/tf",
                FakeController.class,
                NO_FILTERS,
                NO_FILTERS);
        assertNotNull(m.match(m("GET"), req("GET", "/gu/tf")));
        assertNotNull(m.match(m("POST"), req("POST", "/gu/tf")));
        assertNull(m.match(m("PUT"), req("PUT", "/gu/tf")));
    }

    /**
     * T-12 默认任意方法：ANY 匹配 GET/POST/DELETE 等全部方法。
     */
    @Test
    public void t12_any_method() {
        WnMvcMapping m = new WnMvcMapping();
        routeAny(m, "/a/me", FakeController.class);
        assertNotNull(m.match(m("GET"), req("GET", "/a/me")));
        assertNotNull(m.match(m("POST"), req("POST", "/a/me")));
        assertNotNull(m.match(m("DELETE"), req("DELETE", "/a/me")));
    }

    /**
     * T-13 OPTIONS 预检：声明 OPTIONS 的路由可命中；未声明时 ANY 兜底。
     */
    @Test
    public void t13_options_preflight() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.OPTIONS, "/o/fetch", FakeController.class);
        assertNotNull(m.match(m("OPTIONS"), req("OPTIONS", "/o/fetch")));
        assertNull(m.match(m("GET"), req("GET", "/o/fetch")));

        WnMvcMapping m2 = new WnMvcMapping();
        routeAny(m2, "/o/fetch", FakeController.class);
        assertNotNull(m2.match(m("OPTIONS"), req("OPTIONS", "/o/fetch")));
    }

    /**
     * T-14 先注册先匹配：具体模式 `/u/version` 先于 `/**` 注册时优先命中。
     */
    @Test
    public void t14_first_registered_first_match() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/u/version", FakeController.class);
        route(m, WnMvcHttpMethod.GET, "/**", FakeBornController2.class);
        Route r = m.match(m("GET"), req("GET", "/u/version"));
        assertEquals(FakeController.class, r.controllerType());
        Route r2 = m.match(m("GET"), req("GET", "/anything/else"));
        assertEquals(FakeBornController2.class, r2.controllerType());
    }

    /** T-14 辅助：另一个控制器类用于区分命中路由。 */
    public static class FakeBornController2 extends FakeController {}

    /**
     * T-15 根路径：模式 `/` 只命中 `/`；`/**` 命中含根在内的一切。
     */
    @Test
    public void t15_root_path() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/", FakeController.class);
        assertNotNull(m.match(m("GET"), req("GET", "/")));
        assertNull(m.match(m("GET"), req("GET", "/x")));

        WnMvcMapping m2 = new WnMvcMapping();
        route(m2, WnMvcHttpMethod.GET, "/**", FakeController.class);
        assertNotNull(m2.match(m("GET"), req("GET", "/")));
    }

    /**
     * T-16 路径忽略空段：连续斜杠/尾斜杠不产生空段。
     */
    @Test
    public void t16_ignore_empty_segments() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/a/me", FakeController.class);
        assertNotNull(m.match(m("GET"), req("GET", "/a/me/")));
        assertNotNull(m.match(m("GET"), req("GET", "//a//me")));
    }

    /**
     * T-17 has 快速判定：不产生绑定；patterns/size 反映注册内容。
     */
    @Test
    public void t17_has_patterns_size() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/u/version", FakeController.class);
        route(m, WnMvcHttpMethod.POST, "/u/do/login", FakeController.class);
        assertTrue(m.has(m("GET"), "/u/version"));
        assertFalse(m.has(m("POST"), "/u/version"));
        assertTrue(m.has(m("POST"), "/u/do/login"));
        assertFalse(m.has(m("GET"), "/no/such"));
        assertEquals(2, m.size());
        assertEquals("/u/version", m.patterns().get(0));
        assertEquals("/u/do/login", m.patterns().get(1));
    }

    /**
     * T-18 部分匹配不污染绑定：前一条路由部分匹配失败后，活容器仍为空。
     */
    @Test
    public void t18_partial_match_no_pollution() {
        WnMvcMapping m = new WnMvcMapping();
        route(m, WnMvcHttpMethod.GET, "/a/{x}/b/{y}", FakeController.class);
        route(m, WnMvcHttpMethod.GET, "/a/p/c/{y}", FakeController.class);
        WnMvcRequest req = req("GET", "/a/p/c/d");
        Route r = m.match(m("GET"), req);
        assertNotNull(r);
        // 命中第二条：只有 y=d；第一条的 x=p 不得残留
        assertNull(req.getPathParams().get("x"));
        assertEquals("d", req.getPathParams().getString("y"));
        assertEquals(1, req.getPathParams().size());
    }

}
