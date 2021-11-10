package org.nutz.walnut.ooml;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nutz.json.JsonField;
import org.nutz.lang.Encoding;
import org.nutz.lang.util.LinkedByteBuffer;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wpath;

public class OomlEntry {

    private WnRace race;

    private String type;

    private String path;

    private long size;

    @JsonField(ignore = true)
    private byte[] content;

    public OomlEntry() {}

    public OomlEntry(ZipEntry en, ZipInputStream zip, byte[] bs) throws IOException {
        this.path = en.getName();
        this.type = Wpath.getSuffixName(this.path);
        this.size = en.getSize();
        this.race = en.isDirectory() ? WnRace.DIR : WnRace.FILE;

        // 读取实体内容
        long maxCap = Math.max(this.size + 8192, 1024 * 10240);
        int readed;
        LinkedByteBuffer buf = new LinkedByteBuffer(8192, 10, (int) maxCap);
        while ((readed = zip.read(bs)) >= 0) {
            if (readed > 0) {
                buf.write(bs, 0, readed);
            }
        }
        this.content = buf.toArray();
    }

    public NutBean toBean() {
        NutMap bean = new NutMap();
        bean.put("race", this.race);
        bean.put("type", this.type);
        bean.put("path", this.path);
        bean.put("size", this.size);
        bean.put("content", this.content.length);
        return bean;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.isDir() ? "D" : "F");
        sb.append(':').append(type);
        sb.append(':').append(size);
        if (this.size != content.length) {
            sb.append('/').append(content.length);
        }
        sb.append(':').append(path);
        return sb.toString();
    }

    public boolean isXml() {
        return "xml".equals(this.type);
    }

    public boolean isRels() {
        return "rels".equals(this.type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRelsPath() {
        String name = Wpath.getName(path);
        return "word/_rels/" + name + ".rels";
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSuffixName() {
        return Wpath.getSuffixName(this.path);
    }

    public void renameSuffix(String suffixName) {
        this.path = Wpath.renameSuffix(path, suffixName);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long len) {
        this.size = len;
    }

    public boolean isDir() {
        return WnRace.DIR == this.race;
    }

    public boolean isFile() {
        return WnRace.FILE == this.race;
    }

    public WnRace getRace() {
        return race;
    }

    public void setRace(WnRace race) {
        this.race = race;
    }

    public String getContentStr(Charset encoding) {
        return new String(this.content, encoding);
    }

    public String getContentStr() {
        return new String(this.content, Encoding.CHARSET_UTF8);
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
