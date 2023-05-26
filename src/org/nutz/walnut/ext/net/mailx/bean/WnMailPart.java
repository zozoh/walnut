package org.nutz.walnut.ext.net.mailx.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.bouncycastle.util.io.Streams;
import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutMap;

import jakarta.mail.BodyPart;
import jakarta.mail.Header;
import jakarta.mail.MessagingException;

public class WnMailPart {

    private String contentType;

    private NutMap headers;

    private byte[] data;

    public WnMailPart(BodyPart part) throws MessagingException, IOException {
        this.contentType = part.getContentType();
        this.headers = new NutMap();
        Enumeration<Header> ih = part.getAllHeaders();
        while (ih.hasMoreElements()) {
            Header h = ih.nextElement();
            this.headers.addv(h.getName(), h.getValue());
        }
        InputStream ins = part.getInputStream();
        data = Streams.readAll(ins);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Content Type: %s\n", contentType));
        sb.append("Headers:").append(Json.toJson(headers));
        sb.append("\nDATA: ").append(String.format("%d bytes", data.length)).append("\n");
        sb.append(new String(data, Encoding.CHARSET_UTF8));
        return sb.toString();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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

}
