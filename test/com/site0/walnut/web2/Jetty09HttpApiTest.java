package com.site0.walnut.web2;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.IoCoreSetup;
import com.site0.walnut.impl.box.JvmExecutorFactory;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.WnLoginRoleOptions;
import com.site0.walnut.login.WnLoginUserOptions;
import com.site0.walnut.login.session.WnLoginSessionOptions;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.mvc.MockWnMvcRequest;
import com.site0.walnut.mvc.MockWnMvcResponse;
import com.site0.walnut.mvc.WnMvcDispatcher;
import com.site0.walnut.mvc.WnMvcFactory;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcHttpMethod;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web.setup.WnSetup;
import com.site0.walnut.web2.controller.app.WnAppService;
import com.site0.walnut.web2.controller.httpapi.HttpApiController;
import com.site0.walnut.web2.controller.httpapi.HttpApiEngine;
import com.site0.walnut.web2.filter.global.CleanupFilter;
import com.site0.walnut.web2.filter.global.CreateMvcContextFilter;
import com.site0.walnut.web2.router.WnRoutes;
import com.site0.walnut.web2.support.WnLoginAuthLookup;

/**
 * JETTY-09：HttpApiModule 迁移测试（任务文档 T6 十组用例）。
 *
 * <p>引擎直接 new + WnRun setter 装配<b>真实服务</b>（IoCoreSetup：Mongo 索引 +
 * LocalSha1BM + 真实 JvmBoxService 执行真实命令）；路由组走完整 dispatch。</p>
 *
 * <p>环境依赖：本机可用的 Mongo（localhost:27017）与 Redis（127.0.0.1:6379，db8）
 * 测试实例——与 IoCoreSetup/BaseIoTest 相同的前提；缺失时整组 BLOCKED。</p>
 */
public class Jetty09HttpApiTest {

    private IoCoreSetup setup;

    private WnIo io;

    private WnLoginApi auth;

    private WnBoxService boxes;

    private WnServiceHolder services;

    private WnConfig conf;

    private JvmExecutorFactory jef;

    private WnUser me;

    private String homePath;

    private WnObj oApiHome;

    @Before
    public void setUp() {
        Wn.Ctx.clear();
        setup = new IoCoreSetup();
        setup.cleanAllData();
        io = setup.getIo();
        WnSetup.makeWalnutKeyDirIfNoExists(io);
        auth = setup.getLoginApi();
        auth.addRootUserIfNoExists("123456");
        boxes = setup.getBoxService();
        services = setup.getServiceFactory();
        conf = new WnConfig("conf/web.properties");
        services.setConfig(conf);
        // 与生产一致：扫描内置命令包（IoCoreSetup.getBoxService 同款装配）
        jef = new JvmExecutorFactory();
        Mirror.me(jef)
              .setValue(jef,
                        "scanPkgs",
                        Wlang.array("com.site0.walnut.impl.box.cmd"));
        // appService（HttpApiController.__engine 的 jef(ctx) 依赖它）
        WnAppService appService = new WnAppService();
        appService.setServices(services);
        appService.setIo(io);
        appService.setAuth(auth);
        appService.setConf(conf);
        appService.setBoxes(boxes);
        appService.setHooks(services.getHookApi());
        appService.setJef(jef);
        services.setAppService(appService);

        // API 执行账户 + 注册表目录
        WnSimpleUser u = new WnSimpleUser("xiaobai");
        u.setMainGroup("xiaobai");
        u.genSaltAndRawPasswd("123456");
        me = auth.addUser(u);
        homePath = me.getHomePath();
        oApiHome = io.createIfNoExists(null, homePath + "/.regapi/api", WnRace.DIR);
    }

    @After
    public void tearDown() {
        Wn.Ctx.clear();
    }

    // ------------------------------------------------------------------
    // ① 正常执行（真实命令 → box 输出直写）
    // ------------------------------------------------------------------

    @Test
    public void t01_normal_run_box_output() {
        newApi("hello", "echo HelloAPI", null);
        MockWnMvcResponse resp = run(req("hello").build(), "xiaobai", "hello");
        assertEquals(200, resp.status());
        assertEquals("HelloAPI\n", resp.bodyString());
        assertEquals("text/html", resp.header("Content-Type"));
    }

    // ------------------------------------------------------------------
    // ② API 不存在 → 404 sendError
    // ------------------------------------------------------------------

    @Test
    public void t02_api_not_found_404() {
        MockWnMvcResponse resp = run(req("no_such").build(), "xiaobai", "no_such");
        assertEquals(404, resp.status());
    }

    // ------------------------------------------------------------------
    // ③ e.api.forbid → 403（声明了 api-access-token 但未带 key）
    // ------------------------------------------------------------------

    @Test
    public void t03_access_token_forbid_403() {
        newApi("secret", "echo secret", Wlang.map("api-access-token", "http-qs-key"));
        MockWnMvcResponse resp = run(req("secret").build(), "xiaobai", "secret");
        assertEquals(403, resp.status());
    }

    // ------------------------------------------------------------------
    // ④ OPTIONS → CORS 头应答无 body
    // ------------------------------------------------------------------

    @Test
    public void t04_options_cors_empty_body() {
        NutMap meta = Wlang.map("http-cross-origin", "*");
        meta.put("http-header-ACCESS-CONTROL-ALLOW-ORIGIN", "*");
        newApi("corsapi", "echo cors", meta);
        MockWnMvcResponse resp = run(req("corsapi").method("OPTIONS").build(),
                                     "xiaobai",
                                     "corsapi");
        assertEquals(200, resp.status());
        assertEquals("", resp.bodyString());
        assertEquals("*", resp.header("Access-Control-Allow-Origin"));
    }

    // ------------------------------------------------------------------
    // ⑤ 重定向 301/302
    // ------------------------------------------------------------------

    @Test
    public void t05_redirect_302() {
        newApi("jump", "echo -n http://example.com/x", Wlang.map("http-resp-code", 302));
        MockWnMvcResponse resp = run(req("jump").build(), "xiaobai", "jump");
        assertEquals(302, resp.status());
        assertEquals("http://example.com/x", resp.redirectedTo());
    }

    // ------------------------------------------------------------------
    // ⑥ GET 缓存命中与懒加载
    // ------------------------------------------------------------------

    @Test
    public void t06_cache_lazy_and_hit() {
        WnObj oApi = newApi("cached",
                            "echo CACHE_BODY_1",
                            Wlang.map("cache-test",
                                      Json.fromJson("[{path:'~/.cache/api/cached.txt'}]")));
        // 第一次：未命中 → 懒加载，执行并把输出写入缓存对象
        MockWnMvcResponse resp1 = run(req("cached").build(), "xiaobai", "cached");
        assertEquals(200, resp1.status());
        assertEquals("CACHE_BODY_1\n", resp1.bodyString());
        WnObj oCache = io.fetch(null, homePath + "/.cache/api/cached.txt");
        assertNotNull(oCache);
        assertEquals("CACHE_BODY_1\n", io.readText(oCache));

        // 改掉 API 命令内容；第二次命中缓存，不执行命令
        io.writeText(oApi, "echo CHANGED");
        MockWnMvcResponse resp2 = run(req("cached").build(), "xiaobai", "cached");
        assertEquals(200, resp2.status());
        assertEquals("CACHE_BODY_1\n", resp2.bodyString());
    }

    // ------------------------------------------------------------------
    // ⑦ 异常 → 500 + stacktrace 入 writer（执行账户不存在）
    // ------------------------------------------------------------------

    @Test
    public void t07_exception_500_with_stacktrace() {
        MockWnMvcResponse resp = run(req("hello").build(), "ghost", "hello");
        assertEquals(500, resp.status());
        // stacktrace 入 writer：错误键 + 引擎栈帧（Nutz WebException.toString 不含类名）
        assertTrue(resp.bodyString(),
                   resp.bodyString().contains("e.auth.account.noexists"));
        assertTrue(resp.bodyString(),
                   resp.bodyString().contains("HttpApiEngine.doApi"));
    }

    // ------------------------------------------------------------------
    // ⑧ 动态渲染：stdout 为原始 HTTP 响应直写；errOut → reset + 500
    // ------------------------------------------------------------------

    @Test
    public void t08_dynamic_header_render() {
        // 8a：stdout 是原始 HTTP 响应 → 状态行/头/body 直写
        WnObj oHome = io.check(null, homePath);
        WnObj oResp = io.create(oHome, "dyn-resp.txt", WnRace.FILE);
        io.writeText(oResp, "HTTP/1.1 418\nX-Demo: yes\n\nDynamicBody");
        newApi("dyn", "cat ~/dyn-resp.txt", Wlang.map("http-dynamic-header", true));

        MockWnMvcResponse resp = run(req("dyn").build(), "xiaobai", "dyn");
        assertEquals(418, resp.status());
        assertEquals("yes", resp.header("X-Demo"));
        assertEquals("DynamicBody", resp.bodyString());
    }

    @Test
    public void t08b_dynamic_header_errout_reset_500() {
        // 8b：命令只写错误流 → errOut 分支（未提交时 reset → 500）
        newApi("dynerr",
               "cat /no/such/file",
               Wlang.map("http-dynamic-header", true));
        MockWnMvcResponse resp = run(req("dynerr").build(), "xiaobai", "dynerr");
        assertEquals(500, resp.status());
        assertTrue(resp.bodyString(), resp.bodyString().length() > 0);
    }

    // ------------------------------------------------------------------
    // ⑨ finally 清理：oReq 过期标记 + 会话注销 + WC 清理
    // ------------------------------------------------------------------

    @Test
    public void t09_finally_cleanup() {
        newApi("hello", "echo HelloAPI", null);
        MockWnMvcResponse resp = run(req("hello").build(), "xiaobai", "hello");
        assertEquals(200, resp.status());

        // 请求对象已打过期标记（默认 60s）
        WnObj oTmp = io.check(null, homePath + "/.regapi/tmp");
        List<WnObj> tmpChildren = io.getChildren(oTmp, null);
        assertEquals(1, tmpChildren.size());
        assertTrue(tmpChildren.get(0).getLong("expi", -1) > Wn.now());

        // API 会话已注销（/var/session 无残留）
        WnObj oSeDir = io.check(null, "/var/session");
        assertEquals(0, io.getChildren(oSeDir, null).size());

        // 线程上下文已清理
        assertFalse(Wn.WC().hasTicket());
    }

    // ------------------------------------------------------------------
    // ⑩ 路由顺序 + AsUsr 拒绝（root 缺失 fail-closed）+ 全链路 dispatch
    // ------------------------------------------------------------------

    @Test
    public void t10_route_order_and_asusr() {
        newApi("hello", "echo HelloAPI", null);
        WnMvcFactory factory = newFactory(services);

        // 32 = 31（JETTY-06/07/08 + chse）+ 1（/api/?/**）；兜底仍在最后
        assertEquals(32, factory.mapping().patterns().size());
        assertEquals("/**", factory.mapping().patterns().get(31));

        // /api/?/** 命中 HttpApiController，?/** 绑定进 pathArgs
        MockWnMvcRequest r = req("hello").build();
        assertEquals(HttpApiController.class,
                     factory.mapping()
                            .match(WnMvcHttpMethod.GET, r)
                            .controllerType());
        assertEquals("xiaobai", r.getPathArgs().get(0));
        assertEquals("hello", r.getPathArgs().get(1));

        // 全链路 dispatch（root 存在 → AsUsr 放行 → 引擎执行）
        MockWnMvcResponse resp = dispatch(factory, req("hello").build());
        assertEquals(200, resp.status());
        assertEquals("HelloAPI\n", resp.bodyString());

        // root 不存在的权鉴 → AsUsrFilter fail-closed 403（同一生产路由）
        WnLoginApi authNoRoot = newLoginApiWithoutRoot();
        WnServiceHolder holder2 = setup.getServiceFactory();
        holder2.setConfig(conf);
        holder2.setLoginApi(authNoRoot);
        MockWnMvcResponse resp2 = dispatch(newFactory(holder2), req("hello").build());
        assertEquals(403, resp2.status());
    }

    // ------------------------------------------------------------------
    // 测试脚手架
    // ------------------------------------------------------------------

    private WnObj newApi(String name, String cmdText, NutMap meta) {
        WnObj o = io.create(oApiHome, name, WnRace.FILE);
        io.writeText(o, cmdText);
        if (null != meta) {
            io.appendMeta(o, meta);
        }
        return o;
    }

    /**
     * 路径固定 /api/xiaobai/&lt;api&gt;；带空 body 保证
     * {@code getInputStream()} 非 null（与 servlet 空 body 行为对齐）。
     */
    private MockWnMvcRequest.Builder req(String api) {
        return MockWnMvcRequest.builder()
                               .method("GET")
                               .path("/api/xiaobai/" + api)
                               .body(new byte[0], null);
    }

    /** 引擎直接装配真实服务执行（不经过 dispatch）。 */
    private MockWnMvcResponse run(MockWnMvcRequest req, String usr, String api) {
        MockWnMvcResponse resp = new MockWnMvcResponse();
        newEngine().doApi(usr, api, req, resp);
        return resp;
    }

    private HttpApiEngine newEngine() {
        HttpApiEngine engine = new HttpApiEngine();
        engine.setServices(services);
        engine.setIo(io);
        engine.setAuth(auth);
        engine.setConf(conf);
        engine.setBoxes(boxes);
        engine.setHooks(services.getHookApi());
        engine.setJef(jef);
        engine.setAllocTimeout(0);
        return engine;
    }

    private WnMvcFactory newFactory(WnServiceHolder holder) {
        WnMvcFactory factory = new WnMvcFactory();
        factory.services(holder).io(io);
        WnRoutes.register(factory.mapping());
        return factory;
    }

    private MockWnMvcResponse dispatch(WnMvcFactory factory, MockWnMvcRequest req) {
        MockWnMvcResponse resp = new MockWnMvcResponse();
        WnLoginAuthLookup lookup = new WnLoginAuthLookup(factory.services()
                                                                .getLoginApi());
        WnMvcDispatcher dispatcher = new WnMvcDispatcher(factory.mapping(),
                                                         factory,
                                                         new WnMvcFilter[]{new CreateMvcContextFilter(lookup,
                                                                                                      io)},
                                                         new WnMvcFilter[]{new CleanupFilter()});
        dispatcher.dispatch(factory.newContext(req, resp));
        assertFalse(Wn.WC().hasTicket());
        return resp;
    }

    /**
     * 用户/会话/角色存储换到空目录的登录接口（root 不存在），
     * 用于验证生产路由的 AsUsrFilter("root") fail-closed。
     */
    private WnLoginApi newLoginApiWithoutRoot() {
        WnLoginOptions options = new WnLoginOptions();
        options.session = new WnLoginSessionOptions();
        options.session.path = "/var/session2";
        options.user = new WnLoginUserOptions();
        options.user.path = "/sys/usr2";
        options.role = new WnLoginRoleOptions();
        options.role.path = "/sys/role2";
        options.domain = "root";
        options.sessionDuration = 3600;
        options.sessionShortDu = 3;
        io.createIfNoExists(null, "/var/session2", WnRace.DIR);
        io.createIfNoExists(null, "/sys/usr2", WnRace.DIR);
        io.createIfNoExists(null, "/sys/role2", WnRace.DIR);
        return WnLoginApiMaker.forSys().make(io, new NutMap(), options);
    }

}
