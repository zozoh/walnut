package org.nutz.walnut.ext.net.imap.bean;

import java.io.IOException;
import java.io.InputStream;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.nutz.json.JsonField;
import org.nutz.walnut.util.Ws;

public class WnEmailPart {

    private WnEmailHeaders headers;

    private String contentType;

    private String disposition;

    private String description;

    private String fileName;

    private int lineCount;

    private int size;

    private WnEmailPart[] children;

    @JsonField(ignore = true)
    private InputStream stream;

    private String content;

    public WnEmailPart() {};

    public WnEmailPart(String input) {
        this.contentType = "text/plain";
        this.size = input.getBytes().length;
        this.lineCount = Ws.countChar(input, '\n');
        this.content = input;
    }

    public WnEmailPart(BodyPart body) throws MessagingException, IOException {
        valueOf(body);
    }

    public void valueOf(BodyPart body) throws MessagingException, IOException {
        this.headers = new WnEmailHeaders(body);
        this.disposition = body.getDisposition();
        this.description = body.getDescription();
        this.fileName = body.getFileName();
        if (null != this.fileName) {
            this.fileName = MimeUtility.decodeText(this.fileName);
        }
        this.lineCount = body.getLineCount();
        this.size = body.getSize();
        valueOfContentType(body.getContentType());

        Object data = body.getContent();
        //
        // 嵌套结构
        //
        if (data instanceof MimeMultipart) {
            MimeMultipart subParts = (MimeMultipart) data;
            int len = subParts.getCount();
            this.children = new WnEmailPart[len];
            for (int i = 0; i < len; i++) {
                BodyPart subBody = subParts.getBodyPart(i);
                this.children[i] = new WnEmailPart(subBody);
            }
        }
        // 输入流
        else if (data instanceof InputStream) {
            this.stream = (InputStream) data;
        }
        // 普通文字
        else {
            this.content = data.toString();
        }
    }

    private void valueOfContentType(String ct) {
        int pos = ct.indexOf(';');
        if (pos > 0) {
            ct = ct.substring(0, pos).trim();
        }
        this.contentType = ct;
    }

    public WnEmailHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(WnEmailHeaders headers) {
        this.headers = headers;
    }

    public boolean isContentType(String prefix) {
        if (null == contentType) {
            return false;
        }
        if (prefix.startsWith("^")) {
            return this.contentType.matches(prefix);
        }
        return this.contentType.startsWith(prefix);
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean hasFileName() {
        return !Ws.isBlank(fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean hasChildren() {
        return null != children && children.length > 0;
    }

    public WnEmailPart[] getChildren() {
        return children;
    }

    public void setChildren(WnEmailPart[] children) {
        this.children = children;
    }

    public boolean hasStream() {
        return null != stream;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public boolean hasContent() {
        return null != content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
