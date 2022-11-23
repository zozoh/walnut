package org.nutz.walnut.ext.net.sendmail.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.mail.resolver.DataSourceUrlResolver;
import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.sendmail.api.WnMailApi;
import org.nutz.walnut.ext.net.sendmail.bean.WnMail;
import org.nutz.walnut.ext.net.sendmail.bean.WnMailConfig;
import org.nutz.walnut.ext.net.sendmail.bean.WnMailReceiver;
import org.nutz.walnut.util.Wn;

public class WnMailService implements WnMailApi {

    private static final Log log = Wlog.getEXT();

    private WnMailConfig config;

    private WnIo io;

    private WnObj oHome;

    private WnObj oI18n;

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

    private WnObj loadTemplateObj(String lang, String tmplName) {
        if (null == this.oI18n || !this.oI18n.isDIR()) {
            return null;
        }
        if (Strings.isBlank(lang) || Strings.isBlank(tmplName)) {
            return null;
        }

        return io.fetch(oI18n, Wn.appendPath(lang, tmplName));
    }

    @Override
    public void smtp(WnMail mail, NutBean vars) throws EmailException {
        // 收件人
        if (!mail.hasMailTo()) {
            throw new EmailException("No receivers");
        }

        // 预先处理邮件模板：（会比正文更优先）
        if (mail.hasTemplateName()) {
            String lang = mail.getLang(config.getLang());
            String tmplName = mail.getTemplateName();
            WnObj oTmpl = this.loadTemplateObj(lang, tmplName);
            if (null != oTmpl) {
                String text = io.readText(oTmpl);
                if (!mail.hasSubject()) {
                    String sub = oTmpl.getString("subject");
                    String subject = Tmpl.exec(sub, vars);
                    mail.setSubject(subject);
                }
                if (null != text) {
                    mail.setContent(text);
                }
                if (oTmpl.isMime("text/html")
                    || oTmpl.isType("html")
                    || oTmpl.getBoolean("asHtml")) {
                    mail.setAsHtml(true);
                }
                if (!mail.hasCharset() && oTmpl.has("charset")) {
                    mail.setCharset(oTmpl.getString("charset"));
                }
            }
        }

        // 搞一个邮件对象
        Email mo = this.createEmailObj(mail);

        // HTML 邮件
        if (mo instanceof ImageHtmlEmail) {
            ImageHtmlEmail ihe = (ImageHtmlEmail) mo;
            // 设置
            if (mail.hasBaseUrl()) {
                try {
                    URL url = new URL(mail.getBaseUrl());
                    ihe.setDataSourceResolver(new DataSourceUrlResolver(url));
                }
                catch (MalformedURLException e) {
                    throw Er.create("e.mail.base_url.invalid", mail.getBaseUrl());
                }
            }
            // 木有，也要硬设一个，否则它会 NPE
            else {
                ihe.setDataSourceResolver(new DataSourceUrlResolver(null));
            }
        }

        // 准备发送资料
        String account = config.getAccount();
        String passwd = config.getPassword();
        mo.setHostName(config.getSmtpHost());
        mo.setAuthentication(account, passwd);
        mo.setFrom(account, config.getAlias());
        mo.setCharset(mail.getCharset());
        mo.setSSLOnConnect(config.isSsl());
        // SSL
        if (config.isSsl()) {
            mo.setSslSmtpPort(config.getSmtpPort() + "");
            mo.setSSLOnConnect(true);
        }
        // 非 SSL
        else {
            mo.setSmtpPort(config.getSmtpPort());
            mo.setSSLOnConnect(false);
        }

        // 邮件标题
        if (mail.hasSubject()) {
            String subject = mail.getSubject(vars);
            mo.setSubject(subject);
        }

        // 收件人
        for (WnMailReceiver r : mail.getMailTo()) {
            mo.addTo(r.getAccount(), r.getName());
        }

        // 抄送
        if (mail.hasMailCc()) {
            for (WnMailReceiver r : mail.getMailCc()) {
                mo.addCc(r.getAccount(), r.getName());
            }
        }

        // 密送
        if (mail.hasMailBcc()) {
            for (WnMailReceiver r : mail.getMailBcc()) {
                mo.addBcc(r.getAccount(), r.getName());
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
        } else {
            if (mo instanceof HtmlEmail) {
                ((HtmlEmail) mo).setHtmlMsg("");
            } else {
                mo.setMsg("");
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
