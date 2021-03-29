package org.nutz.walnut.util.archive.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nutz.lang.Streams;
import org.nutz.walnut.util.archive.WnArchiveEntry;

public class WnZipArchiveReading extends AsbatractWnAchiveRading {

    private InputStream ins;
    private ZipInputStream zip;

    public WnZipArchiveReading(InputStream ins) {
        this.zip = new ZipInputStream(ins);
    }

    @Override
    public int readAll() throws IOException {
        int entryCount = 0;
        ZipEntry en;
        try {
            while ((en = zip.getNextEntry()) != null) {
                WnArchiveEntry ae = new WnArchiveEntry();
                ae.name = en.getName();
                ae.len = en.getSize();
                ae.dir = en.isDirectory();
                entryCount++;

                this.callback.invoke(ae, zip);
            }

        }
        finally {
            Streams.safeClose(zip);
            Streams.safeClose(ins);
        }
        return entryCount;
    }

}
