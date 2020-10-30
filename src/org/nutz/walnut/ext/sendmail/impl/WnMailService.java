package org.nutz.walnut.ext.sendmail.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sendmail.api.WnMailApi;
import org.nutz.walnut.ext.sendmail.bean.WnMail;
import org.nutz.walnut.ext.sendmail.bean.WnMailConfig;
import org.nutz.walnut.ext.sendmail.bean.WnMailReceiver;
import org.nutz.walnut.util.Wn;

public class WnMailService implements WnMailApi {

    private static final Log log = Logs.get();

    private WnMailConfig config;

    private WnIo io;

    private WnObj oHome;

    private WnObj oI18n;

    private Map<String, String> cacheContent;

    private Map<String, String> cacheSubject;

    /**
     * @param io
     *            IO 接口
     * @param oHome
     *            邮件的 HOME 对象，其内有 "_default.json" 和 "i18n" 目录
     * @param configName
     *            配置名称，默认为 "_default"
     */
    public WnMailService(WnIo io, WnObj oHome, String configName) {
        this.io = io;
        this.oHome = oHome;
        this.oI18n = io.fetch(oHome, "i18n");
        this.config = this.loadConfig(configName);
        this.cacheContent = new HashMap<>();
        this.cacheSubject = new HashMap<>();
    }

    private WnMailConfig loadConfig(String configName) {
        if (Strings.isBlank(configName)) {
            configName = "_default";
        }
        WnObj oConf = io.check(oHome, configName + ".json");
        return io.readJson(oConf, WnMailConfig.class);
    }

    private Email createEmailObj(WnMail mail) {
        // HTML
        if (mail.isAsHtml()) {
            return new ImageHtmlEmail();
        }
        // 带附件
        if (mail.hasAttachments()) {
            return new MultiPartEmail();
        }

        // 默认为简单邮件
        return new SimpleEmail();
    }

    private String loadTemplateSubject(String lang, String tmplName) {
        if (null == this.oI18n || !this.oI18n.isDIR()) {
            return null;
        }
        if (Strings.isBlank(lang) || Strings.isBlank(tmplName)) {
            return null;
        }
        String key = lang + "_" + tmplName;

        // 缓存加速一把
        if (cacheContent.containsKey(key)) {
            return cacheSubject.get(key);
        }

        WnObj o = io.fetch(oI18n, Wn.appendPath(lang, tmplName));
        if (null == o) {
            return null;
        }
        String subject = o.getString("subject", null);
        cacheSubject.put(key, subject);
        String text = io.readText(o);
        cacheContent.put(key, text);
        return subject;
    }

    private String loadTemplateContent(String lang, String tmplName) {
        if (null == this.oI18n || !this.oI18n.isDIR()) {
            return null;
        }
        if (Strings.isBlank(lang) || Strings.isBlank(tmplName)) {
            return null;
        }
        String key = lang + "_" + tmplName;

        // 缓存加速一把
        if (cacheContent.containsKey(key)) {
            return cacheContent.get(key);
        }

        WnObj o = io.fetch(oI18n, Wn.appendPath(lang, tmplName));
        if (null == o) {
            return null;
        }
        String subject = o.getString("subject", null);
        cacheSubject.put(key, subject);
        String text = io.readText(o);
        cacheContent.put(key, text);
        return text;
    }

    @Override
    public void smtp(WnMail mail, NutBean vars) throws EmailException {
        // 收件人
        if (!mail.hasReceivers()) {
            throw new EmailException("No receivers");
        }

        // 搞一个邮件对象
        Email mo = this.createEmailObj(mail);

        // 准备发送资料
        mo.setHostName(config.getSmtpHost());
        mo.setSmtpPort(config.getSmtpPort());
        mo.setAuthentication(config.getAccount(), config.getPassword());
        mo.setFrom(config.getAccount(), config.getAlias());
        mo.setCharset(mail.getCharset());

        // 邮件标题
        if (mail.hasSubject()) {
            String subject = mail.getSubject(vars);
            mo.setSubject(subject);
        }

        // 收件人
        for (WnMailReceiver r : mail.getReceivers()) {
            mo.addTo(r.getAccount(), r.getName());
        }

        // 抄送
        if (mail.hasCC()) {
            for (WnMailReceiver r : mail.getCarbonCopies()) {
                mo.addCc(r.getAccount(), r.getName());
            }
        }

        // 密送
        if (mail.hasBCC()) {
            for (WnMailReceiver r : mail.getBlindCarbonCopies()) {
                mo.addBcc(r.getAccount(), r.getName());
            }
        }

        // 处理邮件正文: 模板
        if (mail.hasTemplateName()) {
            String lang = mail.getLang(config.getLang());
            String tmplName = mail.getTemplateName();
            String text = this.loadTemplateContent(lang, tmplName);
            if (!mail.hasSubject()) {
                String sub = this.loadTemplateSubject(lang, tmplName);
                String subject = Tmpl.exec(sub, vars);
                mo.setSubject(subject);
            }
            if (null != text) {
                mail.setContent(text);
            }
        }

        // 邮件正文
        if (mail.hasContent()) {
            String content = mail.getContent(vars);
            if (mo instanceof HtmlEmail) {
                ((HtmlEmail) mo).setHtmlMsg(content);
            } else {
                mo.setMsg(content);
            }
        }

        // 附件
        if (mo instanceof MultiPartEmail) {
            mail.appendAttachmentToMail((MultiPartEmail) mo, io);
        }

        // 嗯，发送吧
        mo.buildMimeMessage();
        String re = mo.sendMimeMessage();

        if (log.isDebugEnabled()) {
            log.debugf("Mail sent: %s", re);
        }
    }

}
