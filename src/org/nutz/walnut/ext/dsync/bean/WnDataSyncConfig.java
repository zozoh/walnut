package org.nutz.walnut.ext.dsync.bean;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.nutz.walnut.util.archive.WnArchiveWriting;
import org.nutz.walnut.util.archive.impl.WnTarArchiveWriting;
import org.nutz.walnut.util.archive.impl.WnZipArchiveWriting;

public class WnDataSyncConfig {

    private String name;

    private String archiveType;

    private List<WnDataSyncDir> dirs;

    public WnArchiveWriting createArchiveGenerating(OutputStream ops) throws IOException {
        // Tar
        if ("tar".equals(this.archiveType)) {
            return new WnTarArchiveWriting(ops);
        }
        // Gzip
        else if ("tar.gz".equals(archiveType) || "tgz".equals(archiveType)) {
            GZIPOutputStream gzip = new GZIPOutputStream(ops);
            return new WnTarArchiveWriting(gzip);
        }

        // 默认是 Zip
        return new WnZipArchiveWriting(ops);
    }

    public void checkDirKeys() {
        if (null != dirs) {
            int i = 0;
            for (WnDataSyncDir dir : dirs) {
                if (!dir.hasKey()) {
                    dir.setKey("p" + i);
                }
                i++;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(String archiveType) {
        this.archiveType = archiveType;
    }

    public boolean hasDirs() {
        return null != dirs && dirs.size() > 0;
    }

    public int countDirs() {
        return null == dirs ? 0 : dirs.size();
    }

    public List<WnDataSyncDir> getDirs() {
        return dirs;
    }

    public void setDirs(List<WnDataSyncDir> dirs) {
        this.dirs = dirs;
    }

}
