package com.site0.walnut.security.otp.bean;

import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * `~/.otp/{name}.json5` 的配置内容：邮件 OTP 发送器（sender.type=EMAIL）的设置
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnOtpEmailSenderConfig {

    /**
     * 邮件通道：引用 <code>~/.mailx/{channel}.json</code> 里的 smtp 段
     */
    private String channel;

    /**
     * 邮件标题模板，支持 <code>${value}</code> 等变量
     */
    private String subject;

    /**
     * 语言目录：模板文件位于 <code>~/.mailx/i18n/{lang}/</code>
     */
    private String lang;

    /**
     * 模板文件名：相对 <code>~/.mailx/i18n/{lang}/</code> 的文件名
     */
    private String tmpl;

    /**
     * 发送日志的方式：syslog = 直接写系统日志（目前唯一支持的方式）
     */
    private String writeLog;

    public WnOtpEmailSenderConfig() {
        this.channel = "default";
        this.subject = "Walnut Dynamic Password";
        this.lang = "zh-cn";
        this.tmpl = "OTP.html";
        this.writeLog = "syslog";
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean hasSubject() {
        return !Ws.isBlank(subject);
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * @param fallback
     *            配置未指定语言时的回退值（通常取 SMTP 配置的默认语言）
     * @return 模板语言目录名
     */
    public String getLang(String fallback) {
        return Ws.sBlank(lang, fallback);
    }

    public String getTmpl() {
        return tmpl;
    }

    public void setTmpl(String tmpl) {
        this.tmpl = tmpl;
    }

    public boolean hasTmpl() {
        return !Ws.isBlank(tmpl);
    }

    /**
     * @return 模板是否为 HTML（按文件后缀 <code>.html</code> 判断）
     */
    public boolean isHtmlTmpl() {
        return hasTmpl() && tmpl.toLowerCase().endsWith(".html");
    }

    /**
     * @param lang
     *            语言目录名
     * @return 正文模板的完整路径：<code>~/.mailx/i18n/{lang}/{tmpl}</code>
     */
    public String getTmplPath(String lang) {
        if (!hasTmpl()) {
            return null;
        }
        return Wn.appendPath("~/.mailx/i18n", lang, tmpl);
    }

    public String getWriteLog() {
        return writeLog;
    }

    public void setWriteLog(String writeLog) {
        this.writeLog = writeLog;
    }

}
