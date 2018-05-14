package org.nutz.walnut.ext.www;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.view.HttpServerResponse;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.ext.captcha.Captchas;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.ext.vcode.VCodes;
import org.nutz.walnut.ext.vcode.WnVCodeService;
import org.nutz.walnut.impl.box.Jvms;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.module.AbstractWnModule;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnObjDownloadView;

@IocBean
@At("/www")
@Ok("void")
@Fail("void")
public class WWWModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    // private static final Pattern _P = Pattern.compile("^([^/]+)(/(.+))?$");

    private static final List<String> ENTRIES = Lang.list("index.wnml", "index.html");

    private Tmpl tmpl_400;
    private Tmpl tmpl_404;
    private Tmpl tmpl_500;

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
    private long sessionDu;

    public WWWModule() {
        tmpl_400 = Tmpl.parse(Files.read("html/400.wnml"));
        tmpl_404 = Tmpl.parse(Files.read("html/404.wnml"));
        tmpl_500 = Tmpl.parse(Files.read("html/500.wnml"));
    }

    @Deprecated
    @At("/?/_usr/do/login")
    @Ok("++cookie>>:" + WWW.AT_SEID + "=${dseid},${obj.url}")
    @Fail(">>:${obj.url}")
    public NutMap u_do_login(String grp,
                             @Param("str") String str,
                             @Param("passwd") String passwd,
                             HttpServletResponse resp) {
        // 根据传入的用户名（邮箱，手机号）密码创建一个 Map 以便分析
        NutMap u = __gen_u_map(str, passwd);

        // 执行创建
        StringBuilder sbOut = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        String input = Json.toJson(u, JsonFormat.compact());
        this.exec("duc", grp, input, "dusr -login", sbOut, sbErr);

        // 读取配置信息
        NutMap conf = WWW.read_conf(io, grp);

        // 准备返回值
        NutMap re = new NutMap();

        // 如果失败
        if (sbErr.length() > 0) {
            re.put("url", conf.getString("login_fail", "/login_fail.wnml"));
        }
        // 成功的话
        else {
            re.put("url", conf.getString("login_ok", "/"));
            re.put("dseid", Strings.trim(sbOut));
        }

        // 返回
        return re;
    }

    @Deprecated
    @At("/?/_usr/do/logout")
    @Ok("--cookie>>:" + WWW.AT_SEID + ",${obj}")
    @Fail(">>:${obj}")
    @Filters({@By(type = WWWSetSessionID.class)})
    public String u_do_logout(String grp) {
        String dseid = Wn.WC().getString(WWW.AT_SEID);
        if (null != dseid) {
            this.exec("duc", grp, "dusr -logout " + dseid);
        }

        // 读取配置信息，决定重定向到什么 URL
        NutMap conf = WWW.read_conf(io, grp);
        return conf.getString("logout", "/");
    }

    @Inject("java:$conf.get('usr-name')")
    private Pattern regexName;

    @Inject("java:$conf.get('usr-phone')")
    private Pattern regexPhone;

    @Inject("java:$conf.get('usr-email')")
    private Pattern regexEmail;

    @At("/?/_usr/create")
    @Ok(">>:${obj}")
    @Fail(">>:${obj}")
    public String u_do_create(String grp,
                              @Param("str") String str,
                              @Param("passwd") String passwd) {

        // 根据传入的用户名（邮箱，手机号）密码创建一个 Map 以便分析
        NutMap u = __gen_u_map(str, passwd);

        // 执行创建
        StringBuilder sbOut = new StringBuilder();
        StringBuilder sbErr = new StringBuilder();
        String input = Json.toJson(u, JsonFormat.compact());
        this.exec("duc", grp, input, "dusr -create", sbOut, sbErr);

        // 读取配置信息
        NutMap conf = WWW.read_conf(io, grp);

        // 如果失败
        if (sbErr.length() > 0) {
            return conf.getString("create_fail", "/create_fail.wnml");
        }

        // 成功的话
        return conf.getString("create_ok", "/");
    }

    @Deprecated
    private NutMap __gen_u_map(String str, String passwd) {
        NutMap u = new NutMap();

        // 根据传入的字符串自动判断是该设置电话 ...
        if (regexPhone.matcher(str).find()) {
            u.put("mobile", str);
        }
        // 还是邮箱
        else if (regexEmail.matcher(str).find()) {
            u.put("email", str);
        }
        // 还是登录名
        else if (regexName.matcher(str).find()) {
            u.put("name", str);
        }
        // 不合法
        else {
            throw Er.create("e.d.u.create.invalidStr", str);
        }

        // 记录密码
        u.put("passwd", passwd);
        return u;
    }

    @At("/vcode/captcha/?/?")
    @Ok("raw:image/png")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
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
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public boolean vcode_phone_get(String wwwId,
                                   String phone,
                                   @Param("s") String scene,
                                   @Param("t") String token) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();
        String siteId = oWWW.getString("hm_site_id");

        // 默认场景就是 login
        scene = Strings.sBlank(scene, "login_" + siteId);

        // 首先验证一下图片验证码
        String vcodePath = VCodes.getCaptchaPath(domain, phone);
        if (!vcodes.checkAndRemove(vcodePath, token)) {
            return false;
        }

        // 生成手机验证码
        vcodePath = VCodes.getPathBy(domain, scene, phone);
        String code = R.captchaNumber(6);

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
     * @return 创建成功的会话信息
     */
    @At("/u/login/phone/?")
    @Ok("++cookie->ajax:www=${siteId}/${ticket}")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public NutMap do_login_by_phone(String wwwId,
                                    @Param("s") String scene,
                                    @Param("a") String phone,
                                    @Param("v") String vcode) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();
        String siteId = oWWW.getString("hm_site_id");

        // 默认场景就是 login
        scene = Strings.sBlank(scene, "login_" + siteId);

        // 首先验证手机的短信密码是否正确
        String vcodePath = VCodes.getPathBy(domain, scene, phone);
        if (!vcodes.checkAndRemove(vcodePath, vcode)) {
            throw Er.create("e.www.login.invalid_vcode");
        }

        // 采用 domain 用户的权限，执行会话的创建等逻辑
        WnUsr me = usrs.check(domain);
        WnObj oHome = io.check(null, me.home());
        return Wn.WC().su(me, new Proton<NutMap>() {
            protected NutMap exec() {
                return __do_login_by_phone_as_domain_user(phone, oWWW, siteId, oHome);
            }
        });
    }

    /**
     * 执行手机短信密码登录的主要逻辑
     * 
     * @see #do_login_by_phone(String, String, String)
     */
    private NutMap __do_login_by_phone_as_domain_user(String phone,
                                                      WnObj oWWW,
                                                      String siteId,
                                                      WnObj oHome) {
        // 找到用户表，看看有没有这个用户
        WnThingService ths = __gen_account_service(oWWW);

        // 如果没有的话，就创建一个
        WnObj oU = __get_usr_by_phone(ths, phone);
        if (null == oU) {
            oU = ths.createThing("anonymous", Lang.map("phone", phone));
        }

        // 创建会话
        return __create_session(siteId, oHome, oU);
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

    private NutMap __create_session(String siteId, WnObj oHome, WnObj oU) {
        // 得到会话主目录
        String path = Wn.appendPath(".hmaker/session/", siteId);
        WnObj oSset = io.createIfNoExists(oHome, path, WnRace.DIR);

        // 看看是否已经存在了这个用户，如果存在那么删掉这个会会话
        WnQuery q = Wn.Q.pid(oSset);
        q.setv("uid", oU.id());
        List<WnObj> oSothers = io.query(q);
        for (WnObj oSother : oSothers) {
            io.delete(oSother);
        }

        // 嗯嗯，登陆，创建会话
        String ticket = R.UU64(); // 准备一个票据
        WnObj oSe = io.create(oSset, ticket, WnRace.FILE);

        // 更新会话
        NutMap map = new NutMap();
        map.put("expi", System.currentTimeMillis() + (this.sessionDu * 1000));
        map.put("uid", oU.id());
        map.put("unm", oU.name());
        io.appendMeta(oSe, map);

        // 修改用户的最后登录时间
        oU.setv("login", oSe.lastModified());
        io.set(oU, "^login$");

        // 准备返回对象
        map.put("siteId", siteId);
        map.put("ticket", oSe.name());
        return map;
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
     * @return 创建成功的会话信息
     */
    @At("/u/login/passwd/?")
    @Ok("++cookie->ajax:www=${siteId}/${ticket}")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public NutMap do_login_by_passwd(String wwwId,
                                     @Param("a") String phone,
                                     @Param("w") String passwd) {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();
        String siteId = oWWW.getString("hm_site_id");

        // 采用 domain 用户的权限，执行会话的创建等逻辑
        WnUsr me = usrs.check(domain);
        WnObj oHome = io.check(null, me.home());
        return Wn.WC().su(me, new Proton<NutMap>() {
            protected NutMap exec() {
                return __do_login_by_passwd_as_domain_user(phone, passwd, oWWW, siteId, oHome);
            }
        });
    }

    /**
     * 执行用户名密码登录的主要逻辑
     * 
     * @see #do_login_by_passwd(String, String, String)
     */
    private NutMap __do_login_by_passwd_as_domain_user(String phone,
                                                       String passwd,
                                                       WnObj oWWW,
                                                       String siteId,
                                                       WnObj oHome) {
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

        // 创建会话
        return __create_session(siteId, oHome, oU);
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
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public String do_logout(String wwwId, @Param("url") String theURL, HttpServletRequest req)
            throws URISyntaxException {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();

        // 获取主目录
        WnObj oHome = io.check(null, "/home/" + domain);

        // 从请求对象得到上下文
        NutMap context = __gen_www_context(req, oWWW, null);

        // 准备操作接口并删除会话
        WWWPageAPI api = new WWWPageAPI(io, oHome, oWWW, context);
        api.deleteMySession();

        // 得到要返回的 URL
        return Strings.sBlank(theURL, "/");
    }

    @At("/u/check/phone/?")
    @Ok("ajax")
    @Fail("ajax")
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
    public boolean is_logined_and_has_phone(String wwwId, HttpServletRequest req)
            throws URISyntaxException {
        // 根据 siteId 获取一下对应域名
        WnObj oWWW = io.checkById(wwwId);
        String domain = oWWW.d1();

        // 获取主目录
        WnObj oHome = io.check(null, "/home/" + domain);

        // 从请求对象得到上下文
        NutMap context = __gen_www_context(req, oWWW, null);

        // 准备操作接口
        WWWPageAPI api = new WWWPageAPI(io, oHome, oWWW, context);
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
    @Filters(@By(type = WnAsUsr.class, args = {"root", "root"}))
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

    @At("/?/**")
    @Filters({@By(type = WWWSetSessionID.class)})
    public View show_page(String usr,
                          String a_path,
                          @Param("down") boolean isDownload,
                          @ReqHeader("User-Agent") String ua,
                          @ReqHeader("If-None-Match") String etag,
                          @ReqHeader("Range") String range,
                          HttpServletRequest req,
                          HttpServletResponse resp) {
        // 如果有的话，去掉开头的绝对路径符
        if (null == a_path) {
            a_path = "";
        }
        // 得到相对路径
        else if (a_path.startsWith("/")) {
            a_path = a_path.substring(1);
        }

        if (log.isInfoEnabled())
            log.infof("www(%s): /%s/%s", req.getRemoteAddr(), usr, a_path);

        // ..............................................
        // 找到用户
        WnUsr u = usrs.check(usr);
        String homePath = Strings.sBlank(u.home(), "/home/" + u.name());
        WnObj oHome = io.fetch(null, homePath);

        if (log.isDebugEnabled())
            log.debugf(" - www:usrHome: %s : [%s]", homePath, oHome);

        if (null == oHome) {
            return gen_errpage(tmpl_404, a_path, "Home not exists!", 404);
        }

        // ..............................................
        // 准备起始查询条件: 要找 www 的目录，复制给 oROOT
        WnObj oWWW = null;
        WnQuery q = new WnQuery();
        q.setv("d0", oHome.d0());
        if (!"root".equals(usr))
            q.setv("d1", oHome.d1());

        if (log.isDebugEnabled())
            log.debugf(" - www:query: %s", q.toString());

        // 请求里带了 host 了吗
        Object host = req.getAttribute("wn_www_host");
        if (null != host && !"localhost".equals(host) && !"127.0.0.1".equals(host)) {
            q.setv("www", host.toString());
            oWWW = io.getOne(q);
        }
        if (log.isDebugEnabled())
            log.debugf(" - www:regHost: %s -> %s", host, oWWW);

        // 实在找不到用 www 目录
        if (null == oWWW) {
            oWWW = io.getOne(q.setv("www", "ROOT"));
        }

        if (log.isDebugEnabled())
            log.debugf(" - www:=ROOT: %s -> %s", host, oWWW);

        // 发布目录不存在
        if (null == oWWW) {
            return gen_errpage(tmpl_404, a_path);
        }

        // ..............................................
        // 通过 www 目录找到文件对象
        WnObj o = null;

        // 空路径的话，那么意味着对象是 ROOT
        if (Strings.isBlank(a_path)) {
            o = oWWW;
        }
        // 否则如果有 ROOT 再其内查找
        else if (null != oWWW) {
            o = io.fetch(oWWW, a_path);
        }

        if (log.isDebugEnabled())
            log.debugf(" - www:findObj: %s -> %s", a_path, o);

        // 文件对象不存在，直接 404 咯
        if (null == o) {
            return gen_errpage(tmpl_404, a_path);
        }

        // ..............................................
        // 根据目录找到对应的页面
        // ..............................................
        // 目录的话，依次上传入口
        if (o.isDIR()) {
            // 获取入口网页的可能列表
            List<String> entries = ENTRIES;
            if (null != oWWW) {
                Object eno = oWWW.get("www_entry");
                List<String> en_list = new ArrayList<String>(6);
                Lang.each(eno, new Each<String>() {
                    public void invoke(int index, String ele, int length) {
                        if (!Strings.isBlank(ele))
                            en_list.add(ele);
                    }
                });
                if (en_list.size() > 0)
                    entries = en_list;
            }
            // 依次尝试入口对象
            for (String entry : entries) {
                WnObj o2 = io.fetch(o, entry);
                if (null != o2 && o2.isFILE()) {
                    o = o2;
                    break;
                }
            }

            if (log.isDebugEnabled())
                log.debugf(" - www:findEntry: %s", o);

            // 还是目录，那就抛错吧
            if (o.isDIR()) {
                return gen_errpage(tmpl_400, a_path);
            }
            // 如果不是目录，那么应该返回一个重定向
            // 否则在访问 http://zozoh.com/abc 这样路径的时候，
            // 路径对应的网页里面如果有相对的图片链接，会有问题
            String redirectPath;
            Object orgPath = req.getAttribute("wn_www_path_org");

            // 嗯，不是从 WalnutFilter 过来的
            if (null == orgPath) {
                redirectPath = Wn.appendPath("/www", usr, a_path, o.name());
            }
            // 从 WalnutFilter 过来的，直接使用原始路径
            else {
                redirectPath = Wn.appendPath(orgPath.toString(), o.name());
            }

            if (log.isDebugEnabled())
                log.debugf(" - www:redirect-> %s", redirectPath);

            // 重定向吧
            return new ServerRedirectView(redirectPath);
        }

        // 确保可读，同时处理链接文件
        o = Wn.WC().whenRead(o, false);

        // 渲染这个文件对象
        try {
            // 动态网页
            boolean isDynamic = o.is("as_wnml", true);
            if (!isDynamic)
                isDynamic = o.isType("wnml");
            // 执行命令
            if (isDynamic) {
                if (log.isDebugEnabled())
                    log.debugf(" - www.$ (%s)@%s : %s", o.id(), usr, a_path);

                // 首先创建一个会话
                WnSession se = this.creatSession(usr);

                // 得到文件内容
                String input = io.readText(o);

                // 计算路径
                String rootPath = oWWW.path();
                String currentPath = o.path();
                String currentDir = o.parent().path();
                String pagePath = currentPath.substring(rootPath.length());

                // 从请求对象得到上下文
                NutMap context = __gen_www_context(req, oWWW, pagePath);
                context.put("CURRENT_PATH", currentPath);
                context.put("CURRENT_DIR", currentDir);

                // 得到一些关键接口
                context.put("grp", se.group());
                context.put("fnm", o.name());
                context.put("rs", "/gu/rs");

                // 放置一些上下文的接口
                WWWPageAPI api = new WWWPageAPI(io, oHome, oWWW, context);
                context.put("API", api);

                // 看看是否是已经登录的会话，如果已经登录了，那么要偷偷改一下会话的票据
                String seph = api.updateSessionTicket();

                // 创建一下解析服务
                // WnmlModuleRuntime wrt = new WnmlModuleRuntime(this, se);
                WnBoxContext bc = createBoxContext(se);
                StringBuilder sbOut = new StringBuilder();
                StringBuilder sbErr = new StringBuilder();
                WnSystem sys = Jvms.createWnSystem(this, jef, bc, sbOut, sbErr, null);
                WnmlRuntime wrt = new JvmWnmlRuntime(sys);
                WnmlService ws = new WnmlService();

                // 执行转换
                String html = ws.invoke(wrt, context, input);

                // 准备输出响应内容
                HttpServerResponse hsr = new HttpServerResponse();
                hsr.updateBy(html);

                // 默认的内容类型
                hsr.header().putDefault("CONTENT-TYPE", "text/html");

                // 更新客户端的 cookie 以便匹配上新的 session
                if (!Strings.isBlank(seph)) {
                    hsr.header().addv("SET-COOKIE", "www=" + seph + "; Path=/;");
                }

                return new HttpStatusView(hsr);

                // // 如果以 HTTP/1.x 开头，则认为是要输出 HTTP 头
                // if (html.startsWith("HTTP/1.")) {
                //
                // }
                // if (log.isDebugEnabled())
                // log.debugf(" - www.$ (%s)@%s : %s", o.id(), usr, a_path);
                // // 返回网页
                // return new ViewWrapper(new RawView("text/html"), html);
            }
            // 其他的都是静态资源，就直接下载了
            if (log.isDebugEnabled())
                log.debugf(" - www.S (%s)@%s : %s", o.id(), usr, a_path);

            // 特殊的类型，将不生成下载目标
            ua = WnWeb.autoUserAgent(o, ua, isDownload);

            // 返回下载视图
            return new WnObjDownloadView(io, o, ua, etag, range);

        }
        catch (Exception e) {
            return gen_errpage(tmpl_500, a_path, e.toString(), 500);
        }
    }

    private NutMap __gen_www_context(HttpServletRequest req, WnObj oWWW, String pagePath)
            throws URISyntaxException {
        NutMap context = _gen_context_by_req(req);
        String rootPath = oWWW.path();
        String url = req.getRequestURL().toString();
        URI uri = new URI(url);
        String uriPath = uri.getPath();
        String basePath;
        if (!Strings.isBlank(pagePath) && uriPath.endsWith(pagePath)) {
            basePath = uriPath.substring(0, uriPath.length() - pagePath.length());
        } else {
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

    private View gen_errpage(Tmpl tmpl, String path) {
        String msg;
        int code;
        if (tmpl_400 == tmpl) {
            msg = "Invalid Request";
            code = 400;
        } else if (tmpl_404 == tmpl) {
            msg = "Page NoFound";
            code = 404;
        } else {
            msg = "Server Error";
            code = 500;
        }

        return gen_errpage(tmpl, path, msg, code);
    }

    private View gen_errpage(Tmpl tmpl, String path, String msg, int code) {
        NutMap map = Lang.map("url", path);
        map.setv("msg", msg);
        String html = tmpl.render(map, false);
        return new ViewWrapper(new RawView("text/html") {
            @Override
            public void render(HttpServletRequest req, HttpServletResponse resp, Object obj) throws Throwable {
                resp.setStatus(code);
                super.render(req, resp, obj);
            }
        }, html);
    }

}
