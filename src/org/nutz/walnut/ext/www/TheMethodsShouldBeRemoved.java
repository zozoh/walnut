package org.nutz.walnut.ext.www;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.captcha.Captchas;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.ext.vcode.VCodes;
import org.nutz.walnut.ext.vcode.WnVCodeService;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.module.AbstractWnModule;

public class TheMethodsShouldBeRemoved extends AbstractWnModule {

    @Inject
    private WnVCodeService vcodes;

    /**
     * 图形验证码的有效期(分钟）默认 1 分钟
     */
    @Inject("java:$conf.getInt('vcode-du-phone',1)")
    private int vcodeDuCaptcha;

    /**
     * 手机验证码的有效期(分钟）默认 10 分钟
     */
    @Inject("java:$conf.getInt('vcode-du-phone',10)")
    private int vcodeDuPhone;

    /**
     * 手机验证码的有效期(分钟）默认 86400 秒
     */
    @Inject("java:$conf.getLong('session-du',86400)")
    protected long sessionDu;

    @At("/vcode/captcha/?/?")
    @Ok("raw:image/png")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public byte[] vcode_captcha_get(String wwwId, String accountName) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();

        // 获取
        String vcodePath = VCodes.getCaptchaPath(domain, accountName);
        String code = R.captchaNumber(4);

        // 保存:图形验证码只有一次机会
        vcodes.save(vcodePath, code, this.vcodeDuCaptcha, 1);

        // 返回成功
        return Captchas.genPng(code, 100, 50, Captchas.NOISE);
    }

    @At("/vcode/phone/?/?")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public boolean vcode_phone_get(String wwwId,
                                   String phone,
                                   @Param("s") String scene,
                                   @Param("t") String token,
                                   @Param("c") int c) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();

        // 默认场景就是 login
        scene = Strings.sBlank(scene, "login");

        // 首先验证一下图片验证码
        String vcodePath = VCodes.getCaptchaPath(domain, phone);
        if (!vcodes.checkAndRemove(vcodePath, token)) {
            return false;
        }

        // 生成手机验证码
        vcodePath = VCodes.getPathBy(domain, scene, phone);
        String code = R.captchaNumber(c >= 4 ? 4 : 6);

        // 手机短信验证码最多重试 5 次
        vcodes.save(vcodePath, code, this.vcodeDuPhone, 5);

        // 发送短信
        String cmdText = String.format("sms -r '%s' -t 'i18n:%s' 'min:%d,code:\"%s\"'",
                                       phone,
                                       scene,
                                       this.vcodeDuPhone,
                                       code);
        String re = this.exec("vcode_phone_get", domain, cmdText);

        // 出现意外
        if (!Strings.isBlank(re))
            throw Er.create("e.vcode.phone.get", re);

        // 成功
        return true;
    }

    /**
     * 根据手机号登录。如果用户不存在，则创建一个（相当于注册）
     * 
     * @param wwwId
     *            WWW 目录 ID
     * @param phone
     *            手机号
     * @param vcode
     *            手机验证码
     * @param ticket
     *            会话的票据，null 表示当前不在一个会话里
     * @return 创建成功的会话信息
     */
    @At("/u/login/phone/?")
    @Ok("++cookie->ajax:www=${sid}/${nm}")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public NutBean do_login_by_phone(String wwwId,
                                     @Param("s") String scene,
                                     @Param("a") String phone,
                                     @Param("v") String vcode,
                                     @Param("t") String ticket,
                                     HttpServletRequest req) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();
        String siteId = oWWW.getString("hm_site_id");

        // 默认场景就是 login
        scene = Strings.sBlank(scene, "login");

        // 首先验证手机的短信密码是否正确
        String vcodePath = VCodes.getPathBy(domain, scene, phone);
        if (!vcodes.checkAndRemove(vcodePath, vcode)) {
            throw Er.create("e.www.login.invalid_vcode");
        }

        // 采用 domain 用户的权限，执行会话的创建等逻辑
        WnAccount me = auth.checkAccount(domain);
        WnObj oHome = io.check(null, me.getHomePath());
        return Wn.WC().su(me, new Proton<NutBean>() {
            @Override
            protected NutBean exec() {
                return __do_login_by_phone_as_domain_user(phone, oWWW, siteId, ticket, oHome, req);
            }
        });
    }

    /**
     * 执行手机短信密码登录的主要逻辑
     * 
     * @see #do_login_by_phone(String, String, String)
     */
    private NutBean __do_login_by_phone_as_domain_user(String phone,
                                                       WnObj oWWW,
                                                       String siteId,
                                                       String ticket,
                                                       WnObj oHome,
                                                       HttpServletRequest req) {
        // 找到用户表，看看有没有这个用户
        WnThingService ths = __gen_account_service(oWWW);

        // 根据手机号获取用户
        WnObj oU = __get_usr_by_phone(ths, phone);

        // 查看一下当前，如果已经有一个会话了，那么可能是微信那套逻辑，自动创建的会话
        NutMap context = this.__gen_www_context(req, oWWW, null, null);
        WWWPageAPI api = _api_page(oHome, oWWW, context);

        // 如果没有用户，就传一个手机号进去
        WnObj obj = oU;
        if (null == obj) {
            obj = new WnBean();
            obj.setv("phone", phone);
        }
        WnObj oSe = __try_merge_with_session_user(api, obj, siteId, ticket);

        // 嗯，不用创建会话了
        if (null != oSe)
            return api.genSessionMap(oSe);

        // 嗯，那么就创建一个用户咯
        if (null == oU) {
            oU = ths.createThing(Lang.map("phone", phone).setv("th_nm", "anonymous"));
            // 设置默认密码 123456
            // 根据 siteId 获取一下对应域名
            String domain = oWWW.d1();
            exec("setpasswd", domain, "passwd -u id:" + oU.id() + " 123456");
        }

        // 为创建会话
        return __create_session(oHome, siteId, oU);
    }

    private WnObj __get_usr_by_phone(WnThingService ths, String phone) {
        ThQuery tq = new ThQuery();
        NutMap meta = Lang.map("phone", phone);
        tq.qStr = Json.toJson(meta);
        WnObj oU = ths.getOne(tq);
        return oU;
    }

    private WnThingService __gen_account_service(WnObj oWWW) {
        String tsId = oWWW.getString("hm_account_set");
        if (Strings.isBlank(tsId)) {
            throw Er.create("e.www.login.no_account_set");
        }
        WnObj oUset = io.checkById(tsId);
        WnThingService ths = new WnThingService(io, oUset);
        return ths;
    }

    private NutBean __create_session(WnObj oHome, String siteId, WnObj oU) {
        WWWAPI api = _api(oHome);
        return api.createSession(siteId, oU);
    }

    /**
     * 根据手机号登录。如果用户不存在，则创建一个（相当于注册）
     * 
     * @param wwwId
     *            WWW 目录 ID
     * @param phone
     *            手机号
     * @param passwd
     *            登录密码
     * @param ticket
     *            会话的票据，null 表示当前不在一个会话里
     * @return 创建成功的会话信息
     */
    @At("/u/login/passwd/?")
    @Ok("++cookie->ajax:www=${sid}/${nm}")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public NutBean do_login_by_passwd(String wwwId,
                                      @Param("a") String phone,
                                      @Param("w") String passwd,
                                      @Param("t") String ticket,
                                      HttpServletRequest req) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();
        String siteId = oWWW.getString("hm_site_id");

        // 采用 domain 用户的权限，执行会话的创建等逻辑
        WnAccount me = auth.checkAccount(domain);
        WnObj oHome = io.check(null, me.getHomePath());
        return Wn.WC().su(me, new Proton<NutBean>() {
            @Override
            protected NutBean exec() {
                return __do_login_by_passwd_as_domain_user(phone,
                                                           passwd,
                                                           oWWW,
                                                           siteId,
                                                           ticket,
                                                           oHome,
                                                           req);
            }
        });
    }

    @At("/u/update/meta/?")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public NutBean u_update_meta(String wwwId,
                                 @Param("a") String phone,
                                 @Param("m") String meta,
                                 HttpServletRequest req) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();
        // 采用 domain 用户的权限，执行会话的创建等逻辑
        WnAccount me = auth.checkAccount(domain);
        return Wn.WC().su(me, new Proton<NutBean>() {
            @Override
            protected NutBean exec() {
                return __do_modify_user_meta(phone, meta, oWWW);
            }
        });
    }

    private NutBean __do_modify_user_meta(String phone, String meta, WnObj oWWW) {
        // 找到用户表，看看有没有这个用户
        WnThingService ths = __gen_account_service(oWWW);
        // 如果没有的话，不行啊，抛一个错
        WnObj oU = __get_usr_by_phone(ths, phone);
        if (null == oU) {
            throw Er.create("e.www.login.account_noexists");
        }
        // 更新属性
        NutMap umate = Lang.map(meta);
        NutBean umateSafe = umate.pickBy("!^(id|race|tp|mime|pid|d0|d1|c|m|g|md|ph|passwd|salt|th_live|th_set.*|_.*)$");
        io.appendMeta(oU, umateSafe);
        // 重新读一次
        return __get_usr_by_phone(ths,
                                  phone).pickBy("!^(id|race|tp|mime|pid|d0|d1|c|m|g|md|ph|passwd|salt|th_live|th_set.*|_.*)$");
    }

    @At("/u/modify/passwd/?")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public NutBean u_modify_passwd(String wwwId,
                                   @Param("a") String phone,
                                   @Param("o") String opasswd,
                                   @Param("n") String npasswd,
                                   HttpServletRequest req) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();
        // 采用 domain 用户的权限，执行会话的创建等逻辑
        WnAccount me = auth.checkAccount(domain);
        return Wn.WC().su(me, new Proton<NutBean>() {
            @Override
            protected NutBean exec() {
                return __do_modify_user_passwd(phone, opasswd, npasswd, oWWW);
            }
        });
    }

    private NutBean __do_modify_user_passwd(String phone,
                                            String opasswd,
                                            String npasswd,
                                            WnObj oWWW) {
        // 找到用户表，看看有没有这个用户
        WnThingService ths = __gen_account_service(oWWW);
        // 如果没有的话，不行啊，抛一个错
        WnObj oU = __get_usr_by_phone(ths, phone);
        if (null == oU) {
            throw Er.create("e.www.login.account_noexists");
        }
        // 验证一下用户名和密码
        String db_passwd = oU.getString("passwd");
        String salt = oU.getString("salt");
        String salt_pass = Wn.genSaltPassword(opasswd, salt);
        if (!salt_pass.equals(db_passwd)) {
            throw Er.create("e.www.modify.error_passwd");
        }
        // 设置新密码
        String domain = oWWW.d1();
        exec("setpasswd", domain, "passwd -u id:" + oU.id() + " " + npasswd);
        return oU.pickBy("!^(id|race|tp|mime|pid|d0|d1|c|m|g|md|ph|passwd|salt|th_live|th_set.*|_.*)$");
    }

    /**
     * 执行用户名密码登录的主要逻辑
     * 
     * @throws URISyntaxException
     * 
     * @see #do_login_by_passwd(String, String, String)
     */
    private NutBean __do_login_by_passwd_as_domain_user(String phone,
                                                        String passwd,
                                                        WnObj oWWW,
                                                        String siteId,
                                                        String ticket,
                                                        WnObj oHome,
                                                        HttpServletRequest req) {
        // 找到用户表，看看有没有这个用户
        WnThingService ths = __gen_account_service(oWWW);

        // 如果没有的话，不行啊，抛一个错
        WnObj oU = __get_usr_by_phone(ths, phone);
        if (null == oU) {
            throw Er.create("e.www.login.account_noexists");
        }

        // 验证一下用户名和密码
        String db_passwd = oU.getString("passwd");
        String salt = oU.getString("salt");
        String salt_pass = Wn.genSaltPassword(passwd, salt);
        if (!salt_pass.equals(db_passwd)) {
            throw Er.create("e.www.login.invalid_passwd");
        }

        // 查看一下当前，如果已经有一个会话了，那么可能是微信那套逻辑，自动创建的会话
        NutMap context = this.__gen_www_context(req, oWWW, null, null);
        WWWPageAPI api = _api_page(oHome, oWWW, context);
        WnObj oSe = __try_merge_with_session_user(api, oU, siteId, ticket);

        // 嗯，不用创建会话了
        if (null != oSe)
            return api.genSessionMap(oSe);

        // 创建会话
        return __create_session(oHome, siteId, oU);
    }

    private WnObj __try_merge_with_session_user(WWWPageAPI api,
                                                WnObj oU,
                                                String siteId,
                                                String ticket) {
        WnObj oSe = api.getSessionObj(siteId, ticket);
        if (null != oSe) {
            // 准备接口
            WnObj oAcsSet = api.getAccountSet();
            if (null == oAcsSet)
                return null;
            WnThingService accS = new WnThingService(io, oAcsSet);
            // 那么我们就应该从这个会话中找到对应的微信用户
            String uid2 = oSe.getString("uid");
            WnObj oU2 = accS.getThing(uid2, false);

            // 怎么回事？没有对应的用户，嗯，无视吧
            if (null == oU2)
                return null;

            // 根本就没有用户，只有一个手机号的话
            if (oU.has("phone") && !oU.has("id") && oU.size() == 1) {
                String phone = oU.getString("phone");
                oU = oU2;
                oU.put("phone", phone);
                io.set(oU, "^phone$");
            }
            // 将他信息（openID 之类的）复制到本用户里，然后删掉这个微信用户
            else if (!oU2.isSameId(oU)) {
                NutMap meta = new NutMap();
                for (String key : oU2.keySet()) {
                    // 除了头像，统统补一下，因为头像后面要处理
                    if (!"thumb".equals(key) && !oU.has(key)) {
                        meta.put(key, oU2.get(key));
                    }
                    // 匿名也要改
                    else if ("th_nm".equals(key) && oU.is(key, "anonymous")) {
                        meta.put(key, oU2.get(key));
                    }
                }
                // 如果有头像的话，搞一下
                if (oU2.has("thumb") && !oU.has("thumb")) {
                    WnObj oThumb2 = Wn.getObj(io, oU2.thumbnail());
                    if (null != oThumb2) {
                        WnObj oData = Things.dirTsData(io, oU);
                        String thumb = oU.id() + "/thumb.jpg";
                        WnObj oThumb = io.createIfNoExists(oData, thumb, WnRace.FILE);
                        io.copyData(oThumb2, oThumb);
                        meta.put("thumb", "id:" + oThumb.id());
                    }
                }
                // 更新一下本用户
                io.appendMeta(oU, meta);

                // 删除一下微信用户（因为没用了，硬删除就好了）
                accS.deleteThing(true, oU2.id());
            }

            // 最后复用当前会话
            api.chownSession(oSe, oU);
        }
        return oSe;
    }

    /**
     * 取消当前会话的登录
     * 
     * @param wwwId
     *            WWW 目录 ID
     * @param theURL
     *            取消登录后，要重定向到哪个地址，默认为 "/"
     * @return 重定向的地址
     * @throws URISyntaxException
     */
    @At("/u/logout/?")
    @Ok("--cookie>>:www,${obj}")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public String do_logout(String wwwId, @Param("url") String theURL, HttpServletRequest req)
            throws URISyntaxException {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();

        // 获取主目录
        WnObj oHome = io.check(null, "/home/" + domain);

        // 从请求对象得到上下文
        NutMap context = __gen_www_context(req, oWWW, null, null);

        // 准备操作接口并删除会话
        WWWPageAPI api = _api_page(oHome, oWWW, context);
        api.deleteMySession();

        // 得到要返回的 URL
        return Strings.sBlank(theURL, "/");
    }

    @At("/u/check/phone/?")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public boolean is_logined_and_has_phone(String wwwId, HttpServletRequest req)
            throws URISyntaxException {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();

        // 获取主目录
        WnObj oHome = io.check(null, "/home/" + domain);

        // 从请求对象得到上下文
        NutMap context = __gen_www_context(req, oWWW, null, null);

        WWWPageAPI api = _api_page(oHome, oWWW, context);
        return api.checkMyPhone();
    }

    /**
     * 判断某手机号是否存在
     * 
     * @param wwwId
     *            WWW 目录 ID
     * @param phone
     *            手机号
     * @return true 存在； false 不存在
     */
    @At("/u/exists/phone/?/?")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public boolean is_phone_exists(String wwwId, String phone) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);

        // 找到用户表，看看有没有这个用户
        WnThingService ths = __gen_account_service(oWWW);

        // 如果没有的话，就创建一个
        WnObj oU = __get_usr_by_phone(ths, phone);

        // 返回
        return null != oU;
    }

    //
    // 下面三个方法还是有用的，不要删除
    //

    protected WWWPageAPI _api_page(WnObj oHome, WnObj oWWW, NutMap context) {
        return new WWWPageAPI(io, oHome, sessionDu, oWWW, context);
    }

    protected WWWAPI _api(WnObj oHome) {
        return new WWWAPI(io, oHome, sessionDu);
    }

    protected NutMap __gen_www_context(HttpServletRequest req,
                                       WnObj oWWW,
                                       String pagePath,
                                       String a_path) {
        try {
            NutMap context = _gen_context_by_req(req);
            String rootPath = oWWW.path();
            String url = (String) req.getAttribute("wn_www_url");
            if (url == null)
                url = req.getRequestURL().toString();
            URI uri = new URI(url);
            String uriPath = uri.getPath();
            String basePath;
            // 用 pagePath
            if (!Strings.isBlank(pagePath) && uriPath.endsWith(pagePath)) {
                basePath = uriPath.substring(0, uriPath.length() - pagePath.length());
            }
            // 用 a_path 咯
            else if (!Strings.isBlank(a_path) && uriPath.endsWith(a_path)) {
                basePath = uriPath.substring(0, uriPath.length() - a_path.length());
            }
            // 嗯，没招了
            else {
                basePath = uriPath;
            }

            context.put("WWW", oWWW.pickBy("^(id|hm_.+)$"));
            context.put("SITE_HOME", rootPath);
            context.put("PAGE_PATH", pagePath);
            context.put("URL", url);
            context.put("URI_PATH", uriPath);
            context.put("URI_BASE", basePath);

            return context;
        }
        catch (URISyntaxException e) {
            throw Er.wrap(e);
        }
    }
}
