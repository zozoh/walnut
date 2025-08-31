package com.site0.walnut.ext.data.archive.api;

import java.time.Instant;

import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wtime;

public class ArchiveEntry {

    private String name;

    private long len;

    private boolean dir;

    private Instant lastModified;

    public String toString() {
        String jfmt = "yyyy-MM-dd HH:mm:ss";
        String lm = Wtime.formatUTC(lastModified, jfmt);
        String D = this.dir ? "D" : "F";

        return String.format("%s %8d Bytes [%s] %s", D, len, lm, name);
    }

    public ArchiveEntry clone() {
        ArchiveEntry re = new ArchiveEntry();
        re.name = this.name;
        re.len = this.len;
        re.dir = this.dir;
        re.lastModified = this.lastModified;
        return re;
    }

    public WnRace getRace() {
        if (this.dir) {
            return WnRace.DIR;
        }
        return WnRace.FILE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLen() {
        return len;
    }

    public void setLen(long len) {
        this.len = len;
    }

    public boolean isDir() {
        return dir;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

}
