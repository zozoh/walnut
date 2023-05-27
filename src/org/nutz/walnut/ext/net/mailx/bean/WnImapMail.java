package org.nutz.walnut.ext.net.mailx.bean;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Ws;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;

public class WnImapMail extends WnMail {

    private String contentType;

    /**
     * 从 contentType 里获取的属性，譬如 charset 或者 name 或者 boundary
     */
    private NutMap attrs;

    private NutMap headers;

    private String from;

    private List<WnMailPart> bodyParts;

    public WnImapMail() {
        attrs = new NutMap();
        headers = new NutMap();
    }

    public WnImapMail(Message msg) {
        this();
        this.fromMessage(msg);
    }

    public void fromMessage(Message msg) {
        if (null == msg)
            return;

        try {
            // 读取头
            Enumeration<Header> hs = msg.getAllHeaders();
            while (hs.hasMoreElements()) {
                Header h = hs.nextElement();
                headers.addv(h.getName(), h.getValue());
            }

            // Get from
            this.from = getAddressAsStr(msg.getFrom());

            // 设置其他信息
            this.subject = msg.getSubject();
            this.to = getAddressAsStr(msg.getRecipients(RecipientType.TO));
            this.cc = getAddressAsStr(msg.getRecipients(RecipientType.CC));
            this.bcc = getAddressAsStr(msg.getRecipients(RecipientType.BCC));

            this.charset = "UTF-8";
            this.setContentType(msg.getContentType());

            // if (mail.hasContent()) {
            // this.asHtml = msg.i
            // this.content = mail.content;
            // }
            // 读取邮件正文
            Object body = msg.getContent();
            if (body instanceof MimeMultipart) {
                MimeMultipart mparts = (MimeMultipart) body;
                int n = mparts.getCount();
                this.bodyParts = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    /*
                     * text/plain; charset=gb18030 text/html; charset=gb18030
                     * multipart/alternative; application/octet-stream;
                     * charset=gb18030;
                     */
                    BodyPart part = mparts.getBodyPart(i);
                    WnMailPart mp = new WnMailPart(part);
                    this.bodyParts.add(mp);

                }
            }
            // 默认就傻傻的直接字符串了
            else {
                this.content = body.toString();
            }

            // 读取邮件附件
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

    protected String getAddressAsStr(Address[] addresses) {
        // 防守一把
        if (null == addresses) {
            return null;
        }
        List<String> list = new ArrayList<>(addresses.length);
        for (Address adr : addresses) {
            InternetAddress iadr = (InternetAddress) adr;
            String person = iadr.getPersonal();
            String address = iadr.getAddress();
            if (Ws.isBlank(person)) {
                list.add(address);
            } else {
                list.add(String.format("<%s=%s>", person, address));
            }
        }
        return Ws.join(list, ",");
    }

    public String toString() {
        return this.dumpString(true);
    }

    public String toString(NutBean vars) {
        return this.dumpString(true);
    }

    private String dumpAttrs() {
        StringBuilder sb = new StringBuilder();
        WnMailPart.joinHeaders(sb, this.attrs, "");
        return sb.toString();
    }

    private String dumpHeaders() {
        StringBuilder sb = new StringBuilder();
        WnMailPart.joinHeaders(sb, this.headers, "");
        return sb.toString();
    }

    public String dumpString(boolean showHeader) {
        String HR = Ws.repeat('-', 40);
        String HR2 = Ws.repeat('.', 40);
        List<String> ss = Lang.list(String.format("%s Email", this.getType().name()));
        ss.add(HR);
        ss.add("Content-Type: " + contentType);
        // 显示头
        if (showHeader) {
            String as = this.dumpAttrs();
            ss.add(HR);
            ss.add("[#MSG ATTRS]");
            ss.add(as);
            String hs = this.dumpHeaders();
            ss.add(HR);
            ss.add("[#MSG HEAD]");
            ss.add(hs);
        }

        // 标题
        if (this.hasSubject()) {
            ss.add("Subject: " + this.getSubject(new NutMap()));
        } else {
            ss.add("-No Title-");
        }
        ss.add(HR);
        ss.add("From: " + from);
        ss.add("To  : " + to);
        if (this.hasCc()) {
            ss.add("CC: " + cc);
        }
        if (this.hasBcc()) {
            ss.add("BCC: " + bcc);
        }

        // 正文及附件
        ss.add(HR);
        if (this.hasBody()) {
            int n = this.bodyParts.size();
            for (int i = 0; i < n; i++) {
                WnMailPart part = this.bodyParts.get(i);
                ss.add(HR2);
                ss.add(String.format("# %d/%d) BODY PART", i + 1, n));
                ss.add(HR2);
                ss.add(part.toString());
            }
        } else {
            ss.add("<~No Body~>");
        }

        ss.add("~ END ~");

        return Ws.join(ss, "\n");
    }

    public Charset getMessageCharset() {
        String cs = this.attrs.getString("charset", this.charset);
        return Charset.forName(cs);
    }

    public boolean hasBody() {
        return null != this.bodyParts && this.bodyParts.size() > 0;
    }

    @Override
    public boolean hasAttachments() {
        return false;
    }

    @Override
    protected String dumpAttachments() {
        return null;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = Ws.evalContentType(contentType, this.attrs);
    }

}
