package org.nutz.walnut.ext.net.imap.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import org.nutz.json.JsonField;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wtime;

public class WnEmailMessage {

    private int number;

    private int contentLength;

    private String folder;

    private WnEmailHeaders headers;

    private String subject;

    private WnEmailAddress[] from;

    private WnEmailAddress[] to;

    private WnEmailAddress[] replyTo;

    private String contentType;

    private String content;

    private WnEmailPart[] attachment;

    @JsonField(ignore = true)
    private WnEmailPart[] parts;

    private Date receivedDate;

    public WnEmailMessage() {}

    public WnEmailMessage(Message msg) throws MessagingException, IOException {
        this.number = msg.getMessageNumber();
        this.contentLength = msg.getSize();
        this.folder = msg.getFolder().getFullName();
        this.headers = new WnEmailHeaders(msg);
        this.subject = msg.getSubject();
        this.from = _to_address(msg.getFrom());
        this.to = _to_address(msg.getAllRecipients());
        this.replyTo = _to_address(msg.getReplyTo());
        this.contentType = msg.getContentType();
        this.receivedDate = msg.getReceivedDate();
        //
        // 处理内容
        //
        Object data = msg.getContent();
        if (data instanceof MimeMultipart) {
            MimeMultipart parts = (MimeMultipart) data;
            int len = parts.getCount();
            this.parts = new WnEmailPart[len];
            for (int i = 0; i < len; i++) {
                BodyPart subBody = parts.getBodyPart(i);
                this.parts[i] = new WnEmailPart(subBody);
            }
        }
        // 普通文本
        else {
            WnEmailPart part = new WnEmailPart(data.toString());
            parts = Wlang.array(part);
        }
        //
        // 根据解析出来的内容，分析内容
        //
        WnEmailPart contentPart = this.findContentPart(this.parts);
        if (null != contentPart) {
            this.contentType = contentPart.getContentType();
            this.content = contentPart.getContent();
        }
        //
        // 附件
        //
        this.attachment = this.findAttachmentParts(this.parts);
    }

    private WnEmailPart findContentPart(WnEmailPart[] parts) {
        WnEmailPart re = null;
        if (null != parts && parts.length > 0) {
            for (WnEmailPart part : parts) {
                // 递归
                if (part.hasChildren()) {
                    re = findContentPart(part.getChildren());
                }
                // 判断自身
                else if (part.isContentType("text/")) {
                    re = part;
                }
            }
        }
        return re;
    }

    private WnEmailPart[] findAttachmentParts(WnEmailPart[] parts) {
        List<WnEmailPart> list = new ArrayList<>(parts.length);
        __join_attachment_parts(list, parts);
        WnEmailPart[] re = new WnEmailPart[list.size()];
        return list.toArray(re);
    }

    private void __join_attachment_parts(List<WnEmailPart> list, WnEmailPart[] parts) {
        if (null != parts && parts.length > 0) {
            for (WnEmailPart part : parts) {
                // 递归
                if (part.hasChildren()) {
                    __join_attachment_parts(list, part.getChildren());
                }
                // 判断自身
                else if (part.hasFileName() && part.hasStream()) {
                    list.add(part);
                }
            }
        }
    }

    private WnEmailAddress[] _to_address(Address[] addrs) {
        WnEmailAddress[] re = new WnEmailAddress[addrs.length];
        for (int i = 0; i < addrs.length; i++) {
            InternetAddress addr = (InternetAddress) addrs[i];
            re[i] = new WnEmailAddress(addr);
        }
        return re;
    }

    private List<NutBean> _to_address_bean(WnEmailAddress[] addrs) {
        List<NutBean> re = new ArrayList<>(addrs.length);
        for (int i = 0; i < addrs.length; i++) {
            re.add(addrs[i].toBean());
        }
        return re;
    }

    public String getMessageId() {
        if (null != this.headers) {
            String id = this.headers.getString("Message-ID");
            if (null != id) {
                id = id.trim();
                if (Ws.isQuoteBy(id, '<', '>')) {
                    return id.substring(1, id.length() - 1).trim();
                }
                return id;
            }
        }
        return null;
    }

    public Object get(String key) {
        switch (key) {
        case "num":
        case "number":
            return this.number;
        case "subject":
            return this.subject;
        case "folder":
            return this.folder;
        case "from":
            return Ws.join(this.from, ",");
        case "to":
            return Ws.join(this.to, ",");
        case "reply":
            return Ws.join(this.replyTo, ",");
        case "time":
            return Wtime.format(this.receivedDate, "yyyy-MM-dd HH:mm");
        case "mime":
        case "contentType":
            return this.contentType;
        case "brief":
            if (this.content.length() > 10) {
                return this.content.substring(0, 10) + "...";
            }
            return this.content;
        case "content":
            return this.content;
        case "attachment":
            List<String> names = new LinkedList<>();
            if (this.hasAttachment()) {
                for (WnEmailPart atta : this.attachment) {
                    names.add(atta.getFileName() + "(" + Ws.sizeText(atta.getSize(), false) + ")");
                }
            }
            return Ws.join(names, "; ");
        case "attachmentCount":
        case "ac":
            if (this.hasAttachment()) {
                return this.attachment.length;
            }
            return 0;
        }
        return null;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public WnEmailHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(WnEmailHeaders headers) {
        this.headers = headers;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<NutBean> getFromBeans() {
        return this._to_address_bean(from);
    }

    public WnEmailAddress[] getFrom() {
        return from;
    }

    public void setFrom(WnEmailAddress[] from) {
        this.from = from;
    }

    public List<NutBean> getToBeans() {
        return this._to_address_bean(to);
    }

    public WnEmailAddress[] getTo() {
        return to;
    }

    public void setTo(WnEmailAddress[] to) {
        this.to = to;
    }

    public List<NutBean> getReplayToBeans() {
        return this._to_address_bean(replyTo);
    }

    public WnEmailAddress[] getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(WnEmailAddress[] replyTo) {
        this.replyTo = replyTo;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean hasContent() {
        return null != this.content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean hasAttachment() {
        return null != attachment && attachment.length > 0;
    }

    public WnEmailPart[] getAttachment() {
        return attachment;
    }

    public void setAttachment(WnEmailPart[] attachment) {
        this.attachment = attachment;
    }

    public boolean hasParts() {
        return null != parts && parts.length > 0;
    }

    public WnEmailPart[] getParts() {
        return parts;
    }

    public void setParts(WnEmailPart[] parts) {
        this.parts = parts;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

}
