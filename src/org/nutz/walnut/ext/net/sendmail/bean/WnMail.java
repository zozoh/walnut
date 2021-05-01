package org.nutz.walnut.ext.net.sendmail.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.activation.DataSource;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
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
    private List<WnMailReceiver> mailTo;

    /**
     * 抄送
     */
    private List<WnMailReceiver> mailCc;

    /**
     * 密送
     */
    private List<WnMailReceiver> mailBcc;

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

    public WnMail clone() {
        WnMail mail = new WnMail();
        mail.copyFrom(this);
        return mail;
    }

    public String toString() {
        return this.toString(new NutMap());
    }

    public String toString(NutBean vars) {
        String HR = Strings.dup('-', 40);
        List<String> ss = Lang.list(String.format("%s Email", this.getType().name()));
        ss.add(HR);
        if (this.hasSubject()) {
            ss.add("Subject: " + this.getSubject(vars));
        } else {
            ss.add("-No Title-");
        }
        ss.add(HR);
        ss.add("To: " + __str_list(this.mailTo));
        if (this.hasMailCc()) {
            ss.add(HR);
            ss.add("CC: " + __str_list(this.mailCc));
        }
        if (this.hasMailBcc()) {
            ss.add(HR);
            ss.add("BCC: " + __str_list(this.mailBcc));
        }
        ss.add(HR);
        if (this.hasTemplateName()) {
            ss.add("TEMPLATE<" + this.templateName + ">\n");
        }
        if (this.hasContent()) {
            ss.add(this.getContent(vars));
        }
        if (this.hasAttachments()) {
            ss.add(HR);
            ss.add("BCC: " + __str_list(this.attachments));
        }
        ss.add(HR);
        ss.add("~ END ~");

        return Strings.join("\n", ss);
    }

    private String __str_list(List<?> list) {
        if (null != list && !list.isEmpty()) {
            List<String> ss = new ArrayList<>(list.size());
            for (Object li : list) {
                ss.add(li.toString());
            }
            return Strings.join("; ", ss);
        }
        return "";
    }

    public void copyFrom(WnMail mail) {
        if (null == mail)
            return;

        if (mail.hasSubject())
            this.subject = mail.subject;

        if (mail.hasMailTo()) {
            this.mailTo = __copy_list(mail.mailTo);
        }

        if (mail.hasMailCc()) {
            this.mailCc = __copy_list(mail.mailCc);
        }

        if (mail.hasMailBcc()) {
            this.mailBcc = __copy_list(mail.mailBcc);
        }

        if (!Strings.isBlank(mail.charset)) {
            this.charset = mail.charset;
        }

        if (!Strings.isBlank(mail.baseUrl)) {
            this.baseUrl = mail.baseUrl;
        }

        if (mail.hasContent()) {
            this.asHtml = mail.asHtml;
            this.content = mail.content;
        }

        if (mail.hasTemplateName()) {
            this.asHtml = mail.asHtml;
            this.templateName = mail.templateName;
        }

        if (mail.hasAttachments()) {
            this.attachments = new LinkedList<>();
            for (WnObj o : mail.attachments) {
                this.attachments.add(o.clone());
            }
        }
    }

    private List<WnMailReceiver> __copy_list(List<WnMailReceiver> src) {
        if (null == src)
            return src;
        List<WnMailReceiver> list = new LinkedList<>();
        if (!src.isEmpty())
            for (WnMailReceiver r : src) {
                list.add(r.clone());
            }
        return list;
    }

    public WnMailType getType() {
        if (this.asHtml)
            return WnMailType.HTML;

        if (this.hasAttachments())
            return WnMailType.MULTIPART;

        return WnMailType.SIMPLE;
    }

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

    public boolean hasMailTo() {
        return null != mailTo && mailTo.size() > 0;
    }

    public List<WnMailReceiver> getMailTo() {
        return mailTo;
    }

    public void addMailTo(String... res) {
        if (null == this.mailTo) {
            this.mailTo = new LinkedList<>();
        }
        for (String re : res) {
            WnMailReceiver r = new WnMailReceiver(re);
            this.mailTo.add(r);
        }
    }

    public void addMailToR(WnMailReceiver... res) {
        if (null == this.mailTo) {
            this.mailTo = new LinkedList<>();
        }
        for (WnMailReceiver r : res) {
            this.mailTo.add(r);
        }
    }

    public void setMailTo(List<WnMailReceiver> receivers) {
        this.mailTo = receivers;
    }

    public boolean hasMailCc() {
        return null != mailCc && mailCc.size() > 0;
    }

    public void addMailCc(String... res) {
        if (null == this.mailCc) {
            this.mailCc = new LinkedList<>();
        }
        for (String re : res) {
            WnMailReceiver r = new WnMailReceiver(re);
            this.mailCc.add(r);
        }
    }

    public void addMailCcR(WnMailReceiver... res) {
        if (null == this.mailCc) {
            this.mailCc = new LinkedList<>();
        }
        for (WnMailReceiver r : res) {
            this.mailCc.add(r);
        }
    }

    public List<WnMailReceiver> getMailCc() {
        return mailCc;
    }

    public void setMailCc(List<WnMailReceiver> carbonCopies) {
        this.mailCc = carbonCopies;
    }

    public List<WnMailReceiver> getMailBcc() {
        return mailBcc;
    }

    public boolean hasMailBcc() {
        return null != mailBcc && mailBcc.size() > 0;
    }

    public void addMailBcc(String... res) {
        if (null == this.mailBcc) {
            this.mailBcc = new LinkedList<>();
        }
        for (String re : res) {
            WnMailReceiver r = new WnMailReceiver(re);
            this.mailBcc.add(r);
        }
    }

    public void addMailBccR(WnMailReceiver... res) {
        if (null == this.mailBcc) {
            this.mailBcc = new LinkedList<>();
        }
        for (WnMailReceiver r : res) {
            this.mailBcc.add(r);
        }
    }

    public void setMailBcc(List<WnMailReceiver> blindCarbonCopies) {
        this.mailBcc = blindCarbonCopies;
    }

    public boolean hasCharset() {
        return Strings.isBlank(charset);
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
            String desc = (String) o.getString("brief");
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
