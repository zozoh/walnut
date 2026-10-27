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
import com.site0.walnut.security.otp.bean.WnOtpEmailSenderConfig;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * 系统邮件 OTP 发送器：给定邮件发送配置（通常由 {@code WnOTPService} 从
 * <code>~/.otp/{name}.json5</code> 的 sender.config 解析而来），
 * SMTP 通道取自 <code>~/.mailx/{channel}.json</code>
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSysEmailOTPSender extends WnEmailOTPSender {

    private static final Log log = Wlog.getAUTH();

    private WnIo io;

    private NutBean vars;

    private WnOtpEmailSenderConfig config;

    // 懒加载
    private MailxSmtpConfig smtp;

    public WnSysEmailOTPSender(WnSystem sys, WnOtpEmailSenderConfig config) {
        this(sys.io, sys.session.getEnv(), config);
    }

    public WnSysEmailOTPSender(WnIo io, NutBean sessionVars, WnOtpEmailSenderConfig config) {
        this.io = io;
        this.vars = sessionVars;
        if (null == config) {
            throw Er.create("e.otp.email.WithoutConfig");
        }
        this.config = config;
    }

    @Override
    protected MailxSmtpConfig getSmtpConfig() {
        if (null == smtp) {
            String channel = config.getChannel();
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
        // 邮件对象：收件人就是正在验证的账号
        WnSmtpMail mail = new WnSmtpMail();
        mail.addTo(otp.getAccount());
        mail.setSubject(config.getSubject());

        // 正文模板：~/.mailx/i18n/{lang}/{tmpl}
        if (config.hasTmpl()) {
            // 语言：OTP 配置 → SMTP 配置 → en-us
            String lang = config.getLang(Ws.sBlank(getSmtpConfig().getLang(), "en-us"));
            String aph = config.getTmplPath(lang);
            WnObj oTmpl = Wn.checkObj(io, vars, aph);
            String content = io.readText(oTmpl);
            mail.setContent(content);
            // HTML 模板
            if (config.isHtmlTmpl()) {
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
        String way = config.getWriteLog();
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
