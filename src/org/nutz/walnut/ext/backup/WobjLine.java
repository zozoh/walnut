package org.nutz.walnut.ext.backup;

public class WobjLine {
    public String id;
    public String path;
    public String obj_sha1;
    public String fdata_sha1;
    public transient BackupPackage pkg; // 从哪里可以找到这个sha1数据呢? 标记一下

    public WobjLine() {}

    public WobjLine(String line) {
        String[] tmp = line.split("[\\:]");
        id = tmp[0];
        path = tmp[1];
        obj_sha1 = tmp[2];
        if (tmp.length > 3)
            fdata_sha1 = tmp[3];
    }
}