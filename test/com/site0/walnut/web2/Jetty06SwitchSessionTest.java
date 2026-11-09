package com.site0.walnut.web2;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.WnVersion;
import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.core.io.WnIoSaftyWrapper;
import com.site0.walnut.login.WnSimpleLoginApi;
import com.site0.walnut.login.role.WnRoleLoader;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.session.WnSimpleSession;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.mvc.MockWnMvcRequest;
import com.site0.walnut.mvc.MockWnMvcResponse;
import com.site0.walnut.mvc.WnMvcController;
import com.site0.walnut.mvc.WnMvcDispatcher;
import com.site0.walnut.mvc.WnMvcFactory;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcFilterException;
import com.site0.walnut.mvc.WnMvcHttpMethod;
import com.site0.walnut.mvc.WnMvcMapping;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.controller.SwitchSessionController;
import com.site0.walnut.web2.controller.WebRootController;
import com.site0.walnut.web2.filter.atpath.SessionCheckFilter;
import com.site0.walnut.web2.filter.global.CleanupFilter;
import com.site0.walnut.web2.filter.global.CreateMvcContextFilter;
import com.site0.walnut.web2.router.WnRoutes;
import com.site0.walnut.web2.support.WnLoginAuthLookup;

public class Jetty06SwitchSessionTest {

    @Rule
    public TemporaryFolder files = new TemporaryFolder();

    private FakeAuth auth;
    private WnConfig conf;
    private CountingFactory factory;
    private WnSimpleSession parent;
    private WnSimpleSession child;

    @Before
    public void setUp() {
        Wn.Ctx.clear();
        auth = new FakeAuth();
        parent = session("parent");
        child = session("child");
        child.setParentTicket(parent.getTicket());
        auth.sessions.put(parent.getTicket(), parent);
        auth.sessions.put(child.getTicket(), child);
        conf = new WnConfig("conf/web.properties");
        conf.set("app-root", files.getRoot().getAbsolutePath());
        conf.set("sys-entry-url", "/a/login/");
        conf.set("cookie_same_site", "Auto");
        WnServiceHolder holder = new WnServiceHolder();
        holder.setLoginApi(auth);
        holder.setConfig(conf);
        factory = new CountingFactory();
        factory.services(holder);
        WnRoutes.register(factory.mapping());
    }

    @After
    public void tearDown() {
        Wn.Ctx.clear();
    }

    @Test
    public void t01_enter_child_session_cookie_cors_and_ajax() {
        MockWnMvcRequest req = chse("seid=child&site=ignored&host=ignored")
                .header("X-Walnut-Ticket", "parent").build();
        MockWnMvcResponse resp = dispatch(req);
        assertSuccess(resp, child);
        assertEquals("SEID=child; Path=/;", resp.header("Set-Cookie"));
        assertEquals("*", resp.header("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, PATCH",
                     resp.header("Access-Control-Allow-Methods"));
        assertEquals("Origin, Content-Type, Accept, X-Requested-With, X-Walnut-Ticket",
                     resp.header("Access-Control-Allow-Headers"));
        assertEquals("true", resp.header("Access-Control-Allow-Credentials"));
        assertEquals(Arrays.asList("child"), auth.checked);
        assertEquals(1, factory.switchCalls);
        assertEquals(0, factory.rootCalls);
        assertFalse(Wn.WC().hasTicket());
    }

    @Test
    public void t02_exit_after_child_deleted() {
        auth.sessions.remove("child");
        parent.setChildTicket("child");
        MockWnMvcResponse resp = dispatch(chse("seid=parent&exit=true&old_seid=child")
                                             .cookie("SEID", "child").build());
        assertSuccess(resp, parent);
        assertEquals("SEID=parent; Path=/;", resp.header("Set-Cookie"));
    }

    @Test
    public void t03_legacy_boolean_values_and_ticket_sources() {
        for (String exit : new String[]{"", "%20", "false", "0", "off", "no"}) {
            assertSuccess(dispatch(chse("seid=child&exit=" + exit + "&_wn_ticket_=parent")
                                       .build()), child);
        }
        parent.setChildTicket("child");
        for (String exit : new String[]{"true", "1", "yes", "on"}) {
            assertSuccess(dispatch(chse("seid=parent&old_seid=child&exit=" + exit).build()),
                          parent);
        }
        assertSuccess(dispatch(chse("seid=child").cookie("SEID", "parent").build()), child);
    }

    @Test
    public void t04_self_rejected_before_target_lookup() {
        assertBusinessFailure(dispatch(chse("seid=parent")
                                       .header("X-Walnut-Ticket", "parent").build()),
                              "e.web.u.chse.self");
        assertTrue(auth.checked.isEmpty());
    }

    @Test
    public void t05_missing_and_unrelated_current_session() {
        assertBusinessFailure(dispatch(chse("seid=child").build()), "e.web.u.chse.NoSession");
        child.setParentTicket("other");
        assertBusinessFailure(dispatch(chse("seid=child")
                                       .header("X-Walnut-Ticket", "parent").build()),
                              "e.web.u.chse.NotMyChild");
    }

    @Test
    public void t06_exit_validation_errors() {
        assertBusinessFailure(dispatch(chse("seid=parent&exit=true").build()),
                              "e.web.u.chse.exit.NeedOldSEID");
        assertBusinessFailure(dispatch(chse("seid=parent&exit=true&old_seid=child").build()),
                              "e.web.u.chse.exit.NewSEWithoutChildTicket");
        parent.setChildTicket("other");
        assertBusinessFailure(dispatch(chse("seid=parent&exit=true&old_seid=child").build()),
                              "e.web.u.chse.exit.NewSEChildTicketNoMatched");
    }

    @Test
    public void t07_bad_target_preserves_error_reason_and_no_cookie() {
        MockWnMvcResponse resp = dispatch(chse("seid=missing")
                                         .header("X-Walnut-Ticket", "parent").build());
        assertBusinessFailure(resp, "e.auth.session.NoExists");
        assertEquals("missing", body(resp).getString("data"));
        assertBusinessFailure(dispatch(chse("").build()), "e.auth.session.NoExists");
    }

    @Test
    public void t08_cookie_configuration_compatibility() {
        for (String scheme : new String[]{"http", "https"}) {
            for (String mode : new String[]{"Auto", "none", "strict"}) {
                conf.set("cookie_same_site", mode);
                MockWnMvcResponse resp = dispatch(chse("seid=child").scheme(scheme)
                                                 .header("X-Walnut-Ticket", "parent").build());
                String suffix = "none".equals(mode) ? "; SameSite=None; Secure"
                        : "strict".equals(mode) ? "; SameSite=Strict" : "";
                assertEquals("SEID=child" + suffix + "; Path=/;", resp.header("Set-Cookie"));
                assertSuccess(resp, child);
            }
        }
    }

    @Test
    public void t09_session_filter_401_never_enters_controller() {
        SessionCheckFilter filter = new SessionCheckFilter(new WnLoginAuthLookup(auth), true, false);
        for (String ticket : new String[]{null, "bad", "expired"}) {
            WnSimpleSession expired = session("expired");
            expired.setExpiAt(1);
            auth.sessions.put("expired", expired);
            MockWnMvcRequest.Builder req = chse("seid=child");
            if (null != ticket)
                req.header("X-Walnut-Ticket", ticket);
            MockWnMvcResponse resp = dispatch(req.build(), filter);
            assertEquals(401, resp.status());
            assertEquals("e.se.noexists", body(resp).getString("errCode"));
            assertNull(resp.header("Set-Cookie"));
            assertFalse(Wn.WC().hasTicket());
        }
        assertEquals(0, factory.switchCalls);
        assertTrue(auth.checked.isEmpty());
    }

    @Test
    public void t10_simulated_filter_403_and_valid_session_pass() {
        WnMvcFilter deny = ctx -> {
            throw new WnMvcFilterException(403, "e.se.forbid", "denied");
        };
        MockWnMvcResponse resp = dispatch(chse("seid=child")
                                         .header("X-Walnut-Ticket", "parent").build(), deny);
        assertEquals(403, resp.status());
        assertEquals("e.se.forbid", body(resp).getString("errCode"));
        assertEquals(0, factory.switchCalls);
        assertTrue(auth.checked.isEmpty());
        assertNull(resp.header("Set-Cookie"));
        assertFalse(Wn.WC().hasTicket());
        assertSuccess(dispatch(chse("seid=child").header("X-Walnut-Ticket", "parent").build(),
                               new SessionCheckFilter(new WnLoginAuthLookup(auth), true, false)),
                      child);
        assertEquals(1, factory.switchCalls);
    }

    @Test
    public void t11_root_redirects_anonymous_valid_and_expired() {
        assertEquals("/a/login/", dispatch(req("GET", "/")).redirectedTo());
        parent.getEnv().put("OPEN", "demo");
        assertEquals("/a/open/demo", dispatch(MockWnMvcRequest.builder().path("/")
                                             .cookie("SEID", "parent").build()).redirectedTo());
        parent.getEnv().remove("OPEN");
        assertEquals("/a/open/wn.console", dispatch(MockWnMvcRequest.builder().path("/")
                                                   .cookie("SEID", "parent").build()).redirectedTo());
        parent.setExpiAt(1);
        conf.set("sys-entry-url", "/custom-login");
        assertEquals("/custom-login", dispatch(MockWnMvcRequest.builder().path("/")
                                               .cookie("SEID", "parent").build()).redirectedTo());
    }

    @Test
    public void t12_favicon_version_runtime() throws IOException {
        MockWnMvcResponse resp = dispatch(req("GET", "/favicon"));
        assertEquals(200, resp.status());
        assertEquals("image/x-icon", resp.header("Content-Type"));
        try (var in = WebRootController.class.getResourceAsStream("/com/site0/walnut/web/favicon.ico")) {
            assertNotNull(in);
            assertArrayEquals(in.readAllBytes(), resp.bodyBytes());
        }
        resp = dispatch(req("GET", "/u/version"));
        assertEquals(200, resp.status());
        assertEquals(WnVersion.getName(), resp.bodyString());
        assertEquals("text/plain;charset=utf-8", resp.header("Content-Type"));
        resp = dispatch(req("GET", "/u/runtime"));
        assertEquals(200, resp.status());
        assertTrue(resp.header("Content-Type").startsWith("application/json"));
        assertEquals(Json.toJson(null == Wn.getRuntime() ? new NutMap() : Wn.getRuntime()),
                     Json.toJson(body(resp)));
    }

    @Test
    public void t13_unknown_page_and_api_404() {
        for (String path : new String[]{"/missing", "/.well-known/appspecific/com.chrome.devtools.json",
                                        "/missing.css"}) {
            assertNotFound(dispatch(req("GET", path)), false);
        }
        for (String path : new String[]{"/u/missing", "/o/missing", "/api/missing", "/u/missing.css"}) {
            assertNotFound(dispatch(req("GET", path)), true);
        }
    }

    @Test
    public void t14_static_suffix_hit_and_miss_without_dispatcher_fallback() throws IOException {
        Files.writeString(files.newFile("site.css").toPath(), "body{}", StandardCharsets.UTF_8);
        MockWnMvcResponse resp = dispatch(req("GET", "/site.css"));
        assertEquals(200, resp.status());
        assertEquals("body{}", resp.bodyString());
        assertNotFound(dispatch(req("GET", "/absent.css")), false);
        assertEquals(2, factory.rootCalls);
    }

    @Test
    public void t15_c_record_index_static_and_missing_root() {
        FakeStaticIo io = new FakeStaticIo();
        factory.io(io);
        io.files.put("index.html", file("index.html", "text/html"));
        io.files.put("site.css", file("site.css", "text/css"));
        MockWnMvcResponse resp = dispatch(cRecord("/"));
        assertEquals(200, resp.status());
        assertEquals("index.html", resp.bodyString());
        resp = dispatch(cRecord("/site.css"));
        assertEquals(200, resp.status());
        assertEquals("text/css", resp.header("Content-Type"));
        assertEquals("site.css", resp.bodyString());
        assertNotFound(dispatch(cRecord("/missing.css")), false);
        io.files.clear();
        assertNotFound(dispatch(cRecord("/")), false);
    }

    @Test
    public void t16_route_order_exactly_two_and_get_only() {
        WnMvcMapping mapping = factory.mapping();
        assertEquals(Arrays.asList("/u/ajax/chse", "/**"), mapping.patterns());
        WnMvcMapping.Route route = mapping.match(WnMvcHttpMethod.GET, req("GET", "/u/ajax/chse"));
        assertEquals(SwitchSessionController.class, route.controllerType());
        assertArrayEquals(new WnMvcHttpMethod[]{WnMvcHttpMethod.GET}, route.methods());
        assertEquals(0, route.headFilters().length);
        route = mapping.match(WnMvcHttpMethod.GET, req("GET", "/"));
        assertEquals(WebRootController.class, route.controllerType());
        assertArrayEquals(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY}, route.methods());
        for (String method : new String[]{"POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"}) {
            route = mapping.match(WnMvcHttpMethod.valueOf(method), req(method, "/u/ajax/chse"));
            assertEquals(WebRootController.class, route.controllerType());
            assertNotFound(dispatch(req(method, "/u/ajax/chse")), true);
        }
        assertEquals(0, factory.switchCalls);
        assertTrue(auth.checked.isEmpty());
    }

    @Test
    public void t17_deprecated_routes_are_not_migrated() {
        for (String path : new String[]{"/u/do/login", "/u/do/login/ajax", "/u/do/logout", "/u/h/login.html"}) {
            assertNotFound(dispatch(req("GET", path)), true);
            assertNotFound(dispatch(req("POST", path)), true);
        }
        assertEquals(0, factory.switchCalls);
        assertTrue(auth.checked.isEmpty());
    }

    @Test
    public void t18_unexpected_auth_failure_is_not_business_success() {
        auth.failure = new IllegalStateException("test auth unavailable");
        MockWnMvcResponse resp = dispatch(chse("seid=child").build());
        assertEquals(500, resp.status());
        assertFalse(body(resp).getBoolean("ok"));
        assertNull(resp.header("Set-Cookie"));
        assertFalse(Wn.WC().hasTicket());
    }

    @Test
    public void t19_repeated_parameters_keep_legacy_binding() {
        assertSuccess(dispatch(chse("seid=child&exit=false&exit=true")
                               .header("X-Walnut-Ticket", "parent").build()), child);
        parent.setChildTicket("child");
        assertSuccess(dispatch(chse("seid=parent&exit=true&exit=false&old_seid=child").build()),
                      parent);
        MockWnMvcResponse resp = dispatch(chse("seid=child&seid=missing")
                                         .header("X-Walnut-Ticket", "parent").build());
        assertBusinessFailure(resp, "e.auth.session.NoExists");
        assertEquals("child,missing", body(resp).getString("data"));
        parent.setChildTicket("child,other");
        assertSuccess(dispatch(chse("seid=parent&exit=1&old_seid=child&old_seid=other").build()),
                      parent);
    }

    private MockWnMvcResponse dispatch(MockWnMvcRequest req, WnMvcFilter... filters) {
        List<WnMvcFilter> head = new ArrayList<>();
        head.add(new CreateMvcContextFilter(new WnLoginAuthLookup(auth), factory.io()));
        head.addAll(Arrays.asList(filters));
        WnMvcDispatcher dispatcher = new WnMvcDispatcher(factory.mapping(), factory,
                head.toArray(new WnMvcFilter[0]), new WnMvcFilter[]{new CleanupFilter()},
                ctx -> { throw new AssertionError("dispatcher fallback must not run"); });
        MockWnMvcResponse resp = new MockWnMvcResponse();
        dispatcher.dispatch(factory.newContext(req, resp));
        return resp;
    }

    private static MockWnMvcRequest.Builder chse(String query) {
        return MockWnMvcRequest.builder().method("GET").path("/u/ajax/chse").query(query);
    }

    private static MockWnMvcRequest req(String method, String path) {
        return MockWnMvcRequest.builder().method(method).path(path).build();
    }

    private static MockWnMvcRequest cRecord(String path) {
        return MockWnMvcRequest.builder().path(path).attr("wn_www_static", "yes")
                .attr("wn_www_grp", "demo").attr("wn_www_site", "/home/demo/www").build();
    }

    private static NutMap body(MockWnMvcResponse resp) {
        return Json.fromJson(NutMap.class, resp.bodyString());
    }

    private void assertSuccess(MockWnMvcResponse resp, WnSession se) {
        assertEquals(200, resp.status());
        assertAjaxHeaders(resp);
        NutMap actual = body(resp);
        assertTrue(actual.getBoolean("ok"));
        assertEquals(Json.toJson(se.toBean(auth)), Json.toJson(actual.get("data")));
        assertEquals(Json.toJson(new NutMap().setv("ok", true).setv("data", se.toBean(auth)),
                                 JsonFormat.nice()), resp.bodyString());
    }

    private static void assertBusinessFailure(MockWnMvcResponse resp, String code) {
        assertEquals(200, resp.status());
        assertAjaxHeaders(resp);
        NutMap actual = body(resp);
        assertFalse(actual.getBoolean("ok"));
        assertEquals(code, actual.getString("errCode"));
        assertEquals(code, actual.getString("msg"));
        assertNull(resp.header("Set-Cookie"));
        assertEquals("*", resp.header("Access-Control-Allow-Origin"));
    }

    private static void assertAjaxHeaders(MockWnMvcResponse resp) {
        assertEquals("text/json", resp.header("Content-Type"));
        assertEquals("no-cache", resp.header("Cache-Control"));
        assertEquals(Integer.toString(resp.bodyBytes().length), resp.header("Content-Length"));
    }

    private static void assertNotFound(MockWnMvcResponse resp, boolean api) {
        assertEquals(404, resp.status());
        if (api) {
            assertTrue(resp.header("Content-Type").startsWith("application/json"));
            assertEquals("e.mvc.nofound", body(resp).getString("errCode"));
            assertFalse(body(resp).getBoolean("ok"));
            assertFalse(resp.bodyString().contains("<html"));
        }
        else {
            assertTrue(resp.header("Content-Type").startsWith("text/html"));
            assertTrue(resp.bodyString().contains("<html"));
            assertTrue(resp.bodyString().contains("404"));
        }
    }

    private static WnSimpleSession session(String ticket) {
        WnSimpleSession se = new WnSimpleSession(new WnSimpleUser("tester"), 3600);
        se.setTicket(ticket);
        return se;
    }

    private static WnObj file(String name, String mime) {
        WnIoObj obj = new WnIoObj();
        obj.name(name);
        obj.mime(mime);
        return obj;
    }

    private static class CountingFactory extends WnMvcFactory {
        int switchCalls;
        int rootCalls;

        @Override
        public WnMvcController bornController(Class<? extends WnMvcController> type) {
            WnMvcController controller = super.bornController(type);
            return ctx -> {
                if (SwitchSessionController.class == type)
                    switchCalls++;
                if (WebRootController.class == type)
                    rootCalls++;
                return controller.handle(ctx);
            };
        }
    }

    private static class FakeAuth extends WnSimpleLoginApi {
        final Map<String, WnSession> sessions = new LinkedHashMap<>();
        final List<String> checked = new ArrayList<>();
        RuntimeException failure;

        FakeAuth() {
            super(null);
        }

        @Override
        public WnSession getSession(String ticket) {
            return sessions.get(ticket);
        }

        @Override
        public WnSession checkSession(String ticket) {
            checked.add(ticket);
            if (null != failure)
                throw failure;
            WnSession se = getSession(ticket);
            if (null == se)
                throw Er.create("e.auth.session.NoExists", ticket);
            return se;
        }

        @Override
        public void touchSession(WnSession se) {}

        @Override
        public WnRoleLoader roleLoader(WnSession se) {
            return null;
        }
    }

    private static class FakeStaticIo extends WnIoSaftyWrapper {
        final WnObj dir = new WnIoObj();
        final Map<String, WnObj> files = new LinkedHashMap<>();

        @Override
        public WnObj check(WnObj parent, String path) {
            assertEquals("/home/demo/www", path);
            return dir;
        }

        @Override
        public WnObj fetch(WnObj parent, String path) {
            assertSame(dir, parent);
            return files.get(path);
        }

        @Override
        public byte[] readBytes(WnObj obj) {
            return obj.name().getBytes(StandardCharsets.UTF_8);
        }
    }

}
