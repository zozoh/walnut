package com.site0.walnut.security.otp.impl.sender;

import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;
import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.mailx.bean.MailxConfig;
import com.site0.walnut.ext.net.mailx.bean.MailxSmtpConfig;
import com.site0.walnut.ext.net.mailx.bean.WnSmtpMail;
import com.site0.walnut.ext.net.mailx.impl.WnMailPosting;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOtpEmailConfig;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * 系统邮件 OTP 发送器：配置驱动，从 <code>~/.otp/{name}.json5</code> 读取
 * 发送设置（channel / subject / lang / tmpl / writeLog），
 * SMTP 通道取自 <code>~/.mailx/{channel}.json</code>
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSysEmailOTPSender extends WnEmailOTPSender {

    private static final Log log = Wlog.getAUTH();

    private WnIo io;

    private NutBean vars;

    private String name;

    // 懒加载
    private WnOtpEmailConfig config;

    private MailxSmtpConfig smtp;

    public WnSysEmailOTPSender(WnSystem sys) {
        this(sys, "default");
    }

    public WnSysEmailOTPSender(WnSystem sys, String name) {
        this(sys.io, sys.session.getEnv(), name);
    }

    public WnSysEmailOTPSender(WnIo io, NutBean sessionVars) {
        this(io, sessionVars, "default");
    }

    public WnSysEmailOTPSender(WnIo io, NutBean sessionVars, String name) {
        this.io = io;
        this.vars = sessionVars;
        this.name = Ws.sBlank(name, "default");
    }

    /**
     * @return 邮件 OTP 发送配置（懒加载，且只读取一次）
     */
    private WnOtpEmailConfig getConfig() {
        if (null == config) {
            String ph = Wn.appendPath("~/.otp", name + ".json5");
            WnObj oConf = Wn.checkObj(io, vars, ph);
            config = io.readJson(oConf, WnOtpEmailConfig.class);
            if (null == config) {
                throw Er.create("e.otp.email.BlankConfig", ph);
            }
        }
        return config;
    }

    @Override
    protected MailxSmtpConfig getSmtpConfig() {
        if (null == smtp) {
            String channel = getConfig().getChannel();
            String ph = Wn.appendPath("~/.mailx", channel + ".json");
            WnObj oConf = Wn.checkObj(io, vars, ph);
            MailxConfig mailx = io.readJson(oConf, MailxConfig.class);
            smtp = mailx.smtp;
            if (null == smtp) {
                throw Er.create("e.otp.email.WithoutSmtp", ph);
            }
        }
        return smtp;
    }

    @Override
    protected WnSmtpMail buildSmtpEmail(WnOTPBean otp) {
        WnOtpEmailConfig cfg = getConfig();

        // 邮件对象：收件人就是正在验证的账号
        WnSmtpMail mail = new WnSmtpMail();
        mail.addTo(otp.getAccount());
        mail.setSubject(cfg.getSubject());

        // 正文模板：~/.mailx/i18n/{lang}/{tmpl}
        if (cfg.hasTmpl()) {
            // 语言：OTP 配置 → SMTP 配置 → en-us
            String lang = cfg.getLang(Ws.sBlank(getSmtpConfig().getLang(), "en-us"));
            String aph = cfg.getTmplPath(lang);
            WnObj oTmpl = Wn.checkObj(io, vars, aph);
            String content = io.readText(oTmpl);
            mail.setContent(content);
            // HTML 模板
            if (cfg.isHtmlTmpl()) {
                mail.setAsHtml(true);
            }
        }

        // 渲染模板变量（标题、正文里的 ${value} 等）
        mail.render(otp.toBean());

        return mail;
    }

    @Override
    protected WnMailPosting getMailPosting() {
        return new WnMailPosting(io, vars);
    }

    @Override
    protected void writeLog(WnOTPBean otp, AjaxReturn result) {
        String way = getConfig().getWriteLog();
        // 目前仅支持 syslog：直接把发送结果写进系统日志
        if ("syslog".equalsIgnoreCase(way)) {
            if (result.isOk()) {
                log.infof("OTP mail sent: %s", otp.toKey());
            } else {
                log.warnf("OTP mail fail: %s -> %s", otp.toKey(), result.getData());
            }
        }
    }

}
