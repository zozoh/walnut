package org.nutz.walnut.ext.net.mailx.bean;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.bouncycastle.util.io.Streams;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Ws;

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

    private NutMap headers;

    private byte[] data;

    private List<WnMailPart> parts;

    /**
     * 文本模式下，邮件段的正文内容
     */
    private String content;

    public WnMailPart(BodyPart part) throws MessagingException, IOException {
        this.attrs = new NutMap();
        this.setContentType(part.getContentType());

        // 加载头
        this.headers = new NutMap();
        Enumeration<Header> ih = part.getAllHeaders();
        while (ih.hasMoreElements()) {
            Header h = ih.nextElement();
            this.headers.addv(h.getName(), h.getValue());
        }
        // 这是一个包裹父类，里面混合了超文本以及纯文本内容
        if (this.isAlternative()) {
            Object body = part.getContent();
            if (body instanceof MimeMultipart) {
                MimeMultipart mparts = (MimeMultipart) body;
                int n = mparts.getCount();
                this.parts = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    BodyPart sub = mparts.getBodyPart(i);
                    WnMailPart mp = new WnMailPart(sub);
                    this.parts.add(mp);

                }
            } else {
                this.content = body.toString();
            }
        }
        // 其他的直接加载二进制内容
        else {
            String fnm = part.getFileName();
            if (null != fnm) {
                this.fileName = MimeUtility.decodeText(fnm);
            }
            InputStream ins = part.getInputStream();
            data = Streams.readAll(ins);
        }

        // 如果是文本内容，也转换一遍文本
        if (this.isText()) {
            this.content = new String(this.data, this.getCharset());
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
        sb.append(String.format("\n%s[ATTR:]", prefix));
        joinHeaders(sb, this.attrs, prefix);
        sb.append(String.format("\n%s[HEAD:]", prefix));
        joinHeaders(sb, this.headers, prefix);

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
            if (this.isText()) {
                sb.append("\n").append(this.content);
            }
        }
    }

    static void joinHeaders(StringBuilder sb, NutMap map, String prefix) {
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            sb.append("\n").append(prefix).append(" - ").append(key).append(": ");
            if (null != val) {
                if (val instanceof CharSequence) {
                    sb.append(val.toString());
                } else {
                    sb.append(Json.toJson(val));
                }
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
        return "application/octet-stream".equals(this.contentType);
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = Ws.evalContentType(contentType, this.attrs);
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
