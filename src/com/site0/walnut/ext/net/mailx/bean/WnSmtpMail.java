package com.site0.walnut.ext.net.mailx.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmpl;
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
    List<String> attachmentPaths;

    /**
     * 固定附件列表
     */
    List<WnMailAttachment> attachments;

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

        if (null != attachmentPaths && attachmentPaths.size() > 0) {
            List<String> ats = new ArrayList<String>(attachmentPaths.size());
            for (String at : this.attachmentPaths) {
                at = WnTmpl.exec(at, vars);
                ats.add(at);
            }
            this.attachmentPaths = ats;
        }

        if (null != security) {
            security.render(vars);
        }
    }

    protected String dumpAttachments() {
        return Ws.join(this.attachmentPaths, "; ");
    }

    @Override
    public void copyFrom(WnSmtpMail mail) {
        if (null == mail)
            return;

        super.copyFrom(mail);

        if (null != mail.attachmentPaths && mail.attachmentPaths.size() > 0) {
            this.attachmentPaths = new ArrayList<>(mail.attachmentPaths.size());
            this.attachmentPaths.addAll(mail.attachmentPaths);
        }

        if (null != mail.attachments && mail.attachments.size() > 0) {
            this.attachments = new ArrayList<>(mail.attachments.size());
            for (WnMailAttachment at : mail.attachments) {
                this.attachments.add(at.clone());
            }
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
        if (null != attachmentPaths && attachmentPaths.size() > 0) {
            return true;
        }
        if (null != attachments && attachments.size() > 0) {
            return true;
        }
        return false;
    }

    public List<String> getAttachmentPaths() {
        return attachmentPaths;
    }

    public void setAttachmentPaths(List<String> attachments) {
        this.attachmentPaths = attachments;
    }

    public void addAttachmentPath(String at) {
        if (null == attachmentPaths) {
            attachmentPaths = new LinkedList<>();
        }
        this.attachmentPaths.add(at);
    }

    public void addAttachmentPaths(String... aphs) {
        if (null == aphs || aphs.length == 0)
            return;

        if (null == attachmentPaths) {
            attachmentPaths = new LinkedList<>();
        }
        for (String aph : aphs) {
            attachmentPaths.add(aph);
        }
    }

    public List<WnMailAttachment> getAttachment() {
        return attachments;
    }

    public void setAttachment(List<WnMailAttachment> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(WnMailAttachment at) {
        if (null == attachments) {
            attachments = new LinkedList<>();
        }
        this.attachments.add(at);
    }

    public void addAttachment(WnMailAttachment... ats) {
        if (null == ats || ats.length == 0)
            return;

        if (null == attachments) {
            attachments = new LinkedList<>();
        }
        for (WnMailAttachment at : ats) {
            attachments.add(at);
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
