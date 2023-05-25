package org.nutz.walnut.ext.net.mailx.bean;

import java.util.List;

import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.tmpl.WnTmpl;

public abstract class WnMail {

    /**
     * 邮件标题
     */
    protected String subject;

    /**
     * 收信人
     */
    protected String to;

    /**
     * 抄送
     */
    protected String cc;

    /**
     * 密送
     */
    protected String bcc;

    /**
     * 字符集（默认 UTF-8）
     */
    protected String charset;

    /**
     * 是否为 HTML内容的邮件
     */
    protected boolean asHtml;

    /**
     * 邮件正文
     */
    protected String content;

    public abstract boolean hasAttachments();

    protected String dumpHeaders() {
        return null;
    };

    protected abstract String dumpAttachments();

    public String toString() {
        return this.toString(new NutMap());
    }

    public String toString(NutBean vars) {
        String HR = Ws.repeat('-', 40);
        List<String> ss = Lang.list(String.format("%s Email", this.getType().name()));
        ss.add(HR);
        String hs = this.dumpHeaders();
        if (!Ws.isBlank(hs)) {
            ss.add(hs);
        }
        ss.add(HR);
        if (this.hasSubject()) {
            ss.add("Subject: " + this.getSubject(vars));
        } else {
            ss.add("-No Title-");
        }
        ss.add(HR);
        ss.add("To: " + to);
        if (this.hasCc()) {
            ss.add(HR);
            ss.add("CC: " + cc);
        }
        if (this.hasBcc()) {
            ss.add(HR);
            ss.add("BCC: " + bcc);
        }
        ss.add(HR);
        if (this.hasContent()) {
            ss.add(this.getContent());
        }
        if (this.hasAttachments()) {
            ss.add(HR);
            ss.add("{Att>: " + dumpAttachments());
        }
        ss.add(HR);
        ss.add("~ END ~");

        return Ws.join(ss, "\n");
    }

    public void copyFrom(WnSmtpMail mail) {
        if (null == mail)
            return;

        this.subject = mail.subject;
        this.to = mail.to;
        this.cc = mail.cc;
        this.bcc = mail.bcc;

        if (!Ws.isBlank(mail.charset)) {
            this.charset = mail.charset;
        }

        if (mail.hasContent()) {
            this.asHtml = mail.asHtml;
            this.content = mail.content;
        }

    }

    public WnMailType getType() {
        if (this.hasAttachments()) {
            return asHtml ? WnMailType.MULTIPART_HTML : WnMailType.MULTIPART;
        }
        return asHtml ? WnMailType.HTML : WnMailType.SIMPLE;

    }

    public boolean hasSubject() {
        return !Ws.isBlank(subject);
    }

    public String getSubject(NutBean vars) {
        return WnTmpl.exec(subject, vars);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    private String __append_list(String src, String[] ss2) {
        if (null == ss2) {
            return src;
        }
        String[] ss0 = Ws.splitIgnoreBlank(src, ";");

        if (null == ss0 || ss0.length == 0) {
            return Ws.join(ss2, "; ");
        }

        if (null == ss2 || ss2.length == 0) {
            return src;
        }

        int N0 = ss0.length;
        int N2 = ss2.length;
        String[] ss = new String[N0 + N2];
        System.arraycopy(ss0, 0, ss, 0, N0);
        System.arraycopy(ss2, 0, ss, N0, N2);
        return Ws.join(ss, "; ");
    }

    public boolean hasTo() {
        return !Ws.isBlank(to);
    }

    public void addTo(String... accounts) {
        this.to = this.__append_list(this.to, accounts);
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean hasCc() {
        return !Ws.isBlank(cc);
    }

    public void addCc(String... accounts) {
        this.cc = this.__append_list(this.cc, accounts);
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public boolean hasBcc() {
        return !Ws.isBlank(bcc);
    }

    public void addBcc(String... accounts) {
        this.bcc = this.__append_list(this.bcc, accounts);
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public boolean hasCharset() {
        return !Ws.isBlank(charset);
    }

    public String getCharset() {
        return Ws.sBlank(charset, Encoding.UTF8);
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean hasContent() {
        return !Ws.isBlank(content);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAsHtml() {
        return asHtml;
    }

    public void setAsHtml(boolean asHtml) {
        this.asHtml = asHtml;
    }

}
