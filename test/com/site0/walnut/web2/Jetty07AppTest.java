package com.site0.walnut.web2;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.core.io.WnIoSaftyWrapper;
import com.site0.walnut.impl.box.JvmExecutorFactory;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnSimpleLoginApi;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.role.WnRoleLoader;
import com.site0.walnut.login.role.WnRoleType;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.session.WnSimpleSession;
import com.site0.walnut.login.usr.WnSimpleUser;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.lookup.WnLookup;
import com.site0.walnut.lookup.impl.WnLookupMaker;
import com.site0.walnut.mvc.MockWnMvcRequest;
import com.site0.walnut.mvc.MockWnMvcResponse;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcController;
import com.site0.walnut.mvc.WnMvcDispatcher;
import com.site0.walnut.mvc.WnMvcFactory;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcHttpMethod;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.security.captcha.WnCaptchaObj;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web.bean.WnApp;
import com.site0.walnut.web2.controller.*;
import com.site0.walnut.web2.controller.app.WnOcmInfo;
import com.site0.walnut.web2.controller.app.WnAppService;
import com.site0.walnut.web2.filter.global.CleanupFilter;
import com.site0.walnut.web2.filter.global.CreateMvcContextFilter;
import com.site0.walnut.web2.router.WnRoutes;
import com.site0.walnut.web2.support.WnLoginAuthLookup;

public class Jetty07AppTest {

    private FakeIo io;
    private FakeAuth auth;
    private FakeApps apps;
    private FakeLookupMaker lookups;
    private TestFactory factory;
    private WnConfig conf;
    private WnServiceHolder holder;
    private WnSimpleSession session;
    private FakeCaptcha captcha;
    private String lastCaptchaName;
    private WnOcmInfo lastCaptchaOcm;
    private NutMap lastOtpParams;
    private WnOcmInfo lastOtpOcm;
    private WnMvcContext lastOtpContext;

    @Before
    public void setUp() {
        Wn.Ctx.clear();
        io = new FakeIo();
        auth = new FakeAuth();
        auth.addUser("root").setHomePath("/root");
        WnSimpleUser user = auth.addUser("tester");
        user.setRawPasswd("secret");
        user.setEmail("tester@example.invalid");
        session = auth.newSession(user, "ticket");
        apps = new FakeApps();
        lookups = new FakeLookupMaker(io);
        conf = new WnConfig("conf/web.properties");
        conf.set("app-module-sys-login", "PASSWD_ONLY");
        conf.set("app-module-dmn-login", "PASSWD_ONLY");
        conf.set("sys-entry-url", "/a/login/");
        conf.set("cookie_same_site", "Auto");
        holder = new WnServiceHolder();
        holder.setConfig(conf);
        holder.setLoginApi(auth);
        holder.setAppService(apps);
        holder.setLookupMaker(lookups);
        captcha = new FakeCaptcha();
        factory = new TestFactory();
        factory.services(holder).io(io);
        WnRoutes.register(factory.mapping());
        io.add("/apps/demo", WnRace.DIR, "");
        io.add("/apps/demo/main.txt", WnRace.FILE, "0123456789");
    }

    @After
    public void tearDown() {
        Wn.Ctx.clear();
    }

    @Test
    public void t01_login_redirect() {
        MockWnMvcResponse resp = dispatch(req("/a/login").build());
        assertEquals(302, resp.status());
        assertEquals("/a/login/", resp.redirectedTo());
    }

    @Test
    public void t02_login_trailing_slash_failure_is_html() {
        io.failure = new IllegalStateException("login storage unavailable");
        MockWnMvcResponse resp = dispatch(req("/a/login/").build());
        assertEquals(500, resp.status());
        assertTrue(resp.header("Content-Type").startsWith("text/html"));
    }

    @Test
    public void t03_login_page_builtin_and_existing_session() {
        MockWnMvcResponse resp = dispatch(req("/a/login/").build());
        assertEquals(200, resp.status());
        assertTrue(resp.bodyString().contains("<html"));
        resp = dispatch(signed("/a/login/index.html").build());
        assertEquals("/a/open/wn-term", resp.redirectedTo());
        assertEquals("SEID=ticket; Path=/;", resp.header("Set-Cookie"));
    }

    @Test
    public void t04_login_page_missing_resource() {
        io.add("/etc/hosts.d", WnRace.DIR, "");
        io.add("/etc/hosts.d/default/pages", WnRace.DIR, "");
        assertEquals(404, dispatch(req("/a/login/missing.css").build()).status());
    }

    @Test
    public void t05_open_html_and_etag_304() {
        MockWnMvcResponse resp = dispatch(signed("/a/open/demo").query("ph=%2Ftmp&id=obj").build());
        assertEquals(200, resp.status());
        assertEquals("<html>demo</html>", resp.bodyString());
        assertEquals(Wlang.sha1(resp.bodyString()), resp.header("ETag"));
        resp = dispatch(signed("/a/open/demo").header("If-None-Match", resp.header("ETag")).build());
        assertEquals(304, resp.status());
        assertEquals("", resp.bodyString());
    }

    @Test
    public void t06_open_missing_app_and_unauthorized() {
        assertEquals(404, dispatch(signed("/a/open/missing").build()).status());
        int calls = apps.checked;
        MockWnMvcResponse resp = dispatch(req("/a/open/demo").build());
        assertEquals(302, resp.status());
        assertEquals("/", resp.redirectedTo());
        assertEquals(calls, apps.checked);
    }

    @Test
    public void t07_load_bytes_etag_and_304() {
        MockWnMvcResponse resp = dispatch(signed("/a/load/demo/main.txt").build());
        assertEquals(200, resp.status());
        assertEquals("0123456789", resp.bodyString());
        assertEquals(Wn.getEtag(io.objects.get("/apps/demo/main.txt")), resp.header("ETag"));
        int reads = io.streamReads;
        resp = dispatch(signed("/a/load/demo/main.txt").header("If-None-Match", resp.header("ETag")).build());
        assertEquals(304, resp.status());
        assertEquals("", resp.bodyString());
        assertEquals(reads, io.streamReads);
    }

    @Test
    public void t08_load_missing_resource_or_app() {
        assertEquals(404, dispatch(signed("/a/load/demo/missing.txt").build()).status());
        assertEquals(404, dispatch(signed("/a/load/missing/main.txt").build()).status());
    }

    @Test
    public void t09_beacon_json_writes_without_ajax_envelope() {
        MockWnMvcResponse resp = dispatch(signed("/a/beacon_run/demo").method("POST")
                .bodyJson("{cmd:'beacon',mime:'text/custom',in:'input',PWD:'/tmp',mos:'END'}").build());
        assertEquals(200, resp.status());
        assertEquals("OUT:beacon", resp.bodyString());
        assertEquals("text/custom", resp.header("Content-Type"));
        assertEquals("input", apps.stdin);
        assertEquals("/tmp", apps.pwd);
        assertEquals("END", apps.separator);
    }

    @Test
    public void t10_beacon_failure_keeps_legacy_empty_body() {
        apps.failure = new IllegalStateException("command failed");
        MockWnMvcResponse resp = dispatch(signed("/a/beacon_run/demo").method("POST")
                .bodyJson("{cmd:'beacon'}").build());
        assertEquals(200, resp.status());
        assertEquals("", resp.bodyString());
    }

    @Test
    public void t11_run_form_writes_directly() {
        MockWnMvcResponse resp = dispatch(signed("/a/run/demo").method("POST")
                .bodyForm("cmd=hello&in=stdin&ffb=true&log_off=on").build());
        assertEquals(200, resp.status());
        assertEquals("OUT:hello", resp.bodyString());
        assertEquals("stdin", apps.stdin);
        assertEquals("yes", resp.header("X-ACCEL-BUFFERING"));
        assertTrue(apps.logOff);
        assertEquals("*", resp.header("Access-Control-Allow-Origin"));
    }

    @Test
    public void t12_run_known_failure_and_unknown_exception() {
        fail(dispatch(signed("/a/run/missing").build()), "e.app.noexists");
        apps.failure = new IllegalStateException("command infrastructure failure");
        assertEquals(500, dispatch(signed("/a/run/demo").build()).status());
    }

    @Test
    public void t13_sys_login_password_cookie_profile_and_form() {
        MockWnMvcResponse resp = dispatch(req("/a/sys_login_by_passwd").method("POST")
                .bodyForm("name=tester&passwd=secret&ajax=true").build());
        ok(resp);
        assertEquals("new-ticket", body(resp).getAs("data", NutMap.class).getString("ticket"));
        assertEquals("SEID=new-ticket; Path=/;", resp.header("Set-Cookie"));
        assertEquals(1, apps.profiles);
        assertEquals(1, auth.created);
    }

    @Test
    public void t14_sys_login_bad_password_ajax_and_redirect() {
        MockWnMvcResponse resp = dispatch(req("/a/sys_login_by_passwd")
                .query("name=tester&passwd=bad&ajax=true").build());
        fail(resp, "e.auth.login.Failed");
        assertNull(resp.header("Set-Cookie"));
        assertEquals(0, auth.created);
        resp = dispatch(req("/a/sys_login_by_passwd").query("name=tester&passwd=bad")
                .header("Referer", "/login-again").build());
        assertEquals("/login-again", resp.redirectedTo());
    }

    @Test
    public void t15_sys_logout_parent_cookie_and_delete_cookie() {
        WnSimpleSession parent = auth.newSession(auth.users.get("tester"), "parent");
        auth.logoutParent = parent;
        MockWnMvcResponse resp = dispatch(signed("/a/sys_logout").query("ajax=true").build());
        ok(resp);
        assertEquals("SEID=parent; Path=/;", resp.header("Set-Cookie"));
        assertEquals("ticket", auth.loggedOut);
        auth.logoutParent = null;
        resp = dispatch(req("/a/sys_logout").build());
        assertEquals("/a/login/", resp.redirectedTo());
        assertNotNull(resp.findCookie("SEID"));
        assertEquals(0, resp.findCookie("SEID").maxAge());
        assertEquals("/", resp.findCookie("SEID").path());
    }

    @Test
    public void t16_sys_logout_failure_does_not_set_cookie() {
        auth.logoutFailure = Er.create("e.logout.rejected");
        MockWnMvcResponse resp = dispatch(signed("/a/sys_logout").build());
        assertEquals(500, resp.status());
        assertNull(resp.header("Set-Cookie"));
    }

    @Test
    public void t17_domain_login_owner_password() {
        String path = "/home/tester/login-" + UUID.randomUUID();
        io.add(path, WnRace.DIR, "");
        MockWnMvcResponse resp = dispatch(req("/a/auth_login_by_domain_passwd")
                .query("site=" + path + "&name=tester&passwd=secret&ajax=true").build());
        ok(resp);
        assertEquals("SEID=new-ticket; Path=/;", resp.header("Set-Cookie"));
        assertEquals("root", factory.asUser);
        assertEquals(1, apps.profiles);
    }

    @Test
    public void t18_domain_login_missing_site_and_root_filter_failure() {
        fail(dispatch(req("/a/auth_login_by_domain_passwd").query("ajax=true").build()),
             "e.auth.login.domain_without_www");
        factory.entered.clear();
        auth.users.remove("root");
        assertEquals(403, dispatch(req("/a/auth_login_by_domain_passwd").build()).status());
        assertTrue(factory.entered.isEmpty());
    }

    @Test
    public void t19_captcha_get_controller_and_raw_json() {
        MockWnMvcResponse resp = dispatch(req("/a/captcha/get").query("captcha=slide&account=tester").build());
        ok(resp);
        assertEquals("text/json", resp.header("Content-Type"));
        assertEquals("captcha-id", body(resp).getAs("data", NutMap.class).getString("id"));
        assertEquals("slide", lastCaptchaName);
        assertEquals("/root", lastCaptchaOcm.getConfHomePath());
        assertEquals(1, captcha.generated);
    }

    @Test
    public void t20_captcha_get_unavailable() {
        captcha.unavailable = true;
        fail(dispatch(req("/a/captcha/get").build()), "e.captcha.unavailable");
    }

    @Test
    public void t21_captcha_verify_raw_json_and_domain_binding() {
        // 2026-11-10 用户裁定：captcha 系列 GET-only，参数走 query string
        // （data 传 URL 编码的 JSON 串）
        MockWnMvcResponse resp = dispatch(req("/a/captcha/verify/tester")
                .query("domain=tester&account=tester&captcha=slide&id=captcha-id&data="
                       + URLEncoder.encode("{\"x\":12}", StandardCharsets.UTF_8)).build());
        ok(resp);
        assertEquals("captcha-ticket", body(resp).getAs("data", NutMap.class).getString("ticket"));
        assertEquals(12, Json.fromJson(NutMap.class, captcha.data).getInt("x"));
        assertEquals("captcha-id", captcha.id);
        assertEquals("tester", captcha.account);
        assertEquals("text/json", resp.header("Content-Type"));
    }

    @Test
    public void t22_captcha_verify_mismatch_missing_account_and_invalid() {
        fail(dispatch(req("/a/captcha/verify/tester")
                .query("domain=other&account=tester").build()), "e.captcha.domain.mismatch");
        assertEquals(0, captcha.checked);
        fail(dispatch(req("/a/captcha/verify").query("id=captcha-id").build()),
             "e.captcha.need_account");
        captcha.valid = false;
        fail(dispatch(req("/a/captcha/verify")
                .query("account=tester&id=captcha-id&data=trace").build()), "e.captcha.invalid");
        // body JSON 的参数不再生效（GET-only，只取 query string）
        assertEquals(1, captcha.checked);
        fail(dispatch(req("/a/captcha/verify")
                .bodyJson("{account:'tester',id:'captcha-id'}").build()), "e.captcha.need_account");
    }

    @Test
    public void t23_otp_send_adapter_binds_form_and_ocm_without_network() {
        factory.stubOtpSend = true;
        MockWnMvcResponse resp = dispatch(req("/a/otp/send/tester").method("POST")
                .bodyForm("account=tester&ticket=captcha-ticket&otp=email&captcha=slide").build());
        ok(resp);
        assertEquals("email", lastOtpParams.getString("otp"));
        assertEquals("slide", lastOtpParams.getString("captcha"));
        assertEquals("captcha-ticket", lastOtpParams.getString("ticket"));
        assertEquals("tester@example.invalid", lastOtpOcm.getUser().getEmail());
        assertEquals("/root", lastOtpOcm.getConfHomePath());
        assertSame(io, lastOtpContext.io());
    }

    @Test
    public void t24_otp_send_controller_rejects_missing_account_and_ticket() {
        fail(dispatch(req("/a/otp/send").build()), "e.otp.need_account");
        fail(dispatch(req("/a/otp/send").query("account=tester").build()), "e.otp.need_ticket");
    }

    @Test
    public void t25_login_mode_returns_both_modes_including_options() {
        for (String method : new String[]{"GET", "OPTIONS"}) {
            MockWnMvcResponse resp = dispatch(req("/a/login_mode").method(method).build());
            ok(resp);
            NutMap data = body(resp).getAs("data", NutMap.class);
            assertEquals("PASSWD_ONLY", data.getString("system"));
            assertEquals("PASSWD_ONLY", data.getString("domain"));
            assertEquals("*", resp.header("Access-Control-Allow-Origin"));
        }
    }

    @Test
    public void t26_login_mode_bad_config_rethrows_to_500() {
        conf.set("app-module-sys-login", "invalid-mode");
        assertEquals(500, dispatch(req("/a/login_mode").build()).status());
    }

    @Test
    public void t27_me_session_data() {
        MockWnMvcResponse resp = dispatch(signed("/a/me").build());
        ok(resp);
        assertEquals("ticket", body(resp).getAs("data", NutMap.class).getString("ticket"));
    }

    @Test
    public void t28_me_without_session_fails_closed() {
        MockWnMvcResponse resp = dispatch(req("/a/me").build());
        assertEquals(200, resp.status());
        assertFalse(body(resp).getBoolean("ok"));
        assertNotNull(body(resp).getString("errCode"));
    }

    @Test
    public void t29_ajax_logout_returns_ticket_without_cookie_mutation() {
        MockWnMvcResponse resp = dispatch(signed("/a/sys_ajax_logout").build());
        ok(resp);
        assertEquals("ticket", body(resp).getAs("data", NutMap.class).getString("ticket"));
        assertNull(resp.header("Set-Cookie"));
    }

    @Test
    public void t30_ajax_logout_known_failure_and_unknown_rethrow() {
        auth.logoutFailure = Er.create("e.logout.rejected");
        fail(dispatch(signed("/a/sys_ajax_logout").build()), "e.logout.rejected");
        auth.logoutFailure = new IllegalStateException("logout store down");
        assertEquals(500, dispatch(signed("/a/sys_ajax_logout").build()).status());
    }

    @Test
    public void t31_lookup_default_limit_fetch_reset_and_empty() {
        ok(dispatch(req("/a/lookup/demo/people").query("hint=al").build()));
        assertEquals("demo/people", lookups.name);
        assertEquals("al", lookups.hint);
        assertEquals(30, lookups.limit);
        ok(dispatch(req("/a/lookup/demo/people").query("hint=bo&limit=7&reset=true").build()));
        assertEquals(7, lookups.limit);
        assertEquals(1, lookups.cleared);
        ok(dispatch(req("/a/lookup/demo/people").query("id=42&hint=unused").build()));
        assertEquals("42", lookups.id);
        assertTrue(body(dispatch(req("/a/lookup/demo/people").build())).getList("data", NutMap.class).isEmpty());
    }

    @Test
    public void t32_lookup_business_error_and_unknown_exception() {
        lookups.failure = Er.create("e.lookup.name.Invalid", "bad");
        fail(dispatch(req("/a/lookup/bad").build()), "e.lookup.name.Invalid");
        lookups.failure = new IllegalStateException("database down");
        assertEquals(500, dispatch(req("/a/lookup/bad").build()).status());
    }

    @Test
    public void t33_all_19_routes_before_fallback_with_any_method() {
        String[] paths = {"/a/login", "/a/login/index.html", "/a/open/demo/main", "/a/load/demo/main",
                "/a/beacon_run/demo", "/a/run/demo", "/a/sys_login_by_passwd", "/a/sys_logout",
                "/a/auth_login_by_domain_passwd", "/a/captcha/get", "/a/captcha/get/tester",
                "/a/captcha/verify", "/a/captcha/verify/tester", "/a/otp/send", "/a/otp/send/tester",
                "/a/login_mode", "/a/me", "/a/sys_ajax_logout", "/a/lookup/demo/people"};
        Class<?>[] types = {AppLoginController.class, AppLoginPageController.class, AppOpenController.class,
                AppLoadController.class, AppBeaconRunController.class, AppRunController.class,
                AppSysLoginByPasswdController.class, AppSysLogoutController.class,
                AppAuthLoginByDomainPasswdController.class, AppCaptchaGetController.class,
                AppCaptchaGetController.class, AppCaptchaVerifyController.class, AppCaptchaVerifyController.class,
                AppOtpSendController.class, AppOtpSendController.class, AppLoginModeController.class,
                AppGetMyInfoController.class, AppSysAjaxLogoutController.class, AppLookupController.class};
        // JETTY-08 追加 10 条 /o/* 路由、JETTY-09 追加 1 条 /api/?/** 路由后：
        // 19 + chse + 兜底 + 10 + 1 = 32，兜底仍在最后
        assertEquals(32, factory.mapping().patterns().size());
        assertEquals("/u/ajax/chse", factory.mapping().patterns().get(0));
        assertEquals("/**", factory.mapping().patterns().get(31));
        // 2026-11-10 用户裁定收紧：读操作入口 GET-only——
        // 严格 GET（无 CORS）：POST/OPTIONS 都落兜底
        java.util.Set<String> getStrict = new java.util.HashSet<>(Arrays.asList("/a/open/demo/main",
                                                                                "/a/load/demo/main",
                                                                                "/a/sys_logout"));
        // 带 CORS 的 GET-only：POST 落兜底、OPTIONS 保留（预检）
        java.util.Set<String> getOpt = new java.util.HashSet<>(Arrays.asList("/a/captcha/get",
                                                                             "/a/captcha/get/tester",
                                                                             "/a/captcha/verify",
                                                                             "/a/captcha/verify/tester",
                                                                             "/a/login_mode",
                                                                             "/a/me",
                                                                             "/a/sys_ajax_logout",
                                                                             "/a/lookup/demo/people"));
        for (int i = 0; i < paths.length; i++) {
            // GET 一定命中自己的控制器
            assertEquals(paths[i] + " GET",
                         types[i],
                         factory.mapping().match(WnMvcHttpMethod.GET, req(paths[i]).build()).controllerType());
            boolean isAny = !getStrict.contains(paths[i]) && !getOpt.contains(paths[i]);
            // POST：仅 ANY 路由命中，收紧路由落 /** 兜底
            assertEquals(paths[i] + " POST",
                         isAny ? types[i] : WebRootController.class,
                         factory.mapping().match(WnMvcHttpMethod.POST, req(paths[i]).build()).controllerType());
            // OPTIONS：ANY 与 {GET,OPTIONS} 路由命中，严格 GET 落兜底
            boolean optOk = isAny || getOpt.contains(paths[i]);
            assertEquals(paths[i] + " OPTIONS",
                         optOk ? types[i] : WebRootController.class,
                         factory.mapping().match(WnMvcHttpMethod.OPTIONS, req(paths[i]).build()).controllerType());
        }
    }

    @Test
    public void t34_session_filters_block_before_controller() {
        for (String path : new String[]{"/a/load/demo/main.txt", "/a/run/demo", "/a/beacon_run/demo"}) {
            for (String ticket : new String[]{"", "bad", "expired"}) {
                WnSimpleSession expired = auth.newSession(auth.users.get("tester"), "expired");
                expired.setExpiAt(1);
                factory.entered.clear();
                MockWnMvcResponse resp = dispatch(req(path).header("X-Walnut-Ticket", ticket).build());
                assertEquals(path, 401, resp.status());
                assertTrue(factory.entered.isEmpty());
            }
        }
    }

    @Test
    public void t35_download_range_and_invalid_range() {
        MockWnMvcResponse resp = dispatch(signed("/a/load/demo/main.txt").header("Range", "bytes=2-5").build());
        assertEquals(206, resp.status());
        assertEquals("2345", resp.bodyString());
        assertEquals("bytes 2-5/10", resp.header("Content-Range"));
        assertEquals("4", resp.header("Content-Length"));
        for (String range : new String[]{"bytes=1-2,4-5", "invalid"}) {
            resp = dispatch(signed("/a/load/demo/main.txt").header("Range", range).build());
            assertEquals(400, resp.status());
            assertNotNull(resp.header("Walnut-Http-Range-WARN"));
        }
    }

    @Test
    public void t36_download_force_raw_and_auto() {
        for (String mode : new String[]{"force", "raw", "auto"}) {
            MockWnMvcResponse resp = dispatch(signed("/a/load/demo/main.txt").query("d=" + mode)
                    .header("User-Agent", "Mozilla/5.0").build());
            assertEquals(200, resp.status());
            assertEquals("0123456789", resp.bodyString());
            if ("force".equals(mode))
                assertTrue(resp.header("Content-Disposition").contains("attachment"));
            else
                assertNull(resp.header("Content-Disposition"));
        }
    }

    @Test
    public void t37_run_stderr_sets_500_and_first_output_wins() {
        apps.stderr = true;
        MockWnMvcResponse resp = dispatch(signed("/a/run/demo").query("cmd=bad").build());
        assertEquals(500, resp.status());
        assertEquals("ERR:bad", resp.bodyString());
        apps.mixed = true;
        resp = dispatch(signed("/a/run/demo").query("cmd=mixed").build());
        assertEquals(200, resp.status());
        assertEquals("OUT:mixedERR:mixed", resp.bodyString());
    }

    @Test
    public void t38_options_cors_does_not_consume_body_or_call_services() {
        for (String path : new String[]{"/a/captcha/get", "/a/captcha/verify", "/a/otp/send",
                "/a/sys_login_by_passwd", "/a/run/demo", "/a/me", "/a/sys_ajax_logout", "/a/lookup/demo"}) {
            MockWnMvcResponse resp = dispatch(req(path).method("OPTIONS").bodyJson("invalid-json").build());
            assertEquals(path, 200, resp.status());
            assertEquals(path, "", resp.bodyString());
            assertEquals("*", resp.header("Access-Control-Allow-Origin"));
        }
        assertEquals(0, apps.checked);
        assertEquals(0, captcha.generated);
        assertEquals(0, captcha.checked);
    }

    @Test
    public void t39_query_form_repeated_values_and_boolean_first_value() {
        MockWnMvcResponse resp = dispatch(signed("/a/run/demo").method("POST")
                .query("cmd=query&ffb=false").bodyForm("cmd=form&ffb=true").build());
        assertEquals("OUT:query,form", resp.bodyString());
        assertNull(resp.header("X-ACCEL-BUFFERING"));
        JvmExecutorFactory executorFactory = new JvmExecutorFactory();
        apps.setJef(executorFactory);
        MimeMap mimeMap = new MimeMap() {
            public String getMime(String type) { return "text/plain"; }
            public String getMime(String type, String dftMime) { return dftMime; }
            public java.util.Set<String> keys() { return Collections.emptySet(); }
        };
        factory.mimes(mimeMap);
        new AbstractWnController() {
            public WnMvcView handle(WnMvcContext ctx) {
                assertSame(holder, services(ctx));
                assertSame(io, io(ctx));
                assertSame(mimeMap, mimes(ctx));
                assertSame(auth, auth(ctx));
                assertSame(holder.getBoxApi(), boxes(ctx));
                assertSame(holder.getHookApi(), hooks(ctx));
                assertSame(executorFactory, jef(ctx));
                assertSame(conf, conf(ctx));
                assertSame(holder.getTaskApi(), tasks(ctx));
                assertSame(holder.getScheduleApi(), schedules(ctx));
                assertSame(holder.getCronApi(), crons(ctx));
                return WnMvcView.none();
            }
        }.handle(factory.newContext(req("/").build(), new MockWnMvcResponse()));
    }

    @Test
    public void t40_download_committed_stream_failure_preserves_legacy_behavior() {
        io.streamFailure = true;
        MockWnMvcResponse resp = new MockWnMvcResponse();
        WnMvcDispatcher dispatcher = new WnMvcDispatcher(factory.mapping(), factory,
                new WnMvcFilter[]{new CreateMvcContextFilter(new WnLoginAuthLookup(auth), io)},
                new WnMvcFilter[]{new CleanupFilter()});
        try {
            dispatcher.dispatch(factory.newContext(signed("/a/load/demo/main.txt").build(), resp));
            org.junit.Assert.fail("Expected streaming IO failure");
        }
        catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
        assertTrue(resp.committed());
        assertEquals(200, resp.status());
        assertEquals("", resp.bodyString());
        assertFalse(Wn.WC().hasTicket());
    }

    private MockWnMvcRequest.Builder req(String path) {
        return MockWnMvcRequest.builder().path(path);
    }

    private MockWnMvcRequest.Builder signed(String path) {
        return req(path).header("X-Walnut-Ticket", session.getTicket());
    }

    private MockWnMvcResponse dispatch(MockWnMvcRequest req) {
        MockWnMvcResponse resp = new MockWnMvcResponse();
        WnMvcDispatcher dispatcher = new WnMvcDispatcher(factory.mapping(), factory,
                new WnMvcFilter[]{new CreateMvcContextFilter(new WnLoginAuthLookup(auth), io)},
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

    private class TestFactory extends WnMvcFactory {
        final List<Class<?>> entered = new ArrayList<>();
        String asUser;
        boolean stubOtpSend;

        @Override
        public WnMvcController bornController(Class<? extends WnMvcController> type) {
            WnMvcController controller;
            if (type == AppCaptchaGetController.class) {
                controller = new AppCaptchaGetController() {
                    protected WnCaptchaApi createCaptcha(WnMvcContext ctx, WnOcmInfo ocm, String captchaName) {
                        assertSame(io, ctx.io());
                        lastCaptchaName = captchaName;
                        lastCaptchaOcm = ocm;
                        return captcha;
                    }
                };
            }
            else if (type == AppCaptchaVerifyController.class) {
                controller = new AppCaptchaVerifyController() {
                    protected WnCaptchaApi createCaptcha(WnMvcContext ctx, WnOcmInfo ocm, String captchaName) {
                        assertSame(io, ctx.io());
                        lastCaptchaName = captchaName;
                        lastCaptchaOcm = ocm;
                        return captcha;
                    }
                };
            }
            else if (type == AppOtpSendController.class && stubOtpSend) {
                controller = new AppOtpSendController() {
                    protected WnMvcView send(WnMvcContext ctx, WnOcmInfo ocm, NutMap params) {
                        lastOtpContext = ctx;
                        lastOtpOcm = ocm;
                        lastOtpParams = params;
                        assertNotNull(ctx.request());
                        assertNotNull(ctx.response());
                        return WnMvcView.raw("{\"ok\":true}".getBytes(StandardCharsets.UTF_8), "text/json");
                    }
                };
            }
            else {
                controller = super.bornController(type);
            }
            return ctx -> {
                entered.add(type);
                asUser = Wn.WC().getMe() == null ? null : Wn.WC().getMe().getName();
                return controller.handle(ctx);
            };
        }
    }

    private static class FakeCaptcha implements WnCaptchaApi {
        int generated;
        int checked;
        boolean unavailable;
        boolean valid = true;
        String id;
        String data;
        String account;

        public WnCaptchaObj generate() {
            generated++;
            if (unavailable)
                throw new IllegalStateException("captcha store unavailable");
            return new WnCaptchaObj() {
                public String getId() { return "captcha-id"; }
                public String toString() { return "{\"id\":\"captcha-id\",\"image\":\"fixture\"}"; }
                public void fromString(String input) { throw new UnsupportedOperationException(); }
            };
        }
        public boolean check(String id, String data) {
            checked++;
            this.id = id;
            this.data = data;
            return valid;
        }
        public String createTicket(String account) {
            this.account = account;
            return "captcha-ticket";
        }
        public boolean checkTicket(String ticket, String account) { return valid; }
    }

    private static class FakeAuth extends WnSimpleLoginApi {
        final Map<String, WnSimpleUser> users = new LinkedHashMap<>();
        final Map<String, WnSession> sessions = new LinkedHashMap<>();
        WnSession logoutParent;
        RuntimeException logoutFailure;
        String loggedOut;
        int created;

        FakeAuth() { super(null); }
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
            se.getEnv().put("APP_PATH", "/apps");
            sessions.put(ticket, se);
            return se;
        }
        public WnUser getUser(String name) { return users.get(name); }
        public WnUser checkUser(String name) {
            WnUser user = getUser(name);
            if (user == null)
                throw Er.create("e.auth.user.NoExists", name);
            return user;
        }
        public WnSession getSession(String ticket) { return sessions.get(ticket); }
        public WnSession checkSession(String ticket) {
            WnSession se = getSession(ticket);
            if (se == null)
                throw Er.create("e.auth.session.NoExists", ticket);
            return se;
        }
        public void touchSession(WnSession se) {}
        public WnSession createSession(WnUser user, String type, int duration) {
            created++;
            return newSession(user, "new-ticket");
        }
        public WnSession logout(String ticket) {
            if (logoutFailure != null)
                throw logoutFailure;
            loggedOut = ticket;
            return logoutParent;
        }
        public WnRoleLoader roleLoader(WnSession se) {
            return new WnRoleLoader(null, null) {
                public WnRoleList getRoles(WnUser user) { return new WnRoleList(Collections.emptyList()); }
                public WnRoleType getRoleTypeOfMainGroup(WnUser user) { return WnRoleType.ADMIN; }
            };
        }
    }

    private class FakeApps extends WnAppService {
        int checked;
        int profiles;
        RuntimeException failure;
        String stdin;
        String pwd;
        String separator;
        boolean stderr;
        boolean mixed;
        boolean logOff;

        public WnApp checkApp(String name, WnLoginApi auth) {
            checked++;
            if (!"demo".equals(name))
                throw Er.create("e.app.noexists", name);
            WnApp app = new WnApp();
            app.setName(name);
            app.setHome(io.objects.get("/apps/demo"));
            app.setSession(session);
            return app;
        }
        public String renderAppHtml(WnApp app) { return "<html>demo</html>"; }
        public void tryRunProfile(WnSession se) { profiles++; }
        public void runCommand(WnApp app, String separator, String pwd, String cmd,
                               OutputStream out, OutputStream err, InputStream ins) {
            if (failure != null)
                throw failure;
            this.separator = separator;
            this.pwd = pwd;
            this.logOff = Wn.WC().isLogOff();
            try {
                stdin = ins == null ? null : new String(ins.readAllBytes(), StandardCharsets.UTF_8);
                if (!stderr || mixed)
                    out.write(("OUT:" + cmd).getBytes(StandardCharsets.UTF_8));
                if (stderr)
                    err.write(("ERR:" + cmd).getBytes(StandardCharsets.UTF_8));
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static class FakeIo extends WnIoSaftyWrapper {
        final Map<String, WnObj> objects = new LinkedHashMap<>();
        final Map<String, byte[]> contents = new LinkedHashMap<>();
        RuntimeException failure;
        int streamReads;
        boolean streamFailure;

        WnObj add(String path, WnRace race, String text) {
            WnObj obj = new WnIoObj();
            obj.id(path).path(path).name(path.substring(path.lastIndexOf('/') + 1));
            obj.race(race).creator("tester").group("tester").mode(0777);
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
            if (failure != null)
                throw failure;
            return objects.get(parent == null ? path : Wn.appendPath(parent.path(), path));
        }
        public WnObj check(WnObj parent, String path) {
            WnObj obj = fetch(parent, path);
            if (obj == null)
                throw Er.create("e.io.noexists", path);
            return obj;
        }
        public String readText(WnObj obj) { return new String(contents.get(obj.path()), StandardCharsets.UTF_8); }
        public InputStream getInputStream(WnObj obj, long offset) {
            streamReads++;
            if (streamFailure) {
                return new InputStream() {
                    public int read() throws IOException {
                        throw new IOException("fixture read failure");
                    }
                };
            }
            byte[] bytes = contents.get(obj.path());
            return new ByteArrayInputStream(bytes, (int) offset, bytes.length - (int) offset);
        }
    }

    private static class FakeLookupMaker extends WnLookupMaker {
        String name;
        String id;
        String hint;
        int limit;
        int cleared;
        RuntimeException failure;

        FakeLookupMaker(FakeIo io) { super(io); }
        public void clear() { cleared++; }
        public WnLookup getLookup(String name) {
            if (failure != null)
                throw failure;
            this.name = name;
            return new WnLookup() {
                public List<NutBean> lookup(String hint, int limit) {
                    FakeLookupMaker.this.hint = hint;
                    FakeLookupMaker.this.limit = limit;
                    return Arrays.asList(new NutMap("hint", hint));
                }
                public List<NutBean> fetch(String id) {
                    FakeLookupMaker.this.id = id;
                    return Arrays.asList(new NutMap("id", id));
                }
            };
        }
    }
}
