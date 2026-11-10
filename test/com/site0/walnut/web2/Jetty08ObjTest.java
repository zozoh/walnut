package com.site0.walnut.web2;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.core.io.WnIoSaftyWrapper;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.session.WnSimpleSession;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.login.WnSimpleLoginApi;
import com.site0.walnut.mvc.MockWnMvcRequest;
import com.site0.walnut.mvc.MockWnMvcResponse;
import com.site0.walnut.mvc.WnMvcDispatcher;
import com.site0.walnut.mvc.WnMvcFactory;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.controller.ObjAncestorsController;
import com.site0.walnut.web2.controller.ObjChildrenController;
import com.site0.walnut.web2.controller.ObjContentController;
import com.site0.walnut.web2.controller.ObjFetch2Controller;
import com.site0.walnut.web2.controller.ObjFetchController;
import com.site0.walnut.web2.controller.ObjQueryController;
import com.site0.walnut.web2.controller.ObjSaveStreamController;
import com.site0.walnut.web2.controller.ObjSaveTextController;
import com.site0.walnut.web2.controller.ObjUpdateController;
import com.site0.walnut.web2.controller.WebRootController;
import com.site0.walnut.web2.filter.global.CleanupFilter;
import com.site0.walnut.web2.filter.global.CreateMvcContextFilter;
import com.site0.walnut.web2.filter.global.CorsFilter;
import com.site0.walnut.web2.router.WnRoutes;
import com.site0.walnut.web2.support.WnLoginAuthLookup;

/**
 * RM-NUTZ-08：ObjModule 10 个非废弃入口的迁移测试。每入口 1 正常 + 1 失败； 重点覆盖：update 的 JSON body
 * 绑定（body 无 str → 报错、query str 被忽略）、 query 的 limit 上限/分页、find 的 _me 与 _
 * 键剔除、content 的下载视图与 Range、 saveText 的 base64、saveStream 的 mode r/s/a
 * 与原始流、OPTIONS 空 200。
 */
public class Jetty08ObjTest {

    private FakeIo io;
    private FakeAuth auth;
    private WnServiceHolder holder;
    private WnSimpleSession session;
    private WnMvcFactory factory;

    @Before
    public void setUp() {
        Wn.Ctx.clear();
        io = new FakeIo();
        auth = new FakeAuth();
        auth.addUser("root").setHomePath("/root");
        WnSimpleUser user = auth.addUser("tester");
        session = auth.newSession(user, "ticket");
        WnConfig conf = new WnConfig("conf/web.properties");
        holder = new WnServiceHolder();
        holder.setConfig(conf);
        holder.setLoginApi(auth);
        factory = new WnMvcFactory();
        factory.services(holder).io(io);
        WnRoutes.register(factory.mapping());
        // 对象树.fixture：8 个对象
        io.add("/home", WnRace.DIR, "");
        io.add("/home/tester", WnRace.DIR, "");
        io.add("/home/tester/main.txt", WnRace.FILE, "0123456789");
        io.add("/home/tester/find", WnRace.DIR, "");
        io.add("/home/tester/find/a.txt", WnRace.FILE, "aaa");
        io.add("/home/tester/find/b.txt", WnRace.FILE, "bbb");
        io.add("/other", WnRace.DIR, "");
        io.add("/other/outside.txt", WnRace.FILE, "out");
    }

    @After
    public void tearDown() {
        Wn.Ctx.clear();
    }

    // ------------------------------------------------------------------
    // fetch / fetch2
    // ------------------------------------------------------------------

    @Test
    public void t01_fetch_ok_with_path_and_axis() {
        // 默认不带 path/axis（先断言：axis 分支会照抄旧代码 o.put("ancestors", ans)
        // 原地写对象，fake io 共享对象会带上次的结果）
        MockWnMvcResponse resp = dispatch(signed("/o/fetch")
            .query("str=/home/tester/main.txt")
            .build());
        ok(resp);
        assertNull(body(resp).getAs("data", NutMap.class).get("ancestors"));
        resp = dispatch(signed("/o/fetch")
            .query("str=/home/tester/main.txt&path=true&axis=true")
            .build());
        ok(resp);
        NutMap data = body(resp).getAs("data", NutMap.class);
        assertEquals("main.txt", data.getString("nm"));
        assertEquals("/home/tester/main.txt", data.getString("ph"));
        List<NutMap> ans = data.getAsList("ancestors", NutMap.class);
        assertEquals(2, ans.size());
        assertEquals("home", ans.get(0).getString("nm"));
        assertEquals("tester", ans.get(1).getString("nm"));
    }

    @Test
    public void t02_fetch_failures() {
        // 未登录：ajaxSession → 401
        assertEquals(401,
                     dispatch(req("/o/fetch").query("str=/home/tester/main.txt")
                         .build()).status());
        // 对象不存在：业务错误 ajax 信封
        fail(dispatch(signed("/o/fetch").query("str=/no/such.txt").build()),
             "e.io.obj.noexists");
    }

    @Test
    public void t03_fetch2_ok_loads_path_without_axis() {
        MockWnMvcResponse resp = dispatch(signed("/o/fetch2")
            .query("str=/home/tester/main.txt")
            .build());
        ok(resp);
        NutMap data = body(resp).getAs("data", NutMap.class);
        assertEquals("main.txt", data.getString("nm"));
        assertEquals("/home/tester/main.txt", data.getString("ph"));
        assertNull(data.get("ancestors"));
    }

    @Test
    public void t04_fetch2_failures() {
        assertEquals(401,
                     dispatch(req("/o/fetch2")
                         .query("str=/home/tester/main.txt")
                         .build()).status());
        fail(dispatch(signed("/o/fetch2").query("str=/no/such.txt").build()),
             "e.io.obj.noexists");
    }

    // ------------------------------------------------------------------
    // ancestors
    // ------------------------------------------------------------------

    @Test
    public void t05_ancestors_ok() {
        MockWnMvcResponse resp = dispatch(signed("/o/ancestors")
            .query("str=/home/tester/main.txt")
            .build());
        ok(resp);
        List<NutMap> ans = body(resp).getAsList("data", NutMap.class);
        assertEquals(2, ans.size());
        assertEquals("home", ans.get(0).getString("nm"));
        assertEquals("tester", ans.get(1).getString("nm"));
    }

    @Test
    public void t06_ancestors_failures() {
        // 未登录：session 过滤器 → 302 重定向 /
        MockWnMvcResponse resp = dispatch(req("/o/ancestors")
            .query("str=/home/tester")
            .build());
        assertEquals(302, resp.status());
        assertEquals("/", resp.redirectedTo());
        fail(dispatch(signed("/o/ancestors").query("str=/no/such").build()),
             "e.io.obj.noexists");
    }

    // ------------------------------------------------------------------
    // update（JsonAdaptor：整个 body 即 JSON，query 完全忽略）
    // ------------------------------------------------------------------

    @Test
    public void t07_update_ok_appends_whole_body_map() {
        MockWnMvcResponse resp = dispatch(signed("/o/update").method("POST")
            .bodyJson("{str:'/home/tester/main.txt',custom:'v1'}")
            .build());
        ok(resp);
        WnObj o = io.objects.get("/home/tester/main.txt");
        assertEquals("v1", o.getString("custom"));
        // 未注解 NutMap map = 整个 body map（含 str 键），照抄 appendMeta
        assertEquals("/home/tester/main.txt", o.getString("str"));
    }

    @Test
    public void t08_update_missing_str_and_query_ignored() {
        // body 不含 str → 报错；即使 query string 带 str 也被忽略
        MockWnMvcResponse resp = dispatch(signed("/o/update").method("POST")
            .query("str=/home/tester/main.txt")
            .bodyJson("{custom:'v2'}")
            .build());
        fail(resp, "e.io.obj.noexists");
        assertNull(io.objects.get("/home/tester/main.txt").getString("custom"));
        // 未登录 → 302
        resp = dispatch(req("/o/update").method("POST")
            .bodyJson("{str:'/home/tester/main.txt'}")
            .build());
        assertEquals(302, resp.status());
    }

    // ------------------------------------------------------------------
    // query（ObjectPairInjector：仅 limit/skip/sort/list 绑定，其余忽略）
    // ------------------------------------------------------------------

    @Test
    public void t09_query_default_returns_plain_list() {
        MockWnMvcResponse resp = dispatch(signed("/o/query").build());
        ok(resp);
        List<NutMap> list = body(resp).getAsList("data", NutMap.class);
        assertEquals(io.objects.size(), list.size());
    }

    @Test
    public void t10_query_paging_limit_cap_and_params_ignored() {
        // limit=2 → 分页结构
        MockWnMvcResponse resp = dispatch(signed("/o/query").query("limit=2")
            .build());
        ok(resp);
        NutMap data = body(resp).getAs("data", NutMap.class);
        assertEquals(2, data.getAs("list", List.class).size());
        NutMap pager = data.getAs("pager", NutMap.class);
        assertEquals(2, pager.getInt("pageSize"));
        assertEquals(io.objects.size(), pager.getInt("totalCount"));
        // limit 上限 5000
        resp = dispatch(signed("/o/query").query("limit=6000").build());
        ok(resp);
        assertEquals(5000,
                     body(resp).getAs("data", NutMap.class)
                         .getAs("pager", NutMap.class)
                         .getInt("pageSize"));
        // 非字段参数（zzz/nm）被忽略：不作为查询条件
        resp = dispatch(signed("/o/query").query("limit=2&zzz=1&nm=a.txt")
            .build());
        ok(resp);
        assertEquals(io.objects.size(),
                     body(resp).getAs("data", NutMap.class)
                         .getAs("pager", NutMap.class)
                         .getInt("totalCount"));
    }

    @Test
    public void t11_query_failures() {
        MockWnMvcResponse resp = dispatch(req("/o/query").build());
        assertEquals(302, resp.status());
        assertEquals("/", resp.redirectedTo());
    }

    // ------------------------------------------------------------------
    // children
    // ------------------------------------------------------------------

    @Test
    public void t12_children_plain_list() {
        MockWnMvcResponse resp = dispatch(signed("/o/children")
            .query("str=/home/tester/find&pg=false")
            .build());
        ok(resp);
        List<NutMap> list = body(resp).getAsList("data", NutMap.class);
        assertEquals(2, list.size());
    }

    @Test
    public void t13_children_paging() {
        MockWnMvcResponse resp = dispatch(signed("/o/children")
            .query("str=/home/tester/find&pg=true")
            .build());
        ok(resp);
        NutMap data = body(resp).getAs("data", NutMap.class);
        assertEquals(2, data.getAs("list", List.class).size());
        NutMap pager = data.getAs("pager", NutMap.class);
        assertEquals(100, pager.getInt("pageSize"));
        assertEquals(2, pager.getInt("totalCount"));
    }

    @Test
    public void t14_children_failures() {
        // 缺 pg：旧语义 !paging NPE → 未知异常 → 500
        assertEquals(500,
                     dispatch(signed("/o/children")
                         .query("str=/home/tester/find")
                         .build()).status());
        // 未登录 → 302
        assertEquals(302,
                     dispatch(req("/o/children")
                         .query("str=/home/tester/find&pg=false")
                         .build()).status());
    }

    // content（JsonAdaptor + 下载视图）
    // ------------------------------------------------------------------

    @Test
    public void t18_content_download_with_download_name_and_force() {
        MockWnMvcResponse resp = dispatch(signed("/o/content/renamed.txt")
            .header("User-Agent", "Mozilla/5.0")
            .query("str=/home/tester/main.txt&d=force")
            .build());
        assertEquals(resp.bodyString(), 200, resp.status());
        assertEquals("0123456789", resp.bodyString());
        String cd = resp.header("Content-Disposition");
        assertNotNull(cd);
        assertTrue(cd.contains("attachment"));
        assertTrue(cd.contains("renamed.txt"));
        assertEquals(Wn.getEtag(io.objects.get("/home/tester/main.txt")),
                     resp.header("ETag"));
    }

    @Test
    public void t19_content_range_and_etag_304() {
        MockWnMvcResponse resp = dispatch(signed("/o/content/x.txt")
            .header("Range", "bytes=2-5")
            .query("str=/home/tester/main.txt")
            .build());
        assertEquals(206, resp.status());
        assertEquals("2345", resp.bodyString());
        assertEquals("bytes 2-5/10", resp.header("Content-Range"));
        resp = dispatch(signed("/o/content/x.txt")
            .header("If-None-Match",
                    Wn.getEtag(io.objects.get("/home/tester/main.txt")))
            .query("str=/home/tester/main.txt")
            .build());
        assertEquals(304, resp.status());
        assertEquals("", resp.bodyString());
    }

    @Test
    public void t20_content_failures() {
        // 无 str → 报错
        fail(dispatch(signed("/o/content/x.txt").build()), "e.io.obj.noexists");
        // GET 带 body JSON 的 str 也不生效（2026-11-10 用户修订：str/d 只从
        // query string 取，不解析请求体；路由已收紧 GET-only）→ 报错
        fail(dispatch(signed("/o/content/x.txt")
            .bodyJson("{str:'/home/tester/main.txt'}")
            .build()), "e.io.obj.noexists");
        // 未登录 → 302
        assertEquals(302,
                     dispatch(req("/o/content/x.txt")
                         .query("str=/home/tester/main.txt")
                         .build()).status());
    }

    @Test
    public void t20b_content_get_query_str_with_ticket() {
        // 用户 ATEST 真机场景（2026-11-10 上报）：GET /o/content?str=<path>
        // 无 downloadName 段、无 body，带票据 → 200 直写内容
        // （对照 /o/content?str=/home/bunya/.domain/dicts.json5）
        MockWnMvcResponse resp = dispatch(signed("/o/content")
            .query("str=/home/tester/main.txt")
            .build());
        assertEquals(resp.bodyString(), 200, resp.status());
        assertEquals("0123456789", resp.bodyString());
    }

    // ------------------------------------------------------------------
    // saveText
    // ------------------------------------------------------------------

    @Test
    public void t21_savetext_write_plain() {
        MockWnMvcResponse resp = dispatch(signed("/o/save/text").method("POST")
            .bodyForm("str=/home/tester/main.txt&content=hello")
            .build());
        ok(resp);
        assertEquals("hello",
                     new String(io.contents.get("/home/tester/main.txt"),
                                StandardCharsets.UTF_8));
    }

    @Test
    public void t22_savetext_base64_and_create_if_noexists() {
        MockWnMvcResponse resp = dispatch(signed("/o/save/text").method("POST")
            .bodyForm("str=/home/tester/new.txt&cine=true&base64=true&content=aGVsbG8=")
            .build());
        ok(resp);
        assertNotNull(io.objects.get("/home/tester/new.txt"));
        assertEquals("hello",
                     new String(io.contents.get("/home/tester/new.txt"),
                                StandardCharsets.UTF_8));
    }

    @Test
    public void t23_savetext_failures() {
        // 目标不存在且未声明 cine → 报错
        fail(dispatch(signed("/o/save/text").method("POST")
            .bodyForm("str=/no/such.txt&content=x")
            .build()), "e.io.obj.noexists");
        // 未登录 → 302
        assertEquals(302,
                     dispatch(req("/o/save/text").method("POST")
                         .bodyForm("str=/home/tester/main.txt&content=x")
                         .build()).status());
    }

    // ------------------------------------------------------------------
    // saveStream（QueryStringAdaptor：参数走 query string，body = 原始流）
    // ------------------------------------------------------------------

    @Test
    public void t24_savestream_mode_r_creates_and_writes_stream() {
        byte[] bytes = "STREAM-DATA".getBytes(StandardCharsets.UTF_8);
        MockWnMvcResponse resp = dispatch(signed("/o/save/stream")
            .method("POST")
            .query("str=/home/tester/up.bin&nm=up.bin&sz=11&mime=text/plain")
            .body(bytes, "application/octet-stream")
            .build());
        ok(resp);
        WnObj o = io.objects.get("/home/tester/up.bin");
        assertNotNull(o);
        assertArrayEquals(bytes, io.contents.get("/home/tester/up.bin"));
        // 原始数据与标题
        NutMap local = o.getAs("local", NutMap.class);
        assertEquals("up.bin", local.getString("name"));
        assertEquals("text/plain", local.getString("mime"));
        assertEquals(11, local.getInt("size"));
        assertEquals("up", o.getString("title"));
    }

    @Test
    public void t25_savestream_mode_s_replaces_and_requires_existing_file() {
        byte[] bytes = "REPLACED".getBytes(StandardCharsets.UTF_8);
        MockWnMvcResponse resp = dispatch(signed("/o/save/stream")
            .method("POST")
            .query("str=/home/tester/main.txt&nm=main.txt&m=s&sz=8")
            .body(bytes, "application/octet-stream")
            .build());
        ok(resp);
        assertArrayEquals(bytes, io.contents.get("/home/tester/main.txt"));
        // s 模式目标不存在 → 报错
        fail(dispatch(signed("/o/save/stream").method("POST")
            .query("str=/no/such.txt&nm=x.txt&m=s")
            .body(bytes, "application/octet-stream")
            .build()), "e.web.obj.save_stream.targetNoExists");
    }

    @Test
    public void t26_savestream_mode_a_dedup_with_tmpl() {
        io.add("/home/tester/up", WnRace.DIR, "");
        io.add("/home/tester/up/a.txt", WnRace.FILE, "old");
        byte[] bytes = "NEW".getBytes(StandardCharsets.UTF_8);
        MockWnMvcResponse resp = dispatch(signed("/o/save/stream")
            .method("POST")
            .query("str=/home/tester/up&nm=a.txt&m=a&sz=3&tmpl=${major}(${nb})${suffix}")
            .body(bytes, "application/octet-stream")
            .build());
        ok(resp);
        // 重名 → 生成 a(1).txt
        assertNotNull(io.objects.get("/home/tester/up/a(1).txt"));
        assertArrayEquals(bytes, io.contents.get("/home/tester/up/a(1).txt"));
        // 原文件不动
        assertEquals("old",
                     new String(io.contents.get("/home/tester/up/a.txt"),
                                StandardCharsets.UTF_8));
    }

    @Test
    public void t27_savestream_failures() {
        byte[] bytes = "X".getBytes(StandardCharsets.UTF_8);
        // 非法 mode
        fail(dispatch(signed("/o/save/stream").method("POST")
            .query("str=/home/tester/main.txt&nm=x.txt&m=x")
            .body(bytes, "application/octet-stream")
            .build()), "e.web.obj.save_stream.invalidMode");
        // 未登录 → 302
        assertEquals(302,
                     dispatch(req("/o/save/stream").method("POST")
                         .query("str=/home/tester/up.bin&nm=up.bin")
                         .body(bytes, "application/octet-stream")
                         .build()).status());
    }

    // ------------------------------------------------------------------
    // OPTIONS / 路由 / 会话过滤
    // ------------------------------------------------------------------

    @Test
    public void t28_options_preflight_returns_empty_204() {
        for (String path : new String[]{"/o/fetch",
                                        "/o/fetch2",
                                        "/o/content/x.txt",
                                        "/o/save/text",
                                        "/o/save/stream"}) {
            MockWnMvcResponse resp = dispatch(req(path).method("OPTIONS")
                .build());
            assertEquals(path, 204, resp.status());
            assertEquals(path, "", resp.bodyString());
            assertEquals(path, "*", resp.header("Access-Control-Allow-Origin"));
        }
    }

    @Test
    public void t29_obj_routes_registered_before_fallback_and_deprecated_not_registered() {
        String[] paths = {"/o/fetch",
                          "/o/fetch2",
                          "/o/ancestors",
                          "/o/update",
                          "/o/query",
                          "/o/children",
                          "/o/content/x.txt",
                          "/o/save/text",
                          "/o/save/stream"};
        Class<?>[] types = {ObjFetchController.class,
                            ObjFetch2Controller.class,
                            ObjAncestorsController.class,
                            ObjUpdateController.class,
                            ObjQueryController.class,
                            ObjChildrenController.class,
                            ObjContentController.class,
                            ObjSaveTextController.class,
                            ObjSaveStreamController.class};
        // 18（RM-NUTZ-06/07，/a/login 已合并）+ 2（chse/兜底）+ 9（RM-NUTZ-08，/o/find 弃用删除）+ 1（RM-NUTZ-09 /api/?/**）= 30
        assertEquals(30, factory.mapping().patterns().size());
        assertEquals("/**", factory.mapping().patterns().get(29));
        for (int i = 0; i < paths.length; i++) {
            assertEquals(paths[i],
                         types[i],
                         factory.mapping()
                             .match(com.site0.walnut.mvc.WnMvcHttpMethod.GET,
                                    req(paths[i]).build())
                             .controllerType());
        }
        // 7 个弃用入口不注册（6 个 @Deprecated + /o/find 2026-11-10 弃用）：落入 /** 兜底
        for (String path : new String[]{"/o/find",
"/o/get/x",
                                        "/o/set/x",
                                        "/o/thumbnail/x",
                                        "/o/read/x",
                                        "/o/write/x",
                                        "/o/upload/x"}) {
            assertEquals(path,
                         WebRootController.class,
                         factory.mapping()
                             .match(com.site0.walnut.mvc.WnMvcHttpMethod.GET,
                                    req(path).build())
                             .controllerType());
        }
        // 2026-11-10 用户裁定：读操作 GET-only——POST 落兜底；query 例外（条件复杂，POST+GET）；update/save 保持 ANY
        for (String path : new String[]{"/o/fetch",
                                        "/o/fetch2",
                                        "/o/ancestors",
                                        "/o/children",
                                        "/o/content/x.txt"}) {
            assertEquals(path + " POST",
                         WebRootController.class,
                         factory.mapping()
                             .match(com.site0.walnut.mvc.WnMvcHttpMethod.POST,
                                    req(path).build())
                             .controllerType());
        }
        String[] anyPaths = {"/o/update", "/o/query", "/o/save/text", "/o/save/stream"};
        Class<?>[] anyTypes = {ObjUpdateController.class,
                               ObjQueryController.class,
                               ObjSaveTextController.class,
                               ObjSaveStreamController.class};
        for (int i = 0; i < anyPaths.length; i++) {
            assertEquals(anyPaths[i] + " POST",
                         anyTypes[i],
                         factory.mapping()
                             .match(com.site0.walnut.mvc.WnMvcHttpMethod.POST,
                                    req(anyPaths[i]).build())
                             .controllerType());
        }
    }

    @Test
    public void t30_session_filters_fail_closed() {
        // fetch/fetch2 = SessionCheck(true) → 401；其余 = SessionCheck() → 302 重定向
        // /
        assertEquals(401,
                     dispatch(req("/o/fetch").query("str=x").build()).status());
        assertEquals(401,
                     dispatch(req("/o/fetch2").query("str=x").build())
                         .status());
        for (String path : new String[]{"/o/ancestors",
                                        "/o/query",
                                        "/o/children"}) {
            MockWnMvcResponse resp = dispatch(req(path).build());
            assertEquals(path, 302, resp.status());
            assertEquals(path, "/", resp.redirectedTo());
        }
    }

    // ------------------------------------------------------------------
    // 测试脚手架（与 RM-NUTZ-07 同构）
    // ------------------------------------------------------------------

    private MockWnMvcRequest.Builder req(String path) {
        return MockWnMvcRequest.builder().path(path);
    }

    private MockWnMvcRequest.Builder signed(String path) {
        return req(path).header("X-Walnut-Ticket", session.getTicket());
    }

    private MockWnMvcResponse dispatch(MockWnMvcRequest req) {
        MockWnMvcResponse resp = new MockWnMvcResponse();
        WnMvcDispatcher dispatcher = new WnMvcDispatcher(factory.mapping(),
                                                         factory,
                                                         new WnMvcFilter[]{new CreateMvcContextFilter(new WnLoginAuthLookup(auth),
                                                                                                      io),
                                                                            new CorsFilter()},
                                                         new WnMvcFilter[]{new CleanupFilter()});
        dispatcher.dispatch(factory.newContext(req, resp));
        assertFalse(Wn.WC().hasTicket());
        return resp;
    }

    private static NutMap body(MockWnMvcResponse resp) {
        return Json.fromJson(NutMap.class, resp.bodyString());
    }

    private static void ok(MockWnMvcResponse resp) {
        assertEquals(resp.bodyString(), 200, resp.status());
        assertTrue(resp.bodyString(), body(resp).getBoolean("ok"));
    }

    private static void fail(MockWnMvcResponse resp, String code) {
        assertEquals(resp.bodyString(), 200, resp.status());
        assertFalse(resp.bodyString(), body(resp).getBoolean("ok"));
        assertEquals(code, body(resp).getString("errCode"));
    }

    private static class FakeAuth extends WnSimpleLoginApi {
        final Map<String, WnSimpleUser> users = new LinkedHashMap<>();
        final Map<String, WnSession> sessions = new LinkedHashMap<>();

        FakeAuth() {
            super(null);
        }

        WnSimpleUser addUser(String name) {
            WnSimpleUser user = new WnSimpleUser(name);
            user.setId("id-" + name);
            user.setMainGroup(name);
            user.setHomePath("/home/" + name);
            users.put(name, user);
            return user;
        }

        WnSimpleSession newSession(WnUser user, String ticket) {
            WnSimpleSession se = new WnSimpleSession(user, 3600);
            se.setTicket(ticket);
            sessions.put(ticket, se);
            return se;
        }

        public WnUser getUser(String name) {
            return users.get(name);
        }

        public WnUser checkUser(String name) {
            WnUser user = getUser(name);
            if (user == null)
                throw Er.create("e.auth.user.NoExists", name);
            return user;
        }

        public WnSession getSession(String ticket) {
            return sessions.get(ticket);
        }

        public WnSession checkSession(String ticket) {
            WnSession se = getSession(ticket);
            if (se == null)
                throw Er.create("e.auth.session.NoExists", ticket);
            return se;
        }

        public void touchSession(WnSession se) {}
    }

    private static class FakeIo extends WnIoSaftyWrapper {
        final Map<String, WnObj> objects = new LinkedHashMap<>();
        final Map<String, byte[]> contents = new LinkedHashMap<>();

        WnObj add(String path, WnRace race, String text) {
            WnObj obj = new WnIoObj();
            obj.id(path).name(path.substring(path.lastIndexOf('/') + 1));
            obj.race(race).creator("tester").group("tester").mode(0777);
            int pos = path.lastIndexOf('/');
            if (pos > 0) {
                WnObj parent = objects.get(path.substring(0, pos));
                if (null != parent)
                    ((WnIoObj) obj).setParent(parent);
            }
            obj.path(path);
            String[] segments = path.split("/");
            obj.d0(segments[1]);
            if (segments.length > 2)
                obj.d1(segments[2]);
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            obj.len(bytes.length).mime("text/plain").sha1(Wlang.sha1(text));
            objects.put(path, obj);
            contents.put(path, bytes);
            return obj;
        }

        public WnObj fetch(WnObj parent, String path) {
            if (null == parent)
                return objects.get(path);
            return objects.get(Wn.appendPath(parent.path(), path));
        }

        public WnObj check(WnObj parent, String path) {
            WnObj obj = fetch(parent, path);
            if (obj == null)
                throw Er.create("e.io.noexists", path);
            return obj;
        }

        public boolean exists(WnObj parent, String path) {
            return null != fetch(parent, path);
        }

        public WnObj createIfNoExists(WnObj parent, String path, WnRace race) {
            WnObj obj = fetch(parent, path);
            if (null != obj)
                return obj;
            String full = null == parent ? path
                                         : Wn.appendPath(parent.path(), path);
            return add(full, race, "");
        }

        public WnObj create(WnObj parent, String path, WnRace race) {
            String full = null == parent ? path
                                         : Wn.appendPath(parent.path(), path);
            return add(full, race, "");
        }

        public List<WnObj> getChildren(WnObj o, String name) {
            List<WnObj> list = new ArrayList<>();
            String prefix = o.path() + "/";
            for (String path : objects.keySet()) {
                if (path.startsWith(prefix)
                    && path.length() > prefix.length()
                    && path.indexOf('/', prefix.length()) < 0) {
                    list.add(objects.get(path));
                }
            }
            return list;
        }

        public List<WnObj> query(WnQuery q) {
            List<WnObj> matched = new ArrayList<>();
            for (WnObj o : objects.values()) {
                if (__matches(q, o))
                    matched.add(o);
            }
            int from = Math.min(Math.max(0, q.skip()), matched.size());
            int to = q.limit() > 0 ? Math.min(from + q.limit(), matched.size())
                                   : matched.size();
            return new ArrayList<>(matched.subList(from, to));
        }

        public int count(WnQuery q) {
            int n = 0;
            for (WnObj o : objects.values()) {
                if (__matches(q, o))
                    n++;
            }
            return n;
        }

        private boolean __matches(WnQuery q, WnObj o) {
            List<NutMap> conds = q.getList();
            if (null == conds || conds.isEmpty())
                return true;
            // 多条件表 = OR；表内键 = AND（只覆盖本测试用到的等值匹配）
            for (NutMap cond : conds) {
                boolean all = true;
                for (Map.Entry<String, Object> en : cond.entrySet()) {
                    if (!Objects.equals(String.valueOf(o.get(en.getKey())),
                                        String.valueOf(en.getValue()))) {
                        all = false;
                        break;
                    }
                }
                if (all)
                    return true;
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        public void appendMeta(WnObj o, Object meta) {
            if (meta instanceof Map) {
                ((WnIoObj) o).putAll((Map<String, Object>) meta);
            }
        }

        public long writeText(WnObj o, CharSequence cs) {
            byte[] bytes = cs.toString().getBytes(StandardCharsets.UTF_8);
            return writeBytes(o, bytes);
        }

        public long writeBytes(WnObj o, byte[] buf) {
            contents.put(o.path(), buf);
            o.len(buf.length)
                .sha1(Wlang.sha1(new String(buf, StandardCharsets.UTF_8)));
            return buf.length;
        }

        public OutputStream getOutputStream(WnObj o, long off) {
            return new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    super.close();
                    FakeIo.this.writeBytes(o, toByteArray());
                }
            };
        }

        public String readText(WnObj obj) {
            return new String(contents.get(obj.path()), StandardCharsets.UTF_8);
        }

        public InputStream getInputStream(WnObj obj, long offset) {
            byte[] bytes = contents.get(obj.path());
            return new ByteArrayInputStream(bytes,
                                            (int) offset,
                                            bytes.length - (int) offset);
        }
    }
}
