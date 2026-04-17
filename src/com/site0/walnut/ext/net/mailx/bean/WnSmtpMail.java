package com.site0.walnut.ext.net.mailx.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmplX;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

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

    public NutBean toInfo() {
        NutMap info = new NutMap();
        if (this.hasSubject())
            info.put("subject", subject);

        if (this.hasTo())
            info.put("to", to);

        if (this.hasCc())
            info.put("cc", cc);

        if (this.hasBcc())
            info.put("bcc", bcc);

        if (this.hasContent()) {
            info.put("content", "<len=" + content.length() + ">");
            info.put("asHtml", this.isAsHtml());
        }

        if (this.hasAttachments()) {
            List<String> atPaths = getAttachmentPaths();

            // 根据路径读取
            if (null != atPaths && atPaths.size() > 0) {
                info.put("attachmentPaths", atPaths);
            }

            // 设置的固定
            List<WnMailAttachment> ats = getAttachment();
            if (null != ats) {
                for (WnMailAttachment at : ats) {
                    String name = at.getName();
                    String mime = at.getMime();
                    info.addv2("attachments",
                               Wlang.map("name", name).setv("mime", mime));
                }
            }
        }

        return info;
    }

    public WnSmtpMail clone() {
        WnSmtpMail mail = new WnSmtpMail();
        mail.copyFrom(this);
        return mail;
    }

    public void render(NutBean vars) {
        if (this.hasSubject())
            subject = WnTmplX.exec(subject, vars);

        if (this.hasTo())
            to = WnTmplX.exec(to, vars);

        if (this.hasCc())
            cc = WnTmplX.exec(cc, vars);

        if (this.hasBcc())
            bcc = WnTmplX.exec(bcc, vars);

        if (this.hasContent())
            content = WnTmplX.exec(content, vars);

        if (null != attachmentPaths && attachmentPaths.size() > 0) {
            List<String> ats = new ArrayList<String>(attachmentPaths.size());
            for (String at : this.attachmentPaths) {
                at = WnTmplX.exec(at, vars);
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
