package com.site0.walnut.web.module.app;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.mvc.View;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ViewWrapper;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.security.captcha.WnCaptchaBuilder;
import com.site0.walnut.security.captcha.WnCaptchaObj;
import com.site0.walnut.security.captcha.impl.WnSysCaptchaBuilder;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.util.WnWeb;

/**
 * 对应入口函数 <code>AppModule.captcha_get</code> /
 * <code>AppModule.captcha_verify</code> 的服务类：人机验证码（captcha）
 * 的生成与校验。
 *
 * <p>
 * 验证码按域配置：域就是系统用户名，配置位于其家目录的
 * <code>~/.captcha/{name}.json</code>（配置名缺省 <code>"default"</code>），
 * 域为空表示 root 用户。{@link WnCaptchaBuilder} 按域懒构建并缓存——
 * 引擎是单例级别的资源，每个域一份，不反复构建。
 *
 * <p>
 * <code>get</code> 吐出 {@link WnCaptchaObj#toString()}——客户端直接渲染的
 * 验证码 JSON（含 <code>id</code> 与渲染数据）；
 * <code>verify</code> 校验用户提交的行为数据，通过即作废（一发一验），
 * 并签发一张绑定账号的临时票据（一次性，带过期）——客户端在后续业务
 * 请求（如 OTP 发送 / 登录提交）里带上 <code>ticket + account</code>，
 * 由业务侧 <code>checkTicket</code> 消费。
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAppModuleCaptcha {

    private static final Log log = Wlog.getAPP();

    /**
     * 按域（系统用户名）缓存的 captcha 构建器：不同域的家目录不同
     * （<code>~/.captcha</code> 的位置不同），每个域一份
     */
    private static final Map<String, WnCaptchaBuilder> builders = new ConcurrentHashMap<>();

    private WnLoginApi auth;

    private WnIo io;

    /**
     * 域（系统用户名），空表示 root 用户
     */
    private String domain;

    /**
     * 验证码配置名（对应 <code>~/.captcha/{name}.json</code>），缺省
     * "default"
     */
    private String captchaName;

    /**
     * 验证码的唯一标识（verify 时客户端从 get 的返回里原样带回）
     */
    private String captchaId;

    /**
     * 用户行为的采集结果（拖拽轨迹 / 点选坐标等，JSON 字符串）
     */
    private String captchaData;

    /**
     * 票据绑定的账号（后续业务提交时原样带回做比对）
     */
    private String account;

    private HttpServletRequest req;

    private HttpServletResponse resp;

    /**
     * 生成一份人机验证码，返回 {@link WnCaptchaObj} 的 JSON
     *
     * @return 验证码 JSON 视图
     */
    public View get() {
        // 允许跨域
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        String reJson;
        try {
            WnCaptchaApi api = this.__check_api();
            WnCaptchaObj obj = api.generate();
            reJson = obj.toString();
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail to generate captcha", e);
            }
            reJson = this.__fail_json("e.captcha.unavailable", e);
        }
        return new ViewWrapper(new RawView("json"), reJson);
    }

    /**
     * 校验人机验证码，通过则签发绑定账号的临时票据
     *
     * @return 成功 <code>{"ok":true,"ticket":"..."}</code>；失败
     *         <code>{"ok":false,"errCode":...}</code>
     */
    public View verify() {
        // 允许跨域
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        String reJson;
        try {
            // 票据必须绑定账号，否则后续业务没法比对
            if (Ws.isBlank(account)) {
                reJson = this.__fail_json("e.captcha.need_account", null);
                return new ViewWrapper(new RawView("json"), reJson);
            }
            WnCaptchaApi api = this.__check_api();
            // 校验行为数据，通过即作废（一发一验）
            if (!api.check(captchaId, captchaData)) {
                reJson = this.__fail_json("e.captcha.invalid", null);
                return new ViewWrapper(new RawView("json"), reJson);
            }
            // 签发临时票据：ticket -> account（一次性，带过期）
            String ticket = api.createTicket(account);
            NutMap re = new NutMap();
            re.put("ok", true);
            re.put("ticket", ticket);
            reJson = Json.toJson(re, JsonFormat.compact());
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail to verify captcha", e);
            }
            reJson = this.__fail_json("e.captcha.unavailable", e);
        }
        return new ViewWrapper(new RawView("json"), reJson);
    }

    /**
     * @return 按域（缓存）的构建器取的验证码服务接口
     */
    private WnCaptchaApi __check_api() {
        String name = Strings.sBlank(captchaName, "default");
        WnCaptchaBuilder builder = this.__check_builder();
        return builder.build(name);
    }

    /**
     * @return 当前域的 captcha 构建器（按域缓存，懒构建）
     */
    private WnCaptchaBuilder __check_builder() {
        String usrName = Strings.sBlank(domain, "root");
        return builders.computeIfAbsent(usrName, this::__create_builder);
    }

    /**
     * 为指定域构建一个 captcha 构建器：域的家目录决定
     * <code>~/.captcha</code> 的位置
     *
     * @param usrName
     *            域（系统用户名）
     * @return 新构建器
     */
    private WnCaptchaBuilder __create_builder(String usrName) {
        String usrHome = this.auth.checkUser(usrName).getHomePath();
        NutMap vars = new NutMap();
        vars.put("HOME", usrHome);
        return new WnSysCaptchaBuilder(this.io, vars);
    }

    /**
     * @param errCode
     *            错误码
     * @param e
     *            异常（可为 <code>null</code>，其 message 会写入
     *            <code>msg</code> 字段）
     * @return 失败 JSON 字符串
     */
    private String __fail_json(String errCode, Exception e) {
        NutMap re = new NutMap();
        re.put("ok", false);
        re.put("errCode", errCode);
        if (null != e) {
            re.put("msg", e.getMessage());
        }
        return Json.toJson(re, JsonFormat.compact());
    }

    public WnLoginApi getAuth() {
        return auth;
    }

    public void setAuth(WnLoginApi auth) {
        this.auth = auth;
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCaptchaName() {
        return captchaName;
    }

    public void setCaptchaName(String captchaName) {
        this.captchaName = captchaName;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaData() {
        return captchaData;
    }

    public void setCaptchaData(String captchaData) {
        this.captchaData = captchaData;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public HttpServletRequest getReq() {
        return req;
    }

    public void setReq(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public void setResp(HttpServletResponse resp) {
        this.resp = resp;
    }

}
