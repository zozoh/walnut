package com.site0.walnut.web2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.WnVersion;
import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.session.WnSimpleSession;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.mvc.MockWnMvcRequest;
import com.site0.walnut.mvc.MockWnMvcResponse;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcController;
import com.site0.walnut.mvc.WnMvcDispatcher;
import com.site0.walnut.mvc.WnMvcFactory;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcFilterException;
import com.site0.walnut.mvc.WnMvcHttpMethod;
import com.site0.walnut.mvc.WnMvcMapping;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcResponse;
import com.site0.walnut.mvc.WnMvcStaticService;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.WnConfig;
import com.site0.walnut.web2.controller.WebRootController;
import com.site0.walnut.web2.filter.atpath.AsUsrFilter;
import com.site0.walnut.web2.filter.atpath.SessionCheckFilter;
import com.site0.walnut.web2.filter.global.CleanupFilter;
import com.site0.walnut.web2.filter.router.DmnRule;
import com.site0.walnut.web2.filter.router.DomainRouteFilter;

import com.site0.walnut.mvc.WnDefaultStaticService;
import com.site0.walnut.web2.support.WnAuthLookup;
import com.site0.walnut.web2.support.WnPathRewriter;
import com.site0.walnut.mvc.WnStaticResolver;

/**
 * RM-NUTZ-05 T14：八组组合测试（JUnit，不真起服务器）。
 * <p>用例编号：T14-1~T14-8，每组含正常路径与失败/边界路径。</p>
 */
public class Jetty05ChainTest {

    private static final WnMvcFilter[] NO = new WnMvcFilter[0];

    // ======================================================================
    // T14-1：head 全局 + 路由过滤器顺序 / 前置失败中断
    // ======================================================================

    @Test
    public void t14_1_head_order() {
        List<String> trace = new ArrayList<>();
        TraceController.TRACE = trace;
        WnMvcFilter g1 = c -> trace.add("g1");
        WnMvcFilter g2 = c -> trace.add("g2");
        WnMvcFilter r1 = c -> trace.add("r1");
        WnMvcFilter t1 = c -> trace.add("t1");
        WnMvcFilter t2 = c -> trace.add("t2");

        WnMvcFactory f = new WnMvcFactory();
        WnMvcMapping m = f.mapping();
        m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY},
                "/x",
                TraceController.class,
                new WnMvcFilter[]{r1},
                new WnMvcFilter[]{t1});
        WnMvcDispatcher d = new WnMvcDispatcher(m,
                                               f,
                                               new WnMvcFilter[]{g1, g2},
                                               new WnMvcFilter[]{t2});
        MockWnMvcResponse resp = dispatch(d, f, req("GET", "/x", null));
        assertEquals(Arrays.asList("g1", "g2", "r1", "ctl", "t1", "t2"), trace);
        assertEquals(200, resp.status());
    }

    @Test
    public void t14_1_head_fail_closed() {
        List<String> trace = new ArrayList<>();
        TraceController.TRACE = trace;
        WnMvcFilter g1 = c -> trace.add("g1");
        WnMvcFilter g2 = c -> {
            trace.add("g2");
            throw new WnMvcFilterException(403, "e.se.forbid", "no");
        };
        WnMvcFilter t1 = c -> trace.add("t1");
        WnMvcFilter t2 = c -> trace.add("t2");

        WnMvcFactory f = new WnMvcFactory();
        WnMvcMapping m = f.mapping();
        m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY},
                "/x",
                TraceController.class,
                NO,
                new WnMvcFilter[]{t1});
        WnMvcDispatcher d = new WnMvcDispatcher(m,
                                               f,
                                               new WnMvcFilter[]{g1, g2},
                                               new WnMvcFilter[]{t2});
        MockWnMvcResponse resp = dispatch(d, f, req("GET", "/x", null));
        assertEquals(Arrays.asList("g1", "g2", "t1", "t2"), trace);
        assertEquals(403, resp.status());
        assertTrue(resp.bodyString().contains("403"));
    }

    // ======================================================================
    // T14-2：SessionCheck / AsUsr fail-closed
    // ======================================================================

    @Test
    public void t14_2_session_check_fail_closed() {
        SessionCheckFilter flt = new SessionCheckFilter(null, true, false);
        WnMvcContext ctx = ctx(req("GET", "/o/x", null), new MockWnMvcResponse());
        try {
            flt.doFilter(ctx);
            fail("expected 401");
        }
        catch (WnMvcFilterException e) {
            assertEquals(401, e.getStatus());
        }
    }

    @Test
    public void t14_2_session_check_pass_and_redirect() {
        // 有会话 → 通过
        WnMvcContext ok = ctx(req("GET", "/o/x", null), new MockWnMvcResponse());
        ok.setSession(session("app"));
        new SessionCheckFilter(null, true, false).doFilter(ok);
        assertNotNull(Wn.WC().getSession());

        // 无会话 + 非 ajax → 302 到 /
        MockWnMvcResponse resp = new MockWnMvcResponse();
        WnMvcContext no = ctx(req("GET", "/o/x", null), resp);
        try {
            new SessionCheckFilter(null, false, false).doFilter(no);
            fail("expected redirect");
        }
        catch (WnMvcFilterException e) {
            assertEquals(302, e.getStatus());
        }
        assertEquals("/", resp.redirectedTo());
    }

    @Test
    public void t14_2_asusr() {
        WnUser root = new WnSimpleUser("root");
        FakeAuth auth = new FakeAuth();
        auth.user = root;
        WnMvcContext ctx = ctx(req("GET", "/o/x", null), new MockWnMvcResponse());
        new AsUsrFilter(auth, "root").doFilter(ctx);
        assertEquals("root", ctx.attrs().get("wn.mvc.filter.asusr"));

        FakeAuth none = new FakeAuth();
        try {
            new AsUsrFilter(none, "root").doFilter(ctx);
            fail("expected 403");
        }
        catch (WnMvcFilterException e) {
            assertEquals(403, e.getStatus());
        }
    }

    // ======================================================================
    // T14-3：Cleanup 在前置失败时也执行
    // ======================================================================

    @Test
    public void t14_3_cleanup_on_failure() {
        Wn.WC().setTicket("t-clean");
        WnMvcFactory f = new WnMvcFactory();
        WnMvcMapping m = f.mapping();
        m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY},
                "/x",
                TraceController.class,
                NO,
                NO);
        WnMvcFilter boom = c -> {
            throw new WnMvcFilterException(401, "e.se.noexists", "no");
        };
        WnMvcDispatcher d = new WnMvcDispatcher(m,
                                               f,
                                               new WnMvcFilter[]{boom},
                                               new WnMvcFilter[]{new CleanupFilter()});
        dispatch(d, f, req("GET", "/x", null));
        assertFalse(Wn.WC().hasTicket());
    }

    // ======================================================================
    // T14-4：未命中三级分流
    // ======================================================================

    @Test
    public void t14_4_fallback() throws IOException {
        WnMvcFactory f = new WnMvcFactory();
        WnMvcMapping m = f.mapping();
        WnMvcStaticService ss = ctx -> {
            if (ctx.request().getPath().endsWith(".js")) {
                try {
                    ctx.response().contentType("text/javascript");
                    ctx.response().writer().write("JS");
                    ctx.response().writer().flush();
                    ctx.response().flush();
                }
                catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                return true;
            }
            return false;
        };
        WnMvcDispatcher d = new WnMvcDispatcher(m, f, NO, NO, ss);

        MockWnMvcResponse api = dispatch(d, f, req("GET", "/o/x", null));
        assertEquals(404, api.status());
        assertTrue(api.bodyString().contains("e.mvc.nofound"));
        assertFalse(api.bodyString().contains("<html"));

        MockWnMvcResponse js = dispatch(d, f, req("GET", "/a.js", null));
        assertEquals(200, js.status());
        assertEquals("JS", js.bodyString());

        MockWnMvcResponse css = dispatch(d, f, req("GET", "/a.css", null));
        assertEquals(404, css.status());
        assertTrue(css.bodyString().contains("<html"));

        MockWnMvcResponse page = dispatch(d, f, req("GET", "/foo", null));
        assertEquals(404, page.status());
        assertTrue(page.bodyString().contains("<html"));
        assertTrue(page.bodyString().contains("404"));
    }

    // ======================================================================
    // T14-5：错误页状态映射 + 页面/API 区分
    // ======================================================================

    @Test
    public void t14_5_error_pages() {
        // 页面 401
        MockWnMvcResponse page = dispatchWith(Boom401Controller.class, "/p", "GET");
        assertEquals(401, page.status());
        assertTrue(page.header("Content-Type").toLowerCase().contains("text/html"));
        assertTrue(page.bodyString().contains("401"));

        // API 401（JSON）
        MockWnMvcResponse api = dispatchWith(Boom401Controller.class, "/o/p", "GET");
        assertEquals(401, api.status());
        assertTrue(api.header("Content-Type").toLowerCase().contains("application/json"));
        assertTrue(api.bodyString().contains("\"ok\":false"));
        assertTrue(api.bodyString().contains("e.se.noexists"));

        // 页面 500
        MockWnMvcResponse e500 = dispatchWith(Boom500Controller.class, "/p", "GET");
        assertEquals(500, e500.status());
        assertTrue(e500.bodyString().contains("500"));
    }

    // ======================================================================
    // T14-6：WnDefaultStaticService（C 记录静态站点）命中 / 不存在
    // ======================================================================

    @Test
    public void t14_6_static_service_no_c_record() {
        // 无 wn_www_grp/wn_www_site 属性 → 未命中（webRoot 文件来源已随 WebContent 弃用移除）
        WnDefaultStaticService svc = new WnDefaultStaticService(null, null);
        MockWnMvcResponse miss = new MockWnMvcResponse();
        assertFalse(svc.serve(ctx(req("GET", "/nope.txt", null), miss)));
    }

    @Test
    public void t14_6_static_c_record() {
        FakeStaticResolver r = new FakeStaticResolver();
        r.dir = new WnIoObj();
        WnIoObj obj = new WnIoObj();
        obj.name("index.html");
        obj.mime("text/html");
        r.files.put("index.html", obj);
        r.data.put("index.html", "C-INDEX".getBytes(StandardCharsets.UTF_8));

        WnDefaultStaticService svc = new WnDefaultStaticService(r, null);
        MockWnMvcRequest req = req("GET", "/", null);
        req.getAttrs().put("wn_www_grp", "demo");
        req.getAttrs().put("wn_www_site", "/home/demo/www");
        MockWnMvcResponse resp = new MockWnMvcResponse();
        assertTrue(svc.serve(ctx(req, resp)));
        assertEquals("C-INDEX", resp.bodyString());
    }

    // ======================================================================
    // T14-7：DomainRouteFilter 域映射 / 路径重写
    // ======================================================================

    @Test
    public void t14_7_domain_route_a() {
        FakeDomainResolver r = new FakeDomainResolver();
        r.obj = domain("A", "demo", "/home/demo/www");
        DomainRouteFilter flt = new DomainRouteFilter(Pattern.compile("^$"),
                                                      r,
                                                      Arrays.asList(DmnRule.of("^/(.*)$",
                                                                              "/www/${grp}/${1}")));
        MockWnMvcRequest base = MockWnMvcRequest.builder()
                                                .method("GET")
                                                .path("/foo")
                                                .host("x.com")
                                                .build();
        MutableTestRequest req = new MutableTestRequest(base);
        WnMvcContext ctx = ctx(req, new MockWnMvcResponse());
        flt.doFilter(ctx);
        assertEquals("/www/demo/foo", req.getPath());
        assertEquals("demo", req.getAttr("wn_www_grp"));
        assertEquals("/home/demo/www", req.getAttr("wn_www_site"));
    }

    @Test
    public void t14_7_domain_route_b_c_and_none() {
        // C 记录：静态标记，路径不变
        FakeDomainResolver rc = new FakeDomainResolver();
        rc.obj = domain("C", "demo", "/home/demo/www");
        DomainRouteFilter fc = new DomainRouteFilter(Pattern.compile("^$"), rc, null);
        MockWnMvcRequest base = req("GET", "/about", null);
        MutableTestRequest req = new MutableTestRequest(base);
        WnMvcContext ctx = ctx(req, new MockWnMvcResponse());
        fc.doFilter(ctx);
        assertEquals("yes", req.getAttr("wn_www_static"));
        assertEquals("/about", req.getPath());

        // 无记录：不写属性、不改路径
        DomainRouteFilter fn = new DomainRouteFilter(Pattern.compile("^$"),
                                                     new FakeDomainResolver(),
                                                     null);
        MutableTestRequest req2 = new MutableTestRequest(req("GET", "/foo", null));
        fn.doFilter(ctx(req2, new MockWnMvcResponse()));
        assertEquals("/foo", req2.getPath());
        assertNull(req2.getAttr("wn_www_grp"));
    }

    // ======================================================================
    // T14-8：WebRootController 各路径
    // ======================================================================

    @Test
    public void t14_8_root_redirects() {
        WnConfig conf = conf();
        WnServiceHolder holder = new WnServiceHolder();
        holder.setConfig(conf);

        MockWnMvcResponse no = new MockWnMvcResponse();
        dispatchRoot(holder, req("GET", "/", null), no);
        assertEquals("/a/login/", no.redirectedTo());

        WnMvcContext ctx = ctx(req("GET", "/", null), new MockWnMvcResponse());
        ctx.setSession(session("myapp"));
        WnMvcView v = new WebRootController().handle(ctx);
        v.render(ctx);
        assertEquals("/a/open/myapp", ((MockWnMvcResponse) ctx.response()).redirectedTo());
    }

    @Test
    public void t14_8_favicon_version_runtime() {
        WnServiceHolder holder = new WnServiceHolder();
        holder.setConfig(conf());

        MockWnMvcResponse ico = new MockWnMvcResponse();
        dispatchRoot(holder, req("GET", "/favicon", null), ico);
        assertEquals(200, ico.status());
        assertTrue(ico.bodyString().length() > 0);
        assertTrue(ico.header("Content-Type").contains("image/x-icon"));

        // /favicon.ico 与 /favicon 同处理（2026-11-11 用户拍板 B 方案：浏览器自动请求）
        MockWnMvcResponse ico2 = new MockWnMvcResponse();
        dispatchRoot(holder, req("GET", "/favicon.ico", null), ico2);
        assertEquals(200, ico2.status());
        assertTrue(ico2.bodyString().length() > 0);
        assertTrue(ico2.header("Content-Type").contains("image/x-icon"));

        MockWnMvcResponse ver = new MockWnMvcResponse();
        dispatchRoot(holder, req("GET", "/u/version", null), ver);
        assertTrue(ver.bodyString().contains(WnVersion.getName()));

        MockWnMvcResponse rt = new MockWnMvcResponse();
        dispatchRoot(holder, req("GET", "/u/runtime", null), rt);
        assertEquals(200, rt.status());
        assertTrue(rt.header("Content-Type").contains("application/json"));
    }

    @Test
    public void t14_8_any_404() {
        // RM-NUTZ-14：webRoot 静态文件来源已弃用（Walnut 无 webRoot）；
        // 静态命中场景由 t14_6_static_c_record 覆盖，此处只锁未命中 → 404
        WnMvcContext miss = ctx(req("GET", "/nope", null), new MockWnMvcResponse());
        try {
            new WebRootController().handle(miss);
            fail("expected 404");
        }
        catch (WnMvcFilterException e) {
            assertEquals(404, e.getStatus());
        }
    }

    // ======================================================================
    // helpers
    // ======================================================================

    private static MockWnMvcRequest req(String method, String path, String query) {
        return MockWnMvcRequest.builder().method(method).path(path).query(query).build();
    }

    private static WnMvcContext ctx(WnMvcRequest req, WnMvcResponse resp) {
        WnMvcContext ctx = new WnMvcFactory().newContext(req, resp);
        // 模拟 web2 生产路径（CreateMvcContextFilter 会打此标）
        ctx.attrs().put("wn.mvc.error.page", Boolean.TRUE);
        return ctx;
    }

    private static MockWnMvcResponse dispatch(WnMvcDispatcher d,
                                              WnMvcFactory f,
                                              WnMvcRequest req) {
        MockWnMvcResponse resp = new MockWnMvcResponse();
        WnMvcContext ctx = f.newContext(req, resp);
        ctx.attrs().put("wn.mvc.error.page", Boolean.TRUE);
        d.dispatch(ctx);
        return resp;
    }

    private static MockWnMvcResponse dispatchWith(Class<? extends WnMvcController> ctl,
                                                  String path,
                                                  String method) {
        WnMvcFactory f = new WnMvcFactory();
        WnMvcMapping m = f.mapping();
        m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY}, path, ctl, NO, NO);
        WnMvcDispatcher d = new WnMvcDispatcher(m, f, NO, NO);
        return dispatch(d, f, req(method, path, null));
    }

    private static void dispatchRoot(WnServiceHolder holder,
                                     MockWnMvcRequest req,
                                     MockWnMvcResponse resp) {
        WnMvcFactory f = new WnMvcFactory().services(holder);
        WnMvcMapping m = f.mapping();
        m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY},
                req.getPath(),
                WebRootController.class,
                NO,
                NO);
        WnMvcDispatcher d = new WnMvcDispatcher(m, f, NO, NO);
        d.dispatch(f.newContext(req, resp));
    }

    private static WnConfig conf() {
        return new WnConfig("conf/web.properties");
    }

    private static WnSession session(String open) {
        WnSimpleSession se = new WnSimpleSession(new WnSimpleUser("root"), 3600);
        se.setTicket("t-" + open);
        se.setExpiAt(Wn.now() + 60000);
        se.setEnv(Wlang.map("OPEN", open));
        return se;
    }

    private static WnObj domain(String type, String grp, String site) {
        WnIoObj o = new WnIoObj();
        o.type(type);
        o.put("domain", grp);
        o.put("site", site);
        return o;
    }

    // ======================================================================
    // fakes
    // ======================================================================

    public static class TraceController implements WnMvcController {

        static List<String> TRACE;

        @Override
        public WnMvcView handle(WnMvcContext ctx) {
            if (null != TRACE)
                TRACE.add("ctl");
            return WnMvcView.none();
        }
    }

    public static class Boom401Controller implements WnMvcController {

        @Override
        public WnMvcView handle(WnMvcContext ctx) {
            throw new WnMvcFilterException(401, "e.se.noexists", "no session");
        }
    }

    public static class Boom500Controller implements WnMvcController {

        @Override
        public WnMvcView handle(WnMvcContext ctx) {
            throw new RuntimeException("boom");
        }
    }

    static class FakeAuth implements WnAuthLookup {

        WnSession session;

        WnUser user;

        @Override
        public WnSession getSession(String ticket) {
            return session;
        }

        @Override
        public void touchSession(WnSession session) {}

        @Override
        public WnUser checkUser(String name) {
            return user;
        }
    }

    static class FakeDomainResolver implements com.site0.walnut.web2.support.WnDomainResolver {

        WnObj obj;

        @Override
        public WnObj fetchDomain(String host) {
            return obj;
        }
    }

    static class FakeStaticResolver implements WnStaticResolver {

        WnObj dir;

        final Map<String, WnObj> files = new LinkedHashMap<>();

        final Map<String, byte[]> data = new LinkedHashMap<>();

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
            return null == obj ? null : data.get(obj.name());
        }
    }

    /**
     * 可重写路径的请求（DomainRouteFilter 测试用）。
     */
    static class MutableTestRequest implements WnMvcRequest, WnPathRewriter {

        private final MockWnMvcRequest base;

        private String path;

        MutableTestRequest(MockWnMvcRequest base) {
            this.base = base;
            this.path = base.getPath();
        }

        @Override
        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getMethod() {
            return base.getMethod();
        }

        @Override
        public String getQueryString() {
            return base.getQueryString();
        }

        @Override
        public NutMap queryStringMap() {
            return base.queryStringMap();
        }

        @Override
        public NutMap getPathParams() {
            return base.getPathParams();
        }

        @Override
        public List<String> getPathArgs() {
            return base.getPathArgs();
        }

        @Override
        public String getHeader(String name) {
            return base.getHeader(name);
        }

        @Override
        public NutMap getHeaders() {
            return base.getHeaders();
        }

        @Override
        public String getCookie(String name) {
            return base.getCookie(name);
        }

        @Override
        public NutMap getCookieMap() {
            return base.getCookieMap();
        }

        @Override
        public Object getAttr(String name) {
            return base.getAttr(name);
        }

        @Override
        public NutMap getAttrs() {
            return base.getAttrs();
        }

        @Override
        public java.io.InputStream getInputStream() {
            return base.getInputStream();
        }

        @Override
        public java.io.Reader getReader() {
            return base.getReader();
        }

        @Override
        public String getInputJson() {
            return base.getInputJson();
        }

        @Override
        public NutMap getInputParams() {
            return base.getInputParams();
        }

        @Override
        public List<NutMap> getUploadParts() {
            return base.getUploadParts();
        }

        @Override
        public String getContentType() {
            return base.getContentType();
        }

        @Override
        public String getRemoteAddr() {
            return base.getRemoteAddr();
        }

        @Override
        public String getRemoteHost() {
            return base.getRemoteHost();
        }

        @Override
        public int getRemotePort() {
            return base.getRemotePort();
        }

        @Override
        public String getScheme() {
            return base.getScheme();
        }

        @Override
        public String getHost() {
            return base.getHost();
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            return base.unwrap(type);
        }
    }

}
