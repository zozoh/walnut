package org.nutz.walnut.util.upload;

import org.nutz.lang.Encoding;

public class HttpFormFile {

    private String name;

    private String contentType;

    private byte[] bytes;

    public HttpFormFile() {}

    public HttpFormFile(FormField fld, byte[] bytes) {
        this(fld.getFileName(), fld.getContentType(), bytes);
    }

    public HttpFormFile(String name, String contentType, byte[] bytes) {
        this.name = name;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getContent() {
        return new String(bytes, Encoding.CHARSET_UTF8);
    }

    public void setBytes(byte[] content) {
        this.bytes = content;
    }

}
