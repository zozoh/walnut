package com.site0.walnut.ext.net.mailx.bean;

public class WnMailAttachment {

    private String name;

    private String mime;

    private byte[] content;

    public WnMailAttachment() {}

    public WnMailAttachment(WnMailAttachment at) {
        this(at.getName(), at.getMime(), at.getContent());
    }

    public WnMailAttachment(String name, String mime, byte[] content) {
        this.name = name;
        this.mime = mime;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public WnMailAttachment clone() {
        WnMailAttachment re = new WnMailAttachment();
        re.name = this.name;
        re.mime = this.mime;
        re.content = this.content;
        return this;
    }

}
