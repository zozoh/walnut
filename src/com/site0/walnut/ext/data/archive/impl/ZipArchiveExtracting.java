package com.site0.walnut.ext.data.archive.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;

import com.site0.walnut.ext.data.archive.api.ArchiveEntry;
import com.site0.walnut.ext.data.archive.api.ArchiveExtracting;
import com.site0.walnut.ext.data.archive.api.EachArchive;
import static com.site0.walnut.ext.data.archive.util.Archives.T;
import com.site0.walnut.util.Wlang;

public class ZipArchiveExtracting implements ArchiveExtracting {

    private InputStream ins;
    private ZipInputStream zip;

    public ZipArchiveExtracting(InputStream ins) {
        this(ins, null);
    }

    public ZipArchiveExtracting(InputStream ins, Charset charset) {
        if (null == charset) {
            charset = Encoding.CHARSET_UTF8;
        }
        this.ins = ins;
        this.zip = new ZipInputStream(ins, charset);
    }

    @Override
    public int extract(EachArchive each) throws IOException {
        int count = 0;
        try {
            ZipEntry en;
            while ((en = zip.getNextEntry()) != null) {
                if (null != each) {
                    ArchiveEntry a = new ArchiveEntry();
                    long ams = en.getTime();
                    Instant dft = Instant.ofEpochMilli(ams);
                    FileTime ct = en.getCreationTime();
                    FileTime lm = en.getLastModifiedTime();
                    Instant lmi = Wlang.fallback(T(lm), T(ct), dft);

                    a.setName(en.getName());
                    a.setLen(en.getSize());
                    a.setDir(en.isDirectory());
                    a.setLastModified(lmi);
                    each.invoke(count, a, zip);
                }
                count++;
            }
        }
        finally {
            Streams.safeClose(zip);
            Streams.safeClose(ins);
        }
        return count;
    }

}
