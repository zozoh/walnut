package com.site0.walnut.util.range;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.site0.walnut.util.RandomAccessFileInputStream;
import com.site0.walnut.util.Wn;

public class WnGetLocalFileInputStream implements WnGetInputStream {

    private File f;

    private String sha1;

    public WnGetLocalFileInputStream(File f, String sha1) {
        this.f = f;
        this.sha1 = sha1;
    }

    @Override
    public InputStream getStream(long offset) throws IOException {
        RandomAccessFileInputStream rfs = new RandomAccessFileInputStream(f);
        if (offset > 0) {
            rfs.skip(offset);
        }
        return rfs;
    }

    @Override
    public long getContentLenth() {
        return f.length();
    }

    @Override
    public String getETag() {
        return Wn.getEtag(sha1, f);
    }

}
