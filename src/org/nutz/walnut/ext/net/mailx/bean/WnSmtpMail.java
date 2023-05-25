package org.nutz.walnut.ext.net.mailx.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;

/**
 * 封装了一个电子邮件的信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSmtpMail extends WnMail {

    /**
     * 邮件正文路径（比正文更优先）
     */
    String contentPath;

    /**
     * 附件列表，如果不是html邮件，且无附件，则采用简单邮件发送
     */
    List<String> attachments;

    /**
     * 附件列表，如果不是html邮件，且无附件，则采用简单邮件发送
     */
    WnMailSecurity security;

    public WnSmtpMail clone() {
        WnSmtpMail mail = new WnSmtpMail();
        mail.copyFrom(this);
        return mail;
    }

    public void render(NutBean vars) {
        if (this.hasSubject())
            subject = WnTmpl.exec(subject, vars);

        if (this.hasTo())
            to = WnTmpl.exec(to, vars);

        if (this.hasCc())
            cc = WnTmpl.exec(cc, vars);

        if (this.hasBcc())
            bcc = WnTmpl.exec(bcc, vars);

        if (this.hasContent())
            content = WnTmpl.exec(content, vars);

        if (this.hasAttachments()) {
            List<String> ats = new ArrayList<String>(attachments.size());
            for (String at : this.attachments) {
                at = WnTmpl.exec(at, vars);
                ats.add(at);
            }
            this.attachments = ats;
        }

        if (null != security) {
            security.render(vars);
        }
    }

    protected String dumpAttachments() {
        return Ws.join(this.attachments, "; ");
    }

    @Override
    public void copyFrom(WnSmtpMail mail) {
        if (null == mail)
            return;

        super.copyFrom(mail);

        if (mail.hasAttachments()) {
            this.attachments = new ArrayList<>(mail.attachments.size());
            this.attachments.addAll(mail.attachments);
        }
    }

    public boolean hasContentPath() {
        return !Ws.isBlank(contentPath);
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    @Override
    public boolean hasAttachments() {
        return null != attachments && !attachments.isEmpty();
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(String at) {
        if (null == attachments) {
            attachments = new LinkedList<>();
        }
        this.attachments.add(at);
    }

    public void addAttachments(String... aphs) {
        if (null == aphs || aphs.length == 0)
            return;

        if (null == attachments) {
            attachments = new LinkedList<>();
        }
        for (String aph : aphs) {
            attachments.add(aph);
        }
    }

    public boolean hasSecurity() {
        return null != security;
    }

    public WnMailSecurity getSecurity() {
        return security;
    }

    public void setSecurity(WnMailSecurity security) {
        this.security = security;
    }

}
