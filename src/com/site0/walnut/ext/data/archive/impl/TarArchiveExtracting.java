package com.site0.walnut.ext.data.archive.impl;

import static com.site0.walnut.ext.data.archive.util.Archives.TD;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;

import com.site0.walnut.ext.data.archive.api.ArchiveEntry;
import com.site0.walnut.ext.data.archive.api.ArchiveExtracting;
import com.site0.walnut.ext.data.archive.api.EachArchive;
import com.site0.walnut.util.Wlang;

public class TarArchiveExtracting implements ArchiveExtracting {

    private InputStream ins;
    private TarArchiveInputStream tar;

    public TarArchiveExtracting(InputStream ins) {
        this(ins, null);
    }

    public TarArchiveExtracting(InputStream ins, Charset charset) {
        if (null == charset) {
            charset = Encoding.CHARSET_UTF8;
        }
        this.ins = ins;
        this.tar = new TarArchiveInputStream(ins, charset.name());
    }

    @Override
    public int extract(EachArchive each) throws IOException {
        int count = 0;
        try {
            TarArchiveEntry en;
            while ((en = tar.getNextTarEntry()) != null) {
                if (null != each) {
                    ArchiveEntry a = new ArchiveEntry();
                    Date mo = en.getModTime();
                    Date lm = en.getLastModifiedDate();
                    Instant lmi = Wlang.fallback(TD(lm), TD(mo));
                    
                    a.setName(en.getName());
                    a.setLen(en.getSize());
                    a.setDir(en.isDirectory());
                    a.setLastModified(lmi);
                    
                    each.invoke(count, a, tar);
                }
                count++;
            }
        }
        finally {
            Streams.safeClose(tar);
            Streams.safeClose(ins);
        }
        return count;
    }
}