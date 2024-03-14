package com.site0.walnut.ext.net.mailx.bean;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.util.io.Streams;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.mailx.util.Mailx;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

import jakarta.mail.BodyPart;
import jakarta.mail.Header;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;

public class WnMailPart {

    private String contentType;

    /**
     * 从 contentType 里获取的属性，譬如 charset 或者 name 或者 boundary
     */
    private NutMap attrs;

    /**
     * 如果是下载附件，或者内嵌文件，这个则是解码后的文件名
     */
    private String fileName;

    /**
     * 根据头里面的 <code>Content-Disposition</code>来判断是否是附件 <br>
     * <code>Content-Disposition: attachment; filename="=?gb18030?B?uL28/i50eHQ=?="</code>
     */
    private boolean attachment;

    private NutMap headers;

    private byte[] data;

    private List<WnMailPart> parts;

    /**
     * 文本模式下，邮件段的正文内容
     */
    private String content;

    /**
     * @param part
     *            邮件的数据段
     * @param asContent
     *            一个 AutoMatch （可以是字符串或者正则）指定某种 contentType 强制转换为正文内容
     * @throws MessagingException
     * @throws IOException
     */
    public WnMailPart(BodyPart part, String asContent) throws MessagingException, IOException {
        this.attrs = new NutMap();
        this.setContentType(part.getContentType());

        // 加载文件名称（如果是附件的话）
        String fnm = part.getFileName();
        if (null != fnm) {
            this.fileName = MimeUtility.decodeText(fnm);
        }

        // 加载头
        this.headers = new NutMap();
        Enumeration<Header> ih = part.getAllHeaders();
        while (ih.hasMoreElements()) {
            Header h = ih.nextElement();
            String name = h.getName();
            String value = h.getValue();
            this.headers.addv(name, value);
            //
            // 对于附件的头信息，特殊处理，提取出解码后的文件名
            //
            if ("Content-Disposition".equalsIgnoreCase(name)) {
                NutMap props = new NutMap();
                String cdType = Mailx.evalContentType(value, props);
                this.attachment = "attachment".equals(cdType);
                fnm = props.getString("filename");
                this.fileName = MimeUtility.decodeText(fnm);
            }
        }
        // 这是一个包裹父类，里面混合了超文本以及纯文本内容
        Object body = part.getContent();
        if (body instanceof MimeMultipart) {
            MimeMultipart mparts = (MimeMultipart) body;
            int n = mparts.getCount();
            this.parts = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                BodyPart sub = mparts.getBodyPart(i);
                WnMailPart mp = new WnMailPart(sub, asContent);
                this.parts.add(mp);

            }
        }
        // 其他的直接加载二进制内容
        else {
            InputStream ins = part.getInputStream();
            data = Streams.readAll(ins);
        }

        // 如果是文本内容，也转换一遍文本
        if (this.isText() || this.isContentType(asContent)) {
            Charset c = this.getCharset();
            String text = new String(this.data, c);
            this.setContent(text);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        joinString(sb, "");
        return sb.toString();
    }

    protected void joinString(StringBuilder sb, String prefix) {
        sb.append(String.format("%s[Content Type]: %s", prefix, contentType));
        if (this.isAttachment()) {
            sb.append(String.format("\n%s[Attachment]: %s", prefix, this.fileName));
        }
        sb.append(String.format("\n%s[ATTR]:", prefix));
        Mailx.joinHeaders(sb, this.attrs, prefix);
        sb.append(String.format("\n%s[HEAD]:", prefix));
        Mailx.joinHeaders(sb, this.headers, prefix);

        if (this.isAlternative()) {
            String HR = Ws.repeat('~', 60);
            String px = ">>> ";
            int N = this.parts.size();
            for (int i = 0; i < this.parts.size(); i++) {
                sb.append("\n").append(px).append(HR);
                sb.append("\n").append(px).append(String.format("%d/%d) SUB PART", i + 1, N));
                sb.append("\n").append(px).append(HR).append("\n");
                WnMailPart sub = this.parts.get(i);
                sub.joinString(sb, prefix + px);
            }
            sb.append("\n").append(px).append(HR).append(N).append(" SUB PARTS");
        }
        // 直接显示内容
        else {
            sb.append(String.format("\n%s<DATA: %s>",
                                    prefix,
                                    String.format("%d bytes", data.length)));
            if (null != this.content) {
                sb.append("\n").append(this.content);
            }
        }
    }

    public Charset getCharset() {
        String cs = this.attrs.getString("charset", "UTF-8");
        return Charset.forName(cs);
    }

    public boolean isAlternative() {
        return "multipart/alternative".equals(this.contentType);
    }

    public boolean isText() {
        return this.contentType.startsWith("text/");
    }

    public boolean isAttachment() {
        return this.attachment;
    }

    public void joinContentPart(List<WnMailPart> list, String asContent) {
        if (this.isText() || this.isContentType(asContent)) {
            list.add(this);
        }
        // 还有子块，递归
        else if (null != parts) {
            for (WnMailPart sub : parts) {
                sub.joinContentPart(list, asContent);
            }
        }
    }

    public void joinAttachmentPart(List<WnMailPart> list, String asContent) {
        if (this.isAttachment() && !this.isContentType(asContent)) {
            list.add(this);
        }
        // 还有子块，递归
        else if (null != parts) {
            for (WnMailPart sub : parts) {
                sub.joinAttachmentPart(list, asContent);
            }
        }
    }

    public boolean isContentType(String contentType) {
        if (null == contentType) {
            return false;
        }
        WnMatch wm = AutoMatch.parse(contentType.toLowerCase(), false);
        return wm.match(this.contentType.toLowerCase());
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = Mailx.evalContentType(contentType, this.attrs);
    }

    public NutMap getHeaders() {
        return headers;
    }

    public void setHeaders(NutMap headers) {
        this.headers = headers;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
