package com.site0.walnut.ext.data.archive.util;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.archive.api.ArchiveExtracting;
import com.site0.walnut.ext.data.archive.impl.TarArchiveExtracting;
import com.site0.walnut.ext.data.archive.impl.TgzArchiveExtracting;
import com.site0.walnut.ext.data.archive.impl.ZipArchiveExtracting;

public abstract class Archives {

    public static ArchiveExtracting extract(String type,
                                            InputStream ins,
                                            Charset charset) {
        // ZIP
        if ("zip".equals(type)) {
            return new ZipArchiveExtracting(ins, charset);
        }
        // TAR
        else if ("tar".equals(type)) {
            return new TarArchiveExtracting(ins, charset);
        }
        // TGZ | tar.gz
        else if ("tar.gz".equals(type) || "tgz".equals(type)) {
            return new TgzArchiveExtracting(ins, charset);
        }

        throw Er.create("e.io.archive.extract.UnsupportType", type);
    }

    public static Instant T(FileTime ft) {
        if (null == ft) {
            return null;
        }
        return ft.toInstant();
    }

    public static Instant TD(Date d) {
        if (null == d) {
            return null;
        }
        return Instant.ofEpochMilli(d.getTime());
    }

}
