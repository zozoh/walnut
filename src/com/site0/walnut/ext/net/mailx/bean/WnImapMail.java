package com.site0.walnut.ext.net.mailx.bean;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.net.mailx.util.Mailx;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

import com.sun.mail.imap.IMAPMessage;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;

public class WnImapMail extends WnMail {

    private int number;

    private String messageId;

    private long receiveAt;

    private String contentType;

    /**
     * 从 contentType 里获取的属性，譬如 charset 或者 name 或者 boundary
     */
    private NutMap attrs;

    private NutMap headers;

    private String sender;

    protected List<WnMailPart> bodyParts;

    public WnImapMail() {
        attrs = new NutMap();
        headers = new NutMap();
    }

    public WnImapMail(Message msg) {
        this();
        this.fromMessage(msg, null);
    }

    public WnImapMail(Message msg, String asContent) {
        this();
        this.fromMessage(msg, asContent);
    }

    public List<WnMailPart> findContentParts(String asContent) {
        if (null == bodyParts) {
            return new LinkedList<>();
        }

        int n = Math.max(bodyParts.size(), 5);
        List<WnMailPart> list = new ArrayList<>(n);
        for (WnMailPart part : bodyParts) {
            part.joinContentPart(list, asContent);
        }
        return list;
    }

    public List<WnMailPart> findAttachmentParts(String asContent) {
        if (null == bodyParts) {
            return new LinkedList<>();
        }

        int n = Math.max(bodyParts.size(), 5);
        List<WnMailPart> list = new ArrayList<>(n);
        for (WnMailPart part : bodyParts) {
            part.joinAttachmentPart(list, asContent);
        }
        return list;
    }

    public void fromMessage(Message msg, String asContent) {
        if (null == msg)
            return;
        // 读取消息信息
        try {
            IMAPMessage imsg = (IMAPMessage) msg;
            this.receiveAt = msg.getReceivedDate().getTime();
            this.messageId = imsg.getMessageID();
            this.number = msg.getMessageNumber();
        }
        catch (MessagingException e) {
            throw Er.wrap(e);
        }
        // 读取头
        this.loadHeadInfo(msg);

        // 读取邮件正文
        this.loadBody(msg, asContent);
    }

    protected void loadBody(Message msg, String asContent) {
        try {
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
                    WnMailPart mp = new WnMailPart(part, asContent);

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

    protected void loadHeadInfo(Message msg) {
        try {
            Enumeration<Header> hs = msg.getAllHeaders();
            while (hs.hasMoreElements()) {
                Header h = hs.nextElement();
                headers.addv(h.getName(), h.getValue());
            }

            // Get from
            this.sender = getAddressAsStr(msg.getFrom());

            // 设置其他信息
            this.subject = msg.getSubject();
            this.to = getAddressAsStr(msg.getRecipients(RecipientType.TO));
            this.cc = getAddressAsStr(msg.getRecipients(RecipientType.CC));
            this.bcc = getAddressAsStr(msg.getRecipients(RecipientType.BCC));

            this.charset = "UTF-8";
            this.setContentType(msg.getContentType());
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

    public NutMap toMeta(boolean includeHeader) {
        NutMap meta = new NutMap();
        // 基本信息
        meta.put("nm", this.messageId);
        meta.put("title", this.getSubject());
        meta.put("sort", this.number);
        meta.put("msg_receive_at", this.receiveAt);
        meta.put("msg_receive_ats", this.getReceiveAtDateStr());
        meta.put("msg_sender", this.getSender());
        meta.put("msg_to", this.getTo());
        meta.put("msg_cc", this.getCc());
        meta.put("msg_bcc", this.getBcc());
        meta.put("msg_content_type", this.getContentType());
        meta.put("msg_charset", this.getMessageCharset().toString());

        // 消息属性
        if (null != this.attrs) {
            meta.put("msg_attrs", this.attrs);
        }

        // 其他更多消息头
        if (null != this.headers && includeHeader) {
            meta.put("msg_head", this.headers);
        }

        return meta;
    }

    public String toString() {
        return this.dumpString(true);
    }

    public String toString(NutBean vars) {
        return this.dumpString(true);
    }

    public String toBrief() {
        return String.format("[%s]%s : %s : %s",
                             this.number,
                             this.messageId,
                             this.getSender(),
                             this.getSubject());
    }

    private String dumpAttrs() {
        StringBuilder sb = new StringBuilder();
        Mailx.joinHeaders(sb, this.attrs, "");
        return sb.toString();
    }

    private String dumpHeaders() {
        StringBuilder sb = new StringBuilder();
        Mailx.joinHeaders(sb, this.headers, "");
        return sb.toString();
    }

    public String dumpString(boolean showHeader) {
        String HR = Ws.repeat('-', 40);
        String HR2 = Ws.repeat('.', 40);
        List<String> ss = Wlang.list(String.format("%s Email", this.getType().name()));
        ss.add(HR);
        ss.add(String.format("[%s]%s %s",
                             number,
                             messageId,
                             Wtime.formatDateTime(new Date(this.receiveAt))));
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
        ss.add("From: " + sender);
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getReceiveAtDateStr() {
        Date d = getReceiveAtDate();
        return Wtime.format(d, "yyyy-MM-dd HH:mm:ss");
    }

    public Date getReceiveAtDate() {
        return new Date(receiveAt);
    }

    public long getReceiveAt() {
        return receiveAt;
    }

    public void setReceiveAt(long receiveAt) {
        this.receiveAt = receiveAt;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String from) {
        this.sender = from;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = Mailx.evalContentType(contentType, this.attrs);
    }

}
