package com.site0.walnut.web.module.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.log.WnLog;
import org.nutz.mvc.View;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.security.captcha.WnCaptchaBuilder;
import com.site0.walnut.security.captcha.WnCaptchaObj;
import com.site0.walnut.security.captcha.impl.WnSysCaptchaBuilder;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;

/**
 * 对应入口函数 <code>AppModule.captcha_get</code> /
 * <code>AppModule.captcha_verify</code> 的服务类：人机验证码（captcha）
 * 的生成与校验。
 *
 * <p>
 * 配置目录取自 {@link WamOcmInfo#getConfHomePath()}（登录模式决定，
 * 规则见 <code>62-auth-with-captcha-otp.md</code>）：验证码配置位于
 * <code>{confHome}/.captcha/{name}.json</code>（配置名缺省
 * <code>"default"</code>）。每次请求都现场构建 {@link WnCaptchaBuilder}，
 * 不做缓存——构建代价小，且能始终读到最新的配置。
 *
 * <p>
 * 返回统一用 {@link AjaxReturn} 包裹：成功 <code>{ok:true, data:...}</code>，
 * 失败 <code>{ok:false, errCode, msg}</code>——服务故障与「校验不通过」
 * 客户端可以明确区分。<code>get</code> 的 <code>data</code> 是
 * {@link WnCaptchaObj} 的 JSON（含 <code>id</code> 与渲染数据），客户端
 * 拿到即可绘制；<code>verify</code> 的 <code>data</code> 是
 * <code>{ticket:...}</code>。verify 校验用户提交的行为数据，通过即作废
 * （一发一验），签发的临时票据（一次性，带过期）由客户端在后续业务
 * 请求（如 OTP 发送 / 登录提交）里带上 <code>ticket + account</code>，
 * 由业务侧 <code>checkTicket</code> 消费。
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WamCaptcha {

    private static final WnLog log = Wlog.getAPP();

    private WnIo io;

    /**
     * 本次操作的信息：配置主目录、用户等（由入口函数经
     * <code>AppModule.get_ocm_info(domain, account)</code> 解析）
     */
    private WamOcmInfo ocm;

    /**
     * 验证码配置名（对应 <code>{confHome}/.captcha/{name}.json</code>），缺省
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
        String reJson;
        try {
            WnCaptchaApi api = this.__check_api();
            WnCaptchaObj obj = api.generate();
            // 统一用 AjaxReturn 包裹：{ok:true, data: WnCaptchaObj 的 JSON}
            reJson = String.format("{\"ok\":true, \"data\":%s}",
                                   obj.toString());
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
     * @return 成功 <code>{"ok":true,"data":{"ticket":"..."}}</code>；失败
     *         <code>{"ok":false,"errCode":...}</code>
     */
    public View verify() {
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
            NutMap data = new NutMap("ticket", ticket);
            AjaxReturn re = Ajax.ok().setData(data);
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
     * @return 当前登录模式下现场构建的验证码服务接口
     */
    private WnCaptchaApi __check_api() {
        String ccName = Ws.sBlank(captchaName, "default");
        NutMap vars = new NutMap();
        vars.put("HOME", ocm.getConfHomePath());
        WnCaptchaBuilder builder = new WnSysCaptchaBuilder(this.io, vars);
        return builder.build(ccName);
    }

    /**
     * @param errCode
     *            错误码
     * @param e
     *            异常（可为 <code>null</code>，其 message 会写入 <code>msg</code> 字段）
     * @return 失败 JSON 字符串
     */
    private String __fail_json(String errCode, Exception e) {
        AjaxReturn re = Ajax.fail().setErrCode(errCode);
        if (null != e) {
            re.setMsg(e.getMessage());
        }
        return Json.toJson(re, JsonFormat.compact());
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public WamOcmInfo getOcm() {
        return ocm;
    }

    public void setOcm(WamOcmInfo ocm) {
        this.ocm = ocm;
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
