package org.nutz.walnut.ext.sendmail.bean;

import java.util.LinkedList;
import java.util.List;

import javax.activation.DataSource;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.nutz.lang.Encoding;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

/**
 * 封装了一个电子邮件的信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnMail {

    /**
     * 邮件标题
     */
    private String subject;

    /**
     * 接收者
     */
    private List<WnMailReceiver> receivers;

    /**
     * 抄送
     */
    private List<WnMailReceiver> carbonCopies;

    /**
     * 密送
     */
    private List<WnMailReceiver> blindCarbonCopies;

    /**
     * 字符集（默认 UTF-8）
     */
    private String charset;

    /**
     * 是否为 HTML内容的邮件
     */
    private boolean asHtml;

    /**
     * 对于 HTML 邮件，里面链接和资源的 baseUrl
     */
    private String baseUrl;

    /**
     * 邮件正文
     */
    private String content;

    /**
     * 邮件正文模板，优先级高于邮件正文
     */
    private String templateName;

    /**
     * 附件列表，如果不是html邮件，且无附件，则采用简单邮件发送
     */
    private List<WnObj> attachments;

    /**
     * 邮件语言（当 templateName模式时有效） 如果为空，则看全局配置的默认语言
     */
    private String lang;

    public boolean hasSubject() {
        return !Strings.isBlank(subject);
    }

    public String getSubject(NutBean vars) {
        return Tmpl.exec(subject, vars);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean hasReceivers() {
        return null != receivers && receivers.size() > 0;
    }

    public List<WnMailReceiver> getReceivers() {
        return receivers;
    }

    public void addReceivers(String... res) {
        if (null == this.receivers) {
            this.receivers = new LinkedList<>();
        }
        for (String re : res) {
            WnMailReceiver r = new WnMailReceiver(re);
            this.receivers.add(r);
        }
    }

    public void addReceivers(WnMailReceiver... res) {
        if (null == this.receivers) {
            this.receivers = new LinkedList<>();
        }
        for (WnMailReceiver r : res) {
            this.receivers.add(r);
        }
    }

    public void setReceivers(List<WnMailReceiver> receivers) {
        this.receivers = receivers;
    }

    public boolean hasCC() {
        return null != carbonCopies && carbonCopies.size() > 0;
    }

    public void addCC(String... res) {
        if (null == this.carbonCopies) {
            this.carbonCopies = new LinkedList<>();
        }
        for (String re : res) {
            WnMailReceiver r = new WnMailReceiver(re);
            this.carbonCopies.add(r);
        }
    }

    public void addCC(WnMailReceiver... res) {
        if (null == this.carbonCopies) {
            this.carbonCopies = new LinkedList<>();
        }
        for (WnMailReceiver r : res) {
            this.carbonCopies.add(r);
        }
    }

    public List<WnMailReceiver> getCarbonCopies() {
        return carbonCopies;
    }

    public void setCarbonCopies(List<WnMailReceiver> carbonCopies) {
        this.carbonCopies = carbonCopies;
    }

    public List<WnMailReceiver> getBlindCarbonCopies() {
        return blindCarbonCopies;
    }

    public boolean hasBCC() {
        return null != blindCarbonCopies && blindCarbonCopies.size() > 0;
    }

    public void addBCC(String... res) {
        if (null == this.blindCarbonCopies) {
            this.blindCarbonCopies = new LinkedList<>();
        }
        for (String re : res) {
            WnMailReceiver r = new WnMailReceiver(re);
            this.blindCarbonCopies.add(r);
        }
    }

    public void addBCC(WnMailReceiver... res) {
        if (null == this.blindCarbonCopies) {
            this.blindCarbonCopies = new LinkedList<>();
        }
        for (WnMailReceiver r : res) {
            this.blindCarbonCopies.add(r);
        }
    }

    public void setBlindCarbonCopies(List<WnMailReceiver> blindCarbonCopies) {
        this.blindCarbonCopies = blindCarbonCopies;
    }

    public String getCharset() {
        return Strings.sBlank(charset, Encoding.UTF8);
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean hasBaseUrl() {
        return !Strings.isBlank(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean hasContent() {
        return !Strings.isBlank(content);
    }

    public String getContent(NutBean vars) {
        return Tmpl.exec(content, vars);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean hasTemplateName() {
        return !Strings.isBlank(templateName);
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean hasAttachments() {
        return null != attachments && attachments.size() > 0;
    }

    public List<WnObj> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<WnObj> attachments) {
        this.attachments = attachments;
    }

    public void addAttachments(WnObj... objs) {
        if (null == objs || objs.length == 0)
            return;

        if (null == attachments) {
            attachments = new LinkedList<>();
        }
        for (WnObj o : objs) {
            attachments.add(o);
        }
    }

    public void appendAttachmentToMail(MultiPartEmail mail, WnIo io) throws EmailException {
        if (!this.hasAttachments()) {
            return;
        }
        for (WnObj o : this.attachments) {
            String name = o.getOr("title|nm", "un-title").toString();
            String type = o.type();
            if (!Strings.isBlank(type) && !name.endsWith("." + type)) {
                name += "." + type;
            }
            String desc = (String) o.getOr("brief");
            DataSource ds = new WnObjMailDataSource(io, o);
            mail.attach(ds, name, desc);
        }
    }

    public boolean isAsHtml() {
        return asHtml;
    }

    public void setAsHtml(boolean asHtml) {
        this.asHtml = asHtml;
    }

    public String getLang(String dft) {
        return Strings.sBlank(lang, dft);
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
